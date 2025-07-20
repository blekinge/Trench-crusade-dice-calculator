package dk.askov.dicecalc;

import java.util.List;

public record DiceRoll(Integer score, List<Integer> diceScores, int count) {

    public DiceRoll(List<Integer> roll, int modifier, int count) {
        this(roll.stream().mapToInt(a->a).sum()+modifier, roll, count);
    }

}
