package me.neznamy.tab.libs.redis.clients.jedis.providers;

import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Connection;

public class ManagedConnectionProvider implements ConnectionProvider {
   private Connection connection;

   public final void setConnection(Connection connection) {
      this.connection = connection;
   }

   @Override
   public void close() {
   }

   @Override
   public final Connection getConnection() {
      return this.connection;
   }

   @Override
   public final Connection getConnection(CommandArguments args) {
      return this.connection;
   }
}
