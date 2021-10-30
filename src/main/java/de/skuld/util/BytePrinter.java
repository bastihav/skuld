package de.skuld.util;

public class BytePrinter {

  public static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(byteToHex(b)).append(" ");
    }

    return sb.toString();
  }

  public static String byteToHex(byte b) {
    return String.format("%02X",b);
  }

  public static void printBytesAsHex(byte[] bytes) {
    System.out.println(bytesToHex(bytes));
  }
}
