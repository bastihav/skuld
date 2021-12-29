package de.skuld.solvers;

import com.google.common.primitives.Ints;
import de.skuld.prng.JavaRandom;
import de.skuld.prng.PRNG;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class JavaRandomSolverTest extends AbstractSolverTest {
    @Override
    protected byte[] getSolverInput(long seed) {
        PRNG prng = new JavaRandom(seed);
        return prng.getRandomBytes(Integer.BYTES * 4);
    }

    @Override
    protected Solver getSolver() {
        return new JavaRandomSolver();
    }
}
