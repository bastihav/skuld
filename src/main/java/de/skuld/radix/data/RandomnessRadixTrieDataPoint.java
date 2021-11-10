package de.skuld.radix.data;

import de.skuld.prng.ImplementedPRNGs;

public class RandomnessRadixTrieDataPoint {
  private final ImplementedPRNGs rng;
  private final int seedIndex;
  private final int byteIndexInRandomness;

  public ImplementedPRNGs getRng() {
    return rng;
  }

  public int getSeedIndex() {
    return seedIndex;
  }

  public int getByteIndexInRandomness() {
    return byteIndexInRandomness;
  }

  public RandomnessRadixTrieDataPoint(ImplementedPRNGs rng, int seedIndex,
      int byteIndexInRandomness) {
    this.rng = rng;
    this.seedIndex = seedIndex;
    this.byteIndexInRandomness = byteIndexInRandomness;
  }

  @Override
  public String toString() {
    return "RandomnessRadixTrieDataPoint{" +
        "rng=" + rng +
        ", seedIndex=" + seedIndex +
        ", byteIndexInRandomness=" + byteIndexInRandomness +
        '}';
  }

  public byte[] serialize() {
    byte[] result = new byte[6];
    result[0] = ((Integer) rng.ordinal()).byteValue();
    // TODO serialze rest


    return result;
  }
}
