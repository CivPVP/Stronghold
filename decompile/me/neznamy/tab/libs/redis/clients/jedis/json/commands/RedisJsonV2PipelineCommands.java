package me.neznamy.tab.libs.redis.clients.jedis.json.commands;

import java.util.List;
import me.neznamy.tab.libs.org.json.JSONArray;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.json.JsonSetParams;
import me.neznamy.tab.libs.redis.clients.jedis.json.Path2;

public interface RedisJsonV2PipelineCommands {
   default Response<String> jsonSet(String key, Object object) {
      return this.jsonSet(key, Path2.ROOT_PATH, object);
   }

   default Response<String> jsonSetWithEscape(String key, Object object) {
      return this.jsonSetWithEscape(key, Path2.ROOT_PATH, object);
   }

   default Response<String> jsonSet(String key, Object object, JsonSetParams params) {
      return this.jsonSet(key, Path2.ROOT_PATH, object, params);
   }

   default Response<String> jsonSetWithEscape(String key, Object object, JsonSetParams params) {
      return this.jsonSetWithEscape(key, Path2.ROOT_PATH, object, params);
   }

   Response<String> jsonSet(String var1, Path2 var2, Object var3);

   Response<String> jsonSetWithEscape(String var1, Path2 var2, Object var3);

   Response<String> jsonSet(String var1, Path2 var2, Object var3, JsonSetParams var4);

   Response<String> jsonSetWithEscape(String var1, Path2 var2, Object var3, JsonSetParams var4);

   Response<String> jsonMerge(String var1, Path2 var2, Object var3);

   Response<Object> jsonGet(String var1);

   Response<Object> jsonGet(String var1, Path2... var2);

   default Response<List<JSONArray>> jsonMGet(String... keys) {
      return this.jsonMGet(Path2.ROOT_PATH, keys);
   }

   Response<List<JSONArray>> jsonMGet(Path2 var1, String... var2);

   Response<Long> jsonDel(String var1);

   Response<Long> jsonDel(String var1, Path2 var2);

   Response<Long> jsonClear(String var1);

   Response<Long> jsonClear(String var1, Path2 var2);

   Response<List<Boolean>> jsonToggle(String var1, Path2 var2);

   Response<List<Class<?>>> jsonType(String var1, Path2 var2);

   Response<List<Long>> jsonStrAppend(String var1, Path2 var2, Object var3);

   Response<List<Long>> jsonStrLen(String var1, Path2 var2);

   Response<Object> jsonNumIncrBy(String var1, Path2 var2, double var3);

   Response<List<Long>> jsonArrAppend(String var1, Path2 var2, Object... var3);

   Response<List<Long>> jsonArrAppendWithEscape(String var1, Path2 var2, Object... var3);

   Response<List<Long>> jsonArrIndex(String var1, Path2 var2, Object var3);

   Response<List<Long>> jsonArrIndexWithEscape(String var1, Path2 var2, Object var3);

   Response<List<Long>> jsonArrInsert(String var1, Path2 var2, int var3, Object... var4);

   Response<List<Long>> jsonArrInsertWithEscape(String var1, Path2 var2, int var3, Object... var4);

   Response<List<Object>> jsonArrPop(String var1, Path2 var2);

   Response<List<Object>> jsonArrPop(String var1, Path2 var2, int var3);

   Response<List<Long>> jsonArrLen(String var1, Path2 var2);

   Response<List<Long>> jsonArrTrim(String var1, Path2 var2, int var3, int var4);
}
