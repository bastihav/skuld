package de.skuld.radix;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Optional;
import java.util.Stack;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractRadixTrie<D extends AbstractRadixTrieData<I>, I, N extends RadixTrieNode<D, E>, E extends AbstractRadixTrieEdge<D, N>> implements RadixTrie<D, I, N, E> {
  protected N root = getDummyNode();

  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public @NotNull N getRoot() {
    return root;
  }

  @Override
  public boolean add(@NotNull D data, @NotNull I indexingData) {
    return add(root, data, indexingData);
  }

  @Override
  public boolean add(@NotNull N parent, @NotNull D data, @NotNull I indexingData) {
    String[] edges = data.toLabels(indexingData);
    String[] coveredEdges = parent.getPathFromRoot();

    if (parent.isLeafNode() && parent != root) {
      return parent.mergeNodes(data);
    }

    String[] remainingEdges = Arrays.copyOfRange(edges, coveredEdges.length, edges.length);

    Optional<E> edge = parent.getOutgoingEdge(remainingEdges[0]);

    if (edge.isPresent()) {
      return add(edge.get().getChild(), data, indexingData);
    }

    for (E e : parent.getOutgoingEdges()) {
      if (e.isSummary()) {
        // walk this edge
        if (e.queryIncludesEdge(remainingEdges)) {
          return add(e.getChild(), data, indexingData);
        }

        int size = 0;
        for (int i = 1; i <= remainingEdges.length; i++) {
          if (e.edgeIncludesQuery(Arrays.copyOfRange(remainingEdges, 0, i))) {
            size = i;
          } else {
            break;
          }
        }

        if (size > 0) {
          // merge nodes
          if (size == remainingEdges.length) {
            return add(e.getChild(), data, indexingData);
          } else {
            // split edge
            E firstEdge = this.createEdge(Arrays.copyOfRange(remainingEdges, 0, size), parent);
            firstEdge.setSummary(size > 1);
            firstEdge.setAmountOfSummarizedElements(size);

            N middleNode = createNode(null, firstEdge);

            E secondEdge = this.createEdge(Arrays.copyOfRange(e.getLabel(), size, e.getLabel().length), middleNode);
            secondEdge.setSummary(e.amountOfSummarizedElements() - size > 1);
            secondEdge.setAmountOfSummarizedElements(e.amountOfSummarizedElements() - size);

            N oldSubtree = e.getChild();
            oldSubtree.setParentEdge(secondEdge);
            secondEdge.setChild(oldSubtree);

            parent.removeEdge(e);

            return add(middleNode, data, indexingData);
          }
        }

      }
    }

    // last case: we branch off of here
    E newEdge = this.createEdge(remainingEdges, parent);
    newEdge.setSummary(remainingEdges.length > 1);
    newEdge.setAmountOfSummarizedElements(remainingEdges.length);

    N newNode = this.createNode(data, newEdge);

    if (LOGGER.getLevel() == Level.DEBUG) {
      StringBuilder sb = new StringBuilder();
      sb.append("Created new node for data: {").append(Arrays.toString(edges)).append("} with path (from root): ");
      newNode.getEdgesFromRoot().forEach(e -> sb.append(Arrays.toString(e.getLabel())).append(", "));
      LOGGER.debug(sb.toString());
    }

    return true;
  }

}
