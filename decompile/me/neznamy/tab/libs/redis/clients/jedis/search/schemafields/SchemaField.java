package me.neznamy.tab.libs.redis.clients.jedis.search.schemafields;

import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;
import me.neznamy.tab.libs.redis.clients.jedis.search.FieldName;

public abstract class SchemaField implements IParams {
   protected final FieldName fieldName;

   public SchemaField(String fieldName) {
      this.fieldName = new FieldName(fieldName);
   }

   public SchemaField(FieldName fieldName) {
      this.fieldName = fieldName;
   }

   public SchemaField as(String attribute) {
      this.fieldName.as(attribute);
      return this;
   }

   public final FieldName getFieldName() {
      return this.fieldName;
   }

   public final String getName() {
      return this.fieldName.getName();
   }
}
