package de.skuld.util;

import com.google.common.primitives.Ints;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Wrapper class to sort arrays and treat chunks of array elements as single elements for comparison
 * during sorting. This is done to reduce overhead of creating arrays/objects from these chunks
 *
 */
public class WrappedByteBuffers {

  private final ByteBuffer[] dataArray;
  public int[] getIndexArray() {
    return indexArray;
  }

  private int[] indexArray;
  private int[] helper;
  private int[] firstBytes;
  private final int chunkSize;
  private final int elementCount;
  private final int compareSize;
  private int elementsInFullMBB;

  public WrappedByteBuffers(ByteBuffer[] dataArray, int chunkSize, int elementCount,
      int compareSize) {
    this.dataArray = dataArray;
    this.chunkSize = chunkSize;
    this.elementCount = elementCount;
    this.compareSize = compareSize;
  }

  // TODO only works if chunkSize and compareSize >= 4
  private int[] fillFirstByteArray(int exclusiveEnd) {
    int[] firstByteArray = new int[exclusiveEnd];
    for (int i = 0; i < exclusiveEnd; i++) {
      firstByteArray[i] = getFirstBytesAsInt(i);
    }
    return firstByteArray;
  }

  /**
   * Gets the element described by index, i.e. the element with index 0 is the first element, aka first 32 bytes of the first mbb.
   * The element with index 1 is the second element, aka [32..63] of the first mbb
   *
   * This method calculates the target mbb
   *
   * @param index
   * @return
   */
  public byte[] get(int index) {
    byte[] result = new byte[chunkSize];

    dataArray[getIndexOfMBB(index)].position(getOffsetInMBB(index) * chunkSize).get(result);

    return result;
  }

  public ByteBuffer getBuffer(int index) {
    return dataArray[index];
  }

  public int getOffsetInMBB(int index) {
    return index % elementsInFullMBB;
  }

  public int getIndexOfMBB(int index) {
    return index / elementsInFullMBB;
  }

  private int getFirstBytesAsInt(int index) {
    int mbbIndex = getIndexOfMBB(index);
    int offset = getOffsetInMBB(index);

    return this.dataArray[mbbIndex].getInt(offset * chunkSize);
  }

  public int compare(int x, int y) {
    int firstBytesX = firstBytes[x];
    int firstBytesY = firstBytes[y];
    int comparison = Integer.compareUnsigned(firstBytesX, firstBytesY);

    if (comparison != 0) {
      return comparison;
    }

    int mbbIndexX = getIndexOfMBB(x);
    int offsetX = getOffsetInMBB(x);

    int mbbIndexY = getIndexOfMBB(y);
    int offsetY = getOffsetInMBB(y);

    for (int i = 4; i < compareSize;) {
      if (compareSize - i < Long.BYTES) {
        byte xVal = dataArray[mbbIndexX].get(offsetX * chunkSize + i);
        byte yVal = dataArray[mbbIndexY].get(offsetY * chunkSize + i);

        comparison = Byte.compareUnsigned(xVal, yVal);
        i++;
      } else {
        long xVal = dataArray[mbbIndexX].getLong(offsetX * chunkSize + i);
        long yVal = dataArray[mbbIndexY].getLong(offsetY * chunkSize + i);

        comparison = Long.compareUnsigned(xVal, yVal);
        i+= Long.BYTES;
      }
      if (comparison != 0) {
        return comparison;
      }
    }
    return 0;
  }

  public void sort() {
    if (indexArray == null) {
      elementsInFullMBB = dataArray[0].limit() / chunkSize;
      this.indexArray = IntStream.range(0, elementCount).toArray();
      this.firstBytes = fillFirstByteArray(elementCount);
      this.helper = new int[indexArray.length];
    }

    final ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() - 2);
    forkJoinPool.invoke(new ParallelMergeSort(this, indexArray, helper, firstBytes, 0, elementCount-1));
  }

  public int size() {
    return elementCount;
  }
}
