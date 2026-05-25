package me.neznamy.tab.libs.redis.clients.jedis.json;

public interface JsonObjectMapper {
   <T> T fromJson(String var1, Class<T> var2);

   String toJson(Object var1);
}
