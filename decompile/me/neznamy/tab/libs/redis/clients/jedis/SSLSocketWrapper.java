package me.neznamy.tab.libs.redis.clients.jedis;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.List;
import java.util.function.BiFunction;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

public class SSLSocketWrapper extends SSLSocket {
   SSLSocket actual;
   Socket underlying;
   InputStream wrapper;

   public SSLSocketWrapper(SSLSocket actual, Socket underlying) throws IOException {
      this.actual = actual;
      this.underlying = underlying;
      this.wrapper = new SSLSocketWrapper.InputStreamWrapper(actual.getInputStream(), underlying.getInputStream());
   }

   @Override
   public void connect(SocketAddress endpoint) throws IOException {
      this.actual.connect(endpoint);
   }

   @Override
   public void connect(SocketAddress endpoint, int timeout) throws IOException {
      this.actual.connect(endpoint, timeout);
   }

   @Override
   public void bind(SocketAddress bindpoint) throws IOException {
      this.actual.bind(bindpoint);
   }

   @Override
   public InetAddress getInetAddress() {
      return this.actual.getInetAddress();
   }

   @Override
   public InetAddress getLocalAddress() {
      return this.actual.getLocalAddress();
   }

   @Override
   public int getPort() {
      return this.actual.getPort();
   }

   @Override
   public int getLocalPort() {
      return this.actual.getLocalPort();
   }

   @Override
   public SocketAddress getRemoteSocketAddress() {
      return this.actual.getRemoteSocketAddress();
   }

   @Override
   public SocketAddress getLocalSocketAddress() {
      return this.actual.getLocalSocketAddress();
   }

   @Override
   public void setTcpNoDelay(boolean on) throws SocketException {
      this.actual.setTcpNoDelay(on);
   }

   @Override
   public boolean getTcpNoDelay() throws SocketException {
      return this.actual.getTcpNoDelay();
   }

   @Override
   public void setSoLinger(boolean on, int linger) throws SocketException {
      this.actual.setSoLinger(on, linger);
   }

   @Override
   public int getSoLinger() throws SocketException {
      return this.actual.getSoLinger();
   }

   @Override
   public void sendUrgentData(int data) throws IOException {
      this.actual.sendUrgentData(data);
   }

   @Override
   public void setOOBInline(boolean on) throws SocketException {
      this.actual.setOOBInline(on);
   }

   @Override
   public boolean getOOBInline() throws SocketException {
      return this.actual.getOOBInline();
   }

   @Override
   public synchronized void setSoTimeout(int timeout) throws SocketException {
      this.actual.setSoTimeout(timeout);
   }

   @Override
   public synchronized int getSoTimeout() throws SocketException {
      return this.actual.getSoTimeout();
   }

   @Override
   public synchronized void setSendBufferSize(int size) throws SocketException {
      this.actual.setSendBufferSize(size);
   }

   @Override
   public synchronized int getSendBufferSize() throws SocketException {
      return this.actual.getSendBufferSize();
   }

   @Override
   public synchronized void setReceiveBufferSize(int size) throws SocketException {
      this.actual.setReceiveBufferSize(size);
   }

   @Override
   public synchronized int getReceiveBufferSize() throws SocketException {
      return this.actual.getReceiveBufferSize();
   }

   @Override
   public void setKeepAlive(boolean on) throws SocketException {
      this.actual.setKeepAlive(on);
   }

   @Override
   public boolean getKeepAlive() throws SocketException {
      return this.actual.getKeepAlive();
   }

   @Override
   public void setTrafficClass(int tc) throws SocketException {
      this.actual.setTrafficClass(tc);
   }

   @Override
   public int getTrafficClass() throws SocketException {
      return this.actual.getTrafficClass();
   }

   @Override
   public void setReuseAddress(boolean on) throws SocketException {
      this.actual.setReuseAddress(on);
   }

   @Override
   public boolean getReuseAddress() throws SocketException {
      return this.actual.getReuseAddress();
   }

   @Override
   public synchronized void close() throws IOException {
      this.actual.close();
   }

   @Override
   public void shutdownInput() throws IOException {
      this.actual.shutdownInput();
   }

   @Override
   public void shutdownOutput() throws IOException {
      this.actual.shutdownOutput();
   }

   @Override
   public String toString() {
      return this.actual.toString();
   }

   @Override
   public boolean isConnected() {
      return this.actual.isConnected();
   }

   @Override
   public boolean isBound() {
      return this.actual.isBound();
   }

   @Override
   public boolean isClosed() {
      return this.actual.isClosed();
   }

   @Override
   public boolean isInputShutdown() {
      return this.actual.isInputShutdown();
   }

