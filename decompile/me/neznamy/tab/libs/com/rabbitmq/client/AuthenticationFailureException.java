package me.neznamy.tab.libs.com.rabbitmq.client;

public class AuthenticationFailureException extends PossibleAuthenticationFailureException {
   public AuthenticationFailureException(String reason) {
      super(reason);
   }
}
