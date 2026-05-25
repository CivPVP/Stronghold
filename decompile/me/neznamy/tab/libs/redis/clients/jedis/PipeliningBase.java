package me.neznamy.tab.libs.redis.clients.jedis;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import me.neznamy.tab.libs.org.json.JSONArray;
import me.neznamy.tab.libs.redis.clients.jedis.args.BitCountOption;
import me.neznamy.tab.libs.redis.clients.jedis.args.BitOP;
import me.neznamy.tab.libs.redis.clients.jedis.args.ExpiryOption;
import me.neznamy.tab.libs.redis.clients.jedis.args.FlushMode;
import me.neznamy.tab.libs.redis.clients.jedis.args.FunctionRestorePolicy;
import me.neznamy.tab.libs.redis.clients.jedis.args.GeoUnit;
import me.neznamy.tab.libs.redis.clients.jedis.args.ListDirection;
import me.neznamy.tab.libs.redis.clients.jedis.args.ListPosition;
import me.neznamy.tab.libs.redis.clients.jedis.args.SortedSetOption;
import me.neznamy.tab.libs.redis.clients.jedis.bloom.BFInsertParams;
import me.neznamy.tab.libs.redis.clients.jedis.bloom.BFReserveParams;
import me.neznamy.tab.libs.redis.clients.jedis.bloom.CFInsertParams;
import me.neznamy.tab.libs.redis.clients.jedis.bloom.CFReserveParams;
import me.neznamy.tab.libs.redis.clients.jedis.bloom.TDigestMergeParams;
import me.neznamy.tab.libs.redis.clients.jedis.commands.PipelineBinaryCommands;
import me.neznamy.tab.libs.redis.clients.jedis.commands.PipelineCommands;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ProtocolCommand;
import me.neznamy.tab.libs.redis.clients.jedis.commands.RedisModulePipelineCommands;
import me.neznamy.tab.libs.redis.clients.jedis.graph.GraphCommandObjects;
import me.neznamy.tab.libs.redis.clients.jedis.graph.ResultSet;
import me.neznamy.tab.libs.redis.clients.jedis.json.JsonObjectMapper;
import me.neznamy.tab.libs.redis.clients.jedis.json.JsonSetParams;
import me.neznamy.tab.libs.redis.clients.jedis.json.Path;
import me.neznamy.tab.libs.redis.clients.jedis.json.Path2;
import me.neznamy.tab.libs.redis.clients.jedis.params.BitPosParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.GeoAddParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.GeoRadiusParam;
import me.neznamy.tab.libs.redis.clients.jedis.params.GeoRadiusStoreParam;
import me.neznamy.tab.libs.redis.clients.jedis.params.GeoSearchParam;
import me.neznamy.tab.libs.redis.clients.jedis.params.GetExParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.LCSParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.LPosParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.MigrateParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.RestoreParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ScanParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.SetParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.SortingParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XAddParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XAutoClaimParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XClaimParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XPendingParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XReadGroupParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XReadParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XTrimParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ZAddParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ZIncrByParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ZParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ZRangeParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.FunctionStats;
import me.neznamy.tab.libs.redis.clients.jedis.resps.GeoRadiusResponse;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LCSMatchResult;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LibraryInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ScanResult;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamConsumerInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamConsumersInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamEntry;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamFullInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamGroupInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamPendingEntry;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamPendingSummary;
import me.neznamy.tab.libs.redis.clients.jedis.resps.Tuple;
import me.neznamy.tab.libs.redis.clients.jedis.search.FTCreateParams;
import me.neznamy.tab.libs.redis.clients.jedis.search.FTSearchParams;
import me.neznamy.tab.libs.redis.clients.jedis.search.FTSpellCheckParams;
import me.neznamy.tab.libs.redis.clients.jedis.search.IndexOptions;
import me.neznamy.tab.libs.redis.clients.jedis.search.Query;
import me.neznamy.tab.libs.redis.clients.jedis.search.Schema;
import me.neznamy.tab.libs.redis.clients.jedis.search.SearchResult;
import me.neznamy.tab.libs.redis.clients.jedis.search.aggr.AggregationBuilder;
import me.neznamy.tab.libs.redis.clients.jedis.search.aggr.AggregationResult;
import me.neznamy.tab.libs.redis.clients.jedis.search.schemafields.SchemaField;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.AggregationType;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSAddParams;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSAlterParams;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSCreateParams;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSDecrByParams;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSElement;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSGetParams;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSIncrByParams;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSInfo;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSMGetElement;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSMGetParams;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSMRangeElements;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSMRangeParams;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSRangeParams;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public abstract class PipeliningBase implements PipelineCommands, PipelineBinaryCommands, RedisModulePipelineCommands {
   protected final CommandObjects commandObjects;
   private GraphCommandObjects graphCommandObjects;

   protected PipeliningBase(CommandObjects commandObjects) {
      this.commandObjects = commandObjects;
   }

   protected final void setGraphCommands(GraphCommandObjects graphCommandObjects) {
      this.graphCommandObjects = graphCommandObjects;
   }

   protected abstract <T> Response<T> appendCommand(CommandObject<T> var1);

   @Override
   public Response<Boolean> exists(String key) {
      return this.appendCommand(this.commandObjects.exists(key));
   }

   @Override
   public Response<Long> exists(String... keys) {
      return this.appendCommand(this.commandObjects.exists(keys));
   }

   @Override
   public Response<Long> persist(String key) {
      return this.appendCommand(this.commandObjects.persist(key));
   }

   @Override
   public Response<String> type(String key) {
      return this.appendCommand(this.commandObjects.type(key));
   }

   @Override
   public Response<byte[]> dump(String key) {
      return this.appendCommand(this.commandObjects.dump(key));
   }

   @Override
   public Response<String> restore(String key, long ttl, byte[] serializedValue) {
      return this.appendCommand(this.commandObjects.restore(key, ttl, serializedValue));
   }

   @Override
   public Response<String> restore(String key, long ttl, byte[] serializedValue, RestoreParams params) {
      return this.appendCommand(this.commandObjects.restore(key, ttl, serializedValue, params));
   }

   @Override
   public Response<Long> expire(String key, long seconds) {
      return this.appendCommand(this.commandObjects.expire(key, seconds));
   }

   @Override
   public Response<Long> expire(String key, long seconds, ExpiryOption expiryOption) {
      return this.appendCommand(this.commandObjects.expire(key, seconds, expiryOption));
   }

   @Override
   public Response<Long> pexpire(String key, long milliseconds) {
      return this.appendCommand(this.commandObjects.pexpire(key, milliseconds));
   }

   @Override
   public Response<Long> pexpire(String key, long milliseconds, ExpiryOption expiryOption) {
      return this.appendCommand(this.commandObjects.pexpire(key, milliseconds, expiryOption));
   }

   @Override
   public Response<Long> expireTime(String key) {
      return this.appendCommand(this.commandObjects.expireTime(key));
   }

   @Override
   public Response<Long> pexpireTime(String key) {
      return this.appendCommand(this.commandObjects.pexpireTime(key));
   }

   @Override
   public Response<Long> expireAt(String key, long unixTime) {
      return this.appendCommand(this.commandObjects.expireAt(key, unixTime));
   }

   @Override
   public Response<Long> expireAt(String key, long unixTime, ExpiryOption expiryOption) {
      return this.appendCommand(this.commandObjects.expireAt(key, unixTime, expiryOption));
   }

   @Override
   public Response<Long> pexpireAt(String key, long millisecondsTimestamp) {
      return this.appendCommand(this.commandObjects.pexpireAt(key, millisecondsTimestamp));
   }

   @Override
   public Response<Long> pexpireAt(String key, long millisecondsTimestamp, ExpiryOption expiryOption) {
      return this.appendCommand(this.commandObjects.pexpireAt(key, millisecondsTimestamp, expiryOption));
   }

   @Override
   public Response<Long> ttl(String key) {
      return this.appendCommand(this.commandObjects.ttl(key));
   }

   @Override
   public Response<Long> pttl(String key) {
      return this.appendCommand(this.commandObjects.pttl(key));
   }

   @Override
   public Response<Long> touch(String key) {
      return this.appendCommand(this.commandObjects.touch(key));
   }

   @Override
   public Response<Long> touch(String... keys) {
      return this.appendCommand(this.commandObjects.touch(keys));
   }

   @Override
   public Response<List<String>> sort(String key) {
      return this.appendCommand(this.commandObjects.sort(key));
   }

   @Override
   public Response<Long> sort(String key, String dstKey) {
      return this.appendCommand(this.commandObjects.sort(key, dstKey));
   }

   @Override
   public Response<List<String>> sort(String key, SortingParams sortingParams) {
      return this.appendCommand(this.commandObjects.sort(key, sortingParams));
   }

   @Override
   public Response<Long> sort(String key, SortingParams sortingParams, String dstKey) {
      return this.appendCommand(this.commandObjects.sort(key, sortingParams, dstKey));
   }

   @Override
   public Response<List<String>> sortReadonly(String key, SortingParams sortingParams) {
      return this.appendCommand(this.commandObjects.sortReadonly(key, sortingParams));
   }

   @Override
   public Response<Long> del(String key) {
      return this.appendCommand(this.commandObjects.del(key));
   }

   @Override
   public Response<Long> del(String... keys) {
      return this.appendCommand(this.commandObjects.del(keys));
   }

   @Override
   public Response<Long> unlink(String key) {
      return this.appendCommand(this.commandObjects.unlink(key));
   }

   @Override
   public Response<Long> unlink(String... keys) {
      return this.appendCommand(this.commandObjects.unlink(keys));
   }

   @Override
   public Response<Boolean> copy(String srcKey, String dstKey, boolean replace) {
      return this.appendCommand(this.commandObjects.copy(srcKey, dstKey, replace));
   }

   @Override
   public Response<String> rename(String oldkey, String newkey) {
      return this.appendCommand(this.commandObjects.rename(oldkey, newkey));
   }

   @Override
   public Response<Long> renamenx(String oldkey, String newkey) {
      return this.appendCommand(this.commandObjects.renamenx(oldkey, newkey));
   }

   @Override
   public Response<Long> memoryUsage(String key) {
      return this.appendCommand(this.commandObjects.memoryUsage(key));
   }

   @Override
   public Response<Long> memoryUsage(String key, int samples) {
      return this.appendCommand(this.commandObjects.memoryUsage(key, samples));
   }

   @Override
   public Response<Long> objectRefcount(String key) {
      return this.appendCommand(this.commandObjects.objectRefcount(key));
   }

   @Override
   public Response<String> objectEncoding(String key) {
      return this.appendCommand(this.commandObjects.objectEncoding(key));
   }

   @Override
   public Response<Long> objectIdletime(String key) {
      return this.appendCommand(this.commandObjects.objectIdletime(key));
   }

   @Override
   public Response<Long> objectFreq(String key) {
      return this.appendCommand(this.commandObjects.objectFreq(key));
   }

   @Override
   public Response<String> migrate(String host, int port, String key, int timeout) {
      return this.appendCommand(this.commandObjects.migrate(host, port, key, timeout));
   }

   @Override
   public Response<String> migrate(String host, int port, int timeout, MigrateParams params, String... keys) {
      return this.appendCommand(this.commandObjects.migrate(host, port, timeout, params, keys));
   }

   @Override
   public Response<Set<String>> keys(String pattern) {
      return this.appendCommand(this.commandObjects.keys(pattern));
   }

   @Override
   public Response<ScanResult<String>> scan(String cursor) {
      return this.appendCommand(this.commandObjects.scan(cursor));
   }

   @Override
   public Response<ScanResult<String>> scan(String cursor, ScanParams params) {
      return this.appendCommand(this.commandObjects.scan(cursor, params));
   }

   @Override
   public Response<ScanResult<String>> scan(String cursor, ScanParams params, String type) {
      return this.appendCommand(this.commandObjects.scan(cursor, params, type));
   }

   @Override
   public Response<String> randomKey() {
      return this.appendCommand(this.commandObjects.randomKey());
   }

   @Override
   public Response<String> get(String key) {
      return this.appendCommand(this.commandObjects.get(key));
   }

   @Override
   public Response<String> setGet(String key, String value) {
      return this.appendCommand(this.commandObjects.setGet(key, value));
   }

   @Override
   public Response<String> setGet(String key, String value, SetParams params) {
      return this.appendCommand(this.commandObjects.setGet(key, value, params));
   }

   @Override
   public Response<String> getDel(String key) {
      return this.appendCommand(this.commandObjects.getDel(key));
   }

   @Override
   public Response<String> getEx(String key, GetExParams params) {
      return this.appendCommand(this.commandObjects.getEx(key, params));
   }

   @Override
   public Response<Boolean> setbit(String key, long offset, boolean value) {
      return this.appendCommand(this.commandObjects.setbit(key, offset, value));
   }

   @Override
   public Response<Boolean> getbit(String key, long offset) {
      return this.appendCommand(this.commandObjects.getbit(key, offset));
   }

   @Override
   public Response<Long> setrange(String key, long offset, String value) {
      return this.appendCommand(this.commandObjects.setrange(key, offset, value));
   }

   @Override
   public Response<String> getrange(String key, long startOffset, long endOffset) {
      return this.appendCommand(this.commandObjects.getrange(key, startOffset, endOffset));
   }

   @Deprecated
   @Override
   public Response<String> getSet(String key, String value) {
      return this.appendCommand(this.commandObjects.getSet(key, value));
   }

   @Override
   public Response<Long> setnx(String key, String value) {
      return this.appendCommand(this.commandObjects.setnx(key, value));
   }

   @Override
   public Response<String> setex(String key, long seconds, String value) {
      return this.appendCommand(this.commandObjects.setex(key, seconds, value));
   }

   @Override
   public Response<String> psetex(String key, long milliseconds, String value) {
      return this.appendCommand(this.commandObjects.psetex(key, milliseconds, value));
   }

   @Override
   public Response<List<String>> mget(String... keys) {
      return this.appendCommand(this.commandObjects.mget(keys));
   }

   @Override
   public Response<String> mset(String... keysvalues) {
      return this.appendCommand(this.commandObjects.mset(keysvalues));
   }

   @Override
   public Response<Long> msetnx(String... keysvalues) {
      return this.appendCommand(this.commandObjects.msetnx(keysvalues));
   }

   @Override
   public Response<Long> incr(String key) {
      return this.appendCommand(this.commandObjects.incr(key));
   }

   @Override
   public Response<Long> incrBy(String key, long increment) {
      return this.appendCommand(this.commandObjects.incrBy(key, increment));
   }

   @Override
   public Response<Double> incrByFloat(String key, double increment) {
      return this.appendCommand(this.commandObjects.incrByFloat(key, increment));
   }

   @Override
   public Response<Long> decr(String key) {
      return this.appendCommand(this.commandObjects.decr(key));
   }

   @Override
   public Response<Long> decrBy(String key, long decrement) {
      return this.appendCommand(this.commandObjects.decrBy(key, decrement));
   }

   @Override
   public Response<Long> append(String key, String value) {
      return this.appendCommand(this.commandObjects.append(key, value));
   }

   @Override
   public Response<String> substr(String key, int start, int end) {
      return this.appendCommand(this.commandObjects.substr(key, start, end));
   }

   @Override
   public Response<Long> strlen(String key) {
      return this.appendCommand(this.commandObjects.strlen(key));
   }

   @Override
   public Response<Long> bitcount(String key) {
      return this.appendCommand(this.commandObjects.bitcount(key));
   }

   @Override
   public Response<Long> bitcount(String key, long start, long end) {
      return this.appendCommand(this.commandObjects.bitcount(key, start, end));
   }

   @Override
   public Response<Long> bitcount(String key, long start, long end, BitCountOption option) {
      return this.appendCommand(this.commandObjects.bitcount(key, start, end, option));
   }

   @Override
   public Response<Long> bitpos(String key, boolean value) {
      return this.appendCommand(this.commandObjects.bitpos(key, value));
   }

   @Override
   public Response<Long> bitpos(String key, boolean value, BitPosParams params) {
      return this.appendCommand(this.commandObjects.bitpos(key, value, params));
   }

   @Override
   public Response<List<Long>> bitfield(String key, String... arguments) {
      return this.appendCommand(this.commandObjects.bitfield(key, arguments));
   }

   @Override
   public Response<List<Long>> bitfieldReadonly(String key, String... arguments) {
      return this.appendCommand(this.commandObjects.bitfieldReadonly(key, arguments));
   }

   @Override
   public Response<Long> bitop(BitOP op, String destKey, String... srcKeys) {
      return this.appendCommand(this.commandObjects.bitop(op, destKey, srcKeys));
   }

   @Override
   public Response<LCSMatchResult> lcs(String keyA, String keyB, LCSParams params) {
      return this.appendCommand(this.commandObjects.lcs(keyA, keyB, params));
   }

   @Override
   public Response<String> set(String key, String value) {
      return this.appendCommand(this.commandObjects.set(key, value));
   }

   @Override
   public Response<String> set(String key, String value, SetParams params) {
      return this.appendCommand(this.commandObjects.set(key, value, params));
   }

   @Override
   public Response<Long> rpush(String key, String... string) {
      return this.appendCommand(this.commandObjects.rpush(key, string));
   }

   @Override
   public Response<Long> lpush(String key, String... string) {
      return this.appendCommand(this.commandObjects.lpush(key, string));
   }

   @Override
   public Response<Long> llen(String key) {
      return this.appendCommand(this.commandObjects.llen(key));
   }

   @Override
   public Response<List<String>> lrange(String key, long start, long stop) {
      return this.appendCommand(this.commandObjects.lrange(key, start, stop));
   }

   @Override
   public Response<String> ltrim(String key, long start, long stop) {
      return this.appendCommand(this.commandObjects.ltrim(key, start, stop));
   }

   @Override
   public Response<String> lindex(String key, long index) {
      return this.appendCommand(this.commandObjects.lindex(key, index));
   }

   @Override
   public Response<String> lset(String key, long index, String value) {
      return this.appendCommand(this.commandObjects.lset(key, index, value));
   }

   @Override
   public Response<Long> lrem(String key, long count, String value) {
      return this.appendCommand(this.commandObjects.lrem(key, count, value));
   }

   @Override
   public Response<String> lpop(String key) {
      return this.appendCommand(this.commandObjects.lpop(key));
   }

   @Override
   public Response<List<String>> lpop(String key, int count) {
      return this.appendCommand(this.commandObjects.lpop(key, count));
   }

   @Override
   public Response<Long> lpos(String key, String element) {
      return this.appendCommand(this.commandObjects.lpos(key, element));
   }

   @Override
   public Response<Long> lpos(String key, String element, LPosParams params) {
      return this.appendCommand(this.commandObjects.lpos(key, element, params));
   }

   @Override
   public Response<List<Long>> lpos(String key, String element, LPosParams params, long count) {
      return this.appendCommand(this.commandObjects.lpos(key, element, params, count));
   }

   @Override
   public Response<String> rpop(String key) {
      return this.appendCommand(this.commandObjects.rpop(key));
   }

   @Override
   public Response<List<String>> rpop(String key, int count) {
      return this.appendCommand(this.commandObjects.rpop(key, count));
   }

   @Override
   public Response<Long> linsert(String key, ListPosition where, String pivot, String value) {
      return this.appendCommand(this.commandObjects.linsert(key, where, pivot, value));
   }

   @Override
   public Response<Long> lpushx(String key, String... strings) {
      return this.appendCommand(this.commandObjects.lpushx(key, strings));
   }

   @Override
   public Response<Long> rpushx(String key, String... strings) {
      return this.appendCommand(this.commandObjects.rpushx(key, strings));
   }

   @Override
   public Response<List<String>> blpop(int timeout, String key) {
      return this.appendCommand(this.commandObjects.blpop(timeout, key));
   }

   @Override
   public Response<KeyValue<String, String>> blpop(double timeout, String key) {
      return this.appendCommand(this.commandObjects.blpop(timeout, key));
   }

   @Override
   public Response<List<String>> brpop(int timeout, String key) {
      return this.appendCommand(this.commandObjects.brpop(timeout, key));
   }

   @Override
   public Response<KeyValue<String, String>> brpop(double timeout, String key) {
      return this.appendCommand(this.commandObjects.brpop(timeout, key));
   }

   @Override
   public Response<List<String>> blpop(int timeout, String... keys) {
      return this.appendCommand(this.commandObjects.blpop(timeout, keys));
   }

   @Override
   public Response<KeyValue<String, String>> blpop(double timeout, String... keys) {
      return this.appendCommand(this.commandObjects.blpop(timeout, keys));
   }

   @Override
   public Response<List<String>> brpop(int timeout, String... keys) {
      return this.appendCommand(this.commandObjects.brpop(timeout, keys));
   }

   @Override
   public Response<KeyValue<String, String>> brpop(double timeout, String... keys) {
      return this.appendCommand(this.commandObjects.brpop(timeout, keys));
   }

   @Override
   public Response<String> rpoplpush(String srcKey, String dstKey) {
      return this.appendCommand(this.commandObjects.rpoplpush(srcKey, dstKey));
   }

   @Override
   public Response<String> brpoplpush(String source, String destination, int timeout) {
      return this.appendCommand(this.commandObjects.brpoplpush(source, destination, timeout));
   }

   @Override
   public Response<String> lmove(String srcKey, String dstKey, ListDirection from, ListDirection to) {
      return this.appendCommand(this.commandObjects.lmove(srcKey, dstKey, from, to));
   }

   @Override
   public Response<String> blmove(String srcKey, String dstKey, ListDirection from, ListDirection to, double timeout) {
      return this.appendCommand(this.commandObjects.blmove(srcKey, dstKey, from, to, timeout));
   }

   @Override
   public Response<KeyValue<String, List<String>>> lmpop(ListDirection direction, String... keys) {
      return this.appendCommand(this.commandObjects.lmpop(direction, keys));
   }

   @Override
   public Response<KeyValue<String, List<String>>> lmpop(ListDirection direction, int count, String... keys) {
      return this.appendCommand(this.commandObjects.lmpop(direction, count, keys));
   }

   @Override
   public Response<KeyValue<String, List<String>>> blmpop(double timeout, ListDirection direction, String... keys) {
      return this.appendCommand(this.commandObjects.blmpop(timeout, direction, keys));
   }

   @Override
   public Response<KeyValue<String, List<String>>> blmpop(double timeout, ListDirection direction, int count, String... keys) {
      return this.appendCommand(this.commandObjects.blmpop(timeout, direction, count, keys));
   }

   @Override
   public Response<Long> hset(String key, String field, String value) {
      return this.appendCommand(this.commandObjects.hset(key, field, value));
   }

   @Override
   public Response<Long> hset(String key, Map<String, String> hash) {
      return this.appendCommand(this.commandObjects.hset(key, hash));
   }

   @Override
   public Response<String> hget(String key, String field) {
      return this.appendCommand(this.commandObjects.hget(key, field));
   }

   @Override
   public Response<Long> hsetnx(String key, String field, String value) {
      return this.appendCommand(this.commandObjects.hsetnx(key, field, value));
   }

   @Override
   public Response<String> hmset(String key, Map<String, String> hash) {
      return this.appendCommand(this.commandObjects.hmset(key, hash));
   }

   @Override
   public Response<List<String>> hmget(String key, String... fields) {
      return this.appendCommand(this.commandObjects.hmget(key, fields));
   }

   @Override
   public Response<Long> hincrBy(String key, String field, long value) {
      return this.appendCommand(this.commandObjects.hincrBy(key, field, value));
   }

   @Override
   public Response<Double> hincrByFloat(String key, String field, double value) {
      return this.appendCommand(this.commandObjects.hincrByFloat(key, field, value));
   }

   @Override
   public Response<Boolean> hexists(String key, String field) {
      return this.appendCommand(this.commandObjects.hexists(key, field));
   }

   @Override
   public Response<Long> hdel(String key, String... field) {
      return this.appendCommand(this.commandObjects.hdel(key, field));
   }

   @Override
   public Response<Long> hlen(String key) {
      return this.appendCommand(this.commandObjects.hlen(key));
   }

   @Override
   public Response<Set<String>> hkeys(String key) {
      return this.appendCommand(this.commandObjects.hkeys(key));
   }

   @Override
   public Response<List<String>> hvals(String key) {
      return this.appendCommand(this.commandObjects.hvals(key));
   }

   @Override
   public Response<Map<String, String>> hgetAll(String key) {
      return this.appendCommand(this.commandObjects.hgetAll(key));
   }

   @Override
   public Response<String> hrandfield(String key) {
      return this.appendCommand(this.commandObjects.hrandfield(key));
   }

   @Override
   public Response<List<String>> hrandfield(String key, long count) {
      return this.appendCommand(this.commandObjects.hrandfield(key, count));
   }

   @Override
   public Response<List<Entry<String, String>>> hrandfieldWithValues(String key, long count) {
      return this.appendCommand(this.commandObjects.hrandfieldWithValues(key, count));
   }

   @Override
   public Response<ScanResult<Entry<String, String>>> hscan(String key, String cursor, ScanParams params) {
      return this.appendCommand(this.commandObjects.hscan(key, cursor, params));
   }

   @Override
   public Response<ScanResult<String>> hscanNoValues(String key, String cursor, ScanParams params) {
      return this.appendCommand(this.commandObjects.hscanNoValues(key, cursor, params));
   }

   @Override
   public Response<Long> hstrlen(String key, String field) {
      return this.appendCommand(this.commandObjects.hstrlen(key, field));
   }

   @Override
   public Response<List<Long>> hexpire(String key, long seconds, String... fields) {
      return this.appendCommand(this.commandObjects.hexpire(key, seconds, fields));
   }

   @Override
   public Response<List<Long>> hexpire(String key, long seconds, ExpiryOption condition, String... fields) {
      return this.appendCommand(this.commandObjects.hexpire(key, seconds, condition, fields));
   }

   @Override
   public Response<List<Long>> hpexpire(String key, long milliseconds, String... fields) {
      return this.appendCommand(this.commandObjects.hpexpire(key, milliseconds, fields));
   }

   @Override
   public Response<List<Long>> hpexpire(String key, long milliseconds, ExpiryOption condition, String... fields) {
      return this.appendCommand(this.commandObjects.hpexpire(key, milliseconds, condition, fields));
   }

   @Override
   public Response<List<Long>> hexpireAt(String key, long unixTimeSeconds, String... fields) {
      return this.appendCommand(this.commandObjects.hexpireAt(key, unixTimeSeconds, fields));
   }

   @Override
   public Response<List<Long>> hexpireAt(String key, long unixTimeSeconds, ExpiryOption condition, String... fields) {
      return this.appendCommand(this.commandObjects.hexpireAt(key, unixTimeSeconds, condition, fields));
   }

   @Override
   public Response<List<Long>> hpexpireAt(String key, long unixTimeMillis, String... fields) {
      return this.appendCommand(this.commandObjects.hpexpireAt(key, unixTimeMillis, fields));
   }

   @Override
   public Response<List<Long>> hpexpireAt(String key, long unixTimeMillis, ExpiryOption condition, String... fields) {
      return this.appendCommand(this.commandObjects.hpexpireAt(key, unixTimeMillis, condition, fields));
   }

   @Override
   public Response<List<Long>> hexpireTime(String key, String... fields) {
      return this.appendCommand(this.commandObjects.hexpireTime(key, fields));
   }

   @Override
   public Response<List<Long>> hpexpireTime(String key, String... fields) {
      return this.appendCommand(this.commandObjects.hpexpireTime(key, fields));
   }

   @Override
   public Response<List<Long>> httl(String key, String... fields) {
      return this.appendCommand(this.commandObjects.httl(key, fields));
   }

   @Override
   public Response<List<Long>> hpttl(String key, String... fields) {
      return this.appendCommand(this.commandObjects.hpttl(key, fields));
   }

   @Override
   public Response<List<Long>> hpersist(String key, String... fields) {
      return this.appendCommand(this.commandObjects.hpersist(key, fields));
   }

   @Override
   public Response<Long> sadd(String key, String... members) {
      return this.appendCommand(this.commandObjects.sadd(key, members));
   }

   @Override
   public Response<Set<String>> smembers(String key) {
      return this.appendCommand(this.commandObjects.smembers(key));
   }

   @Override
   public Response<Long> srem(String key, String... members) {
      return this.appendCommand(this.commandObjects.srem(key, members));
   }

   @Override
   public Response<String> spop(String key) {
      return this.appendCommand(this.commandObjects.spop(key));
   }

   @Override
   public Response<Set<String>> spop(String key, long count) {
      return this.appendCommand(this.commandObjects.spop(key, count));
   }

   @Override
   public Response<Long> scard(String key) {
      return this.appendCommand(this.commandObjects.scard(key));
   }

   @Override
   public Response<Boolean> sismember(String key, String member) {
      return this.appendCommand(this.commandObjects.sismember(key, member));
   }

   @Override
   public Response<List<Boolean>> smismember(String key, String... members) {
      return this.appendCommand(this.commandObjects.smismember(key, members));
   }

   @Override
   public Response<String> srandmember(String key) {
      return this.appendCommand(this.commandObjects.srandmember(key));
   }

   @Override
   public Response<List<String>> srandmember(String key, int count) {
      return this.appendCommand(this.commandObjects.srandmember(key, count));
   }

   @Override
   public Response<ScanResult<String>> sscan(String key, String cursor, ScanParams params) {
      return this.appendCommand(this.commandObjects.sscan(key, cursor, params));
   }

   @Override
   public Response<Set<String>> sdiff(String... keys) {
      return this.appendCommand(this.commandObjects.sdiff(keys));
   }

   @Override
   public Response<Long> sdiffstore(String dstKey, String... keys) {
      return this.appendCommand(this.commandObjects.sdiffstore(dstKey, keys));
   }

   @Override
   public Response<Set<String>> sinter(String... keys) {
      return this.appendCommand(this.commandObjects.sinter(keys));
   }

   @Override
   public Response<Long> sinterstore(String dstKey, String... keys) {
      return this.appendCommand(this.commandObjects.sinterstore(dstKey, keys));
   }

   @Override
   public Response<Long> sintercard(String... keys) {
      return this.appendCommand(this.commandObjects.sintercard(keys));
   }

   @Override
   public Response<Long> sintercard(int limit, String... keys) {
      return this.appendCommand(this.commandObjects.sintercard(limit, keys));
   }

   @Override
   public Response<Set<String>> sunion(String... keys) {
      return this.appendCommand(this.commandObjects.sunion(keys));
   }

   @Override
   public Response<Long> sunionstore(String dstKey, String... keys) {
      return this.appendCommand(this.commandObjects.sunionstore(dstKey, keys));
   }

   @Override
   public Response<Long> smove(String srcKey, String dstKey, String member) {
      return this.appendCommand(this.commandObjects.smove(srcKey, dstKey, member));
   }

   @Override
   public Response<Long> zadd(String key, double score, String member) {
      return this.appendCommand(this.commandObjects.zadd(key, score, member));
   }

   @Override
   public Response<Long> zadd(String key, double score, String member, ZAddParams params) {
      return this.appendCommand(this.commandObjects.zadd(key, score, member, params));
   }

   @Override
   public Response<Long> zadd(String key, Map<String, Double> scoreMembers) {
      return this.appendCommand(this.commandObjects.zadd(key, scoreMembers));
   }

   @Override
   public Response<Long> zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
      return this.appendCommand(this.commandObjects.zadd(key, scoreMembers, params));
   }

   @Override
   public Response<Double> zaddIncr(String key, double score, String member, ZAddParams params) {
      return this.appendCommand(this.commandObjects.zaddIncr(key, score, member, params));
   }

   @Override
   public Response<Long> zrem(String key, String... members) {
      return this.appendCommand(this.commandObjects.zrem(key, members));
   }

   @Override
   public Response<Double> zincrby(String key, double increment, String member) {
      return this.appendCommand(this.commandObjects.zincrby(key, increment, member));
   }

   @Override
   public Response<Double> zincrby(String key, double increment, String member, ZIncrByParams params) {
      return this.appendCommand(this.commandObjects.zincrby(key, increment, member, params));
   }

   @Override
   public Response<Long> zrank(String key, String member) {
      return this.appendCommand(this.commandObjects.zrank(key, member));
   }

   @Override
   public Response<Long> zrevrank(String key, String member) {
      return this.appendCommand(this.commandObjects.zrevrank(key, member));
   }

   @Override
   public Response<KeyValue<Long, Double>> zrankWithScore(String key, String member) {
      return this.appendCommand(this.commandObjects.zrankWithScore(key, member));
   }

   @Override
   public Response<KeyValue<Long, Double>> zrevrankWithScore(String key, String member) {
      return this.appendCommand(this.commandObjects.zrevrankWithScore(key, member));
   }

   @Override
   public Response<List<String>> zrange(String key, long start, long stop) {
      return this.appendCommand(this.commandObjects.zrange(key, start, stop));
   }

   @Override
   public Response<List<String>> zrevrange(String key, long start, long stop) {
      return this.appendCommand(this.commandObjects.zrevrange(key, start, stop));
   }

   @Override
   public Response<List<Tuple>> zrangeWithScores(String key, long start, long stop) {
      return this.appendCommand(this.commandObjects.zrangeWithScores(key, start, stop));
   }

   @Override
   public Response<List<Tuple>> zrevrangeWithScores(String key, long start, long stop) {
      return this.appendCommand(this.commandObjects.zrevrangeWithScores(key, start, stop));
   }

   @Override
   public Response<String> zrandmember(String key) {
      return this.appendCommand(this.commandObjects.zrandmember(key));
   }

   @Override
   public Response<List<String>> zrandmember(String key, long count) {
      return this.appendCommand(this.commandObjects.zrandmember(key, count));
   }

   @Override
   public Response<List<Tuple>> zrandmemberWithScores(String key, long count) {
      return this.appendCommand(this.commandObjects.zrandmemberWithScores(key, count));
   }

   @Override
   public Response<Long> zcard(String key) {
      return this.appendCommand(this.commandObjects.zcard(key));
   }

   @Override
   public Response<Double> zscore(String key, String member) {
      return this.appendCommand(this.commandObjects.zscore(key, member));
   }

   @Override
   public Response<List<Double>> zmscore(String key, String... members) {
      return this.appendCommand(this.commandObjects.zmscore(key, members));
   }

   @Override
   public Response<Tuple> zpopmax(String key) {
      return this.appendCommand(this.commandObjects.zpopmax(key));
   }

   @Override
   public Response<List<Tuple>> zpopmax(String key, int count) {
      return this.appendCommand(this.commandObjects.zpopmax(key, count));
   }

   @Override
   public Response<Tuple> zpopmin(String key) {
      return this.appendCommand(this.commandObjects.zpopmin(key));
   }

   @Override
   public Response<List<Tuple>> zpopmin(String key, int count) {
      return this.appendCommand(this.commandObjects.zpopmin(key, count));
   }

   @Override
   public Response<Long> zcount(String key, double min, double max) {
      return this.appendCommand(this.commandObjects.zcount(key, min, max));
   }

   @Override
   public Response<Long> zcount(String key, String min, String max) {
      return this.appendCommand(this.commandObjects.zcount(key, min, max));
   }

   @Override
   public Response<List<String>> zrangeByScore(String key, double min, double max) {
      return this.appendCommand(this.commandObjects.zrangeByScore(key, min, max));
   }

   @Override
   public Response<List<String>> zrangeByScore(String key, String min, String max) {
      return this.appendCommand(this.commandObjects.zrangeByScore(key, min, max));
   }

   @Override
   public Response<List<String>> zrevrangeByScore(String key, double max, double min) {
      return this.appendCommand(this.commandObjects.zrevrangeByScore(key, max, min));
   }

   @Override
   public Response<List<String>> zrangeByScore(String key, double min, double max, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrangeByScore(key, min, max, offset, count));
   }

   @Override
   public Response<List<String>> zrevrangeByScore(String key, String max, String min) {
      return this.appendCommand(this.commandObjects.zrevrangeByScore(key, max, min));
   }

   @Override
   public Response<List<String>> zrangeByScore(String key, String min, String max, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrangeByScore(key, min, max, offset, count));
   }

   @Override
   public Response<List<String>> zrevrangeByScore(String key, double max, double min, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrevrangeByScore(key, max, min, offset, count));
   }

   @Override
   public Response<List<Tuple>> zrangeByScoreWithScores(String key, double min, double max) {
      return this.appendCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max));
   }

   @Override
   public Response<List<Tuple>> zrevrangeByScoreWithScores(String key, double max, double min) {
      return this.appendCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min));
   }

   @Override
   public Response<List<Tuple>> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max, offset, count));
   }

   @Override
   public Response<List<String>> zrevrangeByScore(String key, String max, String min, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrevrangeByScore(key, max, min, offset, count));
   }

   @Override
   public Response<List<Tuple>> zrangeByScoreWithScores(String key, String min, String max) {
      return this.appendCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max));
   }

   @Override
   public Response<List<Tuple>> zrevrangeByScoreWithScores(String key, String max, String min) {
      return this.appendCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min));
   }

   @Override
   public Response<List<Tuple>> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max, offset, count));
   }

   @Override
   public Response<List<Tuple>> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min, offset, count));
   }

   @Override
   public Response<List<Tuple>> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min, offset, count));
   }

   @Override
   public Response<List<String>> zrange(String key, ZRangeParams zRangeParams) {
      return this.appendCommand(this.commandObjects.zrange(key, zRangeParams));
   }

   @Override
   public Response<List<Tuple>> zrangeWithScores(String key, ZRangeParams zRangeParams) {
      return this.appendCommand(this.commandObjects.zrangeWithScores(key, zRangeParams));
   }

   @Override
   public Response<Long> zrangestore(String dest, String src, ZRangeParams zRangeParams) {
      return this.appendCommand(this.commandObjects.zrangestore(dest, src, zRangeParams));
   }

   @Override
   public Response<Long> zremrangeByRank(String key, long start, long stop) {
      return this.appendCommand(this.commandObjects.zremrangeByRank(key, start, stop));
   }

   @Override
   public Response<Long> zremrangeByScore(String key, double min, double max) {
      return this.appendCommand(this.commandObjects.zremrangeByScore(key, min, max));
   }

   @Override
   public Response<Long> zremrangeByScore(String key, String min, String max) {
      return this.appendCommand(this.commandObjects.zremrangeByScore(key, min, max));
   }

   @Override
   public Response<Long> zlexcount(String key, String min, String max) {
      return this.appendCommand(this.commandObjects.zlexcount(key, min, max));
   }

   @Override
   public Response<List<String>> zrangeByLex(String key, String min, String max) {
      return this.appendCommand(this.commandObjects.zrangeByLex(key, min, max));
   }

   @Override
   public Response<List<String>> zrangeByLex(String key, String min, String max, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrangeByLex(key, min, max, offset, count));
   }

   @Override
   public Response<List<String>> zrevrangeByLex(String key, String max, String min) {
      return this.appendCommand(this.commandObjects.zrevrangeByLex(key, max, min));
   }

   @Override
   public Response<List<String>> zrevrangeByLex(String key, String max, String min, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrevrangeByLex(key, max, min, offset, count));
   }

   @Override
   public Response<Long> zremrangeByLex(String key, String min, String max) {
      return this.appendCommand(this.commandObjects.zremrangeByLex(key, min, max));
   }

   @Override
   public Response<ScanResult<Tuple>> zscan(String key, String cursor, ScanParams params) {
      return this.appendCommand(this.commandObjects.zscan(key, cursor, params));
   }

   @Override
   public Response<KeyValue<String, Tuple>> bzpopmax(double timeout, String... keys) {
      return this.appendCommand(this.commandObjects.bzpopmax(timeout, keys));
   }

   @Override
   public Response<KeyValue<String, Tuple>> bzpopmin(double timeout, String... keys) {
      return this.appendCommand(this.commandObjects.bzpopmin(timeout, keys));
   }

   @Override
   public Response<KeyValue<String, List<Tuple>>> zmpop(SortedSetOption option, String... keys) {
      return this.appendCommand(this.commandObjects.zmpop(option, keys));
   }

   @Override
   public Response<KeyValue<String, List<Tuple>>> zmpop(SortedSetOption option, int count, String... keys) {
      return this.appendCommand(this.commandObjects.zmpop(option, count, keys));
   }

   @Override
   public Response<KeyValue<String, List<Tuple>>> bzmpop(double timeout, SortedSetOption option, String... keys) {
      return this.appendCommand(this.commandObjects.bzmpop(timeout, option, keys));
   }

   @Override
   public Response<KeyValue<String, List<Tuple>>> bzmpop(double timeout, SortedSetOption option, int count, String... keys) {
      return this.appendCommand(this.commandObjects.bzmpop(timeout, option, count, keys));
   }

   @Override
   public Response<List<String>> zdiff(String... keys) {
      return this.appendCommand(this.commandObjects.zdiff(keys));
   }

   @Override
   public Response<List<Tuple>> zdiffWithScores(String... keys) {
      return this.appendCommand(this.commandObjects.zdiffWithScores(keys));
   }

   @Deprecated
   @Override
   public Response<Long> zdiffStore(String dstKey, String... keys) {
      return this.appendCommand(this.commandObjects.zdiffStore(dstKey, keys));
   }

   @Override
   public Response<Long> zdiffstore(String dstKey, String... keys) {
      return this.appendCommand(this.commandObjects.zdiffstore(dstKey, keys));
   }

   @Override
   public Response<Long> zinterstore(String dstKey, String... sets) {
      return this.appendCommand(this.commandObjects.zinterstore(dstKey, sets));
   }

   @Override
   public Response<Long> zinterstore(String dstKey, ZParams params, String... sets) {
      return this.appendCommand(this.commandObjects.zinterstore(dstKey, params, sets));
   }

   @Override
   public Response<List<String>> zinter(ZParams params, String... keys) {
      return this.appendCommand(this.commandObjects.zinter(params, keys));
   }

   @Override
   public Response<List<Tuple>> zinterWithScores(ZParams params, String... keys) {
      return this.appendCommand(this.commandObjects.zinterWithScores(params, keys));
   }

   @Override
   public Response<Long> zintercard(String... keys) {
      return this.appendCommand(this.commandObjects.zintercard(keys));
   }

   @Override
   public Response<Long> zintercard(long limit, String... keys) {
      return this.appendCommand(this.commandObjects.zintercard(limit, keys));
   }

   @Override
   public Response<List<String>> zunion(ZParams params, String... keys) {
      return this.appendCommand(this.commandObjects.zunion(params, keys));
   }

   @Override
   public Response<List<Tuple>> zunionWithScores(ZParams params, String... keys) {
      return this.appendCommand(this.commandObjects.zunionWithScores(params, keys));
   }

   @Override
   public Response<Long> zunionstore(String dstKey, String... sets) {
      return this.appendCommand(this.commandObjects.zunionstore(dstKey, sets));
   }

   @Override
   public Response<Long> zunionstore(String dstKey, ZParams params, String... sets) {
      return this.appendCommand(this.commandObjects.zunionstore(dstKey, params, sets));
   }

   @Override
   public Response<Long> geoadd(String key, double longitude, double latitude, String member) {
      return this.appendCommand(this.commandObjects.geoadd(key, longitude, latitude, member));
   }

   @Override
   public Response<Long> geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
      return this.appendCommand(this.commandObjects.geoadd(key, memberCoordinateMap));
   }

   @Override
   public Response<Long> geoadd(String key, GeoAddParams params, Map<String, GeoCoordinate> memberCoordinateMap) {
      return this.appendCommand(this.commandObjects.geoadd(key, params, memberCoordinateMap));
   }

   @Override
   public Response<Double> geodist(String key, String member1, String member2) {
      return this.appendCommand(this.commandObjects.geodist(key, member1, member2));
   }

   @Override
   public Response<Double> geodist(String key, String member1, String member2, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.geodist(key, member1, member2, unit));
   }

   @Override
   public Response<List<String>> geohash(String key, String... members) {
      return this.appendCommand(this.commandObjects.geohash(key, members));
   }

   @Override
   public Response<List<GeoCoordinate>> geopos(String key, String... members) {
      return this.appendCommand(this.commandObjects.geopos(key, members));
   }

   @Override
   public Response<List<GeoRadiusResponse>> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.georadius(key, longitude, latitude, radius, unit));
   }

   @Override
   public Response<List<GeoRadiusResponse>> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.georadiusReadonly(key, longitude, latitude, radius, unit));
   }

   @Override
   public Response<List<GeoRadiusResponse>> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
      return this.appendCommand(this.commandObjects.georadius(key, longitude, latitude, radius, unit, param));
   }

   @Override
   public Response<List<GeoRadiusResponse>> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
      return this.appendCommand(this.commandObjects.georadiusReadonly(key, longitude, latitude, radius, unit, param));
   }

   @Override
   public Response<List<GeoRadiusResponse>> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.georadiusByMember(key, member, radius, unit));
   }

   @Override
   public Response<List<GeoRadiusResponse>> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.georadiusByMemberReadonly(key, member, radius, unit));
   }

   @Override
   public Response<List<GeoRadiusResponse>> georadiusByMember(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
      return this.appendCommand(this.commandObjects.georadiusByMember(key, member, radius, unit, param));
   }

   @Override
   public Response<List<GeoRadiusResponse>> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
      return this.appendCommand(this.commandObjects.georadiusByMemberReadonly(key, member, radius, unit, param));
   }

   @Override
   public Response<Long> georadiusStore(
      String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam
   ) {
      return this.appendCommand(this.commandObjects.georadiusStore(key, longitude, latitude, radius, unit, param, storeParam));
   }

   @Override
   public Response<Long> georadiusByMemberStore(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
      return this.appendCommand(this.commandObjects.georadiusByMemberStore(key, member, radius, unit, param, storeParam));
   }

   @Override
   public Response<List<GeoRadiusResponse>> geosearch(String key, String member, double radius, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.geosearch(key, member, radius, unit));
   }

   @Override
   public Response<List<GeoRadiusResponse>> geosearch(String key, GeoCoordinate coord, double radius, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.geosearch(key, coord, radius, unit));
   }

   @Override
   public Response<List<GeoRadiusResponse>> geosearch(String key, String member, double width, double height, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.geosearch(key, member, width, height, unit));
   }

   @Override
   public Response<List<GeoRadiusResponse>> geosearch(String key, GeoCoordinate coord, double width, double height, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.geosearch(key, coord, width, height, unit));
   }

   @Override
   public Response<List<GeoRadiusResponse>> geosearch(String key, GeoSearchParam params) {
      return this.appendCommand(this.commandObjects.geosearch(key, params));
   }

   @Override
   public Response<Long> geosearchStore(String dest, String src, String member, double radius, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.geosearchStore(dest, src, member, radius, unit));
   }

   @Override
   public Response<Long> geosearchStore(String dest, String src, GeoCoordinate coord, double radius, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.geosearchStore(dest, src, coord, radius, unit));
   }

   @Override
   public Response<Long> geosearchStore(String dest, String src, String member, double width, double height, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.geosearchStore(dest, src, member, width, height, unit));
   }

   @Override
   public Response<Long> geosearchStore(String dest, String src, GeoCoordinate coord, double width, double height, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.geosearchStore(dest, src, coord, width, height, unit));
   }

   @Override
   public Response<Long> geosearchStore(String dest, String src, GeoSearchParam params) {
      return this.appendCommand(this.commandObjects.geosearchStore(dest, src, params));
   }

   @Override
   public Response<Long> geosearchStoreStoreDist(String dest, String src, GeoSearchParam params) {
      return this.appendCommand(this.commandObjects.geosearchStoreStoreDist(dest, src, params));
   }

   @Override
   public Response<Long> pfadd(String key, String... elements) {
      return this.appendCommand(this.commandObjects.pfadd(key, elements));
   }

   @Override
   public Response<String> pfmerge(String destkey, String... sourcekeys) {
      return this.appendCommand(this.commandObjects.pfmerge(destkey, sourcekeys));
   }

   @Override
   public Response<Long> pfcount(String key) {
      return this.appendCommand(this.commandObjects.pfcount(key));
   }

   @Override
   public Response<Long> pfcount(String... keys) {
      return this.appendCommand(this.commandObjects.pfcount(keys));
   }

   @Override
   public Response<StreamEntryID> xadd(String key, StreamEntryID id, Map<String, String> hash) {
      return this.appendCommand(this.commandObjects.xadd(key, id, hash));
   }

   @Override
   public Response<StreamEntryID> xadd(String key, XAddParams params, Map<String, String> hash) {
      return this.appendCommand(this.commandObjects.xadd(key, params, hash));
   }

   @Override
   public Response<Long> xlen(String key) {
      return this.appendCommand(this.commandObjects.xlen(key));
   }

   @Override
   public Response<List<StreamEntry>> xrange(String key, StreamEntryID start, StreamEntryID end) {
      return this.appendCommand(this.commandObjects.xrange(key, start, end));
   }

   @Override
   public Response<List<StreamEntry>> xrange(String key, StreamEntryID start, StreamEntryID end, int count) {
      return this.appendCommand(this.commandObjects.xrange(key, start, end, count));
   }

   @Override
   public Response<List<StreamEntry>> xrevrange(String key, StreamEntryID end, StreamEntryID start) {
      return this.appendCommand(this.commandObjects.xrevrange(key, end, start));
   }

   @Override
   public Response<List<StreamEntry>> xrevrange(String key, StreamEntryID end, StreamEntryID start, int count) {
      return this.appendCommand(this.commandObjects.xrevrange(key, end, start, count));
   }

   @Override
   public Response<List<StreamEntry>> xrange(String key, String start, String end) {
      return this.appendCommand(this.commandObjects.xrange(key, start, end));
   }

   @Override
   public Response<List<StreamEntry>> xrange(String key, String start, String end, int count) {
      return this.appendCommand(this.commandObjects.xrange(key, start, end, count));
   }

   @Override
   public Response<List<StreamEntry>> xrevrange(String key, String end, String start) {
      return this.appendCommand(this.commandObjects.xrevrange(key, end, start));
   }

   @Override
   public Response<List<StreamEntry>> xrevrange(String key, String end, String start, int count) {
      return this.appendCommand(this.commandObjects.xrevrange(key, end, start, count));
   }

   @Override
   public Response<Long> xack(String key, String group, StreamEntryID... ids) {
      return this.appendCommand(this.commandObjects.xack(key, group, ids));
   }

   @Override
   public Response<String> xgroupCreate(String key, String groupName, StreamEntryID id, boolean makeStream) {
      return this.appendCommand(this.commandObjects.xgroupCreate(key, groupName, id, makeStream));
   }

   @Override
   public Response<String> xgroupSetID(String key, String groupName, StreamEntryID id) {
      return this.appendCommand(this.commandObjects.xgroupSetID(key, groupName, id));
   }

   @Override
   public Response<Long> xgroupDestroy(String key, String groupName) {
      return this.appendCommand(this.commandObjects.xgroupDestroy(key, groupName));
   }

   @Override
   public Response<Boolean> xgroupCreateConsumer(String key, String groupName, String consumerName) {
      return this.appendCommand(this.commandObjects.xgroupCreateConsumer(key, groupName, consumerName));
   }

   @Override
   public Response<Long> xgroupDelConsumer(String key, String groupName, String consumerName) {
      return this.appendCommand(this.commandObjects.xgroupDelConsumer(key, groupName, consumerName));
   }

   @Override
   public Response<StreamPendingSummary> xpending(String key, String groupName) {
      return this.appendCommand(this.commandObjects.xpending(key, groupName));
   }

   @Override
   public Response<List<StreamPendingEntry>> xpending(String key, String groupName, XPendingParams params) {
      return this.appendCommand(this.commandObjects.xpending(key, groupName, params));
   }

   @Override
   public Response<Long> xdel(String key, StreamEntryID... ids) {
      return this.appendCommand(this.commandObjects.xdel(key, ids));
   }

   @Override
   public Response<Long> xtrim(String key, long maxLen, boolean approximate) {
      return this.appendCommand(this.commandObjects.xtrim(key, maxLen, approximate));
   }

   @Override
   public Response<Long> xtrim(String key, XTrimParams params) {
      return this.appendCommand(this.commandObjects.xtrim(key, params));
   }

   @Override
   public Response<List<StreamEntry>> xclaim(String key, String group, String consumerName, long minIdleTime, XClaimParams params, StreamEntryID... ids) {
      return this.appendCommand(this.commandObjects.xclaim(key, group, consumerName, minIdleTime, params, ids));
   }

   @Override
   public Response<List<StreamEntryID>> xclaimJustId(String key, String group, String consumerName, long minIdleTime, XClaimParams params, StreamEntryID... ids) {
      return this.appendCommand(this.commandObjects.xclaimJustId(key, group, consumerName, minIdleTime, params, ids));
   }

   @Override
   public Response<Entry<StreamEntryID, List<StreamEntry>>> xautoclaim(
      String key, String group, String consumerName, long minIdleTime, StreamEntryID start, XAutoClaimParams params
   ) {
      return this.appendCommand(this.commandObjects.xautoclaim(key, group, consumerName, minIdleTime, start, params));
   }

   @Override
   public Response<Entry<StreamEntryID, List<StreamEntryID>>> xautoclaimJustId(
      String key, String group, String consumerName, long minIdleTime, StreamEntryID start, XAutoClaimParams params
   ) {
      return this.appendCommand(this.commandObjects.xautoclaimJustId(key, group, consumerName, minIdleTime, start, params));
   }

   @Override
   public Response<StreamInfo> xinfoStream(String key) {
      return this.appendCommand(this.commandObjects.xinfoStream(key));
   }

   @Override
   public Response<StreamFullInfo> xinfoStreamFull(String key) {
      return this.appendCommand(this.commandObjects.xinfoStreamFull(key));
   }

   @Override
   public Response<StreamFullInfo> xinfoStreamFull(String key, int count) {
      return this.appendCommand(this.commandObjects.xinfoStreamFull(key, count));
   }

   @Override
   public Response<List<StreamGroupInfo>> xinfoGroups(String key) {
      return this.appendCommand(this.commandObjects.xinfoGroups(key));
   }

   @Override
   public Response<List<StreamConsumersInfo>> xinfoConsumers(String key, String group) {
      return this.appendCommand(this.commandObjects.xinfoConsumers(key, group));
   }

   @Override
   public Response<List<StreamConsumerInfo>> xinfoConsumers2(String key, String group) {
      return this.appendCommand(this.commandObjects.xinfoConsumers2(key, group));
   }

   @Override
   public Response<List<Entry<String, List<StreamEntry>>>> xread(XReadParams xReadParams, Map<String, StreamEntryID> streams) {
      return this.appendCommand(this.commandObjects.xread(xReadParams, streams));
   }

   @Override
   public Response<Map<String, List<StreamEntry>>> xreadAsMap(XReadParams xReadParams, Map<String, StreamEntryID> streams) {
      return this.appendCommand(this.commandObjects.xreadAsMap(xReadParams, streams));
   }

   @Override
   public Response<List<Entry<String, List<StreamEntry>>>> xreadGroup(
      String groupName, String consumer, XReadGroupParams xReadGroupParams, Map<String, StreamEntryID> streams
   ) {
      return this.appendCommand(this.commandObjects.xreadGroup(groupName, consumer, xReadGroupParams, streams));
   }

   @Override
   public Response<Map<String, List<StreamEntry>>> xreadGroupAsMap(
      String groupName, String consumer, XReadGroupParams xReadGroupParams, Map<String, StreamEntryID> streams
   ) {
      return this.appendCommand(this.commandObjects.xreadGroupAsMap(groupName, consumer, xReadGroupParams, streams));
   }

   @Override
   public Response<Object> eval(String script) {
      return this.appendCommand(this.commandObjects.eval(script));
   }

   @Override
   public Response<Object> eval(String script, int keyCount, String... params) {
      return this.appendCommand(this.commandObjects.eval(script, keyCount, params));
   }

   @Override
   public Response<Object> eval(String script, List<String> keys, List<String> args) {
      return this.appendCommand(this.commandObjects.eval(script, keys, args));
   }

   @Override
   public Response<Object> evalReadonly(String script, List<String> keys, List<String> args) {
      return this.appendCommand(this.commandObjects.evalReadonly(script, keys, args));
   }

   @Override
   public Response<Object> evalsha(String sha1) {
      return this.appendCommand(this.commandObjects.evalsha(sha1));
   }

   @Override
   public Response<Object> evalsha(String sha1, int keyCount, String... params) {
      return this.appendCommand(this.commandObjects.evalsha(sha1, keyCount, params));
   }

   @Override
   public Response<Object> evalsha(String sha1, List<String> keys, List<String> args) {
      return this.appendCommand(this.commandObjects.evalsha(sha1, keys, args));
   }

   @Override
   public Response<Object> evalshaReadonly(String sha1, List<String> keys, List<String> args) {
      return this.appendCommand(this.commandObjects.evalshaReadonly(sha1, keys, args));
   }

   @Override
   public Response<Long> waitReplicas(String sampleKey, int replicas, long timeout) {
      return this.appendCommand(this.commandObjects.waitReplicas(sampleKey, replicas, timeout));
   }

   @Override
   public Response<KeyValue<Long, Long>> waitAOF(String sampleKey, long numLocal, long numReplicas, long timeout) {
      return this.appendCommand(this.commandObjects.waitAOF(sampleKey, numLocal, numReplicas, timeout));
   }

   @Override
   public Response<Object> eval(String script, String sampleKey) {
      return this.appendCommand(this.commandObjects.eval(script, sampleKey));
   }

   @Override
   public Response<Object> evalsha(String sha1, String sampleKey) {
      return this.appendCommand(this.commandObjects.evalsha(sha1, sampleKey));
   }

   @Override
   public Response<List<Boolean>> scriptExists(String sampleKey, String... sha1) {
      return this.appendCommand(this.commandObjects.scriptExists(sampleKey, sha1));
   }

   @Override
   public Response<String> scriptLoad(String script, String sampleKey) {
      return this.appendCommand(this.commandObjects.scriptLoad(script, sampleKey));
   }

   @Override
   public Response<String> scriptFlush(String sampleKey) {
      return this.appendCommand(this.commandObjects.scriptFlush(sampleKey));
   }

   @Override
   public Response<String> scriptFlush(String sampleKey, FlushMode flushMode) {
      return this.appendCommand(this.commandObjects.scriptFlush(sampleKey, flushMode));
   }

   @Override
   public Response<String> scriptKill(String sampleKey) {
      return this.appendCommand(this.commandObjects.scriptKill(sampleKey));
   }

   @Override
   public Response<Object> fcall(byte[] name, List<byte[]> keys, List<byte[]> args) {
      return this.appendCommand(this.commandObjects.fcall(name, keys, args));
   }

   @Override
   public Response<Object> fcall(String name, List<String> keys, List<String> args) {
      return this.appendCommand(this.commandObjects.fcall(name, keys, args));
   }

   @Override
   public Response<Object> fcallReadonly(byte[] name, List<byte[]> keys, List<byte[]> args) {
      return this.appendCommand(this.commandObjects.fcallReadonly(name, keys, args));
   }

   @Override
   public Response<Object> fcallReadonly(String name, List<String> keys, List<String> args) {
      return this.appendCommand(this.commandObjects.fcallReadonly(name, keys, args));
   }

   @Override
   public Response<String> functionDelete(byte[] libraryName) {
      return this.appendCommand(this.commandObjects.functionDelete(libraryName));
   }

   @Override
   public Response<String> functionDelete(String libraryName) {
      return this.appendCommand(this.commandObjects.functionDelete(libraryName));
   }

   @Override
   public Response<byte[]> functionDump() {
      return this.appendCommand(this.commandObjects.functionDump());
   }

   @Override
   public Response<List<LibraryInfo>> functionList(String libraryNamePattern) {
      return this.appendCommand(this.commandObjects.functionList(libraryNamePattern));
   }

   @Override
   public Response<List<LibraryInfo>> functionList() {
      return this.appendCommand(this.commandObjects.functionList());
   }

   @Override
   public Response<List<LibraryInfo>> functionListWithCode(String libraryNamePattern) {
      return this.appendCommand(this.commandObjects.functionListWithCode(libraryNamePattern));
   }

   @Override
   public Response<List<LibraryInfo>> functionListWithCode() {
      return this.appendCommand(this.commandObjects.functionListWithCode());
   }

   @Override
   public Response<List<Object>> functionListBinary() {
      return this.appendCommand(this.commandObjects.functionListBinary());
   }

   @Override
   public Response<List<Object>> functionList(byte[] libraryNamePattern) {
      return this.appendCommand(this.commandObjects.functionList(libraryNamePattern));
   }

   @Override
   public Response<List<Object>> functionListWithCodeBinary() {
      return this.appendCommand(this.commandObjects.functionListWithCodeBinary());
   }

   @Override
   public Response<List<Object>> functionListWithCode(byte[] libraryNamePattern) {
      return this.appendCommand(this.commandObjects.functionListWithCode(libraryNamePattern));
   }

   @Override
   public Response<String> functionLoad(byte[] functionCode) {
      return this.appendCommand(this.commandObjects.functionLoad(functionCode));
   }

   @Override
   public Response<String> functionLoad(String functionCode) {
      return this.appendCommand(this.commandObjects.functionLoad(functionCode));
   }

   @Override
   public Response<String> functionLoadReplace(byte[] functionCode) {
      return this.appendCommand(this.commandObjects.functionLoadReplace(functionCode));
   }

   @Override
   public Response<String> functionLoadReplace(String functionCode) {
      return this.appendCommand(this.commandObjects.functionLoadReplace(functionCode));
   }

   @Override
   public Response<String> functionRestore(byte[] serializedValue) {
      return this.appendCommand(this.commandObjects.functionRestore(serializedValue));
   }

   @Override
   public Response<String> functionRestore(byte[] serializedValue, FunctionRestorePolicy policy) {
      return this.appendCommand(this.commandObjects.functionRestore(serializedValue, policy));
   }

   @Override
   public Response<String> functionFlush() {
      return this.appendCommand(this.commandObjects.functionFlush());
   }

   @Override
   public Response<String> functionFlush(FlushMode mode) {
      return this.appendCommand(this.commandObjects.functionFlush(mode));
   }

   @Override
   public Response<String> functionKill() {
      return this.appendCommand(this.commandObjects.functionKill());
   }

   @Override
   public Response<FunctionStats> functionStats() {
      return this.appendCommand(this.commandObjects.functionStats());
   }

   @Override
   public Response<Object> functionStatsBinary() {
      return this.appendCommand(this.commandObjects.functionStatsBinary());
   }

   @Override
   public Response<Long> geoadd(byte[] key, double longitude, double latitude, byte[] member) {
      return this.appendCommand(this.commandObjects.geoadd(key, longitude, latitude, member));
   }

   @Override
   public Response<Long> geoadd(byte[] key, Map<byte[], GeoCoordinate> memberCoordinateMap) {
      return this.appendCommand(this.commandObjects.geoadd(key, memberCoordinateMap));
   }

   @Override
   public Response<Long> geoadd(byte[] key, GeoAddParams params, Map<byte[], GeoCoordinate> memberCoordinateMap) {
      return this.appendCommand(this.commandObjects.geoadd(key, params, memberCoordinateMap));
   }

   @Override
   public Response<Double> geodist(byte[] key, byte[] member1, byte[] member2) {
      return this.appendCommand(this.commandObjects.geodist(key, member1, member2));
   }

   @Override
   public Response<Double> geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.geodist(key, member1, member2, unit));
   }

   @Override
   public Response<List<byte[]>> geohash(byte[] key, byte[]... members) {
      return this.appendCommand(this.commandObjects.geohash(key, members));
   }

   @Override
   public Response<List<GeoCoordinate>> geopos(byte[] key, byte[]... members) {
      return this.appendCommand(this.commandObjects.geopos(key, members));
   }

   @Override
   public Response<List<GeoRadiusResponse>> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.georadius(key, longitude, latitude, radius, unit));
   }

   @Override
   public Response<List<GeoRadiusResponse>> georadiusReadonly(byte[] key, double longitude, double latitude, double radius, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.georadiusReadonly(key, longitude, latitude, radius, unit));
   }

   @Override
   public Response<List<GeoRadiusResponse>> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
      return this.appendCommand(this.commandObjects.georadius(key, longitude, latitude, radius, unit, param));
   }

   @Override
   public Response<List<GeoRadiusResponse>> georadiusReadonly(byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
      return this.appendCommand(this.commandObjects.georadiusReadonly(key, longitude, latitude, radius, unit, param));
   }

   @Override
   public Response<List<GeoRadiusResponse>> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.georadiusByMember(key, member, radius, unit));
   }

   @Override
   public Response<List<GeoRadiusResponse>> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.georadiusByMemberReadonly(key, member, radius, unit));
   }

   @Override
   public Response<List<GeoRadiusResponse>> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param) {
      return this.appendCommand(this.commandObjects.georadiusByMember(key, member, radius, unit, param));
   }

   @Override
   public Response<List<GeoRadiusResponse>> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param) {
      return this.appendCommand(this.commandObjects.georadiusByMemberReadonly(key, member, radius, unit, param));
   }

   @Override
   public Response<Long> georadiusStore(
      byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam
   ) {
      return this.appendCommand(this.commandObjects.georadiusStore(key, longitude, latitude, radius, unit, param, storeParam));
   }

   @Override
   public Response<Long> georadiusByMemberStore(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
      return this.appendCommand(this.commandObjects.georadiusByMemberStore(key, member, radius, unit, param, storeParam));
   }

   @Override
   public Response<List<GeoRadiusResponse>> geosearch(byte[] key, byte[] member, double radius, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.geosearch(key, member, radius, unit));
   }

   @Override
   public Response<List<GeoRadiusResponse>> geosearch(byte[] key, GeoCoordinate coord, double radius, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.geosearch(key, coord, radius, unit));
   }

   @Override
   public Response<List<GeoRadiusResponse>> geosearch(byte[] key, byte[] member, double width, double height, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.geosearch(key, member, width, height, unit));
   }

   @Override
   public Response<List<GeoRadiusResponse>> geosearch(byte[] key, GeoCoordinate coord, double width, double height, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.geosearch(key, coord, width, height, unit));
   }

   @Override
   public Response<List<GeoRadiusResponse>> geosearch(byte[] key, GeoSearchParam params) {
      return this.appendCommand(this.commandObjects.geosearch(key, params));
   }

   @Override
   public Response<Long> geosearchStore(byte[] dest, byte[] src, byte[] member, double radius, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.geosearchStore(dest, src, member, radius, unit));
   }

   @Override
   public Response<Long> geosearchStore(byte[] dest, byte[] src, GeoCoordinate coord, double radius, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.geosearchStore(dest, src, coord, radius, unit));
   }

   @Override
   public Response<Long> geosearchStore(byte[] dest, byte[] src, byte[] member, double width, double height, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.geosearchStore(dest, src, member, width, height, unit));
   }

   @Override
   public Response<Long> geosearchStore(byte[] dest, byte[] src, GeoCoordinate coord, double width, double height, GeoUnit unit) {
      return this.appendCommand(this.commandObjects.geosearchStore(dest, src, coord, width, height, unit));
   }

   @Override
   public Response<Long> geosearchStore(byte[] dest, byte[] src, GeoSearchParam params) {
      return this.appendCommand(this.commandObjects.geosearchStore(dest, src, params));
   }

   @Override
   public Response<Long> geosearchStoreStoreDist(byte[] dest, byte[] src, GeoSearchParam params) {
      return this.appendCommand(this.commandObjects.geosearchStoreStoreDist(dest, src, params));
   }

   @Override
   public Response<Long> hset(byte[] key, byte[] field, byte[] value) {
      return this.appendCommand(this.commandObjects.hset(key, field, value));
   }

   @Override
   public Response<Long> hset(byte[] key, Map<byte[], byte[]> hash) {
      return this.appendCommand(this.commandObjects.hset(key, hash));
   }

   @Override
   public Response<byte[]> hget(byte[] key, byte[] field) {
      return this.appendCommand(this.commandObjects.hget(key, field));
   }

   @Override
   public Response<Long> hsetnx(byte[] key, byte[] field, byte[] value) {
      return this.appendCommand(this.commandObjects.hsetnx(key, field, value));
   }

   @Override
   public Response<String> hmset(byte[] key, Map<byte[], byte[]> hash) {
      return this.appendCommand(this.commandObjects.hmset(key, hash));
   }

   @Override
   public Response<List<byte[]>> hmget(byte[] key, byte[]... fields) {
      return this.appendCommand(this.commandObjects.hmget(key, fields));
   }

   @Override
   public Response<Long> hincrBy(byte[] key, byte[] field, long value) {
      return this.appendCommand(this.commandObjects.hincrBy(key, field, value));
   }

   @Override
   public Response<Double> hincrByFloat(byte[] key, byte[] field, double value) {
      return this.appendCommand(this.commandObjects.hincrByFloat(key, field, value));
   }

   @Override
   public Response<Boolean> hexists(byte[] key, byte[] field) {
      return this.appendCommand(this.commandObjects.hexists(key, field));
   }

   @Override
   public Response<Long> hdel(byte[] key, byte[]... field) {
      return this.appendCommand(this.commandObjects.hdel(key, field));
   }

   @Override
   public Response<Long> hlen(byte[] key) {
      return this.appendCommand(this.commandObjects.hlen(key));
   }

   @Override
   public Response<Set<byte[]>> hkeys(byte[] key) {
      return this.appendCommand(this.commandObjects.hkeys(key));
   }

   @Override
   public Response<List<byte[]>> hvals(byte[] key) {
      return this.appendCommand(this.commandObjects.hvals(key));
   }

   @Override
   public Response<Map<byte[], byte[]>> hgetAll(byte[] key) {
      return this.appendCommand(this.commandObjects.hgetAll(key));
   }

   @Override
   public Response<byte[]> hrandfield(byte[] key) {
      return this.appendCommand(this.commandObjects.hrandfield(key));
   }

   @Override
   public Response<List<byte[]>> hrandfield(byte[] key, long count) {
      return this.appendCommand(this.commandObjects.hrandfield(key, count));
   }

   @Override
   public Response<List<Entry<byte[], byte[]>>> hrandfieldWithValues(byte[] key, long count) {
      return this.appendCommand(this.commandObjects.hrandfieldWithValues(key, count));
   }

   @Override
   public Response<ScanResult<Entry<byte[], byte[]>>> hscan(byte[] key, byte[] cursor, ScanParams params) {
      return this.appendCommand(this.commandObjects.hscan(key, cursor, params));
   }

   @Override
   public Response<ScanResult<byte[]>> hscanNoValues(byte[] key, byte[] cursor, ScanParams params) {
      return this.appendCommand(this.commandObjects.hscanNoValues(key, cursor, params));
   }

   @Override
   public Response<Long> hstrlen(byte[] key, byte[] field) {
      return this.appendCommand(this.commandObjects.hstrlen(key, field));
   }

   @Override
   public Response<List<Long>> hexpire(byte[] key, long seconds, byte[]... fields) {
      return this.appendCommand(this.commandObjects.hexpire(key, seconds, fields));
   }

   @Override
   public Response<List<Long>> hexpire(byte[] key, long seconds, ExpiryOption condition, byte[]... fields) {
      return this.appendCommand(this.commandObjects.hexpire(key, seconds, condition, fields));
   }

   @Override
   public Response<List<Long>> hpexpire(byte[] key, long milliseconds, byte[]... fields) {
      return this.appendCommand(this.commandObjects.hpexpire(key, milliseconds, fields));
   }

   @Override
   public Response<List<Long>> hpexpire(byte[] key, long milliseconds, ExpiryOption condition, byte[]... fields) {
      return this.appendCommand(this.commandObjects.hpexpire(key, milliseconds, condition, fields));
   }

   @Override
   public Response<List<Long>> hexpireAt(byte[] key, long unixTimeSeconds, byte[]... fields) {
      return this.appendCommand(this.commandObjects.hexpireAt(key, unixTimeSeconds, fields));
   }

   @Override
   public Response<List<Long>> hexpireAt(byte[] key, long unixTimeSeconds, ExpiryOption condition, byte[]... fields) {
      return this.appendCommand(this.commandObjects.hexpireAt(key, unixTimeSeconds, condition, fields));
   }

   @Override
   public Response<List<Long>> hpexpireAt(byte[] key, long unixTimeMillis, byte[]... fields) {
      return this.appendCommand(this.commandObjects.hpexpireAt(key, unixTimeMillis, fields));
   }

   @Override
   public Response<List<Long>> hpexpireAt(byte[] key, long unixTimeMillis, ExpiryOption condition, byte[]... fields) {
      return this.appendCommand(this.commandObjects.hpexpireAt(key, unixTimeMillis, condition, fields));
   }

   @Override
   public Response<List<Long>> hexpireTime(byte[] key, byte[]... fields) {
      return this.appendCommand(this.commandObjects.hexpireTime(key, fields));
   }

   @Override
   public Response<List<Long>> hpexpireTime(byte[] key, byte[]... fields) {
      return this.appendCommand(this.commandObjects.hpexpireTime(key, fields));
   }

   @Override
   public Response<List<Long>> httl(byte[] key, byte[]... fields) {
      return this.appendCommand(this.commandObjects.httl(key, fields));
   }

   @Override
   public Response<List<Long>> hpttl(byte[] key, byte[]... fields) {
      return this.appendCommand(this.commandObjects.hpttl(key, fields));
   }

   @Override
   public Response<List<Long>> hpersist(byte[] key, byte[]... fields) {
      return this.appendCommand(this.commandObjects.hpersist(key, fields));
   }

   @Override
   public Response<Long> pfadd(byte[] key, byte[]... elements) {
      return this.appendCommand(this.commandObjects.pfadd(key, elements));
   }

   @Override
   public Response<String> pfmerge(byte[] destkey, byte[]... sourcekeys) {
      return this.appendCommand(this.commandObjects.pfmerge(destkey, sourcekeys));
   }

   @Override
   public Response<Long> pfcount(byte[] key) {
      return this.appendCommand(this.commandObjects.pfcount(key));
   }

   @Override
   public Response<Long> pfcount(byte[]... keys) {
      return this.appendCommand(this.commandObjects.pfcount(keys));
   }

   @Override
   public Response<Boolean> exists(byte[] key) {
      return this.appendCommand(this.commandObjects.exists(key));
   }

   @Override
   public Response<Long> exists(byte[]... keys) {
      return this.appendCommand(this.commandObjects.exists(keys));
   }

   @Override
   public Response<Long> persist(byte[] key) {
      return this.appendCommand(this.commandObjects.persist(key));
   }

   @Override
   public Response<String> type(byte[] key) {
      return this.appendCommand(this.commandObjects.type(key));
   }

   @Override
   public Response<byte[]> dump(byte[] key) {
      return this.appendCommand(this.commandObjects.dump(key));
   }

   @Override
   public Response<String> restore(byte[] key, long ttl, byte[] serializedValue) {
      return this.appendCommand(this.commandObjects.restore(key, ttl, serializedValue));
   }

   @Override
   public Response<String> restore(byte[] key, long ttl, byte[] serializedValue, RestoreParams params) {
      return this.appendCommand(this.commandObjects.restore(key, ttl, serializedValue, params));
   }

   @Override
   public Response<Long> expire(byte[] key, long seconds) {
      return this.appendCommand(this.commandObjects.expire(key, seconds));
   }

   @Override
   public Response<Long> expire(byte[] key, long seconds, ExpiryOption expiryOption) {
      return this.appendCommand(this.commandObjects.expire(key, seconds, expiryOption));
   }

   @Override
   public Response<Long> pexpire(byte[] key, long milliseconds) {
      return this.appendCommand(this.commandObjects.pexpire(key, milliseconds));
   }

   @Override
   public Response<Long> pexpire(byte[] key, long milliseconds, ExpiryOption expiryOption) {
      return this.appendCommand(this.commandObjects.pexpire(key, milliseconds, expiryOption));
   }

   @Override
   public Response<Long> expireTime(byte[] key) {
      return this.appendCommand(this.commandObjects.expireTime(key));
   }

   @Override
   public Response<Long> pexpireTime(byte[] key) {
      return this.appendCommand(this.commandObjects.pexpireTime(key));
   }

   @Override
   public Response<Long> expireAt(byte[] key, long unixTime) {
      return this.appendCommand(this.commandObjects.expireAt(key, unixTime));
   }

   @Override
   public Response<Long> expireAt(byte[] key, long unixTime, ExpiryOption expiryOption) {
      return this.appendCommand(this.commandObjects.expireAt(key, unixTime, expiryOption));
   }

   @Override
   public Response<Long> pexpireAt(byte[] key, long millisecondsTimestamp) {
      return this.appendCommand(this.commandObjects.pexpireAt(key, millisecondsTimestamp));
   }

   @Override
   public Response<Long> pexpireAt(byte[] key, long millisecondsTimestamp, ExpiryOption expiryOption) {
      return this.appendCommand(this.commandObjects.pexpireAt(key, millisecondsTimestamp, expiryOption));
   }

   @Override
   public Response<Long> ttl(byte[] key) {
      return this.appendCommand(this.commandObjects.ttl(key));
   }

   @Override
   public Response<Long> pttl(byte[] key) {
      return this.appendCommand(this.commandObjects.pttl(key));
   }

   @Override
   public Response<Long> touch(byte[] key) {
      return this.appendCommand(this.commandObjects.touch(key));
   }

   @Override
   public Response<Long> touch(byte[]... keys) {
      return this.appendCommand(this.commandObjects.touch(keys));
   }

   @Override
   public Response<List<byte[]>> sort(byte[] key) {
      return this.appendCommand(this.commandObjects.sort(key));
   }

   @Override
   public Response<List<byte[]>> sort(byte[] key, SortingParams sortingParams) {
      return this.appendCommand(this.commandObjects.sort(key, sortingParams));
   }

   @Override
   public Response<List<byte[]>> sortReadonly(byte[] key, SortingParams sortingParams) {
      return this.appendCommand(this.commandObjects.sortReadonly(key, sortingParams));
   }

   @Override
   public Response<Long> del(byte[] key) {
      return this.appendCommand(this.commandObjects.del(key));
   }

   @Override
   public Response<Long> del(byte[]... keys) {
      return this.appendCommand(this.commandObjects.del(keys));
   }

   @Override
   public Response<Long> unlink(byte[] key) {
      return this.appendCommand(this.commandObjects.unlink(key));
   }

   @Override
   public Response<Long> unlink(byte[]... keys) {
      return this.appendCommand(this.commandObjects.unlink(keys));
   }

   @Override
   public Response<Boolean> copy(byte[] srcKey, byte[] dstKey, boolean replace) {
      return this.appendCommand(this.commandObjects.copy(srcKey, dstKey, replace));
   }

   @Override
   public Response<String> rename(byte[] oldkey, byte[] newkey) {
      return this.appendCommand(this.commandObjects.rename(oldkey, newkey));
   }

   @Override
   public Response<Long> renamenx(byte[] oldkey, byte[] newkey) {
      return this.appendCommand(this.commandObjects.renamenx(oldkey, newkey));
   }

   @Override
   public Response<Long> sort(byte[] key, SortingParams sortingParams, byte[] dstkey) {
      return this.appendCommand(this.commandObjects.sort(key, sortingParams, dstkey));
   }

   @Override
   public Response<Long> sort(byte[] key, byte[] dstkey) {
      return this.appendCommand(this.commandObjects.sort(key, dstkey));
   }

   @Override
   public Response<Long> memoryUsage(byte[] key) {
      return this.appendCommand(this.commandObjects.memoryUsage(key));
   }

   @Override
   public Response<Long> memoryUsage(byte[] key, int samples) {
      return this.appendCommand(this.commandObjects.memoryUsage(key, samples));
   }

   @Override
   public Response<Long> objectRefcount(byte[] key) {
      return this.appendCommand(this.commandObjects.objectRefcount(key));
   }

   @Override
   public Response<byte[]> objectEncoding(byte[] key) {
      return this.appendCommand(this.commandObjects.objectEncoding(key));
   }

   @Override
   public Response<Long> objectIdletime(byte[] key) {
      return this.appendCommand(this.commandObjects.objectIdletime(key));
   }

   @Override
   public Response<Long> objectFreq(byte[] key) {
      return this.appendCommand(this.commandObjects.objectFreq(key));
   }

   @Override
   public Response<String> migrate(String host, int port, byte[] key, int timeout) {
      return this.appendCommand(this.commandObjects.migrate(host, port, key, timeout));
   }

   @Override
   public Response<String> migrate(String host, int port, int timeout, MigrateParams params, byte[]... keys) {
      return this.appendCommand(this.commandObjects.migrate(host, port, timeout, params, keys));
   }

   @Override
   public Response<Set<byte[]>> keys(byte[] pattern) {
      return this.appendCommand(this.commandObjects.keys(pattern));
   }

   @Override
   public Response<ScanResult<byte[]>> scan(byte[] cursor) {
      return this.appendCommand(this.commandObjects.scan(cursor));
   }

   @Override
   public Response<ScanResult<byte[]>> scan(byte[] cursor, ScanParams params) {
      return this.appendCommand(this.commandObjects.scan(cursor, params));
   }

   @Override
   public Response<ScanResult<byte[]>> scan(byte[] cursor, ScanParams params, byte[] type) {
      return this.appendCommand(this.commandObjects.scan(cursor, params, type));
   }

   @Override
   public Response<byte[]> randomBinaryKey() {
      return this.appendCommand(this.commandObjects.randomBinaryKey());
   }

   @Override
   public Response<Long> rpush(byte[] key, byte[]... args) {
      return this.appendCommand(this.commandObjects.rpush(key, args));
   }

   @Override
   public Response<Long> lpush(byte[] key, byte[]... args) {
      return this.appendCommand(this.commandObjects.lpush(key, args));
   }

   @Override
   public Response<Long> llen(byte[] key) {
      return this.appendCommand(this.commandObjects.llen(key));
   }

   @Override
   public Response<List<byte[]>> lrange(byte[] key, long start, long stop) {
      return this.appendCommand(this.commandObjects.lrange(key, start, stop));
   }

   @Override
   public Response<String> ltrim(byte[] key, long start, long stop) {
      return this.appendCommand(this.commandObjects.ltrim(key, start, stop));
   }

   @Override
   public Response<byte[]> lindex(byte[] key, long index) {
      return this.appendCommand(this.commandObjects.lindex(key, index));
   }

   @Override
   public Response<String> lset(byte[] key, long index, byte[] value) {
      return this.appendCommand(this.commandObjects.lset(key, index, value));
   }

   @Override
   public Response<Long> lrem(byte[] key, long count, byte[] value) {
      return this.appendCommand(this.commandObjects.lrem(key, count, value));
   }

   @Override
   public Response<byte[]> lpop(byte[] key) {
      return this.appendCommand(this.commandObjects.lpop(key));
   }

   @Override
   public Response<List<byte[]>> lpop(byte[] key, int count) {
      return this.appendCommand(this.commandObjects.lpop(key, count));
   }

   @Override
   public Response<Long> lpos(byte[] key, byte[] element) {
      return this.appendCommand(this.commandObjects.lpos(key, element));
   }

   @Override
   public Response<Long> lpos(byte[] key, byte[] element, LPosParams params) {
      return this.appendCommand(this.commandObjects.lpos(key, element, params));
   }

   @Override
   public Response<List<Long>> lpos(byte[] key, byte[] element, LPosParams params, long count) {
      return this.appendCommand(this.commandObjects.lpos(key, element, params, count));
   }

   @Override
   public Response<byte[]> rpop(byte[] key) {
      return this.appendCommand(this.commandObjects.rpop(key));
   }

   @Override
   public Response<List<byte[]>> rpop(byte[] key, int count) {
      return this.appendCommand(this.commandObjects.rpop(key, count));
   }

   @Override
   public Response<Long> linsert(byte[] key, ListPosition where, byte[] pivot, byte[] value) {
      return this.appendCommand(this.commandObjects.linsert(key, where, pivot, value));
   }

   @Override
   public Response<Long> lpushx(byte[] key, byte[]... args) {
      return this.appendCommand(this.commandObjects.lpushx(key, args));
   }

   @Override
   public Response<Long> rpushx(byte[] key, byte[]... args) {
      return this.appendCommand(this.commandObjects.rpushx(key, args));
   }

   @Override
   public Response<List<byte[]>> blpop(int timeout, byte[]... keys) {
      return this.appendCommand(this.commandObjects.blpop(timeout, keys));
   }

   @Override
   public Response<KeyValue<byte[], byte[]>> blpop(double timeout, byte[]... keys) {
      return this.appendCommand(this.commandObjects.blpop(timeout, keys));
   }

   @Override
   public Response<List<byte[]>> brpop(int timeout, byte[]... keys) {
      return this.appendCommand(this.commandObjects.brpop(timeout, keys));
   }

   @Override
   public Response<KeyValue<byte[], byte[]>> brpop(double timeout, byte[]... keys) {
      return this.appendCommand(this.commandObjects.brpop(timeout, keys));
   }

   @Override
   public Response<byte[]> rpoplpush(byte[] srckey, byte[] dstkey) {
      return this.appendCommand(this.commandObjects.rpoplpush(srckey, dstkey));
   }

   @Override
   public Response<byte[]> brpoplpush(byte[] source, byte[] destination, int timeout) {
      return this.appendCommand(this.commandObjects.brpoplpush(source, destination, timeout));
   }

   @Override
   public Response<byte[]> lmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to) {
      return this.appendCommand(this.commandObjects.lmove(srcKey, dstKey, from, to));
   }

   @Override
   public Response<byte[]> blmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to, double timeout) {
      return this.appendCommand(this.commandObjects.blmove(srcKey, dstKey, from, to, timeout));
   }

   @Override
   public Response<KeyValue<byte[], List<byte[]>>> lmpop(ListDirection direction, byte[]... keys) {
      return this.appendCommand(this.commandObjects.lmpop(direction, keys));
   }

   @Override
   public Response<KeyValue<byte[], List<byte[]>>> lmpop(ListDirection direction, int count, byte[]... keys) {
      return this.appendCommand(this.commandObjects.lmpop(direction, count, keys));
   }

   @Override
   public Response<KeyValue<byte[], List<byte[]>>> blmpop(double timeout, ListDirection direction, byte[]... keys) {
      return this.appendCommand(this.commandObjects.blmpop(timeout, direction, keys));
   }

   @Override
   public Response<KeyValue<byte[], List<byte[]>>> blmpop(double timeout, ListDirection direction, int count, byte[]... keys) {
      return this.appendCommand(this.commandObjects.blmpop(timeout, direction, count, keys));
   }

   @Override
   public Response<Long> waitReplicas(byte[] sampleKey, int replicas, long timeout) {
      return this.appendCommand(this.commandObjects.waitReplicas(sampleKey, replicas, timeout));
   }

   @Override
   public Response<KeyValue<Long, Long>> waitAOF(byte[] sampleKey, long numLocal, long numReplicas, long timeout) {
      return this.appendCommand(this.commandObjects.waitAOF(sampleKey, numLocal, numReplicas, timeout));
   }

   @Override
   public Response<Object> eval(byte[] script, byte[] sampleKey) {
      return this.appendCommand(this.commandObjects.eval(script, sampleKey));
   }

   @Override
   public Response<Object> evalsha(byte[] sha1, byte[] sampleKey) {
      return this.appendCommand(this.commandObjects.evalsha(sha1, sampleKey));
   }

   @Override
   public Response<List<Boolean>> scriptExists(byte[] sampleKey, byte[]... sha1s) {
      return this.appendCommand(this.commandObjects.scriptExists(sampleKey, sha1s));
   }

   @Override
   public Response<byte[]> scriptLoad(byte[] script, byte[] sampleKey) {
      return this.appendCommand(this.commandObjects.scriptLoad(script, sampleKey));
   }

   @Override
   public Response<String> scriptFlush(byte[] sampleKey) {
      return this.appendCommand(this.commandObjects.scriptFlush(sampleKey));
   }

   @Override
   public Response<String> scriptFlush(byte[] sampleKey, FlushMode flushMode) {
      return this.appendCommand(this.commandObjects.scriptFlush(sampleKey, flushMode));
   }

   @Override
   public Response<String> scriptKill(byte[] sampleKey) {
      return this.appendCommand(this.commandObjects.scriptKill(sampleKey));
   }

   @Override
   public Response<Object> eval(byte[] script) {
      return this.appendCommand(this.commandObjects.eval(script));
   }

   @Override
   public Response<Object> eval(byte[] script, int keyCount, byte[]... params) {
      return this.appendCommand(this.commandObjects.eval(script, keyCount, params));
   }

   @Override
   public Response<Object> eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
      return this.appendCommand(this.commandObjects.eval(script, keys, args));
   }

   @Override
   public Response<Object> evalReadonly(byte[] script, List<byte[]> keys, List<byte[]> args) {
      return this.appendCommand(this.commandObjects.evalReadonly(script, keys, args));
   }

   @Override
   public Response<Object> evalsha(byte[] sha1) {
      return this.appendCommand(this.commandObjects.evalsha(sha1));
   }

   @Override
   public Response<Object> evalsha(byte[] sha1, int keyCount, byte[]... params) {
      return this.appendCommand(this.commandObjects.evalsha(sha1, keyCount, params));
   }

   @Override
   public Response<Object> evalsha(byte[] sha1, List<byte[]> keys, List<byte[]> args) {
      return this.appendCommand(this.commandObjects.evalsha(sha1, keys, args));
   }

   @Override
   public Response<Object> evalshaReadonly(byte[] sha1, List<byte[]> keys, List<byte[]> args) {
      return this.appendCommand(this.commandObjects.evalshaReadonly(sha1, keys, args));
   }

   @Override
   public Response<Long> sadd(byte[] key, byte[]... members) {
      return this.appendCommand(this.commandObjects.sadd(key, members));
   }

   @Override
   public Response<Set<byte[]>> smembers(byte[] key) {
      return this.appendCommand(this.commandObjects.smembers(key));
   }

   @Override
   public Response<Long> srem(byte[] key, byte[]... members) {
      return this.appendCommand(this.commandObjects.srem(key, members));
   }

   @Override
   public Response<byte[]> spop(byte[] key) {
      return this.appendCommand(this.commandObjects.spop(key));
   }

   @Override
   public Response<Set<byte[]>> spop(byte[] key, long count) {
      return this.appendCommand(this.commandObjects.spop(key, count));
   }

   @Override
   public Response<Long> scard(byte[] key) {
      return this.appendCommand(this.commandObjects.scard(key));
   }

   @Override
   public Response<Boolean> sismember(byte[] key, byte[] member) {
      return this.appendCommand(this.commandObjects.sismember(key, member));
   }

   @Override
   public Response<List<Boolean>> smismember(byte[] key, byte[]... members) {
      return this.appendCommand(this.commandObjects.smismember(key, members));
   }

   @Override
   public Response<byte[]> srandmember(byte[] key) {
      return this.appendCommand(this.commandObjects.srandmember(key));
   }

   @Override
   public Response<List<byte[]>> srandmember(byte[] key, int count) {
      return this.appendCommand(this.commandObjects.srandmember(key, count));
   }

   @Override
   public Response<ScanResult<byte[]>> sscan(byte[] key, byte[] cursor, ScanParams params) {
      return this.appendCommand(this.commandObjects.sscan(key, cursor, params));
   }

   @Override
   public Response<Set<byte[]>> sdiff(byte[]... keys) {
      return this.appendCommand(this.commandObjects.sdiff(keys));
   }

   @Override
   public Response<Long> sdiffstore(byte[] dstkey, byte[]... keys) {
      return this.appendCommand(this.commandObjects.sdiffstore(dstkey, keys));
   }

   @Override
   public Response<Set<byte[]>> sinter(byte[]... keys) {
      return this.appendCommand(this.commandObjects.sinter(keys));
   }

   @Override
   public Response<Long> sinterstore(byte[] dstkey, byte[]... keys) {
      return this.appendCommand(this.commandObjects.sinterstore(dstkey, keys));
   }

   @Override
   public Response<Long> sintercard(byte[]... keys) {
      return this.appendCommand(this.commandObjects.sintercard(keys));
   }

   @Override
   public Response<Long> sintercard(int limit, byte[]... keys) {
      return this.appendCommand(this.commandObjects.sintercard(limit, keys));
   }

   @Override
   public Response<Set<byte[]>> sunion(byte[]... keys) {
      return this.appendCommand(this.commandObjects.sunion(keys));
   }

   @Override
   public Response<Long> sunionstore(byte[] dstkey, byte[]... keys) {
      return this.appendCommand(this.commandObjects.sunionstore(dstkey, keys));
   }

   @Override
   public Response<Long> smove(byte[] srckey, byte[] dstkey, byte[] member) {
      return this.appendCommand(this.commandObjects.smove(srckey, dstkey, member));
   }

   @Override
   public Response<Long> zadd(byte[] key, double score, byte[] member) {
      return this.appendCommand(this.commandObjects.zadd(key, score, member));
   }

   @Override
   public Response<Long> zadd(byte[] key, double score, byte[] member, ZAddParams params) {
      return this.appendCommand(this.commandObjects.zadd(key, score, member, params));
   }

   @Override
   public Response<Long> zadd(byte[] key, Map<byte[], Double> scoreMembers) {
      return this.appendCommand(this.commandObjects.zadd(key, scoreMembers));
   }

   @Override
   public Response<Long> zadd(byte[] key, Map<byte[], Double> scoreMembers, ZAddParams params) {
      return this.appendCommand(this.commandObjects.zadd(key, scoreMembers, params));
   }

   @Override
   public Response<Double> zaddIncr(byte[] key, double score, byte[] member, ZAddParams params) {
      return this.appendCommand(this.commandObjects.zaddIncr(key, score, member, params));
   }

   @Override
   public Response<Long> zrem(byte[] key, byte[]... members) {
      return this.appendCommand(this.commandObjects.zrem(key, members));
   }

   @Override
   public Response<Double> zincrby(byte[] key, double increment, byte[] member) {
      return this.appendCommand(this.commandObjects.zincrby(key, increment, member));
   }

   @Override
   public Response<Double> zincrby(byte[] key, double increment, byte[] member, ZIncrByParams params) {
      return this.appendCommand(this.commandObjects.zincrby(key, increment, member, params));
   }

   @Override
   public Response<Long> zrank(byte[] key, byte[] member) {
      return this.appendCommand(this.commandObjects.zrank(key, member));
   }

   @Override
   public Response<Long> zrevrank(byte[] key, byte[] member) {
      return this.appendCommand(this.commandObjects.zrevrank(key, member));
   }

   @Override
   public Response<KeyValue<Long, Double>> zrankWithScore(byte[] key, byte[] member) {
      return this.appendCommand(this.commandObjects.zrankWithScore(key, member));
   }

   @Override
   public Response<KeyValue<Long, Double>> zrevrankWithScore(byte[] key, byte[] member) {
      return this.appendCommand(this.commandObjects.zrevrankWithScore(key, member));
   }

   @Override
   public Response<List<byte[]>> zrange(byte[] key, long start, long stop) {
      return this.appendCommand(this.commandObjects.zrange(key, start, stop));
   }

   @Override
   public Response<List<byte[]>> zrevrange(byte[] key, long start, long stop) {
      return this.appendCommand(this.commandObjects.zrevrange(key, start, stop));
   }

   @Override
   public Response<List<Tuple>> zrangeWithScores(byte[] key, long start, long stop) {
      return this.appendCommand(this.commandObjects.zrangeWithScores(key, start, stop));
   }

   @Override
   public Response<List<Tuple>> zrevrangeWithScores(byte[] key, long start, long stop) {
      return this.appendCommand(this.commandObjects.zrevrangeWithScores(key, start, stop));
   }

   @Override
   public Response<byte[]> zrandmember(byte[] key) {
      return this.appendCommand(this.commandObjects.zrandmember(key));
   }

   @Override
   public Response<List<byte[]>> zrandmember(byte[] key, long count) {
      return this.appendCommand(this.commandObjects.zrandmember(key, count));
   }

   @Override
   public Response<List<Tuple>> zrandmemberWithScores(byte[] key, long count) {
      return this.appendCommand(this.commandObjects.zrandmemberWithScores(key, count));
   }

   @Override
   public Response<Long> zcard(byte[] key) {
      return this.appendCommand(this.commandObjects.zcard(key));
   }

   @Override
   public Response<Double> zscore(byte[] key, byte[] member) {
      return this.appendCommand(this.commandObjects.zscore(key, member));
   }

   @Override
   public Response<List<Double>> zmscore(byte[] key, byte[]... members) {
      return this.appendCommand(this.commandObjects.zmscore(key, members));
   }

   @Override
   public Response<Tuple> zpopmax(byte[] key) {
      return this.appendCommand(this.commandObjects.zpopmax(key));
   }

   @Override
   public Response<List<Tuple>> zpopmax(byte[] key, int count) {
      return this.appendCommand(this.commandObjects.zpopmax(key, count));
   }

   @Override
   public Response<Tuple> zpopmin(byte[] key) {
      return this.appendCommand(this.commandObjects.zpopmin(key));
   }

   @Override
   public Response<List<Tuple>> zpopmin(byte[] key, int count) {
      return this.appendCommand(this.commandObjects.zpopmin(key, count));
   }

   @Override
   public Response<Long> zcount(byte[] key, double min, double max) {
      return this.appendCommand(this.commandObjects.zcount(key, min, max));
   }

   @Override
   public Response<Long> zcount(byte[] key, byte[] min, byte[] max) {
      return this.appendCommand(this.commandObjects.zcount(key, min, max));
   }

   @Override
   public Response<List<byte[]>> zrangeByScore(byte[] key, double min, double max) {
      return this.appendCommand(this.commandObjects.zrangeByScore(key, min, max));
   }

   @Override
   public Response<List<byte[]>> zrangeByScore(byte[] key, byte[] min, byte[] max) {
      return this.appendCommand(this.commandObjects.zrangeByScore(key, min, max));
   }

   @Override
   public Response<List<byte[]>> zrevrangeByScore(byte[] key, double max, double min) {
      return this.appendCommand(this.commandObjects.zrevrangeByScore(key, max, min));
   }

   @Override
   public Response<List<byte[]>> zrangeByScore(byte[] key, double min, double max, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrangeByScore(key, min, max, offset, count));
   }

   @Override
   public Response<List<byte[]>> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
      return this.appendCommand(this.commandObjects.zrevrangeByScore(key, max, min));
   }

   @Override
   public Response<List<byte[]>> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrangeByScore(key, min, max, offset, count));
   }

   @Override
   public Response<List<byte[]>> zrevrangeByScore(byte[] key, double max, double min, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrevrangeByScore(key, max, min, offset, count));
   }

   @Override
   public Response<List<Tuple>> zrangeByScoreWithScores(byte[] key, double min, double max) {
      return this.appendCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max));
   }

   @Override
   public Response<List<Tuple>> zrevrangeByScoreWithScores(byte[] key, double max, double min) {
      return this.appendCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min));
   }

   @Override
   public Response<List<Tuple>> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max, offset, count));
   }

   @Override
   public Response<List<byte[]>> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrevrangeByScore(key, max, min, offset, count));
   }

   @Override
   public Response<List<Tuple>> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
      return this.appendCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max));
   }

   @Override
   public Response<List<Tuple>> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min) {
      return this.appendCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min));
   }

   @Override
   public Response<List<Tuple>> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max, offset, count));
   }

   @Override
   public Response<List<Tuple>> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min, offset, count));
   }

   @Override
   public Response<List<Tuple>> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min, offset, count));
   }

   @Override
   public Response<Long> zremrangeByRank(byte[] key, long start, long stop) {
      return this.appendCommand(this.commandObjects.zremrangeByRank(key, start, stop));
   }

   @Override
   public Response<Long> zremrangeByScore(byte[] key, double min, double max) {
      return this.appendCommand(this.commandObjects.zremrangeByScore(key, min, max));
   }

   @Override
   public Response<Long> zremrangeByScore(byte[] key, byte[] min, byte[] max) {
      return this.appendCommand(this.commandObjects.zremrangeByScore(key, min, max));
   }

   @Override
   public Response<Long> zlexcount(byte[] key, byte[] min, byte[] max) {
      return this.appendCommand(this.commandObjects.zlexcount(key, min, max));
   }

   @Override
   public Response<List<byte[]>> zrangeByLex(byte[] key, byte[] min, byte[] max) {
      return this.appendCommand(this.commandObjects.zrangeByLex(key, min, max));
   }

   @Override
   public Response<List<byte[]>> zrangeByLex(byte[] key, byte[] min, byte[] max, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrangeByLex(key, min, max, offset, count));
   }

   @Override
   public Response<List<byte[]>> zrevrangeByLex(byte[] key, byte[] max, byte[] min) {
      return this.appendCommand(this.commandObjects.zrevrangeByLex(key, max, min));
   }

   @Override
   public Response<List<byte[]>> zrevrangeByLex(byte[] key, byte[] max, byte[] min, int offset, int count) {
      return this.appendCommand(this.commandObjects.zrevrangeByLex(key, max, min, offset, count));
   }

   @Override
   public Response<List<byte[]>> zrange(byte[] key, ZRangeParams zRangeParams) {
      return this.appendCommand(this.commandObjects.zrange(key, zRangeParams));
   }

   @Override
   public Response<List<Tuple>> zrangeWithScores(byte[] key, ZRangeParams zRangeParams) {
      return this.appendCommand(this.commandObjects.zrangeWithScores(key, zRangeParams));
   }

   @Override
   public Response<Long> zrangestore(byte[] dest, byte[] src, ZRangeParams zRangeParams) {
      return this.appendCommand(this.commandObjects.zrangestore(dest, src, zRangeParams));
   }

   @Override
   public Response<Long> zremrangeByLex(byte[] key, byte[] min, byte[] max) {
      return this.appendCommand(this.commandObjects.zremrangeByLex(key, min, max));
   }

   @Override
   public Response<ScanResult<Tuple>> zscan(byte[] key, byte[] cursor, ScanParams params) {
      return this.appendCommand(this.commandObjects.zscan(key, cursor, params));
   }

   @Override
   public Response<KeyValue<byte[], Tuple>> bzpopmax(double timeout, byte[]... keys) {
      return this.appendCommand(this.commandObjects.bzpopmax(timeout, keys));
   }

   @Override
   public Response<KeyValue<byte[], Tuple>> bzpopmin(double timeout, byte[]... keys) {
      return this.appendCommand(this.commandObjects.bzpopmin(timeout, keys));
   }

   @Override
   public Response<KeyValue<byte[], List<Tuple>>> zmpop(SortedSetOption option, byte[]... keys) {
      return this.appendCommand(this.commandObjects.zmpop(option, keys));
   }

   @Override
   public Response<KeyValue<byte[], List<Tuple>>> zmpop(SortedSetOption option, int count, byte[]... keys) {
      return this.appendCommand(this.commandObjects.zmpop(option, count, keys));
   }

   @Override
   public Response<KeyValue<byte[], List<Tuple>>> bzmpop(double timeout, SortedSetOption option, byte[]... keys) {
      return this.appendCommand(this.commandObjects.bzmpop(timeout, option, keys));
   }

   @Override
   public Response<KeyValue<byte[], List<Tuple>>> bzmpop(double timeout, SortedSetOption option, int count, byte[]... keys) {
      return this.appendCommand(this.commandObjects.bzmpop(timeout, option, count, keys));
   }

   @Override
   public Response<List<byte[]>> zdiff(byte[]... keys) {
      return this.appendCommand(this.commandObjects.zdiff(keys));
   }

   @Override
   public Response<List<Tuple>> zdiffWithScores(byte[]... keys) {
      return this.appendCommand(this.commandObjects.zdiffWithScores(keys));
   }

   @Deprecated
   @Override
   public Response<Long> zdiffStore(byte[] dstkey, byte[]... keys) {
      return this.appendCommand(this.commandObjects.zdiffStore(dstkey, keys));
   }

   @Override
   public Response<Long> zdiffstore(byte[] dstkey, byte[]... keys) {
      return this.appendCommand(this.commandObjects.zdiffstore(dstkey, keys));
   }

   @Override
   public Response<List<byte[]>> zinter(ZParams params, byte[]... keys) {
      return this.appendCommand(this.commandObjects.zinter(params, keys));
   }

   @Override
   public Response<List<Tuple>> zinterWithScores(ZParams params, byte[]... keys) {
      return this.appendCommand(this.commandObjects.zinterWithScores(params, keys));
   }

   @Override
   public Response<Long> zinterstore(byte[] dstkey, byte[]... sets) {
      return this.appendCommand(this.commandObjects.zinterstore(dstkey, sets));
   }

   @Override
   public Response<Long> zinterstore(byte[] dstkey, ZParams params, byte[]... sets) {
      return this.appendCommand(this.commandObjects.zinterstore(dstkey, params, sets));
   }

   @Override
   public Response<Long> zintercard(byte[]... keys) {
      return this.appendCommand(this.commandObjects.zintercard(keys));
   }

   @Override
   public Response<Long> zintercard(long limit, byte[]... keys) {
      return this.appendCommand(this.commandObjects.zintercard(limit, keys));
   }

   @Override
   public Response<List<byte[]>> zunion(ZParams params, byte[]... keys) {
      return this.appendCommand(this.commandObjects.zunion(params, keys));
   }

   @Override
   public Response<List<Tuple>> zunionWithScores(ZParams params, byte[]... keys) {
      return this.appendCommand(this.commandObjects.zunionWithScores(params, keys));
   }

   @Override
   public Response<Long> zunionstore(byte[] dstkey, byte[]... sets) {
      return this.appendCommand(this.commandObjects.zunionstore(dstkey, sets));
   }

   @Override
   public Response<Long> zunionstore(byte[] dstkey, ZParams params, byte[]... sets) {
      return this.appendCommand(this.commandObjects.zunionstore(dstkey, params, sets));
   }

   @Override
   public Response<byte[]> xadd(byte[] key, XAddParams params, Map<byte[], byte[]> hash) {
      return this.appendCommand(this.commandObjects.xadd(key, params, hash));
   }

   @Override
   public Response<Long> xlen(byte[] key) {
      return this.appendCommand(this.commandObjects.xlen(key));
   }

   @Override
   public Response<List<Object>> xrange(byte[] key, byte[] start, byte[] end) {
      return this.appendCommand(this.commandObjects.xrange(key, start, end));
   }

   @Override
   public Response<List<Object>> xrange(byte[] key, byte[] start, byte[] end, int count) {
      return this.appendCommand(this.commandObjects.xrange(key, start, end, count));
   }

   @Override
   public Response<List<Object>> xrevrange(byte[] key, byte[] end, byte[] start) {
      return this.appendCommand(this.commandObjects.xrevrange(key, end, start));
   }

   @Override
   public Response<List<Object>> xrevrange(byte[] key, byte[] end, byte[] start, int count) {
      return this.appendCommand(this.commandObjects.xrevrange(key, end, start, count));
   }

   @Override
   public Response<Long> xack(byte[] key, byte[] group, byte[]... ids) {
      return this.appendCommand(this.commandObjects.xack(key, group, ids));
   }

   @Override
   public Response<String> xgroupCreate(byte[] key, byte[] groupName, byte[] id, boolean makeStream) {
      return this.appendCommand(this.commandObjects.xgroupCreate(key, groupName, id, makeStream));
   }

   @Override
   public Response<String> xgroupSetID(byte[] key, byte[] groupName, byte[] id) {
      return this.appendCommand(this.commandObjects.xgroupSetID(key, groupName, id));
   }

   @Override
   public Response<Long> xgroupDestroy(byte[] key, byte[] groupName) {
      return this.appendCommand(this.commandObjects.xgroupDestroy(key, groupName));
   }

   @Override
   public Response<Boolean> xgroupCreateConsumer(byte[] key, byte[] groupName, byte[] consumerName) {
      return this.appendCommand(this.commandObjects.xgroupCreateConsumer(key, groupName, consumerName));
   }

   @Override
   public Response<Long> xgroupDelConsumer(byte[] key, byte[] groupName, byte[] consumerName) {
      return this.appendCommand(this.commandObjects.xgroupDelConsumer(key, groupName, consumerName));
   }

   @Override
   public Response<Long> xdel(byte[] key, byte[]... ids) {
      return this.appendCommand(this.commandObjects.xdel(key, ids));
   }

   @Override
   public Response<Long> xtrim(byte[] key, long maxLen, boolean approximateLength) {
      return this.appendCommand(this.commandObjects.xtrim(key, maxLen, approximateLength));
   }

   @Override
   public Response<Long> xtrim(byte[] key, XTrimParams params) {
      return this.appendCommand(this.commandObjects.xtrim(key, params));
   }

   @Override
   public Response<Object> xpending(byte[] key, byte[] groupName) {
      return this.appendCommand(this.commandObjects.xpending(key, groupName));
   }

   @Override
   public Response<List<Object>> xpending(byte[] key, byte[] groupName, XPendingParams params) {
      return this.appendCommand(this.commandObjects.xpending(key, groupName, params));
   }

   @Override
   public Response<List<byte[]>> xclaim(byte[] key, byte[] group, byte[] consumerName, long minIdleTime, XClaimParams params, byte[]... ids) {
      return this.appendCommand(this.commandObjects.xclaim(key, group, consumerName, minIdleTime, params, ids));
   }

   @Override
   public Response<List<byte[]>> xclaimJustId(byte[] key, byte[] group, byte[] consumerName, long minIdleTime, XClaimParams params, byte[]... ids) {
      return this.appendCommand(this.commandObjects.xclaimJustId(key, group, consumerName, minIdleTime, params, ids));
   }

   @Override
   public Response<List<Object>> xautoclaim(byte[] key, byte[] groupName, byte[] consumerName, long minIdleTime, byte[] start, XAutoClaimParams params) {
      return this.appendCommand(this.commandObjects.xautoclaim(key, groupName, consumerName, minIdleTime, start, params));
   }

   @Override
   public Response<List<Object>> xautoclaimJustId(byte[] key, byte[] groupName, byte[] consumerName, long minIdleTime, byte[] start, XAutoClaimParams params) {
      return this.appendCommand(this.commandObjects.xautoclaimJustId(key, groupName, consumerName, minIdleTime, start, params));
   }

   @Override
   public Response<Object> xinfoStream(byte[] key) {
      return this.appendCommand(this.commandObjects.xinfoStream(key));
   }

   @Override
   public Response<Object> xinfoStreamFull(byte[] key) {
      return this.appendCommand(this.commandObjects.xinfoStreamFull(key));
   }

   @Override
   public Response<Object> xinfoStreamFull(byte[] key, int count) {
      return this.appendCommand(this.commandObjects.xinfoStreamFull(key, count));
   }

   @Override
   public Response<List<Object>> xinfoGroups(byte[] key) {
      return this.appendCommand(this.commandObjects.xinfoGroups(key));
   }

   @Override
   public Response<List<Object>> xinfoConsumers(byte[] key, byte[] group) {
      return this.appendCommand(this.commandObjects.xinfoConsumers(key, group));
   }

   @Override
   public Response<List<Object>> xread(XReadParams xReadParams, Entry<byte[], byte[]>... streams) {
      return this.appendCommand(this.commandObjects.xread(xReadParams, streams));
   }

   @Override
   public Response<List<Object>> xreadGroup(byte[] groupName, byte[] consumer, XReadGroupParams xReadGroupParams, Entry<byte[], byte[]>... streams) {
      return this.appendCommand(this.commandObjects.xreadGroup(groupName, consumer, xReadGroupParams, streams));
   }

   @Override
   public Response<String> set(byte[] key, byte[] value) {
      return this.appendCommand(this.commandObjects.set(key, value));
   }

   @Override
   public Response<String> set(byte[] key, byte[] value, SetParams params) {
      return this.appendCommand(this.commandObjects.set(key, value, params));
   }

   @Override
   public Response<byte[]> get(byte[] key) {
      return this.appendCommand(this.commandObjects.get(key));
   }

   @Override
   public Response<byte[]> setGet(byte[] key, byte[] value) {
      return this.appendCommand(this.commandObjects.setGet(key, value));
   }

   @Override
   public Response<byte[]> setGet(byte[] key, byte[] value, SetParams params) {
      return this.appendCommand(this.commandObjects.setGet(key, value, params));
   }

   @Override
   public Response<byte[]> getDel(byte[] key) {
      return this.appendCommand(this.commandObjects.getDel(key));
   }

   @Override
   public Response<byte[]> getEx(byte[] key, GetExParams params) {
      return this.appendCommand(this.commandObjects.getEx(key, params));
   }

   @Override
   public Response<Boolean> setbit(byte[] key, long offset, boolean value) {
      return this.appendCommand(this.commandObjects.setbit(key, offset, value));
   }

   @Override
   public Response<Boolean> getbit(byte[] key, long offset) {
      return this.appendCommand(this.commandObjects.getbit(key, offset));
   }

   @Override
   public Response<Long> setrange(byte[] key, long offset, byte[] value) {
      return this.appendCommand(this.commandObjects.setrange(key, offset, value));
   }

   @Override
   public Response<byte[]> getrange(byte[] key, long startOffset, long endOffset) {
      return this.appendCommand(this.commandObjects.getrange(key, startOffset, endOffset));
   }

   @Deprecated
   @Override
   public Response<byte[]> getSet(byte[] key, byte[] value) {
      return this.appendCommand(this.commandObjects.getSet(key, value));
   }

   @Override
   public Response<Long> setnx(byte[] key, byte[] value) {
      return this.appendCommand(this.commandObjects.setnx(key, value));
   }

   @Override
   public Response<String> setex(byte[] key, long seconds, byte[] value) {
      return this.appendCommand(this.commandObjects.setex(key, seconds, value));
   }

   @Override
   public Response<String> psetex(byte[] key, long milliseconds, byte[] value) {
      return this.appendCommand(this.commandObjects.psetex(key, milliseconds, value));
   }

   @Override
   public Response<List<byte[]>> mget(byte[]... keys) {
      return this.appendCommand(this.commandObjects.mget(keys));
   }

   @Override
   public Response<String> mset(byte[]... keysvalues) {
      return this.appendCommand(this.commandObjects.mset(keysvalues));
   }

   @Override
   public Response<Long> msetnx(byte[]... keysvalues) {
      return this.appendCommand(this.commandObjects.msetnx(keysvalues));
   }

   @Override
   public Response<Long> incr(byte[] key) {
      return this.appendCommand(this.commandObjects.incr(key));
   }

   @Override
   public Response<Long> incrBy(byte[] key, long increment) {
      return this.appendCommand(this.commandObjects.incrBy(key, increment));
   }

   @Override
   public Response<Double> incrByFloat(byte[] key, double increment) {
      return this.appendCommand(this.commandObjects.incrByFloat(key, increment));
   }

   @Override
   public Response<Long> decr(byte[] key) {
      return this.appendCommand(this.commandObjects.decr(key));
   }

   @Override
   public Response<Long> decrBy(byte[] key, long decrement) {
      return this.appendCommand(this.commandObjects.decrBy(key, decrement));
   }

   @Override
   public Response<Long> append(byte[] key, byte[] value) {
      return this.appendCommand(this.commandObjects.append(key, value));
   }

   @Override
   public Response<byte[]> substr(byte[] key, int start, int end) {
      return this.appendCommand(this.commandObjects.substr(key, start, end));
   }

   @Override
   public Response<Long> strlen(byte[] key) {
      return this.appendCommand(this.commandObjects.strlen(key));
   }

   @Override
   public Response<Long> bitcount(byte[] key) {
      return this.appendCommand(this.commandObjects.bitcount(key));
   }

   @Override
   public Response<Long> bitcount(byte[] key, long start, long end) {
      return this.appendCommand(this.commandObjects.bitcount(key, start, end));
   }

   @Override
   public Response<Long> bitcount(byte[] key, long start, long end, BitCountOption option) {
      return this.appendCommand(this.commandObjects.bitcount(key, start, end, option));
   }

   @Override
   public Response<Long> bitpos(byte[] key, boolean value) {
      return this.appendCommand(this.commandObjects.bitpos(key, value));
   }

   @Override
   public Response<Long> bitpos(byte[] key, boolean value, BitPosParams params) {
      return this.appendCommand(this.commandObjects.bitpos(key, value, params));
   }

   @Override
   public Response<List<Long>> bitfield(byte[] key, byte[]... arguments) {
      return this.appendCommand(this.commandObjects.bitfield(key, arguments));
   }

   @Override
   public Response<List<Long>> bitfieldReadonly(byte[] key, byte[]... arguments) {
      return this.appendCommand(this.commandObjects.bitfieldReadonly(key, arguments));
   }

   @Override
   public Response<Long> bitop(BitOP op, byte[] destKey, byte[]... srcKeys) {
      return this.appendCommand(this.commandObjects.bitop(op, destKey, srcKeys));
   }

   @Override
   public Response<String> ftCreate(String indexName, IndexOptions indexOptions, Schema schema) {
      return this.appendCommand(this.commandObjects.ftCreate(indexName, indexOptions, schema));
   }

   @Override
   public Response<String> ftCreate(String indexName, FTCreateParams createParams, Iterable<SchemaField> schemaFields) {
      return this.appendCommand(this.commandObjects.ftCreate(indexName, createParams, schemaFields));
   }

   @Override
   public Response<String> ftAlter(String indexName, Schema schema) {
      return this.appendCommand(this.commandObjects.ftAlter(indexName, schema));
   }

   @Override
   public Response<String> ftAlter(String indexName, Iterable<SchemaField> schemaFields) {
      return this.appendCommand(this.commandObjects.ftAlter(indexName, schemaFields));
   }

   @Override
   public Response<String> ftAliasAdd(String aliasName, String indexName) {
      return this.appendCommand(this.commandObjects.ftAliasAdd(aliasName, indexName));
   }

   @Override
   public Response<String> ftAliasUpdate(String aliasName, String indexName) {
      return this.appendCommand(this.commandObjects.ftAliasUpdate(aliasName, indexName));
   }

   @Override
   public Response<String> ftAliasDel(String aliasName) {
      return this.appendCommand(this.commandObjects.ftAliasDel(aliasName));
   }

   @Override
   public Response<String> ftDropIndex(String indexName) {
      return this.appendCommand(this.commandObjects.ftDropIndex(indexName));
   }

   @Override
   public Response<String> ftDropIndexDD(String indexName) {
      return this.appendCommand(this.commandObjects.ftDropIndexDD(indexName));
   }

   @Override
   public Response<SearchResult> ftSearch(String indexName, String query) {
      return this.appendCommand(this.commandObjects.ftSearch(indexName, query));
   }

   @Override
   public Response<SearchResult> ftSearch(String indexName, String query, FTSearchParams searchParams) {
      return this.appendCommand(this.commandObjects.ftSearch(indexName, query, searchParams));
   }

   @Override
   public Response<SearchResult> ftSearch(String indexName, Query query) {
      return this.appendCommand(this.commandObjects.ftSearch(indexName, query));
   }

   @Deprecated
   @Override
   public Response<SearchResult> ftSearch(byte[] indexName, Query query) {
      return this.appendCommand(this.commandObjects.ftSearch(indexName, query));
   }

   @Override
   public Response<String> ftExplain(String indexName, Query query) {
      return this.appendCommand(this.commandObjects.ftExplain(indexName, query));
   }

   @Override
   public Response<List<String>> ftExplainCLI(String indexName, Query query) {
      return this.appendCommand(this.commandObjects.ftExplainCLI(indexName, query));
   }

   @Override
   public Response<AggregationResult> ftAggregate(String indexName, AggregationBuilder aggr) {
      return this.appendCommand(this.commandObjects.ftAggregate(indexName, aggr));
   }

   @Override
   public Response<String> ftSynUpdate(String indexName, String synonymGroupId, String... terms) {
      return this.appendCommand(this.commandObjects.ftSynUpdate(indexName, synonymGroupId, terms));
   }

   @Override
   public Response<Map<String, List<String>>> ftSynDump(String indexName) {
      return this.appendCommand(this.commandObjects.ftSynDump(indexName));
   }

   @Override
   public Response<Long> ftDictAdd(String dictionary, String... terms) {
      return this.appendCommand(this.commandObjects.ftDictAdd(dictionary, terms));
   }

   @Override
   public Response<Long> ftDictDel(String dictionary, String... terms) {
      return this.appendCommand(this.commandObjects.ftDictDel(dictionary, terms));
   }

   @Override
   public Response<Set<String>> ftDictDump(String dictionary) {
      return this.appendCommand(this.commandObjects.ftDictDump(dictionary));
   }

   @Override
   public Response<Long> ftDictAddBySampleKey(String indexName, String dictionary, String... terms) {
      return this.appendCommand(this.commandObjects.ftDictAddBySampleKey(indexName, dictionary, terms));
   }

   @Override
   public Response<Long> ftDictDelBySampleKey(String indexName, String dictionary, String... terms) {
      return this.appendCommand(this.commandObjects.ftDictDelBySampleKey(indexName, dictionary, terms));
   }

   @Override
   public Response<Set<String>> ftDictDumpBySampleKey(String indexName, String dictionary) {
      return this.appendCommand(this.commandObjects.ftDictDumpBySampleKey(indexName, dictionary));
   }

   @Override
   public Response<Map<String, Map<String, Double>>> ftSpellCheck(String index, String query) {
      return this.appendCommand(this.commandObjects.ftSpellCheck(index, query));
   }

   @Override
   public Response<Map<String, Map<String, Double>>> ftSpellCheck(String index, String query, FTSpellCheckParams spellCheckParams) {
      return this.appendCommand(this.commandObjects.ftSpellCheck(index, query, spellCheckParams));
   }

   @Override
   public Response<Map<String, Object>> ftInfo(String indexName) {
      return this.appendCommand(this.commandObjects.ftInfo(indexName));
   }

   @Override
   public Response<Set<String>> ftTagVals(String indexName, String fieldName) {
      return this.appendCommand(this.commandObjects.ftTagVals(indexName, fieldName));
   }

   @Override
   public Response<Map<String, Object>> ftConfigGet(String option) {
      return this.appendCommand(this.commandObjects.ftConfigGet(option));
   }

   @Override
   public Response<Map<String, Object>> ftConfigGet(String indexName, String option) {
      return this.appendCommand(this.commandObjects.ftConfigGet(indexName, option));
   }

   @Override
   public Response<String> ftConfigSet(String option, String value) {
      return this.appendCommand(this.commandObjects.ftConfigSet(option, value));
   }

   @Override
   public Response<String> ftConfigSet(String indexName, String option, String value) {
      return this.appendCommand(this.commandObjects.ftConfigSet(indexName, option, value));
   }

   @Override
   public Response<Long> ftSugAdd(String key, String string, double score) {
      return this.appendCommand(this.commandObjects.ftSugAdd(key, string, score));
   }

   @Override
   public Response<Long> ftSugAddIncr(String key, String string, double score) {
      return this.appendCommand(this.commandObjects.ftSugAddIncr(key, string, score));
   }

   @Override
   public Response<List<String>> ftSugGet(String key, String prefix) {
      return this.appendCommand(this.commandObjects.ftSugGet(key, prefix));
   }

   @Override
   public Response<List<String>> ftSugGet(String key, String prefix, boolean fuzzy, int max) {
      return this.appendCommand(this.commandObjects.ftSugGet(key, prefix, fuzzy, max));
   }

   @Override
   public Response<List<Tuple>> ftSugGetWithScores(String key, String prefix) {
      return this.appendCommand(this.commandObjects.ftSugGetWithScores(key, prefix));
   }

   @Override
   public Response<List<Tuple>> ftSugGetWithScores(String key, String prefix, boolean fuzzy, int max) {
      return this.appendCommand(this.commandObjects.ftSugGetWithScores(key, prefix, fuzzy, max));
   }

   @Override
   public Response<Boolean> ftSugDel(String key, String string) {
      return this.appendCommand(this.commandObjects.ftSugDel(key, string));
   }

   @Override
   public Response<Long> ftSugLen(String key) {
      return this.appendCommand(this.commandObjects.ftSugLen(key));
   }

   @Override
   public Response<LCSMatchResult> lcs(byte[] keyA, byte[] keyB, LCSParams params) {
      return this.appendCommand(this.commandObjects.lcs(keyA, keyB, params));
   }

   @Override
   public Response<String> jsonSet(String key, Path2 path, Object object) {
      return this.appendCommand(this.commandObjects.jsonSet(key, path, object));
   }

   @Override
   public Response<String> jsonSetWithEscape(String key, Path2 path, Object object) {
      return this.appendCommand(this.commandObjects.jsonSetWithEscape(key, path, object));
   }

   @Override
   public Response<String> jsonSet(String key, Path path, Object object) {
      return this.appendCommand(this.commandObjects.jsonSet(key, path, object));
   }

   @Override
   public Response<String> jsonSet(String key, Path2 path, Object object, JsonSetParams params) {
      return this.appendCommand(this.commandObjects.jsonSet(key, path, object, params));
   }

   @Override
   public Response<String> jsonSetWithEscape(String key, Path2 path, Object object, JsonSetParams params) {
      return this.appendCommand(this.commandObjects.jsonSetWithEscape(key, path, object, params));
   }

   @Override
   public Response<String> jsonSet(String key, Path path, Object object, JsonSetParams params) {
      return this.appendCommand(this.commandObjects.jsonSet(key, path, object, params));
   }

   @Override
   public Response<String> jsonMerge(String key, Path2 path, Object object) {
      return this.appendCommand(this.commandObjects.jsonMerge(key, path, object));
   }

   @Override
   public Response<String> jsonMerge(String key, Path path, Object object) {
      return this.appendCommand(this.commandObjects.jsonMerge(key, path, object));
   }

   @Override
   public Response<Object> jsonGet(String key) {
      return this.appendCommand(this.commandObjects.jsonGet(key));
   }

   @Override
   public <T> Response<T> jsonGet(String key, Class<T> clazz) {
      return this.appendCommand(this.commandObjects.jsonGet(key, clazz));
   }

   @Override
   public Response<Object> jsonGet(String key, Path2... paths) {
      return this.appendCommand(this.commandObjects.jsonGet(key, paths));
   }

   @Override
   public Response<Object> jsonGet(String key, Path... paths) {
      return this.appendCommand(this.commandObjects.jsonGet(key, paths));
   }

   @Override
   public <T> Response<T> jsonGet(String key, Class<T> clazz, Path... paths) {
      return this.appendCommand(this.commandObjects.jsonGet(key, clazz, paths));
   }

   @Override
   public Response<List<JSONArray>> jsonMGet(Path2 path, String... keys) {
      return this.appendCommand(this.commandObjects.jsonMGet(path, keys));
   }

   @Override
   public <T> Response<List<T>> jsonMGet(Path path, Class<T> clazz, String... keys) {
      return this.appendCommand(this.commandObjects.jsonMGet(path, clazz, keys));
   }

   @Override
   public Response<Long> jsonDel(String key) {
      return this.appendCommand(this.commandObjects.jsonDel(key));
   }

   @Override
   public Response<Long> jsonDel(String key, Path2 path) {
      return this.appendCommand(this.commandObjects.jsonDel(key, path));
   }

   @Override
   public Response<Long> jsonDel(String key, Path path) {
      return this.appendCommand(this.commandObjects.jsonDel(key, path));
   }

   @Override
   public Response<Long> jsonClear(String key) {
      return this.appendCommand(this.commandObjects.jsonClear(key));
   }

   @Override
   public Response<Long> jsonClear(String key, Path2 path) {
      return this.appendCommand(this.commandObjects.jsonClear(key, path));
   }

   @Override
   public Response<Long> jsonClear(String key, Path path) {
      return this.appendCommand(this.commandObjects.jsonClear(key, path));
   }

   @Override
   public Response<List<Boolean>> jsonToggle(String key, Path2 path) {
      return this.appendCommand(this.commandObjects.jsonToggle(key, path));
   }

   @Override
   public Response<String> jsonToggle(String key, Path path) {
      return this.appendCommand(this.commandObjects.jsonToggle(key, path));
   }

   @Override
   public Response<Class<?>> jsonType(String key) {
      return this.appendCommand(this.commandObjects.jsonType(key));
   }

   @Override
   public Response<List<Class<?>>> jsonType(String key, Path2 path) {
      return this.appendCommand(this.commandObjects.jsonType(key, path));
   }

   @Override
   public Response<Class<?>> jsonType(String key, Path path) {
      return this.appendCommand(this.commandObjects.jsonType(key, path));
   }

   @Override
   public Response<Long> jsonStrAppend(String key, Object string) {
      return this.appendCommand(this.commandObjects.jsonStrAppend(key, string));
   }

   @Override
   public Response<List<Long>> jsonStrAppend(String key, Path2 path, Object string) {
      return this.appendCommand(this.commandObjects.jsonStrAppend(key, path, string));
   }

   @Override
   public Response<Long> jsonStrAppend(String key, Path path, Object string) {
      return this.appendCommand(this.commandObjects.jsonStrAppend(key, path, string));
   }

   @Override
   public Response<Long> jsonStrLen(String key) {
      return this.appendCommand(this.commandObjects.jsonStrLen(key));
   }

   @Override
   public Response<List<Long>> jsonStrLen(String key, Path2 path) {
      return this.appendCommand(this.commandObjects.jsonStrLen(key, path));
   }

   @Override
   public Response<Long> jsonStrLen(String key, Path path) {
      return this.appendCommand(this.commandObjects.jsonStrLen(key, path));
   }

   @Override
   public Response<Object> jsonNumIncrBy(String key, Path2 path, double value) {
      return this.appendCommand(this.commandObjects.jsonNumIncrBy(key, path, value));
   }

   @Override
   public Response<Double> jsonNumIncrBy(String key, Path path, double value) {
      return this.appendCommand(this.commandObjects.jsonNumIncrBy(key, path, value));
   }

   @Override
   public Response<List<Long>> jsonArrAppend(String key, Path2 path, Object... objects) {
      return this.appendCommand(this.commandObjects.jsonArrAppend(key, path, objects));
   }

   @Override
   public Response<List<Long>> jsonArrAppendWithEscape(String key, Path2 path, Object... objects) {
      return this.appendCommand(this.commandObjects.jsonArrAppendWithEscape(key, path, objects));
   }

   @Override
   public Response<Long> jsonArrAppend(String key, Path path, Object... objects) {
      return this.appendCommand(this.commandObjects.jsonArrAppend(key, path, objects));
   }

   @Override
   public Response<List<Long>> jsonArrIndex(String key, Path2 path, Object scalar) {
      return this.appendCommand(this.commandObjects.jsonArrIndex(key, path, scalar));
   }

   @Override
   public Response<List<Long>> jsonArrIndexWithEscape(String key, Path2 path, Object scalar) {
      return this.appendCommand(this.commandObjects.jsonArrIndexWithEscape(key, path, scalar));
   }

   @Override
   public Response<Long> jsonArrIndex(String key, Path path, Object scalar) {
      return this.appendCommand(this.commandObjects.jsonArrIndex(key, path, scalar));
   }

   @Override
   public Response<List<Long>> jsonArrInsert(String key, Path2 path, int index, Object... objects) {
      return this.appendCommand(this.commandObjects.jsonArrInsert(key, path, index, objects));
   }

   @Override
   public Response<List<Long>> jsonArrInsertWithEscape(String key, Path2 path, int index, Object... objects) {
      return this.appendCommand(this.commandObjects.jsonArrInsertWithEscape(key, path, index, objects));
   }

   @Override
   public Response<Long> jsonArrInsert(String key, Path path, int index, Object... pojos) {
      return this.appendCommand(this.commandObjects.jsonArrInsert(key, path, index, pojos));
   }

   @Override
   public Response<Object> jsonArrPop(String key) {
      return this.appendCommand(this.commandObjects.jsonArrPop(key));
   }

   @Override
   public Response<Long> jsonArrLen(String key, Path path) {
      return this.appendCommand(this.commandObjects.jsonArrLen(key, path));
   }

   @Override
   public Response<List<Long>> jsonArrTrim(String key, Path2 path, int start, int stop) {
      return this.appendCommand(this.commandObjects.jsonArrTrim(key, path, start, stop));
   }

   @Override
   public Response<Long> jsonArrTrim(String key, Path path, int start, int stop) {
      return this.appendCommand(this.commandObjects.jsonArrTrim(key, path, start, stop));
   }

   @Override
   public <T> Response<T> jsonArrPop(String key, Class<T> clazz, Path path) {
      return this.appendCommand(this.commandObjects.jsonArrPop(key, clazz, path));
   }

   @Override
   public Response<List<Object>> jsonArrPop(String key, Path2 path, int index) {
      return this.appendCommand(this.commandObjects.jsonArrPop(key, path, index));
   }

   @Override
   public Response<Object> jsonArrPop(String key, Path path, int index) {
      return this.appendCommand(this.commandObjects.jsonArrPop(key, path, index));
   }

   @Override
   public <T> Response<T> jsonArrPop(String key, Class<T> clazz, Path path, int index) {
      return this.appendCommand(this.commandObjects.jsonArrPop(key, clazz, path, index));
   }

   @Override
   public Response<Long> jsonArrLen(String key) {
      return this.appendCommand(this.commandObjects.jsonArrLen(key));
   }

   @Override
   public Response<List<Long>> jsonArrLen(String key, Path2 path) {
      return this.appendCommand(this.commandObjects.jsonArrLen(key, path));
   }

   @Override
   public <T> Response<T> jsonArrPop(String key, Class<T> clazz) {
      return this.appendCommand(this.commandObjects.jsonArrPop(key, clazz));
   }

   @Override
   public Response<List<Object>> jsonArrPop(String key, Path2 path) {
      return this.appendCommand(this.commandObjects.jsonArrPop(key, path));
   }

   @Override
   public Response<Object> jsonArrPop(String key, Path path) {
      return this.appendCommand(this.commandObjects.jsonArrPop(key, path));
   }

   @Override
   public Response<String> tsCreate(String key) {
      return this.appendCommand(this.commandObjects.tsCreate(key));
   }

   @Override
   public Response<String> tsCreate(String key, TSCreateParams createParams) {
      return this.appendCommand(this.commandObjects.tsCreate(key, createParams));
   }

   @Override
   public Response<Long> tsDel(String key, long fromTimestamp, long toTimestamp) {
      return this.appendCommand(this.commandObjects.tsDel(key, fromTimestamp, toTimestamp));
   }

   @Override
   public Response<String> tsAlter(String key, TSAlterParams alterParams) {
      return this.appendCommand(this.commandObjects.tsAlter(key, alterParams));
   }

   @Override
   public Response<Long> tsAdd(String key, double value) {
      return this.appendCommand(this.commandObjects.tsAdd(key, value));
   }

   @Override
   public Response<Long> tsAdd(String key, long timestamp, double value) {
      return this.appendCommand(this.commandObjects.tsAdd(key, timestamp, value));
   }

   @Override
   public Response<Long> tsAdd(String key, long timestamp, double value, TSCreateParams createParams) {
      return this.appendCommand(this.commandObjects.tsAdd(key, timestamp, value, createParams));
   }

   @Override
   public Response<Long> tsAdd(String key, long timestamp, double value, TSAddParams addParams) {
      return this.appendCommand(this.commandObjects.tsAdd(key, timestamp, value, addParams));
   }

   @Override
   public Response<List<Long>> tsMAdd(Entry<String, TSElement>... entries) {
      return this.appendCommand(this.commandObjects.tsMAdd(entries));
   }

   @Override
   public Response<Long> tsIncrBy(String key, double value) {
      return this.appendCommand(this.commandObjects.tsIncrBy(key, value));
   }

   @Override
   public Response<Long> tsIncrBy(String key, double value, long timestamp) {
      return this.appendCommand(this.commandObjects.tsIncrBy(key, value, timestamp));
   }

   @Override
   public Response<Long> tsIncrBy(String key, double addend, TSIncrByParams incrByParams) {
      return this.appendCommand(this.commandObjects.tsIncrBy(key, addend, incrByParams));
   }

   @Override
   public Response<Long> tsDecrBy(String key, double value) {
      return this.appendCommand(this.commandObjects.tsDecrBy(key, value));
   }

   @Override
   public Response<Long> tsDecrBy(String key, double value, long timestamp) {
      return this.appendCommand(this.commandObjects.tsDecrBy(key, value, timestamp));
   }

   @Override
   public Response<Long> tsDecrBy(String key, double subtrahend, TSDecrByParams decrByParams) {
      return this.appendCommand(this.commandObjects.tsDecrBy(key, subtrahend, decrByParams));
   }

   @Override
   public Response<List<TSElement>> tsRange(String key, long fromTimestamp, long toTimestamp) {
      return this.appendCommand(this.commandObjects.tsRange(key, fromTimestamp, toTimestamp));
   }

   @Override
   public Response<List<TSElement>> tsRange(String key, TSRangeParams rangeParams) {
      return this.appendCommand(this.commandObjects.tsRange(key, rangeParams));
   }

   @Override
   public Response<List<TSElement>> tsRevRange(String key, long fromTimestamp, long toTimestamp) {
      return this.appendCommand(this.commandObjects.tsRevRange(key, fromTimestamp, toTimestamp));
   }

   @Override
   public Response<List<TSElement>> tsRevRange(String key, TSRangeParams rangeParams) {
      return this.appendCommand(this.commandObjects.tsRevRange(key, rangeParams));
   }

   @Override
   public Response<Map<String, TSMRangeElements>> tsMRange(long fromTimestamp, long toTimestamp, String... filters) {
      return this.appendCommand(this.commandObjects.tsMRange(fromTimestamp, toTimestamp, filters));
   }

   @Override
   public Response<Map<String, TSMRangeElements>> tsMRange(TSMRangeParams multiRangeParams) {
      return this.appendCommand(this.commandObjects.tsMRange(multiRangeParams));
   }

   @Override
   public Response<Map<String, TSMRangeElements>> tsMRevRange(long fromTimestamp, long toTimestamp, String... filters) {
      return this.appendCommand(this.commandObjects.tsMRevRange(fromTimestamp, toTimestamp, filters));
   }

   @Override
   public Response<Map<String, TSMRangeElements>> tsMRevRange(TSMRangeParams multiRangeParams) {
      return this.appendCommand(this.commandObjects.tsMRevRange(multiRangeParams));
   }

   @Override
   public Response<TSElement> tsGet(String key) {
      return this.appendCommand(this.commandObjects.tsGet(key));
   }

   @Override
   public Response<TSElement> tsGet(String key, TSGetParams getParams) {
      return this.appendCommand(this.commandObjects.tsGet(key, getParams));
   }

   @Override
   public Response<Map<String, TSMGetElement>> tsMGet(TSMGetParams multiGetParams, String... filters) {
      return this.appendCommand(this.commandObjects.tsMGet(multiGetParams, filters));
   }

   @Override
   public Response<String> tsCreateRule(String sourceKey, String destKey, AggregationType aggregationType, long timeBucket) {
      return this.appendCommand(this.commandObjects.tsCreateRule(sourceKey, destKey, aggregationType, timeBucket));
   }

   @Override
   public Response<String> tsCreateRule(String sourceKey, String destKey, AggregationType aggregationType, long bucketDuration, long alignTimestamp) {
      return this.appendCommand(this.commandObjects.tsCreateRule(sourceKey, destKey, aggregationType, bucketDuration, alignTimestamp));
   }

   @Override
   public Response<String> tsDeleteRule(String sourceKey, String destKey) {
      return this.appendCommand(this.commandObjects.tsDeleteRule(sourceKey, destKey));
   }

   @Override
   public Response<List<String>> tsQueryIndex(String... filters) {
      return this.appendCommand(this.commandObjects.tsQueryIndex(filters));
   }

   @Override
   public Response<TSInfo> tsInfo(String key) {
      return this.appendCommand(this.commandObjects.tsInfo(key));
   }

   @Override
   public Response<TSInfo> tsInfoDebug(String key) {
      return this.appendCommand(this.commandObjects.tsInfoDebug(key));
   }

   @Override
   public Response<String> bfReserve(String key, double errorRate, long capacity) {
      return this.appendCommand(this.commandObjects.bfReserve(key, errorRate, capacity));
   }

   @Override
   public Response<String> bfReserve(String key, double errorRate, long capacity, BFReserveParams reserveParams) {
      return this.appendCommand(this.commandObjects.bfReserve(key, errorRate, capacity, reserveParams));
   }

   @Override
   public Response<Boolean> bfAdd(String key, String item) {
      return this.appendCommand(this.commandObjects.bfAdd(key, item));
   }

   @Override
   public Response<List<Boolean>> bfMAdd(String key, String... items) {
      return this.appendCommand(this.commandObjects.bfMAdd(key, items));
   }

   @Override
   public Response<List<Boolean>> bfInsert(String key, String... items) {
      return this.appendCommand(this.commandObjects.bfInsert(key, items));
   }

   @Override
   public Response<List<Boolean>> bfInsert(String key, BFInsertParams insertParams, String... items) {
      return this.appendCommand(this.commandObjects.bfInsert(key, insertParams, items));
   }

   @Override
   public Response<Boolean> bfExists(String key, String item) {
      return this.appendCommand(this.commandObjects.bfExists(key, item));
   }

   @Override
   public Response<List<Boolean>> bfMExists(String key, String... items) {
      return this.appendCommand(this.commandObjects.bfMExists(key, items));
   }

   @Override
   public Response<Entry<Long, byte[]>> bfScanDump(String key, long iterator) {
      return this.appendCommand(this.commandObjects.bfScanDump(key, iterator));
   }

   @Override
   public Response<String> bfLoadChunk(String key, long iterator, byte[] data) {
      return this.appendCommand(this.commandObjects.bfLoadChunk(key, iterator, data));
   }

   @Override
   public Response<Long> bfCard(String key) {
      return this.appendCommand(this.commandObjects.bfCard(key));
   }

   @Override
   public Response<Map<String, Object>> bfInfo(String key) {
      return this.appendCommand(this.commandObjects.bfInfo(key));
   }

   @Override
   public Response<String> cfReserve(String key, long capacity) {
      return this.appendCommand(this.commandObjects.cfReserve(key, capacity));
   }

   @Override
   public Response<String> cfReserve(String key, long capacity, CFReserveParams reserveParams) {
      return this.appendCommand(this.commandObjects.cfReserve(key, capacity, reserveParams));
   }

   @Override
   public Response<Boolean> cfAdd(String key, String item) {
      return this.appendCommand(this.commandObjects.cfAdd(key, item));
   }

   @Override
   public Response<Boolean> cfAddNx(String key, String item) {
      return this.appendCommand(this.commandObjects.cfAddNx(key, item));
   }

   @Override
   public Response<List<Boolean>> cfInsert(String key, String... items) {
      return this.appendCommand(this.commandObjects.cfInsert(key, items));
   }

   @Override
   public Response<List<Boolean>> cfInsert(String key, CFInsertParams insertParams, String... items) {
      return this.appendCommand(this.commandObjects.cfInsert(key, insertParams, items));
   }

   @Override
   public Response<List<Boolean>> cfInsertNx(String key, String... items) {
      return this.appendCommand(this.commandObjects.cfInsertNx(key, items));
   }

   @Override
   public Response<List<Boolean>> cfInsertNx(String key, CFInsertParams insertParams, String... items) {
      return this.appendCommand(this.commandObjects.cfInsertNx(key, insertParams, items));
   }

   @Override
   public Response<Boolean> cfExists(String key, String item) {
      return this.appendCommand(this.commandObjects.cfExists(key, item));
   }

   @Override
   public Response<List<Boolean>> cfMExists(String key, String... items) {
      return this.appendCommand(this.commandObjects.cfMExists(key, items));
   }

   @Override
   public Response<Boolean> cfDel(String key, String item) {
      return this.appendCommand(this.commandObjects.cfDel(key, item));
   }

   @Override
   public Response<Long> cfCount(String key, String item) {
      return this.appendCommand(this.commandObjects.cfCount(key, item));
   }

   @Override
   public Response<Entry<Long, byte[]>> cfScanDump(String key, long iterator) {
      return this.appendCommand(this.commandObjects.cfScanDump(key, iterator));
   }

   @Override
   public Response<String> cfLoadChunk(String key, long iterator, byte[] data) {
      return this.appendCommand(this.commandObjects.cfLoadChunk(key, iterator, data));
   }

   @Override
   public Response<Map<String, Object>> cfInfo(String key) {
      return this.appendCommand(this.commandObjects.cfInfo(key));
   }

   @Override
   public Response<String> cmsInitByDim(String key, long width, long depth) {
      return this.appendCommand(this.commandObjects.cmsInitByDim(key, width, depth));
   }

   @Override
   public Response<String> cmsInitByProb(String key, double error, double probability) {
      return this.appendCommand(this.commandObjects.cmsInitByProb(key, error, probability));
   }

   @Override
   public Response<List<Long>> cmsIncrBy(String key, Map<String, Long> itemIncrements) {
      return this.appendCommand(this.commandObjects.cmsIncrBy(key, itemIncrements));
   }

   @Override
   public Response<List<Long>> cmsQuery(String key, String... items) {
      return this.appendCommand(this.commandObjects.cmsQuery(key, items));
   }

   @Override
   public Response<String> cmsMerge(String destKey, String... keys) {
      return this.appendCommand(this.commandObjects.cmsMerge(destKey, keys));
   }

   @Override
   public Response<String> cmsMerge(String destKey, Map<String, Long> keysAndWeights) {
      return this.appendCommand(this.commandObjects.cmsMerge(destKey, keysAndWeights));
   }

   @Override
   public Response<Map<String, Object>> cmsInfo(String key) {
      return this.appendCommand(this.commandObjects.cmsInfo(key));
   }

   @Override
   public Response<String> topkReserve(String key, long topk) {
      return this.appendCommand(this.commandObjects.topkReserve(key, topk));
   }

   @Override
   public Response<String> topkReserve(String key, long topk, long width, long depth, double decay) {
      return this.appendCommand(this.commandObjects.topkReserve(key, topk, width, depth, decay));
   }

   @Override
   public Response<List<String>> topkAdd(String key, String... items) {
      return this.appendCommand(this.commandObjects.topkAdd(key, items));
   }

   @Override
   public Response<List<String>> topkIncrBy(String key, Map<String, Long> itemIncrements) {
      return this.appendCommand(this.commandObjects.topkIncrBy(key, itemIncrements));
   }

   @Override
   public Response<List<Boolean>> topkQuery(String key, String... items) {
      return this.appendCommand(this.commandObjects.topkQuery(key, items));
   }

   @Override
   public Response<List<String>> topkList(String key) {
      return this.appendCommand(this.commandObjects.topkList(key));
   }

   @Override
   public Response<Map<String, Long>> topkListWithCount(String key) {
      return this.appendCommand(this.commandObjects.topkListWithCount(key));
   }

   @Override
   public Response<Map<String, Object>> topkInfo(String key) {
      return this.appendCommand(this.commandObjects.topkInfo(key));
   }

   @Override
   public Response<String> tdigestCreate(String key) {
      return this.appendCommand(this.commandObjects.tdigestCreate(key));
   }

   @Override
   public Response<String> tdigestCreate(String key, int compression) {
      return this.appendCommand(this.commandObjects.tdigestCreate(key, compression));
   }

   @Override
   public Response<String> tdigestReset(String key) {
      return this.appendCommand(this.commandObjects.tdigestReset(key));
   }

   @Override
   public Response<String> tdigestMerge(String destinationKey, String... sourceKeys) {
      return this.appendCommand(this.commandObjects.tdigestMerge(destinationKey, sourceKeys));
   }

   @Override
   public Response<String> tdigestMerge(TDigestMergeParams mergeParams, String destinationKey, String... sourceKeys) {
      return this.appendCommand(this.commandObjects.tdigestMerge(mergeParams, destinationKey, sourceKeys));
   }

   @Override
   public Response<Map<String, Object>> tdigestInfo(String key) {
      return this.appendCommand(this.commandObjects.tdigestInfo(key));
   }

   @Override
   public Response<String> tdigestAdd(String key, double... values) {
      return this.appendCommand(this.commandObjects.tdigestAdd(key, values));
   }

   @Override
   public Response<List<Double>> tdigestCDF(String key, double... values) {
      return this.appendCommand(this.commandObjects.tdigestCDF(key, values));
   }

   @Override
   public Response<List<Double>> tdigestQuantile(String key, double... quantiles) {
      return this.appendCommand(this.commandObjects.tdigestQuantile(key, quantiles));
   }

   @Override
   public Response<Double> tdigestMin(String key) {
      return this.appendCommand(this.commandObjects.tdigestMin(key));
   }

   @Override
   public Response<Double> tdigestMax(String key) {
      return this.appendCommand(this.commandObjects.tdigestMax(key));
   }

   @Override
   public Response<Double> tdigestTrimmedMean(String key, double lowCutQuantile, double highCutQuantile) {
      return this.appendCommand(this.commandObjects.tdigestTrimmedMean(key, lowCutQuantile, highCutQuantile));
   }

   @Override
   public Response<List<Long>> tdigestRank(String key, double... values) {
      return this.appendCommand(this.commandObjects.tdigestRank(key, values));
   }

   @Override
   public Response<List<Long>> tdigestRevRank(String key, double... values) {
      return this.appendCommand(this.commandObjects.tdigestRevRank(key, values));
   }

   @Override
   public Response<List<Double>> tdigestByRank(String key, long... ranks) {
      return this.appendCommand(this.commandObjects.tdigestByRank(key, ranks));
   }

   @Override
   public Response<List<Double>> tdigestByRevRank(String key, long... ranks) {
      return this.appendCommand(this.commandObjects.tdigestByRevRank(key, ranks));
   }

   @Override
   public Response<ResultSet> graphQuery(String name, String query) {
      return this.appendCommand(this.graphCommandObjects.graphQuery(name, query));
   }

   @Override
   public Response<ResultSet> graphReadonlyQuery(String name, String query) {
      return this.appendCommand(this.graphCommandObjects.graphReadonlyQuery(name, query));
   }

   @Override
   public Response<ResultSet> graphQuery(String name, String query, long timeout) {
      return this.appendCommand(this.graphCommandObjects.graphQuery(name, query, timeout));
   }

   @Override
   public Response<ResultSet> graphReadonlyQuery(String name, String query, long timeout) {
      return this.appendCommand(this.graphCommandObjects.graphReadonlyQuery(name, query, timeout));
   }

   @Override
   public Response<ResultSet> graphQuery(String name, String query, Map<String, Object> params) {
      return this.appendCommand(this.graphCommandObjects.graphQuery(name, query, params));
   }

   @Override
   public Response<ResultSet> graphReadonlyQuery(String name, String query, Map<String, Object> params) {
      return this.appendCommand(this.graphCommandObjects.graphReadonlyQuery(name, query, params));
   }

   @Override
   public Response<ResultSet> graphQuery(String name, String query, Map<String, Object> params, long timeout) {
      return this.appendCommand(this.graphCommandObjects.graphQuery(name, query, params, timeout));
   }

   @Override
   public Response<ResultSet> graphReadonlyQuery(String name, String query, Map<String, Object> params, long timeout) {
      return this.appendCommand(this.graphCommandObjects.graphReadonlyQuery(name, query, params, timeout));
   }

   @Override
   public Response<String> graphDelete(String name) {
      return this.appendCommand(this.graphCommandObjects.graphDelete(name));
   }

   @Override
   public Response<List<String>> graphProfile(String graphName, String query) {
      return this.appendCommand(this.commandObjects.graphProfile(graphName, query));
   }

   public Response<Object> sendCommand(ProtocolCommand cmd, String... args) {
      return this.sendCommand(new CommandArguments(cmd).addObjects(args));
   }

   public Response<Object> sendCommand(ProtocolCommand cmd, byte[]... args) {
      return this.sendCommand(new CommandArguments(cmd).addObjects((Object[])args));
   }

   public Response<Object> sendCommand(CommandArguments args) {
      return this.executeCommand(new CommandObject<>(args, BuilderFactory.RAW_OBJECT));
   }

   public <T> Response<T> executeCommand(CommandObject<T> command) {
      return this.appendCommand(command);
   }

   public void setJsonObjectMapper(JsonObjectMapper jsonObjectMapper) {
      this.commandObjects.setJsonObjectMapper(jsonObjectMapper);
   }
}
