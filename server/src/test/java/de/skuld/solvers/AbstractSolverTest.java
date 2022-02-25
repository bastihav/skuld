package de.skuld.solvers;

import com.google.common.primitives.Longs;
import de.skuld.prng.JavaRandom;
import de.skuld.util.ByteHexUtil;
import java.util.Date;
import java.util.List;
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
       /* for (long seed : getSeeds()) {
            long[] possibleSeeds = solver.solve(getSolverInput(seed));
            Assertions.assertTrue(Arrays.stream(possibleSeeds).anyMatch(element -> element == seed));
        }*/

        List<byte[]> possibleSeeds = solver.solve(getSolverInput(0));
        System.out.println(Arrays.toString(possibleSeeds.get(0)));
        JavaRandom javaRandom = new JavaRandom(Longs.fromByteArray(possibleSeeds.get(0)));
        byte[] someBytes = new byte[2048];
        javaRandom.nextBytes(someBytes);
        ByteHexUtil.printBytesAsHex(someBytes);
    }


    public long[] getSeeds() {
        return new long[]{0, 1, 2, 3, 42, 1337};
    }
}
