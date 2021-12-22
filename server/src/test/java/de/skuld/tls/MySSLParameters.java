package de.skuld.tls;

import java.security.AlgorithmConstraints;
import java.security.AlgorithmParameters;
import java.security.CryptoPrimitive;
import java.security.Key;
import java.util.Set;
import javax.net.ssl.SSLParameters;

public class MySSLParameters extends SSLParameters {
  @Override
  public AlgorithmConstraints getAlgorithmConstraints() {

    return new AlgorithmConstraints() {
      @Override
      public boolean permits(Set<CryptoPrimitive> set, String s,
          AlgorithmParameters algorithmParameters) {
        return true;
      }

      @Override
      public boolean permits(Set<CryptoPrimitive> set, Key key) {
        return true;
      }

      @Override
      public boolean permits(Set<CryptoPrimitive> set, String s, Key key,
          AlgorithmParameters algorithmParameters) {
        return true;
      }
    };
  }

  @Override
  public String[] getCipherSuites() {
    return new String[]{"TLS_DH_anon_WITH_AES_256_CBC_SHA256"};
  }
}
