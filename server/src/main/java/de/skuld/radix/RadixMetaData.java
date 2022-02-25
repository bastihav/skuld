package de.skuld.radix;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import de.skuld.web.model.MetaData;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
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

  public RadixMetaData(MetaData metaData) {
    this(UUID.fromString(metaData.getUuid()), Date.from(Instant.parse(metaData.getDate())), RadixTrieStatus.valueOf(metaData.getStatus()));
  }

  public MetaData toAPIMetaData() {
    MetaData metaData = new MetaData();
    metaData.setDate(date.toInstant().toString());
    metaData.setUuid(id.toString());
    metaData.setStatus(RadixTrieStatus.toAPIStatus(status));
    return metaData;
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
