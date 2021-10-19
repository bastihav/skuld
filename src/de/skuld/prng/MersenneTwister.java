package de.skuld.prng;

public abstract class MersenneTwister implements SeedablePRNG {

  MersenneTwister() {
    this.seed(null);
  }
}
