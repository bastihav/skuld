package de.skuld.radix;

import de.skuld.radix.data.RandomnessRadixTrieData;
import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractRadixTrie<D extends AbstractRadixTrieData<I>, I, N extends RadixTrieNode<D, E>, E extends AbstractRadixTrieEdge<D, N>> implements RadixTrie<D, I, N, E> {
  protected N root = getDummyNode();

  @Override
  public @NotNull N getRoot() {
    return root;
  }

  protected boolean hasOutgoingEdge(N node, E edge) {
    return node.getOutgoingEdges().contains(edge);
  }

  protected boolean hasOutgoingEdge(N node, String label) {
    return node.getOutgoingEdge(label).isPresent();
  }

  @Override
  public boolean add(@NotNull D data, @NotNull I indexingData) {
    return add(root, data, indexingData);
  }

  @Override
  public boolean add(@NotNull N parent, @NotNull D data, @NotNull I indexingData) {
    if (parent == null) {
      N newNode = this.createNode(data, null);
      this.root = newNode;
      return true;
    }

    String[] edges = data.toLabels(indexingData);
    String[] coveredEdges = parent.getPathFromRoot();
    System.out.println("coveredEdges " + Arrays.toString(coveredEdges));

    if (parent.isLeafNode() && parent != root) {
      return parent.mergeNodes(data);
    }

    String[] remainingEdges = Arrays.copyOfRange(edges, coveredEdges.length, edges.length);
    System.out.println("remainingEdges " + Arrays.toString(remainingEdges));

    Optional<E> edge = parent.getOutgoingEdge(remainingEdges[0]);

    if (edge.isPresent()) {
      return add(edge.get().getChild(), data, indexingData);
    }

    for (E e : parent.getOutgoingEdges()) {
      System.out.println(e.isSummary());
      if (e.isSummary()) {
        System.out.println("walking summary");
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
            System.out.println("---");
            System.out.println("edge label: " + e.label);
            System.out.println(Arrays.toString(remainingEdges));
            System.out.println("-> split after " +  size);

            System.out.println("---");
            E firstEdge = this.createEdge(Arrays.copyOfRange(remainingEdges, 0, size));
            firstEdge.setSummary(size > 1);
            firstEdge.setAmountOfSummarizedElements(size);

            E secondEdge = this.createEdge(Arrays.copyOfRange(e.getLabel(), size, e.getLabel().length));
            secondEdge.setSummary(e.amountOfSummarizedElements() - size > 1);
            secondEdge.setAmountOfSummarizedElements(e.amountOfSummarizedElements() - size);

            N middleNode = createNode(null, firstEdge);
            middleNode.addOutgoingEdge(secondEdge);
            secondEdge.setParent(middleNode);
            firstEdge.setChild(middleNode);

            N oldSubtree = e.getChild();
            oldSubtree.setParentEdge(secondEdge);
            secondEdge.setChild(oldSubtree);

            parent.removeEdge(e);
            parent.addOutgoingEdge(firstEdge);
            firstEdge.setParent(parent);

            System.out.println("calling recursively");
            return add(middleNode, data, indexingData);
          }
        }

      }
    }

    System.out.println("-->");

    // last case: we branch off of here
    E newEdge = this.createEdge(remainingEdges);
    newEdge.setSummary(remainingEdges.length > 1);
    newEdge.setAmountOfSummarizedElements(remainingEdges.length);

    N newNode = this.createNode(data, newEdge);
    newEdge.setChild(newNode);
    newEdge.setParent(parent);
    parent.addOutgoingEdge(newEdge);

    System.out.println(parent.getOutgoingEdges());
    System.out.println(
        Arrays.toString(parent.getOutgoingEdges().stream().findFirst().get().getLabel()));
    return false;
  }

}
