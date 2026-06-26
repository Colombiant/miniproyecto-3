package edu.univalle.cincuentazo.model;

import edu.univalle.cincuentazo.exception.EmptyDeckException;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeckTest {

    @Test
    void shouldCreateCompleteStandardDeck() {
        Deck deck = Deck.standardShuffled(new Random(1));

        assertEquals(52, deck.size());
        assertEquals(52, new HashSet<>(deck.snapshot()).size());
    }

    @Test
    void shouldDrawFromTopAndAddToBottom() throws EmptyDeckException {
        Card first = new Card(Rank.ACE, Suit.CLUBS);
        Card second = new Card(Rank.KING, Suit.SPADES);
        Deck deck = new Deck(List.of(first, second));

        assertEquals(first, deck.draw());
        deck.addToBottom(first);

        assertEquals(second, deck.draw());
        assertEquals(first, deck.draw());
    }

    @Test
    void shouldThrowWhenDrawingFromEmptyDeck() {
        Deck deck = new Deck();

        assertThrows(EmptyDeckException.class, deck::draw);
    }
}
