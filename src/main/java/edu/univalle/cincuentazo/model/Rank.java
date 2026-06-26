package edu.univalle.cincuentazo.model;

/**
 * Represents card ranks and their Cincuentazo values.
 */
public enum Rank {
    ACE("A", 1, true),
    TWO("2", 2, false),
    THREE("3", 3, false),
    FOUR("4", 4, false),
    FIVE("5", 5, false),
    SIX("6", 6, false),
    SEVEN("7", 7, false),
    EIGHT("8", 8, false),
    NINE("9", 0, false),
    TEN("10", 10, false),
    JACK("J", -10, false),
    QUEEN("Q", -10, false),
    KING("K", -10, false);

    private final String symbol;
    private final int baseValue;
    private final boolean flexible;

    Rank(String symbol, int baseValue, boolean flexible) {
        this.symbol = symbol;
        this.baseValue = baseValue;
        this.flexible = flexible;
    }

    /**
     * Gets the compact rank symbol shown on cards.
     *
     * @return rank symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Gets the value used when the card starts the table pile.
     *
     * @return initial table value
     */
    public int getInitialTableValue() {
        return baseValue;
    }

    /**
     * Indicates whether the rank can use more than one value.
     *
     * @return true for aces
     */
    public boolean isFlexible() {
        return flexible;
    }

    /**
     * Resolves the best playable value for the current table sum.
     *
     * @param currentSum current table sum
     * @param limit maximum allowed table sum
     * @return value applied by this rank
     */
    public int resolvePlayValue(int currentSum, int limit) {
        if (!flexible) {
            return baseValue;
        }
        return currentSum + 10 <= limit ? 10 : 1;
    }

    /**
     * Checks whether the rank can be played without exceeding the limit.
     *
     * @param currentSum current table sum
     * @param limit maximum allowed table sum
     * @return true when the rank is playable
     */
    public boolean canBePlayed(int currentSum, int limit) {
        if (!flexible) {
            return currentSum + baseValue <= limit;
        }
        return currentSum + 1 <= limit || currentSum + 10 <= limit;
    }
}
