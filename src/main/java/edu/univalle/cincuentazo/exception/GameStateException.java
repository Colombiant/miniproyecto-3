package edu.univalle.cincuentazo.exception;

/**
 * Unchecked exception used for programming errors or invalid state transitions.
 */
public class GameStateException extends RuntimeException {

    /**
     * Creates a game state exception.
     *
     * @param message explanation of the invalid state
     */
    public GameStateException(String message) {
        super(message);
    }
}
