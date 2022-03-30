package de.skuld.util;

import java.util.Arrays;
import java.util.List;

public class ArrayUtil {

  /**
   * Returns -1, if subarray is not a subarry of array, returns the index of the first byte else
   *
   * @param array
   * @param subarray
   * @return
   */
  public static int isSubArray(byte[] array, byte[] subarray) {
    if (array.length < subarray.length) {
      return -1;
    }
    if (subarray.length == 0) {
      return 0;
    }
    if (Arrays.equals(array, subarray)) {
      return 0;
    }

    for (int i = 0; i < array.length - subarray.length; i++) {
      for (int j = 0; j < subarray.length; j++) {
        if (!(array[i + j] == subarray[j])) {
          break;
        }
        if (j == subarray.length - 1) {
          return i;
        }
      }
    }

    return -1;
  }

  public static boolean checkSubArraysInArraySequential(byte[] array, List<byte[]> subArrays) {
    int subArrayIdx = 0;

    for (int arrayIdx = 0; arrayIdx < array.length; arrayIdx++) {
      if (subArrayIdx > subArrays.size() -1) {
        return true;
      }

      byte[] subarray = subArrays.get(subArrayIdx);

      if (subarray.length == 0 || array[arrayIdx] == subarray[0]) {
        boolean found = true;
        if (array.length - arrayIdx < subarray.length) {
          return false;
        }

        for (int idxInSubArray = 1; idxInSubArray < subarray.length; idxInSubArray++) {
          if (array[arrayIdx + idxInSubArray] != subarray[idxInSubArray]) {
            found = false;
            break;
          }
        }
        if (found) {
          subArrayIdx++;
          arrayIdx += subarray.length-1;
        }
      }
    }

    return subArrayIdx > subArrays.size() - 1;
  }
}
