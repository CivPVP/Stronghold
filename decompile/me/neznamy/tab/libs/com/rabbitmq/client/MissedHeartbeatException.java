package me.neznamy.tab.libs.com.rabbitmq.client;

import java.net.SocketTimeoutException;

public class MissedHeartbeatException extends SocketTimeoutException {
   private static final long serialVersionUID = 1L;

   public MissedHeartbeatException(String reason) {
      super(reason);
   }
}
