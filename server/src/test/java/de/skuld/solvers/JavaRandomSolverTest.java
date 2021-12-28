package de.skuld.solvers;

import de.skuld.prng.JavaRandom;
import de.skuld.prng.PRNG;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.Random;

public class JavaRandomSolverTest extends AbstractSolverTest {
    @Override
    protected byte[] getSolverInput(long seed) {
        PRNG prng = new JavaRandom(seed);
        Random r = new Random(seed);
        //long l1 = r.nextLong();
        //long l2 = r.nextLong();

        //byte[] array = new byte[Long.BYTES * 2];
        //ByteBuffer.allocate(Long.BYTES * 2).putLong(l1).putLong(l2).rewind().get(array);
        //System.out.println(l1 + " " + l2);
        //System.out.println("array from random " + Arrays.toString(array));
        System.out.println("array from prng   " + Arrays.toString(prng.getRandomBytes(Long.BYTES * 2)));
        //return array;
        //LongBuffer.wrap(new long[]{r.nextLong(), r.nextLong()});
        return prng.getRandomBytes(Long.BYTES * 2);
    }

    @Override
    protected Solver getSolver() {
        return new JavaRandomSolver();
    }


}
