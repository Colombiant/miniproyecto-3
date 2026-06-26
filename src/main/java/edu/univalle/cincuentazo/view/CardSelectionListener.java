package edu.univalle.cincuentazo.view;

/**
 * Functional interface used when the user selects a card in the hand.
 */
@FunctionalInterface
public interface CardSelectionListener {

    /**
     * Receives the selected card index.
     *
     * @param handIndex index inside the human hand
     */
    void onCardSelected(int handIndex);
}
