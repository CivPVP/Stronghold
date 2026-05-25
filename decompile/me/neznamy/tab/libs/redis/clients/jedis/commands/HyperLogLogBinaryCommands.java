package me.neznamy.tab.libs.redis.clients.jedis.commands;

public interface HyperLogLogBinaryCommands {
   long pfadd(byte[] var1, byte[]... var2);

   String pfmerge(byte[] var1, byte[]... var2);

   long pfcount(byte[] var1);

   long pfcount(byte[]... var1);
}
