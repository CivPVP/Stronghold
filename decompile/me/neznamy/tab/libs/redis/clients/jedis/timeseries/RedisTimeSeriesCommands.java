package me.neznamy.tab.libs.redis.clients.jedis.timeseries;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public interface RedisTimeSeriesCommands {
   String tsCreate(String var1);

   String tsCreate(String var1, TSCreateParams var2);

   long tsDel(String var1, long var2, long var4);

   String tsAlter(String var1, TSAlterParams var2);

   long tsAdd(String var1, double var2);

   long tsAdd(String var1, long var2, double var4);

   @Deprecated
   long tsAdd(String var1, long var2, double var4, TSCreateParams var6);

   long tsAdd(String var1, long var2, double var4, TSAddParams var6);

   List<Long> tsMAdd(Entry<String, TSElement>... var1);

   long tsIncrBy(String var1, double var2);

   long tsIncrBy(String var1, double var2, long var4);

   long tsIncrBy(String var1, double var2, TSIncrByParams var4);

   long tsDecrBy(String var1, double var2);

   long tsDecrBy(String var1, double var2, long var4);

   long tsDecrBy(String var1, double var2, TSDecrByParams var4);

   List<TSElement> tsRange(String var1, long var2, long var4);

   List<TSElement> tsRange(String var1, TSRangeParams var2);

   List<TSElement> tsRevRange(String var1, long var2, long var4);

   List<TSElement> tsRevRange(String var1, TSRangeParams var2);

   Map<String, TSMRangeElements> tsMRange(long var1, long var3, String... var5);

   Map<String, TSMRangeElements> tsMRange(TSMRangeParams var1);

   Map<String, TSMRangeElements> tsMRevRange(long var1, long var3, String... var5);

   Map<String, TSMRangeElements> tsMRevRange(TSMRangeParams var1);

   TSElement tsGet(String var1);

   TSElement tsGet(String var1, TSGetParams var2);

   Map<String, TSMGetElement> tsMGet(TSMGetParams var1, String... var2);

   String tsCreateRule(String var1, String var2, AggregationType var3, long var4);

   String tsCreateRule(String var1, String var2, AggregationType var3, long var4, long var6);

   String tsDeleteRule(String var1, String var2);

   List<String> tsQueryIndex(String... var1);

   TSInfo tsInfo(String var1);

   TSInfo tsInfoDebug(String var1);
}
