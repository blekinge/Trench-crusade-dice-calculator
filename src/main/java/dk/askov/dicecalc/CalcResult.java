package dk.askov.dicecalc;

import lombok.Builder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Builder
public record CalcResult(int extraDice, List<CriteriaResult> results) {
    
    public String format() {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); PrintStream out = new PrintStream(bytes)) {
            out.printf("Extra Dice=%+d%n", this.extraDice());
            int longestModifierRangeName = this.results().stream()
                                                 .map(CriteriaResult::name)
                                                 .mapToInt(String::length)
                                                 .max()
                                                 .orElse(0);
            int modifierRangeIndent = Math.max(longestModifierRangeName, "Modifier".length());
            
            
            if (!this.results().stream().allMatch(a -> a.modifier() == 0)) {
                out.print(("Modifier" + " ".repeat(99)).substring(0, modifierRangeIndent - 1));
                this.results()
                    .stream()
                    .map(CriteriaResult::modifier)
                    .sorted()
                    .distinct()
                    .forEach(modifier -> out.printf("%+7d", modifier));
                out.println();
            }
            
            var resultLines = this.results()
                                    .stream()
                                    .collect(Collectors.groupingBy(CriteriaResult::name))
                                    .entrySet()
                                    .stream()
                                    .toList()
                                    .reversed();
            resultLines.forEach(line -> {
                out.printf("%-" + modifierRangeIndent + "s:", line.getKey());
                line.getValue()
                    .stream()
                    .sorted(Comparator.comparing(CriteriaResult::modifier))
                    .forEach(criteriaResult -> out.printf("%6.2f%%", criteriaResult.chance() * 100));
                out.println();
            });
            out.flush();
            return bytes.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public String toString() {
        return "CalcResult{" +
               "extraDice=" + extraDice +
               ", results=" + results.stream().map(Record::toString).collect(Collectors.joining("\n")) +
               '}';
    }
}


@Builder
record CriteriaResult(SuccessCriteria successCriteria, int modifier, Double chance) {
    public String name() {
        return successCriteria.name();
    }
    
    public Predicate<DiceRoll> criteria() {
        return successCriteria.criteria();
    }
    
    public int sort() {
        return successCriteria.sort();
    }
}
