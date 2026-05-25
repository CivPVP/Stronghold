package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

@FunctionalInterface
public interface SocketConfigurator {
   void configure(Socket var1) throws IOException;

   default SocketConfigurator andThen(SocketConfigurator after) {
      Objects.requireNonNull(after);
      return t -> {
         this.configure(t);
         after.configure(t);
      };
   }
}
