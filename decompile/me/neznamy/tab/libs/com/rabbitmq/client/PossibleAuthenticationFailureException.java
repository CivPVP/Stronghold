package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;

public class PossibleAuthenticationFailureException extends IOException {
   private static final long serialVersionUID = 1L;

   public PossibleAuthenticationFailureException(Throwable cause) {
      super("Possibly caused by authentication failure");
      super.initCause(cause);
   }

   public PossibleAuthenticationFailureException(String reason) {
      super(reason);
   }
}
