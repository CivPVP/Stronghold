package me.neznamy.tab.libs.redis.clients.jedis.executors;

import me.neznamy.tab.libs.redis.clients.jedis.CommandObject;
import me.neznamy.tab.libs.redis.clients.jedis.Connection;
import me.neznamy.tab.libs.redis.clients.jedis.providers.ConnectionProvider;
import me.neznamy.tab.libs.redis.clients.jedis.util.IOUtils;

public class DefaultCommandExecutor implements CommandExecutor {
   protected final ConnectionProvider provider;

   public DefaultCommandExecutor(ConnectionProvider provider) {
      this.provider = provider;
   }

   @Override
   public void close() {
      IOUtils.closeQuietly(this.provider);
   }

   @Override
   public final <T> T executeCommand(CommandObject<T> commandObject) {
      try (Connection connection = this.provider.getConnection(commandObject.getArguments())) {
         return connection.executeCommand(commandObject);
      }
   }
}
