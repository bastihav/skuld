package de.skuld.radix.data;

import de.skuld.radix.AbstractRadixTrieData;
import de.skuld.radix.disk.DiskBasedRadixTrie;
import de.skuld.util.ByteHexUtil;
import de.skuld.util.ConfigurationHelper;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class RandomnessRadixTrieData extends
    AbstractRadixTrieData<byte[], RandomnessRadixTrieDataPoint> {

  protected final Set<RandomnessRadixTrieDataPoint> dataPoints;

  public RandomnessRadixTrieData(RandomnessRadixTrieDataPoint data) {
    this.dataPoints = new HashSet<>();
    if (data != null) {
      dataPoints.add(data);
    }
  }

  private RandomnessRadixTrieData(Set<RandomnessRadixTrieDataPoint> data) {
    this.dataPoints = data;
  }

  public static String[] staticToLabels(byte[] data) {
    String[] result = new String[data.length];

    for (int i = 0; i < data.length; i++) {
      result[i] = ByteHexUtil.byteToHex(data[i]);
    }

    return result;
  }

  @Override
  public int getElementCount() {
    return dataPoints.size();
  }

  @Override
  public RandomnessRadixTrieData mergeData(
      AbstractRadixTrieData<byte[], RandomnessRadixTrieDataPoint> other) {
    this.dataPoints.addAll(other.getDataPoints());
    return this;
  }

  // TODO
  @Override
  public Collection<RandomnessRadixTrieDataPoint> getDataPoints(byte[] indexingData) {
    return null;
  }

  public byte[] serialize(DiskBasedRadixTrie trie) {
    int partitionSizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.partition.serialized");
    byte[] serializedData = new byte[dataPoints.size() * ConfigurationHelper.getConfig()
        .getInt("radix.partition.serialized")];

    AtomicInteger index = new AtomicInteger();
    dataPoints.forEach(dp -> {
      dp.serialize(serializedData, index.get());
      index.addAndGet(partitionSizeOnDisk);
    });

    return serializedData;
  }

  @Override
  public void serialize(ByteBuffer mappedByteBuffer, int offset) {
    int partitionSizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.partition.serialized");
    AtomicInteger index = new AtomicInteger(offset);
    dataPoints.forEach(dp -> dp.serialize(mappedByteBuffer, index.getAndAdd(partitionSizeOnDisk)));
  }

  @Override
  public void removePrefixFromRemainingIndexingData(int amount) {
    this.dataPoints.forEach(dp -> dp.removePrefixFromRemainingIndexingData(amount));
  }

  @Override
  public Collection<RandomnessRadixTrieDataPoint> getDataPoints() {
    return dataPoints;
  }

  @Override
  public String[] toLabels(byte[] data) {
    return RandomnessRadixTrieData.staticToLabels(data);
  }

  @Override
  public String concatenateLabels() {
    // TODO
    return concatenateLabels(this.toLabels(null));
  }

  @Override
  public String concatenateLabels(String[] labels) {
    return Arrays.stream(labels).reduce((a, b) -> a + getSeparator() + b).orElse("");
  }

  @Override
  public String getSeparator() {
    return "";
  }

  @Override
  public String toString() {
    return "RandomnessRadixTrieData{" +
        "dataPoints=" + getDataPoints() +
        '}';
  }
}
