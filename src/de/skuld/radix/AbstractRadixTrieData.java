package de.skuld.radix;

public abstract class AbstractRadixTrieData {
  public abstract AbstractRadixTrieData mergeData(AbstractRadixTrieData other);

  public abstract String[] toLabels();

  public abstract String concatenateLabels();

  public abstract String concatenateLabels(String[] labels);

  public abstract String getSeparator();
}
