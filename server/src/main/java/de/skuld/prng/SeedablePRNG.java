package de.skuld.prng;

public interface SeedablePRNG extends PRNG {

  /**
   * Seeds the PRNG
   *
   * @param seed The seed
   */
  void seed(long seed);
}
