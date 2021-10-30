package de.skuld.radix.memory;

import de.skuld.radix.AbstractRadixTrie;
import de.skuld.radix.RadixTrieEdge;
import de.skuld.radix.RadixTrieNode;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.disk.DiskBasedRadixTrieNode;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class MemoryRadixTrie extends AbstractRadixTrie<RandomnessRadixTrieData, byte[], MemoryRadixTrieNode, StringRadixTrieEdge> {

  @Override
  public @NotNull MemoryRadixTrieNode getDummyNode() {
    return new MemoryRadixTrieNode();
  }

  @Override
  public boolean contains(byte @NotNull [] indexingData) {
    System.out.println("looking for data " + indexingData);
    String[] edgeLabels = RandomnessRadixTrieData.staticToLabels(indexingData);

    MemoryRadixTrieNode currentNode = getRoot();

    int i = 0;
    boolean advanced = true;

    while(!currentNode.isLeafNode() && advanced) {
      advanced = false;

      // match
      if (Arrays.equals(currentNode.getPathFromRoot(), edgeLabels))
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
          System.out.println("found summary -> " + Arrays.toString(edge.getLabel()));
          break;
        }
      }
    }

    return Arrays.equals(currentNode.getPathFromRoot(), edgeLabels);
  }

  @Override
  public Optional<MemoryRadixTrieNode> getNode(byte @NotNull [] indexingData) {
    String[] edgeLabels = RandomnessRadixTrieData.staticToLabels(indexingData);

    MemoryRadixTrieNode currentNode = getRoot();

    int i = 0;
    boolean advanced = true;

    while(!currentNode.isLeafNode() && advanced) {
      advanced = false;

      // match
      if (Arrays.equals(currentNode.getPathFromRoot(), edgeLabels))
        return Optional.of(currentNode);

      // single edge
      Optional<StringRadixTrieEdge> target = currentNode.getOutgoingEdge(edgeLabels[0]);
      if (target.isPresent()) {
        currentNode = target.get().getChild();
        i++;
        advanced = true;
        continue;
      }

      // summary edge
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

    if (Arrays.equals(currentNode.getPathFromRoot(), edgeLabels))
      return Optional.of(currentNode);

    return Optional.empty();
  }

  @Override
  public @NotNull StringRadixTrieEdge createEdge(String[] label) {
    return new StringRadixTrieEdge(label);
  }

  @Override
  public @NotNull MemoryRadixTrieNode createNode(RandomnessRadixTrieData data,
      StringRadixTrieEdge parentEdge) {
    return new MemoryRadixTrieNode(data, parentEdge);
  }
}
