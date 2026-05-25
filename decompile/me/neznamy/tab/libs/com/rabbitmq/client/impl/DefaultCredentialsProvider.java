package me.neznamy.tab.libs.com.rabbitmq.client.impl;

public class DefaultCredentialsProvider implements CredentialsProvider {
   private final String username;
   private final String password;

   public DefaultCredentialsProvider(String username, String password) {
      this.username = username;
      this.password = password;
   }

   @Override
   public String getUsername() {
      return this.username;
   }

   @Override
   public String getPassword() {
      return this.password;
   }
}
