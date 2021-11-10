package de.skuld.radix;

import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.memory.StringRadixTrieEdge;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class AbstractRadixTrieNode<D extends AbstractRadixTrieData<I, P>, I, P, E extends RadixTrieEdge<D, ? extends RadixTrieNode<D, E>>> implements RadixTrieNode<D, E> {

  protected D data;
  protected E parentEdge;

  @Override
  public boolean mergeNodes(RadixTrieNode<D, E> other) {
    return mergeNodes(other.getData());
  }

  @Override
  public boolean mergeNodes(D otherData) {
    this.getData().mergeData(otherData);
    return true;
  }

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

  @Override
  public String[] getPathFromRoot() {
    if (this.getParentEdge() == null) {
      return new String[0];
    } else {
      Stream<String> parent = Arrays.stream(this.getParentEdge().getParent().getPathFromRoot());
      Stream<String> last = Stream.of(this.getParentEdge().getLabel());
      return Stream.concat(parent, last).toArray(String[]::new);
    }
  }
}
