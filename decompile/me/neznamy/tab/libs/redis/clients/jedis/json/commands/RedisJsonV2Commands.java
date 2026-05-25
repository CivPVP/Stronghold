package me.neznamy.tab.libs.redis.clients.jedis.json.commands;

import java.util.List;
import me.neznamy.tab.libs.org.json.JSONArray;
import me.neznamy.tab.libs.redis.clients.jedis.json.JsonSetParams;
import me.neznamy.tab.libs.redis.clients.jedis.json.Path2;

public interface RedisJsonV2Commands {
   default String jsonSet(String key, Object object) {
      return this.jsonSet(key, Path2.ROOT_PATH, object);
   }

   default String jsonSetWithEscape(String key, Object object) {
      return this.jsonSetWithEscape(key, Path2.ROOT_PATH, object);
   }

   default String jsonSet(String key, Object object, JsonSetParams params) {
      return this.jsonSet(key, Path2.ROOT_PATH, object, params);
   }

   default String jsonSetWithEscape(String key, Object object, JsonSetParams params) {
      return this.jsonSetWithEscape(key, Path2.ROOT_PATH, object, params);
   }

   String jsonSet(String var1, Path2 var2, Object var3);

   String jsonSetWithEscape(String var1, Path2 var2, Object var3);

   String jsonSet(String var1, Path2 var2, Object var3, JsonSetParams var4);

   String jsonSetWithEscape(String var1, Path2 var2, Object var3, JsonSetParams var4);

   String jsonMerge(String var1, Path2 var2, Object var3);

   Object jsonGet(String var1);

   Object jsonGet(String var1, Path2... var2);

   default List<JSONArray> jsonMGet(String... keys) {
      return this.jsonMGet(Path2.ROOT_PATH, keys);
   }

   List<JSONArray> jsonMGet(Path2 var1, String... var2);

   long jsonDel(String var1);

   long jsonDel(String var1, Path2 var2);

   long jsonClear(String var1);

   long jsonClear(String var1, Path2 var2);

   List<Boolean> jsonToggle(String var1, Path2 var2);

   List<Class<?>> jsonType(String var1, Path2 var2);

   List<Long> jsonStrAppend(String var1, Path2 var2, Object var3);

   List<Long> jsonStrLen(String var1, Path2 var2);

   Object jsonNumIncrBy(String var1, Path2 var2, double var3);

   List<Long> jsonArrAppend(String var1, Path2 var2, Object... var3);

   List<Long> jsonArrAppendWithEscape(String var1, Path2 var2, Object... var3);

   List<Long> jsonArrIndex(String var1, Path2 var2, Object var3);

   List<Long> jsonArrIndexWithEscape(String var1, Path2 var2, Object var3);

   List<Long> jsonArrInsert(String var1, Path2 var2, int var3, Object... var4);

   List<Long> jsonArrInsertWithEscape(String var1, Path2 var2, int var3, Object... var4);

   List<Object> jsonArrPop(String var1, Path2 var2);

   List<Object> jsonArrPop(String var1, Path2 var2, int var3);

   List<Long> jsonArrLen(String var1, Path2 var2);

   List<Long> jsonArrTrim(String var1, Path2 var2, int var3, int var4);

   List<Long> jsonObjLen(String var1, Path2 var2);

   List<List<String>> jsonObjKeys(String var1, Path2 var2);

   List<Long> jsonDebugMemory(String var1, Path2 var2);
}
