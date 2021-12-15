package de.skuld.radix;

import com.google.common.primitives.Ints;
import de.skuld.prng.ImplementedPRNGs;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import de.skuld.radix.disk.DiskBasedRadixTrie;
import de.skuld.radix.disk.DiskBasedRadixTrieNode;
import de.skuld.radix.disk.DiskBasedRandomnessRadixTrieData;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


public class DiskBasedRadixTrieTest {

  @Test
  @Disabled
  public void test() throws IOException {
    Path tempDir = Paths.get("G:\\skuld\\");
    //Path tempDir = Files.createTempDirectory("skuld");

    DiskBasedRadixTrie trie = new DiskBasedRadixTrie(tempDir, null);

    //trie.createEdge(new String[]{"test"}, trie.getRoot());

    byte[] randomness = new byte[32];
    for (int i = 0; i < 32; i++) {
      randomness[i] = 0;
    }

    DiskBasedRandomnessRadixTrieData data = new DiskBasedRandomnessRadixTrieData(
        new RandomnessRadixTrieDataPoint(
            randomness, ImplementedPRNGs.JAVA_RANDOM, 0,
            42), trie);

    byte[] randomness2 = new byte[32];
    for (int i = 0; i < 32; i++) {
      randomness2[i] = 1;
    }
    randomness2[0] = 0;

    DiskBasedRandomnessRadixTrieData data2 = new DiskBasedRandomnessRadixTrieData(
        new RandomnessRadixTrieDataPoint(
            randomness2, ImplementedPRNGs.JAVA_RANDOM, 0,
            42), trie);

    byte[] randomness3 = new byte[32];
    for (int i = 0; i < 32; i++) {
      randomness3[i] = 2;
    }
    randomness3[0] = 0;
    randomness3[1] = 0;

    DiskBasedRandomnessRadixTrieData data3 = new DiskBasedRandomnessRadixTrieData(
        new RandomnessRadixTrieDataPoint(
            randomness3, ImplementedPRNGs.JAVA_RANDOM, 0,
            42), trie);

    byte[] randomness4 = new byte[32];
    for (int i = 0; i < 32; i++) {
      randomness4[i] = 3;
    }
    randomness4[0] = 0;
    randomness4[1] = 0;
    randomness4[2] = 0;

    DiskBasedRandomnessRadixTrieData data4 = new DiskBasedRandomnessRadixTrieData(
        new RandomnessRadixTrieDataPoint(
            randomness4, ImplementedPRNGs.JAVA_RANDOM, 0,
            42), trie);

    System.out.println("created data for test " + data.hashCode());

    /*trie.add(data, randomness);
    trie.add(data2, randomness2);
    trie.add(data3, randomness3);
    trie.add(data4, randomness4);*/

    System.out.println(trie.contains(randomness));
    System.out.println(trie.getNode(randomness));
    System.out.println(trie.getNode(randomness).get().getData());
    System.out.println(" -- ");
    System.out.println(trie.getNode(randomness).get().getData().getDataPoint(randomness));
  }

  @Test
  @Disabled
  public void testParentEdges() throws IOException {
    DiskBasedRadixTrieNode n = new DiskBasedRadixTrieNode(false, null, Paths
        .get("C:\\Users\\basti\\AppData\\Local\\Temp\\skuld6523388402481457665\\00\\00\\00\\00"),
        null);
    System.out.println(n.getEdgesFromRoot());
  }

