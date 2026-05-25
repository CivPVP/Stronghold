package me.neznamy.tab.libs.redis.clients.jedis.commands;

public interface HyperLogLogCommands {
   long pfadd(String var1, String... var2);

   String pfmerge(String var1, String... var2);

   long pfcount(String var1);

   long pfcount(String... var1);
}
