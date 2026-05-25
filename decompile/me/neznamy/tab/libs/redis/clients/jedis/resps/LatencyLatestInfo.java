package me.neznamy.tab.libs.redis.clients.jedis.resps;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.Builder;
import me.neznamy.tab.libs.redis.clients.jedis.BuilderFactory;

public class LatencyLatestInfo {
   private final String command;
   private final long timestamp;
   private final long lastEventLatency;
   private final long maxEventLatency;
   public static final Builder<LatencyLatestInfo> LATENCY_LATEST_BUILDER = new Builder<LatencyLatestInfo>() {
      public LatencyLatestInfo build(Object data) {
         List<Object> commandData = (List<Object>)data;
         String command = BuilderFactory.STRING.build(commandData.get(0));
         long timestamp = BuilderFactory.LONG.build(commandData.get(1));
         long lastEventLatency = BuilderFactory.LONG.build(commandData.get(2));
         long maxEventLatency = BuilderFactory.LONG.build(commandData.get(3));
         return new LatencyLatestInfo(command, timestamp, lastEventLatency, maxEventLatency);
      }
   };

   public LatencyLatestInfo(String command, long timestamp, long lastEventLatency, long maxEventLatency) {
      this.command = command;
      this.timestamp = timestamp;
      this.lastEventLatency = lastEventLatency;
      this.maxEventLatency = maxEventLatency;
   }

   public String getCommand() {
      return this.command;
   }

   public long getTimestamp() {
      return this.timestamp;
   }

   public long getLastEventLatency() {
      return this.lastEventLatency;
   }

   public long getMaxEventLatency() {
      return this.maxEventLatency;
   }
}
