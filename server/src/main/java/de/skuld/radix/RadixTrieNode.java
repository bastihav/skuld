package de.skuld.radix;

import java.util.Collection;
import java.util.Deque;
import java.util.Optional;

public interface RadixTrieNode<D extends AbstractRadixTrieData<?, ?>, E extends RadixTrieEdge<D, ? extends RadixTrieNode<D, E>>> {

  boolean mergeNodes(RadixTrieNode<D, E> other);

  boolean mergeNodes(D otherData);

  String[] getPathFromRoot();

  D getData();

  boolean hasData();

  boolean isLeafNode();

  String serialize();

  String[] toEdgeDescriptors();

  Collection<E> getOutgoingEdges();

  Optional<E> getOutgoingEdge(String label);

  @SuppressWarnings("UnusedReturnValue")
  boolean removeEdge(E edge);

  @SuppressWarnings("UnusedReturnValue")
  boolean addOutgoingEdge(E edge);

  E getParentEdge();

  void setParentEdge(E parentEdge);

  Deque<E> getEdgesFromRoot();
}
