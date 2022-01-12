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
  private int elementsInLastMBB;

  public WrappedByteBuffers(ByteBuffer[] dataArray, int chunkSize, int elementCount,
      int compareSize) {
    this.dataArray = dataArray;
    this.chunkSize = chunkSize;
    this.elementCount = elementCount;
    this.compareSize = compareSize;

    ParallelMergeSort.setChunkSize(chunkSize);
    ParallelMergeSort.setCompareSize(compareSize);
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
   * The element with index 1 is the second element, aka [32..64) of the first mbb
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
    //System.out.println("--");
    //System.out.println("looking for element " + index);
/*
    int counter = index;
    int mbbIndex = 0;
*/

    //int block = index / elementsInFullMBB;

    //System.out.println("mbbIndex " + mbbIndex + " has elementcount: " + dataArray[mbbIndex].limit() / chunkSize);
/*    while (counter >= elementsInFullMBB) {
      counter -= dataArray[mbbIndex++].limit() / chunkSize;
    }*/

    //long result = (long) block << (4 * Integer.BYTES) & offset;

    return index % elementsInFullMBB;
  }

  public int getIndexOfMBB(int index) {
    //System.out.println("--");
    //System.out.println("looking for element " + index);
/*
    int counter = index;
    int mbbIndex = 0;
*/
    //System.out.println(index +" " + elementsInFullMBB + " " + index / elementsInFullMBB);

    return index / elementsInFullMBB;
  }

  private int getFirstBytesAsInt(int index) {
    //Pair<Integer, Integer> indexOffsetPair = getIndexAndOffset(index);

    int mbbIndex = getIndexOfMBB(index);//indexOffsetPair.getKey();
    int offset = getOffsetInMBB(index);//indexOffsetPair.getValue();

    //System.out.println("reading int in " + mbbIndex);
    //System.out.println("reading int at " + offset*chunkSize);
    return this.dataArray[mbbIndex].getInt(offset * chunkSize);
  }

  public int compare(int x, int y) {
    int indexX = indexArray[x];
    int indexY = indexArray[y];

    int firstBytesX = firstBytes[x];
    int firstBytesY = firstBytes[y];
    //System.out.println("comparing for elements " + x + " and " + y);
    //System.out.println("they have " + firstBytesX + " and " + firstBytesY);
    int comparison = Integer.compareUnsigned(firstBytesX, firstBytesY);

    if (comparison != 0) {
      return comparison;
    }

    int mbbIndexX = getIndexOfMBB(x);;
    int offsetX = getOffsetInMBB(x);

    int mbbIndexY = getIndexOfMBB(y);;
    int offsetY = getOffsetInMBB(y);

/*    System.out.println("integer is same for ");
    System.out.println(Arrays.toString(get(x)));
    System.out.println(Arrays.toString(get(y)));
    System.out.println("comparing next bytes now");*/

    for (int i = 4; i < compareSize;) {
      // TODO dont need to compare single bytes here, use getInt/ getLong / depending on remaining bytes

      if (compareSize - i < Long.BYTES) {
        byte xVal = dataArray[mbbIndexX].get(offsetX * chunkSize + i);
        byte yVal = dataArray[mbbIndexY].get(offsetY * chunkSize + i);

        //System.out.println("comparing " + xVal + " and " + yVal);
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

/*  public void add(byte[] element) {
    if (element.length != chunkSize) {
      throw new AssertionError("Chunksize and Input size must be equal!");
    }
    int offset = currentChunkIndex * chunkSize;
    System.arraycopy(element, 0, dataArray, offset, chunkSize);
    currentChunkIndex++;
  }*/

  public void sort() {
    if (indexArray == null) {
      elementsInFullMBB = dataArray[0].limit() / chunkSize;
      elementsInLastMBB = dataArray[dataArray.length-1].limit() / chunkSize;
      this.indexArray = IntStream.range(0, elementCount).toArray();
      this.firstBytes = fillFirstByteArray(elementCount);
      //System.out.println("first bytes:");
      //System.out.println(Arrays.toString(firstBytes));
      this.helper = new int[indexArray.length];
    }

    System.out.println("#elements: " + elementCount);
    System.out.println(Runtime.getRuntime().availableProcessors() + " processors available");
    final ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() - 2);

    forkJoinPool.invoke(new ParallelMergeSort(this, indexArray, helper, firstBytes, 0, elementCount-1));
  }

  public int size() {
    return elementCount;
  }
}
