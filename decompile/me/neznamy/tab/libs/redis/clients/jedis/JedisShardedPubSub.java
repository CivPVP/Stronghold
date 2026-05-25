package me.neznamy.tab.libs.redis.clients.jedis;

import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public abstract class JedisShardedPubSub extends JedisShardedPubSubBase<String> {
   protected final String encode(byte[] raw) {
      return SafeEncoder.encode(raw);
   }
}
