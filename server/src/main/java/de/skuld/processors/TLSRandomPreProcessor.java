package de.skuld.processors;

import de.skuld.util.ByteHexUtil;
import de.skuld.web.model.Result;
import de.skuld.web.model.Result.FallbackProtectionEnum;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TLSRandomPreProcessor implements PreProcessor {

  // TLS 1.3 specific message requesting to send a new ClientHello
  private final static byte[] HELLO_RETRY_REQUEST_CONST =
      ByteHexUtil.hexToByte("CF21AD74E59A6111BE1D8C021E65B891C2A211167ABB8C5E079E09E2C8A8339C");

  // TLS 1.3 to TLS 1.2 Downgrade prevention
  private final static byte[] TLS_1_3_TO_TLS_1_2_DOWNGRADE_CONST =
      ByteHexUtil.hexToByte("444F574E47524401");

  // TLS 1.3 to TLS 1.1 or lower Downgrade prevention
  private final static byte[] TLS_1_3_TO_TLS_1_1_DOWNGRADE_CONST =
      ByteHexUtil.hexToByte("444F574E47524400");

  @Override
  public List<byte[]> preprocess(Result result, List<byte[]> input) {
    // TODO
    //result.setHelloRetry(input.stream().anyMatch(arr -> Arrays.equals(HELLO_RETRY_REQUEST_CONST, arr)));

    if (result.isHelloRetry()) {
      return Collections.emptyList();
    }

    //TODO ignore HRR specifically
    result.setUnixtime(input.stream().anyMatch(this::hasUnixTimestamp));

    result.setFallbackProtection(input.stream().map(this::hasFallbackProtection).filter(Objects::nonNull).findAny().orElse(null));
    return input.stream().map(arr -> removeNonRandom(arr, result.getFallbackProtection() != null,
        result.isUnixtime())).filter(arr -> !Arrays.equals(arr, HELLO_RETRY_REQUEST_CONST)).collect(Collectors.toList());
  }

  private byte[] removeNonRandom(byte[] input, boolean hasFallback, boolean hasUnix) {
    int start = hasUnix ? 4 : 0;
    int end = hasFallback ? 24 : 32;

    byte[] result = new byte[end - start];
    System.arraycopy(input, start, result, 0, end);
    return result;
  }

  private boolean hasUnixTimestamp(byte[] input) {
    final int UNIX_TIME_OFFSET = 172800; // two days

    ByteBuffer buffer = ByteBuffer.wrap(input);
    int possibleUnixTime = buffer.getInt();

    return possibleUnixTime > System.currentTimeMillis() / 1000 - UNIX_TIME_OFFSET &&
        possibleUnixTime < System.currentTimeMillis() / 1000 + UNIX_TIME_OFFSET;
  }

  private FallbackProtectionEnum hasFallbackProtection(byte[] input) {
    if (Arrays.equals(input, input.length - TLS_1_3_TO_TLS_1_1_DOWNGRADE_CONST.length, input.length, TLS_1_3_TO_TLS_1_1_DOWNGRADE_CONST, 0, TLS_1_3_TO_TLS_1_1_DOWNGRADE_CONST.length)) {
      return FallbackProtectionEnum._11;
    }
    if (Arrays.equals(input, input.length - TLS_1_3_TO_TLS_1_2_DOWNGRADE_CONST.length, input.length, TLS_1_3_TO_TLS_1_2_DOWNGRADE_CONST, 0, TLS_1_3_TO_TLS_1_2_DOWNGRADE_CONST.length)) {
      return FallbackProtectionEnum._12;
    }

    return null;
  }

}
