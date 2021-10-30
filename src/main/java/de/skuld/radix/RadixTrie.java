package de.skuld.radix;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Generic de.skuld.radix trie interface
 *
 * Example:
 *  Leaves store meta-information on randomness, i.e. seed and algorithm
 *  Their position is based on the randomness, which is a byte array
 *
 * @param <D> Data that will be stored in leaves
 * @param <I> datatype that describes the position of a node
 * @param <N> Node type
 * @param <E> Edge type
 */
public interface RadixTrie<D extends AbstractRadixTrieData<I>, I, N extends RadixTrieNode<D, E>, E extends RadixTrieEdge<D, N>> {

  /**
   * Method that returns an instance of a dummy node.
   * The dummy node is used as non-leaves and may have null-data.
   * Depending on the implementation, this node may be a singleton.
   *
   * @return dummy node instance
   */
  @NotNull
  N getDummyNode();

  /**
   * Method that returns root node of the trie.
   *
   * @return root node
   */
  @NotNull
  N getRoot();

  /**
   * Method that inserts data in the trie. Position is based on indexingData.
   *
   * @param data leaf data
   * @param indexingData indexing data
   * @return true if the data was successfully added
   */
  boolean add(@NotNull D data, @NotNull I indexingData);

  /**
   * Method that inserts data in the sub-trie under a node. Position is based on indexingData.
   *
   * @param parent parent node
   * @param data leaf data
   * @param indexingData indexing data
   * @return true if the data was successfully added
   */
  boolean add(@NotNull N parent, @NotNull D data, @NotNull I indexingData);

  /**
   * Methot that returns whether a node at the position described by indexingdata exists.
   *
   * @param indexingData position query
   * @return true if the node exists
   */
  boolean contains(@NotNull I indexingData);

  /**
   * Method that optionally returns a node from position indexed by indexingData.
   *
   * @param indexingData position query
   * @return Optional with node, if present
   */
  Optional<N> getNode(@NotNull I indexingData);

  /**
   * Method that creates a new edge for the de.skuld.radix trie. The edge should not yet be connected or inserted in the trie.
   *
   * @param label edge label
   * @param parentNode parent node
   * @return new edge instance
   */
  @NotNull
  E createEdge(@NotNull String[] label, @NotNull N parentNode);

  /**
   * Method that creates a new node for the de.skuld.radix trie. The edge should not yet be inserted in the trie.
   *
   * @param data node data. May be null if this is not a leaf
   * @param parentEdge parent edge. May be null if this is the root element
   * @return new node instance
   */
  @NotNull
  N createNode(@Nullable D data, @Nullable E parentEdge);
}
