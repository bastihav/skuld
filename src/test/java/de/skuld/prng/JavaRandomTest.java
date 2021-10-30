package de.skuld.prng;

import java.util.Random;

public class JavaRandomTest extends AbstractPrngImplTest {

  @Override
  public long[] getSeeds() {
    return new long[]{-9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1634668549};
  }

  @Override
  public int getAmountPerSeed() {
    return 2 ^ 24;
  }

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
