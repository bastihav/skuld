package de.skuld.radix.data;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.UnsignedBytes;
import de.skuld.prng.ImplementedPRNGs;
import de.skuld.radix.AbstractRadixTrieDataPoint;
import de.skuld.util.ConfigurationHelper;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class RandomnessRadixTrieDataPoint extends AbstractRadixTrieDataPoint<byte[]> {

  private final ImplementedPRNGs rng;
  private final int seedIndex;
  private final int byteIndexInRandomness;

  public RandomnessRadixTrieDataPoint(byte[] remainingBytes, ImplementedPRNGs rng, int seedIndex,
      int byteIndexInRandomness) {
    this.remainingIndexingData = Arrays.copyOf(remainingBytes, remainingBytes.length);
    this.rng = rng;
    this.seedIndex = seedIndex;
    this.byteIndexInRandomness = byteIndexInRandomness;
  }

  public RandomnessRadixTrieDataPoint(byte[] serializedData, int remainingSize, int rngSize, int seedIndexSize, int byteIndexSize) {
    //int currentIndex = 0;
    //this.remainingIndexingData = new byte[remainingSize];
        //Arrays
        //.copyOfRange(serializedData, currentIndex, currentIndex + remainingSize);
    //currentIndex += remainingSize;

    ByteBuffer byteBuffer = ByteBuffer.wrap(serializedData);
    //byteBuffer.get(remainingIndexingData);
    this.rng = ImplementedPRNGs.values()[byteBuffer.get()];
    //this.rng = ImplementedPRNGs.values()[serializedData[currentIndex]];
    //currentIndex+= rngSize;
    this.seedIndex = byteBuffer.getInt();
    this.byteIndexInRandomness = byteBuffer.getInt();

    /*this.seedIndex = Ints.fromByteArray(
        Arrays.copyOfRange(serializedData, currentIndex, currentIndex + seedIndexSize));
    currentIndex += seedIndexSize;

    this.byteIndexInRandomness = Ints.fromByteArray(
        Arrays.copyOfRange(serializedData, currentIndex, currentIndex + byteIndexSize));*/
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
        "remainingBytes=" + Arrays.toString(remainingIndexingData) +
        ", rng=" + rng +
        ", seedIndex=" + seedIndex +
        ", byteIndexInRandomness=" + byteIndexInRandomness +
        '}';
  }

  @Override
  public void removePrefixFromRemainingIndexingData(int amount) {
    this.remainingIndexingData = Arrays
        .copyOfRange(remainingIndexingData, amount, remainingIndexingData.length);
  }

  @Override
  public byte[] serialize() {
    byte[] prng = new byte[]{((Integer) rng.ordinal()).byteValue()};

    // TODO maybe only store 3 bytes for seeds, 2 bytes for byteIdx
    byte[] seedIdx = Ints.toByteArray(seedIndex);
    byte[] byteIdx = Ints.toByteArray(byteIndexInRandomness);
    return Bytes.concat(remainingIndexingData, prng, seedIdx, byteIdx);
  }

  @Override
  public int compareTo(AbstractRadixTrieDataPoint<byte[]> o) {
    return UnsignedBytes.lexicographicalComparator()
        .compare(this.getRemainingIndexingData(), o.getRemainingIndexingData());
  }

  @Override
  public void serialize(byte[] serializedData, int index) {
    int currentIndex = index;

    System.arraycopy(remainingIndexingData, 0, serializedData, currentIndex,
        remainingIndexingData.length);
    currentIndex += remainingIndexingData.length;

    System.arraycopy(new byte[]{((Integer) rng.ordinal()).byteValue()}, 0, serializedData,
        currentIndex, 1);
    currentIndex++;

    System.arraycopy(Ints.toByteArray(seedIndex), 0, serializedData, currentIndex, 4);
    currentIndex += 4;

    System.arraycopy(Ints.toByteArray(byteIndexInRandomness), 0, serializedData, currentIndex, 4);
  }
}
