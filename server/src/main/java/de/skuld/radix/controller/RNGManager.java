package de.skuld.radix.controller;

import de.skuld.prng.JavaRandom;
import de.skuld.prng.PRNG;
import java.util.Collection;
import java.util.Set;

public class RNGManager {

  public Collection<Class<? extends PRNG>> getPRNGs() {
    // TODO fill
    return Set.of(JavaRandom.class);
  }
}
