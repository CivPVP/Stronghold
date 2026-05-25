package me.neznamy.tab.libs.redis.clients.jedis.search.aggr;

import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.util.DoublePrecision;

public class Row {
   private final Map<String, Object> fields;

   public Row(Map<String, Object> fields) {
      this.fields = fields;
   }

   public boolean containsKey(String key) {
      return this.fields.containsKey(key);
   }

   public Object get(String key) {
      return this.fields.get(key);
   }

   public String getString(String key) {
      return !this.containsKey(key) ? "" : (String)this.fields.get(key);
   }

   public long getLong(String key) {
      return !this.containsKey(key) ? 0L : Long.parseLong((String)this.fields.get(key));
   }

   public double getDouble(String key) {
      return !this.containsKey(key) ? 0.0 : DoublePrecision.parseFloatingPointNumber((String)this.fields.get(key));
   }

   @Override
   public String toString() {
      return String.valueOf(this.fields);
   }
}
