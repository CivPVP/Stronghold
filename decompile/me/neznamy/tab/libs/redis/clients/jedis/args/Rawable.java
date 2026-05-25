package me.neznamy.tab.libs.redis.clients.jedis.args;

public interface Rawable {
   byte[] getRaw();

   @Override
   int hashCode();

   @Override
   boolean equals(Object var1);
}
