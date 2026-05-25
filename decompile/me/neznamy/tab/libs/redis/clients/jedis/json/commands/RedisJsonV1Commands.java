package me.neznamy.tab.libs.redis.clients.jedis.json.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.json.JsonSetParams;
import me.neznamy.tab.libs.redis.clients.jedis.json.Path;

@Deprecated
public interface RedisJsonV1Commands {
   @Deprecated
   default String jsonSetLegacy(String key, Object pojo) {
      return this.jsonSet(key, Path.ROOT_PATH, pojo);
   }

   @Deprecated
   default String jsonSetLegacy(String key, Object pojo, JsonSetParams params) {
      return this.jsonSet(key, Path.ROOT_PATH, pojo, params);
   }

   @Deprecated
   String jsonSet(String var1, Path var2, Object var3);

   @Deprecated
   String jsonSetWithPlainString(String var1, Path var2, String var3);

   @Deprecated
   String jsonSet(String var1, Path var2, Object var3, JsonSetParams var4);

   @Deprecated
   String jsonMerge(String var1, Path var2, Object var3);

   Object jsonGet(String var1);

   @Deprecated
   <T> T jsonGet(String var1, Class<T> var2);

   @Deprecated
   Object jsonGet(String var1, Path... var2);

   @Deprecated
   String jsonGetAsPlainString(String var1, Path var2);

   @Deprecated
   <T> T jsonGet(String var1, Class<T> var2, Path... var3);

   @Deprecated
   default <T> List<T> jsonMGet(Class<T> clazz, String... keys) {
      return this.jsonMGet(Path.ROOT_PATH, clazz, keys);
   }

   @Deprecated
   <T> List<T> jsonMGet(Path var1, Class<T> var2, String... var3);

   long jsonDel(String var1);

   @Deprecated
   long jsonDel(String var1, Path var2);

   long jsonClear(String var1);

   @Deprecated
   long jsonClear(String var1, Path var2);

   @Deprecated
   String jsonToggle(String var1, Path var2);

   @Deprecated
   Class<?> jsonType(String var1);

   @Deprecated
   Class<?> jsonType(String var1, Path var2);

   @Deprecated
   long jsonStrAppend(String var1, Object var2);

   @Deprecated
   long jsonStrAppend(String var1, Path var2, Object var3);

   @Deprecated
   Long jsonStrLen(String var1);

   @Deprecated
   Long jsonStrLen(String var1, Path var2);

   @Deprecated
   double jsonNumIncrBy(String var1, Path var2, double var3);

   @Deprecated
   Long jsonArrAppend(String var1, Path var2, Object... var3);

   @Deprecated
   long jsonArrIndex(String var1, Path var2, Object var3);

   @Deprecated
   long jsonArrInsert(String var1, Path var2, int var3, Object... var4);

   @Deprecated
   Object jsonArrPop(String var1);

   @Deprecated
   <T> T jsonArrPop(String var1, Class<T> var2);

   @Deprecated
   Object jsonArrPop(String var1, Path var2);

   @Deprecated
   <T> T jsonArrPop(String var1, Class<T> var2, Path var3);

   @Deprecated
   Object jsonArrPop(String var1, Path var2, int var3);

   @Deprecated
   <T> T jsonArrPop(String var1, Class<T> var2, Path var3, int var4);

   @Deprecated
   Long jsonArrLen(String var1);

   @Deprecated
   Long jsonArrLen(String var1, Path var2);

   @Deprecated
   Long jsonArrTrim(String var1, Path var2, int var3, int var4);

   @Deprecated
   Long jsonObjLen(String var1);

   @Deprecated
   Long jsonObjLen(String var1, Path var2);

   @Deprecated
   List<String> jsonObjKeys(String var1);

   @Deprecated
   List<String> jsonObjKeys(String var1, Path var2);

   @Deprecated
   long jsonDebugMemory(String var1);

   @Deprecated
   long jsonDebugMemory(String var1, Path var2);
}
