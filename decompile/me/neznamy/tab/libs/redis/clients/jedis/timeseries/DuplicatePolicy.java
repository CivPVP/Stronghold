package me.neznamy.tab.libs.redis.clients.jedis.timeseries;

import me.neznamy.tab.libs.redis.clients.jedis.args.Rawable;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public enum DuplicatePolicy implements Rawable {
   BLOCK,
   FIRST,
   LAST,
   MIN,
   MAX,
   SUM;

   private final byte[] raw = SafeEncoder.encode(this.name());

   @Override
   public byte[] getRaw() {
      return this.raw;
   }
}
