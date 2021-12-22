package de.skuld.radix.manager;

import de.skuld.prng.PRNG;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.LongStream;

public class SeedManager {

  // 60 seconds * 60 minutes * 24 hours * 2 days
  private static final int UNIX_TIME_TO_GENERATE = 172800;
  private final RNGManager rngManager;

  public SeedManager(RNGManager rngManager) {
    this.rngManager = rngManager;
  }

  /**
   * Method that returns all seeds that should be used per prng to fill the radix tree
   *
   * @return seed array
   */
  public long[] getSeeds(Date scanDate) {
    long[] unixSeeds = getUnixSeeds(scanDate);
    long[] defaultSeeds = getDefaultSeeds();
    long[] badSeeds = badSeeds();

    return LongStream
        .concat(LongStream.concat(Arrays.stream(unixSeeds), Arrays.stream(defaultSeeds)),
            Arrays.stream(badSeeds)).distinct().toArray();
  }

  /**
   * Method that returns a predefined set of bad seeds
   *
   * @return
   */
  private long[] badSeeds() {
    return new long[]{0};
  }

  /**
   * Method that returns the last {@link SeedManager#UNIX_TIME_TO_GENERATE} unix timestamps from
   * scanDate downwards
   *
   * @param scanDate date on which a scan should be conducted
   * @return seeds
   */
  private long[] getUnixSeeds(Date scanDate) {
    long unixTime = scanDate.getTime() / 1000;

    long[] result = new long[UNIX_TIME_TO_GENERATE];

    for (int i = 0; i < UNIX_TIME_TO_GENERATE; i++) {
      result[i] = unixTime - i;
    }

    return result;
  }

  private long[] getDefaultSeeds() {
    Collection<Class<? extends PRNG>> prngs = rngManager.getPRNGs();

    Set<Long> seeds = new HashSet<>(prngs.size());

    prngs.forEach(prng -> {
      try {
        seeds.add(prng.getConstructor().newInstance().usesUnixTimeAsDefault() ? 0
            : prng.getConstructor().newInstance().getDefaultSeed());
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

    return seeds.stream().mapToLong(Long::longValue).toArray();
  }
}
