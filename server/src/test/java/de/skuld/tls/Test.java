package de.skuld.tls;

import de.skuld.tls.MyInsecureSSLSocketFactory;
import de.skuld.tls.MySecureRandom;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.Socket;
import java.security.AlgorithmConstraints;
import java.security.AlgorithmParameters;
import java.security.CryptoPrimitive;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Set;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Test {

  @org.junit.jupiter.api.Test
  public void test()
      throws IOException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException, InterruptedException {



    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        SSLServerSocket socket = null;
        try {
          socket = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(33098);
          socket.setSSLParameters(new MySSLParameters());
          Field sslContext = socket.getClass().getDeclaredField("sslContext");
          sslContext.setAccessible(true);

          socket.setEnabledCipherSuites(new String[]{"TLS_DH_anon_WITH_AES_256_CBC_SHA256"});

          Object context = sslContext.get(socket);
          System.out.println(context.getClass());

          Field secureRandomField = context.getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredField("secureRandom");
          secureRandomField.setAccessible(true);
          SecureRandom secureRandom = (SecureRandom) secureRandomField.get(context);

          System.out.println(secureRandom.getAlgorithm());

          secureRandomField.set(context, new MySecureRandom());

          secureRandom = (SecureRandom) secureRandomField.get(context);
          System.out.println(secureRandom.getAlgorithm());

          while (true) {
            SSLSocket s = (SSLSocket) socket.accept();
            System.out.println(Arrays.toString(s.getEnabledCipherSuites()));
          }
        } catch (IOException | IllegalAccessException | NoSuchFieldException e) {
          e.printStackTrace();
        }
      }
    });
    t.start();

    new Thread(new Runnable() {
      @Override
      public void run() {
        try {

          SSLSocket socket = (SSLSocket) new MyInsecureSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket("localhost", 33098);
          socket.setSSLParameters(new MySSLParameters());
          socket.setEnabledCipherSuites(new String[]{"TLS_DH_anon_WITH_AES_256_CBC_SHA256"});
          //SSLLogger.
          System.out.println(Arrays.toString(socket.getEnabledProtocols()));
          System.out.println(Arrays.toString(socket.getEnabledCipherSuites()));
          socket.setNeedClientAuth(false);
          socket.setWantClientAuth(false);
          System.out.println(socket.getSSLParameters().getAlgorithmConstraints());
          //socket.setSSLParameters(new SSLParameters());
          //socket.getHandshakeSession().getSessionContext().get;
          socket.startHandshake();
          OutputStream out = socket.getOutputStream();
          PrintWriter pw = new PrintWriter(out, true);
          int i = 0;
          while (true) {
            pw.println("test data " + i++);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();

    t.join();


    // TODO extend SecureRandom
    // TODO extend SSLContextImpl, add new SecureRandom in there
    // TODO create ServerSocket using this ssl context
  }

  private class NiceAlgorithmConstraints {
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
  }
}
