package dk.askov.dicecalc;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CalcTest {
    
    @Test
    void calcValuesInjury() {
        assertThat(Calc.calcValuesInjury(0, 2),
                   is(new CalcResult(0,
                                     List.of(
                                         new CriteriaResult("Unscathed (1 or lower)", -3, 0.16666666666666666),
                                         new CriteriaResult("Unscathed (1 or lower)", -2, 0.08333333333333333),
                                         new CriteriaResult("Unscathed (1 or lower)", -1, 0.027777777777777776),
                                         new CriteriaResult("Unscathed (1 or lower)", 0, 0.0),
                                         new CriteriaResult("Unscathed (1 or lower)", 1, 0.0),
                                         new CriteriaResult("Unscathed (1 or lower)", 2, 0.0),
                                         new CriteriaResult("Minor hit (2-6)", -3, 0.6666666666666666),
                                         new CriteriaResult("Minor hit (2-6)", -2, 0.6388888888888888),
                                         new CriteriaResult("Minor hit (2-6)", -1, 0.5555555555555556),
                                         new CriteriaResult("Minor hit (2-6)", 0, 0.4166666666666667),
                                         new CriteriaResult("Minor hit (2-6)", 1, 0.2777777777777778),
                                         new CriteriaResult("Minor hit (2-6)", 2, 0.16666666666666666),
                                         new CriteriaResult("Down (7-8)", -3, 0.1388888888888889),
                                         new CriteriaResult("Down (7-8)", -2, 0.19444444444444445),
                                         new CriteriaResult("Down (7-8)", -1, 0.25),
                                         new CriteriaResult("Down (7-8)", 0, 0.3055555555555556),
                                         new CriteriaResult("Down (7-8)", 1, 0.3055555555555556),
                                         new CriteriaResult("Down (7-8)", 2, 0.25),
                                         new CriteriaResult("Out of action (9+)", -3, 0.027777777777777776),
                                         new CriteriaResult("Out of action (9+)", -2, 0.08333333333333333),
                                         new CriteriaResult("Out of action (9+)", -1, 0.16666666666666666),
                                         new CriteriaResult("Out of action (9+)", 0, 0.2777777777777778),
                                         new CriteriaResult("Out of action (9+)", 1, 0.4166666666666667),
                                         new CriteriaResult("Out of action (9+)", 2, 0.5833333333333334)))));
    }
    
    @Test
    void calcValuesActions() {
        assertThat(Calc.calcValuesActions(0, 2),
                   is(new CalcResult(0,
                                     List.of(new CriteriaResult("Failure", 0, 0.4166666666666667),
                                             new CriteriaResult("Success", 0, 0.5833333333333334),
                                             new CriteriaResult("Critical Success", 0, 0.027777777777777776)))));
    }
}