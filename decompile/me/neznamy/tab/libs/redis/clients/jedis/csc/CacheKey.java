package me.neznamy.tab.libs.redis.clients.jedis.csc;

import java.util.List;
import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandObject;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ProtocolCommand;

public class CacheKey<T> {
   private final CommandObject<T> command;

   public CacheKey(CommandObject<T> command) {
      this.command = Objects.requireNonNull(command);
   }

   @Override
   public int hashCode() {
      return this.command.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         CacheKey other = (CacheKey)obj;
         return Objects.equals(this.command, other.command);
      } else {
         return false;
      }
   }

   public List<Object> getRedisKeys() {
      return this.command.getArguments().getKeys();
   }

   public ProtocolCommand getRedisCommand() {
      return this.command.getArguments().getCommand();
   }
}
