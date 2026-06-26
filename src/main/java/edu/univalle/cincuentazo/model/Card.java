package edu.univalle.cincuentazo.model;

import java.util.Objects;

/**
 * Immutable card from a standard poker deck.
 */
public final class Card {
    private final Rank rank;
    private final Suit suit;

    /**
     * Creates a card with rank and suit.
     *
     * @param rank card rank
     * @param suit card suit
     */
    public Card(Rank rank, Suit suit) {
        this.rank = Objects.requireNonNull(rank, "rank must not be null");
        this.suit = Objects.requireNonNull(suit, "suit must not be null");
    }

    /**
     * Gets the card rank.
     *
     * @return rank
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * Gets the card suit.
     *
     * @return suit
     */
    public Suit getSuit() {
        return suit;
    }

    /**
     * Gets the value used when this card opens the table pile.
     *
     * @return initial table value
     */
    public int getInitialTableValue() {
        return rank.getInitialTableValue();
    }

    /**
     * Indicates whether this card can be played with more than one value.
     *
     * @return true for aces
     */
    public boolean isFlexible() {
        return rank.isFlexible();
    }

    /**
     * Resolves the value used when this card is played.
     *
     * @param currentSum current table sum
     * @param limit maximum allowed table sum
     * @return value applied by this card
     */
    public int resolvePlayValue(int currentSum, int limit) {
        return rank.resolvePlayValue(currentSum, limit);
    }

    /**
     * Checks if this card is legal for the current table sum.
     *
     * @param currentSum current table sum
     * @param limit maximum allowed table sum
     * @return true when playing the card keeps the sum within the limit
     */
    public boolean canBePlayed(int currentSum, int limit) {
        return rank.canBePlayed(currentSum, limit);
    }

    /**
     * Gets a compact label for the GUI.
     *
     * @return display label
     */
    public String getShortName() {
        return rank.getSymbol() + "-" + suit.getCode();
    }

    /**
     * Gets a readable label for logs and tests.
     *
     * @return display name
     */
    public String getDisplayName() {
        return rank.getSymbol() + " of " + suit.getDisplayName();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Card card)) {
            return false;
        }
        return rank == card.rank && suit == card.suit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, suit);
    }

    @Override
    public String toString() {
        return getShortName();
    }
}
