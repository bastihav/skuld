package de.skuld.util;

import de.skuld.prng.JavaRandom;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import de.skuld.radix.disk.DiskBasedRandomnessRadixTrieData;
import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class WrappedArrayTest {

  @Test
  public void test() {

    File file = Paths.get("G:\\skuld\\caches\\0.bin").toFile();

    int sizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized");
    int remainingOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized.remaining");
    final int maxPartitionBytesInArray = Integer.MAX_VALUE - (Integer.MAX_VALUE % sizeOnDisk);

    byte[] array = new byte[maxPartitionBytesInArray];

    try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(file.toPath(), EnumSet.of(
        StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
      int reads = (int) Math.ceil(((double) fileChannel.size() / maxPartitionBytesInArray));

      System.out.println("reading x" + reads);
      for (int i = 0; i < reads; i++) {
        long offset = (long) i * maxPartitionBytesInArray;
        long remaining = Math.min(fileChannel.size() - offset, maxPartitionBytesInArray);

        long ping = System.nanoTime();
        System.out.println("reading " + remaining + " bytes");
        MappedByteBuffer mappedByteBuffer = fileChannel.map(MapMode.READ_ONLY, offset, remaining);
        mappedByteBuffer.load();
        mappedByteBuffer.get(array);
        long pong = System.nanoTime();
        System.out.println("read #" + i + " in " + (pong-ping) + " ns");

        ping = System.nanoTime();
        WrappedByteArray wrappedArray = new WrappedByteArray(array, sizeOnDisk, (int) (remaining / sizeOnDisk), remainingOnDisk);

        wrappedArray.sort();
        pong = System.nanoTime();
        System.out.println("sorted #" + i + " in " + (pong-ping) + " ns");
        return;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void shortTest() {
    int chunkSize = 4;
    int compareSize = 2;
    int amount = 32;

    JavaRandom random = new JavaRandom(0);
    byte[] array = random.getRandomBytes(chunkSize * amount);

    WrappedByteArray wrappedByteArray = new WrappedByteArray(array, chunkSize, amount, compareSize);

    System.out.println(Arrays.toString(array));

    wrappedByteArray.sort();

    System.out.println(Arrays.toString(array));
  }

}
