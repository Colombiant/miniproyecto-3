package edu.univalle.cincuentazo.model;

/**
 * Empty adapter for game event listeners that only need selected callbacks.
 */
public class GameEventAdapter implements GameEventListener {

    @Override
    public void onStateChanged(GameModel model) {
        // Default adapter implementation.
    }

    @Override
    public void onMessage(String message) {
        // Default adapter implementation.
    }

    @Override
    public void onGameOver(Player winner) {
        // Default adapter implementation.
    }
}
