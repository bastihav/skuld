package de.skuld.radix.disk;

import com.google.common.primitives.UnsignedBytes;
import de.skuld.radix.AbstractRadixTrieData;
import de.skuld.radix.data.RandomnessRadixTrieData;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import de.skuld.util.BytePrinter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class DiskBasedRandomnessRadixTrieData extends RandomnessRadixTrieData {

  private final Path p;
  private MappedByteBuffer mappedByteBuffer;
  private long readSizeInBytes;
  private long writeSizeInBytes;
  private boolean resolved = false;
  private DiskBasedRadixTrie trie;

  public DiskBasedRandomnessRadixTrieData(RandomnessRadixTrieDataPoint data, DiskBasedRadixTrie trie) {
    super(data);
    this.p = null;
    this.resolved = true;
    this.trie = trie;
  }

  public DiskBasedRandomnessRadixTrieData(Path p) {
    super(null);
    if (!p.toFile().exists()) {
      throw new AssertionError("File does not exist");
    } else {
      this.p = p;
    }
  }

  @Override
  public DiskBasedRandomnessRadixTrieData mergeData(AbstractRadixTrieData<byte[], RandomnessRadixTrieDataPoint> other) {
    //System.out.println("merging into " + this.hashCode());
    //System.out.println("other: " + other.hashCode());

    //System.out.println("merging data on disk");

    Collection<RandomnessRadixTrieDataPoint> otherDp = other.getDataPoints();

    long amountOfNewData = otherDp.size() * 38L;

    Stream<RandomnessRadixTrieDataPoint> dpSorted = Stream.concat(this.getDataPoints().stream(), otherDp.stream()).sorted();


    try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(p, EnumSet.of(
        StandardOpenOption.READ, StandardOpenOption.WRITE))) {
      readSizeInBytes = fileChannel.size();
      writeSizeInBytes = readSizeInBytes + amountOfNewData;
      System.out.println(writeSizeInBytes + " vs " + Integer.MAX_VALUE);
      mappedByteBuffer = fileChannel.map(MapMode.READ_WRITE, 0, writeSizeInBytes);
      System.out.println("didnt fail");
    } catch (IOException e) {
      e.printStackTrace();
    }
    //long amountOfDataPoints = readSizeInBytes / 38;


    dpSorted.forEach(dp -> {
      mappedByteBuffer.put(dp.serialize());
    });



/*    for (RandomnessRadixTrieDataPoint dp : other.getDataPoints()) {
      //System.out.println("dp.getRemainingBytes: " + Arrays.toString(dp.getRemainingBytes()));
      long index = binarySearch(0, Math.max(0,amountOfDataPoints-1), dp.getRemainingBytes());

      // move old data
      long toShiftAmount = amountOfDataPoints - index;
      //System.out.println("amountOfDataPoints: " + amountOfDataPoints);
      //System.out.println("index: " + index);
      // TODO config

      if (toShiftAmount > 0 ) {
        //System.out.println("new size: " + sizeInBytes);
        //System.out.println("old data would be " + toShiftAmount * 38);
        byte[] oldData = new byte[(int) (toShiftAmount * 38)];

        //System.out.println("offset is " + index * 38 + ", length will be " + oldData.length);
        mappedByteBuffer.position((int) index * 38);
        mappedByteBuffer.get(oldData, 0, oldData.length);
        //System.out.println("putting the old data at " + (index+1) * 38);
        mappedByteBuffer.position((int) (index+1) * 38);
        mappedByteBuffer.put(oldData);
      }

      // insert new data
      //System.out.println("putting the new data at " + (index) * 38);
      mappedByteBuffer.position((int) index * 38);
      mappedByteBuffer.put(dp.serialize(trie));
      amountOfDataPoints++;
    }*/

    dataPoints.addAll(otherDp);

    //System.out.println("here comes the array");
    //byte[] array  = new byte[(int) sizeInBytes];
    //mappedByteBuffer.position(0);
    //mappedByteBuffer.get(array);
    //System.out.println(Arrays.toString(array));

    return this;
  }

  public Collection<RandomnessRadixTrieDataPoint> getDataPointsRaw() {
    return dataPoints;
  }

  public DiskBasedRandomnessRadixTrieData mergeDataRaw(DiskBasedRandomnessRadixTrieData other) {
    this.dataPoints.addAll(other.getDataPointsRaw());
    return this;
  }

  @Override
  public Collection<RandomnessRadixTrieDataPoint> getDataPoints() {
    //System.out.println("calling get DataPoints on " + this.hashCode());
    //System.out.println("size in bytes for this: " + sizeInBytes);

    if (!resolved) {
      try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(p, EnumSet.of(
          StandardOpenOption.READ))) {
        mappedByteBuffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size());
        readSizeInBytes = fileChannel.size();
        long amountOfDataPoints = readSizeInBytes / 38;
        //System.out.println("setting size in bytes for " + this.hashCode() + " to " + fileChannel.size());
        //System.out.println("now reading for " + amountOfDataPoints + " points of data");
        for (int i = 0; i < amountOfDataPoints; i++) {
          byte[] array = new byte[38];
          mappedByteBuffer.position(i * 38);
          mappedByteBuffer.get(array, 0, 38);
          //System.out.println("read data from disk: " + Arrays.toString(array));
          RandomnessRadixTrieDataPoint dataPoint = new RandomnessRadixTrieDataPoint(array);
          dataPoints.add(dataPoint);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    resolved = true;
    //System.out.println("returning data points: " + this.dataPoints);
    return dataPoints;
  }

  public Optional<RandomnessRadixTrieDataPoint> getDataPoint(byte[] query) {
    // TODO config length of remaining bytes
    byte[] leastSignificantBytes = Arrays.copyOfRange(query, query.length - 29, query.length);

    if (resolved) {
      return dataPoints.stream().filter(dp -> Arrays.equals(dp.getRemainingIndexingData(), leastSignificantBytes)).findFirst();
    }
    // TODO config length on file system

    try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(p, EnumSet.of(
        StandardOpenOption.READ))) {
      mappedByteBuffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size());
      readSizeInBytes = fileChannel.size();
      long amountOfDataPoints = readSizeInBytes / 38;
      long position = binarySearch(0, amountOfDataPoints-1, query);
      byte[] array = new byte[38];
      /*System.out.println("size: " + readSizeInBytes);
      System.out.println("pos: " + position);
      System.out.println("in byte: " + (int) position*38);*/
      mappedByteBuffer.position((int) (position * 38));
      mappedByteBuffer.get(array, 0, 38);

      RandomnessRadixTrieDataPoint middleDataPoint = new RandomnessRadixTrieDataPoint(array);

      if (UnsignedBytes.lexicographicalComparator().compare(leastSignificantBytes,
          middleDataPoint.getRemainingIndexingData()) == 0) {
        return Optional.of(middleDataPoint);
      } else {
        return Optional.empty();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  public long binarySearch(long left, long right, byte[] query) {
    byte[] leastSignificantBytes = Arrays.copyOfRange(query, query.length - 29, query.length);
    //System.out.println("query: " + BytePrinter.bytesToHex(query));
    //System.out.println("least sig: " + BytePrinter.bytesToHex(leastSignificantBytes));
    while (left <= right) {
      long mid = left + (right - left) / 2;
      //System.out.println(left + " " + right + " " + mid +" " + Arrays.toString(query));

      //try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(p, EnumSet.of(
         // StandardOpenOption.READ))) {
        //mappedByteBuffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size());
        byte[] array = new byte[38];
        //readSizeInBytes = fileChannel.size();


        // TODO config
        mappedByteBuffer.position((int) (mid * 38));
        mappedByteBuffer.get(array, 0, 38);

        RandomnessRadixTrieDataPoint middleDataPoint = new RandomnessRadixTrieDataPoint(array);

        /*System.out.println("query is: " + Arrays.toString(query));
        System.out.println(query.length + " vs " + middleDataPoint.getRemainingBytes().length);*/


      //System.out.println("comparing to " + BytePrinter.bytesToHex(middleDataPoint.getRemainingIndexingData()));
        int comparison = UnsignedBytes.lexicographicalComparator().compare(leastSignificantBytes,
            middleDataPoint.getRemainingIndexingData());
        if (comparison < 0) {
          //System.out.println("left");
          right =  mid - 1;
        } else if (comparison == 0) {
          //System.out.println("returning mid");
          return mid;
        } else {
          //System.out.println("right");
          left = mid + 1;
        }

      //} catch (IOException e) {
      //  e.printStackTrace();
      //}
      //mappedByteBuffer = null;
    }

    //System.out.println("returning left");

    return left;
  }


}
