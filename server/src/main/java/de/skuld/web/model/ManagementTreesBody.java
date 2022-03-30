package de.skuld.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import org.springframework.validation.annotation.Validated;

/**
 * ManagementTreesBody
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-02-22T13:22:34.185Z[GMT]")

public class ManagementTreesBody {

  @JsonProperty("date")
  private String date = null;

  public ManagementTreesBody date(String date) {
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ManagementTreesBody managementTreesBody = (ManagementTreesBody) o;
    return Objects.equals(this.date, managementTreesBody.date);
  }

  @Override
  public int hashCode() {
    return Objects.hash(date);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ManagementTreesBody {\n");

    sb.append("    date: ").append(toIndentedString(date)).append("\n");
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
}
