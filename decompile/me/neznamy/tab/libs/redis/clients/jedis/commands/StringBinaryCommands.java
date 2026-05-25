package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.params.GetExParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.LCSParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.SetParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LCSMatchResult;

public interface StringBinaryCommands extends BitBinaryCommands {
   String set(byte[] var1, byte[] var2);

   String set(byte[] var1, byte[] var2, SetParams var3);

   byte[] get(byte[] var1);

   byte[] setGet(byte[] var1, byte[] var2);

   byte[] setGet(byte[] var1, byte[] var2, SetParams var3);

   byte[] getDel(byte[] var1);

   byte[] getEx(byte[] var1, GetExParams var2);

   long setrange(byte[] var1, long var2, byte[] var4);

   byte[] getrange(byte[] var1, long var2, long var4);

   @Deprecated
   byte[] getSet(byte[] var1, byte[] var2);

   long setnx(byte[] var1, byte[] var2);

   String setex(byte[] var1, long var2, byte[] var4);

   String psetex(byte[] var1, long var2, byte[] var4);

   List<byte[]> mget(byte[]... var1);

   String mset(byte[]... var1);

   long msetnx(byte[]... var1);

   long incr(byte[] var1);

   long incrBy(byte[] var1, long var2);

   double incrByFloat(byte[] var1, double var2);

   long decr(byte[] var1);

   long decrBy(byte[] var1, long var2);

   long append(byte[] var1, byte[] var2);

   byte[] substr(byte[] var1, int var2, int var3);

   long strlen(byte[] var1);

   LCSMatchResult lcs(byte[] var1, byte[] var2, LCSParams var3);
}