  @Test
  @Disabled
  public void writePerformanceTest() throws IOException {
    Path tempDir = Paths.get("G:\\skuld\\");

    /*String date = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(new Date());
    File dataFile = tempDir.resolve("performance_write_" + date + ".csv").toFile();

    if (!dataFile.exists()) {
      dataFile.createNewFile();
    }*/

    int testSize = 312500;

    //long[] dataPoints = new long[testSize];
    long ping = System.nanoTime();
    DiskBasedRadixTrie trie = new DiskBasedRadixTrie(tempDir, null);
    Random random = new Random(0);

    for (int i = 0; i < testSize; i++) {
      byte[] randomness = new byte[32];
      random.nextBytes(randomness);

      DiskBasedRandomnessRadixTrieData data = new DiskBasedRandomnessRadixTrieData(
          new RandomnessRadixTrieDataPoint(randomness,
              ImplementedPRNGs.JAVA_RANDOM, 0,
              i * 32), trie);

      trie.add(data, randomness);

      if (i % 500 == 0) {
        System.out.println(i);
      }

    }
    trie.flushCache();
    long pong = System.nanoTime();

    System.out.println(pong - ping);

      /*try(PrintWriter printWriter = new PrintWriter(dataFile)) {
        for (int i = 0; i < testSize; i++) {
          printWriter.println(dataPoints[i] + ',');
        }
      }*/
  }

  @Test
  @Disabled
  public void writePerformanceSameNodeRandomTest() throws IOException {
    Path tempDir = Paths.get("G:\\skuld\\");

    /*String date = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(new Date());
    File dataFile = tempDir.resolve("performance_write_" + date + ".csv").toFile();

    if (!dataFile.exists()) {
      dataFile.createNewFile();
    }*/

    int testSize = 7000;

    //long[] dataPoints = new long[testSize];
    long ping = System.nanoTime();
    DiskBasedRadixTrie trie = new DiskBasedRadixTrie(tempDir, null);
    Random random = new Random(0);

    for (int i = 0; i < testSize; i++) {
      byte[] randomness = new byte[32];
      random.nextBytes(randomness);
      randomness[0] = 0;
      randomness[1] = 0;
      randomness[2] = 0;

      DiskBasedRandomnessRadixTrieData data = new DiskBasedRandomnessRadixTrieData(
          new RandomnessRadixTrieDataPoint(randomness,
              ImplementedPRNGs.JAVA_RANDOM, 0,
              i * 32), trie);

      trie.add(data, randomness);

      if (i % 500 == 0) {
        System.out.println(i);
      }

    }
    trie.flushCache();
    long pong = System.nanoTime();

    System.out.println(pong - ping);

      /*try(PrintWriter printWriter = new PrintWriter(dataFile)) {
        for (int i = 0; i < testSize; i++) {
          printWriter.println(dataPoints[i] + ',');
        }
      }*/
  }

  @Test
  @Disabled
  public void writePerformanceSameNodeSortedTest() throws IOException {
    Path tempDir = Paths.get("G:\\skuld\\");

    /*String date = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(new Date());
    File dataFile = tempDir.resolve("performance_write_" + date + ".csv").toFile();

    if (!dataFile.exists()) {
      dataFile.createNewFile();
    }*/

    int testSize = 4;
    //int testSize = 1700000;

    //long[] dataPoints = new long[testSize];
    long ping = System.nanoTime();
    DiskBasedRadixTrie trie = new DiskBasedRadixTrie(tempDir, null);
    Random random = new Random(0);

    byte[] randomness = new byte[32];
    random.nextBytes(randomness);

    for (int i = 0; i < testSize; i++) {
      byte[] number = Ints.toByteArray(i);
      randomness[31] = number[3];
      randomness[30] = number[2];
      randomness[29] = number[1];
      randomness[28] = number[0];
      //System.out.println("inserting " + Arrays.toString(randomness));

      DiskBasedRandomnessRadixTrieData data = new DiskBasedRandomnessRadixTrieData(
          new RandomnessRadixTrieDataPoint(randomness,
              ImplementedPRNGs.JAVA_RANDOM, 0,
              i * 32), trie);

      trie.add(data, Arrays.copyOf(randomness, randomness.length));

      if (i % 500 == 0) {
        System.out.println(i);
      }
    }
    trie.flushCache();
    long pong = System.nanoTime();

    System.out.println("write: " + (pong - ping));

    ping = System.nanoTime();

    byte[] number = Ints.toByteArray(2);
    randomness[31] = number[3];
    randomness[30] = number[2];
    randomness[29] = number[1];
    randomness[28] = number[0];
    Optional<DiskBasedRadixTrieNode> node = trie.getNode(randomness);
    if (node.isPresent()) {
      System.out.println("node found ");
      Optional<RandomnessRadixTrieDataPoint> dp = node.get().getData().getDataPoint(randomness);
      if (dp.isPresent()) {
        System.out.println("found dp");
      }
    }
    pong = System.nanoTime();

    System.out.println("read: " + (pong - ping));

      /*try(PrintWriter printWriter = new PrintWriter(dataFile)) {
        for (int i = 0; i < testSize; i++) {
          printWriter.println(dataPoints[i] + ',');
        }
      }*/
  }

