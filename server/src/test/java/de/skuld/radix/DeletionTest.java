package de.skuld.radix;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

public class DeletionTest {

  Path p0 = Paths.get("G:\\skuld\\trie-078cb4e6-8527-11ec-8b06-55642910297d");
  Path p1 = Paths.get("G:\\skuld\\trie-078cb4e6-8527-11ec-8b06-55642910297d - Kopie");


  @Test
  public void test() {
    try {
      long ping = System.nanoTime();
      //deleteUsingSys();
      long pong = System.nanoTime();
      System.out.println(pong - ping + " ns");
      ping = System.nanoTime();
      FileUtils.deleteDirectory(p1.toFile());
      pong = System.nanoTime();
      System.out.println("java: " + (pong - ping) + " ns");
    } catch (IOException e) {
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
