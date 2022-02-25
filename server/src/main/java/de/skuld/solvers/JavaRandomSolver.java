package de.skuld.solvers;

import com.google.common.primitives.Longs;
import com.rayferric.regen.reverser.RandomReverser;
import com.rayferric.regen.reverser.java.JavaIntegerCall;
import de.skuld.prng.ImplementedPRNGs;
import de.skuld.prng.JavaRandom;
import de.skuld.prng.PRNG;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;
import java.util.stream.Collectors;

public class JavaRandomSolver implements Solver {
    @Override
    public int getConsecutiveBitsNeeded() {
        return Integer.SIZE * 4;
    }

    @Override
    public List<byte[]> solve(byte[] input) {
        if (input.length < getConsecutiveBitsNeeded() / Byte.SIZE) {
            throw new AssertionError("Input too small!");
        }

        IntBuffer buffer = ByteBuffer.wrap(input, 0, input.length).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();

        RandomReverser randomReverser = new RandomReverser();
        while (buffer.hasRemaining()) {
            randomReverser.addCall(new JavaIntegerCall(buffer.get()));
        }
        return randomReverser.solve().map(JavaRandomSolver::unscramble).mapToObj(
            Longs::toByteArray).collect(Collectors.toList());
    }

    @Override
    public PRNG getPrngImpl(byte[] seed) {
        return new JavaRandom(Longs.fromByteArray(seed));
    }

    @Override
    public ImplementedPRNGs getPrng() {
        return ImplementedPRNGs.JAVA_RANDOM;
    }

    private static long unscramble(long input) {
        return (input ^ 25214903917L) & 281474976710655L;
    }
}
