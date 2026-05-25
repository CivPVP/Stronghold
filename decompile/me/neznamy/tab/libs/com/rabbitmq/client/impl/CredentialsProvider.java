package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.time.Duration;

public interface CredentialsProvider {
   String getUsername();

   String getPassword();

   default Duration getTimeBeforeExpiration() {
      return null;
   }

   default void refresh() {
   }
}
