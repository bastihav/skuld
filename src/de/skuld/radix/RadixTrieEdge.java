package de.skuld.radix;

public interface RadixTrieEdge<D extends AbstractRadixTrieData, N extends RadixTrieNode<D, ? extends RadixTrieEdge<D, N>>> {

  /**
   * Method that returns whether this edge is a summary edge, i.e. can be split into multiple edges
   *
   * @return isSummary
   */
  boolean isSummary();

  void setSummary(boolean isSummary);

  int amountOfSummarizedElements();

  void setAmountOfSummarizedElements(int amountOfSummarizedElements);

  boolean edgeIncludesQuery(String query);

  String getLabel();

  boolean queryIncludesEdge(String query);

  N getChild();

  N getParent();

  void setChild(N child);

}
