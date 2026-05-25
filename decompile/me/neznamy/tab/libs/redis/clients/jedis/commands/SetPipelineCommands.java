package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Set;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.params.ScanParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ScanResult;

public interface SetPipelineCommands {
   Response<Long> sadd(String var1, String... var2);

   Response<Set<String>> smembers(String var1);

   Response<Long> srem(String var1, String... var2);

   Response<String> spop(String var1);

   Response<Set<String>> spop(String var1, long var2);

   Response<Long> scard(String var1);

   Response<Boolean> sismember(String var1, String var2);

   Response<List<Boolean>> smismember(String var1, String... var2);

   Response<String> srandmember(String var1);

   Response<List<String>> srandmember(String var1, int var2);

   default Response<ScanResult<String>> sscan(String key, String cursor) {
      return this.sscan(key, cursor, new ScanParams());
   }

   Response<ScanResult<String>> sscan(String var1, String var2, ScanParams var3);

   Response<Set<String>> sdiff(String... var1);

   Response<Long> sdiffstore(String var1, String... var2);

   @Deprecated
   default Response<Long> sdiffStore(String dstKey, String... keys) {
      return this.sdiffstore(dstKey, keys);
   }

   Response<Set<String>> sinter(String... var1);

   Response<Long> sinterstore(String var1, String... var2);

   Response<Long> sintercard(String... var1);

   Response<Long> sintercard(int var1, String... var2);

   Response<Set<String>> sunion(String... var1);

   Response<Long> sunionstore(String var1, String... var2);

   Response<Long> smove(String var1, String var2, String var3);
}
