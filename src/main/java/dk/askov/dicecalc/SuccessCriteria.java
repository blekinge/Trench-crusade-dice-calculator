package dk.askov.dicecalc;

import java.util.function.Predicate;

public record SuccessCriteria(String name, Predicate<DiceRoll> criteria) {
}
