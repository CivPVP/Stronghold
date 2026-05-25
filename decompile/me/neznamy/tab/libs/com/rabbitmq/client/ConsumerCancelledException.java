package me.neznamy.tab.libs.com.rabbitmq.client;

import me.neznamy.tab.libs.com.rabbitmq.utility.SensibleClone;

public class ConsumerCancelledException extends RuntimeException implements SensibleClone<ConsumerCancelledException> {
   private static final long serialVersionUID = 1L;

   public ConsumerCancelledException sensibleClone() {
      try {
         return (ConsumerCancelledException)super.clone();
      } catch (CloneNotSupportedException e) {
         throw new RuntimeException(e);
      }
   }
}
