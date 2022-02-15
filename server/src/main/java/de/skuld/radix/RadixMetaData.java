package de.skuld.radix;

import java.util.Date;
import java.util.UUID;

public class RadixMetaData {
  private UUID id;
  private Date date;
  private RadixTrieStatus status;

  public RadixMetaData(UUID id, Date date, RadixTrieStatus status) {
    this.id = id;
    this.date = date;
    this.status = status;
  }

  public RadixMetaData() {

  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public RadixTrieStatus getStatus() {
    return status;
  }

  public void setStatus(RadixTrieStatus status) {
    this.status = status;
  }
}
