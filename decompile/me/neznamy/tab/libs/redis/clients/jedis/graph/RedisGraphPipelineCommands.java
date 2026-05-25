package me.neznamy.tab.libs.redis.clients.jedis.graph;

import java.util.List;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.Response;

@Deprecated
public interface RedisGraphPipelineCommands {
   @Deprecated
   Response<ResultSet> graphQuery(String var1, String var2);

   @Deprecated
   Response<ResultSet> graphReadonlyQuery(String var1, String var2);

   @Deprecated
   Response<ResultSet> graphQuery(String var1, String var2, long var3);

   @Deprecated
   Response<ResultSet> graphReadonlyQuery(String var1, String var2, long var3);

   @Deprecated
   Response<ResultSet> graphQuery(String var1, String var2, Map<String, Object> var3);

   @Deprecated
   Response<ResultSet> graphReadonlyQuery(String var1, String var2, Map<String, Object> var3);

   @Deprecated
   Response<ResultSet> graphQuery(String var1, String var2, Map<String, Object> var3, long var4);

   @Deprecated
   Response<ResultSet> graphReadonlyQuery(String var1, String var2, Map<String, Object> var3, long var4);

   @Deprecated
   Response<String> graphDelete(String var1);

   @Deprecated
   Response<List<String>> graphProfile(String var1, String var2);
}
