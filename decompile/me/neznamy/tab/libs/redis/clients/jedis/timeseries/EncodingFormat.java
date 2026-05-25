package me.neznamy.tab.libs.redis.clients.jedis.timeseries;

import me.neznamy.tab.libs.redis.clients.jedis.args.Rawable;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public enum EncodingFormat implements Rawable {
   COMPRESSED,
   UNCOMPRESSED;

   private final byte[] raw = SafeEncoder.encode(this.name());

   @Override
   public byte[] getRaw() {
      return this.raw;
   }
}
