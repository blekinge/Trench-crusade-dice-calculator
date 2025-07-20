package dk.askov.dicecalc;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.IntegerRange;
import org.apache.commons.lang3.tuple.Pair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.*;

public class Calc {

    public static void main(String[] args) throws IOException {

        IntegerRange modifierRange = IntegerRange.of(-3, 2);
        int keep = 2;
        IntegerRange diceNumber = IntegerRange.of(-2, 2);

        var InjuryRanges = List.of(Pair.of(IntegerRange.of(-9, 1), "Unscathed (1 or lower)"),
                                   Pair.of(IntegerRange.of(2, 6), "Minor hit (2-6)"),
                                   Pair.of(IntegerRange.of(7, 8), "Down (7-8)"),
                                   Pair.of(IntegerRange.of(9, 20), "Out of action (9+)"));

        var actionRanges = List.of(Pair.of(IntegerRange.of(-9, 6), "Failure"),
                                   Pair.of(IntegerRange.of(7, 20), "Success"));
        diceNumber.toIntStream()
                  .mapToObj(dice -> calcValues(dice, keep, modifierRange, actionRanges))
                  .forEach(System.out::println);
    }

    private static String calcValues(int extraDice, int keep, IntegerRange modifierRange,
                                     List<Pair<IntegerRange, String>> ranges) {

        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); PrintStream out = new PrintStream(bytes)) {
            out.printf("Extra Dice=%+d%n", extraDice);

            int longestRangeNameLength = Math.max(ranges.stream()
                                                        .map(Pair::getValue)
                                                        .mapToInt(String::length)
                                                        .max()
                                                        .orElse(0), "Modifier".length());
            out.print(("Modifier" + " ".repeat(99)).substring(0, longestRangeNameLength - 1));

            Map<Integer, Map<Integer, Double>> calculations = new TreeMap<>();
            modifierRange.toIntStream().forEach(modifier -> {
                var successGraph = calcSuccess(extraDice, keep, modifier);
                calculations.put(modifier, successGraph);
                out.printf("%+7d", modifier);
            });
            out.println();
            for (Pair<IntegerRange, String> range : ranges) {
                out.printf("%-" + longestRangeNameLength + "s:", range.getValue());
                modifierRange.toIntStream().forEach(modifier -> {
                    var successChance = getSuccessChance(calculations.get(modifier), range.getLeft());
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

    private static Double getSuccessChance(Map<Integer, Double> successGraph, IntegerRange targetRange) {
        return successGraph.entrySet()
                           .stream()
                           .filter(entry -> targetRange.contains(entry.getKey()))
                           .mapToDouble(Map.Entry::getValue)
                           .sum();
    }

    private static Map<Integer, Double> calcSuccess(int extraDice, int keep, int modifier) {

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
                        .mapToInt(a -> a)
                        .sum())
                .collect(Collectors.groupingBy(score -> score + modifier))
                .entrySet().stream()
                .map(entry -> Map.entry(
                        entry.getKey(),
                        itod(entry.getValue().size()) / allDiceRolls.size()))
                .collect(mapEntriesToMap());
    }

    private static double itod(int val) {
        return val + 0.0;
    }

    private static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> mapEntriesToMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }
}
