package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.params.GetExParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.LCSParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.SetParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LCSMatchResult;

public interface StringCommands extends BitCommands {
   String set(String var1, String var2);

   String set(String var1, String var2, SetParams var3);

   String get(String var1);

   String setGet(String var1, String var2);

   String setGet(String var1, String var2, SetParams var3);

   String getDel(String var1);

   String getEx(String var1, GetExParams var2);

   long setrange(String var1, long var2, String var4);

   String getrange(String var1, long var2, long var4);

   @Deprecated
   String getSet(String var1, String var2);

   long setnx(String var1, String var2);

   String setex(String var1, long var2, String var4);

   String psetex(String var1, long var2, String var4);

   List<String> mget(String... var1);

   String mset(String... var1);

   long msetnx(String... var1);

   long incr(String var1);

   long incrBy(String var1, long var2);

   double incrByFloat(String var1, double var2);

   long decr(String var1);

   long decrBy(String var1, long var2);

   long append(String var1, String var2);

   String substr(String var1, int var2, int var3);

   long strlen(String var1);

   LCSMatchResult lcs(String var1, String var2, LCSParams var3);
}
