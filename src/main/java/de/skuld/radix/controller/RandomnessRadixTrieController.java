package de.skuld.radix.controller;

import de.skuld.prng.JavaRandom;
import de.skuld.prng.PRNG;
import de.skuld.radix.RadixTrie;
import de.skuld.util.ConfigurationHelper;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.LongStream;

public class RandomnessRadixTrieController<T extends RadixTrie> {

  public T prefillRadixTrie(T trie) {
    int minHeight = ConfigurationHelper.getConfig().getInt("radix.height.min");
    int maxHeight = ConfigurationHelper.getConfig().getInt("radix.height.max");



    return trie;
  }
}
