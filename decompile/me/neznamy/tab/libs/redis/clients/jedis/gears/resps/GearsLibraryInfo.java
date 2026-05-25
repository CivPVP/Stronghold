package me.neznamy.tab.libs.redis.clients.jedis.gears.resps;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import me.neznamy.tab.libs.redis.clients.jedis.Builder;
import me.neznamy.tab.libs.redis.clients.jedis.BuilderFactory;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

@Deprecated
public class GearsLibraryInfo {
   private final String apiVersion;
   private final List<String> clusterFunctions;
   private final String code;
   private final String configuration;
   private final String engine;
   private final List<FunctionInfo> functions;
   private final List<TriggerInfo> keyspaceTriggers;
   private final String name;
   private final List<String> pendingAsyncCalls;
   private final long pendingJobs;
   private final List<StreamTriggerInfo> streamTriggers;
   private final String user;
   @Deprecated
   public static final Builder<GearsLibraryInfo> GEARS_LIBRARY_INFO = new Builder<GearsLibraryInfo>() {
      public GearsLibraryInfo build(Object data) {
         if (data == null) {
            return null;
         }

         List list = (List)data;
         if (list.isEmpty()) {
            return null;
         }

         String apiVersion = null;
         List<String> clusterFunctions = Collections.emptyList();
         String code = null;
         String configuration = null;
         String engine = null;
         List<FunctionInfo> functions = Collections.emptyList();
         List<TriggerInfo> keyspaceTriggers = Collections.emptyList();
         String name = null;
         List<String> pendingAsyncCalls = null;
         long pendingJobs = 0L;
         List<StreamTriggerInfo> streamTriggers = Collections.emptyList();
         String user = null;
         if (list.get(0) instanceof KeyValue) {
            for (KeyValue kv : list) {
               switch ((String)BuilderFactory.STRING.build(kv.getKey())) {
                  case "api_version":
                     apiVersion = BuilderFactory.STRING.build(kv.getValue());
                     break;
                  case "cluster_functions":
                     clusterFunctions = BuilderFactory.STRING_LIST.build(kv.getValue());
                     break;
                  case "configuration":
                     configuration = BuilderFactory.STRING.build(kv.getValue());
                     break;
                  case "engine":
                     engine = BuilderFactory.STRING.build(kv.getValue());
                     break;
                  case "functions":
                     functions = FunctionInfo.FUNCTION_INFO_LIST.build(kv.getValue());
                     break;
                  case "keyspace_triggers":
                     keyspaceTriggers = TriggerInfo.KEYSPACE_TRIGGER_INFO_LIST.build(kv.getValue());
                     break;
                  case "name":
                     name = BuilderFactory.STRING.build(kv.getValue());
                     break;
                  case "pending_async_calls":
                     pendingAsyncCalls = BuilderFactory.STRING_LIST.build(kv.getValue());
                     break;
                  case "pending_jobs":
                     pendingJobs = BuilderFactory.LONG.build(kv.getValue());
                     break;
                  case "stream_triggers":
                     streamTriggers = StreamTriggerInfo.STREAM_TRIGGER_INFO_LIST.build(kv.getValue());
                     break;
                  case "user":
                     user = BuilderFactory.STRING.build(kv.getValue());
                     break;
                  case "code":
                     code = BuilderFactory.STRING.build(kv.getValue());
               }
            }
         } else {
            boolean withCode = list.size() > 23;
            int offset = withCode ? 2 : 0;
            apiVersion = BuilderFactory.STRING.build(list.get(1));
            clusterFunctions = BuilderFactory.STRING_LIST.build(list.get(3));
            code = withCode ? BuilderFactory.STRING.build(list.get(5)) : null;
            configuration = BuilderFactory.STRING.build(list.get(5 + offset));
            engine = BuilderFactory.STRING.build(list.get(7 + offset));
            functions = FunctionInfo.FUNCTION_INFO_LIST.build(list.get(9 + offset));
            keyspaceTriggers = TriggerInfo.KEYSPACE_TRIGGER_INFO_LIST.build(list.get(11 + offset));
            name = BuilderFactory.STRING.build(list.get(13 + offset));
            pendingAsyncCalls = BuilderFactory.STRING_LIST.build(list.get(15 + offset));
            pendingJobs = BuilderFactory.LONG.build(list.get(17 + offset));
            streamTriggers = StreamTriggerInfo.STREAM_TRIGGER_INFO_LIST.build(list.get(19 + offset));
            user = BuilderFactory.STRING.build(list.get(21 + offset));
         }

         return new GearsLibraryInfo(
            apiVersion, clusterFunctions, code, configuration, engine, functions, keyspaceTriggers, name, pendingAsyncCalls, pendingJobs, streamTriggers, user
         );
      }
   };
   @Deprecated
   public static final Builder<List<GearsLibraryInfo>> GEARS_LIBRARY_INFO_LIST = new Builder<List<GearsLibraryInfo>>() {
      public List<GearsLibraryInfo> build(Object data) {
         List<Object> list = (List<Object>)data;
         return list.stream().map(o -> GearsLibraryInfo.GEARS_LIBRARY_INFO.build(o)).collect(Collectors.toList());
      }
   };

   public GearsLibraryInfo(
      String apiVersion,
      List<String> clusterFunctions,
      String code,
      String configuration,
      String engine,
      List<FunctionInfo> functions,
      List<TriggerInfo> keyspaceTriggers,
      String name,
      List<String> pendingAsyncCalls,
      long pendingJobs,
      List<StreamTriggerInfo> streamTriggers,
      String user
   ) {
      this.apiVersion = apiVersion;
      this.clusterFunctions = clusterFunctions;
      this.code = code;
      this.configuration = configuration;
      this.engine = engine;
      this.functions = functions;
      this.keyspaceTriggers = keyspaceTriggers;
      this.name = name;
      this.pendingAsyncCalls = pendingAsyncCalls;
      this.pendingJobs = pendingJobs;
      this.streamTriggers = streamTriggers;
      this.user = user;
   }

   public String getApiVersion() {
      return this.apiVersion;
   }

   public List<String> getClusterFunctions() {
      return this.clusterFunctions;
   }

   public String getCode() {
      return this.code;
   }

   public String getConfiguration() {
      return this.configuration;
   }

   public String getEngine() {
      return this.engine;
   }

   public List<FunctionInfo> getFunctions() {
      return this.functions;
   }

   public List<TriggerInfo> getKeyspaceTriggers() {
      return this.keyspaceTriggers;
   }

   public String getName() {
      return this.name;
   }

   public List<String> getPendingAsyncCalls() {
      return this.pendingAsyncCalls;
   }

   public long getPendingJobs() {
      return this.pendingJobs;
   }

   public List<StreamTriggerInfo> getStreamTriggers() {
      return this.streamTriggers;
   }

   public String getUser() {
      return this.user;
   }
}
