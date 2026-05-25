package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Set;
import me.neznamy.tab.libs.redis.clients.jedis.params.ScanParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ScanResult;

public interface SetCommands {
   long sadd(String var1, String... var2);

   Set<String> smembers(String var1);

   long srem(String var1, String... var2);

   String spop(String var1);

   Set<String> spop(String var1, long var2);

   long scard(String var1);

   boolean sismember(String var1, String var2);

   List<Boolean> smismember(String var1, String... var2);

   String srandmember(String var1);

   List<String> srandmember(String var1, int var2);

   default ScanResult<String> sscan(String key, String cursor) {
      return this.sscan(key, cursor, new ScanParams());
   }

   ScanResult<String> sscan(String var1, String var2, ScanParams var3);

   Set<String> sdiff(String... var1);

   long sdiffstore(String var1, String... var2);

   Set<String> sinter(String... var1);

   long sinterstore(String var1, String... var2);

   long sintercard(String... var1);

   long sintercard(int var1, String... var2);

   Set<String> sunion(String... var1);

   long sunionstore(String var1, String... var2);

   long smove(String var1, String var2, String var3);
}
