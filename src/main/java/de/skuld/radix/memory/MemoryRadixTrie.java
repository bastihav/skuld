package de.skuld.radix.memory;

import de.skuld.radix.AbstractRadixTrie;
import de.skuld.radix.RadixTrieEdge;
import de.skuld.radix.RadixTrieNode;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.disk.DiskBasedRadixTrieNode;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class MemoryRadixTrie extends AbstractRadixTrie<RandomnessRadixTrieData, byte[], MemoryRadixTrieNode, StringRadixTrieEdge> {

  @Override
  public @NotNull MemoryRadixTrieNode getDummyNode() {
    return new MemoryRadixTrieNode();
  }

  @Override
  public String[] getLabels(byte[] indexingData) {
    return RandomnessRadixTrieData.staticToLabels(indexingData);
  }

  @Override
  public @NotNull StringRadixTrieEdge createEdge(String[] label, @NotNull MemoryRadixTrieNode parentNode) {
    StringRadixTrieEdge edge = new StringRadixTrieEdge(label);
    edge.setParent(parentNode);
    parentNode.addOutgoingEdge(edge);
    return edge;
  }

  @Override
  public @NotNull MemoryRadixTrieNode createNode(RandomnessRadixTrieData data,
      StringRadixTrieEdge parentEdge) {
    MemoryRadixTrieNode node = new MemoryRadixTrieNode(data, parentEdge);
    node.setParentEdge(parentEdge);
    parentEdge.setChild(node);
    return node;
  }
}
