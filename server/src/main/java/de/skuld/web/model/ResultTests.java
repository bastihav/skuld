package de.skuld.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import javax.annotation.Generated;
import org.springframework.validation.annotation.Validated;

/**
 * ResultTests
 */
@Validated
@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-01-24T09:48:19.864Z[GMT]")


public class ResultTests   {
  @JsonProperty("testName")
  private String testName = null;

  @JsonProperty("testResult")
  private String testResult = null;

  public ResultTests testName(String testName) {
    this.testName = testName;
    return this;
  }

  /**
   * Get testName
   * @return testName
   **/
  @Schema(description = "")
  
    public String getTestName() {
    return testName;
  }

  public void setTestName(String testName) {
    this.testName = testName;
  }

  public ResultTests testResult(String testResult) {
    this.testResult = testResult;
    return this;
  }

  /**
   * Get testResult
   * @return testResult
   **/
  @Schema(description = "")
  
    public String getTestResult() {
    return testResult;
  }

  public void setTestResult(String testResult) {
    this.testResult = testResult;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResultTests resultTests = (ResultTests) o;
    return Objects.equals(this.testName, resultTests.testName) &&
        Objects.equals(this.testResult, resultTests.testResult);
  }

  @Override
  public int hashCode() {
    return Objects.hash(testName, testResult);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResultTests {\n");
    
    sb.append("    testName: ").append(toIndentedString(testName)).append("\n");
    sb.append("    testResult: ").append(toIndentedString(testResult)).append("\n");
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
