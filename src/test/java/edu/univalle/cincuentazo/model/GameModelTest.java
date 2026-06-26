package edu.univalle.cincuentazo.model;

import edu.univalle.cincuentazo.exception.GameException;
import edu.univalle.cincuentazo.exception.InvalidMoveException;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameModelTest {

    @Test
    void shouldPrepareGameWithSelectedMachinePlayers() throws GameException {
        GameModel model = new GameModel(new Random(3));

        model.startNewGame(2);

        assertEquals(3, model.getPlayers().size());
        assertEquals(4, model.getHumanPlayer().orElseThrow().getHand().size());
        assertTrue(model.getTopTableCard().isPresent());
        assertEquals(39, model.getDeckSize());
        assertFalse(model.isAwaitingDraw());
    }

    @Test
    void shouldRejectInvalidMachinePlayerCount() {
        GameModel model = new GameModel(new Random(4));

        assertThrows(InvalidMoveException.class, () -> model.startNewGame(0));
        assertThrows(InvalidMoveException.class, () -> model.startNewGame(4));
    }

    @Test
    void shouldPlayDrawAndAdvanceTurn() throws GameException {
        GameModel model = new GameModel(new Random(7));
        model.startNewGame(1);
        Player human = model.getCurrentPlayer();

        Card playedCard = model.playCard(0);

        assertNotNull(playedCard);
        assertEquals(3, human.getHand().size());
        assertTrue(model.isAwaitingDraw());

        model.drawForCurrentPlayer();

        assertEquals(4, human.getHand().size());
        assertFalse(model.isAwaitingDraw());
        assertFalse(model.getCurrentPlayer().isHuman());
    }
}
