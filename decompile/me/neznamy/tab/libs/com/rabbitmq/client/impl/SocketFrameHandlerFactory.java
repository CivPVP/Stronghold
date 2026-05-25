package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import javax.net.SocketFactory;
import me.neznamy.tab.libs.com.rabbitmq.client.Address;
import me.neznamy.tab.libs.com.rabbitmq.client.ConnectionFactory;
import me.neznamy.tab.libs.com.rabbitmq.client.SocketConfigurator;
import me.neznamy.tab.libs.com.rabbitmq.client.SslContextFactory;

public class SocketFrameHandlerFactory extends AbstractFrameHandlerFactory {
   private final SocketFactory socketFactory;
   private final ExecutorService shutdownExecutor;
   private final SslContextFactory sslContextFactory;

   public SocketFrameHandlerFactory(int connectionTimeout, SocketFactory socketFactory, SocketConfigurator configurator, boolean ssl) {
      this(connectionTimeout, socketFactory, configurator, ssl, null);
   }

   public SocketFrameHandlerFactory(
      int connectionTimeout, SocketFactory socketFactory, SocketConfigurator configurator, boolean ssl, ExecutorService shutdownExecutor
   ) {
      this(connectionTimeout, socketFactory, configurator, ssl, shutdownExecutor, null, Integer.MAX_VALUE);
   }

   public SocketFrameHandlerFactory(
      int connectionTimeout,
      SocketFactory socketFactory,
      SocketConfigurator configurator,
      boolean ssl,
      ExecutorService shutdownExecutor,
      SslContextFactory sslContextFactory,
      int maxInboundMessageBodySize
   ) {
      super(connectionTimeout, configurator, ssl, maxInboundMessageBodySize);
      this.socketFactory = socketFactory;
      this.shutdownExecutor = shutdownExecutor;
      this.sslContextFactory = sslContextFactory;
   }

   @Override
   public FrameHandler create(Address addr, String connectionName) throws IOException {
      int portNumber = ConnectionFactory.portOrDefault(addr.getPort(), this.ssl);
      Socket socket = null;

      try {
         socket = this.createSocket(connectionName);
         this.configurator.configure(socket);
         socket.connect(addr.toInetSocketAddress(portNumber), this.connectionTimeout);
         return this.create(socket);
      } catch (IOException ioe) {
         quietTrySocketClose(socket);
         throw ioe;
      }
   }

   protected Socket createSocket(String connectionName) throws IOException {
      if (this.socketFactory != null) {
         return this.socketFactory.createSocket();
      } else {
         return this.ssl ? this.sslContextFactory.create(connectionName).getSocketFactory().createSocket() : SocketFactory.getDefault().createSocket();
      }
   }

   public FrameHandler create(Socket sock) throws IOException {
      return new SocketFrameHandler(sock, this.shutdownExecutor, this.maxInboundMessageBodySize);
   }

   private static void quietTrySocketClose(Socket socket) {
      if (socket != null) {
         try {
            socket.close();
         } catch (Exception var2) {
         }
      }
   }
}
