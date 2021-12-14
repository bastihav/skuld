package de.skuld.prng;

import java.util.Random;

public class JavaRandomTest extends AbstractPrngImplTest {

  @Override
  public byte[] getTargetBytes(long seed, int amountPerSeed) {
    Random random = new Random(seed);
    byte[] result = new byte[amountPerSeed];
    random.nextBytes(result);

    return result;
  }

  @Override
  public byte[] getActualBytes(long seed, int amountPerSeed) {
    return new JavaRandom(seed).getRandomBytes(amountPerSeed);
  }
}
