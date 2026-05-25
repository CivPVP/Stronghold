package me.neznamy.tab.libs.com.rabbitmq.client;

public class UnroutableRpcRequestException extends RuntimeException {
   private final Return returnMessage;

   public UnroutableRpcRequestException(Return returnMessage) {
      this.returnMessage = returnMessage;
   }

   public Return getReturnMessage() {
      return this.returnMessage;
   }
}
