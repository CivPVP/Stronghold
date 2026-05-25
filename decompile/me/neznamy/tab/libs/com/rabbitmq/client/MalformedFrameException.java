package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;

public class MalformedFrameException extends IOException {
   private static final long serialVersionUID = 1L;

   public MalformedFrameException(String reason) {
      super(reason);
   }
}
