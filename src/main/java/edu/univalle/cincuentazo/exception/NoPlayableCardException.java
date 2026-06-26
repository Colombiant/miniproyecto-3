package edu.univalle.cincuentazo.exception;

/**
 * Checked exception thrown when a player has no legal cards for the table sum.
 */
public class NoPlayableCardException extends GameException {

    /**
     * Creates a no-playable-card exception.
     *
     * @param message explanation of the blocked turn
     */
    public NoPlayableCardException(String message) {
        super(message);
    }
}
