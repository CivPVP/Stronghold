package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.args.SortedSetOption;
import me.neznamy.tab.libs.redis.clients.jedis.params.ScanParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ZAddParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ZIncrByParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ZParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ZRangeParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ScanResult;
import me.neznamy.tab.libs.redis.clients.jedis.resps.Tuple;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public interface SortedSetCommands {
   long zadd(String var1, double var2, String var4);

   long zadd(String var1, double var2, String var4, ZAddParams var5);

   long zadd(String var1, Map<String, Double> var2);

   long zadd(String var1, Map<String, Double> var2, ZAddParams var3);

   Double zaddIncr(String var1, double var2, String var4, ZAddParams var5);

   long zrem(String var1, String... var2);

   double zincrby(String var1, double var2, String var4);

   Double zincrby(String var1, double var2, String var4, ZIncrByParams var5);

   Long zrank(String var1, String var2);

   Long zrevrank(String var1, String var2);

   KeyValue<Long, Double> zrankWithScore(String var1, String var2);

   KeyValue<Long, Double> zrevrankWithScore(String var1, String var2);

   List<String> zrange(String var1, long var2, long var4);

   List<String> zrevrange(String var1, long var2, long var4);

   List<Tuple> zrangeWithScores(String var1, long var2, long var4);

   List<Tuple> zrevrangeWithScores(String var1, long var2, long var4);

   List<String> zrange(String var1, ZRangeParams var2);

   List<Tuple> zrangeWithScores(String var1, ZRangeParams var2);

   long zrangestore(String var1, String var2, ZRangeParams var3);

   String zrandmember(String var1);

   List<String> zrandmember(String var1, long var2);

   List<Tuple> zrandmemberWithScores(String var1, long var2);

   long zcard(String var1);

   Double zscore(String var1, String var2);

   List<Double> zmscore(String var1, String... var2);

   Tuple zpopmax(String var1);

   List<Tuple> zpopmax(String var1, int var2);

   Tuple zpopmin(String var1);

   List<Tuple> zpopmin(String var1, int var2);

   long zcount(String var1, double var2, double var4);

   long zcount(String var1, String var2, String var3);

   List<String> zrangeByScore(String var1, double var2, double var4);

   List<String> zrangeByScore(String var1, String var2, String var3);

   List<String> zrevrangeByScore(String var1, double var2, double var4);

   List<String> zrangeByScore(String var1, double var2, double var4, int var6, int var7);

   List<String> zrevrangeByScore(String var1, String var2, String var3);

   List<String> zrangeByScore(String var1, String var2, String var3, int var4, int var5);

   List<String> zrevrangeByScore(String var1, double var2, double var4, int var6, int var7);

   List<Tuple> zrangeByScoreWithScores(String var1, double var2, double var4);

   List<Tuple> zrevrangeByScoreWithScores(String var1, double var2, double var4);

   List<Tuple> zrangeByScoreWithScores(String var1, double var2, double var4, int var6, int var7);

   List<String> zrevrangeByScore(String var1, String var2, String var3, int var4, int var5);

   List<Tuple> zrangeByScoreWithScores(String var1, String var2, String var3);

   List<Tuple> zrevrangeByScoreWithScores(String var1, String var2, String var3);

   List<Tuple> zrangeByScoreWithScores(String var1, String var2, String var3, int var4, int var5);

   List<Tuple> zrevrangeByScoreWithScores(String var1, double var2, double var4, int var6, int var7);

   List<Tuple> zrevrangeByScoreWithScores(String var1, String var2, String var3, int var4, int var5);

   long zremrangeByRank(String var1, long var2, long var4);

   long zremrangeByScore(String var1, double var2, double var4);

   long zremrangeByScore(String var1, String var2, String var3);

   long zlexcount(String var1, String var2, String var3);

   List<String> zrangeByLex(String var1, String var2, String var3);

   List<String> zrangeByLex(String var1, String var2, String var3, int var4, int var5);

   List<String> zrevrangeByLex(String var1, String var2, String var3);

   List<String> zrevrangeByLex(String var1, String var2, String var3, int var4, int var5);

   long zremrangeByLex(String var1, String var2, String var3);

   default ScanResult<Tuple> zscan(String key, String cursor) {
      return this.zscan(key, cursor, new ScanParams());
   }

   ScanResult<Tuple> zscan(String var1, String var2, ScanParams var3);

   KeyValue<String, Tuple> bzpopmax(double var1, String... var3);

   KeyValue<String, Tuple> bzpopmin(double var1, String... var3);

   List<String> zdiff(String... var1);

   List<Tuple> zdiffWithScores(String... var1);

   @Deprecated
   long zdiffStore(String var1, String... var2);

   long zdiffstore(String var1, String... var2);

   List<String> zinter(ZParams var1, String... var2);

   List<Tuple> zinterWithScores(ZParams var1, String... var2);

   long zinterstore(String var1, String... var2);

   long zinterstore(String var1, ZParams var2, String... var3);

   long zintercard(String... var1);

   long zintercard(long var1, String... var3);

   List<String> zunion(ZParams var1, String... var2);

   List<Tuple> zunionWithScores(ZParams var1, String... var2);

   long zunionstore(String var1, String... var2);

   long zunionstore(String var1, ZParams var2, String... var3);

   KeyValue<String, List<Tuple>> zmpop(SortedSetOption var1, String... var2);

   KeyValue<String, List<Tuple>> zmpop(SortedSetOption var1, int var2, String... var3);

   KeyValue<String, List<Tuple>> bzmpop(double var1, SortedSetOption var3, String... var4);

   KeyValue<String, List<Tuple>> bzmpop(double var1, SortedSetOption var3, int var4, String... var5);
}
