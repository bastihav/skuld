package de.skuld.prng;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class MersenneTwisterPythonTest extends AbstractPrngImplTest {

  @Override
  byte[] getTargetBytes(long seed, int amountPerSeed) {
    try {
      File f = new File(Objects.requireNonNull(
          this.getClass().getClassLoader().getResource("random_data/python/" + seed + ".bin"))
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
    return new MersenneTwisterPython(seed).getRandomBytes(amountPerSeed);
  }
}
