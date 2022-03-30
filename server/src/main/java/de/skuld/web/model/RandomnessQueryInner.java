package de.skuld.web.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;

/**
 * RandomnessQueryInner
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-02-22T14:03:55.597Z[GMT]")

public class RandomnessQueryInner {

  @JsonProperty("type")
  private TypeEnum type = null;
  @JsonProperty("randomness")
  @Valid
  private List<byte[]> randomness = null;

  public RandomnessQueryInner type(TypeEnum type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   *
   * @return type
   **/
  @Schema(description = "")

  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public RandomnessQueryInner randomness(List<byte[]> randomness) {
    this.randomness = randomness;
    return this;
  }

  public RandomnessQueryInner addRandomnessItem(byte[] randomnessItem) {
    if (this.randomness == null) {
      this.randomness = new ArrayList<byte[]>();
    }
    this.randomness.add(randomnessItem);
    return this;
  }

  /**
   * Randomness to be analyzed. Bytes in a single element must be consecutive. The first element in
   * this array should be the first randomness observed. The order in which randomness was observed
   * is important. Each element is base64 encoded.
   *
   * @return randomness
   **/
  @Schema(description = "Randomness to be analyzed. Bytes in a single element must be consecutive. The first element in this array should be the first randomness observed. The order in which randomness was observed is important. Each element is base64 encoded.")

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
    RandomnessQueryInner randomnessQueryInner = (RandomnessQueryInner) o;
    return Objects.equals(this.type, randomnessQueryInner.type) &&
        Objects.equals(this.randomness, randomnessQueryInner.randomness);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, randomness);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RandomnessQueryInner {\n");

    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    randomness: ").append(toIndentedString(randomness)).append("\n");
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
   * Gets or Sets type
   */
  public enum TypeEnum {
    TLS_SERVER_RANDOM("TLS_SERVER_RANDOM"),

    TLS_CLIENT_RANDOM("TLS_CLIENT_RANDOM"),

    CBC_IV("CBC_IV"),

    MISC("MISC");

    private String value;

    TypeEnum(String value) {
      this.value = value;
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

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }
  }
}
