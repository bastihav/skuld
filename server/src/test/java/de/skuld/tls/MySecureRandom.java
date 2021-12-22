package de.skuld.tls;

import java.security.SecureRandom;
import java.security.SecureRandomParameters;
import java.util.Arrays;

public class MySecureRandom extends SecureRandom {

  /**
   * For Client/ServerRandom, this version of nextBytes is called
   * @param bytes
   */
  @Override
  public void nextBytes(byte[] bytes) {
    System.out.println("called next bytes 1");
    System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
    super.nextBytes(bytes);
  }

  @Override
  public void nextBytes(byte[] bytes, SecureRandomParameters params) {
    System.out.println("called next bytes 2");
    super.nextBytes(bytes, params);
  }

  @Override
  public void reseed() {
    System.out.println("called reseed 1");
    super.reseed();
  }

  @Override
  public void reseed(SecureRandomParameters params) {
    System.out.println("called reseed 2");
    super.reseed(params);
  }

  @Override
  public String getAlgorithm() {
    return "My Secure Random";
  }
}


