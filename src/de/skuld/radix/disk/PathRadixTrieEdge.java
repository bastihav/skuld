package de.skuld.radix.disk;

import de.skuld.radix.AbstractRadixTrieEdge;
import de.skuld.radix.RadixTrieEdge;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.disk.DiskBasedRadixTrieNode;

public class PathRadixTrieEdge extends AbstractRadixTrieEdge<RandomnessRadixTrieData, DiskBasedRadixTrieNode> implements
    RadixTrieEdge<RandomnessRadixTrieData, DiskBasedRadixTrieNode> {

  public PathRadixTrieEdge(String[] label, DiskBasedRadixTrieNode child) {
    this.label = label;
    this.setChild(child);
  }

  @Override
  public boolean isSummary() {
    return false;
  }

  @Override
  public void setSummary(boolean isSummary) {
  }

  @Override
  public int amountOfSummarizedElements() {
    return 0;
  }

  @Override
  public void setAmountOfSummarizedElements(int amountOfSummarizedElements) {

  }

}
