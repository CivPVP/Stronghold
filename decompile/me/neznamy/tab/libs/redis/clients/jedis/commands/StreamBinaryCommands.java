package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.neznamy.tab.libs.redis.clients.jedis.params.XAddParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XAutoClaimParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XClaimParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XPendingParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XReadGroupParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XReadParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XTrimParams;

public interface StreamBinaryCommands {
   default byte[] xadd(byte[] key, Map<byte[], byte[]> hash, XAddParams params) {
      return this.xadd(key, params, hash);
   }

   byte[] xadd(byte[] var1, XAddParams var2, Map<byte[], byte[]> var3);

   long xlen(byte[] var1);

   List<Object> xrange(byte[] var1, byte[] var2, byte[] var3);

   List<Object> xrange(byte[] var1, byte[] var2, byte[] var3, int var4);

   List<Object> xrevrange(byte[] var1, byte[] var2, byte[] var3);

   List<Object> xrevrange(byte[] var1, byte[] var2, byte[] var3, int var4);

   long xack(byte[] var1, byte[] var2, byte[]... var3);

   String xgroupCreate(byte[] var1, byte[] var2, byte[] var3, boolean var4);

   String xgroupSetID(byte[] var1, byte[] var2, byte[] var3);

   long xgroupDestroy(byte[] var1, byte[] var2);

   boolean xgroupCreateConsumer(byte[] var1, byte[] var2, byte[] var3);

   long xgroupDelConsumer(byte[] var1, byte[] var2, byte[] var3);

   long xdel(byte[] var1, byte[]... var2);

   long xtrim(byte[] var1, long var2, boolean var4);

   long xtrim(byte[] var1, XTrimParams var2);

   Object xpending(byte[] var1, byte[] var2);

   List<Object> xpending(byte[] var1, byte[] var2, XPendingParams var3);

   List<byte[]> xclaim(byte[] var1, byte[] var2, byte[] var3, long var4, XClaimParams var6, byte[]... var7);

   List<byte[]> xclaimJustId(byte[] var1, byte[] var2, byte[] var3, long var4, XClaimParams var6, byte[]... var7);

   List<Object> xautoclaim(byte[] var1, byte[] var2, byte[] var3, long var4, byte[] var6, XAutoClaimParams var7);

   List<Object> xautoclaimJustId(byte[] var1, byte[] var2, byte[] var3, long var4, byte[] var6, XAutoClaimParams var7);

   Object xinfoStream(byte[] var1);

   Object xinfoStreamFull(byte[] var1);

   Object xinfoStreamFull(byte[] var1, int var2);

   List<Object> xinfoGroups(byte[] var1);

   List<Object> xinfoConsumers(byte[] var1, byte[] var2);

   List<Object> xread(XReadParams var1, Entry<byte[], byte[]>... var2);

   List<Object> xreadGroup(byte[] var1, byte[] var2, XReadGroupParams var3, Entry<byte[], byte[]>... var4);
}
