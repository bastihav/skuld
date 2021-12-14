package de.skuld.radix.data;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import de.skuld.prng.ImplementedPRNGs;
import de.skuld.radix.disk.DiskBasedRadixTrie;
import java.util.Arrays;

public class RandomnessRadixTrieDataPoint {

  private byte[] remainingBytes;
  private final ImplementedPRNGs rng;
  private final int seedIndex;
  private final int byteIndexInRandomness;

  public byte[] getRemainingBytes() {
    return remainingBytes;
  }

  public void setRemainingBytes(byte[] remainingBytes) {
    this.remainingBytes = remainingBytes;
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

  public RandomnessRadixTrieDataPoint(byte[] remainingBytes, ImplementedPRNGs rng, int seedIndex,
      int byteIndexInRandomness) {
    this.remainingBytes = remainingBytes;
    this.rng = rng;
    this.seedIndex = seedIndex;
    this.byteIndexInRandomness = byteIndexInRandomness;
  }

  public RandomnessRadixTrieDataPoint(byte[] serializedData) {
    this.remainingBytes = Arrays.copyOfRange(serializedData, 0, 29);
    this.rng = ImplementedPRNGs.values()[serializedData[29]];
    this.seedIndex = Ints.fromByteArray(Arrays.copyOfRange(serializedData, 30, 34));
    this.byteIndexInRandomness = Ints.fromByteArray(Arrays.copyOfRange(serializedData, 34, 38));;
  }

  public RandomnessRadixTrieDataPoint(ImplementedPRNGs rng, int seedIndex,
      int byteIndexInRandomness) {
    this(new byte[0], rng, seedIndex, byteIndexInRandomness);
  }

  @Override
  public String toString() {
    return "RandomnessRadixTrieDataPoint{" +
        "remainingBytes=" + Arrays.toString(remainingBytes) +
        ", rng=" + rng +
        ", seedIndex=" + seedIndex +
        ", byteIndexInRandomness=" + byteIndexInRandomness +
        '}';
  }

  public byte[] serialize(DiskBasedRadixTrie trie) {
    byte[] prng = new byte[]{((Integer) rng.ordinal()).byteValue()};

    // TODO maybe only store 3 bytes for seeds, 2 bytes for byteIdx
    byte[] seedIdx = Ints.toByteArray(seedIndex);
    byte[] byteIdx = Ints.toByteArray(byteIndexInRandomness);

    return Bytes.concat(remainingBytes, prng, seedIdx, byteIdx);
  }
}
