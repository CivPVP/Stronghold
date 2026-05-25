package me.neznamy.tab.libs.com.rabbitmq.client.impl;

public class WorkPoolFullException extends RuntimeException {
   public WorkPoolFullException(String msg) {
      super(msg);
   }
}
