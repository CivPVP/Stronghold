package me.neznamy.tab.libs.redis.clients.jedis.args;

import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public enum FunctionRestorePolicy implements Rawable {
   FLUSH,
   APPEND,
   REPLACE;

   private final byte[] raw = SafeEncoder.encode(this.name());

   @Override
   public byte[] getRaw() {
      return this.raw;
   }
}
