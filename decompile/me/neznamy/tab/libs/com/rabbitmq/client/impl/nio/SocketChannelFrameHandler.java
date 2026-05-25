package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.Duration;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.AMQConnection;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.Frame;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.FrameHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketChannelFrameHandler implements FrameHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(SocketChannelFrameHandler.class);
   private final SocketChannelFrameHandlerState state;

   public SocketChannelFrameHandler(SocketChannelFrameHandlerState state) {
      this.state = state;
   }

   @Override
   public InetAddress getLocalAddress() {
      return this.state.getChannel().socket().getLocalAddress();
   }

   @Override
   public int getLocalPort() {
      return this.state.getChannel().socket().getLocalPort();
   }

   @Override
   public InetAddress getAddress() {
      return this.state.getChannel().socket().getInetAddress();
   }

   @Override
   public int getPort() {
      return this.state.getChannel().socket().getPort();
   }

   @Override
   public void setTimeout(int timeoutMs) throws SocketException {
      this.state.getChannel().socket().setSoTimeout(timeoutMs);
      if (this.state.getConnection() != null) {
         this.state.setHeartbeat(Duration.ofSeconds(this.state.getConnection().getHeartbeat()));
      }
   }

   @Override
   public int getTimeout() throws SocketException {
      return this.state.getChannel().socket().getSoTimeout();
   }

   @Override
   public void sendHeader() throws IOException {
      this.state.sendHeader();
   }

   @Override
   public void initialize(AMQConnection connection) {
      this.state.setConnection(connection);
   }

   @Override
   public Frame readFrame() throws IOException {
      throw new UnsupportedOperationException();
   }

   @Override
   public void writeFrame(Frame frame) throws IOException {
      this.state.write(frame);
   }

   @Override
   public void flush() throws IOException {
   }

   @Override
   public void close() {
      try {
         this.state.close();
      } catch (IOException e) {
         LOGGER.warn("Error while closing SocketChannel", e);
      }
   }

   public SocketChannelFrameHandlerState getState() {
      return this.state;
   }
}
