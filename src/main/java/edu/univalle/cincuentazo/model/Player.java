package edu.univalle.cincuentazo.model;

import java.util.List;

/**
 * Defines common behavior for human and machine players.
 */
public interface Player {

    /**
     * Gets the player name.
     *
     * @return player name
     */
    String getName();

    /**
     * Indicates whether the player is controlled by the human.
     *
     * @return true for the human player
     */
    boolean isHuman();

    /**
     * Indicates whether the player is still in the game.
     *
     * @return true when active
     */
    boolean isActive();

    /**
     * Changes the active state of the player.
     *
     * @param active new active state
     */
    void setActive(boolean active);

    /**
     * Gets a read-only hand snapshot.
     *
     * @return cards in hand
     */
    List<Card> getHand();

    /**
     * Adds a card to the player's hand.
     *
     * @param card received card
     */
    void receiveCard(Card card);

    /**
     * Removes a card from the player's hand.
     *
     * @param handIndex card index
     * @return removed card
     */
    Card removeCard(int handIndex);

    /**
     * Removes all cards from the player.
     *
     * @return released cards
     */
    List<Card> releaseHand();

    /**
     * Checks whether the player has at least one legal card.
     *
     * @param tableSum current table sum
     * @param limit maximum allowed table sum
     * @return true when a card can be played
     */
    boolean hasPlayableCard(int tableSum, int limit);

    /**
     * Chooses a card index for automated players.
     *
     * @param tableSum current table sum
     * @param limit maximum allowed table sum
     * @return selected card index, or -1 when no card is legal
     */
    int chooseCardIndex(int tableSum, int limit);
}
