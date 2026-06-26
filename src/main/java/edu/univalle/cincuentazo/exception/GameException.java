package edu.univalle.cincuentazo.exception;

/**
 * Base checked exception for recoverable game flow errors.
 */
public class GameException extends Exception {

    /**
     * Creates a game exception with a readable message.
     *
     * @param message explanation of the error
     */
    public GameException(String message) {
        super(message);
    }

    /**
     * Creates a game exception with a readable message and cause.
     *
     * @param message explanation of the error
     * @param cause original cause
     */
    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}
