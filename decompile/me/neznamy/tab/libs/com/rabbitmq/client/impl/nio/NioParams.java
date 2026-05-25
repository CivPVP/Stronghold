package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;
import me.neznamy.tab.libs.com.rabbitmq.client.SocketChannelConfigurator;
import me.neznamy.tab.libs.com.rabbitmq.client.SocketChannelConfigurators;
import me.neznamy.tab.libs.com.rabbitmq.client.SslEngineConfigurator;
import me.neznamy.tab.libs.com.rabbitmq.client.SslEngineConfigurators;

public class NioParams {
   static Function<NioContext, NioQueue> DEFAULT_WRITE_QUEUE_FACTORY = ctx -> new BlockingQueueNioQueue(
      new ArrayBlockingQueue<>(ctx.getNioParams().getWriteQueueCapacity(), true), ctx.getNioParams().getWriteEnqueuingTimeoutInMs()
   );
   private int readByteBufferSize = 32768;
   private int writeByteBufferSize = 32768;
   private int nbIoThreads = 1;
   private int writeEnqueuingTimeoutInMs = 10000;
   private int writeQueueCapacity = 10000;
   private ExecutorService nioExecutor;
   private ThreadFactory threadFactory;
   private SocketChannelConfigurator socketChannelConfigurator = SocketChannelConfigurators.defaultConfigurator();
   private SslEngineConfigurator sslEngineConfigurator = sslEngine -> {};
   private ExecutorService connectionShutdownExecutor;
   private ByteBufferFactory byteBufferFactory = new DefaultByteBufferFactory();
   private Function<NioContext, NioQueue> writeQueueFactory = DEFAULT_WRITE_QUEUE_FACTORY;

   public NioParams() {
   }

   public NioParams(NioParams nioParams) {
      this.setReadByteBufferSize(nioParams.getReadByteBufferSize());
      this.setWriteByteBufferSize(nioParams.getWriteByteBufferSize());
      this.setNbIoThreads(nioParams.getNbIoThreads());
      this.setWriteEnqueuingTimeoutInMs(nioParams.getWriteEnqueuingTimeoutInMs());
      this.setWriteQueueCapacity(nioParams.getWriteQueueCapacity());
      this.setNioExecutor(nioParams.getNioExecutor());
      this.setThreadFactory(nioParams.getThreadFactory());
      this.setSocketChannelConfigurator(nioParams.getSocketChannelConfigurator());
      this.setSslEngineConfigurator(nioParams.getSslEngineConfigurator());
      this.setConnectionShutdownExecutor(nioParams.getConnectionShutdownExecutor());
      this.setByteBufferFactory(nioParams.getByteBufferFactory());
      this.setWriteQueueFactory(nioParams.getWriteQueueFactory());
   }

   public NioParams enableHostnameVerification() {
      if (this.sslEngineConfigurator == null) {
         this.sslEngineConfigurator = SslEngineConfigurators.ENABLE_HOSTNAME_VERIFICATION;
      } else {
         this.sslEngineConfigurator = this.sslEngineConfigurator.andThen(SslEngineConfigurators.ENABLE_HOSTNAME_VERIFICATION);
      }

      return this;
   }

   public int getReadByteBufferSize() {
      return this.readByteBufferSize;
   }

   public NioParams setReadByteBufferSize(int readByteBufferSize) {
      if (readByteBufferSize <= 0) {
         throw new IllegalArgumentException("Buffer size must be greater than 0");
      }

      this.readByteBufferSize = readByteBufferSize;
      return this;
   }

   public int getWriteByteBufferSize() {
      return this.writeByteBufferSize;
   }

   public NioParams setWriteByteBufferSize(int writeByteBufferSize) {
      if (writeByteBufferSize <= 0) {
         throw new IllegalArgumentException("Buffer size must be greater than 0");
      }

      this.writeByteBufferSize = writeByteBufferSize;
      return this;
   }

   public int getNbIoThreads() {
      return this.nbIoThreads;
   }

   public NioParams setNbIoThreads(int nbIoThreads) {
      if (nbIoThreads <= 0) {
         throw new IllegalArgumentException("Number of threads must be greater than 0");
      }

      this.nbIoThreads = nbIoThreads;
      return this;
   }

   public int getWriteEnqueuingTimeoutInMs() {
      return this.writeEnqueuingTimeoutInMs;
   }

   public NioParams setWriteEnqueuingTimeoutInMs(int writeEnqueuingTimeoutInMs) {
      this.writeEnqueuingTimeoutInMs = writeEnqueuingTimeoutInMs;
      return this;
   }

   public ExecutorService getNioExecutor() {
      return this.nioExecutor;
   }

   public NioParams setNioExecutor(ExecutorService nioExecutor) {
      this.nioExecutor = nioExecutor;
      return this;
   }

   public ThreadFactory getThreadFactory() {
      return this.threadFactory;
   }

   public NioParams setThreadFactory(ThreadFactory threadFactory) {
      this.threadFactory = threadFactory;
      return this;
   }

   public int getWriteQueueCapacity() {
      return this.writeQueueCapacity;
   }

   public NioParams setWriteQueueCapacity(int writeQueueCapacity) {
      if (writeQueueCapacity <= 0) {
         throw new IllegalArgumentException("Write queue capacity must be greater than 0");
      }

      this.writeQueueCapacity = writeQueueCapacity;
      return this;
   }

   public SocketChannelConfigurator getSocketChannelConfigurator() {
      return this.socketChannelConfigurator;
   }

   public void setSocketChannelConfigurator(SocketChannelConfigurator configurator) {
      this.socketChannelConfigurator = configurator;
   }

   public SslEngineConfigurator getSslEngineConfigurator() {
      return this.sslEngineConfigurator;
   }

   public void setSslEngineConfigurator(SslEngineConfigurator configurator) {
      this.sslEngineConfigurator = configurator;
   }

   public ExecutorService getConnectionShutdownExecutor() {
      return this.connectionShutdownExecutor;
   }

   public NioParams setConnectionShutdownExecutor(ExecutorService connectionShutdownExecutor) {
      this.connectionShutdownExecutor = connectionShutdownExecutor;
      return this;
   }

   public NioParams setByteBufferFactory(ByteBufferFactory byteBufferFactory) {
      this.byteBufferFactory = byteBufferFactory;
      return this;
   }

   public ByteBufferFactory getByteBufferFactory() {
      return this.byteBufferFactory;
   }

   public NioParams setWriteQueueFactory(Function<NioContext, NioQueue> writeQueueFactory) {
      this.writeQueueFactory = writeQueueFactory;
      return this;
   }

   public Function<NioContext, NioQueue> getWriteQueueFactory() {
      return this.writeQueueFactory;
   }
}
