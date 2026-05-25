package me.neznamy.tab.libs.redis.clients.jedis.executors;

import me.neznamy.tab.libs.redis.clients.jedis.CommandObject;
import me.neznamy.tab.libs.redis.clients.jedis.Connection;
import me.neznamy.tab.libs.redis.clients.jedis.util.IOUtils;

public class SimpleCommandExecutor implements CommandExecutor {
   protected final Connection connection;

   public SimpleCommandExecutor(Connection connection) {
      this.connection = connection;
   }

   @Override
   public void close() {
      IOUtils.closeQuietly(this.connection);
   }

   @Override
   public final <T> T executeCommand(CommandObject<T> commandObject) {
      return this.connection.executeCommand(commandObject);
   }
}
