package de.skuld.web.model;

import de.skuld.web.model.ResultTests;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-01-28T09:31:31.211Z[GMT]")


public class Result   {
  @JsonProperty("seed")
  private byte[] seed = null;

  @JsonProperty("prng")
  private String prng = null;

  @JsonProperty("unixtime")
  private Boolean unixtime = false;

  /**
   * Gets or Sets fallbackProtection
   */
  public enum FallbackProtectionEnum {
    _12("TLS_13_TO_TLS_12"),

    _11("TLS_13_TO_TLS_11");

    private final String value;

    FallbackProtectionEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static FallbackProtectionEnum fromValue(String text) {
      for (FallbackProtectionEnum b : FallbackProtectionEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("fallbackProtection")
  private FallbackProtectionEnum fallbackProtection = null;

  @JsonProperty("helloRetry")
  private Boolean helloRetry = false;

  public Result seed(byte[] seed) {
    this.seed = seed;
    return this;
  }

  /**
   * Get seed
   * @return seed
   **/
  @Schema(description = "")

  public byte[] getSeed() {
    return seed;
  }

  public void setSeed(byte[] seed) {
    this.seed = seed;
  }

  public Result prng(String prng) {
    this.prng = prng;
    return this;
  }

  /**
   * Get prng
   * @return prng
   **/
  @Schema(description = "")

  public String getPrng() {
    return prng;
  }

  public void setPrng(String prng) {
    this.prng = prng;
  }

  public Result unixtime(Boolean unixtime) {
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

  public Result fallbackProtection(FallbackProtectionEnum fallbackProtection) {
    this.fallbackProtection = fallbackProtection;
    return this;
  }

  /**
   * Get fallbackProtection
   * @return fallbackProtection
   **/
  @Schema(description = "")

  public FallbackProtectionEnum getFallbackProtection() {
    return fallbackProtection;
  }

  public void setFallbackProtection(FallbackProtectionEnum fallbackProtection) {
    this.fallbackProtection = fallbackProtection;
  }

  public Result helloRetry(Boolean helloRetry) {
    this.helloRetry = helloRetry;
    return this;
  }

  /**
   * Get helloRetry
   * @return helloRetry
   **/
  @Schema(description = "")

  public Boolean isHelloRetry() {
    return helloRetry;
  }

  public void setHelloRetry(Boolean helloRetry) {
    this.helloRetry = helloRetry;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Result result = (Result) o;
    return
        Objects.equals(this.seed, result.seed) &&
        Objects.equals(this.unixtime, result.unixtime) &&
        Objects.equals(this.fallbackProtection, result.fallbackProtection) &&
        Objects.equals(this.helloRetry, result.helloRetry);
  }

  @Override
  public int hashCode() {
    return Objects.hash( seed, unixtime, fallbackProtection, helloRetry);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Result {\n");

    sb.append("    seed: ").append(toIndentedString(seed)).append("\n");
    sb.append("    unixtime: ").append(toIndentedString(unixtime)).append("\n");
    sb.append("    fallbackProtection: ").append(toIndentedString(fallbackProtection)).append("\n");
    sb.append("    helloRetry: ").append(toIndentedString(helloRetry)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
