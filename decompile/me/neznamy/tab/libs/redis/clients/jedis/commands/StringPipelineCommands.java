package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.params.GetExParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.LCSParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.SetParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LCSMatchResult;

public interface StringPipelineCommands extends BitPipelineCommands {
   Response<String> set(String var1, String var2);

   Response<String> set(String var1, String var2, SetParams var3);

   Response<String> get(String var1);

   Response<String> setGet(String var1, String var2);

   Response<String> setGet(String var1, String var2, SetParams var3);

   Response<String> getDel(String var1);

   Response<String> getEx(String var1, GetExParams var2);

   Response<Long> setrange(String var1, long var2, String var4);

   Response<String> getrange(String var1, long var2, long var4);

   @Deprecated
   Response<String> getSet(String var1, String var2);

   Response<Long> setnx(String var1, String var2);

   Response<String> setex(String var1, long var2, String var4);

   Response<String> psetex(String var1, long var2, String var4);

   Response<List<String>> mget(String... var1);

   Response<String> mset(String... var1);

   Response<Long> msetnx(String... var1);

   Response<Long> incr(String var1);

   Response<Long> incrBy(String var1, long var2);

   Response<Double> incrByFloat(String var1, double var2);

   Response<Long> decr(String var1);

   Response<Long> decrBy(String var1, long var2);

   Response<Long> append(String var1, String var2);

   Response<String> substr(String var1, int var2, int var3);

   Response<Long> strlen(String var1);

   Response<LCSMatchResult> lcs(String var1, String var2, LCSParams var3);
}
