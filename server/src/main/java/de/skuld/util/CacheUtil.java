package de.skuld.util;

import com.google.common.primitives.UnsignedBytes;
import de.skuld.radix.AbstractRadixTrieDataPoint;
import de.skuld.radix.data.RandomnessRadixTrieDataPoint;
import java.util.Arrays;

public class CacheUtil {

  public static int lastIndexOf(byte[] bytes, AbstractRadixTrieDataPoint<byte[]>[] array, int startIndex, int endIndex) {
    return binarySearch(array, startIndex, endIndex, bytes);
  }

  private static int binarySearch(AbstractRadixTrieDataPoint<byte[]>[] array, int left, int right, byte[] query) {
    while (left <= right) {
      int mid = left + (right - left) / 2;

      int comparison = UnsignedBytes.lexicographicalComparator().compare(query,
          Arrays.copyOfRange(array[mid].getRemainingIndexingData(), 0, query.length));
      if (comparison < 0) {
        right = mid - 1;
      } else if (comparison == 0) {
        if (mid >= query.length-1) {
          return mid;
        }
        if (UnsignedBytes.lexicographicalComparator().compare(query,
            Arrays.copyOfRange(array[mid+1].getRemainingIndexingData(), 0, query.length)) >= 1) {
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
