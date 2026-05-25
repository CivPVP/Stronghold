package me.neznamy.tab.libs.redis.clients.jedis.timeseries;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.neznamy.tab.libs.redis.clients.jedis.Response;

public interface RedisTimeSeriesPipelineCommands {
   Response<String> tsCreate(String var1);

   Response<String> tsCreate(String var1, TSCreateParams var2);

   Response<Long> tsDel(String var1, long var2, long var4);

   Response<String> tsAlter(String var1, TSAlterParams var2);

   Response<Long> tsAdd(String var1, double var2);

   Response<Long> tsAdd(String var1, long var2, double var4);

   @Deprecated
   Response<Long> tsAdd(String var1, long var2, double var4, TSCreateParams var6);

   Response<Long> tsAdd(String var1, long var2, double var4, TSAddParams var6);

   Response<List<Long>> tsMAdd(Entry<String, TSElement>... var1);

   Response<Long> tsIncrBy(String var1, double var2);

   Response<Long> tsIncrBy(String var1, double var2, long var4);

   Response<Long> tsIncrBy(String var1, double var2, TSIncrByParams var4);

   Response<Long> tsDecrBy(String var1, double var2);

   Response<Long> tsDecrBy(String var1, double var2, long var4);

   Response<Long> tsDecrBy(String var1, double var2, TSDecrByParams var4);

   Response<List<TSElement>> tsRange(String var1, long var2, long var4);

   Response<List<TSElement>> tsRange(String var1, TSRangeParams var2);

   Response<List<TSElement>> tsRevRange(String var1, long var2, long var4);

   Response<List<TSElement>> tsRevRange(String var1, TSRangeParams var2);

   Response<Map<String, TSMRangeElements>> tsMRange(long var1, long var3, String... var5);

   Response<Map<String, TSMRangeElements>> tsMRange(TSMRangeParams var1);

   Response<Map<String, TSMRangeElements>> tsMRevRange(long var1, long var3, String... var5);

   Response<Map<String, TSMRangeElements>> tsMRevRange(TSMRangeParams var1);

   Response<TSElement> tsGet(String var1);

   Response<TSElement> tsGet(String var1, TSGetParams var2);

   Response<Map<String, TSMGetElement>> tsMGet(TSMGetParams var1, String... var2);

   Response<String> tsCreateRule(String var1, String var2, AggregationType var3, long var4);

   Response<String> tsCreateRule(String var1, String var2, AggregationType var3, long var4, long var6);

   Response<String> tsDeleteRule(String var1, String var2);

   Response<List<String>> tsQueryIndex(String... var1);

   Response<TSInfo> tsInfo(String var1);

   Response<TSInfo> tsInfoDebug(String var1);
}
