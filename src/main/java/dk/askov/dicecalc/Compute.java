package dk.askov.dicecalc;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.IntegerRange;
import uk.ac.manchester.tornado.api.ImmutableTaskGraph;
import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.TornadoExecutionPlan;
import uk.ac.manchester.tornado.api.annotations.Parallel;
import uk.ac.manchester.tornado.api.enums.DataTransferMode;
import uk.ac.manchester.tornado.api.exceptions.TornadoExecutionPlanException;
import uk.ac.manchester.tornado.api.types.arrays.IntArray;
import uk.ac.manchester.tornado.api.types.collections.VectorInt;
import uk.ac.manchester.tornado.api.types.matrix.Matrix2DFloat;
import uk.ac.manchester.tornado.api.types.matrix.Matrix2DInt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static dk.askov.dicecalc.Calc.keepFunction;
import static dk.askov.dicecalc.Calc.stringToIntList;
import static dk.askov.dicecalc.Calc.uniqueCount;
import static java.util.stream.IntStream.rangeClosed;

public class Compute {
    
    public static void main(String[] args) throws TornadoExecutionPlanException {
        
        //calcSuccess(7, 2, 0);
        var diceRolls = IntegerRange.of(1, 7 + Math.abs(0))
                                    .toIntStream()
                                    .mapToObj(dice -> rangeClosed(1, 6).toArray())
                                    .toArray(int[][]::new);
        
        Matrix2DInt A = new Matrix2DInt(diceRolls);
        var size = 3000;
        // Example usage of the Compute class
        Matrix2DInt B = new Matrix2DInt(A.getNumRows() * A.getNumRows(), A.getNumColumns());
        
        //// Initialize matrices with some values for demonstration purposes
        //for (int i = 0; i < size; i++) {
        //    for (int j = 0; j < size; j++) {
        //        A.set(i, j, i + j);
        //        B.set(i, j, i * j);
        //    }
        //}
        //
        Compute compute = new Compute();
        compute.run(A, B);
        System.out.println(A.toString());
        System.out.println(B);
        //System.out.println(C);
    }
    
    
    private static List<DiceRoll> calcSuccess(int extraDice, int keep, int modifier) {
        
        
        var diceRolls = IntegerRange.of(1, keep + Math.abs(extraDice))
                                    .toIntStream()
                                    .mapToObj(dice -> rangeClosed(1, 6).toArray())
                                    .toArray(int[][]::new);
        
        Matrix2DInt A = new Matrix2DInt(diceRolls);
        
        var a = new VectorInt[A.getNumRows()];
        for (int i = 0; i < A.getNumRows(); i++) {
            a[i] = A.row(i);
        }
        //var result = cartesianProduct(A);
        
        //var diceStrings = Lists.cartesianProduct(diceRolls)
        //                       .stream()
        //                       .map(Calc::IntListToString)
        //                       .toList();
        //var collect =
        //    uniqueCount(diceStrings);
        //
        //Comparator<Integer> keepFunction = keepFunction(extraDice);
        //return collect
        //    .entrySet()
        //    .stream()
        //    .map(entry -> Map.entry(stringToIntList(entry.getKey()), entry.getValue()))
        //    .map(roll -> new DiceRoll(roll.getKey()
        //                                  .stream()
        //                                  .sorted(keepFunction)
        //                                  .limit(keep)
        //                                  .sorted()
        //                                  .toList(), modifier, roll.getValue()))
        //    .toList();
        return null;
    }
    
    public static void cartesianProduct(Matrix2DInt arrays, Matrix2DInt result) {
        
        
        // Start with the first array and initialize the indices
        int[][] indices = new int[arrays.getNumRows()][1];
        for (
            @Parallel
            int i = 0; i < arrays.getNumRows(); i++) {
            indices[i] = new int[]{0};
        }
        
        for (
            @Parallel
            int result_i = 0; result_i < result.getNumRows(); result_i++) {
            // Collect the current elements from each array based on the current indices
            int[] current = new int[arrays.getNumRows()];
            for (
                @Parallel
                int i = 0; i < arrays.getNumRows(); i++) {
                current[i] = arrays.row(i).get(indices[i][0]);
            }
            
            // Add the combination to the result list
            result.row(result_i).set(current);
            
            // Increment the indices in a nested manner (like counting in base N)
            for (int i = arrays.getNumRows() - 1; i >= 0; i--) {
                indices[i][0]++;
                if (indices[i][0] < arrays.row(i).size()) {
                    break;
                } else {
                    // If the current index overflows, reset it and move to the previous array
                    indices[i][0] = 0;
                    continue;
                }
            }
        }
    }
    
    private static void mxmLoop(Matrix2DInt A, Matrix2DInt B, Matrix2DInt C, final int size) {
        for (
            @Parallel
            int i = 0; i < size; i++) {
            for (
                @Parallel
                int j = 0; j < size; j++) {
                int sum = 0;
                for (int k = 0; k < size; k++) {
                    sum += A.get(i, k) * B.get(k, j);
                }
                C.set(i, j, sum);
            }
        }
    }
    
    public void run(Matrix2DInt A, Matrix2DInt B) throws
                                                  TornadoExecutionPlanException {
        TaskGraph taskGraph = new TaskGraph("s0")
            
            .transferToDevice(DataTransferMode.FIRST_EXECUTION,
                              A) // Transfer data from host to device and mark buffers as read-only,
            // since data will be transferred only during the first execution.
            .task("t0", Compute::cartesianProduct, A, B)              // Each task points to an existing Java method
            .transferToHost(DataTransferMode.EVERY_EXECUTION,
                            B);     // Transfer data from device to host in every execution.
        
        // Create an immutable task-graph
        ImmutableTaskGraph immutableTaskGraph = taskGraph.snapshot();
        
        // Create an execution plan from an immutable task-graph
        try (TornadoExecutionPlan executionPlan = new TornadoExecutionPlan(immutableTaskGraph)) {
            
            // Execute the execution plan
            uk.ac.manchester.tornado.api.TornadoExecutionResult executionResult = executionPlan.execute();
            System.out.println(executionResult.toString());
            
        }
    }
}