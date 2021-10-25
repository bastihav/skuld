package de.skuld.radix.disk;

import de.skuld.radix.AbstractRadixTrieNode;
import de.skuld.radix.RadixTrieEdge;
import de.skuld.radix.RadixTrieNode;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.util.BytePrinter;
import java.util.Collection;
import java.util.Optional;

public class DiskBasedRadixTrieNode extends AbstractRadixTrieNode<RandomnessRadixTrieData, PathRadixTrieEdge> implements RadixTrieNode<RandomnessRadixTrieData, PathRadixTrieEdge> {

  private final short PARTITION_SIZE = 32;
  private final byte[] data;

  public DiskBasedRadixTrieNode(byte[] data) {
    if (data.length != PARTITION_SIZE) {
      throw new AssertionError("Node data size must equal partition size!");
    }
    this.data = data;
  }

  @Override
  public boolean mergeNodes(RadixTrieNode<RandomnessRadixTrieData, PathRadixTrieEdge> other) {
    return false;
  }

  @Override
  public boolean mergeNodes(RandomnessRadixTrieData otherData) {
    return false;
  }

  @Override
  public String[] getPathFromRoot() {
    return new String[0];
  }

  @Override
  public boolean isLeafNode() {
    return true;
  }

  @Override
  public String serialize() {
    return null;
  }

  @Override
  public String[] toEdgeDescriptors() {
    String[] result = new String[data.length];

    for (int i = 0; i < data.length; i++) {
      result[i] = BytePrinter.byteToHex(data[i]);
    }

    return result;
  }

  @Override
  public Collection<PathRadixTrieEdge> getOutgoingEdges() {
    return null;
  }

  @Override
  public Optional<PathRadixTrieEdge> getOutgoingEdge(String label) {
    return Optional.empty();
  }

  @Override
  public boolean addOutgoingEdge(PathRadixTrieEdge edge) {
    return false;
  }

  @Override
  public PathRadixTrieEdge getParentEdge() {
    return null;
  }

}
