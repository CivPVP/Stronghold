package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class DefaultSocketChannelConfigurator implements SocketChannelConfigurator {
   @Override
   public void configure(SocketChannel socketChannel) throws IOException {
      socketChannel.socket().setTcpNoDelay(true);
   }
}
