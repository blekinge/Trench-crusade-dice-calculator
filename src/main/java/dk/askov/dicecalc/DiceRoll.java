/**
 * Represents a dice roll with additional information about the scores and count.
 */
package dk.askov.dicecalc;

import java.util.List;

public record DiceRoll(Integer score, List<Integer> diceScores, int count) {
    
    /**
     * Constructs a new {@code DiceRoll} object with the given roll, modifier, and count.
     * 
     * @param roll The list of integers representing the scores of the individual dice.
     * @param modifier An integer representing the modifier to be added to the sum of the dice scores.
     * @param count The number of dice rolled.
     */
    public DiceRoll(List<Integer> roll, int modifier, int count) {
        this(roll.stream().mapToInt(a->a).sum()+modifier, roll, count);
    }
}
