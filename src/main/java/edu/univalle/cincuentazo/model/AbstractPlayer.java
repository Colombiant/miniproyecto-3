package edu.univalle.cincuentazo.model;

import edu.univalle.cincuentazo.exception.GameStateException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base implementation shared by all player types.
 */
public abstract class AbstractPlayer implements Player {
    private final String name;
    private final boolean human;
    private final List<Card> hand;
    private boolean active;

    /**
     * Creates a player.
     *
     * @param name player name
     * @param human whether the player is human controlled
     */
    protected AbstractPlayer(String name, boolean human) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.human = human;
        this.hand = new ArrayList<>();
        this.active = true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isHuman() {
        return human;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public List<Card> getHand() {
        return List.copyOf(hand);
    }

    @Override
    public void receiveCard(Card card) {
        hand.add(Objects.requireNonNull(card, "card must not be null"));
    }

    @Override
    public Card removeCard(int handIndex) {
        if (handIndex < 0 || handIndex >= hand.size()) {
            throw new GameStateException("The selected hand index does not exist.");
        }
        return hand.remove(handIndex);
    }

    @Override
    public List<Card> releaseHand() {
        List<Card> released = new ArrayList<>(hand);
        hand.clear();
        return released;
    }

    @Override
    public boolean hasPlayableCard(int tableSum, int limit) {
        return hand.stream().anyMatch(card -> card.canBePlayed(tableSum, limit));
    }
}
