package de.skuld.util;

public class BytePrinter {

  public static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02X ", b));
    }

    return sb.toString();
  }

  public static void printBytesAsHex(byte[] bytes) {
    System.out.println(bytesToHex(bytes));
  }
}
