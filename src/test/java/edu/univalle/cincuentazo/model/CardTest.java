package edu.univalle.cincuentazo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardTest {

    @Test
    void shouldResolveFixedValues() {
        Card ten = new Card(Rank.TEN, Suit.CLUBS);
        Card nine = new Card(Rank.NINE, Suit.HEARTS);
        Card queen = new Card(Rank.QUEEN, Suit.SPADES);

        assertEquals(10, ten.resolvePlayValue(20, GameModel.TABLE_LIMIT));
        assertEquals(0, nine.resolvePlayValue(49, GameModel.TABLE_LIMIT));
        assertEquals(-10, queen.resolvePlayValue(48, GameModel.TABLE_LIMIT));
    }

    @Test
    void shouldResolveAceAsTenWhenPossibleAndOneWhenNeeded() {
        Card ace = new Card(Rank.ACE, Suit.DIAMONDS);

        assertEquals(10, ace.resolvePlayValue(35, GameModel.TABLE_LIMIT));
        assertEquals(1, ace.resolvePlayValue(45, GameModel.TABLE_LIMIT));
    }

    @Test
    void shouldValidateCardsAgainstTableLimit() {
        Card two = new Card(Rank.TWO, Suit.SPADES);
        Card ace = new Card(Rank.ACE, Suit.CLUBS);
        Card king = new Card(Rank.KING, Suit.HEARTS);

        assertFalse(two.canBePlayed(49, GameModel.TABLE_LIMIT));
        assertFalse(ace.canBePlayed(50, GameModel.TABLE_LIMIT));
        assertTrue(king.canBePlayed(50, GameModel.TABLE_LIMIT));
    }
}
