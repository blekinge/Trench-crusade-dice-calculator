package dk.askov.dicecalc;

import java.util.function.Predicate;

public record SuccessCriteria(Predicate<DiceRoll> criteria, String name) {
}
