package me.neznamy.tab.libs.redis.clients.jedis.search;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.neznamy.tab.libs.redis.clients.jedis.Builder;
import me.neznamy.tab.libs.redis.clients.jedis.BuilderFactory;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public class Document implements Serializable {
   private static final long serialVersionUID = 4884173545291367373L;
   private final String id;
   private Double score;
   private final Map<String, Object> fields;
   static Builder<Document> SEARCH_DOCUMENT = new Document.PerFieldDecoderDocumentBuilder((Map<String, Boolean>)null);

   public Document(String id) {
      this(id, 1.0);
   }

   public Document(String id, double score) {
      this(id, new HashMap<>(), score);
   }

   public Document(String id, Map<String, Object> fields) {
      this(id, fields, 1.0);
   }

   public Document(String id, Map<String, Object> fields, double score) {
      this.id = id;
      this.fields = fields;
      this.score = score;
   }

   private Document(String id, Double score, Map<String, Object> fields) {
      this.id = id;
      this.score = score;
      this.fields = fields;
   }

   public Iterable<Entry<String, Object>> getProperties() {
      return this.fields.entrySet();
   }

   public String getId() {
      return this.id;
   }

   public Double getScore() {
      return this.score;
   }

   public Object get(String key) {
      return this.fields.get(key);
   }

   public String getString(String key) {
      Object value = this.fields.get(key);
      if (value == null) {
         return null;
      } else if (value instanceof String) {
         return (String)value;
      } else {
         return value instanceof byte[] ? SafeEncoder.encode((byte[])value) : String.valueOf(value);
      }
   }

   public boolean hasProperty(String key) {
      return this.fields.containsKey(key);
   }

   public Document set(String key, Object value) {
      this.fields.put(key, value);
      return this;
   }

   @Deprecated
   public Document setScore(float score) {
      this.score = (double)score;
      return this;
   }

   @Override
   public String toString() {
      return "id:" + this.getId() + ", score: " + this.getScore() + ", properties:" + this.getProperties();
   }

   public static Document load(String id, double score, byte[] payload, List<byte[]> fields) {
      return load(id, score, fields, true);
   }

   public static Document load(String id, double score, List<byte[]> fields, boolean decode) {
      return load(id, score, fields, decode, null);
   }

   public static Document load(String id, double score, List<byte[]> fields, boolean decode, Map<String, Boolean> isFieldDecode) {
      Document ret = new Document(id, score);
      if (fields != null) {
         for (int i = 0; i < fields.size(); i += 2) {
            byte[] rawKey = fields.get(i);
            byte[] rawValue = fields.get(i + 1);
            String key = SafeEncoder.encode(rawKey);
            Object value = rawValue == null
               ? null
               : (!decode || isFieldDecode != null && Boolean.FALSE.equals(isFieldDecode.get(key)) ? rawValue : SafeEncoder.encode(rawValue));
            ret.set(key, value);
         }
      }

      return ret;
   }

   private static Map<String, Object> makeFieldsMap(Map<String, Boolean> isDecode, Object data) {
      if (data == null) {
         return null;
      }

      List<KeyValue> list = (List<KeyValue>)data;
      Map<String, Object> map = new HashMap<>(list.size(), 1.0F);
      list.stream().filter(kv -> kv != null && kv.getKey() != null && kv.getValue() != null).forEach(kv -> {
         String key = BuilderFactory.STRING.build(kv.getKey());
         map.put(key, (Boolean.FALSE.equals(isDecode.get(key)) ? BuilderFactory.RAW_OBJECT : BuilderFactory.AGGRESSIVE_ENCODED_OBJECT).build(kv.getValue()));
      });
      return map;
   }

   static final class PerFieldDecoderDocumentBuilder extends Builder<Document> {
      private static final String ID_STR = "id";
      private static final String SCORE_STR = "score";
      private static final String FIELDS_STR = "extra_attributes";
      private final Map<String, Boolean> isFieldDecode;

      public PerFieldDecoderDocumentBuilder(Map<String, Boolean> isFieldDecode) {
         this.isFieldDecode = isFieldDecode != null ? isFieldDecode : Collections.emptyMap();
      }

      public Document build(Object data) {
         List<KeyValue> list = (List<KeyValue>)data;
         String id = null;
         Double score = null;
         Map<String, Object> fields = null;

         for (KeyValue kv : list) {
            String key = BuilderFactory.STRING.build(kv.getKey());
            switch (key) {
               case "id":
                  id = BuilderFactory.STRING.build(kv.getValue());
                  break;
               case "score":
                  score = BuilderFactory.DOUBLE.build(kv.getValue());
                  break;
               case "extra_attributes":
                  fields = Document.makeFieldsMap(this.isFieldDecode, kv.getValue());
            }
         }

         return new Document(id, score, fields);
      }
   }
}
