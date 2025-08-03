package dk.askov.dicecalc;

import org.apache.commons.lang3.IntegerRange;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class CalcTest {
    
    @Test
    void calcValuesInjury() {
        assertThat(Calc.calcValuesInjury(0, 2, IntegerRange.of(-3, 2)),
                   is(new CalcResult(0,
                                     List.of(
                                         new CriteriaResult(SuccessCriteria.UNSCATHED, -3, 0.16666666666666666),
                                         new CriteriaResult(SuccessCriteria.UNSCATHED, -2, 0.08333333333333333),
                                         new CriteriaResult(SuccessCriteria.UNSCATHED, -1, 0.027777777777777776),
                                         new CriteriaResult(SuccessCriteria.UNSCATHED, 0, 0.0),
                                         new CriteriaResult(SuccessCriteria.UNSCATHED, 1, 0.0),
                                         new CriteriaResult(SuccessCriteria.UNSCATHED, 2, 0.0),
                                         new CriteriaResult(SuccessCriteria.MINOR_HIT, -3, 0.6666666666666666),
                                         new CriteriaResult(SuccessCriteria.MINOR_HIT, -2, 0.6388888888888888),
                                         new CriteriaResult(SuccessCriteria.MINOR_HIT, -1, 0.5555555555555556),
                                         new CriteriaResult(SuccessCriteria.MINOR_HIT, 0, 0.4166666666666667),
                                         new CriteriaResult(SuccessCriteria.MINOR_HIT, 1, 0.2777777777777778),
                                         new CriteriaResult(SuccessCriteria.MINOR_HIT, 2, 0.16666666666666666),
                                         new CriteriaResult(SuccessCriteria.DOWN, -3, 0.1388888888888889),
                                         new CriteriaResult(SuccessCriteria.DOWN, -2, 0.19444444444444445),
                                         new CriteriaResult(SuccessCriteria.DOWN, -1, 0.25),
                                         new CriteriaResult(SuccessCriteria.DOWN, 0, 0.3055555555555556),
                                         new CriteriaResult(SuccessCriteria.DOWN, 1, 0.3055555555555556),
                                         new CriteriaResult(SuccessCriteria.DOWN, 2, 0.25),
                                         new CriteriaResult(SuccessCriteria.OUT_OF_ACTION, -3, 0.027777777777777776),
                                         new CriteriaResult(SuccessCriteria.OUT_OF_ACTION, -2, 0.08333333333333333),
                                         new CriteriaResult(SuccessCriteria.OUT_OF_ACTION, -1, 0.16666666666666666),
                                         new CriteriaResult(SuccessCriteria.OUT_OF_ACTION, 0, 0.2777777777777778),
                                         new CriteriaResult(SuccessCriteria.OUT_OF_ACTION, 1, 0.4166666666666667),
                                         new CriteriaResult(SuccessCriteria.OUT_OF_ACTION, 2, 0.5833333333333334)))));
    }
    
    @Test
    void calcValuesActions() {
        assertThat(Calc.calcValuesActions(0, 2),
                   is(new CalcResult(0,
                                     List.of(new CriteriaResult(SuccessCriteria.FAILURE, 0, 0.4166666666666667),
                                             new CriteriaResult(SuccessCriteria.SUCCESS, 0, 0.5833333333333334),
                                             new CriteriaResult(SuccessCriteria.CRITICAL_SUCCESS, 0, 0.027777777777777776)))));
    }
}