package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Objects;

@FunctionalInterface
public interface SocketChannelConfigurator {
   void configure(SocketChannel var1) throws IOException;

   default SocketChannelConfigurator andThen(SocketChannelConfigurator after) {
      Objects.requireNonNull(after);
      return t -> {
         this.configure(t);
         after.configure(t);
      };
   }
}
