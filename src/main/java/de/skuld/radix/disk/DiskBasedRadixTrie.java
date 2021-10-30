package de.skuld.radix.disk;

import de.skuld.radix.AbstractRadixTrie;
import de.skuld.radix.RadixTrieEdge;
import de.skuld.radix.RadixTrieNode;
import de.skuld.radix.data.RandomnessRadixTrieData;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class DiskBasedRadixTrie extends
    AbstractRadixTrie<RandomnessRadixTrieData, byte[], DiskBasedRadixTrieNode, PathRadixTrieEdge> {


  @Override
  public @NotNull DiskBasedRadixTrieNode getDummyNode() {
    return null;
  }

  @Override
  public boolean contains(byte @NotNull [] indexingData) {
    return false;
  }

  @Override
  public Optional<DiskBasedRadixTrieNode> getNode(byte @NotNull [] indexingData) {
    return Optional.empty();
  }

  @Override
  public @NotNull PathRadixTrieEdge createEdge(String[] label) {
    return null;
  }

  @Override
  public @NotNull DiskBasedRadixTrieNode createNode(RandomnessRadixTrieData data,
      PathRadixTrieEdge parentEdge) {
    return null;
  }
}
