package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.args.SortedSetOption;
import me.neznamy.tab.libs.redis.clients.jedis.params.ScanParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ZAddParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ZIncrByParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ZParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ZRangeParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ScanResult;
import me.neznamy.tab.libs.redis.clients.jedis.resps.Tuple;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public interface SortedSetPipelineCommands {
   Response<Long> zadd(String var1, double var2, String var4);

   Response<Long> zadd(String var1, double var2, String var4, ZAddParams var5);

   Response<Long> zadd(String var1, Map<String, Double> var2);

   Response<Long> zadd(String var1, Map<String, Double> var2, ZAddParams var3);

   Response<Double> zaddIncr(String var1, double var2, String var4, ZAddParams var5);

   Response<Long> zrem(String var1, String... var2);

   Response<Double> zincrby(String var1, double var2, String var4);

   Response<Double> zincrby(String var1, double var2, String var4, ZIncrByParams var5);

   Response<Long> zrank(String var1, String var2);

   Response<Long> zrevrank(String var1, String var2);

   Response<KeyValue<Long, Double>> zrankWithScore(String var1, String var2);

   Response<KeyValue<Long, Double>> zrevrankWithScore(String var1, String var2);

   Response<List<String>> zrange(String var1, long var2, long var4);

   Response<List<String>> zrevrange(String var1, long var2, long var4);

   Response<List<Tuple>> zrangeWithScores(String var1, long var2, long var4);

   Response<List<Tuple>> zrevrangeWithScores(String var1, long var2, long var4);

   Response<String> zrandmember(String var1);

   Response<List<String>> zrandmember(String var1, long var2);

   Response<List<Tuple>> zrandmemberWithScores(String var1, long var2);

   Response<Long> zcard(String var1);

   Response<Double> zscore(String var1, String var2);

   Response<List<Double>> zmscore(String var1, String... var2);

   Response<Tuple> zpopmax(String var1);

   Response<List<Tuple>> zpopmax(String var1, int var2);

   Response<Tuple> zpopmin(String var1);

   Response<List<Tuple>> zpopmin(String var1, int var2);

   Response<Long> zcount(String var1, double var2, double var4);

   Response<Long> zcount(String var1, String var2, String var3);

   Response<List<String>> zrangeByScore(String var1, double var2, double var4);

   Response<List<String>> zrangeByScore(String var1, String var2, String var3);

   Response<List<String>> zrevrangeByScore(String var1, double var2, double var4);

   Response<List<String>> zrangeByScore(String var1, double var2, double var4, int var6, int var7);

   Response<List<String>> zrevrangeByScore(String var1, String var2, String var3);

   Response<List<String>> zrangeByScore(String var1, String var2, String var3, int var4, int var5);

   Response<List<String>> zrevrangeByScore(String var1, double var2, double var4, int var6, int var7);

   Response<List<Tuple>> zrangeByScoreWithScores(String var1, double var2, double var4);

   Response<List<Tuple>> zrevrangeByScoreWithScores(String var1, double var2, double var4);

   Response<List<Tuple>> zrangeByScoreWithScores(String var1, double var2, double var4, int var6, int var7);

   Response<List<String>> zrevrangeByScore(String var1, String var2, String var3, int var4, int var5);

   Response<List<Tuple>> zrangeByScoreWithScores(String var1, String var2, String var3);

   Response<List<Tuple>> zrevrangeByScoreWithScores(String var1, String var2, String var3);

   Response<List<Tuple>> zrangeByScoreWithScores(String var1, String var2, String var3, int var4, int var5);

   Response<List<Tuple>> zrevrangeByScoreWithScores(String var1, double var2, double var4, int var6, int var7);

   Response<List<Tuple>> zrevrangeByScoreWithScores(String var1, String var2, String var3, int var4, int var5);

   Response<List<String>> zrange(String var1, ZRangeParams var2);

   Response<List<Tuple>> zrangeWithScores(String var1, ZRangeParams var2);

   Response<Long> zrangestore(String var1, String var2, ZRangeParams var3);

   Response<Long> zremrangeByRank(String var1, long var2, long var4);

   Response<Long> zremrangeByScore(String var1, double var2, double var4);

   Response<Long> zremrangeByScore(String var1, String var2, String var3);

   Response<Long> zlexcount(String var1, String var2, String var3);

   Response<List<String>> zrangeByLex(String var1, String var2, String var3);

   Response<List<String>> zrangeByLex(String var1, String var2, String var3, int var4, int var5);

   Response<List<String>> zrevrangeByLex(String var1, String var2, String var3);

   Response<List<String>> zrevrangeByLex(String var1, String var2, String var3, int var4, int var5);

   Response<Long> zremrangeByLex(String var1, String var2, String var3);

   default Response<ScanResult<Tuple>> zscan(String key, String cursor) {
      return this.zscan(key, cursor, new ScanParams());
   }

   Response<ScanResult<Tuple>> zscan(String var1, String var2, ScanParams var3);

   Response<KeyValue<String, Tuple>> bzpopmax(double var1, String... var3);

   Response<KeyValue<String, Tuple>> bzpopmin(double var1, String... var3);

   Response<List<String>> zdiff(String... var1);

   Response<List<Tuple>> zdiffWithScores(String... var1);

   @Deprecated
   Response<Long> zdiffStore(String var1, String... var2);

   Response<Long> zdiffstore(String var1, String... var2);

   Response<Long> zinterstore(String var1, String... var2);

   Response<Long> zinterstore(String var1, ZParams var2, String... var3);

   Response<List<String>> zinter(ZParams var1, String... var2);

   Response<List<Tuple>> zinterWithScores(ZParams var1, String... var2);

   Response<Long> zintercard(String... var1);

   Response<Long> zintercard(long var1, String... var3);

   Response<List<String>> zunion(ZParams var1, String... var2);

   Response<List<Tuple>> zunionWithScores(ZParams var1, String... var2);

   Response<Long> zunionstore(String var1, String... var2);

   Response<Long> zunionstore(String var1, ZParams var2, String... var3);

   Response<KeyValue<String, List<Tuple>>> zmpop(SortedSetOption var1, String... var2);

   Response<KeyValue<String, List<Tuple>>> zmpop(SortedSetOption var1, int var2, String... var3);

   Response<KeyValue<String, List<Tuple>>> bzmpop(double var1, SortedSetOption var3, String... var4);

   Response<KeyValue<String, List<Tuple>>> bzmpop(double var1, SortedSetOption var3, int var4, String... var5);
}
