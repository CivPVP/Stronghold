package me.neznamy.tab.libs.redis.clients.jedis.resps;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.StreamEntryID;

public class StreamGroupFullInfo implements Serializable {
   public static final String NAME = "name";
   public static final String CONSUMERS = "consumers";
   public static final String PENDING = "pending";
   public static final String LAST_DELIVERED = "last-delivered-id";
   public static final String PEL_COUNT = "pel-count";
   private final String name;
   private final List<StreamConsumerFullInfo> consumers;
   private final List<List<Object>> pending;
   private final Long pelCount;
   private final StreamEntryID lastDeliveredId;
   private final Map<String, Object> groupFullInfo;

   public StreamGroupFullInfo(Map<String, Object> map) {
      this.groupFullInfo = map;
      this.name = (String)map.get("name");
      this.consumers = (List<StreamConsumerFullInfo>)map.get("consumers");
      this.pending = (List<List<Object>>)map.get("pending");
      this.lastDeliveredId = (StreamEntryID)map.get("last-delivered-id");
      this.pelCount = (Long)map.get("pel-count");
      this.pending.stream().forEach(entry -> entry.set(0, new StreamEntryID((String)entry.get(0))));
   }

   public String getName() {
      return this.name;
   }

   public List<StreamConsumerFullInfo> getConsumers() {
      return this.consumers;
   }

   public List<List<Object>> getPending() {
      return this.pending;
   }

   public StreamEntryID getLastDeliveredId() {
      return this.lastDeliveredId;
   }

   public Map<String, Object> getGroupFullInfo() {
      return this.groupFullInfo;
   }

   public Long getPelCount() {
      return this.pelCount;
   }
}
