package de.skuld.radix;

import com.google.common.collect.ObjectArrays;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.stream.Stream;

public abstract class AbstractRadixTrieNode<D extends AbstractRadixTrieData<I, P>, I, P, E extends RadixTrieEdge<D, ? extends RadixTrieNode<D, E>>> implements
    RadixTrieNode<D, E> {

  protected final boolean isRoot;
  protected final AbstractRadixTrie<D, P, I, ? extends RadixTrieNode<D, E>, ? extends RadixTrieEdge<D, ? extends RadixTrieNode<D, E>>> trie;
  protected D data;
  protected E parentEdge;

  protected AbstractRadixTrieNode(boolean isRoot,
      AbstractRadixTrie<D, P, I, ? extends RadixTrieNode<D, E>, ? extends RadixTrieEdge<D, ? extends RadixTrieNode<D, E>>> trie) {
    this.isRoot = isRoot;
    this.trie = trie;
  }

  @Override
  public boolean mergeNodes(RadixTrieNode<D, E> other) {
    return mergeNodes(other.getData());
  }

  @Override
  public boolean mergeNodes(D otherData) {
    if (!this.hasData()) {
      this.data = otherData;
      return true;
    }
    this.getData().mergeData(otherData);
    return true;
  }

  @Override
  public D getData() {
    return data;
  }

  @Override
  public boolean hasData() {
    return this.data != null;
  }

  @Override
  public E getParentEdge() {
    return parentEdge;
  }

  @Override
  public String toString() {
    return "AbstractRadixTrieNode{" +
        "data=" + data +
        ", parentEdge=" + parentEdge +
        '}';
  }

  @Override
  public Deque<E> getEdgesFromRoot() {
    RadixTrieNode<D, E> currentNode = this;
    Deque<E> path = new ArrayDeque<>();
    while (currentNode != null && currentNode.getParentEdge() != null) {
      path.push(currentNode.getParentEdge());
      //noinspection unchecked
      currentNode = currentNode.getParentEdge().getParent();
    }

    return path;
  }

  @Override
  public String[] getPathFromRoot() {
    if (this.getParentEdge() == null) {
      return new String[0];
    } else {
      String[] parent = this.getParentEdge().getParent().getPathFromRoot();
      String[] last = this.getParentEdge().getLabel();

      return ObjectArrays.concat(parent, last, String.class);
    }
  }
}
