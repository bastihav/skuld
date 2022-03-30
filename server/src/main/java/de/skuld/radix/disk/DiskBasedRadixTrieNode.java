package de.skuld.radix.disk;

import de.skuld.radix.AbstractRadixTrieNode;
import de.skuld.radix.RadixTrieNode;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import de.skuld.util.ConfigurationHelper;
import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DiskBasedRadixTrieNode extends
    AbstractRadixTrieNode<DiskBasedRandomnessRadixTrieData, byte[], RandomnessRadixTrieDataPoint, PathRadixTrieEdge> implements
    RadixTrieNode<DiskBasedRandomnessRadixTrieData, PathRadixTrieEdge> {

  private static final Logger LOGGER = LogManager.getLogger();
  private final Path p;
  private final DiskBasedRadixTrie trie;

  public DiskBasedRadixTrieNode(boolean isRoot, DiskBasedRandomnessRadixTrieData data, Path path,
      DiskBasedRadixTrie trie) {
    super(isRoot, trie);
    this.data = data;
    this.p = path;
    this.trie = trie;
  }

  @Override
  public DiskBasedRandomnessRadixTrieData getData() {
    if (!this.isLeafNode()) {
      return null;
    }
    if (this.data == null && this.p
        .endsWith(ConfigurationHelper.getConfig().getString("radix.leaf.file_name"))) {
      this.data = new DiskBasedRandomnessRadixTrieData(p);
    }
    return this.data;
  }

  public Path getPath() {
    return p;
  }

  @Override
  public boolean isLeafNode() {
    return this.p.endsWith(ConfigurationHelper.getConfig().getString("radix.leaf.file_name"));
  }

  @Override
  public boolean hasData() {
    return this.p.endsWith(ConfigurationHelper.getConfig().getString("radix.leaf.file_name"));
  }

  @Override
  public String toString() {
    return "AbstractRadixTrieNode{" +
        "data=" + data +
        ", parentEdge=" + parentEdge +
        ", path=" + p +
        '}';
  }

  @Override
  public String serialize() {
    File file = p.resolve(ConfigurationHelper.getConfig().getString("radix.leaf.file_name"))
        .toFile();
    int sizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.partition.serialized");

    try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(file.toPath(), EnumSet.of(
        StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
      long readSizeInBytes = fileChannel.size();
      if (readSizeInBytes != 0) {
        LOGGER.error("Leaf file at " + p + " not empty");
      }

      long writeSize = (long) this.data.getElementCount() * sizeOnDisk;

      MappedByteBuffer mappedByteBuffer = fileChannel.map(MapMode.READ_WRITE, readSizeInBytes,
          writeSize);

      this.data.serialize(mappedByteBuffer, 0);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return "";
  }

  @Override
  public String[] toEdgeDescriptors() {
    return new String[0];
  }

  @Override
  public Collection<PathRadixTrieEdge> getOutgoingEdges() {
    return Arrays.stream(Objects.requireNonNull(p.toFile().list())).map(pathName -> {

      if (pathName.endsWith(ConfigurationHelper.getConfig().getString("radix.leaf.file_name"))) {
        PathRadixTrieEdge edge = new PathRadixTrieEdge(new String[0],
            new DiskBasedRadixTrieNode(false, null, p.resolve(pathName), trie),
            p.resolve(pathName));
        edge.setSummary(false);
        edge.setAmountOfSummarizedElements(0);
        edge.setParent(this);
        return edge;
      }

      // split after two characters
      String[] labels = pathName.split("(?<=\\G..)");
      PathRadixTrieEdge edge = new PathRadixTrieEdge(labels,
          new DiskBasedRadixTrieNode(false, null, p.resolve(pathName), trie), p.resolve(pathName));
      edge.setSummary(true);
      edge.setAmountOfSummarizedElements(labels.length);
      edge.setParent(this);
      return edge;
    }).collect(Collectors.toList());
  }

  @Override
  public Optional<PathRadixTrieEdge> getOutgoingEdge(String label) {
    if (!p.resolve(label).toFile().exists()) {
      return Optional.empty();
    } else {
      return Optional.of(new PathRadixTrieEdge(new String[]{label},
          new DiskBasedRadixTrieNode(false, null, p.resolve(label), trie), p.resolve(label)));
    }
  }

  @Override
  public boolean removeEdge(PathRadixTrieEdge edge) {
    return false;
  }

  @Override
  public boolean addOutgoingEdge(PathRadixTrieEdge edge) {
    return false;
  }

  @Override
  public PathRadixTrieEdge getParentEdge() {
    if (isRoot) {
      return null;
    } else {
      String[] relevantFragments = removePathPrefix(this.p)
          .replace(ConfigurationHelper.getConfig().getString("radix.leaf.file_name"), "").split(
              Pattern.quote(this.p.getFileSystem().getSeparator()));

      String[] labels;
      // last fragment is the current edge we are interested in
      if (relevantFragments.length > 0) {
        labels = relevantFragments[relevantFragments.length - 1].split("(?<=\\G..)");
      } else {
        labels = new String[0];
      }

      PathRadixTrieEdge edge = new PathRadixTrieEdge(labels, this, p);

      Path pathString;
      if (relevantFragments.length > 0) {
        if (this.p.toString()
            .endsWith(ConfigurationHelper.getConfig().getString("radix.leaf.file_name"))) {
          pathString = this.getPath().getRoot().resolve(
              this.p.subpath(0, this.p.getNameCount() - 2));
        } else {
          pathString = this.getPath().getRoot().resolve(
              this.p.subpath(0, this.p.getNameCount() - 1));
        }
      } else {
        pathString = this.p;
      }

      DiskBasedRadixTrieNode parentNode = new DiskBasedRadixTrieNode(relevantFragments.length == 1,
          null, pathString, trie);
      edge.setParent(parentNode);

      edge.setSummary(labels.length > 2);
      edge.setAmountOfSummarizedElements(labels.length);
      return edge;
    }
  }

  @Override
  public void setParentEdge(PathRadixTrieEdge parentEdge) {
    this.parentEdge = parentEdge;
  }

  private String removePathPrefix(Path path) {
    Path trieRootPath = this.trie.getRoot().getPath();
    if (trieRootPath.getNameCount() == path.getNameCount()) {
      return "";
    }

    return path.subpath(trieRootPath.getNameCount(), path.getNameCount()).toString();
  }

}
