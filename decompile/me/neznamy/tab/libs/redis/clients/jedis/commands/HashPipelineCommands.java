package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.args.ExpiryOption;
import me.neznamy.tab.libs.redis.clients.jedis.params.ScanParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ScanResult;

public interface HashPipelineCommands {
   Response<Long> hset(String var1, String var2, String var3);

   Response<Long> hset(String var1, Map<String, String> var2);

   Response<String> hget(String var1, String var2);

   Response<Long> hsetnx(String var1, String var2, String var3);

   Response<String> hmset(String var1, Map<String, String> var2);

   Response<List<String>> hmget(String var1, String... var2);

   Response<Long> hincrBy(String var1, String var2, long var3);

   Response<Double> hincrByFloat(String var1, String var2, double var3);

   Response<Boolean> hexists(String var1, String var2);

   Response<Long> hdel(String var1, String... var2);

   Response<Long> hlen(String var1);

   Response<Set<String>> hkeys(String var1);

   Response<List<String>> hvals(String var1);

   Response<Map<String, String>> hgetAll(String var1);

   Response<String> hrandfield(String var1);

   Response<List<String>> hrandfield(String var1, long var2);

   Response<List<Entry<String, String>>> hrandfieldWithValues(String var1, long var2);

   default Response<ScanResult<Entry<String, String>>> hscan(String key, String cursor) {
      return this.hscan(key, cursor, new ScanParams());
   }

   Response<ScanResult<Entry<String, String>>> hscan(String var1, String var2, ScanParams var3);

   default Response<ScanResult<String>> hscanNoValues(String key, String cursor) {
      return this.hscanNoValues(key, cursor, new ScanParams());
   }

   Response<ScanResult<String>> hscanNoValues(String var1, String var2, ScanParams var3);

   Response<Long> hstrlen(String var1, String var2);

   Response<List<Long>> hexpire(String var1, long var2, String... var4);

   Response<List<Long>> hexpire(String var1, long var2, ExpiryOption var4, String... var5);

   Response<List<Long>> hpexpire(String var1, long var2, String... var4);

   Response<List<Long>> hpexpire(String var1, long var2, ExpiryOption var4, String... var5);

   Response<List<Long>> hexpireAt(String var1, long var2, String... var4);

   Response<List<Long>> hexpireAt(String var1, long var2, ExpiryOption var4, String... var5);

   Response<List<Long>> hpexpireAt(String var1, long var2, String... var4);

   Response<List<Long>> hpexpireAt(String var1, long var2, ExpiryOption var4, String... var5);

   Response<List<Long>> hexpireTime(String var1, String... var2);

   Response<List<Long>> hpexpireTime(String var1, String... var2);

   Response<List<Long>> httl(String var1, String... var2);

   Response<List<Long>> hpttl(String var1, String... var2);

   Response<List<Long>> hpersist(String var1, String... var2);
}
