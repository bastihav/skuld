package de.skuld.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Longs;
import de.skuld.prng.JavaRandom;
import de.skuld.processors.CBCIVPreProcessor;
import de.skuld.processors.TLSRandomPreProcessor;
import de.skuld.radix.RadixTrie;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import de.skuld.radix.disk.DiskBasedRadixTrie;
import de.skuld.radix.disk.DiskBasedRadixTrieNode;
import de.skuld.radix.disk.DiskBasedRandomnessRadixTrieData;
import de.skuld.radix.disk.PathRadixTrieEdge;
import de.skuld.radix.manager.RadixManager;
import de.skuld.solvers.JavaRandomSolver;
import de.skuld.solvers.Solver;
import de.skuld.solvers.XoShiRo128StarStarSolver;
import de.skuld.util.AnalysisUtil;
import de.skuld.util.ArrayUtil;
import de.skuld.util.ByteHexUtil;
import de.skuld.util.ConfigurationHelper;
import de.skuld.web.model.RandomnessQuery;
import de.skuld.web.model.RandomnessQueryInner;
import de.skuld.web.model.RandomnessQueryInner.TypeEnum;
import de.skuld.web.model.Result;
import de.skuld.web.model.ResultPairs;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-01-24T09:48:19.864Z[GMT]")
@RestController
public class RandomnessApiController implements RandomnessApi {

    private static final Logger log = LoggerFactory.getLogger(RandomnessApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private final RadixManager<DiskBasedRadixTrie> radixManager;
    private final TLSRandomPreProcessor tlsRandomPreProcessor = new TLSRandomPreProcessor();
    private final CBCIVPreProcessor cbcivPreProcessor = new CBCIVPreProcessor();

    @Autowired
    public RandomnessApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;

        this.radixManager = RadixManager.getInstance(Paths.get(ConfigurationHelper.getConfig().getString("radix.root")));

    }

    public ResponseEntity<Result> analyzeRandomness(@Parameter(in = ParameterIn.DEFAULT, description = "randomness to be analyzed", required=true, schema=@Schema()) @Valid @RequestBody RandomnessQuery body) {
        String accept = request.getHeader("Accept");
        if (accept != null && (accept.contains("application/json") || accept.contains("*/*"))) {

            return new ResponseEntity<Result>(getAnalysisResult(body), HttpStatus.OK);
        }

        return new ResponseEntity<Result>(HttpStatus.NOT_IMPLEMENTED);
    }

    private Result getAnalysisResult(RandomnessQuery query) {
        // TODO add time keeping!

        Result result = new Result();

        List<byte[]> randomness;
        // PreProcessors set TLS tests (all zero IV, etc..)
        DiskBasedRadixTrie radixTrie = radixManager.getTrie();
        //DiskBasedRadixTrie radixTrie = radixManager.getTries().values().stream().findFirst().get();

        for (RandomnessQueryInner randomnessQueryInner : query) {
            if (randomnessQueryInner.getType() == TypeEnum.TLS_CLIENT_RANDOM || randomnessQueryInner.getType() == TypeEnum.TLS_SERVER_RANDOM) {
                randomness = tlsRandomPreProcessor.preprocess(result, randomnessQueryInner.getRandomness());
            } else if(randomnessQueryInner.getType() == TypeEnum.CBC_IV) {
                randomness = cbcivPreProcessor.preprocess(result, randomnessQueryInner.getRandomness());
            } else {
                randomness = randomnessQueryInner.getRandomness();
            }

            if (radixTrie != null) {
                AnalysisUtil.analyzeWithPrecomputations(radixTrie, randomness, result, randomnessQueryInner.getType());
            }

            AnalysisUtil.analyzeWithSolvers(randomness, result, randomnessQueryInner.getType());
        }
        return result;
    }
}
