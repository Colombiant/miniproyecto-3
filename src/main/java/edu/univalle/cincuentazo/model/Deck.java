package edu.univalle.cincuentazo.model;

import edu.univalle.cincuentazo.exception.EmptyDeckException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Dynamic deck that draws from the top and receives cards at the bottom.
 */
public class Deck {
    private final Deque<Card> cards;

    /**
     * Creates an empty deck.
     */
    public Deck() {
        this.cards = new ArrayDeque<>();
    }

    /**
     * Creates a deck with the provided cards.
     *
     * @param initialCards cards to load in order
     */
    public Deck(Collection<Card> initialCards) {
        this();
        addToBottom(initialCards);
    }

    /**
     * Builds a complete shuffled poker deck.
     *
     * @param random random generator used for shuffling
     * @return shuffled deck
     */
    public static Deck standardShuffled(Random random) {
        List<Card> fullDeck = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                fullDeck.add(new Card(rank, suit));
            }
        }
        Collections.shuffle(fullDeck, Objects.requireNonNull(random, "random must not be null"));
        return new Deck(fullDeck);
    }

    /**
     * Draws a card from the top.
     *
     * @return drawn card
     * @throws EmptyDeckException when the deck has no cards
     */
    public Card draw() throws EmptyDeckException {
        if (cards.isEmpty()) {
            throw new EmptyDeckException("The deck has no cards to draw.");
        }
        return cards.removeFirst();
    }

    /**
     * Adds one card to the bottom of the deck.
     *
     * @param card card to add
     */
    public void addToBottom(Card card) {
        cards.addLast(Objects.requireNonNull(card, "card must not be null"));
    }

    /**
     * Adds several cards to the bottom of the deck preserving their order.
     *
     * @param newCards cards to add
     */
    public void addToBottom(Collection<Card> newCards) {
        Objects.requireNonNull(newCards, "newCards must not be null");
        for (Card card : newCards) {
            addToBottom(card);
        }
    }

    /**
     * Shuffles the current deck.
     *
     * @param random random generator used for shuffling
     */
    public void shuffle(Random random) {
        List<Card> shuffled = new ArrayList<>(cards);
        Collections.shuffle(shuffled, Objects.requireNonNull(random, "random must not be null"));
        cards.clear();
        cards.addAll(shuffled);
    }

    /**
     * Checks whether the deck has no cards.
     *
     * @return true when empty
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Gets the number of cards in the deck.
     *
     * @return deck size
     */
    public int size() {
        return cards.size();
    }

    /**
     * Returns a read-only snapshot of the deck order.
     *
     * @return deck cards snapshot
     */
    public List<Card> snapshot() {
        return List.copyOf(cards);
    }
}
