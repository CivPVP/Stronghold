package me.neznamy.tab.libs.redis.clients.jedis.search;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.resps.Tuple;
import me.neznamy.tab.libs.redis.clients.jedis.search.aggr.AggregationBuilder;
import me.neznamy.tab.libs.redis.clients.jedis.search.aggr.AggregationResult;
import me.neznamy.tab.libs.redis.clients.jedis.search.schemafields.SchemaField;

public interface RediSearchPipelineCommands {
   Response<String> ftCreate(String var1, IndexOptions var2, Schema var3);

   default Response<String> ftCreate(String indexName, SchemaField... schemaFields) {
      return this.ftCreate(indexName, Arrays.asList(schemaFields));
   }

   default Response<String> ftCreate(String indexName, FTCreateParams createParams, SchemaField... schemaFields) {
      return this.ftCreate(indexName, createParams, Arrays.asList(schemaFields));
   }

   default Response<String> ftCreate(String indexName, Iterable<SchemaField> schemaFields) {
      return this.ftCreate(indexName, FTCreateParams.createParams(), schemaFields);
   }

   Response<String> ftCreate(String var1, FTCreateParams var2, Iterable<SchemaField> var3);

   default Response<String> ftAlter(String indexName, Schema.Field... fields) {
      return this.ftAlter(indexName, Schema.from(fields));
   }

   Response<String> ftAlter(String var1, Schema var2);

   default Response<String> ftAlter(String indexName, SchemaField... schemaFields) {
      return this.ftAlter(indexName, Arrays.asList(schemaFields));
   }

   Response<String> ftAlter(String var1, Iterable<SchemaField> var2);

   Response<String> ftAliasAdd(String var1, String var2);

   Response<String> ftAliasUpdate(String var1, String var2);

   Response<String> ftAliasDel(String var1);

   Response<String> ftDropIndex(String var1);

   Response<String> ftDropIndexDD(String var1);

   default Response<SearchResult> ftSearch(String indexName) {
      return this.ftSearch(indexName, "*");
   }

   Response<SearchResult> ftSearch(String var1, String var2);

   Response<SearchResult> ftSearch(String var1, String var2, FTSearchParams var3);

   Response<SearchResult> ftSearch(String var1, Query var2);

   @Deprecated
   Response<SearchResult> ftSearch(byte[] var1, Query var2);

   Response<String> ftExplain(String var1, Query var2);

   Response<List<String>> ftExplainCLI(String var1, Query var2);

   Response<AggregationResult> ftAggregate(String var1, AggregationBuilder var2);

   Response<String> ftSynUpdate(String var1, String var2, String... var3);

   Response<Map<String, List<String>>> ftSynDump(String var1);

   Response<Long> ftDictAdd(String var1, String... var2);

   Response<Long> ftDictDel(String var1, String... var2);

   Response<Set<String>> ftDictDump(String var1);

   Response<Long> ftDictAddBySampleKey(String var1, String var2, String... var3);

   Response<Long> ftDictDelBySampleKey(String var1, String var2, String... var3);

   Response<Set<String>> ftDictDumpBySampleKey(String var1, String var2);

   Response<Map<String, Map<String, Double>>> ftSpellCheck(String var1, String var2);

   Response<Map<String, Map<String, Double>>> ftSpellCheck(String var1, String var2, FTSpellCheckParams var3);

   Response<Map<String, Object>> ftInfo(String var1);

   Response<Set<String>> ftTagVals(String var1, String var2);

   Response<Map<String, Object>> ftConfigGet(String var1);

   Response<Map<String, Object>> ftConfigGet(String var1, String var2);

   Response<String> ftConfigSet(String var1, String var2);

   Response<String> ftConfigSet(String var1, String var2, String var3);

   Response<Long> ftSugAdd(String var1, String var2, double var3);

   Response<Long> ftSugAddIncr(String var1, String var2, double var3);

   Response<List<String>> ftSugGet(String var1, String var2);

   Response<List<String>> ftSugGet(String var1, String var2, boolean var3, int var4);

   Response<List<Tuple>> ftSugGetWithScores(String var1, String var2);

   Response<List<Tuple>> ftSugGetWithScores(String var1, String var2, boolean var3, int var4);

   Response<Boolean> ftSugDel(String var1, String var2);

   Response<Long> ftSugLen(String var1);
}
