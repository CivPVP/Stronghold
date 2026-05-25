package me.neznamy.tab.libs.redis.clients.jedis;

public abstract class BinaryJedisPubSub extends JedisPubSubBase<byte[]> {
   protected final byte[] encode(byte[] raw) {
      return raw;
   }
}
