package me.neznamy.tab.libs.com.rabbitmq.client.impl;

final class Utils {
   private static final int AVAILABLE_PROCESSORS = Integer.parseInt(
      System.getProperty("rabbitmq.amqp.client.availableProcessors", String.valueOf(Runtime.getRuntime().availableProcessors()))
   );

   static int availableProcessors() {
      return AVAILABLE_PROCESSORS;
   }

   private Utils() {
   }
}
