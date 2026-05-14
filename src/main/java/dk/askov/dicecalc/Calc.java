package dk.askov.dicecalc;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.IntegerRange;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.rangeClosed;

public class Calc {
    
    public static void main(String[] args) {
        int keep = 2;
        IntegerRange diceNumber = IntegerRange.of(7, 7);
        
        IntegerRange injuryModifierRange = IntegerRange.of(-3, 2);
        diceNumber.toIntStream()
                  .mapToObj(dice -> calcValuesInjury(dice, keep, injuryModifierRange))
                  .map(CalcResult::format)
                  .forEach(System.out::println);
    }
    
    public static CalcResult calcValuesInjury(int dice, int keep, IntegerRange injuryModifierRange) {
        var InjuryRanges = List.of(
            SuccessCriteria.UNSCATHED,
            SuccessCriteria.MINOR_HIT,
            SuccessCriteria.DOWN,
            SuccessCriteria.OUT_OF_ACTION);
        return calcValues(dice, keep, injuryModifierRange, InjuryRanges);
    }
    
    public static CalcResult calcValuesActions(int dice, int keep) {
        
        var actionRanges = List.of(SuccessCriteria.FAILURE,
                                   SuccessCriteria.SUCCESS,
                                   SuccessCriteria.CRITICAL_SUCCESS);
        
        return calcValues(dice, keep, null, actionRanges);
    }
    
    
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
                     .boxed()
                     .map(modifier -> Triple.of(modifier,
                                                calculations.get(modifier),
                                                calculations.get(modifier).stream().mapToInt(DiceRoll::count).sum()))
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
    
    private static CriteriaResult buildCriteriaResult(SuccessCriteria range, int modifier, Double successChance) {
        return CriteriaResult
            .builder()
            .successCriteria(range)
            .chance(successChance)
            .modifier(modifier)
            .build();
    }
    
    private static Double getSuccessChance(List<DiceRoll> successGraph, Predicate<DiceRoll> diceRollPredicate,
                                           Integer right) {
        //int sum = successGraph.stream().mapToInt(DiceRoll::count).sum();
        double sum1 = successGraph.stream()
                                  .filter(diceRollPredicate)
                                  .mapToDouble(a -> a.count() + 0.0)
                                  .sum();
        return sum1 / right;
    }
    
    private static List<DiceRoll> calcSuccess(int extraDice, int keep, int modifier) {
        
        var diceRolls = IntegerRange.of(1, keep + Math.abs(extraDice))
                                    .toIntStream()
                                    .mapToObj(dice -> rangeClosed(1, 6).boxed().toList())
                                    .toList();
        
        var diceStrings = Lists.cartesianProduct(diceRolls)
                               .stream()
                               .map(Calc::IntListToString)
                               .toList();
        var collect =
            uniqueCount(diceStrings);
        
        Comparator<Integer> keepFunction = keepFunction(extraDice);
        return collect
            .entrySet()
            .stream()
            .map(entry -> Map.entry(stringToIntList(entry.getKey()), entry.getValue()))
            .map(roll -> new DiceRoll(roll.getKey()
                                          .stream()
                                          .sorted(keepFunction)
                                          .limit(keep)
                                          .sorted()
                                          .toList(), modifier, roll.getValue()))
            .toList();
    }
    
    static <T> Map<T, Integer> uniqueCount(List<T> collection) {
        return collection.stream().collect(uniqCount());
        //return collection.stream().collect(Collectors.toMap(Function.identity(), a -> 1, Integer::sum));
    }
    
    private static <T> Collector<T, Map<T, Integer>, Map<T, Integer>> uniqCount() {
        return Collector.of(
            HashMap::new,
            (map, count) -> map.merge(count, 1, Integer::sum),
            (map1, map2) -> {
                map2.forEach((key, value) -> map1.merge(key, value, Integer::sum));
                return map1;
            },
            Collector.Characteristics.IDENTITY_FINISH);
    }
    
    static List<Integer> stringToIntList(String string) {
        return Arrays.stream(string.split("")).map(Integer::parseInt).toList();
    }
    
    static String IntListToString(List<Integer> a) {

        List<String> list = a.stream()
                             .mapToInt(b -> b)
                             .sorted()
                             .mapToObj(String::valueOf)
                             .toList();
        return String.join("", list);
    }
    
    
    /**
     * Sorts the lowest first, if extra dice is negative and highest first if positive
     */
    static Comparator<Integer> keepFunction(int extraDice) {
        return extraDice < 0 ? Comparator.naturalOrder() : Comparator.reverseOrder();
    }
}
