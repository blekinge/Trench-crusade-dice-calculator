package dk.askov.dicecalc;

import org.apache.commons.lang3.IntegerRange;

import java.util.function.Predicate;

/**
 * Represents a success criterion for evaluating dice rolls.
 *
 * <p>A {@code SuccessCriteria} defines a named condition that determines
 * whether a {@link dk.askov.dicecalc.DiceRoll} is considered successful based on a custom predicate.
 * The {@code sort} field provides an ordering for these criteria, which can be used to prioritize
 * or sequence evaluations during game logic processing.</p>
 *
 * <p>Predefined instances include common criteria such as {@link #FAILURE}, {@link #SUCCESS},
 * {@link #CRITICAL_SUCCESS}, {@link #MINOR_HIT}, {@link #DOWN}, and {@link #OUT_OF_ACTION},
 * each with specific score thresholds and sort priorities.</p>
 *
 * @see dk.askov.dicecalc.DiceRoll
 */
public record SuccessCriteria(String name, Predicate<DiceRoll> criteria, int sort) {
    public static final SuccessCriteria FAILURE = new SuccessCriteria("Failure",
                                                                      diceRoll -> diceRoll.score() <= 6,
                                                                      1);
    public static final SuccessCriteria SUCCESS = new SuccessCriteria("Success",
                                                                      diceRoll -> diceRoll.score() >=
                                                                                  7,
                                                                      2);
    public static final SuccessCriteria CRITICAL_SUCCESS = new SuccessCriteria("Critical Success",
                                                                               diceroll -> diceroll.diceScores()
                                                                                                   .stream()
                                                                                                   .filter(a -> a == 6)
                                                                                                   .count() == 2, 3);
    public static final SuccessCriteria UNSCATHED = new SuccessCriteria("Unscathed (1 or lower)",
                                                                        diceRoll -> diceRoll.score() <= 1,
                                                                        1);
    public static final SuccessCriteria MINOR_HIT = new SuccessCriteria("Minor hit (2-6)",
                                                                        diceRoll -> IntegerRange.of(2, 6)
                                                                                                .contains(diceRoll.score()),
                                                                        2);
    public static final SuccessCriteria DOWN = new SuccessCriteria("Down (7-8)",
                                                                   diceRoll -> IntegerRange.of(7, 8)
                                                                                           .contains(diceRoll.score()),
                                                                   3);
    public static final SuccessCriteria OUT_OF_ACTION = new SuccessCriteria("Out of action (9+)",
                                                                            diceRoll -> diceRoll.score() >= 9,
                                                                            4);
}
