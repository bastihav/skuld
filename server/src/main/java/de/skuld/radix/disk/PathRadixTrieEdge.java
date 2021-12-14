package de.skuld.radix.disk;

import de.skuld.radix.AbstractRadixTrieEdge;
import de.skuld.radix.RadixTrieEdge;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.disk.DiskBasedRadixTrieNode;
import java.nio.file.Path;

public class PathRadixTrieEdge extends AbstractRadixTrieEdge<DiskBasedRandomnessRadixTrieData, DiskBasedRadixTrieNode> implements
    RadixTrieEdge<DiskBasedRandomnessRadixTrieData, DiskBasedRadixTrieNode> {

  public Path getPath() {
    return path;
  }

  private final Path path;

  public PathRadixTrieEdge(String[] label, DiskBasedRadixTrieNode child, Path path) {
    this.label = label;
    this.setChild(child);
    this.path = path;
  }
}
