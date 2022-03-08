package de.skuld.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.skuld.processors.CBCIVPreProcessor;
import de.skuld.processors.TLSRandomPreProcessor;
import de.skuld.radix.disk.DiskBasedRadixTrie;
import de.skuld.radix.manager.RadixManager;
import de.skuld.util.AnalysisUtil;
import de.skuld.util.ByteHexUtil;
import de.skuld.util.ConfigurationHelper;
import de.skuld.web.model.RandomnessQuery;
import de.skuld.web.model.RandomnessQueryInner;
import de.skuld.web.model.Result;
import de.skuld.web.model.ResultTlsTests;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RandomnessApiController implements RandomnessApi {

    private static final Logger LOGGER = LogManager.getLogger();

    private final HttpServletRequest request;

    private final RadixManager<DiskBasedRadixTrie> radixManager;
    private final TLSRandomPreProcessor tlsRandomPreProcessor = new TLSRandomPreProcessor();
    private final CBCIVPreProcessor cbcivPreProcessor = new CBCIVPreProcessor();

    @Autowired
    public RandomnessApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.request = request;

        this.radixManager = RadixManager.getInstance(Paths.get(ConfigurationHelper.getConfig().getString("radix.root")));
    }

    public ResponseEntity<Result> analyzeRandomness(@Parameter(in = ParameterIn.DEFAULT, description = "randomness to be analyzed", required=true, schema=@Schema()) @Valid @RequestBody RandomnessQuery body) {
        String accept = request.getHeader("Accept");
        if (accept != null && (accept.contains("application/json") || accept.contains("*/*"))) {

            return new ResponseEntity<>(getAnalysisResult(body), HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    private Result getAnalysisResult(RandomnessQuery query) {
        // TODO add time keeping!
        Result result = new Result();
        result.setPairs(new ArrayList<>());
        result.setTlsTests(new ResultTlsTests());


        DiskBasedRadixTrie radixTrie = radixManager.getTrie();
        LOGGER.info("Performing analysis with " + radixTrie);

        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        int maxThreads = ConfigurationHelper.getConfig().getInt("radix.analysis.threads");
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(maxThreads, maxThreads, 20, TimeUnit.MILLISECONDS, queue);

        for (RandomnessQueryInner randomnessQueryInner : query) {
            threadPoolExecutor.execute(()-> {
                List<byte[]> randomness;

                // PreProcessors set TLS tests (all zero IV, etc..)
                switch (randomnessQueryInner.getType()) {
                    case TLS_CLIENT_RANDOM:
                    case TLS_SERVER_RANDOM:
                        randomness = tlsRandomPreProcessor.preprocess(result, randomnessQueryInner.getRandomness());
                        break;
                    case CBC_IV:
                        randomness = cbcivPreProcessor.preprocess(result, randomnessQueryInner.getRandomness());
                        break;
                    case MISC:
                    default:
                        randomness = randomnessQueryInner.getRandomness();
                        break;
                }

                List<byte[]> finalRandomness = randomness;
                StringBuilder sb = new StringBuilder("analyze the randomness ");
                randomness.forEach(arr -> sb.append(ByteHexUtil.bytesToHex(arr)).append("\n"));
                System.out.println(sb);

                if (radixTrie != null) {
                    AnalysisUtil.analyzeWithPrecomputations(radixTrie, finalRandomness, result, randomnessQueryInner.getType(), threadPoolExecutor);
                }
                AnalysisUtil.analyzeWithSolvers(finalRandomness, result, randomnessQueryInner.getType(), threadPoolExecutor);
            });

        }
        threadPoolExecutor.shutdown();
        try {
            LOGGER.info("Waiting for threads now");
            boolean finishedGracefully = threadPoolExecutor.awaitTermination(5, TimeUnit.SECONDS);
            LOGGER.info("Threads finished before timeout? " + finishedGracefully);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }
}
