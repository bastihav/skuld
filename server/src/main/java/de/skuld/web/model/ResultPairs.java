package de.skuld.web.model;

import de.skuld.web.model.RandomnessQueryInner.TypeEnum;
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
 * ResultPairs
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-02-22T14:08:42.430Z[GMT]")


public class ResultPairs   {
  /**
   * Gets or Sets type
   */

  @JsonProperty("type")
  private TypeEnum type = null;

  @JsonProperty("seeds")
  @Valid
  private List<byte[]> seeds = null;

  @JsonProperty("prng")
  private String prng = null;

  public ResultPairs type(TypeEnum type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
   **/
  @Schema(description = "")
  
    public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public ResultPairs seeds(List<byte[]> seeds) {
    this.seeds = seeds;
    return this;
  }

  public ResultPairs addSeedsItem(byte[] seedsItem) {
    if (this.seeds == null) {
      this.seeds = new ArrayList<byte[]>();
    }
    this.seeds.add(seedsItem);
    return this;
  }

  /**
   * Get seeds
   * @return seeds
   **/
  @Schema(description = "")
  
    public List<byte[]> getSeeds() {
    return seeds;
  }

  public void setSeeds(List<byte[]> seeds) {
    this.seeds = seeds;
  }

  public ResultPairs prng(String prng) {
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResultPairs resultPairs = (ResultPairs) o;
    return Objects.equals(this.type, resultPairs.type) &&
        Objects.equals(this.seeds, resultPairs.seeds) &&
        Objects.equals(this.prng, resultPairs.prng);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, seeds, prng);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResultPairs {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    seeds: ").append(toIndentedString(seeds)).append("\n");
    sb.append("    prng: ").append(toIndentedString(prng)).append("\n");
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
