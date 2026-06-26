package edu.univalle.cincuentazo.view;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

/**
 * Adapter for JavaFX keyboard handlers with no-op extension points.
 */
public class KeyboardShortcutAdapter implements EventHandler<KeyEvent> {

    @Override
    public final void handle(KeyEvent event) {
        if (event.getEventType() == KeyEvent.KEY_PRESSED) {
            onKeyPressed(event);
        } else if (event.getEventType() == KeyEvent.KEY_RELEASED) {
            onKeyReleased(event);
        } else if (event.getEventType() == KeyEvent.KEY_TYPED) {
            onKeyTyped(event);
        }
    }

    /**
     * Handles a key pressed event.
     *
     * @param event key event
     */
    protected void onKeyPressed(KeyEvent event) {
        // Adapter hook.
    }

    /**
     * Handles a key released event.
     *
     * @param event key event
     */
    protected void onKeyReleased(KeyEvent event) {
        // Adapter hook.
    }

    /**
     * Handles a key typed event.
     *
     * @param event key event
     */
    protected void onKeyTyped(KeyEvent event) {
        // Adapter hook.
    }
}
