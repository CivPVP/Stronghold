package me.neznamy.tab.libs.redis.clients.jedis.executors;

import me.neznamy.tab.libs.redis.clients.jedis.CommandObject;

public interface CommandExecutor extends AutoCloseable {
   <T> T executeCommand(CommandObject<T> var1);

   default <T> T broadcastCommand(CommandObject<T> commandObject) {
      return this.executeCommand(commandObject);
   }
}
