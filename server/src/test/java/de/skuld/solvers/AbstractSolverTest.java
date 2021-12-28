package de.skuld.solvers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

public abstract class AbstractSolverTest {
    protected abstract byte[] getSolverInput(long seed);
    protected abstract Solver getSolver();

    @Test
    public void positiveTest() {
        Solver solver = getSolver();
        for (long seed : getSeeds()) {
            long[] possibleSeeds = solver.solve(getSolverInput(seed));
            System.out.println(Arrays.toString(possibleSeeds));
            Assertions.assertTrue(Arrays.stream(possibleSeeds).anyMatch(element -> element == seed));
            System.out.println("Found seed " + seed);
        }
    }

    public long[] getSeeds() {
        return new long[]{0, 1, 2, 3, 42, 1337, -500};
    }
}
