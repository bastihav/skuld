package de.skuld.util;

import java.util.concurrent.RecursiveAction;

public class ParallelMergeSort extends RecursiveAction {
  private static final int MAX = 1 << 13;
  private final WrappedByteBuffers buffers;
  private final int[] indexArray;
  private final int[] helper;
  private final int[] firstBytes;
  private final int low;
  private final int high;

  public ParallelMergeSort(WrappedByteBuffers buffers, final int[] indexArray, final int[] helper,
      int[] firstBytes, final int low, final int high) {
    this.buffers = buffers;
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
        mergeSort(low, high);
      } else {
        parallelMergeSort();
      }
    }
  }

  private void outPlaceMerge(int s, int e) {
    int mid = (s + e) / 2;

    System.arraycopy(indexArray, s, helper, s, e + 1 - s);

    int helperLeft = s;
    int helperRight = mid + 1;
    int current = s;

    while (helperLeft <= mid && helperRight <= e) {
      int helperLeftValue = helper[helperLeft];
      int helperRightValue = helper[helperRight];
      //noinspection SuspiciousNameCombination
      if (buffers.compare(helperLeftValue, helperRightValue) <= 0) {
        indexArray[current] = helperLeftValue;
        helperLeft++;
      } else {
        indexArray[current] = helperRightValue;
        helperRight++;
      }
      current++;
    }

    System.arraycopy(helper, helperLeft, indexArray, current, Math.max(0, mid-helperLeft+1));
  }



  private void mergeSort(int s, int e) {
    if (s == e) {
      return;
    }

    int mid = (s + e) / 2;

    mergeSort(s, mid);
    mergeSort(mid + 1, e);
    outPlaceMerge(s, e);
  }

  private void parallelMergeSort() {
    final int middle = (low + high) / 2;
    final ParallelMergeSort left = new ParallelMergeSort(buffers, indexArray, helper, firstBytes,
        low, middle);
    final ParallelMergeSort right = new ParallelMergeSort(buffers, indexArray, helper, firstBytes,
        middle + 1, high);
    invokeAll(left, right);
    outPlaceMerge(low, high);
  }
}
