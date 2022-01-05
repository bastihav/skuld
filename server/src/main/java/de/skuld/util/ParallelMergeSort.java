package de.skuld.util;

import java.util.concurrent.RecursiveAction;

public class ParallelMergeSort extends RecursiveAction {
  private static final int MAX = 1 << 13;
  private final byte[] dataArray;
  private final int[] indexArray;
  private final int[] helper;
  private final int[] firstBytes;
  private final int low;
  private final int high;

  public static int getChunkSize() {
    return chunkSize;
  }

  public static void setChunkSize(int chunkSize) {
    ParallelMergeSort.chunkSize = chunkSize;
  }

  public static int getCompareSize() {
    return compareSize;
  }

  public static void setCompareSize(int compareSize) {
    ParallelMergeSort.compareSize = compareSize;
  }

  private static int chunkSize;
  private static int compareSize;

  public ParallelMergeSort(final byte[] array, final int[] indexArray, final int[] helper,
      int[] firstBytes, final int low, final int high) {
    this.dataArray = array;
    this.indexArray = indexArray;
    this.firstBytes = firstBytes;
    this.helper = helper;
    this.low = low;
    this.high = high;
  }

  @Override
  protected void compute() {
    if (low < high) {
      if (high - low <= MAX) {
        //System.out.println("changing to non-parallel ");
        mergeSort(low, high);
      } else {
        parallelMergeSort();
      }
    }
  }

  private static int nextGap(int gap) {
    if (gap <= 1) {
      return 0;
    }
    return (int) Math.ceil(gap / 2.0);
  }

  private void swap(int x, int y) {
    //byte[] tmp = new byte[chunkSize];
    int tmp = indexArray[x];
    indexArray[x] = indexArray[y];
    indexArray[y] = tmp;
    //System.arraycopy(dataArray, x*chunkSize, tmp, 0, chunkSize);
    //System.arraycopy(dataArray, y*chunkSize, dataArray, x*chunkSize, chunkSize);
    //System.arraycopy(tmp, 0, dataArray, y*chunkSize, chunkSize);
  }

/*  private void inPlaceMerge(int start, int end) {
    int gap = end - start + 1;
    for (gap = nextGap(gap); gap > 0; gap = nextGap(gap)) {
      for (int i = start; i + gap <= end; i++) {
        int j = i + gap;
        if (compare(i,j) > 0) {
          swap(i, j);
        }
      }
    }
  }*/

  private void outPlaceMerge(int s, int e) {
    int mid = (s + e) / 2;

    System.arraycopy(indexArray, s, helper, s, e + 1 - s);

    int helperLeft = s;
    int helperRight = mid + 1;
    int current = s;

    while (helperLeft <= mid && helperRight <= e) {
      if (compare(helper[helperLeft], helper[helperRight]) <= 0) {
      //if (helper[helperLeft] <= helper[helperRight]) {
        indexArray[current] = helper[helperLeft++];
      } else {
        indexArray[current] = helper[helperRight++];
      }
      current++;
    }

    while (helperLeft <= mid) {
      indexArray[current++] = helper[helperLeft++];
    }
  }

  private int compare(int x, int y) {
    int indexX = indexArray[x];
    int indexY = indexArray[y];

    int firstBytesX = firstBytes[indexX];
    int firstBytesY = firstBytes[indexY];
    int comparison = Integer.compare(firstBytesX, firstBytesY);

    if (comparison != 0) {
      return comparison;
    }


    for (int i = 4; i < compareSize; i++) {
      int xIndex = (indexX * chunkSize) + i;
      int yIndex = (indexY * chunkSize) + i;

      byte xVal = dataArray[xIndex];
      byte yVal = dataArray[yIndex];

      //System.out.println("comparing " + xVal + " and " + yVal);
      comparison = Byte.compare(xVal, yVal);
      if (comparison != 0) {
        return comparison;
      }
    }
    return 0;
  }

  private void mergeSort(int s, int e) {
    if (s == e) {
      return;
    }

    int mid = (s + e) / 2;

    mergeSort(s, mid);
    mergeSort(mid + 1, e);
    //inPlaceMerge(s, e);
    outPlaceMerge(s, e);
  }

  private void parallelMergeSort() {
    final int middle = (low + high) / 2;
    final ParallelMergeSort left = new ParallelMergeSort(dataArray, indexArray, helper, firstBytes,
        low, middle);
    //System.out.println("left: " + low + " " + middle);
    final ParallelMergeSort right = new ParallelMergeSort(dataArray, indexArray, helper, firstBytes,
        middle + 1, high);
    //System.out.println("right: " + (middle+1) + " " + high);
    invokeAll(left, right);
    //System.out.println("----------------------------------------------------");

    //inPlaceMerge(low, high);
    outPlaceMerge(low, high);
  }
}
