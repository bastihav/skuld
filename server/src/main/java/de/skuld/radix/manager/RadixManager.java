package de.skuld.radix.manager;

import com.github.f4b6a3.uuid.UuidCreator;
import de.skuld.radix.AbstractRadixTrieData;
import de.skuld.radix.AbstractRadixTrieDataPoint;
import de.skuld.radix.AbstractRadixTrieEdge;
import de.skuld.radix.AbstractRadixTrieNode;
import de.skuld.radix.RadixTrie;
import de.skuld.radix.RadixTrieEdge;
import de.skuld.radix.RadixTrieNode;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.disk.DiskBasedRadixTrie;
import de.skuld.util.ConfigurationHelper;
import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Generic radix trie interface
 * <p>
 * Example: Leaves store meta-information on randomness, i.e. seed and algorithm Their position is
 * based on the randomness, which is a byte array
 *
 */
public class RadixManager<R extends RadixTrie<?,?,?,?,?>> {

  private final Map<UUID, R> radixTries = new HashMap<>();
  private final Path rootPath;
  private RadixUpdaterThread<R> radixUpdaterThread;

  public RadixManager(Path rootPath) {
    this.rootPath = rootPath;
    addAllTries();
  }

  public void addRadixTrie(R trie) {
    this.radixTries.put(trie.getMetaData().getId(), trie);
  }

  public void deleteRadixTrie(UUID uuid) {
    R trie = this.radixTries.remove(uuid);
    // TODO do this in another thread and retry on failure
    //noinspection SynchronizationOnLocalVariableOrMethodParameter
    synchronized (trie) {
      trie.delete();
    }
  }

  public void generateTrie(UUID uuid) {
    if (radixTries.get(uuid) != null) {
      generateTrie(radixTries.get(uuid));
    } else {
      throw new RuntimeException("Could not find trie");
    }
  }

  public Map<UUID, R> getTries() {
    return Collections.unmodifiableMap(this.radixTries);
  }

  public void generateTrie(R radixTrie) {
    radixTrie.generate();
  }

  public void addAllTries() {
    File file = rootPath.toFile();

    String[] directories = file.list((file1, s) -> new File(file1, s).isDirectory());

    assert directories != null;
    for (String dirName : directories) {
      if (dirName.startsWith("trie-")) {
        System.out.println(rootPath.resolve(dirName));
        DiskBasedRadixTrie trie = new DiskBasedRadixTrie(rootPath.resolve(dirName));
        //noinspection unchecked
        this.addRadixTrie((R) trie);
      }
    }
  }

  public UUID createNewDiskBasedRadixTrie() {
    return createNewDiskBasedRadixTrie(new Date());
  }

  public void startUpdaterThread() {
    if (this.radixUpdaterThread == null) {
      this.radixUpdaterThread = new RadixUpdaterThread<>(this);
    }

    this.radixUpdaterThread.start();
  }

  public void stopUpdaterThread() {
    if (this.radixUpdaterThread != null) {
      this.radixUpdaterThread.setRunning(false);
    }
  }

  public UUID createNewDiskBasedRadixTrie(Date date) {
    UUID uuid = UuidCreator.getTimeBasedWithRandom();
    Path triePath = rootPath.resolve("trie-" + uuid + rootPath.getFileSystem().getSeparator());

    while (triePath.toFile().exists()) {
      uuid = UuidCreator.getTimeBasedWithRandom();
      triePath = rootPath.resolve("trie-" + uuid + rootPath.getFileSystem().getSeparator());
    }

    DiskBasedRadixTrie trie = new DiskBasedRadixTrie(triePath, date, uuid);

    //noinspection unchecked
    this.radixTries.put(uuid, (R) trie);
    return uuid;
  }

  /**
   * Returns the trie that is relevant to searching at this moment
   * @return
   */
  public R getTrie() {

    int unixTime = ConfigurationHelper.getConfig().getInt("radix.prng.unix");
    long now = Instant.now().getEpochSecond();

    for (R trie : radixTries.values()) {
      Date d = trie.getMetaData().getDate();
      // convert to unix time (seconds)
      long earliestSeed = d.getTime() / 1000L - unixTime;
      long latestSeed = d.getTime() / 1000L;

      if (earliestSeed <= now && latestSeed >= now) {
        return trie;
      }
    }

    throw new RuntimeException("No current trie found");
  }
}
