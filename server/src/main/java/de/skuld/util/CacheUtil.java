package de.skuld.util;

import com.google.common.primitives.UnsignedBytes;
import java.util.Arrays;

public class CacheUtil {

  /**
   * @param query
   * @param array
   * @param startIndex
   * @param endIndex   exclusive
   * @return
   */
  public static int lastIndexOf(int[] sortedIndices, byte[] query, WrappedByteBuffers array,
      int startIndex, int endIndex) {
    return binarySearch(sortedIndices, array, startIndex, endIndex - 1, query);
  }

  /**
   * not really binary search, always gets the lst index of this occurrence
   *
   * @param sortedIndices
   * @param array
   * @param left
   * @param right
   * @param query
   * @return
   */
  private static int binarySearch(int[] sortedIndices, WrappedByteBuffers array, int left,
      int right, byte[] query) {
    while (left <= right) {
      int mid = left + (right - left) / 2;

      if (mid >= array.size() - 1) {
        return mid;
      }

      int comparison = UnsignedBytes.lexicographicalComparator().compare(query,
          Arrays.copyOfRange(array.get(sortedIndices[mid]), 0, query.length));
      if (comparison < 0) {
        right = mid - 1;
      } else if (comparison == 0) {
        if (UnsignedBytes.lexicographicalComparator().compare(query,
            Arrays.copyOfRange(array.get(sortedIndices[mid + 1]), 0, query.length)) < 0) {
          return mid;
        } else {
          left = mid + 1;
        }
      } else {
        left = mid + 1;
      }
    }

    return left;
  }
}
