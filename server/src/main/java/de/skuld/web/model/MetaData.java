package de.skuld.web.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import org.springframework.validation.annotation.Validated;

/**
 * MetaData
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-02-22T13:22:34.185Z[GMT]")

public class MetaData {

  @JsonProperty("uuid")
  private String uuid = null;

  @JsonProperty("date")
  private String date = null;
  @JsonProperty("status")
  private StatusEnum status = null;

  public MetaData uuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /**
   * Get uuid
   *
   * @return uuid
   **/
  @Schema(description = "")

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public MetaData date(String date) {
    this.date = date;
    return this;
  }

  /**
   * Get date
   *
   * @return date
   **/
  @Schema(description = "")

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public MetaData status(StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   *
   * @return status
   **/
  @Schema(description = "")

  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MetaData metaData = (MetaData) o;
    return Objects.equals(this.uuid, metaData.uuid) &&
        Objects.equals(this.date, metaData.date) &&
        Objects.equals(this.status, metaData.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid, date, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MetaData {\n");

    sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
    sb.append("    date: ").append(toIndentedString(date)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first
   * line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

  /**
   * Gets or Sets status
   */
  public enum StatusEnum {
    CREATED("CREATED"),

    GENERATING("GENERATING"),

    GENERATED("GENERATED"),

    SORTING_ADDING("SORTING_ADDING"),

    FINISHED("FINISHED");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @JsonCreator
    public static StatusEnum fromValue(String text) {
      for (StatusEnum b : StatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }
  }
}
