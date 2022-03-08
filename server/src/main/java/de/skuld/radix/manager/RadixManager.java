package de.skuld.radix.manager;

import com.github.f4b6a3.uuid.UuidCreator;
import de.skuld.radix.RadixTrie;
import de.skuld.radix.disk.DiskBasedRadixTrie;
import de.skuld.util.ConfigurationHelper;
import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Generic radix trie interface
 * <p>
 * Example: Leaves store meta-information on randomness, i.e. seed and algorithm Their position is
 * based on the randomness, which is a byte array
 */
public class RadixManager<R extends RadixTrie<?, ?, ?, ?, ?>> {

  private static final Logger LOGGER = LogManager.getLogger();
  private static final Map<Path, RadixManager<?>> instances = new HashMap<>();
  private final Map<UUID, R> radixTries = new HashMap<>();
  private final Path rootPath;
  private RadixUpdaterThread<R> radixUpdaterThread;

  private RadixManager(Path rootPath) {
    this.rootPath = rootPath;
    addAllTries();
  }

  public static <T extends RadixTrie<?, ?, ?, ?, ?>> RadixManager<T> getInstance(Path rootPath) {
    instances.computeIfAbsent(rootPath, RadixManager::new);

    //noinspection unchecked
    return (RadixManager<T>) instances.get(rootPath);
  }

  public void addRadixTrie(R trie) {
    LOGGER.info("Added tree " + trie);
    this.radixTries.put(trie.getMetaData().getId(), trie);
  }

  public void deleteRadixTrie(UUID uuid) {
    R trie = this.radixTries.remove(uuid);
    if (trie == null) {
      return;
    }
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

    if (directories != null) {
      for (String dirName : directories) {
        if (dirName.startsWith("trie-")) {
          DiskBasedRadixTrie trie;
          try {
            trie = new DiskBasedRadixTrie(rootPath.resolve(dirName));
            //noinspection unchecked
            this.addRadixTrie((R) trie);
          } catch (AssertionError error) {
            LOGGER.error("Could not instantiate trie: " + error);

            try {
              boolean isWindows = System.getProperty("os.name")
                  .toLowerCase().startsWith("windows");

              ProcessBuilder builder = new ProcessBuilder();
              if (isWindows) {
                builder.command("cmd.exe", "/c", "rmdir", "/s", "/q",
                    "\"" + rootPath.resolve(dirName).toString() + "\"");
              } else {
                builder.command("sh", "-c", "rm -rf " + rootPath.resolve(dirName).toString());
              }
              builder.directory(new File(System.getProperty("user.home")));
              builder.redirectOutput(Redirect.DISCARD);
              builder.redirectError(Redirect.DISCARD);
              Process process = builder.start();
              LOGGER.info("Deleting trie " + rootPath.resolve(dirName));
            } catch (IOException e) {
              e.printStackTrace();
            }

          }
        }
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

    if (!radixUpdaterThread.isRunning()) {
      this.radixUpdaterThread.start();
    }
  }

  public void stopUpdaterThread() {
    if (this.radixUpdaterThread != null) {
      this.radixUpdaterThread.setRunning(false);
    }
  }

  public boolean isUpdaterThreadRunning() {
    if (this.radixUpdaterThread == null) {
      return false;
    }
    return this.radixUpdaterThread.isRunning();
  }

  public UUID createNewDiskBasedRadixTrie(Date date) {
    UUID uuid = UuidCreator.getTimeBasedWithRandom();
    Path triePath = rootPath.resolve("trie-" + uuid + rootPath.getFileSystem().getSeparator());

    while (triePath.toFile().exists()) {
      uuid = UuidCreator.getTimeBasedWithRandom();
      triePath = rootPath.resolve("trie-" + uuid + rootPath.getFileSystem().getSeparator());
    }

    DiskBasedRadixTrie trie = new DiskBasedRadixTrie(triePath, date, uuid);

    LOGGER.info("Added trie " + trie);

    //noinspection unchecked
    this.radixTries.put(uuid, (R) trie);
    return uuid;
  }

  /**
   * Returns the trie that is relevant to searching at this moment
   *
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

    LOGGER.error("There is no trie for the current time!");
    return getTries().values().stream().findFirst().orElse(null);

  }
}
