package de.skuld.radix;

import com.google.common.primitives.UnsignedBytes;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractRadixTrieDataPoint<I> implements Comparable<AbstractRadixTrieDataPoint<I>>{
  protected I remainingIndexingData;

  public I getRemainingIndexingData() {
    return remainingIndexingData;
  }

  public void setRemainingIndexingData(I remainingIndexingData) {
    this.remainingIndexingData = remainingIndexingData;
  }

  abstract public void removePrefixFromRemainingIndexingData(int amount);

  abstract public byte[] serialize();

  abstract public void serialize(byte[] array, int startIndex);
}
