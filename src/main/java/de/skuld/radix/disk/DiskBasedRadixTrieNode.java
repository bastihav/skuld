package de.skuld.radix.disk;

import de.skuld.radix.AbstractRadixTrieNode;
import de.skuld.radix.RadixTrieEdge;
import de.skuld.radix.RadixTrieNode;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import de.skuld.util.BytePrinter;
import java.util.Collection;
import java.util.Optional;

public class DiskBasedRadixTrieNode extends AbstractRadixTrieNode<RandomnessRadixTrieData, byte[], RandomnessRadixTrieDataPoint, PathRadixTrieEdge> implements RadixTrieNode<RandomnessRadixTrieData, PathRadixTrieEdge> {

  public DiskBasedRadixTrieNode(RandomnessRadixTrieData data) {
    this.data = data;
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
    return new String[0];
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
  public boolean removeEdge(PathRadixTrieEdge edge) {
    return false;
  }

  @Override
  public boolean addOutgoingEdge(PathRadixTrieEdge edge) {
    return false;
  }

  @Override
  public void setParentEdge(PathRadixTrieEdge parentEdge) {

  }

}
