package me.neznamy.tab.libs.redis.clients.jedis;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisDataException;
import me.neznamy.tab.libs.redis.clients.jedis.resps.AccessControlLogEntry;
import me.neznamy.tab.libs.redis.clients.jedis.resps.AccessControlUser;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ClusterShardInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ClusterShardNodeInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.CommandDocument;
import me.neznamy.tab.libs.redis.clients.jedis.resps.CommandInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.GeoRadiusResponse;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LCSMatchResult;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LatencyHistoryInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LatencyLatestInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LibraryInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ScanResult;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamConsumerFullInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamConsumerInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamConsumersInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamEntry;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamFullInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamGroupFullInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamGroupInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamPendingEntry;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamPendingSummary;
import me.neznamy.tab.libs.redis.clients.jedis.resps.Tuple;
import me.neznamy.tab.libs.redis.clients.jedis.util.DoublePrecision;
import me.neznamy.tab.libs.redis.clients.jedis.util.JedisByteHashMap;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public final class BuilderFactory {
   public static final Builder<Object> RAW_OBJECT = new Builder<Object>() {
      @Override
      public Object build(Object data) {
         return data;
      }

      @Override
      public String toString() {
         return "Object";
      }
   };
   public static final Builder<List<Object>> RAW_OBJECT_LIST = new Builder<List<Object>>() {
      public List<Object> build(Object data) {
         return (List<Object>)data;
      }

      @Override
      public String toString() {
         return "List<Object>";
      }
   };
   public static final Builder<Object> ENCODED_OBJECT = new Builder<Object>() {
      @Override
      public Object build(Object data) {
         return SafeEncoder.encodeObject(data);
      }

      @Override
      public String toString() {
         return "Object";
      }
   };
   public static final Builder<List<Object>> ENCODED_OBJECT_LIST = new Builder<List<Object>>() {
      public List<Object> build(Object data) {
         return (List<Object>)SafeEncoder.encodeObject(data);
      }

      @Override
      public String toString() {
         return "List<Object>";
      }
   };
   public static final Builder<Long> LONG = new Builder<Long>() {
      public Long build(Object data) {
         return (Long)data;
      }

      @Override
      public String toString() {
         return "Long";
      }
   };
   public static final Builder<List<Long>> LONG_LIST = new Builder<List<Long>>() {
      public List<Long> build(Object data) {
         return null == data ? null : (List)data;
      }

      @Override
      public String toString() {
         return "List<Long>";
      }
   };
   public static final Builder<Double> DOUBLE = new Builder<Double>() {
      public Double build(Object data) {
         if (data == null) {
            return null;
         } else {
            return data instanceof Double ? (Double)data : DoublePrecision.parseFloatingPointNumber(BuilderFactory.STRING.build(data));
         }
      }

      @Override
      public String toString() {
         return "Double";
      }
   };
   public static final Builder<List<Double>> DOUBLE_LIST = new Builder<List<Double>>() {
      public List<Double> build(Object data) {
         return null == data ? null : ((List)data).stream().map(BuilderFactory.DOUBLE::build).collect(Collectors.toList());
      }

      @Override
      public String toString() {
         return "List<Double>";
      }
   };
   public static final Builder<Boolean> BOOLEAN = new Builder<Boolean>() {
      public Boolean build(Object data) {
         if (data == null) {
            return null;
         } else {
            return data instanceof Boolean ? (Boolean)data : (Long)data == 1L;
         }
      }

      @Override
      public String toString() {
         return "Boolean";
      }
   };
   public static final Builder<List<Boolean>> BOOLEAN_LIST = new Builder<List<Boolean>>() {
      public List<Boolean> build(Object data) {
         return null == data ? null : ((List)data).stream().map(BuilderFactory.BOOLEAN::build).collect(Collectors.toList());
      }

      @Override
      public String toString() {
         return "List<Boolean>";
      }
   };
   public static final Builder<List<Boolean>> BOOLEAN_WITH_ERROR_LIST = new Builder<List<Boolean>>() {
      public List<Boolean> build(Object data) {
         return null == data
            ? null
            : ((List)data).stream().map(val -> val instanceof JedisDataException ? null : BuilderFactory.BOOLEAN.build(val)).collect(Collectors.toList());
      }

      @Override
      public String toString() {
         return "List<Boolean>";
      }
   };
   public static final Builder<byte[]> BINARY = new Builder<byte[]>() {
      public byte[] build(Object data) {
         return (byte[])data;
      }

      @Override
      public String toString() {
         return "byte[]";
      }
   };
   public static final Builder<List<byte[]>> BINARY_LIST = new Builder<List<byte[]>>() {
      public List<byte[]> build(Object data) {
         return (List<byte[]>)data;
      }

      @Override
      public String toString() {
         return "List<byte[]>";
      }
   };
   public static final Builder<Set<byte[]>> BINARY_SET = new Builder<Set<byte[]>>() {
      public Set<byte[]> build(Object data) {
         if (null == data) {
            return null;
         }

         List<byte[]> l = BuilderFactory.BINARY_LIST.build(data);
         return BuilderFactory.SetFromList.of(l);
      }

      @Override
      public String toString() {
         return "Set<byte[]>";
      }
   };
   public static final Builder<List<Entry<byte[], byte[]>>> BINARY_PAIR_LIST = new Builder<List<Entry<byte[], byte[]>>>() {
      public List<Entry<byte[], byte[]>> build(Object data) {
         List<byte[]> flatHash = (List<byte[]>)data;
         List<Entry<byte[], byte[]>> pairList = new ArrayList<>();
         Iterator<byte[]> iterator = flatHash.iterator();

         while (iterator.hasNext()) {
            pairList.add(new SimpleEntry<>(iterator.next(), iterator.next()));
         }

         return pairList;
      }

      @Override
      public String toString() {
         return "List<Map.Entry<byte[], byte[]>>";
      }
   };
   public static final Builder<List<Entry<byte[], byte[]>>> BINARY_PAIR_LIST_FROM_PAIRS = new Builder<List<Entry<byte[], byte[]>>>() {
      public List<Entry<byte[], byte[]>> build(Object data) {
         List<Object> list = (List<Object>)data;
         List<Entry<byte[], byte[]>> pairList = new ArrayList<>();

         for (Object object : list) {
            List<byte[]> flat = (List<byte[]>)object;
            pairList.add(new SimpleEntry<>(flat.get(0), flat.get(1)));
         }

         return pairList;
      }

      @Override
      public String toString() {
         return "List<Map.Entry<byte[], byte[]>>";
      }
   };
   public static final Builder<String> STRING = new Builder<String>() {
      public String build(Object data) {
         return data == null ? null : SafeEncoder.encode((byte[])data);
      }

      @Override
      public String toString() {
         return "String";
      }
   };
   public static final Builder<List<String>> STRING_LIST = new Builder<List<String>>() {
      public List<String> build(Object data) {
         return null == data ? null : ((List)data).stream().map(BuilderFactory.STRING::build).collect(Collectors.toList());
      }

      @Override
      public String toString() {
         return "List<String>";
      }
   };
   public static final Builder<Set<String>> STRING_SET = new Builder<Set<String>>() {
      public Set<String> build(Object data) {
         return null == data ? null : ((List)data).stream().map(BuilderFactory.STRING::build).collect(Collectors.toSet());
      }

      @Override
      public String toString() {
         return "Set<String>";
      }
   };
   public static final Builder<Map<byte[], byte[]>> BINARY_MAP = new Builder<Map<byte[], byte[]>>() {
      public Map<byte[], byte[]> build(Object data) {
         List<Object> list = (List<Object>)data;
         if (list.isEmpty()) {
            return Collections.emptyMap();
         }

         if (list.get(0) instanceof KeyValue) {
            Map<byte[], byte[]> map = new JedisByteHashMap();

            for (KeyValue kv : list) {
               map.put(BuilderFactory.BINARY.build(kv.getKey()), BuilderFactory.BINARY.build(kv.getValue()));
            }

            return map;
         } else {
            Map<byte[], byte[]> map = new JedisByteHashMap();
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
               map.put(BuilderFactory.BINARY.build(iterator.next()), BuilderFactory.BINARY.build(iterator.next()));
            }

            return map;
         }
      }

      @Override
      public String toString() {
         return "Map<byte[], byte[]>";
      }
   };
   public static final Builder<Map<String, String>> STRING_MAP = new Builder<Map<String, String>>() {
      public Map<String, String> build(Object data) {
         List<Object> list = (List<Object>)data;
         if (list.isEmpty()) {
            return Collections.emptyMap();
         }

         if (list.get(0) instanceof KeyValue) {
            Map<String, String> map = new HashMap<>(list.size(), 1.0F);

            for (KeyValue kv : list) {
               map.put(BuilderFactory.STRING.build(kv.getKey()), BuilderFactory.STRING.build(kv.getValue()));
            }

            return map;
         } else {
            Map<String, String> map = new HashMap<>(list.size() / 2, 1.0F);
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
               map.put(BuilderFactory.STRING.build(iterator.next()), BuilderFactory.STRING.build(iterator.next()));
            }

            return map;
         }
      }

      @Override
      public String toString() {
         return "Map<String, String>";
      }
   };
   public static final Builder<Map<String, Object>> ENCODED_OBJECT_MAP = new Builder<Map<String, Object>>() {
      public Map<String, Object> build(Object data) {
         if (data == null) {
            return null;
         }

         List<Object> list = (List<Object>)data;
         if (list.isEmpty()) {
            return Collections.emptyMap();
         }

         if (list.get(0) instanceof KeyValue) {
            Map<String, Object> map = new HashMap<>(list.size(), 1.0F);

            for (KeyValue kv : list) {
               map.put(BuilderFactory.STRING.build(kv.getKey()), BuilderFactory.ENCODED_OBJECT.build(kv.getValue()));
            }

            return map;
         } else {
            Map<String, Object> map = new HashMap<>(list.size() / 2, 1.0F);
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
               map.put(BuilderFactory.STRING.build(iterator.next()), BuilderFactory.ENCODED_OBJECT.build(iterator.next()));
            }

            return map;
         }
      }
   };
   public static final Builder<Object> AGGRESSIVE_ENCODED_OBJECT = new Builder<Object>() {
      @Override
      public Object build(Object data) {
         if (data == null) {
            return null;
         }

         if (data instanceof List) {
            List list = (List)data;
            if (list.isEmpty()) {
               return list == Protocol.PROTOCOL_EMPTY_MAP ? Collections.emptyMap() : Collections.emptyList();
            } else {
               return list.get(0) instanceof KeyValue
                  ? ((List)data)
                     .stream()
                     .filter(kv -> kv != null && kv.getKey() != null && kv.getValue() != null)
                     .collect(Collectors.toMap(kv -> BuilderFactory.STRING.build(kv.getKey()), kv -> this.build(kv.getValue())))
                  : list.stream().map(this::build).collect(Collectors.toList());
            }
         } else {
            return data instanceof byte[] ? BuilderFactory.STRING.build(data) : data;
         }
      }
   };
   public static final Builder<Map<String, Object>> AGGRESSIVE_ENCODED_OBJECT_MAP = new Builder<Map<String, Object>>() {
      public Map<String, Object> build(Object data) {
         return (Map<String, Object>)BuilderFactory.AGGRESSIVE_ENCODED_OBJECT.build(data);
      }
   };
   public static final Builder<List<Entry<String, String>>> STRING_PAIR_LIST = new Builder<List<Entry<String, String>>>() {
      public List<Entry<String, String>> build(Object data) {
         List<byte[]> flatHash = (List<byte[]>)data;
         List<Entry<String, String>> pairList = new ArrayList<>(flatHash.size() / 2);
         Iterator<byte[]> iterator = flatHash.iterator();

         while (iterator.hasNext()) {
            pairList.add(KeyValue.of(BuilderFactory.STRING.build(iterator.next()), BuilderFactory.STRING.build(iterator.next())));
         }

         return pairList;
      }

      @Override
      public String toString() {
         return "List<Map.Entry<String, String>>";
      }
   };
   public static final Builder<List<Entry<String, String>>> STRING_PAIR_LIST_FROM_PAIRS = new Builder<List<Entry<String, String>>>() {
      public List<Entry<String, String>> build(Object data) {
         return ((List)data)
            .stream()
            .map(o -> (List)o)
            .map(l -> KeyValue.of(BuilderFactory.STRING.build(l.get(0)), BuilderFactory.STRING.build(l.get(1))))
            .collect(Collectors.toList());
      }

      @Override
      public String toString() {
         return "List<Map.Entry<String, String>>";
      }
   };
   public static final Builder<Map<String, Long>> STRING_LONG_MAP = new Builder<Map<String, Long>>() {
      public Map<String, Long> build(Object data) {
         List<Object> list = (List<Object>)data;
         if (list.isEmpty()) {
            return Collections.emptyMap();
         }

         if (list.get(0) instanceof KeyValue) {
            Map<String, Long> map = new LinkedHashMap<>(list.size(), 1.0F);

            for (KeyValue kv : list) {
               map.put(BuilderFactory.STRING.build(kv.getKey()), BuilderFactory.LONG.build(kv.getValue()));
            }

            return map;
         } else {
            Map<String, Long> map = new LinkedHashMap<>(list.size() / 2, 1.0F);
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
               map.put(BuilderFactory.STRING.build(iterator.next()), BuilderFactory.LONG.build(iterator.next()));
            }

            return map;
         }
      }

      @Override
      public String toString() {
         return "Map<String, Long>";
      }
   };
   public static final Builder<KeyValue<String, String>> KEYED_ELEMENT = new Builder<KeyValue<String, String>>() {
      public KeyValue<String, String> build(Object data) {
         if (data == null) {
            return null;
         }

         List<Object> l = (List<Object>)data;
         return KeyValue.of(BuilderFactory.STRING.build(l.get(0)), BuilderFactory.STRING.build(l.get(1)));
      }

      @Override
      public String toString() {
         return "KeyValue<String, String>";
      }
   };
   public static final Builder<KeyValue<byte[], byte[]>> BINARY_KEYED_ELEMENT = new Builder<KeyValue<byte[], byte[]>>() {
      public KeyValue<byte[], byte[]> build(Object data) {
         if (data == null) {
            return null;
         }

         List<Object> l = (List<Object>)data;
         return KeyValue.of(BuilderFactory.BINARY.build(l.get(0)), BuilderFactory.BINARY.build(l.get(1)));
      }

      @Override
      public String toString() {
         return "KeyValue<byte[], byte[]>";
      }
   };
   public static final Builder<KeyValue<Long, Double>> ZRANK_WITHSCORE_PAIR = new Builder<KeyValue<Long, Double>>() {
      public KeyValue<Long, Double> build(Object data) {
         if (data == null) {
            return null;
         }

         List<Object> l = (List<Object>)data;
         return new KeyValue<>(BuilderFactory.LONG.build(l.get(0)), BuilderFactory.DOUBLE.build(l.get(1)));
      }

      @Override
      public String toString() {
         return "KeyValue<Long, Double>";
      }
   };
   public static final Builder<KeyValue<String, List<String>>> KEYED_STRING_LIST = new Builder<KeyValue<String, List<String>>>() {
      public KeyValue<String, List<String>> build(Object data) {
         if (data == null) {
            return null;
         }

         List<byte[]> l = (List<byte[]>)data;
         return new KeyValue<>(BuilderFactory.STRING.build(l.get(0)), BuilderFactory.STRING_LIST.build(l.get(1)));
      }

      @Override
      public String toString() {
         return "KeyValue<String, List<String>>";
      }
   };
   public static final Builder<KeyValue<Long, Long>> LONG_LONG_PAIR = new Builder<KeyValue<Long, Long>>() {
      public KeyValue<Long, Long> build(Object data) {
         if (data == null) {
            return null;
         }

         List<Object> dataList = (List<Object>)data;
         return new KeyValue<>(BuilderFactory.LONG.build(dataList.get(0)), BuilderFactory.LONG.build(dataList.get(1)));
      }
   };
   public static final Builder<List<KeyValue<String, List<String>>>> KEYED_STRING_LIST_LIST = new Builder<List<KeyValue<String, List<String>>>>() {
      public List<KeyValue<String, List<String>>> build(Object data) {
         List<Object> list = (List<Object>)data;
         return list.stream().map(BuilderFactory.KEYED_STRING_LIST::build).collect(Collectors.toList());
      }
   };
   public static final Builder<KeyValue<byte[], List<byte[]>>> KEYED_BINARY_LIST = new Builder<KeyValue<byte[], List<byte[]>>>() {
      public KeyValue<byte[], List<byte[]>> build(Object data) {
         if (data == null) {
            return null;
         }

         List<byte[]> l = (List<byte[]>)data;
         return new KeyValue<>(BuilderFactory.BINARY.build(l.get(0)), BuilderFactory.BINARY_LIST.build(l.get(1)));
      }

      @Override
      public String toString() {
         return "KeyValue<byte[], List<byte[]>>";
      }
   };
   public static final Builder<Tuple> TUPLE = new Builder<Tuple>() {
      public Tuple build(Object data) {
         List<byte[]> l = (List<byte[]>)data;
         return l.isEmpty() ? null : new Tuple(l.get(0), BuilderFactory.DOUBLE.build(l.get(1)));
      }

      @Override
      public String toString() {
         return "Tuple";
      }
   };
   public static final Builder<KeyValue<String, Tuple>> KEYED_TUPLE = new Builder<KeyValue<String, Tuple>>() {
      public KeyValue<String, Tuple> build(Object data) {
         if (data == null) {
            return null;
         }

         List<Object> l = (List<Object>)data;
         return l.isEmpty()
            ? null
            : KeyValue.of(BuilderFactory.STRING.build(l.get(0)), new Tuple(BuilderFactory.BINARY.build(l.get(1)), BuilderFactory.DOUBLE.build(l.get(2))));
      }

      @Override
      public String toString() {
         return "KeyValue<String, Tuple>";
      }
   };
   public static final Builder<KeyValue<byte[], Tuple>> BINARY_KEYED_TUPLE = new Builder<KeyValue<byte[], Tuple>>() {
      public KeyValue<byte[], Tuple> build(Object data) {
         if (data == null) {
            return null;
         }

         List<Object> l = (List<Object>)data;
         return l.isEmpty()
            ? null
            : KeyValue.of(BuilderFactory.BINARY.build(l.get(0)), new Tuple(BuilderFactory.BINARY.build(l.get(1)), BuilderFactory.DOUBLE.build(l.get(2))));
      }

      @Override
      public String toString() {
         return "KeyValue<byte[], Tuple>";
      }
   };
   public static final Builder<List<Tuple>> TUPLE_LIST = new Builder<List<Tuple>>() {
      public List<Tuple> build(Object data) {
         if (null == data) {
            return null;
         }

         List<byte[]> l = (List<byte[]>)data;
         List<Tuple> result = new ArrayList<>(l.size() / 2);
         Iterator<byte[]> iterator = l.iterator();

         while (iterator.hasNext()) {
            result.add(new Tuple(iterator.next(), BuilderFactory.DOUBLE.build(iterator.next())));
         }

         return result;
      }

      @Override
      public String toString() {
         return "List<Tuple>";
      }
   };
   public static final Builder<List<Tuple>> TUPLE_LIST_RESP3 = new Builder<List<Tuple>>() {
      public List<Tuple> build(Object data) {
         return null == data ? null : ((List)data).stream().map(BuilderFactory.TUPLE::build).collect(Collectors.toList());
      }

      @Override
      public String toString() {
         return "List<Tuple>";
      }
   };
   @Deprecated
   public static final Builder<Set<Tuple>> TUPLE_ZSET = new Builder<Set<Tuple>>() {
      public Set<Tuple> build(Object data) {
         if (null == data) {
            return null;
         }

         List<byte[]> l = (List<byte[]>)data;
         Set<Tuple> result = new LinkedHashSet<>(l.size() / 2, 1.0F);
         Iterator<byte[]> iterator = l.iterator();

         while (iterator.hasNext()) {
            result.add(new Tuple(iterator.next(), BuilderFactory.DOUBLE.build(iterator.next())));
         }

         return result;
      }

      @Override
      public String toString() {
         return "ZSet<Tuple>";
      }
   };
   @Deprecated
   public static final Builder<Set<Tuple>> TUPLE_ZSET_RESP3 = new Builder<Set<Tuple>>() {
      public Set<Tuple> build(Object data) {
         return null == data ? null : ((List)data).stream().map(BuilderFactory.TUPLE::build).collect(Collectors.toCollection(LinkedHashSet::new));
      }

      @Override
      public String toString() {
         return "ZSet<Tuple>";
      }
   };
   private static final Builder<List<Tuple>> TUPLE_LIST_FROM_PAIRS = new Builder<List<Tuple>>() {
      public List<Tuple> build(Object data) {
         return data == null ? null : ((List)data).stream().map(BuilderFactory.TUPLE::build).collect(Collectors.toList());
      }

      @Override
      public String toString() {
         return "List<Tuple>";
      }
   };
   public static final Builder<KeyValue<String, List<Tuple>>> KEYED_TUPLE_LIST = new Builder<KeyValue<String, List<Tuple>>>() {
      public KeyValue<String, List<Tuple>> build(Object data) {
         if (data == null) {
            return null;
         }

         List<Object> l = (List<Object>)data;
         return new KeyValue<>(BuilderFactory.STRING.build(l.get(0)), BuilderFactory.TUPLE_LIST_FROM_PAIRS.build(l.get(1)));
      }

      @Override
      public String toString() {
         return "KeyValue<String, List<Tuple>>";
      }
   };
   public static final Builder<KeyValue<byte[], List<Tuple>>> BINARY_KEYED_TUPLE_LIST = new Builder<KeyValue<byte[], List<Tuple>>>() {
      public KeyValue<byte[], List<Tuple>> build(Object data) {
         if (data == null) {
            return null;
         }

         List<Object> l = (List<Object>)data;
         return new KeyValue<>(BuilderFactory.BINARY.build(l.get(0)), BuilderFactory.TUPLE_LIST_FROM_PAIRS.build(l.get(1)));
      }

      @Override
      public String toString() {
         return "KeyValue<byte[], List<Tuple>>";
      }
   };
   public static final Builder<ScanResult<String>> SCAN_RESPONSE = new Builder<ScanResult<String>>() {
      public ScanResult<String> build(Object data) {
         List<Object> result = (List<Object>)data;
         String newcursor = new String((byte[])result.get(0));
         List<byte[]> rawResults = (List<byte[]>)result.get(1);
         List<String> results = new ArrayList<>(rawResults.size());

         for (byte[] bs : rawResults) {
            results.add(SafeEncoder.encode(bs));
         }

         return new ScanResult<>(newcursor, results);
      }
   };
   public static final Builder<ScanResult<Entry<String, String>>> HSCAN_RESPONSE = new Builder<ScanResult<Entry<String, String>>>() {
      public ScanResult<Entry<String, String>> build(Object data) {
         List<Object> result = (List<Object>)data;
         String newcursor = new String((byte[])result.get(0));
         List<byte[]> rawResults = (List<byte[]>)result.get(1);
         List<Entry<String, String>> results = new ArrayList<>(rawResults.size() / 2);
         Iterator<byte[]> iterator = rawResults.iterator();

         while (iterator.hasNext()) {
            results.add(new SimpleEntry<>(SafeEncoder.encode(iterator.next()), SafeEncoder.encode(iterator.next())));
         }

         return new ScanResult<>(newcursor, results);
      }
   };
   public static final Builder<ScanResult<String>> SSCAN_RESPONSE = new Builder<ScanResult<String>>() {
      public ScanResult<String> build(Object data) {
         List<Object> result = (List<Object>)data;
         String newcursor = new String((byte[])result.get(0));
         List<byte[]> rawResults = (List<byte[]>)result.get(1);
         List<String> results = new ArrayList<>(rawResults.size());

         for (byte[] bs : rawResults) {
            results.add(SafeEncoder.encode(bs));
         }

         return new ScanResult<>(newcursor, results);
      }
   };
   public static final Builder<ScanResult<Tuple>> ZSCAN_RESPONSE = new Builder<ScanResult<Tuple>>() {
      public ScanResult<Tuple> build(Object data) {
         List<Object> result = (List<Object>)data;
         String newcursor = new String((byte[])result.get(0));
         List<byte[]> rawResults = (List<byte[]>)result.get(1);
         List<Tuple> results = new ArrayList<>(rawResults.size() / 2);
         Iterator<byte[]> iterator = rawResults.iterator();

         while (iterator.hasNext()) {
            results.add(new Tuple(iterator.next(), BuilderFactory.DOUBLE.build(iterator.next())));
         }

         return new ScanResult<>(newcursor, results);
      }
   };
   public static final Builder<ScanResult<byte[]>> SCAN_BINARY_RESPONSE = new Builder<ScanResult<byte[]>>() {
      public ScanResult<byte[]> build(Object data) {
         List<Object> result = (List<Object>)data;
         byte[] newcursor = (byte[])result.get(0);
         List<byte[]> rawResults = (List<byte[]>)result.get(1);
         return new ScanResult<>(newcursor, rawResults);
      }
   };
   public static final Builder<ScanResult<Entry<byte[], byte[]>>> HSCAN_BINARY_RESPONSE = new Builder<ScanResult<Entry<byte[], byte[]>>>() {
      public ScanResult<Entry<byte[], byte[]>> build(Object data) {
         List<Object> result = (List<Object>)data;
         byte[] newcursor = (byte[])result.get(0);
         List<byte[]> rawResults = (List<byte[]>)result.get(1);
         List<Entry<byte[], byte[]>> results = new ArrayList<>(rawResults.size() / 2);
         Iterator<byte[]> iterator = rawResults.iterator();

         while (iterator.hasNext()) {
            results.add(new SimpleEntry<>(iterator.next(), iterator.next()));
         }

         return new ScanResult<>(newcursor, results);
      }
   };
   public static final Builder<ScanResult<byte[]>> SSCAN_BINARY_RESPONSE = new Builder<ScanResult<byte[]>>() {
      public ScanResult<byte[]> build(Object data) {
         List<Object> result = (List<Object>)data;
         byte[] newcursor = (byte[])result.get(0);
         List<byte[]> rawResults = (List<byte[]>)result.get(1);
         return new ScanResult<>(newcursor, rawResults);
      }
   };
   public static final Builder<Map<String, Long>> PUBSUB_NUMSUB_MAP = new Builder<Map<String, Long>>() {
      public Map<String, Long> build(Object data) {
         List<Object> flatHash = (List<Object>)data;
         Map<String, Long> hash = new HashMap<>(flatHash.size() / 2, 1.0F);
         Iterator<Object> iterator = flatHash.iterator();

         while (iterator.hasNext()) {
            hash.put(SafeEncoder.encode((byte[])iterator.next()), (Long)iterator.next());
         }

         return hash;
      }

      @Override
      public String toString() {
         return "PUBSUB_NUMSUB_MAP<String, String>";
      }
   };
   public static final Builder<List<GeoCoordinate>> GEO_COORDINATE_LIST = new Builder<List<GeoCoordinate>>() {
      public List<GeoCoordinate> build(Object data) {
         return null == data ? null : this.interpretGeoposResult((List<Object>)data);
      }

      @Override
      public String toString() {
         return "List<GeoCoordinate>";
      }

      private List<GeoCoordinate> interpretGeoposResult(List<Object> responses) {
         List<GeoCoordinate> responseCoordinate = new ArrayList<>(responses.size());

         for (Object response : responses) {
            if (response == null) {
               responseCoordinate.add(null);
            } else {
               List<Object> respList = (List<Object>)response;
               GeoCoordinate coord = new GeoCoordinate(BuilderFactory.DOUBLE.build(respList.get(0)), BuilderFactory.DOUBLE.build(respList.get(1)));
               responseCoordinate.add(coord);
            }
         }

         return responseCoordinate;
      }
   };
   public static final Builder<List<GeoRadiusResponse>> GEORADIUS_WITH_PARAMS_RESULT = new Builder<List<GeoRadiusResponse>>() {
      public List<GeoRadiusResponse> build(Object data) {
         if (data == null) {
            return null;
         }

         List<Object> objectList = (List<Object>)data;
         List<GeoRadiusResponse> responses = new ArrayList<>(objectList.size());
         if (objectList.isEmpty()) {
            return responses;
         }

         if (objectList.get(0) instanceof List) {
            for (Object obj : objectList) {
               List<Object> informations = (List<Object>)obj;
               GeoRadiusResponse resp = new GeoRadiusResponse((byte[])informations.get(0));
               int size = informations.size();

               for (int idx = 1; idx < size; idx++) {
                  Object info = informations.get(idx);
                  if (info instanceof List) {
                     List<Object> coord = (List<Object>)info;
                     resp.setCoordinate(new GeoCoordinate(BuilderFactory.DOUBLE.build(coord.get(0)), BuilderFactory.DOUBLE.build(coord.get(1))));
                  } else if (info instanceof Long) {
                     resp.setRawScore(BuilderFactory.LONG.build(info));
                  } else {
                     resp.setDistance(BuilderFactory.DOUBLE.build(info));
                  }
               }

               responses.add(resp);
            }
         } else {
            for (Object obj : objectList) {
               responses.add(new GeoRadiusResponse((byte[])obj));
            }
         }

         return responses;
      }

      @Override
      public String toString() {
         return "GeoRadiusWithParamsResult";
      }
   };
   public static final Builder<Map<String, CommandDocument>> COMMAND_DOCS_RESPONSE = new Builder<Map<String, CommandDocument>>() {
      public Map<String, CommandDocument> build(Object data) {
         if (data == null) {
            return null;
         }

         List<Object> list = (List<Object>)data;
         if (list.isEmpty()) {
            return Collections.emptyMap();
         }

         if (list.get(0) instanceof KeyValue) {
            Map<String, CommandDocument> map = new HashMap<>(list.size(), 1.0F);

            for (KeyValue kv : list) {
               map.put(BuilderFactory.STRING.build(kv.getKey()), new CommandDocument(BuilderFactory.ENCODED_OBJECT_MAP.build(kv.getValue())));
            }

            return map;
         } else {
            Map<String, CommandDocument> map = new HashMap<>(list.size() / 2, 1.0F);
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
               map.put(BuilderFactory.STRING.build(iterator.next()), new CommandDocument(BuilderFactory.ENCODED_OBJECT_MAP.build(iterator.next())));
            }

            return map;
         }
      }
   };
   public static final Builder<Map<String, CommandInfo>> COMMAND_INFO_RESPONSE = new Builder<Map<String, CommandInfo>>() {
      public Map<String, CommandInfo> build(Object data) {
         if (data == null) {
            return null;
         }

         List<Object> rawList = (List<Object>)data;
         Map<String, CommandInfo> map = new HashMap<>(rawList.size());

         for (Object rawCommandInfo : rawList) {
            if (rawCommandInfo != null) {
               List<Object> commandInfo = (List<Object>)rawCommandInfo;
               String name = BuilderFactory.STRING.build(commandInfo.get(0));
               CommandInfo info = CommandInfo.COMMAND_INFO_BUILDER.build(commandInfo);
               map.put(name, info);
            }
         }

         return map;
      }
   };
   public static final Builder<Map<String, LatencyLatestInfo>> LATENCY_LATEST_RESPONSE = new Builder<Map<String, LatencyLatestInfo>>() {
      public Map<String, LatencyLatestInfo> build(Object data) {
         if (data == null) {
            return null;
         }

         List<Object> rawList = (List<Object>)data;
         Map<String, LatencyLatestInfo> map = new HashMap<>(rawList.size());

         for (Object rawLatencyLatestInfo : rawList) {
            if (rawLatencyLatestInfo != null) {
               LatencyLatestInfo latestInfo = LatencyLatestInfo.LATENCY_LATEST_BUILDER.build(rawLatencyLatestInfo);
               String name = latestInfo.getCommand();
               map.put(name, latestInfo);
            }
         }

         return map;
      }
   };
   public static final Builder<List<LatencyHistoryInfo>> LATENCY_HISTORY_RESPONSE = new Builder<List<LatencyHistoryInfo>>() {
      public List<LatencyHistoryInfo> build(Object data) {
         if (data == null) {
            return null;
         }

         List<Object> rawList = (List<Object>)data;
         List<LatencyHistoryInfo> response = new ArrayList<>(rawList.size());

         for (Object rawLatencyHistoryInfo : rawList) {
            if (rawLatencyHistoryInfo != null) {
               LatencyHistoryInfo historyInfo = LatencyHistoryInfo.LATENCY_HISTORY_BUILDER.build(rawLatencyHistoryInfo);
               response.add(historyInfo);
            }
         }

         return response;
      }
   };
   private static final Builder<List<List<Long>>> CLUSTER_SHARD_SLOTS_RANGES = new Builder<List<List<Long>>>() {
      public List<List<Long>> build(Object data) {
         if (null == data) {
            return null;
         }

         List<Long> rawSlots = (List<Long>)data;
         List<List<Long>> slotsRanges = new ArrayList<>();

         for (int i = 0; i < rawSlots.size(); i += 2) {
            slotsRanges.add(Arrays.asList(rawSlots.get(i), rawSlots.get(i + 1)));
         }

         return slotsRanges;
      }
   };
   private static final Builder<List<ClusterShardNodeInfo>> CLUSTER_SHARD_NODE_INFO_LIST = new Builder<List<ClusterShardNodeInfo>>() {
      final Map<String, Builder> mappingFunctions = this.createDecoderMap();

      private Map<String, Builder> createDecoderMap() {
         Map<String, Builder> tempMappingFunctions = new HashMap<>();
         tempMappingFunctions.put("id", BuilderFactory.STRING);
         tempMappingFunctions.put("endpoint", BuilderFactory.STRING);
         tempMappingFunctions.put("ip", BuilderFactory.STRING);
         tempMappingFunctions.put("hostname", BuilderFactory.STRING);
         tempMappingFunctions.put("port", BuilderFactory.LONG);
         tempMappingFunctions.put("tls-port", BuilderFactory.LONG);
         tempMappingFunctions.put("role", BuilderFactory.STRING);
         tempMappingFunctions.put("replication-offset", BuilderFactory.LONG);
         tempMappingFunctions.put("health", BuilderFactory.STRING);
         return tempMappingFunctions;
      }

      public List<ClusterShardNodeInfo> build(Object data) {
         if (null == data) {
            return null;
         }

         List<ClusterShardNodeInfo> response = new ArrayList<>();

         for (Object clusterShardNodeInfoObject : (List)data) {
            List<Object> clusterShardNodeInfo = (List<Object>)clusterShardNodeInfoObject;
            Iterator<Object> iterator = clusterShardNodeInfo.iterator();
            response.add(new ClusterShardNodeInfo(BuilderFactory.createMapFromDecodingFunctions(iterator, this.mappingFunctions)));
         }

         return response;
      }

      @Override
      public String toString() {
         return "List<ClusterShardNodeInfo>";
      }
   };
   public static final Builder<List<ClusterShardInfo>> CLUSTER_SHARD_INFO_LIST = new Builder<List<ClusterShardInfo>>() {
      final Map<String, Builder> mappingFunctions = this.createDecoderMap();

      private Map<String, Builder> createDecoderMap() {
         Map<String, Builder> tempMappingFunctions = new HashMap<>();
         tempMappingFunctions.put("slots", BuilderFactory.CLUSTER_SHARD_SLOTS_RANGES);
         tempMappingFunctions.put("nodes", BuilderFactory.CLUSTER_SHARD_NODE_INFO_LIST);
         return tempMappingFunctions;
      }

      public List<ClusterShardInfo> build(Object data) {
         if (null == data) {
            return null;
         }

         List<ClusterShardInfo> response = new ArrayList<>();

         for (Object clusterShardInfoObject : (List)data) {
            List<Object> clusterShardInfo = (List<Object>)clusterShardInfoObject;
            Iterator<Object> iterator = clusterShardInfo.iterator();
            response.add(new ClusterShardInfo(BuilderFactory.createMapFromDecodingFunctions(iterator, this.mappingFunctions)));
         }

         return response;
      }

      @Override
      public String toString() {
         return "List<ClusterShardInfo>";
      }
   };
   public static final Builder<List<Module>> MODULE_LIST = new Builder<List<Module>>() {
      public List<Module> build(Object data) {
         if (data == null) {
            return null;
         }

         List<List<Object>> objectList = (List<List<Object>>)data;
         List<Module> responses = new ArrayList<>(objectList.size());
         if (objectList.isEmpty()) {
            return responses;
         }

         for (List<Object> moduleResp : objectList) {
            if (moduleResp.get(0) instanceof KeyValue) {
               responses.add(
                  new Module(
                     BuilderFactory.STRING.build(((KeyValue)moduleResp.get(0)).getValue()),
                     BuilderFactory.LONG.build(((KeyValue)moduleResp.get(1)).getValue()).intValue()
                  )
               );
            } else {
               Module m = new Module(SafeEncoder.encode((byte[])moduleResp.get(1)), ((Long)moduleResp.get(3)).intValue());
               responses.add(m);
            }
         }

         return responses;
      }

      @Override
      public String toString() {
         return "List<Module>";
      }
   };
   public static final Builder<AccessControlUser> ACCESS_CONTROL_USER = new Builder<AccessControlUser>() {
      public AccessControlUser build(Object data) {
         Map<String, Object> map = BuilderFactory.ENCODED_OBJECT_MAP.build(data);
         return map == null ? null : new AccessControlUser(map);
      }

      @Override
      public String toString() {
         return "AccessControlUser";
      }
   };
   public static final Builder<List<AccessControlLogEntry>> ACCESS_CONTROL_LOG_ENTRY_LIST = new Builder<List<AccessControlLogEntry>>() {
      private final Map<String, Builder> mappingFunctions = this.createDecoderMap();

      private Map<String, Builder> createDecoderMap() {
         Map<String, Builder> tempMappingFunctions = new HashMap<>();
         tempMappingFunctions.put("count", BuilderFactory.LONG);
         tempMappingFunctions.put("reason", BuilderFactory.STRING);
         tempMappingFunctions.put("context", BuilderFactory.STRING);
         tempMappingFunctions.put("object", BuilderFactory.STRING);
         tempMappingFunctions.put("username", BuilderFactory.STRING);
         tempMappingFunctions.put("age-seconds", BuilderFactory.DOUBLE);
         tempMappingFunctions.put("client-info", BuilderFactory.STRING);
         tempMappingFunctions.put("entry-id", BuilderFactory.LONG);
         tempMappingFunctions.put("timestamp-created", BuilderFactory.LONG);
         tempMappingFunctions.put("timestamp-last-updated", BuilderFactory.LONG);
         return tempMappingFunctions;
      }

      public List<AccessControlLogEntry> build(Object data) {
         if (null == data) {
            return null;
         }

         List<AccessControlLogEntry> list = new ArrayList<>();

         for (List<Object> logEntryData : (List)data) {
            Iterator<Object> logEntryDataIterator = logEntryData.iterator();
            AccessControlLogEntry accessControlLogEntry = new AccessControlLogEntry(
               BuilderFactory.createMapFromDecodingFunctions(logEntryDataIterator, this.mappingFunctions, BuilderFactory.BACKUP_BUILDERS_FOR_DECODING_FUNCTIONS)
            );
            list.add(accessControlLogEntry);
         }

         return list;
      }

      @Override
      public String toString() {
         return "List<AccessControlLogEntry>";
      }
   };
   public static final Builder<StreamEntryID> STREAM_ENTRY_ID = new Builder<StreamEntryID>() {
      public StreamEntryID build(Object data) {
         if (null == data) {
            return null;
         }

         String id = SafeEncoder.encode((byte[])data);
         return new StreamEntryID(id);
      }

      @Override
      public String toString() {
         return "StreamEntryID";
      }
   };
   public static final Builder<List<StreamEntryID>> STREAM_ENTRY_ID_LIST = new Builder<List<StreamEntryID>>() {
      public List<StreamEntryID> build(Object data) {
         if (null == data) {
            return null;
         }

         List<Object> objectList = (List<Object>)data;
         List<StreamEntryID> responses = new ArrayList<>(objectList.size());
         if (!objectList.isEmpty()) {
            for (Object object : objectList) {
               responses.add(BuilderFactory.STREAM_ENTRY_ID.build(object));
            }
         }

         return responses;
      }
   };
   public static final Builder<StreamEntry> STREAM_ENTRY = new Builder<StreamEntry>() {
      public StreamEntry build(Object data) {
         if (null == data) {
            return null;
         }

         List<Object> objectList = (List<Object>)data;
         if (objectList.isEmpty()) {
            return null;
         }

         String entryIdString = SafeEncoder.encode((byte[])objectList.get(0));
         StreamEntryID entryID = new StreamEntryID(entryIdString);
         List<byte[]> hash = (List<byte[]>)objectList.get(1);
         Iterator<byte[]> hashIterator = hash.iterator();
         Map<String, String> map = new HashMap<>(hash.size() / 2, 1.0F);

         while (hashIterator.hasNext()) {
            map.put(SafeEncoder.encode(hashIterator.next()), SafeEncoder.encode(hashIterator.next()));
         }

         return new StreamEntry(entryID, map);
      }

      @Override
      public String toString() {
         return "StreamEntry";
      }
   };
   public static final Builder<List<StreamEntry>> STREAM_ENTRY_LIST = new Builder<List<StreamEntry>>() {
      public List<StreamEntry> build(Object data) {
         if (null == data) {
            return null;
         }

         List<ArrayList<Object>> objectList = (List<ArrayList<Object>>)data;
         List<StreamEntry> responses = new ArrayList<>(objectList.size() / 2);
         if (objectList.isEmpty()) {
            return responses;
         }

         for (ArrayList<Object> res : objectList) {
            if (res == null) {
               responses.add(null);
            } else {
               String entryIdString = SafeEncoder.encode((byte[])res.get(0));
               StreamEntryID entryID = new StreamEntryID(entryIdString);
               List<byte[]> hash = (List<byte[]>)res.get(1);
               if (hash == null) {
                  responses.add(new StreamEntry(entryID, null));
               } else {
                  Iterator<byte[]> hashIterator = hash.iterator();
                  Map<String, String> map = new HashMap<>(hash.size() / 2, 1.0F);

                  while (hashIterator.hasNext()) {
                     map.put(SafeEncoder.encode(hashIterator.next()), SafeEncoder.encode(hashIterator.next()));
                  }

                  responses.add(new StreamEntry(entryID, map));
               }
            }
         }

         return responses;
      }

      @Override
      public String toString() {
         return "List<StreamEntry>";
      }
   };
   public static final Builder<Entry<StreamEntryID, List<StreamEntry>>> STREAM_AUTO_CLAIM_RESPONSE = new Builder<Entry<StreamEntryID, List<StreamEntry>>>() {
      public Entry<StreamEntryID, List<StreamEntry>> build(Object data) {
         if (null == data) {
            return null;
         }

         List<Object> objectList = (List<Object>)data;
         return new SimpleEntry<>(BuilderFactory.STREAM_ENTRY_ID.build(objectList.get(0)), BuilderFactory.STREAM_ENTRY_LIST.build(objectList.get(1)));
      }

      @Override
      public String toString() {
         return "Map.Entry<StreamEntryID, List<StreamEntry>>";
      }
   };
   public static final Builder<Entry<StreamEntryID, List<StreamEntryID>>> STREAM_AUTO_CLAIM_JUSTID_RESPONSE = new Builder<Entry<StreamEntryID, List<StreamEntryID>>>() {
      public Entry<StreamEntryID, List<StreamEntryID>> build(Object data) {
         if (null == data) {
            return null;
         }

         List<Object> objectList = (List<Object>)data;
         return new SimpleEntry<>(BuilderFactory.STREAM_ENTRY_ID.build(objectList.get(0)), BuilderFactory.STREAM_ENTRY_ID_LIST.build(objectList.get(1)));
      }

      @Override
      public String toString() {
         return "Map.Entry<StreamEntryID, List<StreamEntryID>>";
      }
   };
   @Deprecated
   public static final Builder<Entry<StreamEntryID, List<StreamEntryID>>> STREAM_AUTO_CLAIM_ID_RESPONSE = STREAM_AUTO_CLAIM_JUSTID_RESPONSE;
   public static final Builder<List<Entry<String, List<StreamEntry>>>> STREAM_READ_RESPONSE = new Builder<List<Entry<String, List<StreamEntry>>>>() {
      public List<Entry<String, List<StreamEntry>>> build(Object data) {
         if (data == null) {
            return null;
         }

         List list = (List)data;
         if (list.isEmpty()) {
            return Collections.emptyList();
         }

         if (list.get(0) instanceof KeyValue) {
            return list.stream()
               .map(kv -> new KeyValue<>(BuilderFactory.STRING.build(kv.getKey()), BuilderFactory.STREAM_ENTRY_LIST.build(kv.getValue())))
               .collect(Collectors.toList());
         }

         List<Entry<String, List<StreamEntry>>> result = new ArrayList<>(list.size());

         for (Object anObj : list) {
            List<Object> streamObj = (List<Object>)anObj;
            String streamKey = BuilderFactory.STRING.build(streamObj.get(0));
            List<StreamEntry> streamEntries = BuilderFactory.STREAM_ENTRY_LIST.build(streamObj.get(1));
            result.add(KeyValue.of(streamKey, streamEntries));
         }

         return result;
      }

      @Override
      public String toString() {
         return "List<Entry<String, List<StreamEntry>>>";
      }
   };
   public static final Builder<Map<String, List<StreamEntry>>> STREAM_READ_MAP_RESPONSE = new Builder<Map<String, List<StreamEntry>>>() {
      public Map<String, List<StreamEntry>> build(Object data) {
         if (data == null) {
            return null;
         }

         List list = (List)data;
         if (list.isEmpty()) {
            return Collections.emptyMap();
         }

         if (list.get(0) instanceof KeyValue) {
            return list.stream()
               .collect(Collectors.toMap(kv -> BuilderFactory.STRING.build(kv.getKey()), kv -> BuilderFactory.STREAM_ENTRY_LIST.build(kv.getValue())));
         }

         Map<String, List<StreamEntry>> result = new HashMap<>(list.size());

         for (Object anObj : list) {
            List<Object> streamObj = (List<Object>)anObj;
            String streamKey = BuilderFactory.STRING.build(streamObj.get(0));
            List<StreamEntry> streamEntries = BuilderFactory.STREAM_ENTRY_LIST.build(streamObj.get(1));
            result.put(streamKey, streamEntries);
         }

         return result;
      }

      @Override
      public String toString() {
         return "Map<String, List<StreamEntry>>";
      }
   };
   public static final Builder<List<StreamPendingEntry>> STREAM_PENDING_ENTRY_LIST = new Builder<List<StreamPendingEntry>>() {
      public List<StreamPendingEntry> build(Object data) {
         if (null == data) {
            return null;
         }

         List<Object> streamsEntries = (List<Object>)data;
         List<StreamPendingEntry> result = new ArrayList<>(streamsEntries.size());

         for (Object streamObj : streamsEntries) {
            List<Object> stream = (List<Object>)streamObj;
            String id = SafeEncoder.encode((byte[])stream.get(0));
            String consumerName = SafeEncoder.encode((byte[])stream.get(1));
            long idleTime = BuilderFactory.LONG.build(stream.get(2));
            long deliveredTimes = BuilderFactory.LONG.build(stream.get(3));
            result.add(new StreamPendingEntry(new StreamEntryID(id), consumerName, idleTime, deliveredTimes));
         }

         return result;
      }

      @Override
      public String toString() {
         return "List<StreamPendingEntry>";
      }
   };
   public static final Builder<StreamInfo> STREAM_INFO = new Builder<StreamInfo>() {
      Map<String, Builder> mappingFunctions = this.createDecoderMap();

      private Map<String, Builder> createDecoderMap() {
         Map<String, Builder> tempMappingFunctions = new HashMap<>();
         tempMappingFunctions.put("last-generated-id", BuilderFactory.STREAM_ENTRY_ID);
         tempMappingFunctions.put("first-entry", BuilderFactory.STREAM_ENTRY);
         tempMappingFunctions.put("length", BuilderFactory.LONG);
         tempMappingFunctions.put("radix-tree-keys", BuilderFactory.LONG);
         tempMappingFunctions.put("radix-tree-nodes", BuilderFactory.LONG);
         tempMappingFunctions.put("last-entry", BuilderFactory.STREAM_ENTRY);
         tempMappingFunctions.put("groups", BuilderFactory.LONG);
         return tempMappingFunctions;
      }

      public StreamInfo build(Object data) {
         if (null == data) {
            return null;
         }

         List<Object> streamsEntries = (List<Object>)data;
         Iterator<Object> iterator = streamsEntries.iterator();
         return new StreamInfo(BuilderFactory.createMapFromDecodingFunctions(iterator, this.mappingFunctions));
      }

      @Override
      public String toString() {
         return "StreamInfo";
      }
   };
   public static final Builder<List<StreamGroupInfo>> STREAM_GROUP_INFO_LIST = new Builder<List<StreamGroupInfo>>() {
      Map<String, Builder> mappingFunctions = this.createDecoderMap();

      private Map<String, Builder> createDecoderMap() {
         Map<String, Builder> tempMappingFunctions = new HashMap<>();
         tempMappingFunctions.put("name", BuilderFactory.STRING);
         tempMappingFunctions.put("consumers", BuilderFactory.LONG);
         tempMappingFunctions.put("pending", BuilderFactory.LONG);
         tempMappingFunctions.put("last-delivered-id", BuilderFactory.STREAM_ENTRY_ID);
         return tempMappingFunctions;
      }

      public List<StreamGroupInfo> build(Object data) {
         if (null == data) {
            return null;
         }

         List<StreamGroupInfo> list = new ArrayList<>();

         for (List<Object> groupInfo : (List)data) {
            Iterator<Object> groupInfoIterator = groupInfo.iterator();
            StreamGroupInfo streamGroupInfo = new StreamGroupInfo(BuilderFactory.createMapFromDecodingFunctions(groupInfoIterator, this.mappingFunctions));
            list.add(streamGroupInfo);
         }

         return list;
      }

      @Override
      public String toString() {
         return "List<StreamGroupInfo>";
      }
   };
   @Deprecated
   public static final Builder<List<StreamConsumersInfo>> STREAM_CONSUMERS_INFO_LIST = new Builder<List<StreamConsumersInfo>>() {
      Map<String, Builder> mappingFunctions = this.createDecoderMap();

      private Map<String, Builder> createDecoderMap() {
         Map<String, Builder> tempMappingFunctions = new HashMap<>();
         tempMappingFunctions.put("name", BuilderFactory.STRING);
         tempMappingFunctions.put("idle", BuilderFactory.LONG);
         tempMappingFunctions.put("pending", BuilderFactory.LONG);
         return tempMappingFunctions;
      }

      public List<StreamConsumersInfo> build(Object data) {
         if (null == data) {
            return null;
         }

         List<StreamConsumersInfo> list = new ArrayList<>();

         for (List<Object> groupInfo : (List)data) {
            Iterator<Object> consumerInfoIterator = groupInfo.iterator();
            StreamConsumersInfo streamGroupInfo = new StreamConsumersInfo(
               BuilderFactory.createMapFromDecodingFunctions(consumerInfoIterator, this.mappingFunctions)
            );
            list.add(streamGroupInfo);
         }

         return list;
      }

      @Override
      public String toString() {
         return "List<StreamConsumersInfo>";
      }
   };
   public static final Builder<List<StreamConsumerInfo>> STREAM_CONSUMER_INFO_LIST = new Builder<List<StreamConsumerInfo>>() {
      Map<String, Builder> mappingFunctions = this.createDecoderMap();

      private Map<String, Builder> createDecoderMap() {
         Map<String, Builder> tempMappingFunctions = new HashMap<>();
         tempMappingFunctions.put("name", BuilderFactory.STRING);
         tempMappingFunctions.put("idle", BuilderFactory.LONG);
         tempMappingFunctions.put("pending", BuilderFactory.LONG);
         return tempMappingFunctions;
      }

      public List<StreamConsumerInfo> build(Object data) {
         if (null == data) {
            return null;
         }

         List<StreamConsumerInfo> list = new ArrayList<>();

         for (List<Object> groupInfo : (List)data) {
            Iterator<Object> consumerInfoIterator = groupInfo.iterator();
            StreamConsumerInfo streamConsumerInfo = new StreamConsumerInfo(
               BuilderFactory.createMapFromDecodingFunctions(consumerInfoIterator, this.mappingFunctions)
            );
            list.add(streamConsumerInfo);
         }

         return list;
      }

      @Override
      public String toString() {
         return "List<StreamConsumerInfo>";
      }
   };
   private static final Builder<List<StreamConsumerFullInfo>> STREAM_CONSUMER_FULL_INFO_LIST = new Builder<List<StreamConsumerFullInfo>>() {
      final Map<String, Builder> mappingFunctions = this.createDecoderMap();

      private Map<String, Builder> createDecoderMap() {
         Map<String, Builder> tempMappingFunctions = new HashMap<>();
         tempMappingFunctions.put("name", BuilderFactory.STRING);
         tempMappingFunctions.put("seen-time", BuilderFactory.LONG);
         tempMappingFunctions.put("pel-count", BuilderFactory.LONG);
         tempMappingFunctions.put("pending", BuilderFactory.ENCODED_OBJECT_LIST);
         return tempMappingFunctions;
      }

      public List<StreamConsumerFullInfo> build(Object data) {
         if (null == data) {
            return null;
         }

         List<StreamConsumerFullInfo> list = new ArrayList<>();

         for (Object streamsEntry : (List)data) {
            List<Object> consumerInfoList = (List<Object>)streamsEntry;
            Iterator<Object> consumerInfoIterator = consumerInfoList.iterator();
            StreamConsumerFullInfo consumerInfo = new StreamConsumerFullInfo(
               BuilderFactory.createMapFromDecodingFunctions(consumerInfoIterator, this.mappingFunctions)
            );
            list.add(consumerInfo);
         }

         return list;
      }

      @Override
      public String toString() {
         return "List<StreamConsumerFullInfo>";
      }
   };
   private static final Builder<List<StreamGroupFullInfo>> STREAM_GROUP_FULL_INFO_LIST = new Builder<List<StreamGroupFullInfo>>() {
      final Map<String, Builder> mappingFunctions = this.createDecoderMap();

      private Map<String, Builder> createDecoderMap() {
         Map<String, Builder> tempMappingFunctions = new HashMap<>();
         tempMappingFunctions.put("name", BuilderFactory.STRING);
         tempMappingFunctions.put("consumers", BuilderFactory.STREAM_CONSUMER_FULL_INFO_LIST);
         tempMappingFunctions.put("pending", BuilderFactory.ENCODED_OBJECT_LIST);
         tempMappingFunctions.put("last-delivered-id", BuilderFactory.STREAM_ENTRY_ID);
         tempMappingFunctions.put("pel-count", BuilderFactory.LONG);
         return tempMappingFunctions;
      }

      public List<StreamGroupFullInfo> build(Object data) {
         if (null == data) {
            return null;
         }

         List<StreamGroupFullInfo> list = new ArrayList<>();

         for (Object streamsEntry : (List)data) {
            List<Object> groupInfo = (List<Object>)streamsEntry;
            Iterator<Object> groupInfoIterator = groupInfo.iterator();
            StreamGroupFullInfo groupFullInfo = new StreamGroupFullInfo(BuilderFactory.createMapFromDecodingFunctions(groupInfoIterator, this.mappingFunctions));
            list.add(groupFullInfo);
         }

         return list;
      }

      @Override
      public String toString() {
         return "List<StreamGroupFullInfo>";
      }
   };
   public static final Builder<StreamFullInfo> STREAM_FULL_INFO = new Builder<StreamFullInfo>() {
      final Map<String, Builder> mappingFunctions = this.createDecoderMap();

      private Map<String, Builder> createDecoderMap() {
         Map<String, Builder> tempMappingFunctions = new HashMap<>();
         tempMappingFunctions.put("last-generated-id", BuilderFactory.STREAM_ENTRY_ID);
         tempMappingFunctions.put("length", BuilderFactory.LONG);
         tempMappingFunctions.put("radix-tree-keys", BuilderFactory.LONG);
         tempMappingFunctions.put("radix-tree-nodes", BuilderFactory.LONG);
         tempMappingFunctions.put("groups", BuilderFactory.STREAM_GROUP_FULL_INFO_LIST);
         tempMappingFunctions.put("entries", BuilderFactory.STREAM_ENTRY_LIST);
         return tempMappingFunctions;
      }

      public StreamFullInfo build(Object data) {
         if (null == data) {
            return null;
         }

         List<Object> streamsEntries = (List<Object>)data;
         Iterator<Object> iterator = streamsEntries.iterator();
         return new StreamFullInfo(BuilderFactory.createMapFromDecodingFunctions(iterator, this.mappingFunctions));
      }

      @Override
      public String toString() {
         return "StreamFullInfo";
      }
   };
   @Deprecated
   public static final Builder<StreamFullInfo> STREAM_INFO_FULL = STREAM_FULL_INFO;
   public static final Builder<StreamPendingSummary> STREAM_PENDING_SUMMARY = new Builder<StreamPendingSummary>() {
      public StreamPendingSummary build(Object data) {
         if (null == data) {
            return null;
         }

         List<Object> objectList = (List<Object>)data;
         long total = BuilderFactory.LONG.build(objectList.get(0));
         StreamEntryID minId = BuilderFactory.STREAM_ENTRY_ID.build(objectList.get(1));
         StreamEntryID maxId = BuilderFactory.STREAM_ENTRY_ID.build(objectList.get(2));
         Map<String, Long> map = objectList.get(3) == null
            ? null
            : ((List)objectList.get(3))
               .stream()
               .collect(Collectors.toMap(pair -> BuilderFactory.STRING.build(pair.get(0)), pair -> Long.parseLong(BuilderFactory.STRING.build(pair.get(1)))));
         return new StreamPendingSummary(total, minId, maxId, map);
      }

      @Override
      public String toString() {
         return "StreamPendingSummary";
      }
   };
   private static final List<Builder> BACKUP_BUILDERS_FOR_DECODING_FUNCTIONS = Arrays.asList(STRING, LONG, DOUBLE);
   public static final Builder<LCSMatchResult> STR_ALGO_LCS_RESULT_BUILDER = new Builder<LCSMatchResult>() {
      public LCSMatchResult build(Object data) {
         if (data == null) {
            return null;
         }

         if (data instanceof byte[]) {
            return new LCSMatchResult(BuilderFactory.STRING.build(data));
         }

         if (data instanceof Long) {
            return new LCSMatchResult(BuilderFactory.LONG.build(data));
         }

         long len = 0L;
         List<LCSMatchResult.MatchedPosition> matchedPositions = new ArrayList<>();
         List<Object> objectList = (List<Object>)data;
         if (objectList.get(0) instanceof KeyValue) {
            for (KeyValue kv : objectList) {
               if ("matches".equalsIgnoreCase(BuilderFactory.STRING.build(kv.getKey()))) {
                  this.addMatchedPosition(matchedPositions, kv.getValue());
               } else if ("len".equalsIgnoreCase(BuilderFactory.STRING.build(kv.getKey()))) {
                  len = BuilderFactory.LONG.build(kv.getValue());
               }
            }
         } else {
            for (int i = 0; i < objectList.size(); i += 2) {
               if ("matches".equalsIgnoreCase(BuilderFactory.STRING.build(objectList.get(i)))) {
                  this.addMatchedPosition(matchedPositions, objectList.get(i + 1));
               } else if ("len".equalsIgnoreCase(BuilderFactory.STRING.build(objectList.get(i)))) {
                  len = BuilderFactory.LONG.build(objectList.get(i + 1));
               }
            }
         }

         return new LCSMatchResult(matchedPositions, len);
      }

      private void addMatchedPosition(List<LCSMatchResult.MatchedPosition> matchedPositions, Object o) {
         for (Object obj : (List)o) {
            if (obj instanceof List) {
               List<Object> positions = (List<Object>)obj;
               LCSMatchResult.Position a = new LCSMatchResult.Position(
                  BuilderFactory.LONG.build(((List)positions.get(0)).get(0)), BuilderFactory.LONG.build(((List)positions.get(0)).get(1))
               );
               LCSMatchResult.Position b = new LCSMatchResult.Position(
                  BuilderFactory.LONG.build(((List)positions.get(1)).get(0)), BuilderFactory.LONG.build(((List)positions.get(1)).get(1))
               );
               long matchLen = 0L;
               if (positions.size() >= 3) {
                  matchLen = BuilderFactory.LONG.build(positions.get(2));
               }

               matchedPositions.add(new LCSMatchResult.MatchedPosition(a, b, matchLen));
            }
         }
      }
   };
   public static final Builder<Map<String, String>> STRING_MAP_FROM_PAIRS = new Builder<Map<String, String>>() {
      public Map<String, String> build(Object data) {
         List list = (List)data;
         if (list.isEmpty()) {
            return Collections.emptyMap();
         }

         if (list.get(0) instanceof KeyValue) {
            return list.stream().collect(Collectors.toMap(kv -> BuilderFactory.STRING.build(kv.getKey()), kv -> BuilderFactory.STRING.build(kv.getValue())));
         }

         Map<String, String> map = new HashMap<>(list.size());

         for (Object object : list) {
            if (object != null) {
               List<Object> flat = (List<Object>)object;
               if (!flat.isEmpty()) {
                  map.put(BuilderFactory.STRING.build(flat.get(0)), BuilderFactory.STRING.build(flat.get(1)));
               }
            }
         }

         return map;
      }

      @Override
      public String toString() {
         return "Map<String, String>";
      }
   };
   public static final Builder<Map<String, Object>> ENCODED_OBJECT_MAP_FROM_PAIRS = new Builder<Map<String, Object>>() {
      public Map<String, Object> build(Object data) {
         List list = (List)data;
         if (list.isEmpty()) {
            return Collections.emptyMap();
         }

         if (list.get(0) instanceof KeyValue) {
            return list.stream()
               .collect(Collectors.toMap(kv -> BuilderFactory.STRING.build(kv.getKey()), kv -> BuilderFactory.ENCODED_OBJECT.build(kv.getValue())));
         }

         Map<String, Object> map = new HashMap<>(list.size());

         for (Object object : list) {
            if (object != null) {
               List<Object> flat = (List<Object>)object;
               if (!flat.isEmpty()) {
                  map.put(BuilderFactory.STRING.build(flat.get(0)), BuilderFactory.STRING.build(flat.get(1)));
               }
            }
         }

         return map;
      }

      @Override
      public String toString() {
         return "Map<String, String>";
      }
   };
   @Deprecated
   public static final Builder<List<LibraryInfo>> LIBRARY_LIST = LibraryInfo.LIBRARY_INFO_LIST;
   public static final Builder<List<List<String>>> STRING_LIST_LIST = new Builder<List<List<String>>>() {
      public List<List<String>> build(Object data) {
         return null == data ? null : ((List)data).stream().map(BuilderFactory.STRING_LIST::build).collect(Collectors.toList());
      }

      @Override
      public String toString() {
         return "List<List<String>>";
      }
   };
   public static final Builder<List<List<Object>>> ENCODED_OBJECT_LIST_LIST = new Builder<List<List<Object>>>() {
      public List<List<Object>> build(Object data) {
         return null == data ? null : ((List)data).stream().map(BuilderFactory.ENCODED_OBJECT_LIST::build).collect(Collectors.toList());
      }

      @Override
      public String toString() {
         return "List<List<Object>>";
      }
   };

   private static Map<String, Object> createMapFromDecodingFunctions(Iterator<Object> iterator, Map<String, Builder> mappingFunctions) {
      return createMapFromDecodingFunctions(iterator, mappingFunctions, null);
   }

   private static Map<String, Object> createMapFromDecodingFunctions(
      Iterator<Object> iterator, Map<String, Builder> mappingFunctions, Collection<Builder> backupBuilders
   ) {
      if (!iterator.hasNext()) {
         return Collections.emptyMap();
      }

      Map<String, Object> resultMap = new HashMap<>();

      while (iterator.hasNext()) {
         Object tempObject = iterator.next();
         String mapKey;
         Object rawValue;
         if (tempObject instanceof KeyValue) {
            KeyValue kv = (KeyValue)tempObject;
            mapKey = STRING.build(kv.getKey());
            rawValue = kv.getValue();
         } else {
            mapKey = STRING.build(tempObject);
            rawValue = iterator.next();
         }

         if (mappingFunctions.containsKey(mapKey)) {
            resultMap.put(mapKey, mappingFunctions.get(mapKey).build(rawValue));
         } else {
            for (Builder b : backupBuilders != null ? backupBuilders : mappingFunctions.values()) {
               try {
                  resultMap.put(mapKey, b.build(rawValue));
                  break;
               } catch (ClassCastException var11) {
               }
            }
         }
      }

      return resultMap;
   }

   private BuilderFactory() {
      throw new InstantiationError("Must not instantiate this class");
   }

   protected static class SetFromList<E> extends AbstractSet<E> implements Serializable {
      private static final long serialVersionUID = -2850347066962734052L;
      private final List<E> list;

      private SetFromList(List<E> list) {
         this.list = list;
      }

      @Override
      public void clear() {
         this.list.clear();
      }

      @Override
      public int size() {
         return this.list.size();
      }

      @Override
      public boolean isEmpty() {
         return this.list.isEmpty();
      }

      @Override
      public boolean contains(Object o) {
         return this.list.contains(o);
      }

      @Override
      public boolean remove(Object o) {
         return this.list.remove(o);
      }

      @Override
      public boolean add(E e) {
         return !this.contains(e) && this.list.add(e);
      }

      @Override
      public Iterator<E> iterator() {
         return this.list.iterator();
      }

      @Override
      public Object[] toArray() {
         return this.list.toArray();
      }

      @Override
      public <T> T[] toArray(T[] a) {
         return (T[])this.list.toArray(a);
      }

      @Override
      public String toString() {
         return this.list.toString();
      }

      @Override
      public int hashCode() {
         return this.list.hashCode();
      }

      @Override
      public boolean equals(Object o) {
         if (o == null) {
            return false;
         }

         if (o == this) {
            return true;
         }

         if (!(o instanceof Set)) {
            return false;
         }

         Collection<?> c = (Collection<?>)o;
         return c.size() != this.size() ? false : this.containsAll(c);
      }

      @Override
      public boolean containsAll(Collection<?> c) {
         return this.list.containsAll(c);
      }

      @Override
      public boolean removeAll(Collection<?> c) {
         return this.list.removeAll(c);
      }

      @Override
      public boolean retainAll(Collection<?> c) {
         return this.list.retainAll(c);
      }

      protected static <E> BuilderFactory.SetFromList<E> of(List<E> list) {
         return list == null ? null : new BuilderFactory.SetFromList<>(list);
      }
   }
}
