package me.neznamy.tab.libs.redis.clients.jedis.resps;

import java.io.Serializable;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.StreamEntryID;

public class StreamGroupInfo implements Serializable {
   public static final String NAME = "name";
   public static final String CONSUMERS = "consumers";
   public static final String PENDING = "pending";
   public static final String LAST_DELIVERED = "last-delivered-id";
   private final String name;
   private final long consumers;
   private final long pending;
   private final StreamEntryID lastDeliveredId;
   private final Map<String, Object> groupInfo;

   public StreamGroupInfo(Map<String, Object> map) {
      this.groupInfo = map;
      this.name = (String)map.get("name");
      this.consumers = (Long)map.get("consumers");
      this.pending = (Long)map.get("pending");
      this.lastDeliveredId = (StreamEntryID)map.get("last-delivered-id");
   }

   public String getName() {
      return this.name;
   }

   public long getConsumers() {
      return this.consumers;
   }

   public long getPending() {
      return this.pending;
   }

   public StreamEntryID getLastDeliveredId() {
      return this.lastDeliveredId;
   }

   public Map<String, Object> getGroupInfo() {
      return this.groupInfo;
   }
}
