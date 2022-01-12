package de.skuld.radix.disk;

import com.google.common.primitives.UnsignedBytes;
import de.skuld.radix.AbstractRadixTrieData;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import de.skuld.util.ConfigurationHelper;
import de.skuld.util.WrappedByteBuffers;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.Stream;

public class DiskBasedRandomnessRadixTrieData extends RandomnessRadixTrieData {

  private final Path p;
  private MappedByteBuffer mappedByteBuffer;
  private long readSizeInBytes;
  private long writeSizeInBytes;
  private boolean resolved = false;
  private DiskBasedRadixTrie trie;
  private WrappedByteBuffers buffer = null;
  private int startIndex = -1;
  private int endIndex = -1;
  private int remainingIndexingDataOffset = 0;

  public DiskBasedRandomnessRadixTrieData(RandomnessRadixTrieDataPoint data,
      DiskBasedRadixTrie trie) {
    super(data);
    this.p = null;
    this.resolved = true;
    this.trie = trie;
  }

  public DiskBasedRandomnessRadixTrieData(DiskBasedRadixTrie trie, WrappedByteBuffers buffer, int startIndex, int endIndex) {
    super(null);
    this.p = null;
    this.resolved = true;
    this.trie = trie;
    this.buffer = buffer;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
  }

  public DiskBasedRandomnessRadixTrieData(Path p) {
    super(null);
    if (!p.toFile().exists()) {
      throw new AssertionError("File does not exist");
    } else {
      this.p = p;
    }
  }

  @Override
  public void serialize(ByteBuffer writeBuffer, int offset) {
    int[] indices = buffer.getIndexArray();
    if (this.buffer != null) {
      writeBuffer.position(offset);
      for (int i = startIndex; i <= endIndex; i++) {
        byte[] array = buffer.get(indices[i]);

        writeBuffer.put(array, remainingIndexingDataOffset, array.length - remainingIndexingDataOffset);
      }
    } else {
      super.serialize(mappedByteBuffer, offset);
    }
  }

  @Override
  public int getElementCount() {
    if (buffer != null) {
      // TODO refactor this to have endIndex exclusive
      return endIndex - startIndex + 1;
    }
    return super.getElementCount();
  }

  @Override
  public void removePrefixFromRemainingIndexingData(int amount) {
    if (buffer != null) {
      this.remainingIndexingDataOffset += amount;
    } else {
      super.removePrefixFromRemainingIndexingData(amount);
    }
  }

  @Override
  public DiskBasedRandomnessRadixTrieData mergeData(
      AbstractRadixTrieData<byte[], RandomnessRadixTrieDataPoint> other) {
    Collection<RandomnessRadixTrieDataPoint> otherDp = other.getDataPoints();
    long amountOfNewData =
        otherDp.size() * ConfigurationHelper.getConfig().getLong("radix.partition.serialized");

    Stream<RandomnessRadixTrieDataPoint> dpSorted = Stream
        .concat(this.getDataPoints().stream(), otherDp.stream()).sorted();

    try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(p, EnumSet.of(
        StandardOpenOption.READ, StandardOpenOption.WRITE))) {
      readSizeInBytes = fileChannel.size();
      writeSizeInBytes = readSizeInBytes + amountOfNewData;
      mappedByteBuffer = fileChannel.map(MapMode.READ_WRITE, 0, writeSizeInBytes);
    } catch (IOException e) {
      e.printStackTrace();
    }

    dpSorted.forEach(dp -> {
      mappedByteBuffer.put(dp.serialize());
    });

    dataPoints.addAll(otherDp);

    return this;
  }

  public Collection<RandomnessRadixTrieDataPoint> getDataPointsRaw() {
    return dataPoints;
  }

  public DiskBasedRandomnessRadixTrieData mergeDataRaw(DiskBasedRandomnessRadixTrieData other) {
    this.dataPoints.addAll(other.getDataPointsRaw());
    return this;
  }

  @Override
  public Collection<RandomnessRadixTrieDataPoint> getDataPoints() {
    int partitionSizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.partition.serialized");

    if (!resolved) {
      try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(p, EnumSet.of(
          StandardOpenOption.READ))) {
        mappedByteBuffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size());
        readSizeInBytes = fileChannel.size();
        long amountOfDataPoints = readSizeInBytes / partitionSizeOnDisk;

        for (int i = 0; i < amountOfDataPoints; i++) {
          byte[] array = new byte[partitionSizeOnDisk];
          mappedByteBuffer.position(i * partitionSizeOnDisk);
          mappedByteBuffer.get(array, 0, partitionSizeOnDisk);

          RandomnessRadixTrieDataPoint dataPoint = new RandomnessRadixTrieDataPoint(array);
          dataPoints.add(dataPoint);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    resolved = true;

    return dataPoints;
  }

  public Optional<RandomnessRadixTrieDataPoint> getDataPoint(byte[] query) {
    int partitionSizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.partition.serialized");
    int remainingSize = ConfigurationHelper.getConfig()
        .getInt("radix.partition.serialized.remaining");

    byte[] leastSignificantBytes = Arrays
        .copyOfRange(query, query.length - remainingSize, query.length);

    if (resolved) {
      return dataPoints.stream()
          .filter(dp -> Arrays.equals(dp.getRemainingIndexingData(), leastSignificantBytes))
          .findFirst();
    }

    try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(p, EnumSet.of(
        StandardOpenOption.READ))) {
      mappedByteBuffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size());
      readSizeInBytes = fileChannel.size();
      long amountOfDataPoints = readSizeInBytes / partitionSizeOnDisk;
      long position = binarySearch(0, amountOfDataPoints - 1, query);
      byte[] array = new byte[partitionSizeOnDisk];
      mappedByteBuffer.position((int) (position * partitionSizeOnDisk));
      mappedByteBuffer.get(array, 0, partitionSizeOnDisk);

      RandomnessRadixTrieDataPoint middleDataPoint = new RandomnessRadixTrieDataPoint(array);

      if (UnsignedBytes.lexicographicalComparator().compare(leastSignificantBytes,
          middleDataPoint.getRemainingIndexingData()) == 0) {
        return Optional.of(middleDataPoint);
      } else {
        return Optional.empty();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  public long binarySearch(long left, long right, byte[] query) {
    int partitionSizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.partition.serialized");
    int remainingSize = ConfigurationHelper.getConfig()
        .getInt("radix.partition.serialized.remaining");
    byte[] leastSignificantBytes = Arrays
        .copyOfRange(query, query.length - remainingSize, query.length);

    while (left <= right) {
      long mid = left + (right - left) / 2;

      byte[] array = new byte[partitionSizeOnDisk];

      mappedByteBuffer.position((int) (mid * partitionSizeOnDisk));
      mappedByteBuffer.get(array, 0, partitionSizeOnDisk);

      RandomnessRadixTrieDataPoint middleDataPoint = new RandomnessRadixTrieDataPoint(array);

      int comparison = UnsignedBytes.lexicographicalComparator().compare(leastSignificantBytes,
          middleDataPoint.getRemainingIndexingData());
      if (comparison < 0) {
        right = mid - 1;
      } else if (comparison == 0) {
        return mid;
      } else {
        left = mid + 1;
      }
    }

    return left;
  }


}
