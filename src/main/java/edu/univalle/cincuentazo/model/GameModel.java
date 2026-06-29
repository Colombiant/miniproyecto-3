package edu.univalle.cincuentazo.model;

import edu.univalle.cincuentazo.exception.EmptyDeckException;
import edu.univalle.cincuentazo.exception.GameException;
import edu.univalle.cincuentazo.exception.GameStateException;
import edu.univalle.cincuentazo.exception.InvalidMoveException;
import edu.univalle.cincuentazo.exception.NoPlayableCardException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/**
 * Central model that owns all Cincuentazo rules and state transitions.
 */
public class GameModel {
    public static final int TABLE_LIMIT = 50;
    public static final int HAND_SIZE = 4;
    public static final int MIN_MACHINE_PLAYERS = 1;
    public static final int MAX_MACHINE_PLAYERS = 3;

    private final Random random;
    private final List<Player> players;
    private final List<Card> tablePile;
    private final List<GameEventListener> listeners;
    private final Map<Player, Integer> playedCardCounts;
    private Deck deck;
    private int tableSum;
    private int currentPlayerIndex;
    private boolean awaitingDraw;
    private boolean gameStarted;
    private boolean gameOver;
    private Player winner;

    /**
     * Creates a model with a random generator.
     */
    public GameModel() {
        this(new Random());
    }

    /**
     * Creates a model with a provided random generator for deterministic tests.
     *
     * @param random random generator
     */
    public GameModel(Random random) {
        this.random = Objects.requireNonNull(random, "random must not be null");
        this.players = new ArrayList<>();
        this.tablePile = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.playedCardCounts = new LinkedHashMap<>();
        this.deck = new Deck();
    }

    /**
     * Starts a new game with the selected number of machine players.
     *
     * @param machinePlayerCount number of machine players
     * @throws GameException when the selected player count is invalid or cards are missing
     */
    public void startNewGame(int machinePlayerCount) throws GameException {
        if (machinePlayerCount < MIN_MACHINE_PLAYERS || machinePlayerCount > MAX_MACHINE_PLAYERS) {
            throw new InvalidMoveException("The game supports only one, two, or three machine players.");
        }

        players.clear();
        tablePile.clear();
        playedCardCounts.clear();
        deck = Deck.standardShuffled(random);
        tableSum = 0;
        currentPlayerIndex = 0;
        awaitingDraw = false;
        gameStarted = true;
        gameOver = false;
        winner = null;

        players.add(new HumanPlayer("Jugador humano"));
        for (int index = 1; index <= machinePlayerCount; index++) {
            players.add(new MachinePlayer("Maquina " + index));
        }
        for (Player player : players) {
            playedCardCounts.put(player, 0);
        }

        for (int round = 0; round < HAND_SIZE; round++) {
            for (Player player : players) {
                player.receiveCard(deck.draw());
            }
        }

        Card initialCard = deck.draw();
        tablePile.add(initialCard);
        tableSum = initialCard.getInitialTableValue();
        notifyMessage("La carta inicial es " + initialCard.getShortName() + ". Suma inicial: " + tableSum + ".");
        resolveBlockedCurrentPlayers();
        notifyStateChanged();
    }

    /**
     * Adds an observer to receive model events.
     *
     * @param listener listener to add
     */
    public void addGameEventListener(GameEventListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener must not be null"));
    }

    /**
     * Plays a card from the current player's hand.
     *
     * @param handIndex selected hand index
     * @return card that was played
     * @throws GameException when the movement is not legal
     */
    public Card playCard(int handIndex) throws GameException {
        return playCard(handIndex, null);
    }

