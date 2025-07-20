package dk.askov.dicecalc;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.IntegerRange;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.rangeClosed;

public class Calc {
    
    public static void main(String[] args) {
        int keep = 2;
        IntegerRange diceNumber = IntegerRange.of(7, 7);
        
        diceNumber.toIntStream()
                  .mapToObj(dice -> calcValuesInjury(dice, keep))
                  .map(CalcResult::toString)
                  .forEach(System.out::println);
    }
    
    public static CalcResult calcValuesInjury(int dice, int keep) {
        var InjuryRanges = List.of(
            new SuccessCriteria("Unscathed (1 or lower)", diceRoll -> diceRoll.score() <= 1),
            new SuccessCriteria("Minor hit (2-6)", diceRoll -> IntegerRange.of(2, 6).contains(diceRoll.score())),
            new SuccessCriteria("Down (7-8)", diceRoll -> IntegerRange.of(7, 8).contains(diceRoll.score())),
            new SuccessCriteria("Out of action (9+)", diceRoll -> diceRoll.score() >= 9));
        IntegerRange injuryModifierRange = IntegerRange.of(-3, 2);
        return calcValues(dice, keep, injuryModifierRange, InjuryRanges);
    }
    
    public static CalcResult calcValuesActions(int dice, int keep) {
        
        var actionRanges = List.of(new SuccessCriteria("Failure", diceRoll -> diceRoll.score() <= 6),
                                   new SuccessCriteria("Success", diceRoll -> diceRoll.score() >= 7),
                                   new SuccessCriteria("Critical Success",
                                                       diceroll -> diceroll.diceScores()
                                                                           .stream()
                                                                           .filter(a -> a == 6)
                                                                           .count() == 2));
        
        return calcValues(dice, keep, null, actionRanges);
    }
    
    
    private static CalcResult calcValues(int extraDice,
                                         int keep,
                                         @Nullable IntegerRange modifierRange,
                                         List<SuccessCriteria> ranges) {
        
        var resultBuilder = CalcResult.builder().extraDice(extraDice);
        
        IntegerRange modifiers = Optional.ofNullable(modifierRange).orElse(IntegerRange.of(0, 0));
        
        var calculations = modifiers.toIntStream()
                                    .mapToObj(modifier -> Pair.of(modifier,
                                                                  calcSuccess(extraDice, keep, modifier)))
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
                     .toList();
        return resultBuilder.results(criteriaResults).build();
    }
    
    private static CriteriaResult buildCriteriaResult(SuccessCriteria range, int modifier, Double successChance) {
        return CriteriaResult
            .builder()
            .name(range.name())
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
        
        Comparator<Integer> keepFunction = keepFunction(extraDice);
        Map<String, List<List<Integer>>> collect = Lists.cartesianProduct(diceRolls)
                                                        .stream()
                                                        .collect(Collectors.groupingBy(a -> a.stream()
                                                                                             .sorted()
                                                                                             .map(b -> "" + b)
                                                                                             .collect(Collectors.joining())));
        return collect
            .values()
            .stream()
            .map(lists -> Map.entry(lists.getFirst(), lists.size()))
            .map(roll -> new DiceRoll(roll.getKey()
                                          .stream()
                                          .sorted(keepFunction)
                                          .limit(keep)
                                          .sorted()
                                          .toList(), modifier, roll.getValue()))
            .toList();
    }
    
    /**
     * Sorts the lowest first, if extra dice is negative and highest first if positive
     */
    private static Comparator<Integer> keepFunction(int extraDice) {
        return extraDice < 0 ? Comparator.naturalOrder() : Comparator.reverseOrder();
    }
}
