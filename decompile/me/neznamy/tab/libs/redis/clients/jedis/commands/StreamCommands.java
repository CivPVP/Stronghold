package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.neznamy.tab.libs.redis.clients.jedis.StreamEntryID;
import me.neznamy.tab.libs.redis.clients.jedis.params.XAddParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XAutoClaimParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XClaimParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XPendingParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XReadGroupParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XReadParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XTrimParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamConsumerInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamConsumersInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamEntry;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamFullInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamGroupInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamPendingEntry;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamPendingSummary;

public interface StreamCommands {
   StreamEntryID xadd(String var1, StreamEntryID var2, Map<String, String> var3);

   default StreamEntryID xadd(String key, Map<String, String> hash, XAddParams params) {
      return this.xadd(key, params, hash);
   }

   StreamEntryID xadd(String var1, XAddParams var2, Map<String, String> var3);

   long xlen(String var1);

   List<StreamEntry> xrange(String var1, StreamEntryID var2, StreamEntryID var3);

   List<StreamEntry> xrange(String var1, StreamEntryID var2, StreamEntryID var3, int var4);

   List<StreamEntry> xrevrange(String var1, StreamEntryID var2, StreamEntryID var3);

   List<StreamEntry> xrevrange(String var1, StreamEntryID var2, StreamEntryID var3, int var4);

   List<StreamEntry> xrange(String var1, String var2, String var3);

   List<StreamEntry> xrange(String var1, String var2, String var3, int var4);

   List<StreamEntry> xrevrange(String var1, String var2, String var3);

   List<StreamEntry> xrevrange(String var1, String var2, String var3, int var4);

   long xack(String var1, String var2, StreamEntryID... var3);

   String xgroupCreate(String var1, String var2, StreamEntryID var3, boolean var4);

   String xgroupSetID(String var1, String var2, StreamEntryID var3);

   long xgroupDestroy(String var1, String var2);

   boolean xgroupCreateConsumer(String var1, String var2, String var3);

   long xgroupDelConsumer(String var1, String var2, String var3);

   long xdel(String var1, StreamEntryID... var2);

   long xtrim(String var1, long var2, boolean var4);

   long xtrim(String var1, XTrimParams var2);

   StreamPendingSummary xpending(String var1, String var2);

   List<StreamPendingEntry> xpending(String var1, String var2, XPendingParams var3);

   List<StreamEntry> xclaim(String var1, String var2, String var3, long var4, XClaimParams var6, StreamEntryID... var7);

   List<StreamEntryID> xclaimJustId(String var1, String var2, String var3, long var4, XClaimParams var6, StreamEntryID... var7);

   Entry<StreamEntryID, List<StreamEntry>> xautoclaim(String var1, String var2, String var3, long var4, StreamEntryID var6, XAutoClaimParams var7);

   Entry<StreamEntryID, List<StreamEntryID>> xautoclaimJustId(String var1, String var2, String var3, long var4, StreamEntryID var6, XAutoClaimParams var7);

   StreamInfo xinfoStream(String var1);

   StreamFullInfo xinfoStreamFull(String var1);

   StreamFullInfo xinfoStreamFull(String var1, int var2);

   List<StreamGroupInfo> xinfoGroups(String var1);

   @Deprecated
   List<StreamConsumersInfo> xinfoConsumers(String var1, String var2);

   List<StreamConsumerInfo> xinfoConsumers2(String var1, String var2);

   List<Entry<String, List<StreamEntry>>> xread(XReadParams var1, Map<String, StreamEntryID> var2);

   Map<String, List<StreamEntry>> xreadAsMap(XReadParams var1, Map<String, StreamEntryID> var2);

   List<Entry<String, List<StreamEntry>>> xreadGroup(String var1, String var2, XReadGroupParams var3, Map<String, StreamEntryID> var4);

   Map<String, List<StreamEntry>> xreadGroupAsMap(String var1, String var2, XReadGroupParams var3, Map<String, StreamEntryID> var4);
}