  @Test
  @Disabled
  public void writeWorstCasePerformanceTest() throws IOException {
    Path tempDir = Paths.get("G:\\skuld\\");

    File dataFile = tempDir.resolve("performanceWriteWorst.csv").toFile();

    if (!dataFile.exists()) {
      dataFile.createNewFile();
    }

    int testSize = 6705;

    long[] dataPoints = new long[testSize];

    DiskBasedRadixTrie trie = new DiskBasedRadixTrie(tempDir, null);
    Random random = new Random(0);
    byte[] randomness = new byte[32];
    random.nextBytes(randomness);

    for (int i = 0; i < testSize; i++) {
      byte[] number = Ints.toByteArray(i);
      randomness[31] = number[3];
      randomness[30] = number[2];
      randomness[29] = number[1];
      randomness[28] = number[0];

      long ping = System.nanoTime();
      DiskBasedRandomnessRadixTrieData data = new DiskBasedRandomnessRadixTrieData(
          new RandomnessRadixTrieDataPoint(
              randomness, ImplementedPRNGs.JAVA_RANDOM, 0,
              i * 32), trie);

      trie.add(data, randomness);

      long pong = System.nanoTime();
      dataPoints[i] = pong - ping;
      if (i % 500 == 0) {
        System.out.println(i);
      }
    }

    try (PrintWriter printWriter = new PrintWriter(dataFile)) {
      for (int i = 0; i < testSize; i++) {
        printWriter.println(dataPoints[i]);
      }
    }
  }

  @Test
  @Disabled
  public void readPerformanceTest() throws IOException {
    Path tempDir = Paths.get("G:\\skuld\\");

    String date = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(new Date());
    File dataFile = tempDir.resolve("performanceRead_" + date + ".csv").toFile();

    if (!dataFile.exists()) {
      dataFile.createNewFile();
    }

    int testSize = 312500;

    long[] dataPoints = new long[testSize];
    boolean[] hasNode = new boolean[testSize];
    boolean[] hasDp = new boolean[testSize];

    DiskBasedRadixTrie trie = new DiskBasedRadixTrie(tempDir, null);
    Random random = new Random(new Date().getTime());

    for (int i = 0; i < testSize; i++) {
      byte[] randomness = new byte[32];
      random.nextBytes(randomness);

      long ping = System.nanoTime();

      Optional<DiskBasedRadixTrieNode> node = trie.getNode(randomness);

      if (node.isPresent()) {
        hasNode[i] = true;
        Optional<RandomnessRadixTrieDataPoint> dp = node.get().getData().getDataPoint(randomness);
        if (dp.isPresent()) {
          hasDp[i] = true;
        }
      }
      long pong = System.nanoTime();

      dataPoints[i] = pong - ping;
      if (i % 500 == 0) {
        System.out.println(i);
      }

    }

    try (PrintWriter printWriter = new PrintWriter(dataFile)) {
      printWriter.println("ReadTime, foundNode, foundDataPoint");
      for (int i = 0; i < testSize; i++) {
        printWriter.println("" + dataPoints[i] + ',' + hasNode[i] + "," + hasDp[i]);
      }
    }
  }

}
