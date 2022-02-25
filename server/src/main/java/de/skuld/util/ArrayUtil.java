package de.skuld.util;

import java.util.Arrays;

public class ArrayUtil {
  public static int isSubArray(byte[] array, byte[] subarray) {
    if (array.length < subarray.length) {
      return -1;
    }
    if (subarray.length == 0) {
      return 0;
    }
    if (Arrays.equals(array, subarray)) return 0;

    for (int i = 0; i < array.length - subarray.length; i++) {
      for (int j = 0; j < subarray.length; j++) {
        if (! (array[i + j] == subarray[j])) break;
        if (j == subarray.length -1) {
          return i;
        }
      }
    }

    return -1;
  }
}
