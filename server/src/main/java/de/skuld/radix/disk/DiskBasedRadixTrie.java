package de.skuld.radix.disk;

import de.skuld.radix.AbstractRadixTrie;
import de.skuld.radix.controller.RNGManager;
import de.skuld.radix.controller.SeedManager;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DiskBasedRadixTrie extends
    AbstractRadixTrie<DiskBasedRandomnessRadixTrieData, RandomnessRadixTrieDataPoint, byte[], DiskBasedRadixTrieNode, PathRadixTrieEdge> {

  private final Path rootPath;

  /**
   * Creates a new DiskBasedRadixTrie at the specified location.
   * @param rootPath path that will be used as root of the trie
   * @param date date (might be null) for which to generate seeds. If null, will use the current system time.
   */
  public DiskBasedRadixTrie(Path rootPath, @Nullable Date date) {
    this.rootPath = rootPath;
    this.root = getDummyNode();

    if (existsOnDisk()) {
      deserializeSeedMap();
    } else {
      RNGManager rngManager = new RNGManager();
      SeedManager seedManager = new SeedManager(rngManager);
      long[] seeds = seedManager.getSeeds(Objects.requireNonNullElse(date, new Date()));
      fillSeedMap(seeds);
      serializeSeedMap();
    }
  }

  @Override
  public @NotNull DiskBasedRadixTrieNode getDummyNode() {
    return new DiskBasedRadixTrieNode(true,null, rootPath, this);
  }

  @Override
  public @NotNull PathRadixTrieEdge createEdge(String[] label, @NotNull DiskBasedRadixTrieNode parentNode) {
    Deque<PathRadixTrieEdge> path = parentNode.getEdgesFromRoot();
    Path newPath = parentNode.getPath().resolve(Arrays.stream(label).reduce(String::concat).get());

    //noinspection ResultOfMethodCallIgnored
    newPath.toFile().mkdir();

    PathRadixTrieEdge edge = new PathRadixTrieEdge(label, new DiskBasedRadixTrieNode(false,null, newPath, this), newPath);
    edge.setParent(parentNode);
    return edge;
  }

  @Override
  public @NotNull DiskBasedRadixTrieNode createNode(DiskBasedRandomnessRadixTrieData data,
      PathRadixTrieEdge parentEdge) {
    if (data == null) {
      String label = Arrays.stream(parentEdge.getLabel()).reduce((a,b) -> a + "" + b).orElseGet(String::new);
      Path newPath = parentEdge.getParent().getPath().resolve(label + rootPath.getFileSystem().getSeparator());

      return new DiskBasedRadixTrieNode(false,null, newPath, this);
    } else {
      DiskBasedRadixTrieNode node = new DiskBasedRadixTrieNode(false, data, parentEdge.getPath(), this);
      node.serialize();
      return node;
    }
  }

  @Override
  public String[] getLabels(byte[] indexingData) {
    return RandomnessRadixTrieData.staticToLabels(indexingData);
  }

  @Override
  public boolean moveSubtree(DiskBasedRadixTrieNode src, PathRadixTrieEdge edge,
      DiskBasedRadixTrieNode dest) {
    Path srcPath = src.getPath();
    Path edgePath = edge.getPath();

    try {
      Files.move(srcPath, edgePath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return super.moveSubtree(src, edge, dest);
  }

  @Override
  public boolean contains(byte @NotNull [] indexingData) {
    Optional<DiskBasedRadixTrieNode> node = super.getNode(indexingData);
    //System.out.println(node + " is the node");
    return node.filter(
        diskBasedRadixTrieNode -> (diskBasedRadixTrieNode
            .getData()).getDataPoint(indexingData).isPresent()).isPresent();
  }

  private boolean existsOnDisk() {
    return this.rootPath.resolve("seeds.bin").toFile().exists();
  }

  private void deserializeSeedMap() {
    //System.out.println("deserializing seed map");
    // TODO config
    Path seedMapPath = this.rootPath.resolve("seeds.bin");

    try(FileChannel fileChannel = (FileChannel) Files.newByteChannel(seedMapPath, EnumSet.of(
        StandardOpenOption.READ))) {
      MappedByteBuffer mappedByteBuffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size());
      long amountOfSeeds = fileChannel.size() / Long.BYTES;

      for (int i = 0; i < amountOfSeeds; i++) {
        seedMap.put(i, mappedByteBuffer.getLong());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    //System.out.println("420 is " + seedMap.get(420));
  }

  private void fillSeedMap(long[] seeds) {
    for(int i = 0; i < seeds.length; i++) {
      this.seedMap.put(i, seeds[i]);
    }
  }

  private void serializeSeedMap() {
    // TODO config
    //System.out.println("serializing seed map");
    Path seedMapPath = this.rootPath.resolve("seeds.bin");

    long space = (long) this.seedMap.keySet().size() * Long.BYTES;

    int i = 0;

    try(FileChannel fileChannel = (FileChannel) Files.newByteChannel(seedMapPath, EnumSet.of(
        StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
      MappedByteBuffer mappedByteBuffer = fileChannel.map(MapMode.READ_WRITE, 0, space);

      Long l = seedMap.get(i++);
      while (l != null) {
        mappedByteBuffer.putLong(l);
        l = seedMap.get(i++);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    //System.out.println("420 is " + seedMap.get(420));
  }
}
