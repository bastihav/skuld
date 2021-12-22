package de.skuld.radix.manager;

import de.skuld.radix.RadixTrie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RadixManager {

  private final Map<UUID, RadixTrie> radixTries = new HashMap<>();

  public RadixManager() {
  }

/*  public void addRadixTrie(RadixTrie trie) {
    this.radixTries.put(trie.getId(), trie);
  }

  public void deleteRadixTrie(UUID uuid) {
    RadixTrie trie = this.radixTries.remove(uuid);
    trie.delete();
  }*/
}
