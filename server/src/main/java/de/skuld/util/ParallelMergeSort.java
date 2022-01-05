package de.skuld.util;

import java.util.concurrent.RecursiveAction;

public class ParallelMergeSort extends RecursiveAction {
  private static final int MAX = 1 << 15;
  private final byte[] array;
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

  public ParallelMergeSort(final byte[] array, final int low, final int high) {
    this.array = array;
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
    byte[] tmp = new byte[chunkSize];

    System.arraycopy(array, x*chunkSize, tmp, 0, chunkSize);
    System.arraycopy(array, y*chunkSize, array, x*chunkSize, chunkSize);
    System.arraycopy(tmp, 0, array, y*chunkSize, chunkSize);
  }

  private void inPlaceMerge(int start, int end) {
    int gap = end - start + 1;
    for (gap = nextGap(gap); gap > 0; gap = nextGap(gap)) {
      for (int i = start; i + gap <= end; i++) {
        int j = i + gap;
        if (compare(i,j) > 0) {
          swap(i, j);
        }
      }
    }
  }

  private int compare(int x, int y) {
    for (int i = 0; i < compareSize; i++) {
      int xIndex = (x * chunkSize) + i;
      int yIndex = (y * chunkSize) + i;

      byte xVal = array[xIndex];
      byte yVal = array[yIndex];

      int comparison = Byte.compare(xVal, yVal);
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
    inPlaceMerge(s, e);
  }

  private void parallelMergeSort() {
    final int middle = (low + high) / 2;
    final ParallelMergeSort left = new ParallelMergeSort(array, low, middle);
    //System.out.println("left: " + low + " " + middle);
    final ParallelMergeSort right = new ParallelMergeSort(array, middle + 1, high);
    //System.out.println("right: " + (middle+1) + " " + high);
    invokeAll(left, right);
    //System.out.println("----------------------------------------------------");
    inPlaceMerge(low, high);
  }
}
