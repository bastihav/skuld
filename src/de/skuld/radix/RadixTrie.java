package de.skuld.radix;

import de.skuld.radix.data.RandomnessRadixTrieData;
import java.util.Collection;
import java.util.Optional;

public interface RadixTrie<D extends AbstractRadixTrieData, N extends RadixTrieNode<D, E>, E extends RadixTrieEdge<D, N>> {

  N getDummyNode();

  N getRoot();

  boolean add(N node);

  boolean add(D data);

  boolean add(N parent, N child);

  boolean add(N parent, D data);

  boolean addAll(Collection<N> nodes);

  boolean contains(D data);

  Optional<N> getNode(D data);

  boolean containsAll(Collection<N> nodes);

  E createEdge(String[] label);

  N createNode(D data, E parentEdge);
}
