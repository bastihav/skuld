package de.skuld.radix.disk;

import de.skuld.radix.AbstractRadixTrie;
import de.skuld.radix.RadixTrieEdge;
import de.skuld.radix.RadixTrieNode;
import de.skuld.radix.data.RandomnessRadixTrieData;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

public class DiskBasedRadixTrie extends
    AbstractRadixTrie<RandomnessRadixTrieData, DiskBasedRadixTrieNode, PathRadixTrieEdge> {

  @Override
  public boolean addAll(Collection nodes) {
    return false;
  }

  @Override
  public boolean containsAll(Collection nodes) {
    return false;
  }

  @Override
  public DiskBasedRadixTrieNode getDummyNode() {
    return null;
  }

  @Override
  public boolean contains(RandomnessRadixTrieData data) {
    return false;
  }

  @Override
  public Optional<DiskBasedRadixTrieNode> getNode(RandomnessRadixTrieData data) {
    return Optional.empty();
  }

  @Override
  public PathRadixTrieEdge createEdge(String label) {
    return null;
  }

  @Override
  public DiskBasedRadixTrieNode createNode(RandomnessRadixTrieData data,
      PathRadixTrieEdge parentEdge) {
    return null;
  }

}
