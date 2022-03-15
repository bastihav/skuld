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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.commons.lang3.ArrayUtils;
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
  PrintWriter writer;

  @Autowired
  public RandomnessApiController(ObjectMapper objectMapper, HttpServletRequest request) {
    this.request = request;

    this.radixManager = RadixManager.getInstance(
        Paths.get(ConfigurationHelper.getConfig().getString("radix.root")));

/*    Path tempDir = Paths.get(ConfigurationHelper.getConfig().getString("radix.root"));
    String date = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(new Date());
    File dataFile = tempDir.resolve("efficacy_analysis_" + date + ".csv").toFile();

    if (!dataFile.exists()) {
      try {
        dataFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try {
      writer = new PrintWriter(new FileOutputStream(dataFile), true);
      writer.println("time_ns" + "," + "found_result" + ",");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }*/
  }

  public ResponseEntity<Result> analyzeRandomness(
      @Parameter(in = ParameterIn.DEFAULT, description = "randomness to be analyzed", required = true, schema = @Schema()) @Valid @RequestBody RandomnessQuery body) {
    String accept = request.getHeader("Accept");
    if (accept != null && (accept.contains("application/json") || accept.contains("*/*"))) {

      return new ResponseEntity<>(getAnalysisResult(body), HttpStatus.OK);
    }

    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }

  private Result getAnalysisResult(RandomnessQuery query) {
    long ping = System.nanoTime();
    // TODO add time keeping!
    Result result = new Result();
    result.setPairs(new ArrayList<>());
    result.setTlsTests(new ResultTlsTests());

    DiskBasedRadixTrie radixTrie = radixManager.getTrie();
    LOGGER.info("Performing analysis with " + radixTrie);

    BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    int maxThreads = ConfigurationHelper.getConfig().getInt("radix.analysis.threads");
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(maxThreads, maxThreads, 20,
        TimeUnit.MILLISECONDS, queue);

    for (RandomnessQueryInner randomnessQueryInner : query) {
      threadPoolExecutor.execute(() -> {
        List<byte[]> randomness;

        // PreProcessors set TLS tests (all zero IV, etc..)
        switch (randomnessQueryInner.getType()) {
          case TLS_CLIENT_RANDOM:
          case TLS_SERVER_RANDOM:
            randomness = tlsRandomPreProcessor.preprocess(result,
                randomnessQueryInner.getRandomness());
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

        List<List<byte[]>> rand = new ArrayList<>();
        rand.add(finalRandomness);

        // randomness might also be taken in a different order, i.e. int wise with little endian
        List<byte[]> intOrder = new ArrayList<>();
        for (byte[] bytes : finalRandomness) {
          ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
          byte[] transposed = new byte[bytes.length];
          ByteBuffer transposedBuffer = ByteBuffer.wrap(transposed).order(ByteOrder.BIG_ENDIAN);

          for (int i = 0; i < bytes.length / Integer.BYTES; i++) {
            transposedBuffer.putInt(buffer.getInt());
          }
          while (buffer.hasRemaining()) {
            transposedBuffer.put(buffer.get());
          }

          intOrder.add(transposed);
        }
        rand.add(intOrder);

        // long wise with little endian
        List<byte[]> longOrder = new ArrayList<>();
        for (byte[] bytes : finalRandomness) {
          ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
          byte[] transposed = new byte[bytes.length];
          ByteBuffer transposedBuffer = ByteBuffer.wrap(transposed).order(ByteOrder.BIG_ENDIAN);

          for (int i = 0; i < bytes.length / Long.BYTES; i++) {
            transposedBuffer.putLong(buffer.getLong());
          }
          while (buffer.hasRemaining()) {
            transposedBuffer.put(buffer.get());
          }
          longOrder.add(transposed);
        }
        rand.add(longOrder);

        // or fill the return vector bottom up
        List<byte[]> flipped = new ArrayList<>();
        for (byte[] bytes : finalRandomness) {
          byte[] reversed = new byte[bytes.length];
          System.arraycopy(bytes, 0, reversed, 0, bytes.length);
          ArrayUtils.reverse(bytes);
          flipped.add(reversed);
        }
        rand.add(flipped);

        for (List<byte[]> bytes : rand) {
          if (radixTrie != null) {
            AnalysisUtil.analyzeWithPrecomputations(radixTrie, bytes, result,
                randomnessQueryInner.getType(), threadPoolExecutor);
          }
          AnalysisUtil.analyzeWithSolvers(bytes, result, randomnessQueryInner.getType(),
              threadPoolExecutor);
        }
      });
    }
    try {
      Thread.sleep(100);
      threadPoolExecutor.shutdown();
      LOGGER.info("Waiting for threads now");
      boolean finishedGracefully = threadPoolExecutor.awaitTermination(5, TimeUnit.SECONDS);
      LOGGER.info("Threads finished before timeout? " + finishedGracefully);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    long pong = System.nanoTime();
    //writer.println((pong-ping) + "," + (result.getPairs() != null && !result.getPairs().isEmpty()) + "," + (result.getPairs() != null && !result.getPairs().isEmpty() ? result.getPairs().get(0).getPrng(): "-"));
    result.setPairs(result.getPairs().stream().distinct().collect(Collectors.toList()));
    return result;
  }
}
