package de.skuld.prng;

public class MersenneTwisterPHP extends MersenneTwister {

  @Override
  public byte[] getRandomBytes(int size) {
    return new byte[0];
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
    return ImplementedPRNGs.MERSENNE_TWISTER_PHP;
  }

  @Override
  public void seed(long seed) {

  }
}
