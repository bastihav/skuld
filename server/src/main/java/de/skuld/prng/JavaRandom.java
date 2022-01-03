package de.skuld.prng;

import java.util.Date;
import java.util.Random;

public class JavaRandom implements SeedablePRNG {

  private final Random r;

  JavaRandom() {
    this.r = new Random();
  }

  public JavaRandom(long seed) {
    this();
    seed(seed);
  }

  @Override
  public byte[] getRandomBytes(int size) {
    byte[] bytes = new byte[size];
    r.nextBytes(bytes);
    return bytes;
  }

  @Override
  public long getDefaultSeed() {
    return new Date().getTime() / 1000;
  }

  @Override
  public boolean usesUnixTimeAsDefault() {
    return true;
  }

  @Override
  public ImplementedPRNGs getPRNG() {
    return ImplementedPRNGs.JAVA_RANDOM;
  }

  @Override
  public void seed(long seed) {
    r.setSeed(seed);
  }
}
