package de.skuld.radix.disk;

import de.skuld.radix.AbstractRadixTrie;
import de.skuld.radix.data.RandomnessRadixTrieData;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;

public class DiskBasedRadixTrie extends
    AbstractRadixTrie<RandomnessRadixTrieData, byte[], DiskBasedRadixTrieNode, PathRadixTrieEdge> {

  private final Path rootPath;

  public DiskBasedRadixTrie(Path rootPath) {
    this.rootPath = rootPath;
  }

  @Override
  public @NotNull DiskBasedRadixTrieNode getDummyNode() {
    return new DiskBasedRadixTrieNode();
  }

  @Override
  public @NotNull PathRadixTrieEdge createEdge(String[] label, @NotNull DiskBasedRadixTrieNode parentNode) {
    Deque<PathRadixTrieEdge> path = parentNode.getEdgesFromRoot();

    AtomicReference<Path> p = new AtomicReference<>(rootPath);

    path.forEach(edge -> p.set(p.get().resolve(Arrays.stream(edge.getLabel()).reduce(String::concat).orElse(""))));

    Path newPath = p.get();
    newPath = newPath.resolve(Arrays.stream(label).reduce(String::concat).orElse(""));

    //noinspection ResultOfMethodCallIgnored
    newPath.toFile().mkdir();

    return null;
  }

  @Override
  public @NotNull DiskBasedRadixTrieNode createNode(RandomnessRadixTrieData data,
      PathRadixTrieEdge parentEdge) {
    if (data == null) {
      return getDummyNode();
    }

    // TODO serialize RandomRadixTrieData into filesystem
    return null;
  }

  @Override
  public String[] getLabels(byte[] indexingData) {
    return RandomnessRadixTrieData.staticToLabels(indexingData);
  }
}
