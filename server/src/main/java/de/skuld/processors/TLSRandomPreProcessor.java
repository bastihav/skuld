package de.skuld.processors;

import de.skuld.util.ByteHexUtil;
import de.skuld.web.model.Result;
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
    if (input == null) {
      return Collections.emptyList();
    }
    List<byte[]> inputCopy = new ArrayList<>(input);

    inputCopy.removeIf(Objects::isNull);
    inputCopy.removeIf(bytes -> bytes.length != 32);

    List<byte[]> withoutHRR = inputCopy.stream().filter(arr -> !Arrays.equals(arr,
        HELLO_RETRY_REQUEST_CONST)).collect(Collectors.toList());

    result.getTlsTests()
        .setUnixtime(withoutHRR.size() > 0 && withoutHRR.stream().allMatch(this::hasUnixTimestamp));
    result.getTlsTests()
        .setAllZeroRandom(withoutHRR.size() > 0 && withoutHRR.stream().allMatch(this::allZero));
    result.getTlsTests().setReusesRandom(
        withoutHRR.size() > 0 && reusesRandom(withoutHRR, result.getTlsTests().isUnixtime()));

    // TODO LIMIT
    return withoutHRR.stream().map(arr -> removeNonRandom(arr,
        result.getTlsTests().isUnixtime())).limit(5).collect(Collectors.toList());
  }

  private boolean reusesRandom(List<byte[]> randoms, boolean usesUnixTime) {
    if (randoms.size() == 0) {
      return false;
    }
    int start = usesUnixTime ? 4 : 0;

    for (int i = 0; i < randoms.size(); i++) {
      for (int i1 = i; i1 < randoms.size(); i1++) {
        if (i == i1) {
          continue;
        }
        boolean reused = Arrays.equals(randoms.get(i), start, 32, randoms.get(i1), start, 32);
        if (reused) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean allZero(byte[] input) {
    int start = hasUnixTimestamp(input) ? 4 : 0;
    int end = hasFallbackProtection(input) ? 24 : 32;

    boolean allZero = true;
    for (int i = start; i < end; i++) {
      allZero &= input[i] == 0;
    }
    return allZero;
  }

  private byte[] removeNonRandom(byte[] input, boolean hasUnix) {
    int start = hasUnix ? 4 : 0;
    int end = hasFallbackProtection(input) ? 24 : 32;
    int length = end-start;

    byte[] result = new byte[length];
    System.arraycopy(input, start, result, 0, length);
    return result;
  }

  private boolean hasUnixTimestamp(byte[] input) {
    final int UNIX_TIME_OFFSET = 172800; // two days

    ByteBuffer buffer = ByteBuffer.wrap(input);
    int possibleUnixTime = buffer.getInt();

    return possibleUnixTime > System.currentTimeMillis() / 1000 - UNIX_TIME_OFFSET &&
        possibleUnixTime < System.currentTimeMillis() / 1000 + UNIX_TIME_OFFSET;
  }

  private boolean hasFallbackProtection(byte[] input) {
    if (Arrays.equals(input, input.length - TLS_1_3_TO_TLS_1_1_DOWNGRADE_CONST.length, input.length,
        TLS_1_3_TO_TLS_1_1_DOWNGRADE_CONST, 0, TLS_1_3_TO_TLS_1_1_DOWNGRADE_CONST.length)) {
      return true;
    }
    return Arrays.equals(input, input.length - TLS_1_3_TO_TLS_1_2_DOWNGRADE_CONST.length,
        input.length, TLS_1_3_TO_TLS_1_2_DOWNGRADE_CONST, 0,
        TLS_1_3_TO_TLS_1_2_DOWNGRADE_CONST.length);
  }

}
