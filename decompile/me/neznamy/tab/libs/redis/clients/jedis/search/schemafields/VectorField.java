package me.neznamy.tab.libs.redis.clients.jedis.search.schemafields;

import java.util.LinkedHashMap;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.search.FieldName;
import me.neznamy.tab.libs.redis.clients.jedis.search.SearchProtocol;

public class VectorField extends SchemaField {
   private final VectorField.VectorAlgorithm algorithm;
   private final Map<String, Object> attributes;
   private boolean indexMissing;

   public VectorField(String fieldName, VectorField.VectorAlgorithm algorithm, Map<String, Object> attributes) {
      super(fieldName);
      this.algorithm = algorithm;
      this.attributes = attributes;
   }

   public VectorField(FieldName fieldName, VectorField.VectorAlgorithm algorithm, Map<String, Object> attributes) {
      super(fieldName);
      this.algorithm = algorithm;
      this.attributes = attributes;
   }

   public VectorField as(String attribute) {
      super.as(attribute);
      return this;
   }

   public VectorField indexMissing() {
      this.indexMissing = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      args.addParams(this.fieldName);
      args.add(SearchProtocol.SearchKeyword.VECTOR);
      args.add(this.algorithm);
      args.add(this.attributes.size() << 1);
      this.attributes.forEach((name, value) -> args.add(name).add(value));
      if (this.indexMissing) {
         args.add(SearchProtocol.SearchKeyword.INDEXMISSING);
      }
   }

   public static VectorField.Builder builder() {
      return new VectorField.Builder();
   }

   public static class Builder {
      private FieldName fieldName;
      private VectorField.VectorAlgorithm algorithm;
      private Map<String, Object> attributes;

      private Builder() {
      }

      public VectorField build() {
         if (this.fieldName != null && this.algorithm != null && this.attributes != null && !this.attributes.isEmpty()) {
            return new VectorField(this.fieldName, this.algorithm, this.attributes);
         } else {
            throw new IllegalArgumentException("All required VectorField parameters are not set.");
         }
      }

      public VectorField.Builder fieldName(String fieldName) {
         this.fieldName = FieldName.of(fieldName);
         return this;
      }

      public VectorField.Builder fieldName(FieldName fieldName) {
         this.fieldName = fieldName;
         return this;
      }

      public VectorField.Builder as(String attribute) {
         this.fieldName.as(attribute);
         return this;
      }

      public VectorField.Builder algorithm(VectorField.VectorAlgorithm algorithm) {
         this.algorithm = algorithm;
         return this;
      }

      public VectorField.Builder attributes(Map<String, Object> attributes) {
         this.attributes = attributes;
         return this;
      }

      public VectorField.Builder addAttribute(String name, Object value) {
         if (this.attributes == null) {
            this.attributes = new LinkedHashMap<>();
         }

         this.attributes.put(name, value);
         return this;
      }
   }

   public enum VectorAlgorithm {
      FLAT,
      HNSW;
   }
}
