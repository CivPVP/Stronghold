package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.args.ExpiryOption;
import me.neznamy.tab.libs.redis.clients.jedis.params.ScanParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ScanResult;

public interface HashPipelineBinaryCommands {
   Response<Long> hset(byte[] var1, byte[] var2, byte[] var3);

   Response<Long> hset(byte[] var1, Map<byte[], byte[]> var2);

   Response<byte[]> hget(byte[] var1, byte[] var2);

   Response<Long> hsetnx(byte[] var1, byte[] var2, byte[] var3);

   Response<String> hmset(byte[] var1, Map<byte[], byte[]> var2);

   Response<List<byte[]>> hmget(byte[] var1, byte[]... var2);

   Response<Long> hincrBy(byte[] var1, byte[] var2, long var3);

   Response<Double> hincrByFloat(byte[] var1, byte[] var2, double var3);

   Response<Boolean> hexists(byte[] var1, byte[] var2);

   Response<Long> hdel(byte[] var1, byte[]... var2);

   Response<Long> hlen(byte[] var1);

   Response<Set<byte[]>> hkeys(byte[] var1);

   Response<List<byte[]>> hvals(byte[] var1);

   Response<Map<byte[], byte[]>> hgetAll(byte[] var1);

   Response<byte[]> hrandfield(byte[] var1);

   Response<List<byte[]>> hrandfield(byte[] var1, long var2);

   Response<List<Entry<byte[], byte[]>>> hrandfieldWithValues(byte[] var1, long var2);

   default Response<ScanResult<Entry<byte[], byte[]>>> hscan(byte[] key, byte[] cursor) {
      return this.hscan(key, cursor, new ScanParams());
   }

   Response<ScanResult<Entry<byte[], byte[]>>> hscan(byte[] var1, byte[] var2, ScanParams var3);

   default Response<ScanResult<byte[]>> hscanNoValues(byte[] key, byte[] cursor) {
      return this.hscanNoValues(key, cursor, new ScanParams());
   }

   Response<ScanResult<byte[]>> hscanNoValues(byte[] var1, byte[] var2, ScanParams var3);

   Response<Long> hstrlen(byte[] var1, byte[] var2);

   Response<List<Long>> hexpire(byte[] var1, long var2, byte[]... var4);

   Response<List<Long>> hexpire(byte[] var1, long var2, ExpiryOption var4, byte[]... var5);

   Response<List<Long>> hpexpire(byte[] var1, long var2, byte[]... var4);

   Response<List<Long>> hpexpire(byte[] var1, long var2, ExpiryOption var4, byte[]... var5);

   Response<List<Long>> hexpireAt(byte[] var1, long var2, byte[]... var4);

   Response<List<Long>> hexpireAt(byte[] var1, long var2, ExpiryOption var4, byte[]... var5);

   Response<List<Long>> hpexpireAt(byte[] var1, long var2, byte[]... var4);

   Response<List<Long>> hpexpireAt(byte[] var1, long var2, ExpiryOption var4, byte[]... var5);

   Response<List<Long>> hexpireTime(byte[] var1, byte[]... var2);

   Response<List<Long>> hpexpireTime(byte[] var1, byte[]... var2);

   Response<List<Long>> httl(byte[] var1, byte[]... var2);

   Response<List<Long>> hpttl(byte[] var1, byte[]... var2);

   Response<List<Long>> hpersist(byte[] var1, byte[]... var2);
}
