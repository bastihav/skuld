package de.skuld.radix.manager;

import de.skuld.radix.RadixTrie;
import de.skuld.radix.RadixTrieStatus;
import de.skuld.util.ConfigurationHelper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class RadixUpdaterThread<T extends RadixTrie<?,?,?,?,?>> extends Thread {
  private final RadixManager<T> radixManager;

  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean running) {
    this.running = running;
  }

  private volatile boolean running = false;

  public RadixUpdaterThread(RadixManager<T> radixManager) {
    this.setDaemon(true);
    this.radixManager = radixManager;
  }

  @Override
  public void run() {
    this.running = true;

    int unixTime = ConfigurationHelper.getConfig().getInt("radix.prng.unix");

    while (running) {
      Map<UUID, T> tries = radixManager.getTries();

      ArrayList<T> sortedTries = tries.values().stream().sorted(
          Comparator.comparing(t -> t.getMetaData().getDate())).collect(Collectors.toCollection(
          ArrayList::new));

      for (int i = 0; i < sortedTries.size(); i++) {
        T trie = sortedTries.get(i);

        if (trie.getMetaData().getStatus() != RadixTrieStatus.FINISHED) {
          continue;
        }

        Date d = trie.getMetaData().getDate();
        // convert to unix time (seconds)
        long earliestSeed = d.getTime() / 1000L - unixTime;
        long latestSeed = d.getTime() / 1000L;

        if (latestSeed <= Instant.now().getEpochSecond()) {
          // no fresh data in this trie, delete it!
          radixManager.deleteRadixTrie(trie.getMetaData().getId());
        }

        if (earliestSeed <= Instant.now().getEpochSecond()) {
          // TODO maybe we want multiple tries
          // only the last trie might be a target for updating
          if (i != sortedTries.size() - 1) {
            continue;
          }

          UUID newUuid = radixManager.createNewDiskBasedRadixTrie(new Date(d.getTime() + unixTime*1000L));

          Thread creatorThread = new Thread(() -> {
            radixManager.generateTrie(newUuid);
          });
          creatorThread.start();
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }

    }
  }
}
