/**
 * NOTE: This class is auto generated by the swagger code generator program (3.0.32).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package de.skuld.web.api;

import de.skuld.web.model.RandomnessQuery;
import de.skuld.web.model.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import javax.annotation.processing.Generated;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-01-24T09:48:19.864Z[GMT]")
@Validated
public interface RandomnessApi {

    @Operation(summary = "analyzes randomness", description = "Randomness submitted to this endpoint will be analyzed. Statistical tests will be performed and we will try to solve for a PRNG and seed combination", tags={ "Public API" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "analysis results", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))) })
    @RequestMapping(value = "/randomness",
        produces = { "application/json" }, 
        consumes = { "application/json" }, 
        method = RequestMethod.POST)
    ResponseEntity<Result> analyzeRandomness(@Parameter(in = ParameterIn.DEFAULT, description = "randomness to be analyzed", required=true, schema=@Schema()) @Valid @RequestBody RandomnessQuery body);

}

