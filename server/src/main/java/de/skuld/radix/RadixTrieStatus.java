package de.skuld.radix;

import de.skuld.web.model.MetaData.StatusEnum;

public enum RadixTrieStatus {
  CREATED, GENERATING, GENERATED, SORTING_ADDING, FINISHED;

  public static RadixTrieStatus valueOf(StatusEnum status) {
    return valueOf(status.toString());
  }

  public static StatusEnum toAPIStatus(RadixTrieStatus status) {
    return StatusEnum.fromValue(status.toString());
  }
}
