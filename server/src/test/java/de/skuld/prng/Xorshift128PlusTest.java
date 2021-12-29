package de.skuld.prng;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class Xorshift128PlusTest extends AbstractPrngImplTest {

  @Override
  public long[] getSeeds() {
    return new long[]{-9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1634668549};
  }

  @Override
  byte[] getTargetBytes(long seed, int amountPerSeed) {
    try {
      File f = new File(Objects.requireNonNull(
          this.getClass().getClassLoader().getResource("random_data/cpp/" + seed + ".bin"))
          .toURI());
      if (f.exists()) {
        return new FileInputStream(f).readNBytes(amountPerSeed);
      }
    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
    }
    return new byte[0];
  }

  @Override
  byte[] getActualBytes(long seed, int amountPerSeed) {
    return new Xorshift128Plus(seed).getRandomBytes(amountPerSeed);
  }
}
