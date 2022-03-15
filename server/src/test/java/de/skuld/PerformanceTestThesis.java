package de.skuld;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Longs;
import de.skuld.prng.ImplementedPRNGs;
import de.skuld.prng.PRNG;
import de.skuld.radix.manager.SeedManager;
import de.skuld.util.ByteHexUtil;
import de.skuld.util.ConfigurationHelper;
import de.skuld.web.model.RandomnessQuery;
import de.skuld.web.model.RandomnessQueryInner;
import de.skuld.web.model.RandomnessQueryInner.TypeEnum;
import de.skuld.web.model.Result;
import de.skuld.web.model.ResultPairs;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import org.assertj.core.util.Strings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PerformanceTestThesis {
  private static final String ANALYSIS_SERVER_IP = "http://tls-randomness.cs.upb.de/api/randomness";
  //private static final String ANALYSIS_SERVER_IP = "http://localhost/api/randomness";
  static PrintWriter writer;

  @BeforeAll
  static void setupFile() {
    Path tempDir = Paths.get("G:\\skuld\\");
    String date = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(new java.util.Date());
    File dataFile = tempDir.resolve("analysis_" + date + ".csv").toFile();

    if (!dataFile.exists()) {
      try {
        dataFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try {
      writer = new PrintWriter(new FileOutputStream(dataFile), true);
      writer.println("time_ns,positive,false_positive,negative,false_negative,actual_prng,actual_seed,expected_prng,expected_seed,check_manually");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Test
    public void test() {
      SeedManager seedManager = new SeedManager();
      long[] preGenSeeds = seedManager.getSeeds(Date.from(Instant.parse("2022-03-15T00:00:00Z")));
      long[] notGenSeeds = seedManager.getSeeds(Date.from(Instant.parse("2022-03-20T00:00:00Z")));

      ImplementedPRNGs[] preGenPRNGs = new ImplementedPRNGs[]{ImplementedPRNGs.PCG32, ImplementedPRNGs.MERSENNE_TWISTER_PYTHON, ImplementedPRNGs.XOSHIRO256STARSTAR, ImplementedPRNGs.XORSHIFT128PLUS, ImplementedPRNGs.JAVA_RANDOM, ImplementedPRNGs.XOSHIRO128STARSTAR};
      ImplementedPRNGs[] notGenPRNGs = new ImplementedPRNGs[]{ImplementedPRNGs.GOLANGLCG, ImplementedPRNGs.GLIBCLCG, ImplementedPRNGs.KNUTH, ImplementedPRNGs.CHA_CHA_8, ImplementedPRNGs.CHA_CHA_12, ImplementedPRNGs.CHA_CHA_20};

      Random random = new Random(0);

      for (int i = 0; i < 10; i++) {
        RandomnessQuery query = new RandomnessQuery();
        boolean usePreGen = random.nextBoolean();

        PRNG prng;
        long seed;

        if (usePreGen) {
          seed = preGenSeeds[random.nextInt(preGenSeeds.length)];
          prng = ImplementedPRNGs.getPRNG(preGenPRNGs[random.nextInt(preGenPRNGs.length)], seed);
        } else {
          seed = notGenSeeds[random.nextInt(notGenSeeds.length)];
          prng = ImplementedPRNGs.getPRNG(notGenPRNGs[random.nextInt(notGenPRNGs.length)], seed);
        }
        boolean useUnixTime = random.nextBoolean();

        RandomnessQueryInner inner = new RandomnessQueryInner();
        inner.setType(TypeEnum.TLS_SERVER_RANDOM);
        inner.setRandomness(new ArrayList<>());
        for (int j = 0; j < 25; j++) {
          byte[] serverRandom = new byte[32];
          int useHRR = random.nextInt(20);
          int useFallback = random.nextInt(100);

          prng.nextBytes(serverRandom);

          if (useUnixTime) { // 50 percent
            setUnixTimestamp(serverRandom);
          }

          // 5 percent
          if (useFallback < 5) {
            setFallback(serverRandom, true);
          } else if (useFallback < 10) { // 5 percent
            setFallback(serverRandom, false);
          }

          // 5 percent
          if (useHRR == 0) {
            setHRR(serverRandom);
          }

          inner.addRandomnessItem(serverRandom);
        }
        query.add(inner);

        System.out.println("sending " + query);

        // send query
        long ping = System.nanoTime();
        Result result = sendRequest(query);
        long pong = System.nanoTime();

        if (result != null) {
          long time_ns = (pong-ping);

          boolean false_positive;
          boolean false_negative;
          boolean positive;
          boolean negative;
          boolean manual;
          String actual_prng = null;
          String actual_seed = null;
          if (result.getPairs().size() == 1) {
            ResultPairs pair = result.getPairs().get(0);
            actual_prng = pair.getPrng();
            actual_seed = pair.getSeeds().stream().map(Longs::fromByteArray).map(Objects::toString).collect(Collectors.joining("_"));

            false_positive = ! (prng.getPRNG().toString().equals(actual_prng) && pair.getSeeds().stream().anyMatch(s -> Longs.fromByteArray(s) == seed));
            false_negative = false;
            positive = prng.getPRNG().toString().equals(actual_prng) && pair.getSeeds().stream().anyMatch(s -> Longs.fromByteArray(s) == seed);
            negative = false;
            manual = false;

          } else if (result.getPairs().size() > 0) {
            System.out.println("found multiple for " + prng + ", " + seed);
            System.out.println("sent:");
            query.get(0).getRandomness().forEach(arr -> System.out.println(Arrays.toString(arr)));
            System.out.println("received:");
            for (ResultPairs pair : result.getPairs()) {
              System.out.println(pair.getPrng());
              pair.getSeeds().forEach(arr -> System.out.println(Arrays.toString(arr)));
            }
            System.out.println("--");

            false_positive = false;
            false_negative = false;
            positive = false;
            negative = false;
            manual = true;
          } else {
            false_positive = false;
            false_negative = usePreGen;
            positive = false;
            negative = !usePreGen;
            manual = false;
          }

          if (false_positive) {
            System.out.println("found false positive for " + prng + ", " + seed);
            System.out.println("sent:");
            query.get(0).getRandomness().forEach(arr -> System.out.println(Arrays.toString(arr)));
            System.out.println("received:");
            for (ResultPairs pair : result.getPairs()) {
              System.out.println(pair.getPrng());
              pair.getSeeds().forEach(arr -> System.out.println(Arrays.toString(arr)));
            }
            System.out.println("--");
            manual = true;
          }

          //writer.println("time_ns,positive,false_positive,negative,false_negative,actual_prng,actual_seed,expected_prng,expected_seed,check_manually");
          writer.println(time_ns + "," + positive+ ","+ false_positive+","+ negative+","+ false_negative+","+ actual_prng+","+ actual_seed+","+ prng.getPRNG()+","+ seed+","+ manual);
          //writer.println((pong-ping) + "," + (result.getPairs() != null && !result.getPairs().isEmpty()) + "," + (result.getPairs() != null && !result.getPairs().isEmpty() ? result.getPairs().get(0).getPrng(): "-"));
        } else {
          writer.println("NO RESPONSE");
        }


      }

    }

  private Result sendRequest(RandomnessQuery query) {
    try {
      URL url = new URL(ANALYSIS_SERVER_IP);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);

      connection.setRequestMethod("POST");
      connection.setRequestProperty("content-type", "application/json");
      connection.setRequestProperty("Accept", "application/json");

      ObjectMapper objectMapper = new ObjectMapper();
      try (OutputStream out = connection.getOutputStream()) {

        byte[] body = objectMapper.writeValueAsString(query).getBytes(StandardCharsets.UTF_8);
        out.write(body);
      }
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

      connection.connect();

      String read = bufferedReader.readLine();
      Result result = objectMapper.readValue(read, Result.class);
      connection.disconnect();
      return result;
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  // TLS 1.3 specific message requesting to send a new ClientHello
  private final static byte[] HELLO_RETRY_REQUEST_CONST =
      ByteHexUtil.hexToByte("CF21AD74E59A6111BE1D8C021E65B891C2A211167ABB8C5E079E09E2C8A8339C");

  // TLS 1.3 to TLS 1.2 Downgrade prevention
  private final static byte[] TLS_1_3_TO_TLS_1_2_DOWNGRADE_CONST =
      ByteHexUtil.hexToByte("444F574E47524401");

  // TLS 1.3 to TLS 1.1 or lower Downgrade prevention
  private final static byte[] TLS_1_3_TO_TLS_1_1_DOWNGRADE_CONST =
      ByteHexUtil.hexToByte("444F574E47524400");

    private void setUnixTimestamp(byte[] random) {
      ByteBuffer.wrap(random).putInt((int) (System.currentTimeMillis() / 1000));
    }

    private void setFallback(byte[] random, boolean use13_12) {
      System.arraycopy(use13_12 ? TLS_1_3_TO_TLS_1_2_DOWNGRADE_CONST : TLS_1_3_TO_TLS_1_1_DOWNGRADE_CONST, 0, random, random.length - TLS_1_3_TO_TLS_1_2_DOWNGRADE_CONST.length, TLS_1_3_TO_TLS_1_2_DOWNGRADE_CONST.length);
    }

    private void setHRR(byte[] random) {
      System.arraycopy(HELLO_RETRY_REQUEST_CONST, 0, random, 0, HELLO_RETRY_REQUEST_CONST.length);
    }
}
