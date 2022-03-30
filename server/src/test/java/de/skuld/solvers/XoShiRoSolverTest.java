package de.skuld.solvers;

import static de.skuld.solvers.XoShiRo128StarStarSolver.conservativeDivision;

import com.google.common.math.IntMath;
import de.skuld.prng.Xoshiro128StarStar;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class XoShiRoSolverTest {
  @Test
  public void validateStateReconstruction() {
    int state0 = 123465916;
    int state1 = Integer.MAX_VALUE-2;
    int state2 = 13546789;
    int state3 = -16548793;
    int[] originalState = new int[]{state0, state1, state2, state3};

    Xoshiro128StarStar xoshiro128StarStar = new Xoshiro128StarStar(originalState);

    byte[] outputs = new byte[4 * Integer.BYTES];
    byte[] expected = new byte[256];
    xoshiro128StarStar.nextBytes(outputs);
    xoshiro128StarStar.nextBytes(expected);

    List<byte[]> possibleStates = new XoShiRo128StarStarSolver().solve(outputs);

    ByteBuffer buffer = ByteBuffer.wrap(possibleStates.get(0));
    int[] state = new int[4];
    buffer.asIntBuffer().get(state);

    Assertions.assertArrayEquals(originalState, state);

    Xoshiro128StarStar xoshiro128StarStar2 = new Xoshiro128StarStar(state);
    byte[] trash = new byte[4 * Integer.BYTES];
    byte[] actual = new byte[256];
    xoshiro128StarStar2.nextBytes(trash);
    xoshiro128StarStar2.nextBytes(actual);

    Assertions.assertArrayEquals(expected, actual);
  }

  @Test
  public void validateStateReconstructiontest() {

    Xoshiro128StarStar xoshiro128StarStar = new Xoshiro128StarStar(1647155671L);

    byte[] outputs = new byte[4 * Integer.BYTES];
    byte[] expected = new byte[256];
    xoshiro128StarStar.nextBytes(outputs);
    xoshiro128StarStar.nextBytes(expected);

    List<byte[]> possibleStates = new XoShiRo128StarStarSolver().solve(outputs);

    ByteBuffer buffer = ByteBuffer.wrap(possibleStates.get(0));
    int[] state = new int[4];
    buffer.asIntBuffer().get(state);

    //Assertions.assertArrayEquals(originalState, state);

    Xoshiro128StarStar xoshiro128StarStar2 = new Xoshiro128StarStar(state);

    byte[] trash = new byte[4 * Integer.BYTES];
    byte[] actual = new byte[256];
    xoshiro128StarStar2.nextBytes(trash);
    xoshiro128StarStar2.nextBytes(actual);

    Assertions.assertArrayEquals(expected, actual);
  }

  @Test
  public void testReverseFunctions() {
    int x = -2345;
    int y = 625347;

    Assertions.assertEquals(x, XoShiRo128StarStarSolver.reverseF(XoShiRo128StarStarSolver.f(x)));
    System.out.println("--");
    Assertions.assertEquals(y, XoShiRo128StarStarSolver.reverseF(XoShiRo128StarStarSolver.f(y)));
    System.out.println("--");
    Assertions.assertEquals(x, XoShiRo128StarStarSolver.f(XoShiRo128StarStarSolver.reverseF(x)));
    System.out.println("--");
    Assertions.assertEquals(y, XoShiRo128StarStarSolver.f(XoShiRo128StarStarSolver.reverseF(y)));

    Assertions.assertEquals(x, XoShiRo128StarStarSolver.reverseG(XoShiRo128StarStarSolver.g(x)));
    Assertions.assertEquals(y, XoShiRo128StarStarSolver.reverseG(XoShiRo128StarStarSolver.g(y)));
    Assertions.assertEquals(x, XoShiRo128StarStarSolver.g(XoShiRo128StarStarSolver.reverseG(x)));
    Assertions.assertEquals(y, XoShiRo128StarStarSolver.g(XoShiRo128StarStarSolver.reverseG(y)));
  }

  @Test
  public void testDivision() {
    for (int i = 1; i <= 16; i++) {
      int number = i << (31 - IntMath.log2(i, RoundingMode.FLOOR));
      Assertions.assertEquals(number, conservativeDivision(number * 9, 9));
    }
  }
}
