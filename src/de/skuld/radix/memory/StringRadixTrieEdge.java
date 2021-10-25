package de.skuld.radix.memory;

import de.skuld.radix.AbstractRadixTrieEdge;
import de.skuld.radix.RadixTrieEdge;
import de.skuld.radix.data.RandomnessRadixTrieData;

public class StringRadixTrieEdge extends AbstractRadixTrieEdge<RandomnessRadixTrieData, MemoryRadixTrieNode> implements
    RadixTrieEdge<RandomnessRadixTrieData, MemoryRadixTrieNode> {

  boolean isSummary = false;
  int amountOfSummarizedElements = 0;

  StringRadixTrieEdge(String[] label) {
    this.label = label;
  }

  @Override
  public boolean isSummary() {
    return isSummary;
  }

  @Override
  public void setSummary(boolean isSummary) {
    this.isSummary = isSummary;
  }

  @Override
  public int amountOfSummarizedElements() {
    return amountOfSummarizedElements;
  }

  @Override
  public void setAmountOfSummarizedElements(int amountOfSummarizedElements) {
    this.amountOfSummarizedElements = amountOfSummarizedElements;
  }

}
