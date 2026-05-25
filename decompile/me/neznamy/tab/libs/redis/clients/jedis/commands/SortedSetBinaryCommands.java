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

public interface SortedSetBinaryCommands {
   long zadd(byte[] var1, double var2, byte[] var4);

   long zadd(byte[] var1, double var2, byte[] var4, ZAddParams var5);

   long zadd(byte[] var1, Map<byte[], Double> var2);

   long zadd(byte[] var1, Map<byte[], Double> var2, ZAddParams var3);

   Double zaddIncr(byte[] var1, double var2, byte[] var4, ZAddParams var5);

   long zrem(byte[] var1, byte[]... var2);

   double zincrby(byte[] var1, double var2, byte[] var4);

   Double zincrby(byte[] var1, double var2, byte[] var4, ZIncrByParams var5);

   Long zrank(byte[] var1, byte[] var2);

   Long zrevrank(byte[] var1, byte[] var2);

   KeyValue<Long, Double> zrankWithScore(byte[] var1, byte[] var2);

   KeyValue<Long, Double> zrevrankWithScore(byte[] var1, byte[] var2);

   List<byte[]> zrange(byte[] var1, long var2, long var4);

   List<byte[]> zrevrange(byte[] var1, long var2, long var4);

   List<Tuple> zrangeWithScores(byte[] var1, long var2, long var4);

   List<Tuple> zrevrangeWithScores(byte[] var1, long var2, long var4);

   List<byte[]> zrange(byte[] var1, ZRangeParams var2);

   List<Tuple> zrangeWithScores(byte[] var1, ZRangeParams var2);

   long zrangestore(byte[] var1, byte[] var2, ZRangeParams var3);

   byte[] zrandmember(byte[] var1);

   List<byte[]> zrandmember(byte[] var1, long var2);

   List<Tuple> zrandmemberWithScores(byte[] var1, long var2);

   long zcard(byte[] var1);

   Double zscore(byte[] var1, byte[] var2);

   List<Double> zmscore(byte[] var1, byte[]... var2);

   Tuple zpopmax(byte[] var1);

   List<Tuple> zpopmax(byte[] var1, int var2);

   Tuple zpopmin(byte[] var1);

   List<Tuple> zpopmin(byte[] var1, int var2);

   long zcount(byte[] var1, double var2, double var4);

   long zcount(byte[] var1, byte[] var2, byte[] var3);

   List<byte[]> zrangeByScore(byte[] var1, double var2, double var4);

   List<byte[]> zrangeByScore(byte[] var1, byte[] var2, byte[] var3);

   List<byte[]> zrevrangeByScore(byte[] var1, double var2, double var4);

   List<byte[]> zrangeByScore(byte[] var1, double var2, double var4, int var6, int var7);

   List<byte[]> zrevrangeByScore(byte[] var1, byte[] var2, byte[] var3);

   List<byte[]> zrangeByScore(byte[] var1, byte[] var2, byte[] var3, int var4, int var5);

   List<byte[]> zrevrangeByScore(byte[] var1, double var2, double var4, int var6, int var7);

   List<Tuple> zrangeByScoreWithScores(byte[] var1, double var2, double var4);

   List<Tuple> zrevrangeByScoreWithScores(byte[] var1, double var2, double var4);

   List<Tuple> zrangeByScoreWithScores(byte[] var1, double var2, double var4, int var6, int var7);

   List<byte[]> zrevrangeByScore(byte[] var1, byte[] var2, byte[] var3, int var4, int var5);

   List<Tuple> zrangeByScoreWithScores(byte[] var1, byte[] var2, byte[] var3);

   List<Tuple> zrevrangeByScoreWithScores(byte[] var1, byte[] var2, byte[] var3);

   List<Tuple> zrangeByScoreWithScores(byte[] var1, byte[] var2, byte[] var3, int var4, int var5);

   List<Tuple> zrevrangeByScoreWithScores(byte[] var1, double var2, double var4, int var6, int var7);

   List<Tuple> zrevrangeByScoreWithScores(byte[] var1, byte[] var2, byte[] var3, int var4, int var5);

   long zremrangeByRank(byte[] var1, long var2, long var4);

   long zremrangeByScore(byte[] var1, double var2, double var4);

   long zremrangeByScore(byte[] var1, byte[] var2, byte[] var3);

   long zlexcount(byte[] var1, byte[] var2, byte[] var3);

   List<byte[]> zrangeByLex(byte[] var1, byte[] var2, byte[] var3);

   List<byte[]> zrangeByLex(byte[] var1, byte[] var2, byte[] var3, int var4, int var5);

   List<byte[]> zrevrangeByLex(byte[] var1, byte[] var2, byte[] var3);

   List<byte[]> zrevrangeByLex(byte[] var1, byte[] var2, byte[] var3, int var4, int var5);

   long zremrangeByLex(byte[] var1, byte[] var2, byte[] var3);

   default ScanResult<Tuple> zscan(byte[] key, byte[] cursor) {
      return this.zscan(key, cursor, new ScanParams());
   }

   ScanResult<Tuple> zscan(byte[] var1, byte[] var2, ScanParams var3);

   KeyValue<byte[], Tuple> bzpopmax(double var1, byte[]... var3);

   KeyValue<byte[], Tuple> bzpopmin(double var1, byte[]... var3);

   List<byte[]> zdiff(byte[]... var1);

   List<Tuple> zdiffWithScores(byte[]... var1);

   @Deprecated
   long zdiffStore(byte[] var1, byte[]... var2);

   long zdiffstore(byte[] var1, byte[]... var2);

   List<byte[]> zinter(ZParams var1, byte[]... var2);

   List<Tuple> zinterWithScores(ZParams var1, byte[]... var2);

   long zinterstore(byte[] var1, byte[]... var2);

   long zinterstore(byte[] var1, ZParams var2, byte[]... var3);

   long zintercard(byte[]... var1);

   long zintercard(long var1, byte[]... var3);

   List<byte[]> zunion(ZParams var1, byte[]... var2);

   List<Tuple> zunionWithScores(ZParams var1, byte[]... var2);

   long zunionstore(byte[] var1, byte[]... var2);

   long zunionstore(byte[] var1, ZParams var2, byte[]... var3);

   KeyValue<byte[], List<Tuple>> zmpop(SortedSetOption var1, byte[]... var2);

   KeyValue<byte[], List<Tuple>> zmpop(SortedSetOption var1, int var2, byte[]... var3);

   KeyValue<byte[], List<Tuple>> bzmpop(double var1, SortedSetOption var3, byte[]... var4);

   KeyValue<byte[], List<Tuple>> bzmpop(double var1, SortedSetOption var3, int var4, byte[]... var5);
}
