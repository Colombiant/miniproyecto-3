package edu.univalle.cincuentazo.controller;

import edu.univalle.cincuentazo.exception.GameException;
import edu.univalle.cincuentazo.exception.GameStateException;
import edu.univalle.cincuentazo.model.Card;
import edu.univalle.cincuentazo.model.GameEventAdapter;
import edu.univalle.cincuentazo.model.GameModel;
import edu.univalle.cincuentazo.model.Player;
import edu.univalle.cincuentazo.view.CardSelectionListener;
import edu.univalle.cincuentazo.view.KeyboardShortcutAdapter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * JavaFX controller that binds the FXML view with the Cincuentazo model.
 */
public class GameController implements Initializable {
    private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int MACHINE_PLAY_MIN_MILLIS = 2_000;
    private static final int MACHINE_PLAY_MAX_MILLIS = 4_000;
    private static final int MACHINE_DRAW_MIN_MILLIS = 1_000;
    private static final int MACHINE_DRAW_MAX_MILLIS = 2_000;

    @FXML
    private BorderPane rootPane;
    @FXML
    private ToggleGroup machineCountGroup;
    @FXML
    private RadioButton oneMachineRadio;
    @FXML
    private RadioButton twoMachinesRadio;
    @FXML
    private RadioButton threeMachinesRadio;
    @FXML
    private Button startButton;
    @FXML
    private Button drawButton;
    @FXML
    private Label statusLabel;
    @FXML
    private Label tableSumLabel;
    @FXML
    private Label tableCardLabel;
    @FXML
    private Label tableCardDetailLabel;
    @FXML
    private Label deckCountLabel;
    @FXML
    private Label currentTurnLabel;
    @FXML
    private Label winnerLabel;
    @FXML
    private HBox humanHandBox;
    @FXML
    private VBox opponentsBox;
    @FXML
    private TextArea logArea;

    private final Random random = new Random();
    private final CardSelectionListener cardSelectionListener = this::playHumanCard;
    private GameModel model;
    private int gameVersion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rootPane.setFocusTraversable(true);
        oneMachineRadio.setUserData(1);
        twoMachinesRadio.setUserData(2);
        threeMachinesRadio.setUserData(3);
        oneMachineRadio.setSelected(true);

