package me.neznamy.tab.libs.redis.clients.jedis.bloom.commands;

import java.util.List;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.bloom.TDigestMergeParams;

public interface TDigestSketchPipelineCommands {
   Response<String> tdigestCreate(String var1);

   Response<String> tdigestCreate(String var1, int var2);

   Response<String> tdigestReset(String var1);

   Response<String> tdigestMerge(String var1, String... var2);

   Response<String> tdigestMerge(TDigestMergeParams var1, String var2, String... var3);

   Response<Map<String, Object>> tdigestInfo(String var1);

   Response<String> tdigestAdd(String var1, double... var2);

   Response<List<Double>> tdigestCDF(String var1, double... var2);

   Response<List<Double>> tdigestQuantile(String var1, double... var2);

   Response<Double> tdigestMin(String var1);

   Response<Double> tdigestMax(String var1);

   Response<Double> tdigestTrimmedMean(String var1, double var2, double var4);

   Response<List<Long>> tdigestRank(String var1, double... var2);

   Response<List<Long>> tdigestRevRank(String var1, double... var2);

   Response<List<Double>> tdigestByRank(String var1, long... var2);

   Response<List<Double>> tdigestByRevRank(String var1, long... var2);
}
