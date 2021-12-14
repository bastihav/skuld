package de.skuld.radix.disk;

import de.skuld.radix.AbstractRadixTrieNode;
import de.skuld.radix.RadixTrieNode;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DiskBasedRadixTrieNode extends AbstractRadixTrieNode<DiskBasedRandomnessRadixTrieData, byte[], RandomnessRadixTrieDataPoint, PathRadixTrieEdge> implements RadixTrieNode<DiskBasedRandomnessRadixTrieData, PathRadixTrieEdge> {

  private final Path p;
  private final DiskBasedRadixTrie trie;

  public DiskBasedRadixTrieNode(boolean isRoot, DiskBasedRandomnessRadixTrieData data, Path path, DiskBasedRadixTrie trie) {
    super(isRoot, trie);
    this.data = data;
    this.p = path;
    this.trie = trie;
  }

  @Override
  public DiskBasedRandomnessRadixTrieData getData() {
    if (!this.isLeafNode()) {
      System.out.println("not a leaf node!");
      return null;
    }
    if (this.data == null && this.p.endsWith("table.bin")) {
      this.data = new DiskBasedRandomnessRadixTrieData(p);
    }
    return this.data;
  }

  public Path getPath() {
    return p;
  }

  @Override
  public boolean isLeafNode() {
    //System.out.println("P -> " + this.p);
    //System.out.println(this.p.endsWith("table.bin"));
    return this.p.endsWith("table.bin");
  }

  @Override
  public boolean hasData() {
    return this.p.endsWith("table.bin");
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
    //System.out.println("Serializing!");
    //TODO config
    File file = p.resolve("table.bin").toFile();

    boolean existed = file.exists();

    try {
      file.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }

    try (FileOutputStream fileOutputStream = new FileOutputStream(file, true)) {
      /*for (RandomnessRadixTrieDataPoint randomnessRadixTrieDataPoint : data.getDataPoints()) {
        ImplementedPRNGs rng = randomnessRadixTrieDataPoint.getRng();
        fileOutputStream.write(rng.ordinal());
      }*/
      fileOutputStream.write(this.data.serialize(trie));
      //fileOutputStream.write(new byte[]{0x00,0x01,0x1F,0x6F});
      fileOutputStream.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }

    //throw new NotImplementedException("serializeee");
    return "";
  }

  @Override
  public String[] toEdgeDescriptors() {
    return new String[0];
  }

  @Override
  public Collection<PathRadixTrieEdge> getOutgoingEdges() {
    return Arrays.stream(p.toFile().list()).map(pathName -> {

      if (pathName.endsWith("bin")) {
        //System.out.println("this is the path name " + pathName);
        PathRadixTrieEdge edge = new PathRadixTrieEdge(new String[0], new DiskBasedRadixTrieNode(false,null, p.resolve(pathName), trie), p.resolve(pathName));
        edge.setSummary(false);
        edge.setAmountOfSummarizedElements(0);
        edge.setParent(this);
        return edge;
      }

      // split after two characters
      String[] labels = pathName.split("(?<=\\G..)");
      PathRadixTrieEdge edge = new PathRadixTrieEdge(labels, new DiskBasedRadixTrieNode(false,null, p.resolve(pathName), trie), p.resolve(pathName));
      edge.setSummary(true);
      edge.setAmountOfSummarizedElements(labels.length);
      edge.setParent(this);
      return edge;
    }).collect(Collectors.toList());
  }

  @Override
  public Optional<PathRadixTrieEdge> getOutgoingEdge(String label) {
    //System.out.println("looking for " + label);
    //System.out.println(Arrays.toString(p.toFile().listFiles()));
    //System.out.println(p.resolve(label).toFile());
    if (!p.resolve(label).toFile().exists()) {
      return Optional.empty();
    } else {
      return Optional.of(new PathRadixTrieEdge(new String[]{label}, new DiskBasedRadixTrieNode(false,null, p.resolve(label), trie), p.resolve(label)));
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
  public void setParentEdge(PathRadixTrieEdge parentEdge) {
    this.parentEdge = parentEdge;
  }

  @Override
  public PathRadixTrieEdge getParentEdge() {
    // TODO
    if (isRoot) {
      return null;
    } else {
      String[] fragments = this.p.toString().split(Pattern.quote(this.p.getFileSystem().getSeparator()));

      String[] relevantFragments = this.p.toString().substring(this.p.toString().lastIndexOf("skuld")).replace("table.bin", "").split(
          Pattern.quote(this.p.getFileSystem().getSeparator()));

      // ignore first fragment as it is skuld/
      String[] labels = relevantFragments[relevantFragments.length - 1].split("(?<=\\G..)");

      PathRadixTrieEdge edge = new PathRadixTrieEdge(labels, this, p);

      String pathString = Arrays.stream(relevantFragments).limit(relevantFragments.length-1).reduce((a,b) -> a + this.p.getFileSystem().getSeparator() + b).get();

      DiskBasedRadixTrieNode parentNode = new DiskBasedRadixTrieNode(relevantFragments.length == 2, null, Paths.get(pathString), trie);
      edge.setParent(parentNode);

      edge.setSummary(fragments.length > 2);
      edge.setAmountOfSummarizedElements(fragments.length - 2);
      return edge;
    }
  }

}
