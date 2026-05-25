package me.neznamy.tab.libs.redis.clients.jedis;

public abstract class BinaryJedisShardedPubSub extends JedisShardedPubSubBase<byte[]> {
   protected final byte[] encode(byte[] raw) {
      return raw;
   }
}
