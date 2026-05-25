package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import javax.net.ssl.SSLEngine;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.AMQConnection;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketChannelFrameHandlerState {
   private static final Logger LOGGER = LoggerFactory.getLogger(SocketChannelFrameHandlerState.class);
   private static final int SOCKET_CLOSING_TIMEOUT = 1;
   private final SocketChannel channel;
   private final NioQueue writeQueue;
   private volatile AMQConnection connection;
   private volatile long heartbeatNanoSeconds = -1L;
   private long lastActivity;
   private final SelectorHolder writeSelectorState;
   private final SelectorHolder readSelectorState;
   final boolean ssl;
   final SSLEngine sslEngine;
   final ByteBuffer plainOut;
   final ByteBuffer plainIn;
   final ByteBuffer cipherOut;
   final ByteBuffer cipherIn;
   final DataOutputStream outputStream;
   final FrameBuilder frameBuilder;

   public SocketChannelFrameHandlerState(SocketChannel channel, NioLoopContext nioLoopsState, NioParams nioParams, SSLEngine sslEngine, int maxFramePayloadSize) {
      this.channel = channel;
      this.readSelectorState = nioLoopsState.readSelectorState;
      this.writeSelectorState = nioLoopsState.writeSelectorState;
      NioContext nioContext = new NioContext(nioParams, sslEngine);
      this.writeQueue = nioParams.getWriteQueueFactory() == null
         ? NioParams.DEFAULT_WRITE_QUEUE_FACTORY.apply(nioContext)
         : nioParams.getWriteQueueFactory().apply(nioContext);
      this.sslEngine = sslEngine;
      if (this.sslEngine == null) {
         this.ssl = false;
         this.plainOut = nioLoopsState.writeBuffer;
         this.cipherOut = null;
         this.plainIn = nioLoopsState.readBuffer;
         this.cipherIn = null;
         this.outputStream = new DataOutputStream(new ByteBufferOutputStream(channel, this.plainOut));
         this.frameBuilder = new FrameBuilder(channel, this.plainIn, maxFramePayloadSize);
      } else {
         this.ssl = true;
         this.plainOut = nioParams.getByteBufferFactory().createWriteBuffer(nioContext);
         this.cipherOut = nioParams.getByteBufferFactory().createEncryptedWriteBuffer(nioContext);
         this.plainIn = nioParams.getByteBufferFactory().createReadBuffer(nioContext);
         this.cipherIn = nioParams.getByteBufferFactory().createEncryptedReadBuffer(nioContext);
         this.outputStream = new DataOutputStream(new SslEngineByteBufferOutputStream(sslEngine, this.plainOut, this.cipherOut, channel));
         this.frameBuilder = new SslEngineFrameBuilder(sslEngine, this.plainIn, this.cipherIn, channel, maxFramePayloadSize);
      }
   }

   public SocketChannel getChannel() {
      return this.channel;
   }

   public NioQueue getWriteQueue() {
      return this.writeQueue;
   }

   public void sendHeader() throws IOException {
      this.sendWriteRequest(HeaderWriteRequest.SINGLETON);
   }

   public void write(Frame frame) throws IOException {
      this.sendWriteRequest(new FrameWriteRequest(frame));
   }

   private void sendWriteRequest(WriteRequest writeRequest) throws IOException {
      try {
         boolean offered = this.writeQueue.offer(writeRequest);
         if (!offered) {
            throw new IOException("Frame enqueuing failed");
         }

         this.writeSelectorState.registerFrameHandlerState(this, 4);
         this.readSelectorState.selector.wakeup();
      } catch (InterruptedException e) {
         LOGGER.warn("Thread interrupted during enqueuing frame in write queue");
         Thread.currentThread().interrupt();
      }
   }

   public void startReading() {
      this.readSelectorState.registerFrameHandlerState(this, 1);
   }

   public AMQConnection getConnection() {
      return this.connection;
   }

   public void setConnection(AMQConnection connection) {
      this.connection = connection;
   }

   void setHeartbeat(Duration ht) {
      this.heartbeatNanoSeconds = ht.toNanos();
   }

   public void setLastActivity(long lastActivity) {
      this.lastActivity = lastActivity;
   }

   public long getLastActivity() {
      return this.lastActivity;
   }

   long getHeartbeatNanoSeconds() {
      return this.heartbeatNanoSeconds;
   }

   void prepareForWriteSequence() {
      if (this.ssl) {
         ((Buffer)this.plainOut).clear();
         ((Buffer)this.cipherOut).clear();
      }
   }

   void endWriteSequence() {
      if (!this.ssl) {
         ((Buffer)this.plainOut).clear();
      }
   }

   void prepareForReadSequence() throws IOException {
      if (this.ssl) {
         if (!this.frameBuilder.isUnderflowHandlingEnabled()) {
            ((Buffer)this.cipherIn).clear();
            ((Buffer)this.cipherIn).flip();
         }

         ((Buffer)this.plainIn).clear();
         ((Buffer)this.plainIn).flip();
      } else {
         NioHelper.read(this.channel, this.plainIn);
         ((Buffer)this.plainIn).flip();
      }
   }

   boolean continueReading() throws IOException {
      if (this.ssl) {
         if (this.frameBuilder.isUnderflowHandlingEnabled()) {
            int bytesRead = NioHelper.read(this.channel, this.cipherIn);
            if (bytesRead == 0) {
               return false;
            }

            ((Buffer)this.cipherIn).flip();
            return true;
         } else if (!this.plainIn.hasRemaining() && !this.cipherIn.hasRemaining()) {
            ((Buffer)this.cipherIn).clear();
            int bytesRead = NioHelper.read(this.channel, this.cipherIn);
            if (bytesRead == 0) {
               return false;
            }

            ((Buffer)this.cipherIn).flip();
            return true;
         } else {
            return true;
         }
      } else {
         if (!this.plainIn.hasRemaining()) {
            ((Buffer)this.plainIn).clear();
            NioHelper.read(this.channel, this.plainIn);
            ((Buffer)this.plainIn).flip();
         }

         return this.plainIn.hasRemaining();
      }
   }

   void close() throws IOException {
      if (this.ssl) {
         SslEngineHelper.close(this.channel, this.sslEngine);
      }

      if (this.channel.isOpen()) {
         this.channel.socket().setSoLinger(true, 1);
         this.channel.close();
      }
   }
}
