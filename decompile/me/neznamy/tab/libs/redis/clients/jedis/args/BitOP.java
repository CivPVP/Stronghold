package me.neznamy.tab.libs.redis.clients.jedis.args;

import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public enum BitOP implements Rawable {
   AND,
   OR,
   XOR,
   NOT;

   private final byte[] raw = SafeEncoder.encode(this.name());

   @Override
   public byte[] getRaw() {
      return this.raw;
   }
}
