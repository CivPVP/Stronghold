package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;
import java.util.Objects;
import javax.net.ssl.SSLEngine;

@FunctionalInterface
public interface SslEngineConfigurator {
   void configure(SSLEngine var1) throws IOException;

   default SslEngineConfigurator andThen(SslEngineConfigurator after) {
      Objects.requireNonNull(after);
      return t -> {
         this.configure(t);
         after.configure(t);
      };
   }
}
