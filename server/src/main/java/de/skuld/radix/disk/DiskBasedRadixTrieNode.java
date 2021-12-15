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
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DiskBasedRadixTrieNode extends
    AbstractRadixTrieNode<DiskBasedRandomnessRadixTrieData, byte[], RandomnessRadixTrieDataPoint, PathRadixTrieEdge> implements
    RadixTrieNode<DiskBasedRandomnessRadixTrieData, PathRadixTrieEdge> {

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

    try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(file.toPath(), EnumSet.of(
        StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
      long readSizeInBytes = fileChannel.size();
      if (readSizeInBytes != 0) {
        System.out.println("File Should be empty");
      }
      byte[] serializedData = this.data.serialize(trie);

      long writeSizeInBytes = readSizeInBytes + serializedData.length;
      MappedByteBuffer mappedByteBuffer = fileChannel.map(MapMode.READ_WRITE, 0, writeSizeInBytes);
      mappedByteBuffer.position(0);
      mappedByteBuffer.put(serializedData);

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
    return Arrays.stream(p.toFile().list()).map(pathName -> {

      if (pathName.endsWith(ConfigurationHelper.getConfig().getString("radix.leaf.file_name"))) {
        //System.out.println("this is the path name " + pathName);
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
      String[] fragments = this.p.toString()
          .split(Pattern.quote(this.p.getFileSystem().getSeparator()));

      // TODO skuld config
      String[] relevantFragments = this.p.toString()
          .substring(this.p.toString().lastIndexOf("skuld"))
          .replace(ConfigurationHelper.getConfig().getString("radix.leaf.file_name"), "").split(
              Pattern.quote(this.p.getFileSystem().getSeparator()));

      // ignore first fragment as it is skuld/
      String[] labels = relevantFragments[relevantFragments.length - 1].split("(?<=\\G..)");

      PathRadixTrieEdge edge = new PathRadixTrieEdge(labels, this, p);

      String pathString = Arrays.stream(relevantFragments).limit(relevantFragments.length - 1)
          .reduce((a, b) -> a + this.p.getFileSystem().getSeparator() + b).get();

      DiskBasedRadixTrieNode parentNode = new DiskBasedRadixTrieNode(relevantFragments.length == 2,
          null, Paths.get(pathString), trie);
      edge.setParent(parentNode);

      edge.setSummary(fragments.length > 2);
      edge.setAmountOfSummarizedElements(fragments.length - 2);
      return edge;
    }
  }

  @Override
  public void setParentEdge(PathRadixTrieEdge parentEdge) {
    this.parentEdge = parentEdge;
  }

}