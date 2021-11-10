package de.skuld.radix.data;

import de.skuld.prng.ImplementedPRNGs;
import de.skuld.radix.AbstractRadixTrieData;
import de.skuld.util.BytePrinter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class RandomnessRadixTrieData extends AbstractRadixTrieData<byte[], RandomnessRadixTrieDataPoint> {
  private final Set<RandomnessRadixTrieDataPoint> dataPoints;

  public RandomnessRadixTrieData(RandomnessRadixTrieDataPoint data) {
    this.dataPoints = new HashSet<>();
    if (data != null) {
      dataPoints.add(data);
    }
  }

  private RandomnessRadixTrieData(Set<RandomnessRadixTrieDataPoint> data) {
    this.dataPoints = data;
  }

  @Override
  public AbstractRadixTrieData<byte[], RandomnessRadixTrieDataPoint> mergeData(AbstractRadixTrieData<byte[], RandomnessRadixTrieDataPoint> other) {
    System.out.println("calling merger ");
    this.dataPoints.addAll(other.getDataPoints());
    System.out.println(this.dataPoints);
    return this;
  }

  @Override
  public Collection<RandomnessRadixTrieDataPoint> getDataPoints() {
    return dataPoints;
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
        "dataPoints=" + dataPoints +
        '}';
  }
}
