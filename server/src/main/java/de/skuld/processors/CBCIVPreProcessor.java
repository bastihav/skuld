package de.skuld.processors;

import de.skuld.web.model.Result;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CBCIVPreProcessor implements PreProcessor {

  @Override
  public List<byte[]> preprocess(Result result, List<byte[]> input) {
    if (input == null) {
      return Collections.emptyList();
    }
    List<byte[]> inputCopy = new ArrayList<>(input);

    inputCopy.removeIf(Objects::isNull);
    inputCopy.removeIf(bytes -> bytes.length != 16);

    result.getTlsTests()
        .setAllZeroIV(inputCopy.size() > 0 && inputCopy.stream().allMatch(this::allZero));
    result.getTlsTests().setReusesIV(inputCopy.size() > 0 && reusesRandom(inputCopy));

    // TODO limit
    return inputCopy.stream().limit(5).collect(Collectors.toList());
  }

  private boolean reusesRandom(List<byte[]> randoms) {
    if (randoms.size() == 0) {
      return false;
    }

    for (int i = 0; i < randoms.size(); i++) {
      for (int i1 = i; i1 < randoms.size(); i1++) {
        if (i == i1) {
          continue;
        }
        boolean reused = Arrays.equals(randoms.get(i), 0, 16, randoms.get(i1), 0, 16);
        if (reused) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean allZero(byte[] input) {
    boolean allZero = true;
    for (byte b : input) {
      allZero &= b == 0;
    }
    return allZero;
  }
}
