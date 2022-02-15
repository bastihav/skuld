package de.skuld.util;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Random;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class WrappedArrayTest {

  @Test
  @Disabled
  public void test() {

    File file = Paths.get("G:\\skuld\\caches\\0.bin").toFile();

    int sizeOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized");
    int remainingOnDisk = ConfigurationHelper.getConfig().getInt("radix.disk_based.hardware_cache.serialized.remaining");
    final int maxPartitionBytesInArray = Integer.MAX_VALUE - (Integer.MAX_VALUE % sizeOnDisk);

    // TODO array might be bigger than necessary!
    //byte[] array = new byte[maxPartitionBytesInArray];
    int elementCount = 0;

    try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(file.toPath(), EnumSet.of(
        StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
      int reads = (int) Math.ceil(((double) fileChannel.size() / maxPartitionBytesInArray));

      MappedByteBuffer[] buffers =  new MappedByteBuffer[reads];
      System.out.println("reading x" + reads);
      for (int i = 0; i < reads; i++) {
        long offset = (long) i * maxPartitionBytesInArray;
        long remaining = Math.min(fileChannel.size() - offset, maxPartitionBytesInArray);

        long ping = System.nanoTime();
        System.out.println("reading " + remaining + " bytes");
        MappedByteBuffer mappedByteBuffer = fileChannel.map(MapMode.READ_ONLY, offset, remaining);
        elementCount += (int) (remaining / sizeOnDisk);
        mappedByteBuffer.load();
        //mappedByteBuffer.get(array);
        buffers[i] = mappedByteBuffer;
        long pong = System.nanoTime();
        System.out.println("read #" + i + " (#" + ((int)remaining / sizeOnDisk) + " elements) in " + (pong-ping) + " ns");


        //break;
        //return;
      }
      long ping = System.nanoTime();
      WrappedByteBuffers wrappedArray = new WrappedByteBuffers(buffers, sizeOnDisk, elementCount, remainingOnDisk);

      wrappedArray.sort();
      long pong = System.nanoTime();
      System.out.println("sorted #" + elementCount + " elements in " + (pong-ping) + " ns");

      System.out.println("validity:");
      for (int i = 0; i < 10; i++) {
        ByteHexUtil.printBytesAsHex(wrappedArray.get(wrappedArray.getIndexArray()[i]));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  @Disabled
  public void shortTest() {
    int chunkSize = 8;
    int compareSize = 6;
    int amount = 4;
    int mbbAmount = 4;

    ByteBuffer[] buffers = new ByteBuffer[mbbAmount];


    int nextInt = mbbAmount * amount;
    System.out.println("raw: ");
    Random r = new Random(0);
    for (int i = 0; i < mbbAmount; i++) {
      byte[] inArray = new byte[chunkSize * amount];
      buffers[i] = ByteBuffer.wrap(inArray);
      for (int j = 0; j < amount; j++) {
        //buffers[i].putInt(nextInt--);
        buffers[i].putInt(0);
        buffers[i].putInt(r.nextInt());
      }
      ByteHexUtil.printBytesAsHex(inArray);
    }
      WrappedByteBuffers wrappedByteArray = new WrappedByteBuffers(buffers, chunkSize, mbbAmount * amount, compareSize);

      wrappedByteArray.sort();

      System.out.println("indices: ");
      System.out.println(Arrays.toString(wrappedByteArray.getIndexArray()));

      System.out.println("array raw:");
      for (int i = 0; i < amount*mbbAmount; i++) {
        ByteHexUtil.printBytesAsHex(wrappedByteArray.get(i));
      }
    System.out.println("array sorted");
    for (int i = 0; i < amount*mbbAmount; i++) {
      ByteHexUtil.printBytesAsHex(wrappedByteArray.get(wrappedByteArray.getIndexArray()[i]));
    }
  }

}
