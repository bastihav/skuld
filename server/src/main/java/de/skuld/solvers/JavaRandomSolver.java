package de.skuld.solvers;

import com.rayferric.regen.reverser.RandomReverser;
import com.rayferric.regen.reverser.java.JavaIntegerCall;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.apache.commons.lang3.ArrayUtils;

public class JavaRandomSolver implements Solver {
    @Override
    public int getConsecutiveBitsNeeded() {
        return Integer.SIZE * 4;
    }

    @Override
    public long[] solve(byte[] input) {
        if (input.length < getConsecutiveBitsNeeded() / 8) {
            throw new AssertionError("Input too small!");
        }

        IntBuffer buffer = ByteBuffer.wrap(flipFourBytes(input), 0, input.length / Integer.BYTES).asIntBuffer();

        RandomReverser randomReverser = new RandomReverser();
        while (buffer.hasRemaining()) {
            randomReverser.addCall(new JavaIntegerCall(buffer.get()));
        }

        return randomReverser.solve().map(JavaRandomSolver::unscramble).toArray();
    }

    private static long unscramble(long input) {
        return (input ^ 25214903917L) & 281474976710655L;
    }

    private static byte[] flipFourBytes(byte[] input) {
        if (input.length < 4) {
            throw new AssertionError("Input too small!");
        }

        byte[] output = new byte[input.length - input.length % 4];

        System.arraycopy(input, 0, output, 0, output.length);

        for (int i = 0; i < output.length / 4; i++) {
            ArrayUtils.reverse(output, i*4, (i*4) + 4);
        }

        return output;
    }
}
