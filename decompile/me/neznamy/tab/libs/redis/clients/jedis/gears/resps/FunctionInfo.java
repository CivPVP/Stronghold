package me.neznamy.tab.libs.redis.clients.jedis.gears.resps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import me.neznamy.tab.libs.redis.clients.jedis.Builder;
import me.neznamy.tab.libs.redis.clients.jedis.BuilderFactory;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

@Deprecated
public class FunctionInfo {
   private final String name;
   private final String description;
   private final boolean isAsync;
   private final List<String> flags;
   @Deprecated
   public static final Builder<List<FunctionInfo>> FUNCTION_INFO_LIST = new Builder<List<FunctionInfo>>() {
      public List<FunctionInfo> build(Object data) {
         List<Object> dataAsList = (List<Object>)data;
         if (!dataAsList.isEmpty()) {
            boolean isListOfList = dataAsList.get(0).getClass().isAssignableFrom(ArrayList.class);
            if (isListOfList) {
               if (((List)((List)data).get(0)).get(0) instanceof KeyValue) {
                  List<List<KeyValue>> dataAsKeyValues = (List<List<KeyValue>>)data;
                  return dataAsKeyValues.stream().map(keyValues -> {
                     String name = null;
                     String description = null;
                     List<String> flags = Collections.emptyList();
                     boolean isAsync = false;

                     for (KeyValue kv : keyValues) {
                        switch ((String)BuilderFactory.STRING.build(kv.getKey())) {
                           case "name":
                              name = BuilderFactory.STRING.build(kv.getValue());
                              break;
                           case "description":
                              description = BuilderFactory.STRING.build(kv.getValue());
                              break;
                           case "raw-arguments":
                              flags = BuilderFactory.STRING_LIST.build(kv.getValue());
                              break;
                           case "is_async":
                              isAsync = BuilderFactory.BOOLEAN.build(kv.getValue());
                        }
                     }

                     return new FunctionInfo(name, description, isAsync, flags);
                  }).collect(Collectors.toList());
               } else {
                  return dataAsList.stream()
                     .map(pairObject -> (List)pairObject)
                     .map(
                        pairList -> new FunctionInfo(
                           BuilderFactory.STRING.build(pairList.get(7)),
                           BuilderFactory.STRING.build(pairList.get(1)),
                           BuilderFactory.BOOLEAN.build(pairList.get(5)),
                           BuilderFactory.STRING_LIST.build(pairList.get(3))
                        )
                     )
                     .collect(Collectors.toList());
               }
            } else {
               return dataAsList.stream().map(BuilderFactory.STRING::build).map(name -> new FunctionInfo(name, null, false, null)).collect(Collectors.toList());
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

   public boolean isAsync() {
      return this.isAsync;
   }

   public List<String> getFlags() {
      return this.flags;
   }

   public FunctionInfo(String name, String description, boolean isAsync, List<String> flags) {
      this.name = name;
      this.description = description;
      this.isAsync = isAsync;
      this.flags = flags;
   }
}
