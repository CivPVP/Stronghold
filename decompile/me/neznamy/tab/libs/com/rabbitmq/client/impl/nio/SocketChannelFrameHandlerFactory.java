package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import me.neznamy.tab.libs.com.rabbitmq.client.Address;
import me.neznamy.tab.libs.com.rabbitmq.client.ConnectionFactory;
import me.neznamy.tab.libs.com.rabbitmq.client.SslContextFactory;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.AbstractFrameHandlerFactory;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.FrameHandler;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.TlsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketChannelFrameHandlerFactory extends AbstractFrameHandlerFactory {
   private static final Logger LOGGER = LoggerFactory.getLogger(SocketChannelFrameHandler.class);
   final NioParams nioParams;
   private final SslContextFactory sslContextFactory;
   private final Lock stateLock = new ReentrantLock();
   private final AtomicLong globalConnectionCount = new AtomicLong();
   private final List<NioLoopContext> nioLoopContexts;

   public SocketChannelFrameHandlerFactory(
      int connectionTimeout, NioParams nioParams, boolean ssl, SslContextFactory sslContextFactory, int maxInboundMessageBodySize
   ) {
      super(connectionTimeout, null, ssl, maxInboundMessageBodySize);
      this.nioParams = new NioParams(nioParams);
      this.sslContextFactory = sslContextFactory;
      this.nioLoopContexts = new ArrayList<>(this.nioParams.getNbIoThreads());

      for (int i = 0; i < this.nioParams.getNbIoThreads(); i++) {
         this.nioLoopContexts.add(new NioLoopContext(this, this.nioParams));
      }
   }

   @Override
   public FrameHandler create(Address addr, String connectionName) throws IOException {
      int portNumber = ConnectionFactory.portOrDefault(addr.getPort(), this.ssl);
      SSLEngine sslEngine = null;
      SocketChannel channel = null;

      try {
         if (this.ssl) {
            SSLContext sslContext = this.sslContextFactory.create(connectionName);
            sslEngine = sslContext.createSSLEngine(addr.getHost(), portNumber);
            sslEngine.setUseClientMode(true);
            if (this.nioParams.getSslEngineConfigurator() != null) {
               this.nioParams.getSslEngineConfigurator().configure(sslEngine);
            }
         }

         SocketAddress address = addr.toInetSocketAddress(portNumber);
         channel = SocketChannel.open();
         channel.configureBlocking(true);
         if (this.nioParams.getSocketChannelConfigurator() != null) {
            this.nioParams.getSocketChannelConfigurator().configure(channel);
         }

         channel.socket().connect(address, this.connectionTimeout);
         if (this.ssl) {
            int initialSoTimeout = channel.socket().getSoTimeout();
            channel.socket().setSoTimeout(this.connectionTimeout);
            sslEngine.beginHandshake();

            try {
               ReadableByteChannel wrappedReadChannel = Channels.newChannel(channel.socket().getInputStream());
               WritableByteChannel wrappedWriteChannel = Channels.newChannel(channel.socket().getOutputStream());
               boolean handshake = SslEngineHelper.doHandshake(wrappedWriteChannel, wrappedReadChannel, sslEngine);
               if (!handshake) {
                  LOGGER.error("TLS connection failed");
                  throw new SSLException("TLS handshake failed");
               }

               channel.socket().setSoTimeout(initialSoTimeout);
            } catch (SSLHandshakeException e) {
               LOGGER.error("TLS connection failed: {}", e.getMessage());
               throw e;
            }

            TlsUtils.logPeerCertificateInfo(sslEngine.getSession());
         }

         channel.configureBlocking(false);
         this.stateLock.lock();
         NioLoopContext nioLoopContext = null;

         try {
            long modulo = this.globalConnectionCount.getAndIncrement() % this.nioParams.getNbIoThreads();
            nioLoopContext = this.nioLoopContexts.get((int)modulo);
            nioLoopContext.initStateIfNecessary();
            SocketChannelFrameHandlerState state = new SocketChannelFrameHandlerState(
               channel, nioLoopContext, this.nioParams, sslEngine, this.maxInboundMessageBodySize
            );
            state.startReading();
            return new SocketChannelFrameHandler(state);
         } finally {
            this.stateLock.unlock();
         }
      } catch (IOException e) {
         try {
            if (sslEngine != null && channel != null) {
               SslEngineHelper.close(channel, sslEngine);
            }

            if (channel != null) {
               channel.close();
            }
         } catch (IOException var18) {
         }

         throw e;
      }
   }

   void lock() {
      this.stateLock.lock();
   }

   void unlock() {
      this.stateLock.unlock();
   }
}
