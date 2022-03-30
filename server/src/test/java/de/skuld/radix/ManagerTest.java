package de.skuld.radix;

import de.skuld.prng.ImplementedPRNGs;
import de.skuld.prng.JavaRandom;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import de.skuld.radix.disk.DiskBasedRadixTrie;
import de.skuld.radix.manager.RadixManager;
import de.skuld.util.ByteHexUtil;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class ManagerTest {

  @Test
  public void test() {
    RadixManager<DiskBasedRadixTrie> radixManager = RadixManager.getInstance(Paths.get("G:\\skuld\\"));
    UUID uuid = radixManager.createNewDiskBasedRadixTrie();
    //radixManager.addAllTries();
    radixManager.generateTrie(uuid);
    //radixManager.startUpdaterThread();
    try {
      Thread.sleep(180*1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.exit(0);
  }

  @Test
  @Disabled
  public void debugReadTest() {
    RadixManager<DiskBasedRadixTrie> radixManager = RadixManager.getInstance(Paths.get("G:\\skuld\\"));
    byte[] serverRandom = ByteHexUtil.hexToByte("F90ADBE743145A68C861B18C8CC482FB3AB2D7FF074221DB");
    System.out.println(radixManager.getTrie().search(serverRandom));
  }

  @Test
  @Disabled
  public void readPerformanceTestSearchAlgorithm() throws IOException {
    RadixManager<DiskBasedRadixTrie> radixManager = RadixManager.getInstance(Paths.get("G:\\skuld\\"));
    UUID uuid = radixManager.createNewDiskBasedRadixTrie();
    //radixManager.addAllTries();
    radixManager.generateTrie(uuid);

    //UUID uuid = UUID.randomUUID();
    DiskBasedRadixTrie trie = radixManager.getTries().get(uuid);

    long seed = trie.getSeedMap().get(0);

    //RandomnessRadixTrieDataPoint dp = new RandomnessRadixTrieDataPoint(bytes, random.getPRNG(), 0, 32*20);
    //trie.add(new DiskBasedRandomnessRadixTrieData(dp, trie), bytes);
    //trie.flushMemoryCache();

    for(int i = 0; i < 32 - 5; i++) {
      JavaRandom random = new JavaRandom(0);
      byte[] bytes = random.getBytes(32*10 - i,32);
      //System.out.println("searching for: " + Arrays.toString(bytes));

      Optional<RandomnessRadixTrieDataPoint> dataPoint = trie.search(bytes);

      Assertions.assertTrue(dataPoint.isPresent());

      Assertions.assertEquals(dataPoint.get().getRng(), ImplementedPRNGs.JAVA_RANDOM);
      Assertions.assertEquals(dataPoint.get().getSeedIndex(), 0);
      Assertions.assertEquals(dataPoint.get().getByteIndexInRandomness(), 32*10);
    }
  }
}
