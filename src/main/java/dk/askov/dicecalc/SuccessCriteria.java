package dk.askov.dicecalc;

import org.apache.commons.lang3.IntegerRange;

import java.util.function.Predicate;

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
