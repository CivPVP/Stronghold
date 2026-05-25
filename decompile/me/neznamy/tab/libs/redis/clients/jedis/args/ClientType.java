package me.neznamy.tab.libs.redis.clients.jedis.args;

import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public enum ClientType implements Rawable {
   NORMAL,
   MASTER,
   SLAVE,
   REPLICA,
   PUBSUB;

   private final byte[] raw = SafeEncoder.encode(this.name().toLowerCase());

   @Override
   public byte[] getRaw() {
      return this.raw;
   }
}
