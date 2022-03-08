package de.skuld.radix.manager;

import de.skuld.prng.ImplementedPRNGs;
import de.skuld.prng.PRNG;
import de.skuld.util.ConfigurationHelper;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RNGManager {

  private final static Logger LOGGER = LogManager.getLogger();

  public static Collection<Class<? extends PRNG>> getPRNGs() {
    String[] prngs = (String[]) ConfigurationHelper.getConfig()
        .getArray(String.class, "radix.pre_comps.prngs");

    Set<Class<? extends PRNG>> set = new HashSet<>();

    for (String s : prngs) {
      ImplementedPRNGs prng;
      try {
        prng = ImplementedPRNGs.valueOf(s);
      } catch (IllegalArgumentException e) {
        LOGGER.debug("Could not find RNG " + s + " from config ");
        prng = null;
      }

      if (prng != null) {
        set.add(ImplementedPRNGs.getPRNG(prng));
      }
    }

    set.removeIf(Objects::isNull);
    return set;
  }

  public static List<ImplementedPRNGs> getPRNGEnum() {
    return ConfigurationHelper.getConfig().getList(String.class, "radix.pre_comps.prngs").stream()
        .map(
            ImplementedPRNGs::valueOf).collect(
            Collectors.toList());
  }
}
