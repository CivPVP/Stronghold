package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

final class Utils {
   private Utils() {
   }

   @FunctionalInterface
   interface IoTimeoutExceptionRunnable {
      void run() throws IOException, TimeoutException;
   }
}
