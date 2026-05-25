package me.neznamy.tab.libs.redis.clients.jedis.resps;

import java.util.Map;

public class StreamConsumerInfo {
   public static final String NAME = "name";
   public static final String IDLE = "idle";
   public static final String PENDING = "pending";
   public static final String INACTIVE = "inactive";
   private final String name;
   private final long idle;
   private final long pending;
   private final Long inactive;
   private final Map<String, Object> consumerInfo;

   public StreamConsumerInfo(Map<String, Object> map) {
      this.consumerInfo = map;
      this.name = (String)map.get("name");
      this.idle = (Long)map.get("idle");
      this.pending = (Long)map.get("pending");
      this.inactive = (Long)map.get("inactive");
   }

   public String getName() {
      return this.name;
   }

   public long getIdle() {
      return this.idle;
   }

   public long getPending() {
      return this.pending;
   }

   public Long getInactive() {
      return this.inactive;
   }

   public Map<String, Object> getConsumerInfo() {
      return this.consumerInfo;
   }
}
