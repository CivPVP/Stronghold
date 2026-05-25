package me.neznamy.tab.libs.redis.clients.jedis.gears.resps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import me.neznamy.tab.libs.redis.clients.jedis.Builder;
import me.neznamy.tab.libs.redis.clients.jedis.BuilderFactory;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

@Deprecated
public class TriggerInfo {
   private final String name;
   private final String description;
   private final String lastError;
   private final long lastExecutionTime;
   private final long numFailed;
   private final long numFinished;
   private final long numSuccess;
   private final long numTrigger;
   private final long totalExecutionTime;
   @Deprecated
   public static final Builder<List<TriggerInfo>> KEYSPACE_TRIGGER_INFO_LIST = new Builder<List<TriggerInfo>>() {
      public List<TriggerInfo> build(Object data) {
         List<Object> dataAsList = (List<Object>)data;
         if (!dataAsList.isEmpty()) {
            boolean isListOfList = dataAsList.get(0).getClass().isAssignableFrom(ArrayList.class);
            if (isListOfList) {
               if (((List)((List)data).get(0)).get(0) instanceof KeyValue) {
                  List<List<KeyValue>> dataAsKeyValues = (List<List<KeyValue>>)data;
                  return dataAsKeyValues.stream()
                     .map(
                        keyValues -> {
                           String name = null;
                           String description = null;
                           String lastError = null;
                           long lastExecutionTime = 0L;
                           long numFailed = 0L;
                           long numFinished = 0L;
                           long numSuccess = 0L;
                           long numTrigger = 0L;
                           long totalExecutionTime = 0L;

                           for (KeyValue kv : keyValues) {
                              switch ((String)BuilderFactory.STRING.build(kv.getKey())) {
                                 case "name":
                                    name = BuilderFactory.STRING.build(kv.getValue());
                                    break;
                                 case "description":
                                    description = BuilderFactory.STRING.build(kv.getValue());
                                    break;
                                 case "last_error":
                                    lastError = BuilderFactory.STRING.build(kv.getValue());
                                    break;
                                 case "last_execution_time":
                                    lastExecutionTime = BuilderFactory.LONG.build(kv.getValue());
                                    break;
                                 case "num_failed":
                                    numFailed = BuilderFactory.LONG.build(kv.getValue());
                                    break;
                                 case "num_finished":
                                    numFinished = BuilderFactory.LONG.build(kv.getValue());
                                    break;
                                 case "num_success":
                                    numSuccess = BuilderFactory.LONG.build(kv.getValue());
                                    break;
                                 case "num_trigger":
                                    numTrigger = BuilderFactory.LONG.build(kv.getValue());
                                    break;
                                 case "total_execution_time":
                                    totalExecutionTime = BuilderFactory.LONG.build(kv.getValue());
                              }
                           }

                           return new TriggerInfo(
                              name, description, lastError, numFinished, numSuccess, numFailed, numTrigger, lastExecutionTime, totalExecutionTime
                           );
                        }
                     )
                     .collect(Collectors.toList());
               } else {
                  return dataAsList.stream()
                     .map(pairObject -> (List)pairObject)
                     .map(
                        pairList -> new TriggerInfo(
                           BuilderFactory.STRING.build(pairList.get(7)),
                           BuilderFactory.STRING.build(pairList.get(1)),
                           BuilderFactory.STRING.build(pairList.get(3)),
                           BuilderFactory.LONG.build(pairList.get(11)),
                           BuilderFactory.LONG.build(pairList.get(13)),
                           BuilderFactory.LONG.build(pairList.get(9)),
                           BuilderFactory.LONG.build(pairList.get(15)),
                           BuilderFactory.LONG.build(pairList.get(5)),
                           BuilderFactory.LONG.build(pairList.get(17))
                        )
                     )
                     .collect(Collectors.toList());
               }
            } else {
               return dataAsList.stream()
                  .map(BuilderFactory.STRING::build)
                  .map(name -> new TriggerInfo(name, null, null, 0L, 0L, 0L, 0L, 0L, 0L))
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

   public String getLastError() {
      return this.lastError;
   }

   public long getLastExecutionTime() {
      return this.lastExecutionTime;
   }

   public long getNumFailed() {
      return this.numFailed;
   }

   public long getNumFinished() {
      return this.numFinished;
   }

   public long getNumSuccess() {
      return this.numSuccess;
   }

   public long getNumTrigger() {
      return this.numTrigger;
   }

   public long getTotalExecutionTime() {
      return this.totalExecutionTime;
   }

   public TriggerInfo(
      String name,
      String description,
      String lastError,
      long numFinished,
      long numSuccess,
      long numFailed,
      long numTrigger,
      long lastExecutionTime,
      long totalExecutionTime
   ) {
      this.name = name;
      this.description = description;
      this.lastError = lastError;
      this.numFinished = numFinished;
      this.numSuccess = numSuccess;
      this.numFailed = numFailed;
      this.numTrigger = numTrigger;
      this.lastExecutionTime = lastExecutionTime;
      this.totalExecutionTime = totalExecutionTime;
   }
}