    /**
     * Plays a card and optionally requests a specific value for a flexible card.
     *
     * @param handIndex selected hand index
     * @param requestedFlexibleValue requested value for aces, or null for automatic resolution
     * @return card that was played
     * @throws GameException when the movement is not legal
     */
    public Card playCard(int handIndex, Integer requestedFlexibleValue) throws GameException {
        ensureGameCanReceiveAction();
        if (awaitingDraw) {
            throw new GameStateException("The current player must draw before another card is played.");
        }

        Player player = getCurrentPlayer();
        if (!player.hasPlayableCard(tableSum, TABLE_LIMIT)) {
            throw new NoPlayableCardException(player.getName() + " has no playable cards.");
        }
        List<Card> hand = player.getHand();
        if (handIndex < 0 || handIndex >= hand.size()) {
            throw new InvalidMoveException("The selected card does not exist.");
        }

        Card selectedCard = hand.get(handIndex);
        if (!selectedCard.canBePlayed(tableSum, TABLE_LIMIT)) {
            throw new InvalidMoveException("That card would exceed the table limit of 50.");
        }

        int value = resolveCardValue(selectedCard, requestedFlexibleValue);
        Card playedCard = player.removeCard(handIndex);
        tablePile.add(playedCard);
        tableSum += value;
        playedCardCounts.merge(player, 1, Integer::sum);
        awaitingDraw = true;
        notifyMessage(player.getName() + " juega " + playedCard.getShortName() + " (" + signedValue(value) + ").");
        notifyStateChanged();
        return playedCard;
    }

    private int resolveCardValue(Card selectedCard, Integer requestedFlexibleValue) throws InvalidMoveException {
        if (!selectedCard.isFlexible() || requestedFlexibleValue == null) {
            return selectedCard.resolvePlayValue(tableSum, TABLE_LIMIT);
        }
        if (requestedFlexibleValue != 1 && requestedFlexibleValue != 10) {
            throw new InvalidMoveException("A flexible card can only use value 1 or 10.");
        }
        if (tableSum + requestedFlexibleValue > TABLE_LIMIT) {
            throw new InvalidMoveException("The requested ace value would exceed the table limit of 50.");
        }
        return requestedFlexibleValue;
    }

    /**
     * Draws one card for the current player and advances the turn.
     *
     * @return drawn card
     * @throws GameException when no card can be drawn
     */
    public Card drawForCurrentPlayer() throws GameException {
        ensureGameCanReceiveAction();
        if (!awaitingDraw) {
            throw new GameStateException("A card can be drawn only after playing a card.");
        }

        rebuildDeckFromTableIfNeeded();
        Player player = getCurrentPlayer();
        Card drawnCard = deck.draw();
        player.receiveCard(drawnCard);
        notifyMessage(player.getName() + " toma una carta del mazo.");
        awaitingDraw = false;
        advanceToNextActivePlayer();
        resolveBlockedCurrentPlayers();
        notifyStateChanged();
        return drawnCard;
    }

    /**
     * Eliminates blocked players until the current player can act or the game ends.
     */
    public void resolveBlockedCurrentPlayers() {
        if (!gameStarted || gameOver || awaitingDraw || players.isEmpty()) {
            return;
        }

        int checkedPlayers = 0;
        while (!gameOver && checkedPlayers < players.size() && !getCurrentPlayer().hasPlayableCard(tableSum, TABLE_LIMIT)) {
            Player blockedPlayer = getCurrentPlayer();
            eliminateCurrentPlayer(blockedPlayer.getName() + " queda eliminado porque no tiene cartas jugables.");
            checkedPlayers++;
        }
        notifyStateChanged();
    }

    /**
     * Selects the card index chosen by the current machine player.
     *
     * @return selected index, or -1 if none is legal
     */
    public int chooseMachineCardIndex() {
        ensureGameStarted();
        return getCurrentPlayer().chooseCardIndex(tableSum, TABLE_LIMIT);
    }

    /**
     * Gets the current player.
     *
     * @return current player
     */
    public Player getCurrentPlayer() {
        ensureGameStarted();
        return players.get(currentPlayerIndex);
    }

    /**
     * Gets the human player when the game has started.
     *
     * @return optional human player
     */
    public Optional<Player> getHumanPlayer() {
        return players.stream().filter(Player::isHuman).findFirst();
    }

    /**
     * Gets all players in table order.
     *
     * @return players snapshot
     */
    public List<Player> getPlayers() {
        return List.copyOf(players);
    }

    /**
     * Gets the table pile from oldest to newest card.
     *
     * @return table pile snapshot
     */
    public List<Card> getTablePile() {
        return List.copyOf(tablePile);
    }

