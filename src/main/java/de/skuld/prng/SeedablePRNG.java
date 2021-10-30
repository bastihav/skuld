package de.skuld.prng;

import org.jetbrains.annotations.Nullable;

public interface SeedablePRNG extends PRNG {

  /**
   * Seeds the PRNG
   *
   * @param seed The seed
   */
  void seed(long seed);
}
