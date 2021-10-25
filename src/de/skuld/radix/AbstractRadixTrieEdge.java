package de.skuld.radix;

public abstract class AbstractRadixTrieEdge<D extends AbstractRadixTrieData, N extends RadixTrieNode<D, ? extends RadixTrieEdge<D, N>>> implements RadixTrieEdge<D, N> {
  protected N child;
  protected N parent;
  protected String label;

  @Override
  public boolean edgeIncludesQuery(String query) {
    return this.label.startsWith(query);
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public boolean queryIncludesEdge(String query) {
    return query.startsWith(this.label);
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
  public String toString() {
    return "AbstractRadixTrieEdge{" +
        "label='" + label + '\'' +
        '}';
  }
}
