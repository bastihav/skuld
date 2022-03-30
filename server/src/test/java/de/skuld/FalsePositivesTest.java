package de.skuld;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import de.skuld.prng.PRNG;
import de.skuld.prng.Xoshiro128StarStar;
import de.skuld.prng.Xoshiro256StarStar;
import de.skuld.util.ByteHexUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class FalsePositivesTest {

  @Test
  public void test() {
    PRNG expected = new Xoshiro128StarStar(1646830334);
    int[] seed = new int[]{
      Ints.fromByteArray(ByteHexUtil.hexToByte("45 98 80 4B")),
          Ints.fromByteArray(ByteHexUtil.hexToByte("DF 57 5D E2")),
          Ints.fromByteArray(ByteHexUtil.hexToByte("CB DA 58 D8")),
          Ints.fromByteArray(ByteHexUtil.hexToByte("10 08 C1 3A"))
    };
    PRNG actual = new Xoshiro128StarStar(seed);


    byte[] bytes = new byte[]{-49, 33, -83, 116, -27, -102, 97, 17, -66, 29, -116, 2, 30, 101, -72, -111, -62, -94, 17, 22, 122, -69, -116, 94, 7, -98, 9, -30, -56, -88, 51, -100};
    //byte[] bytes2 = new byte[]{-104, 118, -92, -66, 34, 70, -41, -21, -12, -113, 14, -29, 70, 64, 82, 121, 111, -5, 82, 8, -73, -24, -31, 38, -109, -29, 93, 84, 45, 2, 75, 89};


    byte[] expectedBytes = new byte[32768];
    byte[] actualBytes = new byte[32768];

    expected.nextBytes(expectedBytes);
    actual.nextBytes(actualBytes);

    ByteHexUtil.printBytesAsHex(bytes);
    /*ByteHexUtil.printBytesAsHex(bytes2);*/
    ByteHexUtil.printBytesAsHex(expectedBytes);
    ByteHexUtil.printBytesAsHex(actualBytes);

  }

}
