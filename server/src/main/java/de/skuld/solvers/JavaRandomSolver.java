package de.skuld.solvers;

import com.google.common.primitives.Longs;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class JavaRandomSolver implements Solver {
    @Override
    public int getConsecutiveBitsNeeded() {
        return Integer.SIZE * 2;
    }

    /**
     * Idea from https://crypto.stackexchange.com/questions/51686/how-to-determine-the-next-number-from-javas-random-method
     * @param input
     * @return
     */
    @Override
    public long[] solve(byte[] input) {
        System.out.println("testing solve");
        //System.out.println(next(0, 32));
        if (input.length < getConsecutiveBitsNeeded() / 8) {
            throw new AssertionError("Input too small!");
        }

        IntBuffer buffer = ByteBuffer.wrap(input, 0, Integer.BYTES * 2).asIntBuffer();

        int i1 = buffer.get(0);
        int i2 = buffer.get(1);
        //System.out.println("solver: " + l1 + " "+ l2);
        long seed = 0;

        Set<Long> seeds = new HashSet<>();

        for (int i = 0; i < 65536; i++) {
            seed = i1 * 65536L + i;
            if (next(seed) == i2) {
                System.out.println("Seed found: " + seed);
                seeds.add(seed);
            }
        }
        return seeds.stream().mapToLong(x -> x).toArray();
    }

    private static int next(long seed) {
        int bits = 32;
        long seed2 = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
        return (int)(seed2 >>> (48 - bits));
    }

    // TODO first 32 bit of array will be generated using next(32) - this returns shifted stuff
    
}
