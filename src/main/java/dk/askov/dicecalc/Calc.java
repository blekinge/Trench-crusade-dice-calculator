package dk.askov.dicecalc;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.IntegerRange;
import org.apache.commons.lang3.tuple.Pair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.*;

public class Calc {

    public static void main(String[] args) {
        int keep = 2;
        IntegerRange diceNumber = IntegerRange.of(-2, 2);

        diceNumber.toIntStream()
                  .mapToObj(dice -> calcValuesInjury(dice, keep))
                  .forEach(System.out::println);
    }

    public static String calcValuesInjury(int dice, int keep) {
        var InjuryRanges = List.of(
                new SuccessCriteria(diceRoll -> IntegerRange.of(-9, 1).contains(diceRoll.score()), "Unscathed (1 or lower)"),
                new SuccessCriteria(diceRoll -> IntegerRange.of(2, 6).contains(diceRoll.score()), "Minor hit (2-6)"),
                new SuccessCriteria(diceRoll -> IntegerRange.of(7, 8).contains(diceRoll.score()), "Down (7-8)"),
                new SuccessCriteria(diceRoll -> IntegerRange.of(9, 20).contains(diceRoll.score()), "Out of action (9+)"));
        IntegerRange injuryModifierRange = IntegerRange.of(-3, 2);
        return calcValues(dice, keep, injuryModifierRange, InjuryRanges);
    }

    public static String calcValuesActions(int dice,
                                            int keep) {

        var actionRanges = List.of(new SuccessCriteria(diceRoll -> IntegerRange.of(-9, 6).contains(diceRoll.score()), "Failure"),
                                   new SuccessCriteria(diceRoll -> IntegerRange.of(7, 20).contains(diceRoll.score()), "Success"),
                                   new SuccessCriteria((DiceRoll diceroll) -> diceroll.diceScores()
                                                                                      .stream()
                                                                                      .limit(2)
                                                                                      .allMatch(a -> a == 6), "Critical Success"));

        return calcValues(dice, keep, null, actionRanges);
    }

    private static String calcValues(int extraDice, int keep, IntegerRange modifierRange,
                                     List<SuccessCriteria> ranges) {

        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); PrintStream out = new PrintStream(bytes)) {
            out.printf("Extra Dice=%+d%n", extraDice);

            int longestModifierRangeName = ranges.stream()
                                                 .map(SuccessCriteria::name)
                                                 .mapToInt(String::length)
                                                 .max()
                                                 .orElse(0);
            int modifierRangeIndent = Math.max(longestModifierRangeName, "Modifier".length());

            if (modifierRange == null) {
                modifierRange = IntegerRange.of(0, 0);
            } else {
                out.print(("Modifier" + " ".repeat(99)).substring(0, modifierRangeIndent - 1));
                modifierRange.toIntStream().forEach(modifier -> out.printf("%+7d", modifier));
                out.println();
            }

            var calculations = modifierRange.toIntStream()
                                            .mapToObj(modifier -> Pair.of(modifier,
                                                                          calcSuccess(extraDice, keep, modifier)))
                                            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
            for (SuccessCriteria range : ranges) {
                out.printf("%-" + modifierRangeIndent + "s:", range.name());
                modifierRange.toIntStream().forEach(modifier -> {
                    var successChance = getSuccessChance(calculations.get(modifier), range.criteria());
                    out.printf("%6.2f%%", successChance * 100);
                });
                out.println();
            }
            out.flush();
            return bytes.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Double getSuccessChance(List<DiceRoll> successGraph, Predicate<DiceRoll> targetRange) {
        return successGraph.stream()
                           .filter(targetRange::test)
                           .mapToDouble(a -> 1)
                           .sum() / successGraph.size();
    }

    private static List<DiceRoll> calcSuccess(int extraDice, int keep, int modifier) {

        var diceRolls = IntegerRange.of(1, keep + Math.abs(extraDice))
                                    .toIntStream()
                                    .mapToObj(dice -> rangeClosed(1, 6).boxed().toList())
                                    .toList();
        var allDiceRolls = Lists.cartesianProduct(diceRolls);

        Comparator<Integer> keepFunction = extraDice < 0 ? Comparator.naturalOrder() : Comparator.reverseOrder();
        return allDiceRolls
                .stream()
                .map(roll -> roll
                        .stream()
                        .sorted(keepFunction)
                        .limit(keep)
                        .sorted(Comparator.reverseOrder())
                        .toList())
                .map(roll1 -> new DiceRoll(roll1, modifier))
                .toList();
    }
}