    /**
     * Gets how many cards each player has played during the current game.
     *
     * @return read-only player statistics map
     */
    public Map<Player, Integer> getPlayedCardCounts() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(playedCardCounts));
    }

    /**
     * Gets the amount of cards played by one player.
     *
     * @param player player to inspect
     * @return played card count
     */
    public int getPlayedCardCount(Player player) {
        return playedCardCounts.getOrDefault(player, 0);
    }

    /**
     * Gets the top table card.
     *
     * @return optional top card
     */
    public Optional<Card> getTopTableCard() {
        if (tablePile.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(tablePile.get(tablePile.size() - 1));
    }

    /**
     * Gets the current table sum.
     *
     * @return table sum
     */
    public int getTableSum() {
        return tableSum;
    }

    /**
     * Gets the remaining deck size.
     *
     * @return deck size
     */
    public int getDeckSize() {
        return deck.size();
    }

    /**
     * Gets the number of active players.
     *
     * @return active player count
     */
    public long getActivePlayerCount() {
        return players.stream().filter(Player::isActive).count();
    }

    /**
     * Indicates whether the current player must draw.
     *
     * @return true when waiting for a draw action
     */
    public boolean isAwaitingDraw() {
        return awaitingDraw;
    }

    /**
     * Indicates whether a game has started.
     *
     * @return true when started
     */
    public boolean isGameStarted() {
        return gameStarted;
    }

    /**
     * Indicates whether the game is finished.
     *
     * @return true when finished
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Gets the winner when the game is over.
     *
     * @return optional winner
     */
    public Optional<Player> getWinner() {
        return Optional.ofNullable(winner);
    }

    private void rebuildDeckFromTableIfNeeded() throws EmptyDeckException {
        if (!deck.isEmpty()) {
            return;
        }
        if (tablePile.size() <= 1) {
            throw new EmptyDeckException("There are no table cards available to rebuild the deck.");
        }

        Card topCard = tablePile.remove(tablePile.size() - 1);
        List<Card> recycledCards = new ArrayList<>(tablePile);
        tablePile.clear();
        tablePile.add(topCard);
        Collections.shuffle(recycledCards, random);
        deck.addToBottom(recycledCards);
        notifyMessage("El mazo se reconstruyo con las cartas de la mesa, excepto la ultima.");
    }

    private void eliminateCurrentPlayer(String message) {
        Player player = getCurrentPlayer();
        player.setActive(false);
        deck.addToBottom(player.releaseHand());
        notifyMessage(message);
        if (getActivePlayerCount() == 1) {
            winner = players.stream().filter(Player::isActive).findFirst().orElse(null);
            gameOver = true;
            if (winner != null) {
                currentPlayerIndex = players.indexOf(winner);
                notifyGameOver(winner);
            }
            return;
        }
        advanceToNextActivePlayer();
    }

    private void advanceToNextActivePlayer() {
        if (players.isEmpty() || getActivePlayerCount() == 0) {
            return;
        }

        int nextIndex = currentPlayerIndex;
        do {
            nextIndex = (nextIndex + 1) % players.size();
        } while (!players.get(nextIndex).isActive());
        currentPlayerIndex = nextIndex;
    }

    private void ensureGameCanReceiveAction() {
        ensureGameStarted();
        if (gameOver) {
            throw new GameStateException("The game is already over.");
        }
    }

    private void ensureGameStarted() {
        if (!gameStarted || players.isEmpty()) {
            throw new GameStateException("The game has not started.");
        }
    }

    private String signedValue(int value) {
        return value > 0 ? "+" + value : String.valueOf(value);
    }

    private void notifyStateChanged() {
        for (GameEventListener listener : List.copyOf(listeners)) {
            listener.onStateChanged(this);
        }
    }

    private void notifyMessage(String message) {
        for (GameEventListener listener : List.copyOf(listeners)) {
            listener.onMessage(message);
        }
    }

    private void notifyGameOver(Player winningPlayer) {
        notifyMessage("El ganador es " + winningPlayer.getName() + ".");
        for (GameEventListener listener : List.copyOf(listeners)) {
            listener.onGameOver(winningPlayer);
        }
    }
}
