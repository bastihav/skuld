package de.skuld.radix.manager;

import com.google.common.primitives.Longs;
import de.skuld.prng.PRNG;
import de.skuld.util.ConfigurationHelper;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.LongStream;

public class SeedManager {

  private static final int UNIX_TIME_TO_GENERATE = ConfigurationHelper.getConfig()
      .getInt("radix.prng.unix");
  private long[] seeds = null;
  private Date date;

  public SeedManager() {
  }

  /**
   * Method that returns all seeds that should be used per prng to fill the radix tree
   *
   * @return seed array
   */
  public long[] getSeeds(Date scanDate) {
    if (seeds == null || (!scanDate.equals(date))) {
      long[] unixSeeds = getUnixSeeds(scanDate);
      long[] defaultSeeds = getDefaultSeeds();
      long[] badSeeds = badSeeds();
      long[] pids = processIds();

      seeds = Arrays.stream(Longs.concat(unixSeeds, defaultSeeds, badSeeds, pids)).distinct()
          .toArray();
      date = scanDate;
    }

    return seeds;
  }

  public long[] processIds() {
    // 32 bit systems have default max pid of 2^15
    return LongStream.rangeClosed(0, (int) Math.pow(2, 15)).toArray();
  }

  /**
   * Method that returns a predefined set of bad seeds
   *
   * @return
   */
  private long[] badSeeds() {
    return new long[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, Long.MAX_VALUE};
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
    Collection<Class<? extends PRNG>> prngs = RNGManager.getPRNGs();

    Set<Long> seeds = new HashSet<>(prngs.size());

    prngs.forEach(prng -> {
      try {
        seeds.add(
            prng.getConstructor(long.class).newInstance(Long.MAX_VALUE).usesUnixTimeAsDefault() ? 0
                : prng.getConstructor(long.class).newInstance(Long.MAX_VALUE).getDefaultSeed());
      } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
        e.printStackTrace();
      }
    });

    return seeds.stream().mapToLong(Long::longValue).toArray();
  }
}
