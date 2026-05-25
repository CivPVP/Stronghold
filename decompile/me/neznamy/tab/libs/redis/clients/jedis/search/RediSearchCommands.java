package me.neznamy.tab.libs.redis.clients.jedis.search;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import me.neznamy.tab.libs.redis.clients.jedis.resps.Tuple;
import me.neznamy.tab.libs.redis.clients.jedis.search.aggr.AggregationBuilder;
import me.neznamy.tab.libs.redis.clients.jedis.search.aggr.AggregationResult;
import me.neznamy.tab.libs.redis.clients.jedis.search.schemafields.SchemaField;

public interface RediSearchCommands {
   String ftCreate(String var1, IndexOptions var2, Schema var3);

   default String ftCreate(String indexName, SchemaField... schemaFields) {
      return this.ftCreate(indexName, Arrays.asList(schemaFields));
   }

   default String ftCreate(String indexName, FTCreateParams createParams, SchemaField... schemaFields) {
      return this.ftCreate(indexName, createParams, Arrays.asList(schemaFields));
   }

   default String ftCreate(String indexName, Iterable<SchemaField> schemaFields) {
      return this.ftCreate(indexName, FTCreateParams.createParams(), schemaFields);
   }

   String ftCreate(String var1, FTCreateParams var2, Iterable<SchemaField> var3);

   default String ftAlter(String indexName, Schema.Field... fields) {
      return this.ftAlter(indexName, Schema.from(fields));
   }

   String ftAlter(String var1, Schema var2);

   default String ftAlter(String indexName, SchemaField... schemaFields) {
      return this.ftAlter(indexName, Arrays.asList(schemaFields));
   }

   String ftAlter(String var1, Iterable<SchemaField> var2);

   String ftAliasAdd(String var1, String var2);

   String ftAliasUpdate(String var1, String var2);

   String ftAliasDel(String var1);

   String ftDropIndex(String var1);

   String ftDropIndexDD(String var1);

   default SearchResult ftSearch(String indexName) {
      return this.ftSearch(indexName, "*");
   }

   SearchResult ftSearch(String var1, String var2);

   SearchResult ftSearch(String var1, String var2, FTSearchParams var3);

   SearchResult ftSearch(String var1, Query var2);

   @Deprecated
   SearchResult ftSearch(byte[] var1, Query var2);

   String ftExplain(String var1, Query var2);

   List<String> ftExplainCLI(String var1, Query var2);

   AggregationResult ftAggregate(String var1, AggregationBuilder var2);

   AggregationResult ftCursorRead(String var1, long var2, int var4);

   String ftCursorDel(String var1, long var2);

   Entry<AggregationResult, Map<String, Object>> ftProfileAggregate(String var1, FTProfileParams var2, AggregationBuilder var3);

   Entry<SearchResult, Map<String, Object>> ftProfileSearch(String var1, FTProfileParams var2, Query var3);

   Entry<SearchResult, Map<String, Object>> ftProfileSearch(String var1, FTProfileParams var2, String var3, FTSearchParams var4);

   String ftSynUpdate(String var1, String var2, String... var3);

   Map<String, List<String>> ftSynDump(String var1);

   long ftDictAdd(String var1, String... var2);

   long ftDictDel(String var1, String... var2);

   Set<String> ftDictDump(String var1);

   long ftDictAddBySampleKey(String var1, String var2, String... var3);

   long ftDictDelBySampleKey(String var1, String var2, String... var3);

   Set<String> ftDictDumpBySampleKey(String var1, String var2);

   Map<String, Map<String, Double>> ftSpellCheck(String var1, String var2);

   Map<String, Map<String, Double>> ftSpellCheck(String var1, String var2, FTSpellCheckParams var3);

   Map<String, Object> ftInfo(String var1);

   Set<String> ftTagVals(String var1, String var2);

   Map<String, Object> ftConfigGet(String var1);

   Map<String, Object> ftConfigGet(String var1, String var2);

   String ftConfigSet(String var1, String var2);

   String ftConfigSet(String var1, String var2, String var3);

   long ftSugAdd(String var1, String var2, double var3);

   long ftSugAddIncr(String var1, String var2, double var3);

   List<String> ftSugGet(String var1, String var2);

   List<String> ftSugGet(String var1, String var2, boolean var3, int var4);

   List<Tuple> ftSugGetWithScores(String var1, String var2);

   List<Tuple> ftSugGetWithScores(String var1, String var2, boolean var3, int var4);

   boolean ftSugDel(String var1, String var2);

   long ftSugLen(String var1);

   Set<String> ftList();
}
