package de.skuld.radix.disk;

import com.google.common.collect.BiMap;
import com.google.gson.Gson;
import de.skuld.prng.ImplementedPRNGs;
import de.skuld.prng.PRNG;
import de.skuld.radix.AbstractRadixTrie;
import de.skuld.radix.RadixMetaData;
import de.skuld.radix.RadixTrieStatus;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import de.skuld.radix.manager.RNGManager;
import de.skuld.radix.manager.SeedManager;
import de.skuld.util.CacheUtil;
import de.skuld.util.ConfigurationHelper;
import de.skuld.util.WrappedByteBuffers;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public class DiskBasedRadixTrie extends
    AbstractRadixTrie<DiskBasedRandomnessRadixTrieData, RandomnessRadixTrieDataPoint, byte[], DiskBasedRadixTrieNode, PathRadixTrieEdge> {

  private final Path rootPath;
  // cache inserts add data-points to single data object, which will result in whole object writes
  private HashMap<ByteBuffer, Pair<DiskBasedRandomnessRadixTrieData, byte[]>> memoryCache;
  private long memoryCacheSize;
  private HashMap<Byte, MappedByteBuffer> hardwareCaches;
  private HashMap<Byte, Long> hardwareCachesWriteOffsets;

  /**
   * Creates a new DiskBasedRadixTrie at the specified location.
   *
   * @param rootPath path that will be used as root of the trie
   * @param date     date for which to generate seeds.
   * @param uuid     id of this trie
   */
  public DiskBasedRadixTrie(@NotNull Path rootPath, @NotNull Date date, @NotNull UUID uuid) {
    super(date, uuid);
    this.rootPath = rootPath;

    //noinspection ResultOfMethodCallIgnored
    rootPath.toFile().mkdirs();

    this.root = getDummyNode();

    RNGManager rngManager = new RNGManager();
    SeedManager seedManager = new SeedManager(rngManager);
    long[] seeds = seedManager.getSeeds(date);
    fillSeedMap(seeds);
    serializeSeedMap();
    serializeMetaData();

    if (ConfigurationHelper.getConfig().getBoolean("radix.disk_based.memory_cache.enabled")) {
      this.memoryCache = new HashMap<>();
    }
  }

  /**
   * Loads a DiskBasedRadixTrie from the specified location.
   *
   * @param rootPath path that was used as root of the trie
   */
  public DiskBasedRadixTrie(@NotNull Path rootPath) {
    this.rootPath = rootPath;
    this.root = getDummyNode();

    if (existsOnDisk()) {
      deserializeSeedMap();
      deserializeMetaData();
    } else {
      throw new AssertionError("No serialized trie found");
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

    Path newPath = parentNode.getPath().resolve(Arrays.stream(label).reduce(String::concat).orElse(""));

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
    try {
      FileUtils.deleteDirectory(this.getRoot().getPath().toFile());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean contains(byte @NotNull [] indexingData) {
    Optional<DiskBasedRadixTrieNode> node = super.getNode(indexingData);
    return node.filter(
        diskBasedRadixTrieNode -> !(diskBasedRadixTrieNode
            .getData()).getDataPoints(indexingData).isEmpty()).isPresent();
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
      Pair<DiskBasedRandomnessRadixTrieData, byte[]> pair = Pair.of(data, indexingData);

      ByteBuffer buffer = ByteBuffer
          .wrap(Arrays.copyOfRange(indexingData, 0, ConfigurationHelper.getConfig()
              .getInt("radix.disk_based.memory_cache.key_length")));

      this.memoryCache.merge(buffer, pair, (old, latest) -> {
        old.getKey().mergeDataRaw(latest.getKey());
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
    // TODO config
    File file = rootPath.resolve("metadata.json").toFile();
    if (!file.exists()) {
      try {
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
      writer.println(new Gson().toJson(this.metaData));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void deserializeMetaData() {
    File file = rootPath.resolve("metadata.json").toFile();
    try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
      String line = reader.readLine();

      this.metaData = new Gson().fromJson(line, RadixMetaData.class);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void generate() {
    if (getMetaData().getStatus() == RadixTrieStatus.CREATED) {
      // start generating data
      getMetaData().setStatus(RadixTrieStatus.GENERATING);
      serializeMetaData();

      long ping = System.nanoTime();
      fillHardwareCaches();
      long pong = System.nanoTime();
      System.out.println("hardware caches filled in " + (pong-ping) + " ns");

      getMetaData().setStatus(RadixTrieStatus.GENERATED);
      serializeMetaData();
    }

    if (getMetaData().getStatus() == RadixTrieStatus.GENERATED) {
      // sort data in hardware caches
      getMetaData().setStatus(RadixTrieStatus.SORTING_ADDING);
      serializeMetaData();

      long ping = System.nanoTime();
      sortAndAddHardwareCaches();
      long pong = System.nanoTime();
      System.out.println("hardware caches sorted and added in " + (pong-ping) + " ns");

      getMetaData().setStatus(RadixTrieStatus.FINISHED);
      serializeMetaData();
    }
  }

  @Override
  public boolean checkDiscardedIndexingData(RandomnessRadixTrieDataPoint dataPoint,
      byte[] discardedIndexingData) {
    int seedIndex = dataPoint.getSeedIndex();
    int byteIndex = dataPoint.getByteIndexInRandomness();
    ImplementedPRNGs prng = dataPoint.getRng();

    Class<? extends PRNG> prngClass = ImplementedPRNGs.getPRNG(prng);
    long seed = seedMap.get(seedIndex);

    try {
      assert prngClass != null;
      PRNG instance = prngClass.getConstructor(long.class).newInstance(seed);
      byte[] precedingBytes = instance.getBytes(byteIndex - discardedIndexingData.length, discardedIndexingData.length);

      if (Arrays.equals(precedingBytes, discardedIndexingData)) {
        return true;
      }

    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }

    return false;
  }

  @Override
  public byte[] shiftIndexingData(byte[] indexingData, int offset) {
    if (offset > indexingData.length) {
      return new byte[0];
    }
    byte[] result = new byte[indexingData.length - offset];
    System.arraycopy(indexingData, offset, result, 0, result.length);
    return result;
  }

  @Override
  public byte[] getDiscardedIndexingData(byte[] indexingData, int offset) {
    if (offset > indexingData.length) {
      return indexingData;
    }
    byte[] result = new byte[offset];
    System.arraycopy(indexingData, 0, result, 0, offset);
    return result;
  }

  /**
   * https://stackoverflow.com/questions/2972986/how-to-unmap-a-file-from-memory-mapped-using-filechannel-in-java
   * @param cb buffer to close
   */
  private static void closeDirectBuffer(ByteBuffer cb) {
    if (cb==null || !cb.isDirect()) return;
    // we could use this type cast and call functions without reflection code,
    // but static import from sun.* package is risky for non-SUN virtual machine.
    //try { ((sun.nio.ch.DirectBuffer)cb).cleaner().clean(); } catch (Exception ex) { }

    // JavaSpecVer: 1.6, 1.7, 1.8, 9, 10
    boolean isOldJDK = System.getProperty("java.specification.version","99").startsWith("1.");
    try {
      if (isOldJDK) {
        Method cleaner = cb.getClass().getMethod("cleaner");
        cleaner.setAccessible(true);
        Method clean = Class.forName("sun.misc.Cleaner").getMethod("clean");
        clean.setAccessible(true);
        clean.invoke(cleaner.invoke(cb));
      } else {
        Class<?> unsafeClass;
        try {
          unsafeClass = Class.forName("sun.misc.Unsafe");
        } catch(Exception ex) {
          // jdk.internal.misc.Unsafe doesn't yet have an invokeCleaner() method,
          // but that method should be added if sun.misc.Unsafe is removed.
          unsafeClass = Class.forName("jdk.internal.misc.Unsafe");
        }
        Method clean = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
        clean.setAccessible(true);
        Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafeField.setAccessible(true);
        Object theUnsafe = theUnsafeField.get(null);
        clean.invoke(theUnsafe, cb);
        System.out.println("cleaned!");
      }
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  private void sortAndAddHardwareCaches() {
    hardwareCaches = null;

    int sizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized");
    int remainingOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized.remaining");

    final int maxPartitionBytesInArray = Integer.MAX_VALUE - (Integer.MAX_VALUE % sizeOnDisk);

    for (int bite = Byte.MIN_VALUE; bite <= Byte.MAX_VALUE; bite++) {
      int elementCount = 0;
      File file = rootPath.resolve("caches" + rootPath.getFileSystem().getSeparator()+bite+".bin").toFile();

      try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(file.toPath(), EnumSet.of(
          StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
        int reads = (int) Math.ceil(((double) fileChannel.size() / maxPartitionBytesInArray));
        MappedByteBuffer[] buffers =  new MappedByteBuffer[reads];
        System.out.println("will allocate " + fileChannel.size() + " bytes");

        long ping = System.nanoTime();
        System.out.println("reading x" + reads);
        for (int i = 0; i < reads; i++) {
          long offset = (long) i * maxPartitionBytesInArray;
          long remaining = Math.min(fileChannel.size() - offset, maxPartitionBytesInArray);

          MappedByteBuffer mappedByteBuffer = fileChannel.map(MapMode.READ_ONLY, offset, remaining);
          elementCount += (int) (remaining / sizeOnDisk);
          System.out.println("now at " + elementCount + " elements");
          mappedByteBuffer.load();
          buffers[i] = mappedByteBuffer;
        }

        WrappedByteBuffers wrappedArray = new WrappedByteBuffers(buffers, sizeOnDisk, elementCount, remainingOnDisk);
        long pong = System.nanoTime();
        System.out.println("hardware cache " + bite + " read in " + (pong-ping) + " ns");

        ping = System.nanoTime();

        if (elementCount > 0) {
          wrappedArray.sort();
        }

        pong = System.nanoTime();
        System.out.println("hardware cache " + bite + " sorted in " + (pong-ping) + " ns");

        collapseCacheAndAdd(wrappedArray, elementCount);

        Arrays.stream(buffers).forEach(DiskBasedRadixTrie::closeDirectBuffer);
      } catch (IOException e) {
        e.printStackTrace();
      }
      deleteHardwareCache((byte) bite);
    }
    // delete empty directory
    //noinspection ResultOfMethodCallIgnored
    rootPath.resolve("caches" + rootPath.getFileSystem().getSeparator()).toFile().delete();
  }

  private void collapseCacheAndAdd(WrappedByteBuffers wrappedArray, int elementCount) {
    int maxHeight = ConfigurationHelper.getConfig().getInt("radix.height.max");
    int remainingOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized.remaining");

    long ping = System.nanoTime();
    this.flushMemoryCache();
    this.memoryCache = null;
    int index = 0;
    int[] sortedIndices = wrappedArray.getIndexArray();

    while(index < elementCount) {
      int sortedIndex = sortedIndices[index];

      long ping1 = System.nanoTime();
      byte[] dataPoint = wrappedArray.get(sortedIndex);
      int lastIndex = CacheUtil.lastIndexOf(sortedIndices, Arrays.copyOfRange(dataPoint, 0, maxHeight), wrappedArray, index, elementCount);

      DiskBasedRandomnessRadixTrieData data = new DiskBasedRandomnessRadixTrieData(this, wrappedArray, index, lastIndex);

      long pong1 = System.nanoTime();

      System.out.println("collapsed [" + index + " , " + lastIndex + "] in " + (pong1 - ping1) +" ns");

      ping1 = System.nanoTime();
      byte[] indexingData = new RandomnessRadixTrieDataPoint(dataPoint, remainingOnDisk).getRemainingIndexingData();

      this.add(data, indexingData);
      pong1 = System.nanoTime();
      System.out.println("added [" + index + " , " + lastIndex + "] in " + (pong1 - ping1) +" ns");
      index = lastIndex+1;
    }
    this.memoryCache = new HashMap<>();
    long pong = System.nanoTime();
    System.out.println("hardware cache " + " collapsed in " + (pong-ping) + " ns");
    System.out.println("hardware cache " + " added to tree in " + (pong-ping) + " ns");
  }

  private void deleteHardwareCache(int bite) {
    try {
      Files.delete(rootPath.resolve("caches" + rootPath.getFileSystem().getSeparator()+bite+".bin"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void readHardwareCache(RandomnessRadixTrieDataPoint[] cacheArray, AtomicInteger offset, MappedByteBuffer mappedByteBuffer) {
    int sizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized");
    int remainingSizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized.remaining");
    int rngIndexSizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized.rng_index");
    int seedIndexSizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized.seed_index");
    int byteIndexSizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized.byte_index");

    byte[] serializedData = new byte[sizeOnDisk];

    while (mappedByteBuffer.hasRemaining() && mappedByteBuffer.remaining() >= sizeOnDisk) {
      mappedByteBuffer.get(serializedData, 0, sizeOnDisk);


      boolean allZero = true;
      for (int i = 0; i < sizeOnDisk && allZero; i ++) {
        allZero = serializedData[i] == 0;
      }

      if (allZero) {
        // TODO stop all other reads too
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
    long amountPerPRNG = ConfigurationHelper.getConfig().getLong("radix.prng.amount");
    int partitions = (int) (amountPerPRNG / partitionSize);

    final BiMap<Integer, Long> seedMap = getSeedMap();
    for (int seedIndex = 0; seedIndex < seedMap.size(); seedIndex++) {
      long seed = seedMap.get(seedIndex);
      System.out.println("seed " + seed);
      for (Class<? extends PRNG> prng : rngManager.getPRNGs()) {
        try {
          PRNG instance = prng.getConstructor(long.class).newInstance(seed);
          for (int j = 0; j < partitions; j++) {
            byte[] randomness = new byte[partitionSize];
            instance.nextBytes(randomness);
            addToHardwareCache(new RandomnessRadixTrieDataPoint(randomness, instance.getPRNG(), seedIndex, j * 32));
          }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
          e.printStackTrace();
        }
      }
    }

    truncateHardwareCaches();

    flushMemoryCache();
  }

  private void truncateHardwareCaches() {
    for (int bite = Byte.MIN_VALUE; bite <= Byte.MAX_VALUE; bite++) {
      File file = rootPath
          .resolve("caches" + rootPath.getFileSystem().getSeparator() + (byte) bite + ".bin").toFile();
      try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(file.toPath(), EnumSet.of(
          StandardOpenOption.READ, StandardOpenOption.WRITE))) {
        MappedByteBuffer mbb = hardwareCaches.get((byte) bite);
        long length = hardwareCachesWriteOffsets.get((byte) bite) + mbb.position();
        hardwareCaches.put((byte) bite, null);
        closeDirectBuffer(mbb);

        //accessing the mbb after cleaning would crash the whole JVM, this way we "only" get a null pointer from which we can recover
        //noinspection UnusedAssignment
        mbb = null;

        System.out.println("I think i wrote " + length + " to " + bite);
        System.out.println("total file channel was " + fileChannel.size());
        System.out.println("so deleting " + (fileChannel.size() - length) + "?");
        fileChannel.truncate(length);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void addToHardwareCache(RandomnessRadixTrieDataPoint randomnessRadixTrieDataPoint) {
    byte bite = randomnessRadixTrieDataPoint.getRemainingIndexingData()[0];
    byte[] serializedData = randomnessRadixTrieDataPoint.serialize();

    MappedByteBuffer mbb = this.hardwareCaches.get(bite);

    if (mbb.remaining() < serializedData.length) {
      // resize memory mapped file
      int length = mbb.position();
      hardwareCachesWriteOffsets.computeIfPresent(bite, (k,v) -> (v + length));

      File file = rootPath.resolve("caches"+ rootPath.getFileSystem().getSeparator() + bite + ".bin").toFile();
      try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(file.toPath(), EnumSet.of(
          StandardOpenOption.READ, StandardOpenOption.WRITE))) {
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
        //noinspection ResultOfMethodCallIgnored
        rootPath.resolve("caches" + rootPath.getFileSystem().getSeparator()).toFile().mkdirs();
        File file = rootPath.resolve("caches"+ rootPath.getFileSystem().getSeparator() + bite + ".bin").toFile();

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
