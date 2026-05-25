package me.neznamy.tab.libs.redis.clients.jedis.graph;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Deprecated
public class RedisGraphQueryUtil {
   public static final List<String> DUMMY_LIST = Collections.emptyList();
   public static final Map<String, List<String>> DUMMY_MAP = Collections.emptyMap();
   public static final String COMPACT_STRING = "--COMPACT";
   public static final String TIMEOUT_STRING = "TIMEOUT";

   private RedisGraphQueryUtil() {
   }

   @Deprecated
   public static String prepareQuery(String query, Map<String, Object> params) {
      StringBuilder sb = new StringBuilder("CYPHER ");

      for (Entry<String, Object> entry : params.entrySet()) {
         sb.append(entry.getKey()).append('=').append(valueToString(entry.getValue())).append(' ');
      }

      sb.append(query);
      return sb.toString();
   }

   private static String valueToString(Object value) {
      if (value == null) {
         return "null";
      } else if (value instanceof String) {
         return quoteString((String)value);
      } else if (value instanceof Character) {
         return quoteString(((Character)value).toString());
      } else if (value instanceof Object[]) {
         return arrayToString((Object[])value);
      } else {
         return value instanceof List ? arrayToString((List<Object>)value) : value.toString();
      }
   }

   private static String quoteString(String str) {
      StringBuilder sb = new StringBuilder(str.length() + 12);
      sb.append('"');
      sb.append(str.replace("\"", "\\\""));
      sb.append('"');
      return sb.toString();
   }

   private static String arrayToString(Object[] arr) {
      return arrayToString(Arrays.asList(arr));
   }

   private static String arrayToString(List<Object> arr) {
      StringBuilder sb = new StringBuilder().append('[');
      sb.append(String.join(", ", arr.stream().map(RedisGraphQueryUtil::valueToString).collect(Collectors.toList())));
      sb.append(']');
      return sb.toString();
   }
}
