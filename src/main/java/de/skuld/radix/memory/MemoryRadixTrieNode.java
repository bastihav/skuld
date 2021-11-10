package de.skuld.radix.memory;

import de.skuld.radix.AbstractRadixTrieNode;
import de.skuld.radix.RadixTrieEdge;
import de.skuld.radix.RadixTrieNode;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class MemoryRadixTrieNode extends AbstractRadixTrieNode<RandomnessRadixTrieData, byte[], RandomnessRadixTrieDataPoint, StringRadixTrieEdge> implements RadixTrieNode<RandomnessRadixTrieData, StringRadixTrieEdge> {

  Collection<StringRadixTrieEdge> outgoingEdges;
  public static final MemoryRadixTrieNode DUMMY_NODE = new MemoryRadixTrieNode(new RandomnessRadixTrieData(null));

  public MemoryRadixTrieNode(RandomnessRadixTrieData data) {
    this.data = data;
  }

  public MemoryRadixTrieNode(RandomnessRadixTrieData data, StringRadixTrieEdge parentEdge) {
    this(data);
    this.parentEdge = parentEdge;
  }

  public MemoryRadixTrieNode(RandomnessRadixTrieData data, StringRadixTrieEdge parentEdge, StringRadixTrieEdge childEdge) {
    this(data, parentEdge);
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
    return Objects.requireNonNullElse(outgoingEdges, Collections.EMPTY_LIST);
  }

  @Override
  public Optional<StringRadixTrieEdge> getOutgoingEdge(String label) {
    return getOutgoingEdges().stream().filter(e -> e.getLabel().length == 1 && e.getLabel()[0].equals(label)).findFirst();
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
    return this.outgoingEdges.add(edge);
  }

  @Override
  public void setParentEdge(StringRadixTrieEdge parentEdge) {
    this.parentEdge = parentEdge;
  }
}
