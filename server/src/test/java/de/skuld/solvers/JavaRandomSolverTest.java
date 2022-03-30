package de.skuld.solvers;

import com.google.common.primitives.Ints;
import de.skuld.prng.JavaRandom;
import de.skuld.prng.PRNG;

import de.skuld.util.ByteHexUtil;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class JavaRandomSolverTest extends AbstractSolverTest {
    @Override
    protected byte[] getSolverInput(long seed) {
/*        PRNG prng = new JavaRandom(seed);
        byte[] array = new byte[Integer.BYTES * 4];
        prng.nextBytes(array);
        return array;*/
        return random1;
    }

    byte[] random1 = ByteHexUtil.hexToByte("CE5732B3075569D9D1E14FBB5FE26E05173AAA5979869739");
    byte[] random2 = ByteHexUtil.hexToByte("688A12B39A8ECDAFAEEB7D4D57099F7335A39E92BFAC31E8");

    @Override
    protected Solver getSolver() {
        return new JavaRandomSolver();
    }
}
