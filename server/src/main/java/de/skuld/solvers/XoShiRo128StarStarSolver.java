package de.skuld.solvers;

import com.google.common.math.LongMath;
import de.skuld.prng.ImplementedPRNGs;
import de.skuld.prng.PRNG;
import de.skuld.prng.Xoshiro128StarStar;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;

public class XoShiRo128StarStarSolver implements Solver {

  @Override
  public int getConsecutiveBitsNeeded() {
    return Integer.SIZE * 4;
  }

  @Override
  public List<byte[]> solve(byte[] input) {
    ByteBuffer buffer = ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN);
    int[] reconstructedState = reconstructState(new int[]{buffer.getInt(), buffer.getInt(), buffer.getInt(), buffer.getInt()});

    ByteBuffer byteBuffer = ByteBuffer.allocate(reconstructedState.length * 4);
    IntBuffer intBuffer = byteBuffer.asIntBuffer();
    intBuffer.put(reconstructedState);

    byte[] array = byteBuffer.array();

    return Collections.singletonList(array);
  }

  public static int[] reconstructState(int[] outputs) {
    int x0_0 = reverseF(outputs[0]); // x0_0
    int y1 = outputs[1]; // f(x0_0 ^ x3_0 ^ x1_0)
    int y2 = outputs[2]; // f(x3_0 ^ g(x3_0 ^ x1_0) ^ x2_0)
    int y3 = outputs[3]; // f(g(x3_0 ^ x1_0) ^ x2_0  ^ g(g(x3_0 ^ x1_0) ^ x1_0 ^ x2_0 ^ x0_0) ^ x0_0 ^ (x1_0 << 9))

    int x0_x3_x1 = reverseF(y1);
    int x3_gx3x1_x2 = reverseF(y2);

    // create original state
    int x3_1 = x0_x3_x1 ^ x0_0; // x3_0 ^ x1_0
    int x3_x1shift = reverseF(y3) ^ x3_gx3x1_x2 ^ x0_0 ^ g(x3_gx3x1_x2 ^ x0_x3_x1);
    int x1_0 = bruteForceX1_0(x3_1, x3_x1shift) ;
    int x3_0 = x3_1 ^ x1_0;
    int x2_0 = x3_gx3x1_x2 ^ g(x3_1) ^ x3_0;

    return new int[]{x0_0, x1_0, x2_0, x3_0};
  }

  /**
   * Bruteforces x1_0 such that x1_0 ^ (x1<<9) == x3_x1 ^  x1_0 ^ (x1<<9) == x3_x1shift
   * @param x3_x1 given from oracle
   * @param x3_x1shift given from oracle
   * @return x1_0 or 0 if it wasn't found
   */
  public static int bruteForceX1_0(int x3_x1, int x3_x1shift) {
    for (long l_x1_0 = Integer.MIN_VALUE; l_x1_0 <= Integer.MAX_VALUE; l_x1_0++) {
      int x1_0 = (int) l_x1_0;
      int x1_x1shift = x1_0 ^ (x1_0 << 9);

      if ((x3_x1 ^ x1_x1shift) == x3_x1shift) {
        return x1_0;
      }
    }

    return 0;
  }

  public static int f(int n) {
    return Integer.rotateLeft(n * 5, 7) * 9;
  }

  public static int reverseF(int n) {
    return conservativeDivision(Integer.rotateRight(conservativeDivision(n,9), 7),5);
  }

  /**
   *
   *
   * @param number
   * @param divisor
   * @return
   */
  public static int conservativeDivision(int number, int divisor) {
    if (number % divisor == 0) {
      return number / divisor;
    }

    long numberInLong = Long.parseUnsignedLong(Integer.toBinaryString(number), 2);
    long i = 1L;

    long l = numberInLong;

    while(l % divisor != 0 && i <= 16) {
      long mask = i << (32 + LongMath.log2(i, RoundingMode.FLOOR));
      l = numberInLong | mask;
      i++;
    }
    return (int) (l / divisor);
  }

  public static int g(int n) {
    return Integer.rotateLeft(n, 11);
  }

  public static int reverseG(int n) {
    return Integer.rotateRight(n, 11);
  }

  @Override
  public PRNG getPrngImpl(byte[] seed) {
    ByteBuffer buffer = ByteBuffer.wrap(seed);

    int[] state = new int[4];
    buffer.asIntBuffer().get(state);

    return new Xoshiro128StarStar(state);
  }

  @Override
  public ImplementedPRNGs getPrng() {
    return ImplementedPRNGs.XOSHIRO128STARSTAR;
  }
}
