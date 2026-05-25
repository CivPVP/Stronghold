package me.neznamy.tab.libs.redis.clients.jedis.gears.resps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import me.neznamy.tab.libs.redis.clients.jedis.Builder;
import me.neznamy.tab.libs.redis.clients.jedis.BuilderFactory;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

@Deprecated
public class StreamTriggerInfo {
   private final String name;
   private final String description;
   private final String prefix;
   private final boolean trim;
   private final long window;
   private final List<FunctionStreamInfo> streams;
   @Deprecated
   public static final Builder<List<StreamTriggerInfo>> STREAM_TRIGGER_INFO_LIST = new Builder<List<StreamTriggerInfo>>() {
      public List<StreamTriggerInfo> build(Object data) {
         List<Object> dataAsList = (List<Object>)data;
         if (!dataAsList.isEmpty()) {
            boolean isListOfList = dataAsList.get(0).getClass().isAssignableFrom(ArrayList.class);
            if (isListOfList) {
               if (((List)((List)data).get(0)).get(0) instanceof KeyValue) {
                  List<List<KeyValue>> dataAsKeyValues = (List<List<KeyValue>>)data;
                  return dataAsKeyValues.stream().map(keyValues -> {
                     String name = null;
                     String description = null;
                     String prefix = null;
                     long window = 0L;
                     boolean trim = false;
                     List<FunctionStreamInfo> streams = null;

                     for (KeyValue kv : keyValues) {
                        switch ((String)BuilderFactory.STRING.build(kv.getKey())) {
                           case "name":
                              name = BuilderFactory.STRING.build(kv.getValue());
                              break;
                           case "description":
                              description = BuilderFactory.STRING.build(kv.getValue());
                              break;
                           case "prefix":
                              prefix = BuilderFactory.STRING.build(kv.getValue());
                              break;
                           case "window":
                              window = BuilderFactory.LONG.build(kv.getValue());
                              break;
                           case "trim":
                              trim = BuilderFactory.BOOLEAN.build(kv.getValue());
                              break;
                           case "streams":
                              streams = FunctionStreamInfo.STREAM_INFO_LIST.build(kv.getValue());
                        }
                     }

                     return new StreamTriggerInfo(name, description, prefix, window, trim, streams);
                  }).collect(Collectors.toList());
               } else {
                  return dataAsList.stream()
                     .map(pairObject -> (List)pairObject)
                     .map(
                        pairList -> {
                           StreamTriggerInfo result = null;
                           switch (pairList.size()) {
                              case 1:
                                 result = new StreamTriggerInfo(BuilderFactory.STRING.build(pairList.get(0)));
                                 break;
                              case 10:
                                 result = new StreamTriggerInfo(
                                    BuilderFactory.STRING.build(pairList.get(3)),
                                    BuilderFactory.STRING.build(pairList.get(1)),
                                    BuilderFactory.STRING.build(pairList.get(5)),
                                    BuilderFactory.LONG.build(pairList.get(9)),
                                    BuilderFactory.BOOLEAN.build(pairList.get(7))
                                 );
                                 break;
                              case 12:
                                 result = new StreamTriggerInfo(
                                    BuilderFactory.STRING.build(pairList.get(3)),
                                    BuilderFactory.STRING.build(pairList.get(1)),
                                    BuilderFactory.STRING.build(pairList.get(5)),
                                    BuilderFactory.LONG.build(pairList.get(11)),
                                    BuilderFactory.BOOLEAN.build(pairList.get(9)),
                                    FunctionStreamInfo.STREAM_INFO_LIST.build(pairList.get(7))
                                 );
                           }

                           return result;
                        }
                     )
                     .collect(Collectors.toList());
               }
            } else {
               return dataAsList.stream()
                  .map(BuilderFactory.STRING::build)
                  .map(name -> new StreamTriggerInfo(name, null, null, 0L, false))
                  .collect(Collectors.toList());
            }
         } else {
            return Collections.emptyList();
         }
      }
   };

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public String getPrefix() {
      return this.prefix;
   }

   public boolean isTrim() {
      return this.trim;
   }

   public long getWindow() {
      return this.window;
   }

   public List<FunctionStreamInfo> getStreams() {
      return this.streams;
   }

   public StreamTriggerInfo(String name, String description, String prefix, long window, boolean trim, List<FunctionStreamInfo> streams) {
      this.name = name;
      this.description = description;
      this.prefix = prefix;
      this.window = window;
      this.trim = trim;
      this.streams = streams;
   }

   public StreamTriggerInfo(String name) {
      this(name, null, null, 0L, false, Collections.emptyList());
   }

   public StreamTriggerInfo(String name, String description, String prefix, long window, boolean trim) {
      this(name, description, prefix, window, trim, Collections.emptyList());
   }
}
