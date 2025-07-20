package dk.askov.dicecalc;

import java.util.List;

public record DiceRoll(Integer score, List<Integer> diceScores) {

    public DiceRoll(List<Integer> roll,  int modifier) {
        this(roll.stream().mapToInt(a->a).sum()+modifier, roll);
    }

}
