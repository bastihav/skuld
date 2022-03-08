package de.skuld.radix;

import java.nio.ByteBuffer;
import java.util.Collection;

public abstract class AbstractRadixTrieData<D, P> {

  @SuppressWarnings("UnusedReturnValue")
  public abstract AbstractRadixTrieData<D, P> mergeData(AbstractRadixTrieData<D, P> other);

  public abstract Collection<P> getDataPoints(D indexingData);

  public abstract Collection<P> getDataPoints();

  public abstract String[] toLabels(D data);

  public abstract String concatenateLabels();

  public abstract String concatenateLabels(String[] labels);

  public abstract String getSeparator();

  public abstract void serialize(ByteBuffer mappedByteBuffer, int offset);

  public abstract void removePrefixFromRemainingIndexingData(int amount);

  public abstract int getElementCount();
}
