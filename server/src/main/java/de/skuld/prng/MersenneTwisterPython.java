package de.skuld.prng;

import java.util.Arrays;
import org.apache.commons.math3.random.MersenneTwister;

/**
 * Implements https://github.com/python/cpython/blob/3.10/Lib/random.py with seeding variant version
 * 2
 */
public class MersenneTwisterPython implements SeedablePRNG {

  private final MersenneTwister twister = new MersenneTwister();

  public MersenneTwisterPython(long seed) {
    super();
    this.seed(seed);
  }

  @Override
  public byte[] getRandomBytes(int size) {
    byte[] result = new byte[size];
    twister.nextBytes(result);
    return result;
  }

  @Override
  public long getDefaultSeed() {
    return 0;
  }

  @Override
  public boolean usesUnixTimeAsDefault() {
    return false;
  }

  @Override
  public ImplementedPRNGs getPRNG() {
    return ImplementedPRNGs.MERSENNE_TWISTER_PYTHON;
  }

  @Override
  public void seed(long seed) {
    System.out.println("original seed: " + seed);
/*    if (seed < 0) {
      {
        seed = seed ^ (1L << 32);
      }
      System.out.println(seed + " " + (seed));
    }*/
    int[] intArray;
    if (Long.numberOfLeadingZeros(seed) >= 32) {
      intArray = new int[]{(int) seed};
    } else {
      intArray = new int[]{(int) seed, (int) (seed >> 32)};
    }
    System.out.println("seed as array " + Arrays.toString(intArray));
    twister.setSeed(intArray);
    //twister.setSeed(seed);
    System.out.println(twister);
  }
}
