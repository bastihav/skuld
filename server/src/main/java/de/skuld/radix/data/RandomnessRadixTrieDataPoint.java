package de.skuld.radix.data;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.UnsignedBytes;
import de.skuld.prng.ImplementedPRNGs;
import de.skuld.radix.AbstractRadixTrieDataPoint;
import de.skuld.util.ConfigurationHelper;
import de.skuld.util.WrappedByteBuffers;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class RandomnessRadixTrieDataPoint extends AbstractRadixTrieDataPoint<byte[]> {

  private final ImplementedPRNGs rng;
  private final int seedIndex;
  private final int byteIndexInRandomness;
  private final WrappedByteBuffers buffers;
  private final int bufferIndex;
  private int remainingIndexingDataOffset = 0;

  public RandomnessRadixTrieDataPoint(byte[] remainingBytes, ImplementedPRNGs rng, int seedIndex,
      int byteIndexInRandomness) {
    this.buffers = null;
    this.bufferIndex = -1;
    this.remainingIndexingData = Arrays.copyOf(remainingBytes, remainingBytes.length);
    this.rng = rng;
    this.seedIndex = seedIndex;
    this.byteIndexInRandomness = byteIndexInRandomness;
  }

  public RandomnessRadixTrieDataPoint(byte[] serializedData, int remainingSize, int rngSize, int seedIndexSize, int byteIndexSize) {
    this.buffers = null;
    this.remainingIndexingData = new byte[remainingSize];
    this.bufferIndex = -1;

    ByteBuffer byteBuffer = ByteBuffer.wrap(serializedData);
    byteBuffer.get(remainingIndexingData);
    this.rng = ImplementedPRNGs.values()[byteBuffer.get()];
    this.seedIndex = byteBuffer.getInt();
    this.byteIndexInRandomness = byteBuffer.getInt();
  }

  public RandomnessRadixTrieDataPoint(WrappedByteBuffers buffers, int index) {
    this.buffers = buffers;
    this.bufferIndex = index;
    this.rng = null;
    this.seedIndex = -1;
    this.byteIndexInRandomness = -1;
  }

  public RandomnessRadixTrieDataPoint(byte[] serializedData, int remainingSize) {
    this(serializedData, remainingSize, ConfigurationHelper.getConfig()
        .getInt("radix.partition.serialized.rng_index"), ConfigurationHelper.getConfig()
        .getInt("radix.partition.serialized.byte_index"), ConfigurationHelper.getConfig()
        .getInt("radix.partition.serialized.seed_index"));
  }

  public RandomnessRadixTrieDataPoint(byte[] serializedData) {
    this(serializedData, ConfigurationHelper.getConfig()
        .getInt("radix.partition.serialized.remaining"), ConfigurationHelper.getConfig()
        .getInt("radix.partition.serialized.rng_index"), ConfigurationHelper.getConfig()
        .getInt("radix.partition.serialized.byte_index"), ConfigurationHelper.getConfig()
        .getInt("radix.partition.serialized.seed_index"));
  }

  @Override
  public byte[] getRemainingIndexingData() {
    if (buffers != null) {
      int serializedRemainingSize = ConfigurationHelper.getConfig().getInt("radix.partition.serialized.remaining");

      return Arrays.copyOfRange(buffers.get(bufferIndex), remainingIndexingDataOffset, serializedRemainingSize - remainingIndexingDataOffset);
    }
    return super.getRemainingIndexingData();
  }

  public ImplementedPRNGs getRng() {
    return rng;
  }

  public int getSeedIndex() {
    return seedIndex;
  }

  public int getByteIndexInRandomness() {
    return byteIndexInRandomness;
  }

  @Override
  public String toString() {
    return "RandomnessRadixTrieDataPoint{" +
        "remainingBytes=" + Arrays.toString(getRemainingIndexingData()) +
        ", rng=" + rng +
        ", seedIndex=" + seedIndex +
        ", byteIndexInRandomness=" + byteIndexInRandomness +
        '}';
  }

  @Override
  public void removePrefixFromRemainingIndexingData(int amount) {
    if (buffers != null) {
      this.remainingIndexingDataOffset += amount;
    } else {
      this.remainingIndexingData = Arrays
          .copyOfRange(remainingIndexingData, amount, remainingIndexingData.length);
    }
  }

  @Override
  public byte[] serialize() {
    byte[] prng = new byte[]{((Integer) rng.ordinal()).byteValue()};

    // TODO maybe only store 3 bytes for seeds, 2 bytes for byteIdx
    byte[] seedIdx = Ints.toByteArray(seedIndex);
    byte[] byteIdx = Ints.toByteArray(byteIndexInRandomness);
    return Bytes.concat(getRemainingIndexingData(), prng, seedIdx, byteIdx);
  }

  @Override
  public int compareTo(AbstractRadixTrieDataPoint<byte[]> o) {
    return UnsignedBytes.lexicographicalComparator()
        .compare(this.getRemainingIndexingData(), o.getRemainingIndexingData());
  }

  @Override
  public void serialize(byte[] serializedData, int index) {
    if (buffers != null) {
      int length = ConfigurationHelper.getConfig().getInt("radix.partition.serialized");

      System.arraycopy(buffers.get(bufferIndex), remainingIndexingDataOffset, serializedData, index,
          length);
    } else {
      int currentIndex = index;
      byte[] remainingData = getRemainingIndexingData();
      System.arraycopy(remainingData, 0, serializedData, currentIndex,
          remainingData.length);
      currentIndex += remainingData.length;

      System.arraycopy(new byte[]{((Integer) rng.ordinal()).byteValue()}, 0, serializedData,
          currentIndex, 1);
      currentIndex++;

      System.arraycopy(Ints.toByteArray(seedIndex), 0, serializedData, currentIndex, 4);
      currentIndex += 4;

      System.arraycopy(Ints.toByteArray(byteIndexInRandomness), 0, serializedData, currentIndex, 4);
    }
  }

  public void serialize(ByteBuffer writeBuffer, int index) {
    if (buffers != null) {
      byte[] array = buffers.get(bufferIndex);

      writeBuffer.position(index).put(array, remainingIndexingDataOffset, array.length - remainingIndexingDataOffset);
    } else {
      byte[] remainingData = getRemainingIndexingData();
      writeBuffer.position(index);
      writeBuffer.put(remainingData);
      writeBuffer.put(((Integer) rng.ordinal()).byteValue());
      writeBuffer.putInt(seedIndex);
      writeBuffer.putInt(byteIndexInRandomness);
    }
  }
}
