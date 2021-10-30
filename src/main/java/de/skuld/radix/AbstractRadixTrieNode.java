package de.skuld.radix;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Optional;

public abstract class AbstractRadixTrieNode<D extends AbstractRadixTrieData<?>, E extends RadixTrieEdge<D, ? extends RadixTrieNode<D, E>>> implements RadixTrieNode<D, E> {

  protected D data;
  protected E parentEdge;

  @Override
  public D getData() {
    return data;
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
      currentNode = currentNode.getParentEdge().getParent();
    }

    return path;
  }
}
