package prng;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import de.skuld.util.BytePrinter;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * Interface to validate the java implementation of the prng
 */
public abstract class AbstractPrngImplTest {

  /**
   * Method that returns seeds used to generate test data
   *
   * @return seeds
   */
  abstract long[] getSeeds();

  /**
   * Method that returns amount of bytes to generate per seed
   */
  abstract int getAmountPerSeed();

  /**
   * Method that returns the target byte arrays for all seeds that were given
   *
   * @param seeds seeds
   * @return byte array with random data
   */
  byte[][] getTargetBytes(long[] seeds) {
    int amountPerSeed = getAmountPerSeed();
    byte[][] result = new byte[seeds.length][];

    for (int i = 0; i < seeds.length; i++) {
      result[i] = getTargetBytes(seeds[i], amountPerSeed);
    }

    return result;
  }

  /**
   * Method that returns the actual byte arrays for all seeds that were given
   *
   * @param seeds seeds
   * @return byte array with random data
   */
  byte[][] getActualBytes(long[] seeds) {
    int amountPerSeed = getAmountPerSeed();
    byte[][] result = new byte[seeds.length][];

    for (int i = 0; i < seeds.length; i++) {
      result[i] = getActualBytes(seeds[i], amountPerSeed);
    }

    return result;
  }

  /**
   * Method that returns the target byte arrays for a given seed. This is usually implemented by
   * reading a file
   *
   * @param seed seed
   * @return random bytes generated by original prng
   */
  abstract byte[] getTargetBytes(long seed, int amountPerSeed);

  /**
   * Method that returns the target byte arrays for a given seed. This is always implemented by
   * using a PRNG of this project
   *
   * @param seed seed
   * @return random bytes generated by replicated prng
   */
  abstract byte[] getActualBytes(long seed, int amountPerSeed);

  @Test
  public void validateRandomGenerator() {
    long[] seeds = getSeeds();

    System.out.println(Arrays.toString(seeds));

    Arrays.stream(getTargetBytes(seeds)).forEach(BytePrinter::printBytesAsHex);
    assertArrayEquals(getTargetBytes(seeds), getActualBytes(seeds));
  }
}
