package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
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

public interface StreamPipelineCommands {
   Response<StreamEntryID> xadd(String var1, StreamEntryID var2, Map<String, String> var3);

   default Response<StreamEntryID> xadd(String key, Map<String, String> hash, XAddParams params) {
      return this.xadd(key, params, hash);
   }

   Response<StreamEntryID> xadd(String var1, XAddParams var2, Map<String, String> var3);

   Response<Long> xlen(String var1);

   Response<List<StreamEntry>> xrange(String var1, StreamEntryID var2, StreamEntryID var3);

   Response<List<StreamEntry>> xrange(String var1, StreamEntryID var2, StreamEntryID var3, int var4);

   Response<List<StreamEntry>> xrevrange(String var1, StreamEntryID var2, StreamEntryID var3);

   Response<List<StreamEntry>> xrevrange(String var1, StreamEntryID var2, StreamEntryID var3, int var4);

   Response<List<StreamEntry>> xrange(String var1, String var2, String var3);

   Response<List<StreamEntry>> xrange(String var1, String var2, String var3, int var4);

   Response<List<StreamEntry>> xrevrange(String var1, String var2, String var3);

   Response<List<StreamEntry>> xrevrange(String var1, String var2, String var3, int var4);

   Response<Long> xack(String var1, String var2, StreamEntryID... var3);

   Response<String> xgroupCreate(String var1, String var2, StreamEntryID var3, boolean var4);

   Response<String> xgroupSetID(String var1, String var2, StreamEntryID var3);

   Response<Long> xgroupDestroy(String var1, String var2);

   Response<Boolean> xgroupCreateConsumer(String var1, String var2, String var3);

   Response<Long> xgroupDelConsumer(String var1, String var2, String var3);

   Response<StreamPendingSummary> xpending(String var1, String var2);

   Response<List<StreamPendingEntry>> xpending(String var1, String var2, XPendingParams var3);

   Response<Long> xdel(String var1, StreamEntryID... var2);

   Response<Long> xtrim(String var1, long var2, boolean var4);

   Response<Long> xtrim(String var1, XTrimParams var2);

   Response<List<StreamEntry>> xclaim(String var1, String var2, String var3, long var4, XClaimParams var6, StreamEntryID... var7);

   Response<List<StreamEntryID>> xclaimJustId(String var1, String var2, String var3, long var4, XClaimParams var6, StreamEntryID... var7);

   Response<Entry<StreamEntryID, List<StreamEntry>>> xautoclaim(String var1, String var2, String var3, long var4, StreamEntryID var6, XAutoClaimParams var7);

   Response<Entry<StreamEntryID, List<StreamEntryID>>> xautoclaimJustId(
      String var1, String var2, String var3, long var4, StreamEntryID var6, XAutoClaimParams var7
   );

   Response<StreamInfo> xinfoStream(String var1);

   Response<StreamFullInfo> xinfoStreamFull(String var1);

   Response<StreamFullInfo> xinfoStreamFull(String var1, int var2);

   Response<List<StreamGroupInfo>> xinfoGroups(String var1);

   @Deprecated
   Response<List<StreamConsumersInfo>> xinfoConsumers(String var1, String var2);

   Response<List<StreamConsumerInfo>> xinfoConsumers2(String var1, String var2);

   Response<List<Entry<String, List<StreamEntry>>>> xread(XReadParams var1, Map<String, StreamEntryID> var2);

   Response<Map<String, List<StreamEntry>>> xreadAsMap(XReadParams var1, Map<String, StreamEntryID> var2);

   Response<List<Entry<String, List<StreamEntry>>>> xreadGroup(String var1, String var2, XReadGroupParams var3, Map<String, StreamEntryID> var4);

   Response<Map<String, List<StreamEntry>>> xreadGroupAsMap(String var1, String var2, XReadGroupParams var3, Map<String, StreamEntryID> var4);
}
