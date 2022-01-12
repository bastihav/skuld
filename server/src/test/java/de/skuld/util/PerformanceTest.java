package de.skuld.util;

import java.util.Random;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  @Disabled
  public void test() {
    Random random = new Random(0);
    int x = random.nextInt();
    int y = random.nextInt();

    long ping = System.nanoTime();

    for (long i = 0; i < Integer.MAX_VALUE; i++) {
      int a= Long.compareUnsigned(x,y);
    }

    long pong = System.nanoTime();
    System.out.println("unsinged compare: " + (pong - ping));


    ping = System.nanoTime();

    for (long i = 0; i < Integer.MAX_VALUE; i++) {
      int a=Long.compare(x,y);
    }

    pong = System.nanoTime();
    System.out.println("signed compare: " + (pong - ping));
  }

}
