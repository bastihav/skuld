package de.skuld.web.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * RandomnessQuery
 */
@Validated
@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-01-24T09:48:19.864Z[GMT]")


public class RandomnessQuery   {
  /**
   * Gets or Sets type
   */
  public enum TypeEnum {
    TLS_SERVER_RANDOM("TLS_SERVER_RANDOM"),
    
    TLS_CLIENT_RANDOM("TLS_CLIENT_RANDOM"),
    
    MISC("MISC");

    private final String value;

    TypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TypeEnum fromValue(String text) {
      for (TypeEnum b : TypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("type")
  private TypeEnum type = null;

  @JsonProperty("randomness")
  @Valid
  private List<byte[]> randomness = new ArrayList<byte[]>();

  public RandomnessQuery type(TypeEnum type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
   **/
  @Schema(required = true, description = "")
      @NotNull

    public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public RandomnessQuery randomness(List<byte[]> randomness) {
    this.randomness = randomness;
    return this;
  }

  public RandomnessQuery addRandomnessItem(byte[] randomnessItem) {
    this.randomness.add(randomnessItem);
    return this;
  }

  /**
   * Randomness to be analyzed. Bytes in a single element must be consecutive. The first element in this array should be the first randomness observed. The order in which randomness was observed is important. Each element is base64 encoded.
   * @return randomness
   **/
  @Schema(required = true, description = "Randomness to be analyzed. Bytes in a single element must be consecutive. The first element in this array should be the first randomness observed. The order in which randomness was observed is important. Each element is base64 encoded.")
      @NotNull

    public List<byte[]> getRandomness() {
    return randomness;
  }

  public void setRandomness(List<byte[]> randomness) {
    this.randomness = randomness;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RandomnessQuery randomnessQuery = (RandomnessQuery) o;
    return Objects.equals(this.type, randomnessQuery.type) &&
        Objects.equals(this.randomness, randomnessQuery.randomness);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, randomness);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RandomnessQuery {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    randomness: ").append(toIndentedString(randomness)).append("\n");
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
