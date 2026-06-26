package edu.univalle.cincuentazo.model;

/**
 * Represents the four suits in a standard poker deck.
 */
public enum Suit {
    CLUBS("Clubs", "C", false),
    DIAMONDS("Diamonds", "D", true),
    HEARTS("Hearts", "H", true),
    SPADES("Spades", "S", false);

    private final String displayName;
    private final String code;
    private final boolean red;

    Suit(String displayName, String code, boolean red) {
        this.displayName = displayName;
        this.code = code;
        this.red = red;
    }

    /**
     * Gets the user readable suit name.
     *
     * @return suit name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets a compact suit code.
     *
     * @return one-letter code
     */
    public String getCode() {
        return code;
    }

    /**
     * Indicates whether the suit is traditionally red.
     *
     * @return true for hearts and diamonds
     */
    public boolean isRed() {
        return red;
    }
}
