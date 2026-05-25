package me.neznamy.tab.libs.redis.clients.jedis.bloom.commands;

import java.util.List;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.bloom.TDigestMergeParams;

public interface TDigestSketchCommands {
   String tdigestCreate(String var1);

   String tdigestCreate(String var1, int var2);

   String tdigestReset(String var1);

   String tdigestMerge(String var1, String... var2);

   String tdigestMerge(TDigestMergeParams var1, String var2, String... var3);

   Map<String, Object> tdigestInfo(String var1);

   String tdigestAdd(String var1, double... var2);

   List<Double> tdigestCDF(String var1, double... var2);

   List<Double> tdigestQuantile(String var1, double... var2);

   double tdigestMin(String var1);

   double tdigestMax(String var1);

   double tdigestTrimmedMean(String var1, double var2, double var4);

   List<Long> tdigestRank(String var1, double... var2);

   List<Long> tdigestRevRank(String var1, double... var2);

   List<Double> tdigestByRank(String var1, long... var2);

   List<Double> tdigestByRevRank(String var1, long... var2);
}
