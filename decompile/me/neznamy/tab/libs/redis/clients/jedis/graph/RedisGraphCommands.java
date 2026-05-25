package me.neznamy.tab.libs.redis.clients.jedis.graph;

import java.util.List;
import java.util.Map;

@Deprecated
public interface RedisGraphCommands {
   @Deprecated
   ResultSet graphQuery(String var1, String var2);

   @Deprecated
   ResultSet graphReadonlyQuery(String var1, String var2);

   @Deprecated
   ResultSet graphQuery(String var1, String var2, long var3);

   @Deprecated
   ResultSet graphReadonlyQuery(String var1, String var2, long var3);

   @Deprecated
   ResultSet graphQuery(String var1, String var2, Map<String, Object> var3);

   @Deprecated
   ResultSet graphReadonlyQuery(String var1, String var2, Map<String, Object> var3);

   @Deprecated
   ResultSet graphQuery(String var1, String var2, Map<String, Object> var3, long var4);

   @Deprecated
   ResultSet graphReadonlyQuery(String var1, String var2, Map<String, Object> var3, long var4);

   @Deprecated
   String graphDelete(String var1);

   @Deprecated
   List<String> graphList();

   @Deprecated
   List<String> graphProfile(String var1, String var2);

   @Deprecated
   List<String> graphExplain(String var1, String var2);

   @Deprecated
   List<List<Object>> graphSlowlog(String var1);

   @Deprecated
   String graphConfigSet(String var1, Object var2);

   @Deprecated
   Map<String, Object> graphConfigGet(String var1);
}
