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
import java.lang.ProcessBuilder.Redirect;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class DiskBasedRadixTrie extends
    AbstractRadixTrie<DiskBasedRandomnessRadixTrieData, RandomnessRadixTrieDataPoint, byte[], DiskBasedRadixTrieNode, PathRadixTrieEdge> {

  private static final Logger LOGGER = LogManager.getLogger();
  private final Path rootPath;
  // cache inserts add data-points to single data object, which will result in whole object writes
  private Map<ByteBuffer, Pair<DiskBasedRandomnessRadixTrieData, byte[]>> memoryCache;
  private Map<Byte, Collection<RandomnessRadixTrieDataPoint>> hardwareFillMemoryCache;
  private Map<Byte, ReentrantReadWriteLock> hardwareFillMemoryCacheLocks;

  private Map<Byte, ReentrantLock> hardwareCacheLocks;

  private long memoryCacheSize;
  private Map<Byte, MappedByteBuffer> hardwareCaches;
  private Map<Byte, Long> hardwareCachesWriteOffsets;
  private ThreadPoolExecutor threadPoolExecutorHardwareCaches;

  int cacheSize = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.mem_cache_for_hw_cache.elements");
  int sizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized");
  int remainingOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized.remaining");
  int maxHeight = ConfigurationHelper.getConfig().getInt("radix.height.max");
  int rngIndexSizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized.rng_index");
  int seedIndexSizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized.seed_index");
  int byteIndexSizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized.byte_index");
  int partitionSize = ConfigurationHelper.getConfig().getInt("radix.partition.size");
  long amountPerPRNG = ConfigurationHelper.getConfig().getLong("radix.prng.amount");

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

    SeedManager seedManager = new SeedManager();
    long[] seeds = seedManager.getSeeds(date);
    fillSeedMap(seeds);
    serializeSeedMap();
    serializeMetaData();

    if (ConfigurationHelper.getConfig().getBoolean("radix.disk_based.memory_cache.enabled")) {
      this.memoryCache = new HashMap<>();
      this.hardwareFillMemoryCache = new HashMap<>();
      hardwareCacheLocks = new HashMap<>();
      hardwareFillMemoryCacheLocks = new HashMap<>();
      for (int i = 0; i < 256; i++) {
        hardwareCacheLocks.put((byte) i, new ReentrantLock());
        hardwareFillMemoryCache.put((byte) i, Collections.synchronizedList(new ArrayList<>()));
        hardwareFillMemoryCacheLocks.put((byte) i, new ReentrantReadWriteLock());
      }
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
      this.hardwareFillMemoryCache = new HashMap<>();
      hardwareCacheLocks = new HashMap<>();
      hardwareFillMemoryCacheLocks = new HashMap<>();
      for (int i = 0; i < 256; i++) {
        hardwareCacheLocks.put((byte) i, new ReentrantLock());
        hardwareFillMemoryCache.put((byte) i, Collections.synchronizedList(new ArrayList<>()));
        hardwareFillMemoryCacheLocks.put((byte) i, new ReentrantReadWriteLock());
      }
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
      boolean isWindows = System.getProperty("os.name")
          .toLowerCase().startsWith("windows");

      ProcessBuilder builder = new ProcessBuilder();
      if (isWindows) {
        builder.command("cmd.exe", "/c", "rmdir", "/s", "/q", "\""+getRoot().getPath().toString()+"\"");
      } else {
        builder.command("sh", "-c", "rm -rf " +getRoot().getPath().toString());
      }
      builder.directory(new File(System.getProperty("user.home")));
      builder.redirectOutput(Redirect.DISCARD);
      builder.redirectError(Redirect.DISCARD);
      Process process = builder.start();
      LOGGER.info("Deleted trie " + this);
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
        .exists() && this.rootPath
        .resolve(ConfigurationHelper.getConfig().getString("radix.trie.file_name")).toFile()
        .exists();
  }

  @Override
  public String toString() {
    return "DiskBasedRadixTrie{" +
        "metaData=" + metaData +
        ", rootPath=" + rootPath +
        '}';
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
    memoryCacheSize = 0;
    this.memoryCache = new HashMap<>();
  }

  @Override
  public void serializeMetaData() {
    File file = rootPath.resolve(ConfigurationHelper.getConfig().getString("radix.trie.file_name")).toFile();
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
    File file = rootPath.resolve(ConfigurationHelper.getConfig().getString("radix.trie.file_name")).toFile();
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
      LOGGER.info("Generated all random data for " + this.metaData.getId() + " in " + (pong-ping) + "ns");

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
      LOGGER.info("Added all random data to tree for " + this.metaData.getId() + " in " + (pong-ping) + "ns");

      getMetaData().setStatus(RadixTrieStatus.FINISHED);
      serializeMetaData();
    }
    LOGGER.info("Finished creation of tree " + this.metaData.getId());
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
      }
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  private void sortAndAddHardwareCaches() {
    hardwareCaches = null;



    final int maxPartitionBytesInArray = Integer.MAX_VALUE - (Integer.MAX_VALUE % sizeOnDisk);

    for (int bite = Byte.MIN_VALUE; bite <= Byte.MAX_VALUE; bite++) {
      int elementCount = 0;
      File file = rootPath.resolve("caches" + rootPath.getFileSystem().getSeparator()+bite+".bin").toFile();

      try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(file.toPath(), EnumSet.of(
          StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
        int reads = (int) Math.ceil(((double) fileChannel.size() / maxPartitionBytesInArray));
        MappedByteBuffer[] buffers =  new MappedByteBuffer[reads];

        long ping = System.nanoTime();
        for (int i = 0; i < reads; i++) {
          long offset = (long) i * maxPartitionBytesInArray;
          long remaining = Math.min(fileChannel.size() - offset, maxPartitionBytesInArray);

          MappedByteBuffer mappedByteBuffer = fileChannel.map(MapMode.READ_ONLY, offset, remaining);
          elementCount += (int) (remaining / sizeOnDisk);
          mappedByteBuffer.load();
          buffers[i] = mappedByteBuffer;
        }

        WrappedByteBuffers wrappedArray = new WrappedByteBuffers(buffers, sizeOnDisk, elementCount, remainingOnDisk);
        long pong = System.nanoTime();
        LOGGER.debug("Read hardware cache for tree " + this.metaData.getId() + " for byte " + bite + " in " + (
            pong - ping) + "ns");

        ping = System.nanoTime();

        if (elementCount > 0) {
          wrappedArray.sort();
        }

        pong = System.nanoTime();
        LOGGER.debug("Sorted hardware cache for tree " + this.metaData.getId() + " for byte " + bite + " in " + (
            pong - ping) + "ns");


        ping = System.nanoTime();
          collapseCacheAndAdd(wrappedArray, elementCount);
          pong = System.nanoTime();
          LOGGER.debug("Collapsed and added hardware cache for tree " + this.metaData.getId() + " for byte " + bite
              + " in " + (pong - ping) + "ns");

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


    this.flushMemoryCache();
    this.memoryCache = null;
    int index = 0;
    int[] sortedIndices = wrappedArray.getIndexArray();
    int coreCount = ConfigurationHelper.getConfig().getInt("radix.disk_based.cores");


    // can do it in parallel, but only one byte at a time (ram size!)
    ThreadPoolExecutor tpe = new ThreadPoolExecutor(Math.min(Runtime.getRuntime()
        .availableProcessors(), coreCount), Math.min(Runtime.getRuntime()
        .availableProcessors(), coreCount), 20, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    while(index < elementCount) {
      int sortedIndex = sortedIndices[index];

      byte[] dataPoint = wrappedArray.get(sortedIndex);
      int lastIndex = CacheUtil.lastIndexOf(sortedIndices, Arrays.copyOfRange(dataPoint, 0, maxHeight), wrappedArray, index, elementCount);

      DiskBasedRandomnessRadixTrieData data = new DiskBasedRandomnessRadixTrieData(this, wrappedArray, index, lastIndex);

      byte[] indexingData = new RandomnessRadixTrieDataPoint(dataPoint, remainingOnDisk).getRemainingIndexingData();

      // race condition: what if wrapped array changes?
      // -> it can not change
      tpe.execute(() -> {
        this.add(data, indexingData);
      });

      index = lastIndex+1;
    }

    tpe.shutdown();
    try {
      boolean allTasksFinished = tpe.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    this.memoryCache = new HashMap<>();
  }

  private void deleteHardwareCache(int bite) {
    try {
      Files.delete(rootPath.resolve("caches" + rootPath.getFileSystem().getSeparator()+bite+".bin"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void readHardwareCache(RandomnessRadixTrieDataPoint[] cacheArray, AtomicInteger offset, MappedByteBuffer mappedByteBuffer) {


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

      RandomnessRadixTrieDataPoint dataPoint = new RandomnessRadixTrieDataPoint(serializedData, remainingOnDisk, rngIndexSizeOnDisk, seedIndexSizeOnDisk, byteIndexSizeOnDisk);
      cacheArray[offset.getAndIncrement()] = dataPoint;
    }
  }

  /**
   * Generates randomness using the prng and all seeds in the specified range and adds it to the hardware cache
   * @param prng
   * @param seedOffset
   * @param end
   */
  private void generateRandomness(ImplementedPRNGs prng, int seedOffset, int end) {
    int partitions = (int) (amountPerPRNG / partitionSize);
    final BiMap<Integer, Long> seedMap = getSeedMap();

    LOGGER.debug("Generating " + prng + " with seeds from " + seedOffset + " to " + end);

    for (int seedIndex = Math.max(0, Math.min(seedMap.size() -1, seedOffset)); seedIndex < Math.min(seedMap.size(), end); seedIndex++) {
      long seed = seedMap.get(seedIndex);
      try {
        PRNG instance = ImplementedPRNGs.getPRNG(prng, seed);
        for (int j = 0; j < partitions; j++) {
          byte[] randomness = new byte[partitionSize];
          instance.nextBytes(randomness);
          addToHardwareCacheDirectly(
              new RandomnessRadixTrieDataPoint(randomness, instance.getPRNG(), seedIndex, j * 32));
        }
      } catch (RuntimeException e) {
        e.printStackTrace();
        LOGGER.error("Could not instantiate RNG " + prng + " with seed " + seed, e);
      }
    }
    LOGGER.info("A generate task for " + prng + " finished.");
  }

  private void fillHardwareCaches() {
    // TODO config for each trie
    initializeHardwareCaches();

    final BiMap<Integer, Long> seedMap = getSeedMap();
    int cores = ConfigurationHelper.getConfig().getInt("radix.disk_based.cores");

    threadPoolExecutorHardwareCaches = new ThreadPoolExecutor(cores, cores, 20, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    threadPoolExecutorHardwareCaches.setRejectedExecutionHandler(new CallerRunsPolicy());

    int threadsPerPrng = cores / RNGManager.getPRNGEnum()
        .size();
    LOGGER.info("Will start " + threadsPerPrng +" threads per PRNG");
    int seedsLengthPerThread = seedMap.size() / Math.max(1, threadsPerPrng);
    LOGGER.info("Will allocate " + seedsLengthPerThread +" seeds per thread");

    for (ImplementedPRNGs prng : RNGManager.getPRNGEnum()) {
      for (int i = 0; i <= threadsPerPrng; i++) {
        int finalI = i;
        threadPoolExecutorHardwareCaches.execute(() -> generateRandomness(prng, finalI * seedsLengthPerThread, (finalI + 1) * seedsLengthPerThread));
      }
    }

    LOGGER.info("Created all fill tasks for " + this);
    threadPoolExecutorHardwareCaches.shutdown();

    boolean finished = false;

    try {
      finished = threadPoolExecutorHardwareCaches.awaitTermination(2, TimeUnit.DAYS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    LOGGER.info("All fill threads finished for " + this + ". Timeout? " + !finished);

    threadPoolExecutorHardwareCaches = new ThreadPoolExecutor(cores, cores, 20, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    threadPoolExecutorHardwareCaches.setRejectedExecutionHandler(new CallerRunsPolicy());

    if (usesMemoryCache()) {
      for (int i = 0; i < 256; i++) {
        flushMemoryCacheForHardwareCache((byte) i);
      }
    }
    LOGGER.info("Created all flush tasks for " + this);
    threadPoolExecutorHardwareCaches.shutdown();

    try {
      finished = threadPoolExecutorHardwareCaches.awaitTermination(2, TimeUnit.DAYS);
    } catch (InterruptedException e) {
      e.printStackTrace();
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
        LOGGER.debug("Deleted MemoryMappedFile for tree " + this.metaData.getId() + " for byte " + bite);
        closeDirectBuffer(mbb);

        //accessing the mbb after cleaning would crash the whole JVM, this way we "only" get a null pointer from which we can recover
        //noinspection UnusedAssignment
        mbb = null;

        fileChannel.truncate(length);
        LOGGER.debug("Truncated hardware cache for tree " + this.metaData.getId() + " for byte " + bite);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void addToHardwareCache(RandomnessRadixTrieDataPoint randomnessRadixTrieDataPoint) {
    byte bite = randomnessRadixTrieDataPoint.getRemainingIndexingData()[0];

    if (usesMemoryCache()) {
      Collection<RandomnessRadixTrieDataPoint> collection = this.hardwareFillMemoryCache.get(bite);
      ReadWriteLock lock = hardwareFillMemoryCacheLocks.get(bite);

      if (collection.size() >= cacheSize) {
        // flush this hw_mem cache

        flushMemoryCacheForHardwareCache(bite);
      }
      lock.readLock().lock();
      collection = this.hardwareFillMemoryCache.get(bite);
      collection.add(randomnessRadixTrieDataPoint);
      lock.readLock().unlock();
    } else {
      addToHardwareCacheDirectly(randomnessRadixTrieDataPoint);
    }
  }

  public void flushMemoryCacheForHardwareCache(byte bite) {
    ReadWriteLock lock = hardwareFillMemoryCacheLocks.get(bite);

    lock.writeLock().lock();

    Collection<RandomnessRadixTrieDataPoint> finalCollection = this.hardwareFillMemoryCache.get(bite);;
    threadPoolExecutorHardwareCaches.execute(() -> addToHardwareCacheDirectly(finalCollection));
    hardwareFillMemoryCache.put(bite, Collections.synchronizedList(new ArrayList<>()));

    lock.writeLock().unlock();
  }


  /**
   * Directly adds data points to hardware cache without memory caching
   * All datapoints need to be with the same starting byte
   * @param randomnessRadixTrieDataPoints
   */
  private void addToHardwareCacheDirectly(Collection<RandomnessRadixTrieDataPoint> randomnessRadixTrieDataPoints) {
    if (randomnessRadixTrieDataPoints == null || randomnessRadixTrieDataPoints.size() == 0) {
      return;
    }
    byte bite = randomnessRadixTrieDataPoints.stream().findFirst().get().getRemainingIndexingData()[0];

    hardwareCacheLocks.get(bite).lock();
    {
      MappedByteBuffer mbb = this.hardwareCaches.get(bite);
      for (RandomnessRadixTrieDataPoint randomnessRadixTrieDataPoint : randomnessRadixTrieDataPoints) {
        byte[] serializedData = randomnessRadixTrieDataPoint.serialize();

        if (mbb.remaining() < serializedData.length) {
          // resize memory mapped file
          resizeHardwareCache(bite);
          mbb = this.hardwareCaches.get(bite);
        }

        mbb.put(serializedData);
      }
    }
    hardwareCacheLocks.get(bite).unlock();
  }

  /**
   * Directly adds data point to hardware cache without memory caching
   * @param randomnessRadixTrieDataPoint
   */
  private void addToHardwareCacheDirectly(RandomnessRadixTrieDataPoint randomnessRadixTrieDataPoint) {
    byte bite = randomnessRadixTrieDataPoint.getRemainingIndexingData()[0];

    //LOGGER.debug("Attempting to write to hw cache");
    hardwareCacheLocks.get(bite).lock();
    {
      MappedByteBuffer mbb = this.hardwareCaches.get(bite);
      byte[] serializedData = randomnessRadixTrieDataPoint.serialize();

      if (mbb.remaining() < serializedData.length) {
        //LOGGER.debug("need to resize hw cache");
        // resize memory mapped file
        resizeHardwareCache(bite);
        mbb = this.hardwareCaches.get(bite);
        //LOGGER.debug("resized hw cache");
      }

        mbb.put(serializedData);
    }
    //System.out.println("final unlock: " + hardwareCacheLocks.get(bite).getHoldCount());
    hardwareCacheLocks.get(bite).unlock();
    //LOGGER.debug("wrote to write to hw cache");
  }

  private void resizeHardwareCache(byte bite) {
    //LOGGER.debug("Attempting to resize hw cache");
    hardwareCacheLocks.get(bite).lock();
    {
      //LOGGER.debug("got lock");
      MappedByteBuffer mbb = this.hardwareCaches.get(bite);
      int length = mbb.position();
      closeDirectBuffer(mbb);
      hardwareCachesWriteOffsets.computeIfPresent(bite, (k,v) -> (v + length));

      File file = rootPath.resolve("caches"+ rootPath.getFileSystem().getSeparator() + bite + ".bin").toFile();
      try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(file.toPath(), EnumSet.of(
          StandardOpenOption.READ, StandardOpenOption.WRITE))) {
        int cacheSize = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.size");
        MappedByteBuffer mappedByteBuffer = fileChannel.map(MapMode.READ_WRITE, hardwareCachesWriteOffsets.get(bite), cacheSize);

        hardwareCaches.put(bite, mappedByteBuffer);
      } catch (IOException e) {
        e.printStackTrace();
      }
      //LOGGER.debug("got resized");
    }
    //LOGGER.debug("Attempting to unlock");
    hardwareCacheLocks.get(bite).unlock();
    //LOGGER.debug("unlocked");
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
