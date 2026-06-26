package edu.univalle.cincuentazo.model;

import java.util.List;

/**
 * Automated player with a simple safe-card strategy.
 */
public class MachinePlayer extends AbstractPlayer {

    /**
     * Creates a machine player.
     *
     * @param name player name
     */
    public MachinePlayer(String name) {
        super(name, false);
    }

    @Override
    public int chooseCardIndex(int tableSum, int limit) {
        List<Card> cards = getHand();
        int bestIndex = -1;
        int bestResult = Integer.MIN_VALUE;
        for (int index = 0; index < cards.size(); index++) {
            Card card = cards.get(index);
            if (card.canBePlayed(tableSum, limit)) {
                int result = tableSum + card.resolvePlayValue(tableSum, limit);
                if (result > bestResult) {
                    bestResult = result;
                    bestIndex = index;
                }
            }
        }
        return bestIndex;
    }
}
