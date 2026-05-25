package me.neznamy.tab.libs.redis.clients.jedis.util;

import me.neznamy.tab.libs.redis.clients.jedis.args.Rawable;

public class LazyRawable implements Rawable {
   private byte[] raw = null;

   public void setRaw(byte[] raw) {
      this.raw = raw;
   }

   @Override
   public byte[] getRaw() {
      return this.raw;
   }
}
