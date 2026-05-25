package me.neznamy.tab.libs.redis.clients.jedis.json;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import me.neznamy.tab.libs.org.json.JSONArray;
import me.neznamy.tab.libs.org.json.JSONException;
import me.neznamy.tab.libs.org.json.JSONObject;
import me.neznamy.tab.libs.redis.clients.jedis.Builder;
import me.neznamy.tab.libs.redis.clients.jedis.BuilderFactory;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;

public final class JsonBuilderFactory {
   public static final Builder<Class<?>> JSON_TYPE = new Builder<Class<?>>() {
      public Class<?> build(Object data) {
         if (data == null) {
            return null;
         }

         String str = BuilderFactory.STRING.build(data);
         switch (str) {
            case "null":
               return null;
            case "boolean":
               return boolean.class;
            case "integer":
               return int.class;
            case "number":
               return float.class;
            case "string":
               return String.class;
            case "object":
               return Object.class;
            case "array":
               return List.class;
            default:
               throw new JedisException("Unknown type: " + str);
         }
      }

      @Override
      public String toString() {
         return "Class<?>";
      }
   };
   public static final Builder<List<Class<?>>> JSON_TYPE_LIST = new Builder<List<Class<?>>>() {
      public List<Class<?>> build(Object data) {
         List<Object> list = (List<Object>)data;
         List<Class<?>> classes = new ArrayList<>(list.size());

         for (Object elem : list) {
            try {
               classes.add(JsonBuilderFactory.JSON_TYPE.build(elem));
            } catch (JedisException je) {
               classes.add(null);
            }
         }

         return classes;
      }
   };
   public static final Builder<List<List<Class<?>>>> JSON_TYPE_RESPONSE_RESP3 = new Builder<List<List<Class<?>>>>() {
      public List<List<Class<?>>> build(Object data) {
         return ((List)data).stream().map(JsonBuilderFactory.JSON_TYPE_LIST::build).collect(Collectors.toList());
      }
   };
   public static final Builder<List<Class<?>>> JSON_TYPE_RESPONSE_RESP3_COMPATIBLE = new Builder<List<Class<?>>>() {
      public List<Class<?>> build(Object data) {
         List<List<Class<?>>> fullReply = JsonBuilderFactory.JSON_TYPE_RESPONSE_RESP3.build(data);
         return fullReply == null ? null : fullReply.get(0);
      }
   };
   public static final Builder<Object> JSON_OBJECT = new Builder<Object>() {
      @Override
      public Object build(Object data) {
         if (data == null) {
            return null;
         }

         if (!(data instanceof byte[])) {
            return data;
         }

         String str = BuilderFactory.STRING.build(data);
         if (str.charAt(0) == '{') {
            try {
               return new JSONObject(str);
            } catch (Exception var5) {
            }
         } else if (str.charAt(0) == '[') {
            try {
               return new JSONArray(str);
            } catch (Exception var4) {
            }
         }

         return str;
      }
   };
   public static final Builder<JSONArray> JSON_ARRAY = new Builder<JSONArray>() {
      public JSONArray build(Object data) {
         if (data == null) {
            return null;
         }

         String str = BuilderFactory.STRING.build(data);

         try {
            return new JSONArray(str);
         } catch (JSONException ex) {
            throw new JedisException(ex);
         }
      }
   };
   public static final Builder<Object> JSON_ARRAY_OR_DOUBLE_LIST = new Builder<Object>() {
      @Override
      public Object build(Object data) {
         if (data == null) {
            return null;
         } else {
            return data instanceof List ? BuilderFactory.DOUBLE_LIST.build(data) : JsonBuilderFactory.JSON_ARRAY.build(data);
         }
      }
   };
   public static final Builder<List<JSONArray>> JSON_ARRAY_LIST = new Builder<List<JSONArray>>() {
      public List<JSONArray> build(Object data) {
         if (data == null) {
            return null;
         }

         List<Object> list = (List<Object>)data;
         return list.stream().map(o -> JsonBuilderFactory.JSON_ARRAY.build(o)).collect(Collectors.toList());
      }
   };

   private JsonBuilderFactory() {
      throw new InstantiationError("Must not instantiate this class");
   }
}
