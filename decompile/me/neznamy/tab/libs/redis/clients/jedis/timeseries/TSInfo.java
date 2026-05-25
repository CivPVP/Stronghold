package me.neznamy.tab.libs.redis.clients.jedis.timeseries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.neznamy.tab.libs.redis.clients.jedis.Builder;
import me.neznamy.tab.libs.redis.clients.jedis.BuilderFactory;
import me.neznamy.tab.libs.redis.clients.jedis.util.DoublePrecision;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public class TSInfo {
   private static final String DUPLICATE_POLICY_PROPERTY = "duplicatePolicy";
   private static final String LABELS_PROPERTY = "labels";
   private static final String RULES_PROPERTY = "rules";
   private static final String CHUNKS_PROPERTY = "Chunks";
   private static final String CHUNKS_BYTES_PER_SAMPLE_PROPERTY = "bytesPerSample";
   private final Map<String, Object> properties;
   private final Map<String, String> labels;
   private final Map<String, TSInfo.Rule> rules;
   private final List<Map<String, Object>> chunks;
   public static Builder<TSInfo> TIMESERIES_INFO = new Builder<TSInfo>() {
      public TSInfo build(Object data) {
         List<Object> list = (List<Object>)data;
         Map<String, Object> properties = new HashMap<>();
         Map<String, String> labels = null;
         Map<String, TSInfo.Rule> rules = null;
         List<Map<String, Object>> chunks = null;

         for (int i = 0; i < list.size(); i += 2) {
            String prop = SafeEncoder.encode((byte[])list.get(i));
            Object value = list.get(i + 1);
            if (!(value instanceof List)) {
               if (value instanceof byte[]) {
                  value = SafeEncoder.encode((byte[])value);
                  if ("duplicatePolicy".equals(prop)) {
                     try {
                        value = DuplicatePolicy.valueOf(((String)value).toUpperCase());
                     } catch (Exception var19) {
                     }
                  }
               }
            } else {
               switch (prop) {
                  case "labels":
                     labels = BuilderFactory.STRING_MAP_FROM_PAIRS.build(value);
                     value = labels;
                     break;
                  case "rules":
                     List<Object> rulesDataList = (List<Object>)value;
                     List<List<Object>> rulesValueList = new ArrayList<>(rulesDataList.size());
                     rules = new HashMap<>(rulesDataList.size());

                     for (Object ruleData : rulesDataList) {
                        List<Object> encodedRule = (List<Object>)SafeEncoder.encodeObject(ruleData);
                        rulesValueList.add(encodedRule);
                        rules.put(
                           (String)encodedRule.get(0),
                           new TSInfo.Rule(
                              (String)encodedRule.get(0),
                              (Long)encodedRule.get(1),
                              AggregationType.safeValueOf((String)encodedRule.get(2)),
                              (Long)encodedRule.get(3)
                           )
                        );
                     }

                     value = rulesValueList;
                     break;
                  case "Chunks":
                     List<Object> chunksDataList = (List<Object>)value;
                     List<Map<String, Object>> chunksValueList = new ArrayList<>(chunksDataList.size());
                     chunks = new ArrayList<>(chunksDataList.size());

                     for (Object chunkData : chunksDataList) {
                        Map<String, Object> chunk = BuilderFactory.ENCODED_OBJECT_MAP.build(chunkData);
                        chunksValueList.add(new HashMap<>(chunk));
                        if (chunk.containsKey("bytesPerSample")) {
                           chunk.put("bytesPerSample", DoublePrecision.parseEncodedFloatingPointNumber(chunk.get("bytesPerSample")));
                        }

                        chunks.add(chunk);
                     }

                     value = chunksValueList;
                     break;
                  default:
                     value = SafeEncoder.encodeObject(value);
               }
            }

            properties.put(prop, value);
         }

         return new TSInfo(properties, labels, rules, chunks);
      }
   };
   public static Builder<TSInfo> TIMESERIES_INFO_RESP3 = new Builder<TSInfo>() {
      public TSInfo build(Object data) {
         List<KeyValue> list = (List<KeyValue>)data;
         Map<String, Object> properties = new HashMap<>();
         Map<String, String> labels = null;
         Map<String, TSInfo.Rule> rules = null;
         List<Map<String, Object>> chunks = null;

         for (KeyValue propertyValue : list) {
            String prop = BuilderFactory.STRING.build(propertyValue.getKey());
            Object value = propertyValue.getValue();
            if (!(value instanceof List)) {
               if (value instanceof byte[]) {
                  value = BuilderFactory.STRING.build(value);
                  if ("duplicatePolicy".equals(prop)) {
                     try {
                        value = DuplicatePolicy.valueOf(((String)value).toUpperCase());
                     } catch (Exception var20) {
                     }
                  }
               }
            } else {
               switch (prop) {
                  case "labels":
                     labels = BuilderFactory.STRING_MAP.build(value);
                     value = labels;
                     break;
                  case "rules":
                     List<KeyValue> rulesDataList = (List<KeyValue>)value;
                     Map<String, List<Object>> rulesValueMap = new HashMap<>(rulesDataList.size(), 1.0F);
                     rules = new HashMap<>(rulesDataList.size());

                     for (KeyValue rkv : rulesDataList) {
                        String ruleName = BuilderFactory.STRING.build(rkv.getKey());
                        List<Object> ruleValueList = BuilderFactory.ENCODED_OBJECT_LIST.build(rkv.getValue());
                        rulesValueMap.put(ruleName, ruleValueList);
                        rules.put(ruleName, new TSInfo.Rule(ruleName, ruleValueList));
                     }

                     value = rulesValueMap;
                     break;
                  case "Chunks":
                     List<List<KeyValue>> chunksDataList = (List<List<KeyValue>>)value;
                     List<Map<String, Object>> chunksValueList = new ArrayList<>(chunksDataList.size());
                     chunks = new ArrayList<>(chunksDataList.size());

                     for (List<KeyValue> chunkDataAsList : chunksDataList) {
                        Map<String, Object> chunk = chunkDataAsList.stream()
                           .collect(Collectors.toMap(kv -> BuilderFactory.STRING.build(kv.getKey()), kv -> BuilderFactory.ENCODED_OBJECT.build(kv.getValue())));
                        chunksValueList.add(chunk);
                        chunks.add(chunk);
                     }

                     value = chunksValueList;
                     break;
                  default:
                     value = SafeEncoder.encodeObject(value);
               }
            }

            properties.put(prop, value);
         }

         return new TSInfo(properties, labels, rules, chunks);
      }
   };

   private TSInfo(Map<String, Object> properties, Map<String, String> labels, Map<String, TSInfo.Rule> rules, List<Map<String, Object>> chunks) {
      this.properties = properties;
      this.labels = labels;
      this.rules = rules;
      this.chunks = chunks;
   }

   public Map<String, Object> getProperties() {
      return this.properties;
   }

   public Object getProperty(String property) {
      return this.properties.get(property);
   }

   public Long getIntegerProperty(String property) {
      return (Long)this.properties.get(property);
   }

   public Map<String, String> getLabels() {
      return this.labels;
   }

   public String getLabel(String label) {
      return this.labels.get(label);
   }

   public Map<String, TSInfo.Rule> getRules() {
      return this.rules;
   }

   public TSInfo.Rule getRule(String rule) {
      return this.rules.get(rule);
   }

   public List<Map<String, Object>> getChunks() {
      return this.chunks;
   }

   public static class Rule {
      private final String compactionKey;
      private final long bucketDuration;
      private final AggregationType aggregator;
      private final long alignmentTimestamp;

      private Rule(String compaction, List<Object> encodedValues) {
         this(compaction, (Long)encodedValues.get(0), AggregationType.safeValueOf((String)encodedValues.get(1)), (Long)encodedValues.get(2));
      }

      private Rule(String compaction, long bucket, AggregationType aggregation, long alignment) {
         this.compactionKey = compaction;
         this.bucketDuration = bucket;
         this.aggregator = aggregation;
         this.alignmentTimestamp = alignment;
      }

      public String getCompactionKey() {
         return this.compactionKey;
      }

      public long getBucketDuration() {
         return this.bucketDuration;
      }

      public AggregationType getAggregator() {
         return this.aggregator;
      }

      public long getAlignmentTimestamp() {
         return this.alignmentTimestamp;
      }
   }
}