        model = createModel();
        startButton.setOnAction(event -> startGame());
        startButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> setStatus("Selecciona maquinas y comienza una partida."));
        drawButton.setOnAction(event -> drawCardForHuman());
        drawButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new DeckMouseHandler());
        rootPane.addEventHandler(KeyEvent.KEY_PRESSED, new GlobalKeyboardHandler());

        render();
        appendLog("Aplicacion lista. Selecciona la cantidad de maquinas.");
    }

    private GameModel createModel() {
        GameModel newModel = new GameModel();
        newModel.addGameEventListener(new UiGameEventAdapter());
        return newModel;
    }

    private void startGame() {
        gameVersion++;
        model = createModel();
        try {
            int machineCount = selectedMachineCount();
            model.startNewGame(machineCount);
            appendLog("Nueva partida con " + machineCount + " maquina(s).");
            render();
            continueTurnFlow();
            rootPane.requestFocus();
        } catch (GameException | GameStateException exception) {
            showError(exception.getMessage());
        }
    }

    private int selectedMachineCount() {
        Object userData = machineCountGroup.getSelectedToggle().getUserData();
        if (userData instanceof Integer count) {
            return count;
        }
        return 1;
    }

    private void playHumanCard(int handIndex) {
        if (!isHumanPlayAllowed()) {
            setStatus("Espera tu turno o toma una carta si ya jugaste.");
            return;
        }
        try {
            Integer preferredValue = requestHumanFlexibleValue(handIndex);
            if (preferredValue == null && isSelectedHumanCardFlexible(handIndex)) {
                return;
            }
            model.playCard(handIndex, preferredValue);
            render();
        } catch (GameException | GameStateException exception) {
            showError(exception.getMessage());
        }
    }

    private Integer requestHumanFlexibleValue(int handIndex) {
        Optional<Card> selectedCard = findHumanCard(handIndex);
        if (selectedCard.isEmpty() || !selectedCard.get().isFlexible()) {
            return null;
        }

        boolean oneIsValid = model.getTableSum() + 1 <= GameModel.TABLE_LIMIT;
        boolean tenIsValid = model.getTableSum() + 10 <= GameModel.TABLE_LIMIT;
        if (oneIsValid && tenIsValid) {
            ChoiceDialog<Integer> dialog = new ChoiceDialog<>(10, List.of(10, 1));
            dialog.setTitle("Valor del As");
            dialog.setHeaderText("Elige el valor para " + selectedCard.get().getShortName());
            dialog.setContentText("Valor:");
            return dialog.showAndWait().orElse(null);
        }
        return tenIsValid ? 10 : 1;
    }

    private boolean isSelectedHumanCardFlexible(int handIndex) {
        return findHumanCard(handIndex).map(Card::isFlexible).orElse(false);
    }

    private Optional<Card> findHumanCard(int handIndex) {
        if (!model.isGameStarted() || model.getHumanPlayer().isEmpty()) {
            return Optional.empty();
        }
        List<Card> hand = model.getHumanPlayer().orElseThrow().getHand();
        if (handIndex < 0 || handIndex >= hand.size()) {
            return Optional.empty();
        }
        return Optional.of(hand.get(handIndex));
    }

    private void drawCardForHuman() {
        if (!isHumanDrawAllowed()) {
            setStatus("Primero juega una carta valida.");
            return;
        }
        try {
            model.drawForCurrentPlayer();
            render();
            continueTurnFlow();
        } catch (GameException | GameStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void continueTurnFlow() {
        if (!model.isGameStarted() || model.isGameOver()) {
            render();
            return;
        }

        model.resolveBlockedCurrentPlayers();
        render();
        if (model.isGameOver()) {
            return;
        }

        Player currentPlayer = model.getCurrentPlayer();
        if (!currentPlayer.isHuman() && !model.isAwaitingDraw()) {
            scheduleMachinePlay(gameVersion);
        } else if (currentPlayer.isHuman()) {
            setStatus(model.isAwaitingDraw() ? "Toma una carta para terminar tu turno." : "Tu turno: juega una carta.");
        }
    }

    private void scheduleMachinePlay(int version) {
        Player machine = model.getCurrentPlayer();
        setStatus(machine.getName() + " esta pensando...");
        Thread playTimer = new Thread(() -> {
            sleepRandomDelay(MACHINE_PLAY_MIN_MILLIS, MACHINE_PLAY_MAX_MILLIS);
            Platform.runLater(() -> playMachineCard(version));
        }, machine.getName() + " play timer");
        playTimer.setDaemon(true);
        playTimer.start();
    }

    private void playMachineCard(int version) {
        if (!isSameGameVersion(version) || model.isGameOver() || model.getCurrentPlayer().isHuman()) {
            return;
        }

        try {
            int selectedIndex = model.chooseMachineCardIndex();
            if (selectedIndex < 0) {
                model.resolveBlockedCurrentPlayers();
                continueTurnFlow();
                return;
            }
            model.playCard(selectedIndex);
            render();
            scheduleMachineDraw(version);
        } catch (GameException | GameStateException exception) {
            showError(exception.getMessage());
            continueTurnFlow();
        }
    }

    private void scheduleMachineDraw(int version) {
        Player machine = model.getCurrentPlayer();
        setStatus(machine.getName() + " tomara una carta...");
        Thread drawTimer = new Thread(() -> {
            sleepRandomDelay(MACHINE_DRAW_MIN_MILLIS, MACHINE_DRAW_MAX_MILLIS);
            Platform.runLater(() -> drawCardForMachine(version));
        }, machine.getName() + " draw timer");
        drawTimer.setDaemon(true);
        drawTimer.start();
    }

    private void drawCardForMachine(int version) {
        if (!isSameGameVersion(version) || model.isGameOver() || model.getCurrentPlayer().isHuman()) {
            return;
        }

        try {
            model.drawForCurrentPlayer();
            render();
            continueTurnFlow();
        } catch (GameException | GameStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void sleepRandomDelay(int minMillis, int maxMillis) {
        int delay = minMillis + random.nextInt(maxMillis - minMillis + 1);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isSameGameVersion(int version) {
        return version == gameVersion && model.isGameStarted();
    }

    private boolean isHumanPlayAllowed() {
        return model.isGameStarted()
                && !model.isGameOver()
                && !model.isAwaitingDraw()
                && model.getCurrentPlayer().isHuman();
    }

    private boolean isHumanDrawAllowed() {
        return model.isGameStarted()
                && !model.isGameOver()
                && model.isAwaitingDraw()
                && model.getCurrentPlayer().isHuman();
    }

    private void render() {
        renderTable();
        renderPlayers();
        renderHumanHand();
        renderControls();
    }

    private void renderTable() {
        if (!model.isGameStarted()) {
            tableSumLabel.setText("0");
            tableCardLabel.setText("--");
            tableCardDetailLabel.setText("Sin carta");
            deckCountLabel.setText("Mazo: --");
            currentTurnLabel.setText("Turno: --");
            winnerLabel.setText("");
            return;
        }

        tableSumLabel.setText(String.valueOf(model.getTableSum()));
        Optional<Card> topCard = model.getTopTableCard();
        tableCardLabel.setText(topCard.map(Card::getShortName).orElse("--"));
        tableCardDetailLabel.setText(topCard.map(Card::getDisplayName).orElse("Sin carta"));
        deckCountLabel.setText("Mazo: " + model.getDeckSize());
        currentTurnLabel.setText("Turno: " + model.getCurrentPlayer().getName());
        winnerLabel.setText(model.getWinner().map(player -> "Ganador: " + player.getName()).orElse(""));
    }

    private void renderPlayers() {
        opponentsBox.getChildren().clear();
        if (!model.isGameStarted()) {
            opponentsBox.getChildren().add(new Label("Las maquinas apareceran aqui."));
            return;
        }

        List<Player> players = model.getPlayers();
        for (Player player : players) {
            if (!player.isHuman()) {
                opponentsBox.getChildren().add(createOpponentRow(player));
            }
        }
    }

    private Node createOpponentRow(Player player) {
        VBox row = new VBox(8);
        row.getStyleClass().add("opponent-row");

        Label nameLabel = new Label(player.getName() + (player.isActive() ? " activa" : " eliminada"));
        nameLabel.getStyleClass().add(player.isActive() ? "player-active" : "player-eliminated");

        HBox cardsBox = new HBox(8);
        cardsBox.setAlignment(Pos.CENTER_LEFT);
        for (int index = 0; index < player.getHand().size(); index++) {
            Label back = new Label("??");
            back.getStyleClass().add("card-back");
            cardsBox.getChildren().add(back);
        }
        if (player.getHand().isEmpty()) {
            Label empty = new Label("Sin cartas");
            empty.getStyleClass().add("muted-label");
            cardsBox.getChildren().add(empty);
        }

        row.getChildren().addAll(nameLabel, cardsBox);
        return row;
    }

    private void renderHumanHand() {
        humanHandBox.getChildren().clear();
        if (!model.isGameStarted() || model.getHumanPlayer().isEmpty()) {
            Label placeholder = new Label("Inicia una partida para recibir tus cartas.");
            placeholder.getStyleClass().add("muted-label");
            humanHandBox.getChildren().add(placeholder);
            return;
        }

        Player human = model.getHumanPlayer().orElseThrow();
        List<Card> hand = human.getHand();
        for (int index = 0; index < hand.size(); index++) {
            Card card = hand.get(index);
            Button cardButton = createHumanCardButton(card, index);
            humanHandBox.getChildren().add(cardButton);
        }
    }

    private Button createHumanCardButton(Card card, int handIndex) {
        Button button = new Button(card.getShortName());
        button.setMinSize(92, 126);
        button.setPrefSize(92, 126);
        button.setMaxSize(92, 126);
        button.getStyleClass().add("card-button");
        button.getStyleClass().add(card.getSuit().isRed() ? "red-card" : "black-card");
        if (model.isGameStarted() && !card.canBePlayed(model.getTableSum(), GameModel.TABLE_LIMIT)) {
            button.getStyleClass().add("blocked-card");
        }
        button.setDisable(!isHumanPlayAllowed() || !card.canBePlayed(model.getTableSum(), GameModel.TABLE_LIMIT));
        button.setTooltip(new javafx.scene.control.Tooltip("Carta " + (handIndex + 1) + ": " + card.getDisplayName()));
        button.addEventHandler(MouseEvent.MOUSE_CLICKED, new HumanCardMouseHandler(handIndex));
        button.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> setStatus("Carta " + (handIndex + 1) + ": " + card.getDisplayName()));
        return button;
    }

    private void renderControls() {
        boolean gameRunning = model.isGameStarted() && !model.isGameOver();
        drawButton.setDisable(!isHumanDrawAllowed());
        oneMachineRadio.setDisable(gameRunning);
        twoMachinesRadio.setDisable(gameRunning);
        threeMachinesRadio.setDisable(gameRunning);
        startButton.setText(gameRunning ? "Reiniciar" : "Iniciar juego");
    }

    private void appendLog(String message) {
        String timestamp = LocalTime.now().format(LOG_TIME_FORMAT);
        logArea.appendText("[" + timestamp + "] " + message + System.lineSeparator());
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void showError(String message) {
        setStatus(message);
        appendLog("Aviso: " + message);
    }

    private final class HumanCardMouseHandler implements javafx.event.EventHandler<MouseEvent> {
        private final int handIndex;

        private HumanCardMouseHandler(int handIndex) {
            this.handIndex = handIndex;
        }

        @Override
        public void handle(MouseEvent event) {
            if (event.getClickCount() >= 1) {
                cardSelectionListener.onCardSelected(handIndex);
                event.consume();
            }
        }
    }

    private final class DeckMouseHandler implements javafx.event.EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            if (isHumanDrawAllowed()) {
                setStatus("Tomaste una carta del mazo.");
            }
        }
    }

    private final class GlobalKeyboardHandler extends KeyboardShortcutAdapter {
        @Override
        protected void onKeyPressed(KeyEvent event) {
            if (event.getCode() == KeyCode.DIGIT1 || event.getCode() == KeyCode.NUMPAD1) {
                playHumanCard(0);
                event.consume();
            } else if (event.getCode() == KeyCode.DIGIT2 || event.getCode() == KeyCode.NUMPAD2) {
                playHumanCard(1);
                event.consume();
            } else if (event.getCode() == KeyCode.DIGIT3 || event.getCode() == KeyCode.NUMPAD3) {
                playHumanCard(2);
                event.consume();
            } else if (event.getCode() == KeyCode.DIGIT4 || event.getCode() == KeyCode.NUMPAD4) {
                playHumanCard(3);
                event.consume();
            } else if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.ENTER) {
                drawCardForHuman();
                event.consume();
            } else if (event.getCode() == KeyCode.R) {
                startGame();
                event.consume();
            }
        }
    }

    private final class UiGameEventAdapter extends GameEventAdapter {
        @Override
        public void onStateChanged(GameModel changedModel) {
            render();
        }

        @Override
        public void onMessage(String message) {
            appendLog(message);
            setStatus(message);
        }

        @Override
        public void onGameOver(Player winner) {
            setStatus("Partida terminada. Gana " + winner.getName() + ".");
        }
    }
}
