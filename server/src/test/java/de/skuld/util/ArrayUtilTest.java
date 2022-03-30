package de.skuld.util;

import static de.skuld.util.ArrayUtil.checkSubArraysInArraySequential;

import com.google.common.base.Stopwatch;
import de.skuld.prng.ImplementedPRNGs;
import de.skuld.prng.PRNG;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArrayUtilTest {

  @Test
  public void testSubArray() {
    byte[] array = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    byte[] subArray1 = new byte[]{1};
    byte[] subArray2 = new byte[]{4, 5, 6};
    byte[] subArray5 = new byte[]{7,8,9};
    byte[] subArray6 = new byte[]{10};

    ArrayList<byte[]> list = new ArrayList<>(5);
    list.add(subArray1);
    list.add(subArray2);
    list.add(subArray5);
    list.add(subArray6);


    Assertions.assertTrue(checkSubArraysInArraySequential(array, list));
  }

  @Test
  public void test() {
    PRNG prng = ImplementedPRNGs.getPRNG(ImplementedPRNGs.MERSENNE_TWISTER_PYTHON, 1646739902);

    Stopwatch stopwatch = Stopwatch.createStarted();
    byte[] array = prng.getBytes(0, 32768);


    prng = ImplementedPRNGs.getPRNG(ImplementedPRNGs.MERSENNE_TWISTER_PYTHON, 1646739902);
    List<byte[]> list = new ArrayList<>();

    for (int i = 0; i < 25; i++) {
      byte[] random = new byte[32];
      prng.nextBytes(random);
      list.add(random);
    }


    System.out.println("now");
    Assertions.assertTrue(checkSubArraysInArraySequential(array, list));
    stopwatch.stop();
    System.out.println(stopwatch.elapsed().getNano());
  }
}
