package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.params.XAddParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XAutoClaimParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XClaimParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XPendingParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XReadGroupParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XReadParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XTrimParams;

public interface StreamPipelineBinaryCommands {
   default Response<byte[]> xadd(byte[] key, Map<byte[], byte[]> hash, XAddParams params) {
      return this.xadd(key, params, hash);
   }

   Response<byte[]> xadd(byte[] var1, XAddParams var2, Map<byte[], byte[]> var3);

   Response<Long> xlen(byte[] var1);

   Response<List<Object>> xrange(byte[] var1, byte[] var2, byte[] var3);

   Response<List<Object>> xrange(byte[] var1, byte[] var2, byte[] var3, int var4);

   Response<List<Object>> xrevrange(byte[] var1, byte[] var2, byte[] var3);

   Response<List<Object>> xrevrange(byte[] var1, byte[] var2, byte[] var3, int var4);

   Response<Long> xack(byte[] var1, byte[] var2, byte[]... var3);

   Response<String> xgroupCreate(byte[] var1, byte[] var2, byte[] var3, boolean var4);

   Response<String> xgroupSetID(byte[] var1, byte[] var2, byte[] var3);

   Response<Long> xgroupDestroy(byte[] var1, byte[] var2);

   Response<Boolean> xgroupCreateConsumer(byte[] var1, byte[] var2, byte[] var3);

   Response<Long> xgroupDelConsumer(byte[] var1, byte[] var2, byte[] var3);

   Response<Long> xdel(byte[] var1, byte[]... var2);

   Response<Long> xtrim(byte[] var1, long var2, boolean var4);

   Response<Long> xtrim(byte[] var1, XTrimParams var2);

   Response<Object> xpending(byte[] var1, byte[] var2);

   Response<List<Object>> xpending(byte[] var1, byte[] var2, XPendingParams var3);

   Response<List<byte[]>> xclaim(byte[] var1, byte[] var2, byte[] var3, long var4, XClaimParams var6, byte[]... var7);

   Response<List<byte[]>> xclaimJustId(byte[] var1, byte[] var2, byte[] var3, long var4, XClaimParams var6, byte[]... var7);

   Response<List<Object>> xautoclaim(byte[] var1, byte[] var2, byte[] var3, long var4, byte[] var6, XAutoClaimParams var7);

   Response<List<Object>> xautoclaimJustId(byte[] var1, byte[] var2, byte[] var3, long var4, byte[] var6, XAutoClaimParams var7);

   Response<Object> xinfoStream(byte[] var1);

   Response<Object> xinfoStreamFull(byte[] var1);

   Response<Object> xinfoStreamFull(byte[] var1, int var2);

   Response<List<Object>> xinfoGroups(byte[] var1);

   Response<List<Object>> xinfoConsumers(byte[] var1, byte[] var2);

   Response<List<Object>> xread(XReadParams var1, Entry<byte[], byte[]>... var2);

   Response<List<Object>> xreadGroup(byte[] var1, byte[] var2, XReadGroupParams var3, Entry<byte[], byte[]>... var4);
}
