package me.neznamy.tab.libs.com.rabbitmq.client.impl;

public class OAuthTokenManagementException extends RuntimeException {
   public OAuthTokenManagementException(String message, Throwable cause) {
      super(message, cause);
   }

   public OAuthTokenManagementException(String message) {
      super(message);
   }
}
