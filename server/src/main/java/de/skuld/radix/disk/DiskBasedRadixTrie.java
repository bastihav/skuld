package de.skuld.radix.disk;

import com.google.common.collect.BiMap;
import de.skuld.prng.PRNG;
import de.skuld.radix.AbstractRadixTrie;
import de.skuld.radix.RadixTrieStatus;
import de.skuld.radix.manager.RNGManager;
import de.skuld.radix.manager.SeedManager;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import de.skuld.util.CacheUtil;
import de.skuld.util.ConfigurationHelper;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.collections.FastArrayList;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DiskBasedRadixTrie extends
    AbstractRadixTrie<DiskBasedRandomnessRadixTrieData, RandomnessRadixTrieDataPoint, byte[], DiskBasedRadixTrieNode, PathRadixTrieEdge> {

  private final Path rootPath;
  // cache inserts add datapoints to single data object, which will result in whole object writes
  private HashMap<ByteBuffer, Pair<DiskBasedRandomnessRadixTrieData, byte[]>> memoryCache;
  private long memoryCacheSize;
  private RNGManager rngManager;
  private SeedManager seedManager;
  private HashMap<Byte, MappedByteBuffer> hardwareCaches;
  private HashMap<Byte, Long> hardwareCachesWriteOffsets;

  /**
   * Creates a new DiskBasedRadixTrie at the specified location.
   *
   * @param rootPath path that will be used as root of the trie
   * @param date     date (might be null) for which to generate seeds. If null, will use the current
   *                 system time.
   */
  public DiskBasedRadixTrie(Path rootPath, @Nullable Date date) {
    super(date);
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

    if (ConfigurationHelper.getConfig().getBoolean("radix.disk_based.memory_cache.enabled")) {
      this.memoryCache = new HashMap<>();
    }
  }

  @Override
  public @NotNull DiskBasedRadixTrieNode getDummyNode() {
    return new DiskBasedRadixTrieNode(true, null, rootPath, this);
  }

  @Override
  public @NotNull PathRadixTrieEdge createEdge(String[] label,
      @NotNull DiskBasedRadixTrieNode parentNode) {
    Deque<PathRadixTrieEdge> path = parentNode.getEdgesFromRoot();
    Path newPath = parentNode.getPath().resolve(Arrays.stream(label).reduce(String::concat).get());

    //noinspection ResultOfMethodCallIgnored
    newPath.toFile().mkdir();

    PathRadixTrieEdge edge = new PathRadixTrieEdge(label,
        new DiskBasedRadixTrieNode(false, null, newPath, this), newPath);
    edge.setParent(parentNode);
    return edge;
  }

  @Override
  public @NotNull DiskBasedRadixTrieNode createNode(DiskBasedRandomnessRadixTrieData data,
      PathRadixTrieEdge parentEdge) {
    if (data == null) {
      String label = Arrays.stream(parentEdge.getLabel()).reduce((a, b) -> a + "" + b)
          .orElseGet(String::new);
      Path newPath = parentEdge.getParent().getPath()
          .resolve(label + rootPath.getFileSystem().getSeparator());

      return new DiskBasedRadixTrieNode(false, null, newPath, this);
    } else {
      DiskBasedRadixTrieNode node = new DiskBasedRadixTrieNode(false, data, parentEdge.getPath(),
          this);
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
  public void delete() {
    // TODO
  }

  @Override
  public boolean contains(byte @NotNull [] indexingData) {
    Optional<DiskBasedRadixTrieNode> node = super.getNode(indexingData);
    return node.filter(
        diskBasedRadixTrieNode -> (diskBasedRadixTrieNode
            .getData()).getDataPoint(indexingData).isPresent()).isPresent();
  }

  private boolean existsOnDisk() {
    return this.rootPath
        .resolve(ConfigurationHelper.getConfig().getString("radix.seed_file.file_name")).toFile()
        .exists();
  }

  private void deserializeSeedMap() {
    Path seedMapPath = this.rootPath
        .resolve(ConfigurationHelper.getConfig().getString("radix.seed_file.file_name"));

    try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(seedMapPath, EnumSet.of(
        StandardOpenOption.READ))) {
      MappedByteBuffer mappedByteBuffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size());
      long amountOfSeeds = fileChannel.size() / Long.BYTES;

      for (int i = 0; i < amountOfSeeds; i++) {
        seedMap.put(i, mappedByteBuffer.getLong());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void fillSeedMap(long[] seeds) {
    for (int i = 0; i < seeds.length; i++) {
      this.seedMap.put(i, seeds[i]);
    }
  }

  private void serializeSeedMap() {
    Path seedMapPath = this.rootPath
        .resolve(ConfigurationHelper.getConfig().getString("radix.seed_file.file_name"));
    long space = (long) this.seedMap.keySet().size() * Long.BYTES;
    int i = 0;

    try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(seedMapPath, EnumSet.of(
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
  }

  private boolean usesMemoryCache() {
    return this.memoryCache != null;
  }

  @Override
  public boolean add(@NotNull DiskBasedRadixTrieNode parent,
      @NotNull DiskBasedRandomnessRadixTrieData data, byte @NotNull [] indexingData) {
    if (parent == root && usesMemoryCache()) {
      Pair<DiskBasedRandomnessRadixTrieData, byte[]> pair = new Pair<>(data, indexingData);

      ByteBuffer buffer = ByteBuffer
          .wrap(Arrays.copyOfRange(indexingData, 0, ConfigurationHelper.getConfig()
              .getInt("radix.disk_based.memory_cache.key_length")));

      this.memoryCache.merge(buffer, pair, (old, latest) -> {
        old.getFirst().mergeDataRaw(latest.getFirst());
        return old;
      });
      memoryCacheSize++;

      if (memoryCacheSize >= ConfigurationHelper.getConfig()
          .getInt("radix.disk_based.memory_cache.elements")) {
        flushMemoryCache();
      }

      return true;
    } else {
      return super.add(parent, data, indexingData);
    }
  }

  public void flushMemoryCache() {
    memoryCache.forEach((key, value) -> super.add(root, value.getKey(), value.getValue()));
    System.out.println("flushed");
    memoryCacheSize = 0;
    this.memoryCache = new HashMap<>();
  }

  @Override
  public void serializeMetaData() {

  }

  @Override
  public void generate() {
    if (getMetaData().getStatus() == RadixTrieStatus.CREATED) {
      // start generating data
      getMetaData().setStatus(RadixTrieStatus.GENERATING);
      serializeMetaData();

      fillHardwareCaches();

      getMetaData().setStatus(RadixTrieStatus.GENERATED);
      serializeMetaData();
    }

    if (getMetaData().getStatus() == RadixTrieStatus.GENERATED) {
      // sort data in hardware caches
      getMetaData().setStatus(RadixTrieStatus.SORTING_ADDING);
      serializeMetaData();

      sortAndAddHardwareCaches();

      getMetaData().setStatus(RadixTrieStatus.FINISHED);
      serializeMetaData();
    }
  }

  private void sortAndAddHardwareCaches() {
    hardwareCaches = null;

    int sizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized");
    final int maxPartitionBytesInArray = Integer.MAX_VALUE - (Integer.MAX_VALUE % sizeOnDisk);

    for (int bite = 0; bite < 256; bite++) {
      File file = rootPath.resolve("caches" + rootPath.getFileSystem().getSeparator()+bite+".bin").toFile();

      try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(file.toPath(), EnumSet.of(
          StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
        int reads = (int) Math.ceil(((double) fileChannel.size() / maxPartitionBytesInArray));
        RandomnessRadixTrieDataPoint[] cacheArray = new RandomnessRadixTrieDataPoint[((int) fileChannel.size() / sizeOnDisk)];
        AtomicInteger posInArray = new AtomicInteger(0);

        for (int i = 0; i < reads; i++) {
          long offset = (long) i * maxPartitionBytesInArray;
          // TODO remaining can be greater than the max int?
          long remaining = fileChannel.size() - offset;

          MappedByteBuffer mappedByteBuffer = fileChannel.map(MapMode.READ_WRITE, offset, remaining);
          //System.out.println("reading " + bite);
          readHardwareCache(cacheArray, posInArray, mappedByteBuffer);
        }
        RandomnessRadixTrieDataPoint[] truncatedArray = new RandomnessRadixTrieDataPoint[posInArray.get()];
        System.arraycopy(cacheArray, 0, truncatedArray, 0, posInArray.get());
        cacheArray = null;

        // TODO fixed univsere, can maybe sort in O(n)? -> radix sort
        Arrays.parallelSort(truncatedArray);
        // collapse cacheList into objects according to max depth
        // create new "from" collection constructor in RadixData, saves on merge complexity
        List<DiskBasedRandomnessRadixTrieData> list = collapseCache(truncatedArray);
        addHardwareCache(list);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void addHardwareCache(List<DiskBasedRandomnessRadixTrieData> list) {
    this.flushMemoryCache();
    this.memoryCache = null;
    list.forEach(data -> {
      byte[] indexingData = data.getDataPoints().stream().findFirst().get().getRemainingIndexingData();
      this.add(data, indexingData);
    });
    this.memoryCache = new HashMap<>();
  }

  private List<DiskBasedRandomnessRadixTrieData> collapseCache(RandomnessRadixTrieDataPoint[] cacheArray) {
    int maxHeight = ConfigurationHelper.getConfig().getInt("radix.height.max");
    ArrayList<DiskBasedRandomnessRadixTrieData> list = new ArrayList<>();

    int index = 0;
    while(index < cacheArray.length) {
      RandomnessRadixTrieDataPoint dataPoint = cacheArray[index];
      int lastIndex = CacheUtil.lastIndexOf(Arrays.copyOfRange(dataPoint.getRemainingIndexingData(), 0, maxHeight), cacheArray, index, cacheArray.length);
      DiskBasedRandomnessRadixTrieData data = new DiskBasedRandomnessRadixTrieData(cacheArray[index], this);
      cacheArray[index] = null;
      for (int i = index + 1; i < lastIndex; i++) {
        RandomnessRadixTrieDataPoint dp = cacheArray[i];
        data.getDataPointsRaw().add(dp);
        cacheArray[i] = null;
      }
      list.add(data);

      index = lastIndex+1;
    }

    return list;
  }

  private void readHardwareCache(RandomnessRadixTrieDataPoint[] cacheArray, AtomicInteger offset, MappedByteBuffer mappedByteBuffer) {
    int sizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized");
    int remainingSizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized.remaining");
    int rngIndexSizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized.rng_index");
    int seedIndexSizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized.seed_index");
    int byteIndexSizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized.byte_index");

    byte[] serializedData = new byte[sizeOnDisk];
    byte[] nullBytes = new byte[sizeOnDisk];
    Arrays.fill(nullBytes, (byte) 0);

    while (mappedByteBuffer.hasRemaining() && mappedByteBuffer.remaining() >= sizeOnDisk) {
      mappedByteBuffer.get(serializedData, 0, sizeOnDisk);
      if (Arrays.equals(serializedData, nullBytes)) {
        // we can stop here
        return;
      }

      RandomnessRadixTrieDataPoint dataPoint = new RandomnessRadixTrieDataPoint(serializedData, remainingSizeOnDisk, rngIndexSizeOnDisk, seedIndexSizeOnDisk, byteIndexSizeOnDisk);
      cacheArray[offset.getAndIncrement()] = dataPoint;
    }
  }

  private void fillHardwareCaches() {
    // TODO config for each trie
    initializeHardwareCaches();

    RNGManager rngManager = new RNGManager();

    int partitionSize = ConfigurationHelper.getConfig().getInt("radix.partition.size");
    int amountPerPRNG = ConfigurationHelper.getConfig().getInt("radix.prng.amount");
    int partitions = amountPerPRNG / partitionSize;

    final BiMap<Integer, Long> seedMap = getSeedMap();
    for (int seedIndex = 0; seedIndex < seedMap.size(); seedIndex++) {
      long seed = seedMap.get(seedIndex);
      for (Class<? extends PRNG> prng : rngManager.getPRNGs()) {
        try {
          PRNG instance = prng.getConstructor(long.class).newInstance(seed);
          for (int j = 0; j < partitions; j++) {
            byte[] randomness = instance.getRandomBytes(partitionSize);
            addToHardwareCache(new RandomnessRadixTrieDataPoint(randomness, instance.getPRNG(), seedIndex, seedIndex * 32));
          }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
          e.printStackTrace();
        }
      }
    }

    flushMemoryCache();
  }

  private void addToHardwareCache(RandomnessRadixTrieDataPoint randomnessRadixTrieDataPoint) {
    byte bite = randomnessRadixTrieDataPoint.getRemainingIndexingData()[0];
    byte[] serializedData = randomnessRadixTrieDataPoint.serialize();

    MappedByteBuffer mbb = this.hardwareCaches.get(bite);

    if (mbb.remaining() < serializedData.length) {
      // resize memory mapped file
      int length = mbb.position();
      hardwareCachesWriteOffsets.computeIfPresent(bite, (k,v) -> (v + length));

      File file = rootPath.resolve("caches"+ rootPath.getFileSystem().getSeparator() +bite + ".bin").toFile();
      try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(file.toPath(), EnumSet.of(
          StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {

        int cacheSize = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.size");
        MappedByteBuffer mappedByteBuffer = fileChannel.map(MapMode.READ_WRITE, hardwareCachesWriteOffsets.get(bite), cacheSize);
        // TODO theoretically do not need first byte
        hardwareCaches.put(bite, mappedByteBuffer);
        mbb = mappedByteBuffer;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    mbb.put(serializedData);
  }

  private void initializeHardwareCaches() {
    if (hardwareCaches == null) {
      int cacheSize = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.size");
      hardwareCaches = new HashMap<>();
      hardwareCachesWriteOffsets = new HashMap<>();
      for (int bite = Byte.MIN_VALUE; bite <= Byte.MAX_VALUE; bite++) {
        // TODO maybe byte -> hex if we want to be pretty
        rootPath.resolve("caches" + rootPath.getFileSystem().getSeparator()).toFile().mkdirs();
        File file = rootPath.resolve("caches"+ rootPath.getFileSystem().getSeparator() +bite + ".bin").toFile();

        try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(file.toPath(), EnumSet.of(
            StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {

          MappedByteBuffer mappedByteBuffer = fileChannel.map(MapMode.READ_WRITE, 0, cacheSize);

          hardwareCaches.put((byte) bite, mappedByteBuffer);
          hardwareCachesWriteOffsets.put((byte) bite, 0L);

        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
