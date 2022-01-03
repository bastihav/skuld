package de.skuld.radix.memory;

import de.skuld.radix.AbstractRadixTrie;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import org.jetbrains.annotations.NotNull;

public class MemoryRadixTrie extends
    AbstractRadixTrie<RandomnessRadixTrieData, RandomnessRadixTrieDataPoint, byte[], MemoryRadixTrieNode, StringRadixTrieEdge> {

  @Override
  public @NotNull MemoryRadixTrieNode getDummyNode() {
    return new MemoryRadixTrieNode(false, null, null);
  }

  @Override
  public String[] getLabels(byte[] indexingData) {
    return RandomnessRadixTrieData.staticToLabels(indexingData);
  }

  @Override
  public void delete() {

  }

  @Override
  public void serializeMetaData() {
    // TODO
  }

  @Override
  public void generate() {
    // TODO
  }

  @Override
  public @NotNull StringRadixTrieEdge createEdge(String[] label,
      @NotNull MemoryRadixTrieNode parentNode) {
    StringRadixTrieEdge edge = new StringRadixTrieEdge(label);
    edge.setParent(parentNode);
    parentNode.addOutgoingEdge(edge);
    return edge;
  }

  @Override
  public @NotNull MemoryRadixTrieNode createNode(RandomnessRadixTrieData data,
      StringRadixTrieEdge parentEdge) {
    MemoryRadixTrieNode node = new MemoryRadixTrieNode(false, data, this, parentEdge);
    node.setParentEdge(parentEdge);
    parentEdge.setChild(node);
    return node;
  }
}
