package de.skuld.solvers;

import de.skuld.prng.ImplementedPRNGs;
import de.skuld.prng.PRNG;
import de.skuld.util.ArrayUtil;
import de.skuld.util.ConfigurationHelper;
import java.util.List;

public interface Solver {

  /**
   * Method that returns the amount of consecutive random bits needed to solve the PRNG
   *
   * @return number of bits
   */
  int getConsecutiveBytesNeeded();

  /**
   * Method that solves the PRNG, returning possible seeds for generating the input bytes
   *
   * @return possible seeds, can be empty
   */
  List<byte[]> solve(byte[] input);

  /**
   * Return whether the amount of randomness is sufficient to attempt to solve the prng
   *
   * @param input input randomness
   * @return whether it is enough randomness
   */
  default boolean solveable(byte[] input) {
    return input != null && input.length > getConsecutiveBytesNeeded();
  }

  /**
   * Verifies whether the PRNG will generate all those randoms in that order (not necessarily
   * consecutively) using the seed
   *
   * @param randoms
   * @param seed
   * @return
   */
  default boolean verify(List<byte[]> randoms, byte[] seed) {
    PRNG random = getPrngImpl(seed);
    byte[] someBytes = new byte[ConfigurationHelper.getConfig().getInt("radix.solver.verify_size")];
    random.nextBytes(someBytes);
    int currentIndex = -1;
    for (byte[] bytes : randoms) {
      int index = ArrayUtil.isSubArray(someBytes, bytes);
      if (index > currentIndex) {
        currentIndex = index;
      } else {
        currentIndex = Integer.MAX_VALUE;
      }
    }

    return currentIndex > -1 && currentIndex < Integer.MAX_VALUE;
  }

  PRNG getPrngImpl(byte[] seed);

  /**
   * Gets the PRNG that this Solver solves
   *
   * @return
   */
  ImplementedPRNGs getPrng();
}
