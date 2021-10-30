package de.skuld.radix;

public abstract class AbstractRadixTrieData<D> {
  public abstract AbstractRadixTrieData<D> mergeData(AbstractRadixTrieData<D> other);

  public abstract String[] toLabels(D data);

  public abstract String concatenateLabels();

  public abstract String concatenateLabels(String[] labels);

  public abstract String getSeparator();
}
