package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketFrameHandler implements FrameHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(SocketFrameHandler.class);
   private final Socket _socket;
   private final ExecutorService _shutdownExecutor;
   private final DataInputStream _inputStream;
   private final Lock _inputStreamLock = new ReentrantLock();
   private final DataOutputStream _outputStream;
   private final Lock _outputStreamLock = new ReentrantLock();
   private final int maxInboundMessageBodySize;
   public static final int SOCKET_CLOSING_TIMEOUT = 1;

   public SocketFrameHandler(Socket socket) throws IOException {
      this(socket, null, Integer.MAX_VALUE);
   }

   public SocketFrameHandler(Socket socket, ExecutorService shutdownExecutor, int maxInboundMessageBodySize) throws IOException {
      this._socket = socket;
      this._shutdownExecutor = shutdownExecutor;
      this.maxInboundMessageBodySize = maxInboundMessageBodySize;
      this._inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
      this._outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
   }

   @Override
   public InetAddress getAddress() {
      return this._socket.getInetAddress();
   }

   @Override
   public InetAddress getLocalAddress() {
      return this._socket.getLocalAddress();
   }

   public DataInputStream getInputStream() {
      return this._inputStream;
   }

   @Override
   public int getPort() {
      return this._socket.getPort();
   }

   @Override
   public int getLocalPort() {
      return this._socket.getLocalPort();
   }

   @Override
   public void setTimeout(int timeoutMs) throws SocketException {
      this._socket.setSoTimeout(timeoutMs);
   }

   @Override
   public int getTimeout() throws SocketException {
      return this._socket.getSoTimeout();
   }

   public void sendHeader(int major, int minor) throws IOException {
      this._outputStreamLock.lock();

      try {
         this._outputStream.write("AMQP".getBytes("US-ASCII"));
         this._outputStream.write(1);
         this._outputStream.write(1);
         this._outputStream.write(major);
         this._outputStream.write(minor);

         try {
            this._outputStream.flush();
         } catch (SSLHandshakeException e) {
            LOGGER.error("TLS connection failed: {}", e.getMessage());
            throw e;
         }
      } finally {
         this._outputStreamLock.unlock();
      }
   }

   public void sendHeader(int major, int minor, int revision) throws IOException {
      this._outputStreamLock.lock();

      try {
         this._outputStream.write("AMQP".getBytes("US-ASCII"));
         this._outputStream.write(0);
         this._outputStream.write(major);
         this._outputStream.write(minor);
         this._outputStream.write(revision);

         try {
            this._outputStream.flush();
         } catch (SSLHandshakeException e) {
            LOGGER.error("TLS connection failed: {}", e.getMessage());
            throw e;
         }
      } finally {
         this._outputStreamLock.unlock();
      }
   }

   @Override
   public void sendHeader() throws IOException {
      this.sendHeader(0, 9, 1);
      if (this._socket instanceof SSLSocket) {
         TlsUtils.logPeerCertificateInfo(((SSLSocket)this._socket).getSession());
      }
   }

   @Override
   public void initialize(AMQConnection connection) {
      connection.startMainLoop();
   }

   @Override
   public Frame readFrame() throws IOException {
      this._inputStreamLock.lock();

      try {
         return Frame.readFrom(this._inputStream, this.maxInboundMessageBodySize);
      } finally {
         this._inputStreamLock.unlock();
      }
   }

   @Override
   public void writeFrame(Frame frame) throws IOException {
      this._outputStreamLock.lock();

      try {
         frame.writeTo(this._outputStream);
      } finally {
         this._outputStreamLock.unlock();
      }
   }

   @Override
   public void flush() throws IOException {
      this._outputStream.flush();
   }

   @Override
   public void close() {
      try {
         this._socket.setSoLinger(true, 1);
      } catch (Exception var5) {
      }

      Callable<Void> flushCallable = new Callable<Void>() {
         public Void call() throws Exception {
            SocketFrameHandler.this.flush();
            return null;
         }
      };
      Future<Void> flushTask = null;

      try {
         if (this._shutdownExecutor == null) {
            flushCallable.call();
         } else {
            flushTask = this._shutdownExecutor.submit(flushCallable);
            flushTask.get(1L, TimeUnit.SECONDS);
         }
      } catch (Exception e) {
         if (flushTask != null) {
            flushTask.cancel(true);
         }
      }

      try {
         this._socket.close();
      } catch (Exception var4) {
      }
   }
}
