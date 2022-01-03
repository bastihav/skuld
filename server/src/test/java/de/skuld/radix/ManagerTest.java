package de.skuld.radix;

import de.skuld.radix.disk.DiskBasedRadixTrie;
import de.skuld.radix.manager.RadixManager;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import org.junit.jupiter.api.Test;

public class ManagerTest {

  @Test
  public void test() {
    Path tempDir = Paths.get("G:\\skuld\\");
    DiskBasedRadixTrie trie = new DiskBasedRadixTrie(tempDir, new Date(0));

    RadixManager radixManager = new RadixManager();
    radixManager.addRadixTrie(trie);

    radixManager.generateTrie(trie);
  }

}
