package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.params.GetExParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.LCSParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.SetParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LCSMatchResult;

public interface StringPipelineBinaryCommands extends BitPipelineBinaryCommands {
   Response<String> set(byte[] var1, byte[] var2);

   Response<String> set(byte[] var1, byte[] var2, SetParams var3);

   Response<byte[]> get(byte[] var1);

   Response<byte[]> setGet(byte[] var1, byte[] var2);

   Response<byte[]> setGet(byte[] var1, byte[] var2, SetParams var3);

   Response<byte[]> getDel(byte[] var1);

   Response<byte[]> getEx(byte[] var1, GetExParams var2);

   Response<Long> setrange(byte[] var1, long var2, byte[] var4);

   Response<byte[]> getrange(byte[] var1, long var2, long var4);

   @Deprecated
   Response<byte[]> getSet(byte[] var1, byte[] var2);

   Response<Long> setnx(byte[] var1, byte[] var2);

   Response<String> setex(byte[] var1, long var2, byte[] var4);

   Response<String> psetex(byte[] var1, long var2, byte[] var4);

   Response<List<byte[]>> mget(byte[]... var1);

   Response<String> mset(byte[]... var1);

   Response<Long> msetnx(byte[]... var1);

   Response<Long> incr(byte[] var1);

   Response<Long> incrBy(byte[] var1, long var2);

   Response<Double> incrByFloat(byte[] var1, double var2);

   Response<Long> decr(byte[] var1);

   Response<Long> decrBy(byte[] var1, long var2);

   Response<Long> append(byte[] var1, byte[] var2);

   Response<byte[]> substr(byte[] var1, int var2, int var3);

   Response<Long> strlen(byte[] var1);

   Response<LCSMatchResult> lcs(byte[] var1, byte[] var2, LCSParams var3);
}
