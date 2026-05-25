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

public interface SortedSetPipelineBinaryCommands {
   Response<Long> zadd(byte[] var1, double var2, byte[] var4);

   Response<Long> zadd(byte[] var1, double var2, byte[] var4, ZAddParams var5);

   Response<Long> zadd(byte[] var1, Map<byte[], Double> var2);

   Response<Long> zadd(byte[] var1, Map<byte[], Double> var2, ZAddParams var3);

   Response<Double> zaddIncr(byte[] var1, double var2, byte[] var4, ZAddParams var5);

   Response<Long> zrem(byte[] var1, byte[]... var2);

   Response<Double> zincrby(byte[] var1, double var2, byte[] var4);

   Response<Double> zincrby(byte[] var1, double var2, byte[] var4, ZIncrByParams var5);

   Response<Long> zrank(byte[] var1, byte[] var2);

   Response<Long> zrevrank(byte[] var1, byte[] var2);

   Response<KeyValue<Long, Double>> zrankWithScore(byte[] var1, byte[] var2);

   Response<KeyValue<Long, Double>> zrevrankWithScore(byte[] var1, byte[] var2);

   Response<List<byte[]>> zrange(byte[] var1, long var2, long var4);

   Response<List<byte[]>> zrevrange(byte[] var1, long var2, long var4);

   Response<List<Tuple>> zrangeWithScores(byte[] var1, long var2, long var4);

   Response<List<Tuple>> zrevrangeWithScores(byte[] var1, long var2, long var4);

   Response<byte[]> zrandmember(byte[] var1);

   Response<List<byte[]>> zrandmember(byte[] var1, long var2);

   Response<List<Tuple>> zrandmemberWithScores(byte[] var1, long var2);

   Response<Long> zcard(byte[] var1);

   Response<Double> zscore(byte[] var1, byte[] var2);

   Response<List<Double>> zmscore(byte[] var1, byte[]... var2);

   Response<Tuple> zpopmax(byte[] var1);

   Response<List<Tuple>> zpopmax(byte[] var1, int var2);

   Response<Tuple> zpopmin(byte[] var1);

   Response<List<Tuple>> zpopmin(byte[] var1, int var2);

   Response<Long> zcount(byte[] var1, double var2, double var4);

   Response<Long> zcount(byte[] var1, byte[] var2, byte[] var3);

   Response<List<byte[]>> zrangeByScore(byte[] var1, double var2, double var4);

   Response<List<byte[]>> zrangeByScore(byte[] var1, byte[] var2, byte[] var3);

   Response<List<byte[]>> zrevrangeByScore(byte[] var1, double var2, double var4);

   Response<List<byte[]>> zrangeByScore(byte[] var1, double var2, double var4, int var6, int var7);

   Response<List<byte[]>> zrevrangeByScore(byte[] var1, byte[] var2, byte[] var3);

   Response<List<byte[]>> zrangeByScore(byte[] var1, byte[] var2, byte[] var3, int var4, int var5);

   Response<List<byte[]>> zrevrangeByScore(byte[] var1, double var2, double var4, int var6, int var7);

   Response<List<Tuple>> zrangeByScoreWithScores(byte[] var1, double var2, double var4);

   Response<List<Tuple>> zrevrangeByScoreWithScores(byte[] var1, double var2, double var4);

   Response<List<Tuple>> zrangeByScoreWithScores(byte[] var1, double var2, double var4, int var6, int var7);

   Response<List<byte[]>> zrevrangeByScore(byte[] var1, byte[] var2, byte[] var3, int var4, int var5);

   Response<List<Tuple>> zrangeByScoreWithScores(byte[] var1, byte[] var2, byte[] var3);

   Response<List<Tuple>> zrevrangeByScoreWithScores(byte[] var1, byte[] var2, byte[] var3);

   Response<List<Tuple>> zrangeByScoreWithScores(byte[] var1, byte[] var2, byte[] var3, int var4, int var5);

   Response<List<Tuple>> zrevrangeByScoreWithScores(byte[] var1, double var2, double var4, int var6, int var7);

   Response<List<Tuple>> zrevrangeByScoreWithScores(byte[] var1, byte[] var2, byte[] var3, int var4, int var5);

   Response<Long> zremrangeByRank(byte[] var1, long var2, long var4);

   Response<Long> zremrangeByScore(byte[] var1, double var2, double var4);

   Response<Long> zremrangeByScore(byte[] var1, byte[] var2, byte[] var3);

   Response<Long> zlexcount(byte[] var1, byte[] var2, byte[] var3);

   Response<List<byte[]>> zrangeByLex(byte[] var1, byte[] var2, byte[] var3);

   Response<List<byte[]>> zrangeByLex(byte[] var1, byte[] var2, byte[] var3, int var4, int var5);

   Response<List<byte[]>> zrevrangeByLex(byte[] var1, byte[] var2, byte[] var3);

   Response<List<byte[]>> zrevrangeByLex(byte[] var1, byte[] var2, byte[] var3, int var4, int var5);

   Response<List<byte[]>> zrange(byte[] var1, ZRangeParams var2);

   Response<List<Tuple>> zrangeWithScores(byte[] var1, ZRangeParams var2);

   Response<Long> zrangestore(byte[] var1, byte[] var2, ZRangeParams var3);

   Response<Long> zremrangeByLex(byte[] var1, byte[] var2, byte[] var3);

   default Response<ScanResult<Tuple>> zscan(byte[] key, byte[] cursor) {
      return this.zscan(key, cursor, new ScanParams());
   }

   Response<ScanResult<Tuple>> zscan(byte[] var1, byte[] var2, ScanParams var3);

   Response<KeyValue<byte[], Tuple>> bzpopmax(double var1, byte[]... var3);

   Response<KeyValue<byte[], Tuple>> bzpopmin(double var1, byte[]... var3);

   Response<List<byte[]>> zdiff(byte[]... var1);

   Response<List<Tuple>> zdiffWithScores(byte[]... var1);

   @Deprecated
   Response<Long> zdiffStore(byte[] var1, byte[]... var2);

   Response<Long> zdiffstore(byte[] var1, byte[]... var2);

   Response<List<byte[]>> zinter(ZParams var1, byte[]... var2);

   Response<List<Tuple>> zinterWithScores(ZParams var1, byte[]... var2);

   Response<Long> zinterstore(byte[] var1, byte[]... var2);

   Response<Long> zinterstore(byte[] var1, ZParams var2, byte[]... var3);

   Response<Long> zintercard(byte[]... var1);

   Response<Long> zintercard(long var1, byte[]... var3);

   Response<List<byte[]>> zunion(ZParams var1, byte[]... var2);

   Response<List<Tuple>> zunionWithScores(ZParams var1, byte[]... var2);

   Response<Long> zunionstore(byte[] var1, byte[]... var2);

   Response<Long> zunionstore(byte[] var1, ZParams var2, byte[]... var3);

   Response<KeyValue<byte[], List<Tuple>>> zmpop(SortedSetOption var1, byte[]... var2);

   Response<KeyValue<byte[], List<Tuple>>> zmpop(SortedSetOption var1, int var2, byte[]... var3);

   Response<KeyValue<byte[], List<Tuple>>> bzmpop(double var1, SortedSetOption var3, byte[]... var4);

   Response<KeyValue<byte[], List<Tuple>>> bzmpop(double var1, SortedSetOption var3, int var4, byte[]... var5);
}
