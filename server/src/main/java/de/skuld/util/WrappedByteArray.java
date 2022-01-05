package de.skuld.util;

import com.google.common.primitives.Ints;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Wrapper class to sort arrays and treat chunks of array elements as single elements for comparison
 * during sorting. This is done to reduce overhead of creating arrays/objects from these chunks
 *
 */
public class WrappedByteArray {

  private final byte[] dataArray;

  public int[] getIndexArray() {
    return indexArray;
  }

  private final int[] indexArray;
  private final int[] helper;
  private final int[] firstBytes;
  private final int chunkSize;
  private int currentChunkIndex;
  private final int compareSize;

  WrappedByteArray(byte[] array, int chunkSize, int currentChunkIndex, int compareSize) {
    this.dataArray = array;
    this.chunkSize = chunkSize;
    this.currentChunkIndex = currentChunkIndex;
    this.compareSize = compareSize;

    this.indexArray = IntStream.range(0, currentChunkIndex).toArray();
    this.firstBytes = fillFirstByteArray(currentChunkIndex);
    this.helper = new int[indexArray.length];
    ParallelMergeSort.setChunkSize(chunkSize);
    ParallelMergeSort.setCompareSize(compareSize);
  }

  // TODO only works if chunkSize and compareSize >= 4
  @SuppressWarnings("unchecked")
  private int[] fillFirstByteArray(int exclusiveEnd) {
    return IntStream.range(0, exclusiveEnd).map(x -> {
      //System.out.println(x);
      int firstBytes = Ints.fromBytes(this.dataArray[x * chunkSize], this.dataArray[x * chunkSize +1], this.dataArray[x * chunkSize +2], this.dataArray[x * chunkSize+3]);
      /*System.out.println(Arrays
          .toString(new byte[]{this.dataArray[x * chunkSize], this.dataArray[x * chunkSize + 1],
              this.dataArray[x * chunkSize + 2], this.dataArray[x * chunkSize + 3]}));
      System.out.println(firstBytes);*/
      return firstBytes;
    }).toArray();
  }

/*  public void add(byte[] element) {
    if (element.length != chunkSize) {
      throw new AssertionError("Chunksize and Input size must be equal!");
    }
    int offset = currentChunkIndex * chunkSize;
    System.arraycopy(element, 0, dataArray, offset, chunkSize);
    currentChunkIndex++;
  }*/

  public void sort() {
    System.out.println("#elements: " + currentChunkIndex);
    System.out.println(Runtime.getRuntime().availableProcessors() + " processors available");
    final ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() - 2);

    forkJoinPool.invoke(new ParallelMergeSort(dataArray, indexArray, helper, firstBytes, 0, currentChunkIndex-1));
  }

}
