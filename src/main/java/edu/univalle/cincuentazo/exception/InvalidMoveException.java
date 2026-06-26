package edu.univalle.cincuentazo.exception;

/**
 * Checked exception thrown when a card cannot be played by the current rules.
 */
public class InvalidMoveException extends GameException {

    /**
     * Creates an invalid move exception.
     *
     * @param message explanation of the invalid movement
     */
    public InvalidMoveException(String message) {
        super(message);
    }
}
