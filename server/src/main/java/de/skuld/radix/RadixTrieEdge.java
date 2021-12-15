package de.skuld.radix;

public interface RadixTrieEdge<D extends AbstractRadixTrieData, N extends RadixTrieNode<D, ? extends RadixTrieEdge<D, N>>> {

  /**
   * Method that returns whether this edge is a summary edge, i.e. can be split into multiple edges
   *
   * @return isSummary
   */
  default boolean isSummary() {
    return getLabel().length > 1;
  }

  void setSummary(boolean isSummary);

  default int amountOfSummarizedElements() {
    return getLabel().length;
  }

  void setAmountOfSummarizedElements(int amountOfSummarizedElements);

  boolean edgeIncludesQuery(String[] query);

  String[] getLabel();

  boolean queryIncludesEdge(String[] query);

  N getChild();

  void setChild(N child);

  N getParent();

  void setParent(N parent);

}
