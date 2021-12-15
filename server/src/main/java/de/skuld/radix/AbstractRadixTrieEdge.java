package de.skuld.radix;

import java.util.Arrays;

public abstract class AbstractRadixTrieEdge<D extends AbstractRadixTrieData, N extends RadixTrieNode<D, ? extends RadixTrieEdge<D, N>>> implements
    RadixTrieEdge<D, N> {

  protected N child;
  protected N parent;
  protected String[] label;
  boolean isSummary = false;
  int amountOfSummarizedElements = 0;

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

  @Override
  public boolean edgeIncludesQuery(String[] query) {
    if (label.length < query.length) {
      return false;
    }

    for (int i = 0; i < query.length; i++) {
      if (!query[i].equals(label[i])) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String[] getLabel() {
    return label;
  }

  @Override
  public boolean queryIncludesEdge(String[] query) {
    if (label.length > query.length) {
      return false;
    }

    for (int i = 0; i < label.length; i++) {
      if (!query[i].equals(label[i])) {
        return false;
      }
    }
    return true;
  }

  @Override
  public N getChild() {
    return child;
  }

  @Override
  public void setChild(N child) {
    this.child = child;
  }

  @Override
  public N getParent() {
    return parent;
  }

  @Override
  public void setParent(N parent) {
    this.parent = parent;
  }

  @Override
  public String toString() {
    return "AbstractRadixTrieEdge{" +
        "label='" + Arrays.toString(label) + '\'' +
        '}';
  }
}