   @Override
   public boolean isOutputShutdown() {
      return this.actual.isOutputShutdown();
   }

   @Override
   public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
      this.actual.setPerformancePreferences(connectionTime, latency, bandwidth);
   }

   @Override
   public InputStream getInputStream() throws IOException {
      return this.wrapper;
   }

   @Override
   public OutputStream getOutputStream() throws IOException {
      return this.actual.getOutputStream();
   }

   @Override
   public String[] getSupportedCipherSuites() {
      return this.actual.getSupportedCipherSuites();
   }

   @Override
   public String[] getEnabledCipherSuites() {
      return this.actual.getEnabledCipherSuites();
   }

   @Override
   public void setEnabledCipherSuites(String[] var1) {
      this.actual.setEnabledCipherSuites(var1);
   }

   @Override
   public String[] getSupportedProtocols() {
      return this.actual.getSupportedProtocols();
   }

   @Override
   public String[] getEnabledProtocols() {
      return this.actual.getEnabledProtocols();
   }

   @Override
   public void setEnabledProtocols(String[] var1) {
      this.actual.setEnabledProtocols(var1);
   }

   @Override
   public SSLSession getSession() {
      return this.actual.getSession();
   }

   @Override
   public SSLSession getHandshakeSession() {
      return this.actual.getHandshakeSession();
   }

   @Override
   public void addHandshakeCompletedListener(HandshakeCompletedListener var1) {
      this.actual.addHandshakeCompletedListener(var1);
   }

   @Override
   public void removeHandshakeCompletedListener(HandshakeCompletedListener var1) {
      this.actual.removeHandshakeCompletedListener(var1);
   }

   @Override
   public void startHandshake() throws IOException {
      this.actual.startHandshake();
   }

   @Override
   public void setUseClientMode(boolean var1) {
      this.actual.setUseClientMode(var1);
   }

   @Override
   public boolean getUseClientMode() {
      return this.actual.getUseClientMode();
   }

   @Override
   public void setNeedClientAuth(boolean var1) {
      this.actual.setNeedClientAuth(var1);
   }

   @Override
   public boolean getNeedClientAuth() {
      return this.actual.getNeedClientAuth();
   }

   @Override
   public void setWantClientAuth(boolean var1) {
      this.actual.setWantClientAuth(var1);
   }

   @Override
   public boolean getWantClientAuth() {
      return this.actual.getWantClientAuth();
   }

   @Override
   public void setEnableSessionCreation(boolean var1) {
      this.actual.setEnableSessionCreation(var1);
   }

   @Override
   public boolean getEnableSessionCreation() {
      return this.actual.getEnableSessionCreation();
   }

   @Override
   public SSLParameters getSSLParameters() {
      return this.actual.getSSLParameters();
   }

   @Override
   public void setSSLParameters(SSLParameters var1) {
      this.actual.setSSLParameters(var1);
   }

   @Override
   public String getApplicationProtocol() {
      return this.actual.getApplicationProtocol();
   }

   @Override
   public String getHandshakeApplicationProtocol() {
      return this.actual.getHandshakeApplicationProtocol();
   }

   @Override
   public void setHandshakeApplicationProtocolSelector(BiFunction<SSLSocket, List<String>, String> var1) {
      this.actual.setHandshakeApplicationProtocolSelector(var1);
   }

   @Override
   public BiFunction<SSLSocket, List<String>, String> getHandshakeApplicationProtocolSelector() {
      return this.actual.getHandshakeApplicationProtocolSelector();
   }

   private class InputStreamWrapper extends InputStream {
      private InputStream actual;
      private InputStream underlying;

      public InputStreamWrapper(InputStream actual, InputStream underlying) {
         this.actual = actual;
         this.underlying = underlying;
      }

      @Override
      public int read() throws IOException {
         return this.actual.read();
      }

      @Override
      public int read(byte[] b) throws IOException {
         return this.actual.read(b);
      }

      @Override
      public int read(byte[] b, int off, int len) throws IOException {
         return this.actual.read(b, off, len);
      }

      @Override
      public long skip(long n) throws IOException {
         return this.actual.skip(n);
      }

      @Override
      public int available() throws IOException {
         return this.underlying.available();
      }

      @Override
      public void close() throws IOException {
         this.actual.close();
      }

      @Override
      public synchronized void mark(int readlimit) {
         this.actual.mark(readlimit);
      }

      @Override
      public synchronized void reset() throws IOException {
         this.actual.reset();
      }

      @Override
      public boolean markSupported() {
         return this.actual.markSupported();
      }
   }
}
