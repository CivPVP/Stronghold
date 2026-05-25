package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.args.ListDirection;
import me.neznamy.tab.libs.redis.clients.jedis.args.ListPosition;
import me.neznamy.tab.libs.redis.clients.jedis.params.LPosParams;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public interface ListCommands {
   long rpush(String var1, String... var2);

   long lpush(String var1, String... var2);

   long llen(String var1);

   List<String> lrange(String var1, long var2, long var4);

   String ltrim(String var1, long var2, long var4);

   String lindex(String var1, long var2);

   String lset(String var1, long var2, String var4);

   long lrem(String var1, long var2, String var4);

   String lpop(String var1);

   List<String> lpop(String var1, int var2);

   Long lpos(String var1, String var2);

   Long lpos(String var1, String var2, LPosParams var3);

   List<Long> lpos(String var1, String var2, LPosParams var3, long var4);

   String rpop(String var1);

   List<String> rpop(String var1, int var2);

   long linsert(String var1, ListPosition var2, String var3, String var4);

   long lpushx(String var1, String... var2);

   long rpushx(String var1, String... var2);

   List<String> blpop(int var1, String... var2);

   List<String> blpop(int var1, String var2);

   KeyValue<String, String> blpop(double var1, String... var3);

   KeyValue<String, String> blpop(double var1, String var3);

   List<String> brpop(int var1, String... var2);

   List<String> brpop(int var1, String var2);

   KeyValue<String, String> brpop(double var1, String... var3);

   KeyValue<String, String> brpop(double var1, String var3);

   String rpoplpush(String var1, String var2);

   String brpoplpush(String var1, String var2, int var3);

   String lmove(String var1, String var2, ListDirection var3, ListDirection var4);

   String blmove(String var1, String var2, ListDirection var3, ListDirection var4, double var5);

   KeyValue<String, List<String>> lmpop(ListDirection var1, String... var2);

   KeyValue<String, List<String>> lmpop(ListDirection var1, int var2, String... var3);

   KeyValue<String, List<String>> blmpop(double var1, ListDirection var3, String... var4);

   KeyValue<String, List<String>> blmpop(double var1, ListDirection var3, int var4, String... var5);
}
