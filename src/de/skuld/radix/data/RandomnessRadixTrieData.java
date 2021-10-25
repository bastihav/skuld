package de.skuld.radix.data;

import de.skuld.prng.ImplementedPRNGs;
import de.skuld.radix.AbstractRadixTrieData;
import de.skuld.util.BytePrinter;
import java.util.Arrays;
import java.util.Objects;

public class RandomnessRadixTrieData extends AbstractRadixTrieData {
  private final byte[] data;
  private final ImplementedPRNGs rng;
  private final int seedIndex;
  private final int byteIndexInRandomness;

  public RandomnessRadixTrieData(byte[] data, ImplementedPRNGs rng, int seedIndex,
      int byteIndexInRandomness) {
    this.data = data;
    this.rng = rng;
    this.seedIndex = seedIndex;
    this.byteIndexInRandomness = byteIndexInRandomness;
  }

  @Override
  public AbstractRadixTrieData mergeData(AbstractRadixTrieData other) {
    return null;
  }

  @Override
  public String[] toLabels() {
    String[] result = new String[data.length];

    for (int i = 0; i < data.length; i++) {
      result[i] = BytePrinter.byteToHex(data[i]);
    }

    return result;
  }

  @Override
  public String concatenateLabels() {
    return concatenateLabels(this.toLabels());
  }

  @Override
  public String concatenateLabels(String[] labels) {
    return Arrays.stream(labels).reduce((a,b) -> a + getSeparator() + b).orElse("");
  }

  @Override
  public String getSeparator() {
    return "";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RandomnessRadixTrieData that = (RandomnessRadixTrieData) o;
    return seedIndex == that.seedIndex && byteIndexInRandomness == that.byteIndexInRandomness
        && Arrays.equals(data, that.data) && rng == that.rng;
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(rng, seedIndex, byteIndexInRandomness);
    result = 31 * result + Arrays.hashCode(data);
    return result;
  }

  @Override
  public String toString() {
    return "RandomnessRadixTrieData{" +
        "data=" + Arrays.toString(data) +
        ", rng=" + rng +
        ", seedIndex=" + seedIndex +
        ", byteIndexInRandomness=" + byteIndexInRandomness +
        '}';
  }
}
