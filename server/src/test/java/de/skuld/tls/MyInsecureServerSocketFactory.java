package de.skuld.tls;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

final class MyInsecureSSLSocketFactory extends SSLSocketFactory {
  final SSLSocketFactory wrappedSSLSocketFactory;

  MyInsecureSSLSocketFactory(SSLSocketFactory wrappedSSLSocketFactory) {
    this.wrappedSSLSocketFactory = wrappedSSLSocketFactory;
  }

  @Override
  public String[] getDefaultCipherSuites() {
    return getMyCipherSuites();
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return getMyCipherSuites();
  }

  @Override
  public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
    final SSLSocket sslSocket = (SSLSocket) wrappedSSLSocketFactory.createSocket(s, host, port, autoClose);

    // change the supported cipher suites on the socket, *before* it's returned to the client
    sslSocket.setEnabledCipherSuites(getMyCipherSuites());

    return sslSocket;
  }

  // other overloaded createSocket() methods do the same

  private String[] getMyCipherSuites() {
    // TODO: change this to return whatever cipher suites you want the server to use, from CipherSuite
    return new String[] {"TLS_DH_anon_WITH_AES_256_CBC_SHA256"};
  }

  @Override
  public Socket createSocket(String s, int i) throws IOException, UnknownHostException {
    final SSLSocket sslSocket = (SSLSocket) wrappedSSLSocketFactory.createSocket(s, i);

    // change the supported cipher suites on the socket, *before* it's returned to the client
    sslSocket.setEnabledCipherSuites(getMyCipherSuites());

    return sslSocket;
  }

  @Override
  public Socket createSocket(String s, int i, InetAddress inetAddress, int i1)
      throws IOException, UnknownHostException {
    final SSLSocket sslSocket = (SSLSocket) wrappedSSLSocketFactory.createSocket(s, i);

    // change the supported cipher suites on the socket, *before* it's returned to the client
    sslSocket.setEnabledCipherSuites(getMyCipherSuites());

    return sslSocket;
  }

  @Override
  public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
    final SSLSocket sslSocket = (SSLSocket) wrappedSSLSocketFactory.createSocket(inetAddress.getHostAddress(), i);

    // change the supported cipher suites on the socket, *before* it's returned to the client
    sslSocket.setEnabledCipherSuites(getMyCipherSuites());

    return sslSocket;
  }

  @Override
  public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1)
      throws IOException {
    final SSLSocket sslSocket = (SSLSocket) wrappedSSLSocketFactory.createSocket(inetAddress.getHostAddress(), i);

    // change the supported cipher suites on the socket, *before* it's returned to the client
    sslSocket.setEnabledCipherSuites(getMyCipherSuites());

    return sslSocket;
  }
}
