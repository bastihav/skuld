package de.skuld.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArrayUtilTest {

  @Test
  public void testSubArray() {
    byte[] array = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    byte[] subArray1 = new byte[]{0, 1};
    byte[] subArray2 = new byte[]{4, 5, 6};
    byte[] subArray3 = new byte[]{4, 6};
    byte[] subArray4 = new byte[]{};
    byte[] subArray5 = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    byte[] subArray6 = new byte[]{42, 125};

    Assertions.assertEquals(0, ArrayUtil.isSubArray(array, subArray1));
    Assertions.assertEquals(4, ArrayUtil.isSubArray(array, subArray2));
    Assertions.assertEquals(-1, ArrayUtil.isSubArray(array, subArray3));
    Assertions.assertEquals(0, ArrayUtil.isSubArray(array, subArray4));
    Assertions.assertEquals(0, ArrayUtil.isSubArray(array, subArray5));
    Assertions.assertEquals(-1, ArrayUtil.isSubArray(array, subArray6));
  }
}
