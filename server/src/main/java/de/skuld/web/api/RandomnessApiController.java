package de.skuld.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Longs;
import de.skuld.processors.TLSRandomPreProcessor;
import de.skuld.radix.RadixTrie;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import de.skuld.radix.disk.DiskBasedRadixTrie;
import de.skuld.radix.disk.DiskBasedRadixTrieNode;
import de.skuld.radix.disk.DiskBasedRandomnessRadixTrieData;
import de.skuld.radix.disk.PathRadixTrieEdge;
import de.skuld.radix.manager.RadixManager;
import de.skuld.solvers.Solver;
import de.skuld.util.ByteHexUtil;
import de.skuld.util.ConfigurationHelper;
import de.skuld.web.model.RandomnessQuery;
import de.skuld.web.model.RandomnessQuery.TypeEnum;
import de.skuld.web.model.Result;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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

    @Autowired
    public RandomnessApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;

        this.radixManager = new RadixManager(Paths.get(ConfigurationHelper.getConfig().getString("radix.root")));

    }

    public ResponseEntity<Result> analyzeRandomness(@Parameter(in = ParameterIn.DEFAULT, description = "randomness to be analyzed", required=true, schema=@Schema()) @Valid @RequestBody RandomnessQuery body) {
        String accept = request.getHeader("Accept");
        if (accept != null && (accept.contains("application/json") || accept.contains("*/*"))) {
            System.out.println("body: ");
            body.getRandomness().forEach(ar -> ByteHexUtil.printBytesAsHex(ar));
            System.out.println("--");
            return new ResponseEntity<Result>(getAnalysisResult(body), HttpStatus.OK);
        }

        return new ResponseEntity<Result>(HttpStatus.NOT_IMPLEMENTED);
    }

    private Result getAnalysisResult(RandomnessQuery query) {
        Result result = new Result();

        List<byte[]> randomness;
        if (query.getType() == TypeEnum.TLS_CLIENT_RANDOM || query.getType() == TypeEnum.TLS_SERVER_RANDOM) {
            randomness = tlsRandomPreProcessor.preprocess(result, query.getRandomness());
        } else {
            randomness = query.getRandomness();
        }

        System.out.println("after preprocessing: ");
        randomness.forEach(ar -> ByteHexUtil.printBytesAsHex(ar));
        System.out.println("--");

        // TODO uncomment
        //DiskBasedRadixTrie radixTrie = radixManager.getTrie();
        DiskBasedRadixTrie radixTrie = radixManager.getTries().values().stream().findFirst().get();

        List<RandomnessRadixTrieDataPoint> dp = new ArrayList<>(randomness.size());

        for (byte[] arr : randomness) {
           radixTrie.search(arr).ifPresent(dp::add);
        }

        boolean sameSeeds = dp.stream().map(p -> p.getSeedIndex()).distinct().count() == 1;
        boolean sameRNG =  dp.stream().map(p -> p.getRng()).distinct().count() == 1;

        // we need to find datapoints for all randomness
        if (sameSeeds && sameRNG && dp.size() == randomness.size()) {
            long current = -1;

            for (RandomnessRadixTrieDataPoint p : dp) {
                long seedIndex = p.getSeedIndex();
                if (seedIndex > current) {
                    current = seedIndex;
                } else {
                    current = Long.MAX_VALUE;
                    break;
                }
            }

            if (current != Long.MAX_VALUE) {
                // increasing seed indices! This is a match!
                result.setPrng(dp.get(0).getRng().toString());
                result.setSeed(
                    Longs.toByteArray(radixTrie.getSeedMap().get(dp.get(0).getSeedIndex())));
            }
        } else {
            result.setSeed(null);
            result.setPrng(null);
        }

        // TODO statistical tests
        // TODO solvers


        return result;
    }
}
