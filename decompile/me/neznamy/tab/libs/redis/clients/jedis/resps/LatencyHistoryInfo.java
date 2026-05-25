package me.neznamy.tab.libs.redis.clients.jedis.resps;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.Builder;
import me.neznamy.tab.libs.redis.clients.jedis.BuilderFactory;

public class LatencyHistoryInfo {
   private final long timestamp;
   private final long latency;
   public static final Builder<LatencyHistoryInfo> LATENCY_HISTORY_BUILDER = new Builder<LatencyHistoryInfo>() {
      public LatencyHistoryInfo build(Object data) {
         List<Object> commandData = (List<Object>)data;
         long timestamp = BuilderFactory.LONG.build(commandData.get(0));
         long latency = BuilderFactory.LONG.build(commandData.get(1));
         return new LatencyHistoryInfo(timestamp, latency);
      }
   };

   public LatencyHistoryInfo(long timestamp, long latency) {
      this.timestamp = timestamp;
      this.latency = latency;
   }

   public long getTimestamp() {
      return this.timestamp;
   }

   public long getLatency() {
      return this.latency;
   }
}
