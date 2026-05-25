package me.neznamy.tab.libs.redis.clients.jedis.args;

import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public enum ClientAttributeOption implements Rawable {
   LIB_NAME("LIB-NAME"),
   LIB_VER("LIB-VER");

   private final byte[] raw;

   ClientAttributeOption(String str) {
      this.raw = SafeEncoder.encode(str);
   }

   @Override
   public byte[] getRaw() {
      return this.raw;
   }
}
