package de.skuld.radix;

import de.skuld.prng.ImplementedPRNGs;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import de.skuld.radix.disk.DiskBasedRadixTrie;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import org.junit.jupiter.api.Test;

public class DiskBasedRadixTrieTest {
  @Test
  public void test() throws IOException {
    Path tempDir = Files.createTempDirectory("skuld");

    DiskBasedRadixTrie trie = new DiskBasedRadixTrie(tempDir);

    //trie.createEdge(new String[]{"test"}, trie.getRoot());

    byte[] randomness = new byte[32];
    for (int i = 0; i < 32; i++) {
      randomness[i] = 0;
    }

    RandomnessRadixTrieData data = new RandomnessRadixTrieData(new RandomnessRadixTrieDataPoint(
        ImplementedPRNGs.JAVA_RANDOM, 0,
        42));

    trie.add(data, randomness);
  }
}
