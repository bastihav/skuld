package de.skuld.radix;

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

    trie.createEdge(new String[]{"test"}, trie.getRoot());
  }
}
