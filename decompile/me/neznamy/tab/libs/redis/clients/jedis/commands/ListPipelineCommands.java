package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.args.ListDirection;
import me.neznamy.tab.libs.redis.clients.jedis.args.ListPosition;
import me.neznamy.tab.libs.redis.clients.jedis.params.LPosParams;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public interface ListPipelineCommands {
   Response<Long> rpush(String var1, String... var2);

   Response<Long> lpush(String var1, String... var2);

   Response<Long> llen(String var1);

   Response<List<String>> lrange(String var1, long var2, long var4);

   Response<String> ltrim(String var1, long var2, long var4);

   Response<String> lindex(String var1, long var2);

   Response<String> lset(String var1, long var2, String var4);

   Response<Long> lrem(String var1, long var2, String var4);

   Response<String> lpop(String var1);

   Response<List<String>> lpop(String var1, int var2);

   Response<Long> lpos(String var1, String var2);

   Response<Long> lpos(String var1, String var2, LPosParams var3);

   Response<List<Long>> lpos(String var1, String var2, LPosParams var3, long var4);

   Response<String> rpop(String var1);

   Response<List<String>> rpop(String var1, int var2);

   Response<Long> linsert(String var1, ListPosition var2, String var3, String var4);

   Response<Long> lpushx(String var1, String... var2);

   Response<Long> rpushx(String var1, String... var2);

   Response<List<String>> blpop(int var1, String var2);

   Response<KeyValue<String, String>> blpop(double var1, String var3);

   Response<List<String>> brpop(int var1, String var2);

   Response<KeyValue<String, String>> brpop(double var1, String var3);

   Response<List<String>> blpop(int var1, String... var2);

   Response<KeyValue<String, String>> blpop(double var1, String... var3);

   Response<List<String>> brpop(int var1, String... var2);

   Response<KeyValue<String, String>> brpop(double var1, String... var3);

   Response<String> rpoplpush(String var1, String var2);

   Response<String> brpoplpush(String var1, String var2, int var3);

   Response<String> lmove(String var1, String var2, ListDirection var3, ListDirection var4);

   Response<String> blmove(String var1, String var2, ListDirection var3, ListDirection var4, double var5);

   Response<KeyValue<String, List<String>>> lmpop(ListDirection var1, String... var2);

   Response<KeyValue<String, List<String>>> lmpop(ListDirection var1, int var2, String... var3);

   Response<KeyValue<String, List<String>>> blmpop(double var1, ListDirection var3, String... var4);

   Response<KeyValue<String, List<String>>> blmpop(double var1, ListDirection var3, int var4, String... var5);
}
