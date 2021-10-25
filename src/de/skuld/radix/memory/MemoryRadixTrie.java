package de.skuld.radix.memory;

import de.skuld.radix.AbstractRadixTrie;
import de.skuld.radix.RadixTrieEdge;
import de.skuld.radix.RadixTrieNode;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.disk.DiskBasedRadixTrieNode;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class MemoryRadixTrie extends AbstractRadixTrie<RandomnessRadixTrieData, MemoryRadixTrieNode, StringRadixTrieEdge> {

  @Override
  public MemoryRadixTrieNode getDummyNode() {
    return MemoryRadixTrieNode.DUMMY_NODE;
  }

  @Override
  public boolean addAll(Collection<MemoryRadixTrieNode> nodes) {
    return false;
  }

  @Override
  public boolean contains(RandomnessRadixTrieData data) {
    System.out.println("looking for data " + data);
    String[] edgeLabels = data.toLabels();

    MemoryRadixTrieNode currentNode = getRoot();

    int i = 0;
    boolean advanced = true;

    while(!currentNode.isLeafNode() && advanced) {
      advanced = false;

      // match
      if (currentNode.getData() != null && currentNode.getData().equals(data))
        return true;

      // single edge
      Optional<StringRadixTrieEdge> target = currentNode.getOutgoingEdge(edgeLabels[0]);
      if (target.isPresent()) {
        currentNode = target.get().getChild();
        i++;
        advanced = true;
        continue;
      }

      // sumary edge
      String[] partial = Arrays.copyOfRange(edgeLabels, i, edgeLabels.length);

      for (StringRadixTrieEdge edge : currentNode.getOutgoingEdges()) {
        if (edge.queryIncludesEdge(partial)) {
          currentNode = edge.getChild();
          i += edge.amountOfSummarizedElements();
          advanced = true;
          System.out.println("found summary -> " + edge.getLabel());
          break;
        }
      }
    }

    System.out.println(currentNode);

    if (currentNode.getData() != null && currentNode.getData().equals(data)) {
      System.out.println("data -> " + currentNode.getData());
      return true;
    }

    return false;
  }

  @Override
  public Optional<MemoryRadixTrieNode> getNode(RandomnessRadixTrieData data) {
    String[] edgeLabels = data.toLabels();

    MemoryRadixTrieNode currentNode = getRoot();

    int i = 0;
    boolean advanced = true;

    while(!currentNode.isLeafNode() && advanced) {
      advanced = false;

      // match
      if (currentNode.getData() != null && currentNode.getData().equals(data))
        return Optional.of(currentNode);

      // single edge
      Optional<StringRadixTrieEdge> target = currentNode.getOutgoingEdge(edgeLabels[0]);
      if (target.isPresent()) {
        currentNode = target.get().getChild();
        i++;
        advanced = true;
        continue;
      }

      // sumary edge
      String[] partial = Arrays.copyOfRange(edgeLabels, i, edgeLabels.length);
      for (StringRadixTrieEdge edge : currentNode.getOutgoingEdges()) {
        if (edge.queryIncludesEdge(partial)) {
          currentNode = edge.getChild();
          i += edge.amountOfSummarizedElements();
          advanced = true;
          break;
        }
      }
    }

    if (currentNode.getData() != null && currentNode.getData().equals(data)) {
      return Optional.of(currentNode);
    }

    return Optional.empty();
  }

  @Override
  public boolean containsAll(Collection<MemoryRadixTrieNode> nodes) {
    return false;
  }

  @Override
  public StringRadixTrieEdge createEdge(String[] label) {
    return new StringRadixTrieEdge(label);
  }

  @Override
  public MemoryRadixTrieNode createNode(RandomnessRadixTrieData data,
      StringRadixTrieEdge parentEdge) {
    return new MemoryRadixTrieNode(data, parentEdge);
  }
}
