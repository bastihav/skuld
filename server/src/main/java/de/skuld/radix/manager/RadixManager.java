package de.skuld.radix.manager;

import com.google.common.collect.BiMap;
import de.skuld.prng.PRNG;
import de.skuld.radix.RadixTrie;
import de.skuld.radix.RadixTrieStatus;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import de.skuld.radix.disk.DiskBasedRandomnessRadixTrieData;
import de.skuld.util.ConfigurationHelper;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RadixManager {

  private final Map<UUID, RadixTrie> radixTries = new HashMap<>();

  public RadixManager() {
  }

  public void addRadixTrie(RadixTrie trie) {
    this.radixTries.put(trie.getMetaData().getId(), trie);
  }

  public void deleteRadixTrie(UUID uuid) {
    RadixTrie trie = this.radixTries.remove(uuid);
    trie.delete();
  }

  public void generateTrie(UUID uuid) {
    if (radixTries.get(uuid) != null) {
      generateTrie(radixTries.get(uuid));
    } else {
      throw new RuntimeException("Could not find trie");
    }
  }

  public void generateTrie(RadixTrie radixTrie) {
    radixTrie.generate();
  }

}
