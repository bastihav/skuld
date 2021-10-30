package de.skuld.solvers;

public interface Solver {

  /**
   * Method that returns the amount of consecutive random bits needed to solve the PRNG
   *
   * @return number of bits
   */
  int getConsecutiveBitsNeeded();

  /**
   * Method that solves the PRNG, returning possible seeds for generating the input bytes
   *
   * @return possible seeds, can be empty
   */
  long[] solve(byte[] input);

  /**
   * Return whether the amount of randomness is sufficient to attempt to solve the prng
   *
   * @param input input randomness
   * @return whether it is enough randomness
   */
  default boolean solveable(byte[] input) {
    return input != null && input.length > getConsecutiveBitsNeeded();
  }
}
