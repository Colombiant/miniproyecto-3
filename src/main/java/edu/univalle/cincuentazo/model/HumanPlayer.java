package edu.univalle.cincuentazo.model;

/**
 * Human controlled player.
 */
public class HumanPlayer extends AbstractPlayer {

    /**
     * Creates the human player.
     *
     * @param name player name
     */
    public HumanPlayer(String name) {
        super(name, true);
    }

    @Override
    public int chooseCardIndex(int tableSum, int limit) {
        return -1;
    }
}
