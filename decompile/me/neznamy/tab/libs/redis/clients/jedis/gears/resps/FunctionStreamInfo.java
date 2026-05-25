package me.neznamy.tab.libs.redis.clients.jedis.gears.resps;

import java.util.List;
import java.util.stream.Collectors;
import me.neznamy.tab.libs.redis.clients.jedis.Builder;
import me.neznamy.tab.libs.redis.clients.jedis.BuilderFactory;

@Deprecated
public class FunctionStreamInfo {
   private final String name;
   private final String idToReadFrom;
   private final String lastError;
   private final long lastLag;
   private final long lastProcessedTime;
   private final long totalLag;
   private final long totalProcessedTime;
   private final long totalRecordProcessed;
   private final List<String> pendingIds;
   @Deprecated
   public static final Builder<List<FunctionStreamInfo>> STREAM_INFO_LIST = new Builder<List<FunctionStreamInfo>>() {
      public List<FunctionStreamInfo> build(Object data) {
         return ((List)data)
            .stream()
            .map(pairObject -> (List)pairObject)
            .map(
               pairList -> new FunctionStreamInfo(
                  BuilderFactory.STRING.build(pairList.get(9)),
                  BuilderFactory.STRING.build(pairList.get(1)),
                  BuilderFactory.STRING.build(pairList.get(3)),
                  BuilderFactory.LONG.build(pairList.get(7)),
                  BuilderFactory.LONG.build(pairList.get(5)),
                  BuilderFactory.LONG.build(pairList.get(13)),
                  BuilderFactory.LONG.build(pairList.get(15)),
                  BuilderFactory.LONG.build(pairList.get(17)),
                  BuilderFactory.STRING_LIST.build(pairList.get(11))
               )
            )
            .collect(Collectors.toList());
      }
   };

   public String getName() {
      return this.name;
   }

   public String getIdToReadFrom() {
      return this.idToReadFrom;
   }

   public String getLastError() {
      return this.lastError;
   }

   public long getLastLag() {
      return this.lastLag;
   }

   public long getLastProcessedTime() {
      return this.lastProcessedTime;
   }

   public long getTotalLag() {
      return this.totalLag;
   }

   public long getTotalProcessedTime() {
      return this.totalProcessedTime;
   }

   public long getTotalRecordProcessed() {
      return this.totalRecordProcessed;
   }

   public List<String> getPendingIds() {
      return this.pendingIds;
   }

   public FunctionStreamInfo(
      String name,
      String idToReadFrom,
      String lastError,
      long lastProcessedTime,
      long lastLag,
      long totalLag,
      long totalProcessedTime,
      long totalRecordProcessed,
      List<String> pendingIds
   ) {
      this.name = name;
      this.idToReadFrom = idToReadFrom;
      this.lastError = lastError;
      this.lastProcessedTime = lastProcessedTime;
      this.lastLag = lastLag;
      this.totalLag = totalLag;
      this.totalProcessedTime = totalProcessedTime;
      this.totalRecordProcessed = totalRecordProcessed;
      this.pendingIds = pendingIds;
   }
}
