package dk.askov.dicecalc;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.IntegerRange;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.rangeClosed;

public class Calc {
    
    /**
     * Entry point for running sample injury calculations.
     *
     * @param args command line arguments (not used)
     */
    static void main(String[] args) {
        int keep = 2;
        IntegerRange diceNumber = IntegerRange.of(8, 8);
        
        IntegerRange injuryModifierRange = IntegerRange.of(-3, 2);
        diceNumber.toIntStream()
                  .mapToObj(dice -> calcValuesInjury(dice, keep, injuryModifierRange))
                  .map(CalcResult::format)
                  .forEach(System.out::println);
    }
    
    /**
     * Calculates success probabilities for injury-related outcomes.
     *
     * @param dice                the number of extra dice to roll
     * @param keep                the number of dice to keep
     * @param injuryModifierRange the range of modifiers to apply to the roll score
     * @return a {@link CalcResult} containing probabilities for various injury levels
     */
    public static CalcResult calcValuesInjury(int dice, int keep, IntegerRange injuryModifierRange) {
        var InjuryRanges = List.of(
            SuccessCriteria.UNSCATHED,
            SuccessCriteria.MINOR_HIT,
            SuccessCriteria.DOWN,
            SuccessCriteria.OUT_OF_ACTION);
        return calcValues(dice, keep, injuryModifierRange, InjuryRanges);
    }
    
    /**
     * Calculates success probabilities for general action outcomes.
     *
     * @param dice the number of extra dice to roll
     * @param keep the number of dice to keep
     * @return a {@link CalcResult} containing probabilities for failure, success, and critical success
     */
    public static CalcResult calcValuesActions(int dice, int keep) {
        
        var actionRanges = List.of(SuccessCriteria.FAILURE,
                                   SuccessCriteria.SUCCESS,
                                   SuccessCriteria.CRITICAL_SUCCESS);
        
        return calcValues(dice, keep, null, actionRanges);
    }
    
    
    /**
     * Core calculation method that evaluates success chances for a set of criteria over a range of modifiers.
     *
     * @param extraDice     the number of extra dice to roll
     * @param keep          the number of dice to keep
     * @param modifierRange the range of modifiers to iterate over (if null, a single 0 modifier is used)
     * @param ranges        the list of {@link SuccessCriteria} to evaluate
     * @return a {@link CalcResult} with the aggregated results
     */
    private static CalcResult calcValues(int extraDice,
                                         int keep,
                                         @Nullable IntegerRange modifierRange,
                                         List<SuccessCriteria> ranges) {
        
        var resultBuilder = CalcResult.builder().extraDice(extraDice);
        
        IntegerRange modifiers = Optional.ofNullable(modifierRange).orElse(IntegerRange.of(0, 0));
        
        var calculations =
            modifiers.toIntStream()
                     .mapToObj(modifier -> Pair.of(modifier, calcSuccess(extraDice, keep, modifier)))
                     .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        
        var criteriaResults =
            modifiers.toIntStream()
                     //.boxed()
                     .mapToObj(modifier -> Triple.of(modifier,
                                                     calculations.get(modifier),
                                                     calculations.get(modifier)
                                                                 .stream()
                                                                 .mapToInt(DiceRoll::count).sum()))
                     .flatMap(modifierPair -> ranges
                         .stream()
                         .map(range -> buildCriteriaResult(
                             range,
                             modifierPair.getLeft(),
                             getSuccessChance(modifierPair.getMiddle(), range.criteria(), modifierPair.getRight()))))
                     .sorted(Comparator.comparing(CriteriaResult::sort))
                     .toList();
        return resultBuilder.results(criteriaResults).build();
    }
    
    /**
     * Creates a {@link CriteriaResult} for a specific success criterion, modifier, and calculated chance.
     *
     * @param range         the success criterion
     * @param modifier      the modifier applied
     * @param successChance the calculated probability of success
     * @return a new {@link CriteriaResult}
     */
    private static CriteriaResult buildCriteriaResult(SuccessCriteria range, int modifier, Double successChance) {
        return CriteriaResult
            .builder()
            .successCriteria(range)
            .chance(successChance)
            .modifier(modifier)
            .build();
    }
    
    /**
     * Calculates the probability of success for a given predicate against a distribution of dice rolls.
     *
     * @param successGraph       the list of unique dice roll outcomes and their counts
     * @param diceRollPredicate  the condition to evaluate for each roll
     * @param totalPossibleRolls the total number of possible dice roll combinations
     * @return the probability of success as a value between 0.0 and 1.0
     */
    private static Double getSuccessChance(List<DiceRoll> successGraph, Predicate<DiceRoll> diceRollPredicate,
                                           Integer totalPossibleRolls) {
        //int sum = successGraph.stream().mapToInt(DiceRoll::count).sum();
        double sum1 = successGraph.stream()
                                  .filter(diceRollPredicate)
                                  .mapToDouble(a -> a.count() + 0.0)
                                  .sum();
        return sum1 / totalPossibleRolls;
    }
    
    /**
     * Simulates all possible dice roll combinations and aggregates them by unique kept results.
     *
     * @param extraDice the number of extra dice (positive for advantage, negative for disadvantage)
     * @param keep      the number of dice to keep
     * @param modifier  the modifier to apply to the final score
     * @return a list of unique {@link DiceRoll} outcomes with their occurrence counts
     */
    private static List<DiceRoll> calcSuccess(int extraDice, int keep, int modifier) {
        
        var diceRolls = IntegerRange.of(1, keep + Math.abs(extraDice))
                                    .toIntStream()
                                    .mapToObj(dice -> rangeClosed(1, 6).boxed().toList())
                                    .toList();
        
        var diceStrings = Lists.cartesianProduct(diceRolls)
                               .parallelStream()
                               .map(a -> a.stream()
                                          //.mapToInt(b -> b)
                                          .sorted()
                                          .map(String::valueOf)
                                          //.reduce("", (i, j) -> i + j)
                                   .collect(Collectors.joining())
                                   )
                               .collect(uniqCount());
        
        Comparator<Integer> keepFunction = keepFunction(extraDice);
        return diceStrings
            .entrySet()
            .stream()
            //.map(entry -> Map.entry(stringToIntList(entry.getKey()), entry.getValue()))
            .map(roll -> new DiceRoll(
                Arrays.stream(roll.getKey().split("")).map(Integer::parseInt).toList()
                      .stream()
                      .sorted(keepFunction)
                      .limit(keep)
                      .sorted()
                      .toList(),
                modifier,
                roll.getValue()))
            .toList();
    }
    
    /**
     * A collector that counts the occurrences of unique items and returns them in a {@link TreeMap}.
     *
     * @param <KEY> the type of the items being counted
     * @return a collector for creating a frequency map
     */
    private static <KEY> Collector<KEY, Map<KEY, Integer>, Map<KEY, Integer>> uniqCount() {
        return Collector.of(
            TreeMap::new,
            (map, count) -> map.merge(count, 1, Integer::sum),
            (map1, map2) -> {
                map2.forEach((key, value) -> map1.merge(key, value, Integer::sum));
                return map1;
            },
            Collector.Characteristics.IDENTITY_FINISH);
    }
    
    
    /**
     * Sorts the lowest first, if extra dice is negative and highest first if positive
     */
    private static Comparator<Integer> keepFunction(int extraDice) {
        return extraDice < 0 ? Comparator.naturalOrder() : Comparator.reverseOrder();
    }
}
