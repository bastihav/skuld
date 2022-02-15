package de.skuld.radix.memory;

import de.skuld.radix.AbstractRadixTrieNode;
import de.skuld.radix.RadixTrieNode;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

public class MemoryRadixTrieNode extends
    AbstractRadixTrieNode<RandomnessRadixTrieData, byte[], RandomnessRadixTrieDataPoint, StringRadixTrieEdge> implements
    RadixTrieNode<RandomnessRadixTrieData, StringRadixTrieEdge> {

  public static final MemoryRadixTrieNode DUMMY_NODE = new MemoryRadixTrieNode(false,
      new RandomnessRadixTrieData(null), null);
  Collection<StringRadixTrieEdge> outgoingEdges;

  public MemoryRadixTrieNode(boolean isRoot, RandomnessRadixTrieData data, MemoryRadixTrie trie) {
    super(isRoot, trie);
    this.data = data;
  }

  public MemoryRadixTrieNode(boolean isRoot, RandomnessRadixTrieData data, MemoryRadixTrie trie,
      StringRadixTrieEdge parentEdge) {
    this(isRoot, data, trie);
    this.parentEdge = parentEdge;
  }

  public MemoryRadixTrieNode(boolean isRoot, RandomnessRadixTrieData data, MemoryRadixTrie trie,
      StringRadixTrieEdge parentEdge, StringRadixTrieEdge childEdge) {
    this(isRoot, data, trie, parentEdge);
    this.addOutgoingEdge(parentEdge);
  }

  @Override
  public boolean isLeafNode() {
    return this.getOutgoingEdges().size() == 0;
  }

  @Override
  public String serialize() {
    return null;
  }

  @Override
  public String[] toEdgeDescriptors() {
    return new String[0];
  }

  @Override
  public Collection<StringRadixTrieEdge> getOutgoingEdges() {
    return Objects.requireNonNullElse(outgoingEdges, Collections.emptyList());
  }

  @Override
  public Optional<StringRadixTrieEdge> getOutgoingEdge(String label) {
    return getOutgoingEdges().stream()
        .filter(e -> e.getLabel().length == 1 && e.getLabel()[0].equals(label)).findFirst();
  }

  @Override
  public boolean removeEdge(StringRadixTrieEdge edge) {
    return this.outgoingEdges.remove(edge);
  }

  @Override
  public boolean addOutgoingEdge(StringRadixTrieEdge edge) {
    if (this.outgoingEdges == null) {
      this.outgoingEdges = new ArrayList<>();
    }
    if (this.outgoingEdges.contains(edge)) {
      return true;
    }
    return this.outgoingEdges.add(edge);
  }

  @Override
  public void setParentEdge(StringRadixTrieEdge parentEdge) {
    this.parentEdge = parentEdge;
  }
}
