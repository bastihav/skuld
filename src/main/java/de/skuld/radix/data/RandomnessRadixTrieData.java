package de.skuld.radix.data;

import de.skuld.prng.ImplementedPRNGs;
import de.skuld.radix.AbstractRadixTrieData;
import de.skuld.util.BytePrinter;
import java.util.Arrays;
import java.util.Objects;

public class RandomnessRadixTrieData extends AbstractRadixTrieData<byte[]> {
  private final ImplementedPRNGs rng;
  private final int seedIndex;
  private final int byteIndexInRandomness;

  public RandomnessRadixTrieData(ImplementedPRNGs rng, int seedIndex,
      int byteIndexInRandomness) {
    this.rng = rng;
    this.seedIndex = seedIndex;
    this.byteIndexInRandomness = byteIndexInRandomness;
  }

  @Override
  public AbstractRadixTrieData<byte[]> mergeData(AbstractRadixTrieData<byte[]> other) {
    return null;
  }

  @Override
  public String[] toLabels(byte[] data) {
    return RandomnessRadixTrieData.staticToLabels(data);
  }

  public static String[] staticToLabels(byte[] data) {
    String[] result = new String[data.length];

    for (int i = 0; i < data.length; i++) {
      result[i] = BytePrinter.byteToHex(data[i]);
    }

    return result;
  }

  @Override
  public String concatenateLabels() {
    // TODO
    return concatenateLabels(this.toLabels(null));
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
  public String toString() {
    return "RandomnessRadixTrieData{" +
        "rng=" + rng +
        ", seedIndex=" + seedIndex +
        ", byteIndexInRandomness=" + byteIndexInRandomness +
        '}';
  }
}
