package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;
import java.net.Socket;

public class DefaultSocketConfigurator implements SocketConfigurator {
   @Override
   public void configure(Socket socket) throws IOException {
      socket.setTcpNoDelay(true);
   }
}
