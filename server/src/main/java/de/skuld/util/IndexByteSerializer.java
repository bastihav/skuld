package de.skuld.util;

import java.nio.ByteBuffer;

public class IndexByteSerializer {

  public static byte[] serialize(int max, int value) {
    if (value > max) {
      throw new AssertionError("Value may never exceed maximum.");
    }

    final int VALUES_PER_BYTE = 256;

    // round up to nearest integer
    final int arraySize = max / VALUES_PER_BYTE + ((max % VALUES_PER_BYTE == 0) ? 0 : 1);

    return ByteBuffer.allocate(arraySize).putInt(value).array();
  }

}
