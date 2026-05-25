package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import me.neznamy.tab.libs.redis.clients.jedis.args.ExpiryOption;
import me.neznamy.tab.libs.redis.clients.jedis.params.ScanParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ScanResult;

public interface HashCommands {
   long hset(String var1, String var2, String var3);

   long hset(String var1, Map<String, String> var2);

   String hget(String var1, String var2);

   long hsetnx(String var1, String var2, String var3);

   String hmset(String var1, Map<String, String> var2);

   List<String> hmget(String var1, String... var2);

   long hincrBy(String var1, String var2, long var3);

   double hincrByFloat(String var1, String var2, double var3);

   boolean hexists(String var1, String var2);

   long hdel(String var1, String... var2);

   long hlen(String var1);

   Set<String> hkeys(String var1);

   List<String> hvals(String var1);

   Map<String, String> hgetAll(String var1);

   String hrandfield(String var1);

   List<String> hrandfield(String var1, long var2);

   List<Entry<String, String>> hrandfieldWithValues(String var1, long var2);

   default ScanResult<Entry<String, String>> hscan(String key, String cursor) {
      return this.hscan(key, cursor, new ScanParams());
   }

   ScanResult<Entry<String, String>> hscan(String var1, String var2, ScanParams var3);

   default ScanResult<String> hscanNoValues(String key, String cursor) {
      return this.hscanNoValues(key, cursor, new ScanParams());
   }

   ScanResult<String> hscanNoValues(String var1, String var2, ScanParams var3);

   long hstrlen(String var1, String var2);

   List<Long> hexpire(String var1, long var2, String... var4);

   List<Long> hexpire(String var1, long var2, ExpiryOption var4, String... var5);

   List<Long> hpexpire(String var1, long var2, String... var4);

   List<Long> hpexpire(String var1, long var2, ExpiryOption var4, String... var5);

   List<Long> hexpireAt(String var1, long var2, String... var4);

   List<Long> hexpireAt(String var1, long var2, ExpiryOption var4, String... var5);

   List<Long> hpexpireAt(String var1, long var2, String... var4);

   List<Long> hpexpireAt(String var1, long var2, ExpiryOption var4, String... var5);

   List<Long> hexpireTime(String var1, String... var2);

   List<Long> hpexpireTime(String var1, String... var2);

   List<Long> httl(String var1, String... var2);

   List<Long> hpttl(String var1, String... var2);

   List<Long> hpersist(String var1, String... var2);
}
