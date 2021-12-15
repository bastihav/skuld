package de.skuld.radix;

public abstract class AbstractRadixTrieDataPoint<I> implements
    Comparable<AbstractRadixTrieDataPoint<I>> {

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
