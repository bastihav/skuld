package de.skuld.radix;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DeletionTest {

  Path p0 = Paths.get("G:\\skuld\\trie-8801b746-9e13-11ec-bb02-7d3cdd5ffd42");
  Path p1 = Paths.get("G:\\skuld\\trie-8801b746-9e13-11ec-bb02-7d3cdd5ffd42 - Kopie");


  @Test
  @Disabled
  public void test() {
    try {
      long ping = System.nanoTime();
      deleteUsingSys();
      long pong = System.nanoTime();
      System.out.println(pong - ping + " ns");
      ping = System.nanoTime();
      ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4,16, 20, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
      for (int i = 0; i < 256; i++) {
        int finalI = i;
        threadPoolExecutor.execute(() -> {
          try {
            FileUtils.deleteDirectory(p1.resolve("" + (byte) finalI).toFile());
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
      }
      threadPoolExecutor.shutdown();
      threadPoolExecutor.awaitTermination(1, TimeUnit.HOURS);
      FileUtils.deleteDirectory(p1.toFile());
      pong = System.nanoTime();
      System.out.println("java: " + (pong - ping) + " ns");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }

  private void deleteUsingSys() throws IOException, InterruptedException {
    boolean isWindows = System.getProperty("os.name")
        .toLowerCase().startsWith("windows");

    ProcessBuilder builder = new ProcessBuilder();
    if (isWindows) {
      builder.command("cmd.exe", "/c", "rmdir", "/s", "/q", "\""+p0.toString()+"\"");
      System.out.println(builder.command());
    } else {
      builder.command("sh", "-c", "rm", "-rf", "\""+p0.toString()+"\"");
    }
    builder.directory(new File(System.getProperty("user.home")));
    builder.inheritIO();
    Process process = builder.start();

    int exitCode = process.waitFor();
    assert exitCode == 0;
  }
}
