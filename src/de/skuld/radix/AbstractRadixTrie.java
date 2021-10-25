package de.skuld.radix;

import de.skuld.radix.data.RandomnessRadixTrieData;
import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.NotImplementedException;

public abstract class AbstractRadixTrie<D extends AbstractRadixTrieData, N extends RadixTrieNode<D, E>, E extends AbstractRadixTrieEdge<D, N>> implements RadixTrie<D, N, E> {
  protected N root = getDummyNode();

  @Override
  public N getRoot() {
    return root;
  }

  protected boolean hasOutgoingEdge(N node, E edge) {
    return node.getOutgoingEdges().contains(edge);
  }

  protected boolean hasOutgoingEdge(N node, String label) {
    return node.getOutgoingEdge(label).isPresent();
  }

  @Override
  public boolean add(D data) {
    return add(root, data);
  }

  @Override
  public boolean add(N parent, D data) {
    if (parent == null) {
      N newNode = this.createNode(data, null);
      this.root = newNode;
      return true;
    }

    String[] edges = data.toLabels();
    String[] coveredEdges = parent.getPathFromRoot();

    if (parent.isLeafNode() && parent != root) {
      return parent.mergeNodes(data);
    }

    String[] remainingEdges = Arrays.copyOfRange(edges, coveredEdges.length, edges.length);
    Optional<E> edge = parent.getOutgoingEdge(remainingEdges[0]);

    if (edge.isPresent()) {
      return add(edge.get().getChild(), data);
    }

    for (E e : parent.getOutgoingEdges()) {
      System.out.println(e.isSummary());
      if (e.isSummary()) {
        System.out.println("walking summary");
        // walk this edge
        if (e.queryIncludesEdge(Arrays.stream(remainingEdges).reduce(String::concat).get())) {
          return add(e.getChild(), data);
        }

        String longestPrefix = "";
        int size = 0;
        for (int i = 1; i <= remainingEdges.length; i++) {
          String pref = Arrays.stream(remainingEdges).limit(i).reduce(String::concat).get();
          if (e.edgeIncludesQuery(pref)) {
            longestPrefix = pref;
            size = i;
          } else {
            break;
          }
        }

        if (!longestPrefix.isBlank()) {
          // merge nodes
          if (size == remainingEdges.length) {
            return add((N) e.getChild(), data);
          } else {
            // split edge
            throw new NotImplementedException();
          }
        }

      }
    }

    System.out.println("-->");

    // last case: we branch off of here
    E newEdge = this.createEdge(Arrays.stream(remainingEdges).reduce(String::concat).get());
    newEdge.setSummary(remainingEdges.length > 1);
    newEdge.setAmountOfSummarizedElements(remainingEdges.length);

    N newNode = this.createNode(data, newEdge);
    newEdge.setChild(newNode);
    parent.addOutgoingEdge(newEdge);

    System.out.println(parent.getOutgoingEdges());
    System.out.println(parent.getOutgoingEdges().stream().findFirst().get().getLabel());
    return false;
  }

  @Override
  public boolean add(N parent, N child) {
    return false;
  }

  public boolean add(N node){
    return add(root, node);
  }
}
