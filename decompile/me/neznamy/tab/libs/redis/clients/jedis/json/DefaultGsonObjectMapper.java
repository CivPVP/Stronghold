package me.neznamy.tab.libs.redis.clients.jedis.json;

import com.google.gson.Gson;

public class DefaultGsonObjectMapper implements JsonObjectMapper {
   private final Gson gson = new Gson();

   @Override
   public <T> T fromJson(String value, Class<T> valueType) {
      return (T)this.gson.fromJson(value, valueType);
   }

   @Override
   public String toJson(Object value) {
      return this.gson.toJson(value);
   }
}
