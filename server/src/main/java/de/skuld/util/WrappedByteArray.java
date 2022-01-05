package de.skuld.util;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Wrapper class to sort arrays and treat chunks of array elements as single elements for comparison
 * during sorting. This is done to reduce overhead of creating arrays/objects from these chunks
 *
 */
public class WrappedByteArray {

  private final byte[] array;
  private final int chunkSize;
  private int currentChunkIndex;
  private final int compareSize;

  WrappedByteArray(byte[] array, int chunkSize, int currentChunkIndex, int compareSize) {
    this.array = array;
    this.chunkSize = chunkSize;
    this.currentChunkIndex = currentChunkIndex;
    this.compareSize = compareSize;
    ParallelMergeSort.setChunkSize(chunkSize);
    ParallelMergeSort.setCompareSize(compareSize);
  }

  public void add(byte[] element) {
    if (element.length != chunkSize) {
      throw new AssertionError("Chunksize and Input size must be equal!");
    }
    int offset = currentChunkIndex * chunkSize;
    System.arraycopy(element, 0, array, offset, chunkSize);
    currentChunkIndex++;
  }

  public void sort() {
    System.out.println("#elements: " + currentChunkIndex);
    System.out.println(Runtime.getRuntime().availableProcessors() + " processors available");
    final ForkJoinPool forkJoinPool = new ForkJoinPool(2);

    forkJoinPool.invoke(new ParallelMergeSort(array, 0, currentChunkIndex-1));
  }

}
