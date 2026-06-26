package edu.univalle.cincuentazo.exception;

/**
 * Checked exception thrown when a draw operation cannot obtain a card.
 */
public class EmptyDeckException extends GameException {

    /**
     * Creates an empty deck exception.
     *
     * @param message explanation of the missing cards
     */
    public EmptyDeckException(String message) {
        super(message);
    }
}
