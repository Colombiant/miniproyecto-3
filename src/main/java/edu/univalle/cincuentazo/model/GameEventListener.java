package edu.univalle.cincuentazo.model;

/**
 * Observer interface used by the model to notify game changes.
 */
public interface GameEventListener {

    /**
     * Called when visible game state changes.
     *
     * @param model changed model
     */
    void onStateChanged(GameModel model);

    /**
     * Called when a message should be shown to the user.
     *
     * @param message readable message
     */
    void onMessage(String message);

    /**
     * Called when the game has a winner.
     *
     * @param winner surviving player
     */
    void onGameOver(Player winner);
}
