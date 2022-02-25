package de.skuld.web.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Result
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-02-22T14:08:42.430Z[GMT]")


public class Result   {
  @JsonProperty("pairs")
  @Valid
  private List<ResultPairs> pairs = null;

  @JsonProperty("tlsTests")
  private ResultTlsTests tlsTests = null;

  public Result pairs(List<ResultPairs> pairs) {
    this.pairs = pairs;
    return this;
  }

  public Result addPairsItem(ResultPairs pairsItem) {
    if (this.pairs == null) {
      this.pairs = new ArrayList<ResultPairs>();
    }
    this.pairs.add(pairsItem);
    return this;
  }

  /**
   * Get pairs
   * @return pairs
   **/
  @Schema(description = "")
      @Valid
    public List<ResultPairs> getPairs() {
    return pairs;
  }

  public void setPairs(List<ResultPairs> pairs) {
    this.pairs = pairs;
  }

  public Result tlsTests(ResultTlsTests tlsTests) {
    this.tlsTests = tlsTests;
    return this;
  }

  /**
   * Get tlsTests
   * @return tlsTests
   **/
  @Schema(description = "")
  
    @Valid
    public ResultTlsTests getTlsTests() {
    return tlsTests;
  }

  public void setTlsTests(ResultTlsTests tlsTests) {
    this.tlsTests = tlsTests;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Result result = (Result) o;
    return Objects.equals(this.pairs, result.pairs) &&
        Objects.equals(this.tlsTests, result.tlsTests);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pairs, tlsTests);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Result {\n");
    
    sb.append("    pairs: ").append(toIndentedString(pairs)).append("\n");
    sb.append("    tlsTests: ").append(toIndentedString(tlsTests)).append("\n");
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
