package de.skuld.radix.memory;

import de.skuld.radix.AbstractRadixTrieEdge;
import de.skuld.radix.RadixTrieEdge;
import de.skuld.radix.data.RandomnessRadixTrieData;

public class StringRadixTrieEdge extends AbstractRadixTrieEdge<RandomnessRadixTrieData, MemoryRadixTrieNode> implements
    RadixTrieEdge<RandomnessRadixTrieData, MemoryRadixTrieNode> {

  StringRadixTrieEdge(String[] label) {
    this.label = label;
  }

}
