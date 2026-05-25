package me.neznamy.tab.libs.redis.clients.jedis.graph;

import java.util.List;

@Deprecated
public interface Record {
   <T> T getValue(int var1);

   <T> T getValue(String var1);

   String getString(int var1);

   String getString(String var1);

   List<String> keys();

   List<Object> values();

   boolean containsKey(String var1);

   int size();
}
