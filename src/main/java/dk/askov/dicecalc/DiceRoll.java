package dk.askov.dicecalc;

import java.util.List;
/**
 * Represents the result of a dice roll calculation.
 * <p>
 * This record encapsulates the total score, individual dice values, and a count
 * associated with a dice roll operation. It is typically used to store and pass
 * dice roll results throughout the application.
 * </p>
 */
public record DiceRoll(
    /**
     * The total score of the dice roll, including any applied modifier.
     * This represents the sum of all individual dice values plus the modifier.
     */
    Integer score,
    
    /**
     * A list containing the individual values of each dice rolled.
     * Each element in the list represents one die's result.
     */
    List<Integer> diceScores,
    
    /**
     * The count associated with the dice roll operation.
     * This typically represents the number of dice used in the roll.
     */
    int count) {

    /**
     * Constructs a DiceRoll instance from individual dice values, a modifier, and a count.
     * <p>
     * This constructor calculates the total score by summing all dice values and adding the modifier.
     * The provided list of dice values is stored as-is in the diceScores field.
     * </p>
     *
     * @param roll     A list of integers representing the individual dice values
     * @param modifier An integer value to be added to the total score
     * @param count    The number of dice used in the roll
     * @throws NullPointerException if {@code roll} is null
     */
    public DiceRoll(List<Integer> roll, int modifier, int count) {
        this(roll.stream().mapToInt(a -> a).sum() + modifier, roll, count);
    }
}