package me.neznamy.tab.libs.redis.clients.jedis.args;

import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public enum SaveMode implements Rawable {
   NOSAVE,
   SAVE;

   private final byte[] raw = SafeEncoder.encode(this.name());

   @Override
   public byte[] getRaw() {
      return this.raw;
   }
}
