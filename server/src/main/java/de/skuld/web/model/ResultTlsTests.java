package de.skuld.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import org.springframework.validation.annotation.Validated;

/**
 * ResultTlsTests
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-02-22T14:08:42.430Z[GMT]")


public class ResultTlsTests   {
  @JsonProperty("unixtime")
  private Boolean unixtime = null;

  @JsonProperty("reusesIV")
  private Boolean reusesIV = null;

  @JsonProperty("reusesRandom")
  private Boolean reusesRandom = null;

  @JsonProperty("allZeroIV")
  private Boolean allZeroIV = null;

  @JsonProperty("allZeroRandom")
  private Boolean allZeroRandom = null;

  public ResultTlsTests unixtime(Boolean unixtime) {
    this.unixtime = unixtime;
    return this;
  }

  /**
   * Get unixtime
   * @return unixtime
   **/
  @Schema(description = "")
  
    public Boolean isUnixtime() {
    return unixtime;
  }

  public void setUnixtime(Boolean unixtime) {
    this.unixtime = unixtime;
  }

  public ResultTlsTests reusesIV(Boolean reusesIV) {
    this.reusesIV = reusesIV;
    return this;
  }

  /**
   * Get reusesIV
   * @return reusesIV
   **/
  @Schema(description = "")
  
    public Boolean isReusesIV() {
    return reusesIV;
  }

  public void setReusesIV(Boolean reusesIV) {
    this.reusesIV = reusesIV;
  }

  public ResultTlsTests reusesRandom(Boolean reusesRandom) {
    this.reusesRandom = reusesRandom;
    return this;
  }

  /**
   * Get reusesRandom
   * @return reusesRandom
   **/
  @Schema(description = "")
  
    public Boolean isReusesRandom() {
    return reusesRandom;
  }

  public void setReusesRandom(Boolean reusesRandom) {
    this.reusesRandom = reusesRandom;
  }

  public ResultTlsTests allZeroIV(Boolean allZeroIV) {
    this.allZeroIV = allZeroIV;
    return this;
  }

  /**
   * Get allZeroIV
   * @return allZeroIV
   **/
  @Schema(description = "")
  
    public Boolean isAllZeroIV() {
    return allZeroIV;
  }

  public void setAllZeroIV(Boolean allZeroIV) {
    this.allZeroIV = allZeroIV;
  }

  public ResultTlsTests allZeroRandom(Boolean allZeroRandom) {
    this.allZeroRandom = allZeroRandom;
    return this;
  }

  /**
   * Get allZeroRandom
   * @return allZeroRandom
   **/
  @Schema(description = "")
  
    public Boolean isAllZeroRandom() {
    return allZeroRandom;
  }

  public void setAllZeroRandom(Boolean allZeroRandom) {
    this.allZeroRandom = allZeroRandom;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResultTlsTests resultTlsTests = (ResultTlsTests) o;
    return Objects.equals(this.unixtime, resultTlsTests.unixtime) &&
        Objects.equals(this.reusesIV, resultTlsTests.reusesIV) &&
        Objects.equals(this.reusesRandom, resultTlsTests.reusesRandom) &&
        Objects.equals(this.allZeroIV, resultTlsTests.allZeroIV) &&
        Objects.equals(this.allZeroRandom, resultTlsTests.allZeroRandom);
  }

  @Override
  public int hashCode() {
    return Objects.hash(unixtime, reusesIV, reusesRandom, allZeroIV, allZeroRandom);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResultTlsTests {\n");
    
    sb.append("    unixtime: ").append(toIndentedString(unixtime)).append("\n");
    sb.append("    reusesIV: ").append(toIndentedString(reusesIV)).append("\n");
    sb.append("    reusesRandom: ").append(toIndentedString(reusesRandom)).append("\n");
    sb.append("    allZeroIV: ").append(toIndentedString(allZeroIV)).append("\n");
    sb.append("    allZeroRandom: ").append(toIndentedString(allZeroRandom)).append("\n");
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
