package de.skuld.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import org.springframework.validation.annotation.Validated;

/**
 * InlineResponse200
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-02-22T13:22:34.185Z[GMT]")


public class InlineResponse200   {
  @JsonProperty("running")
  private Boolean running = null;

  public InlineResponse200 running(Boolean running) {
    this.running = running;
    return this;
  }

  /**
   * Get running
   * @return running
   **/
  @Schema(description = "")
  
    public Boolean isRunning() {
    return running;
  }

  public void setRunning(Boolean running) {
    this.running = running;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InlineResponse200 inlineResponse200 = (InlineResponse200) o;
    return Objects.equals(this.running, inlineResponse200.running);
  }

  @Override
  public int hashCode() {
    return Objects.hash(running);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InlineResponse200 {\n");
    
    sb.append("    running: ").append(toIndentedString(running)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
