package me.neznamy.tab.libs.redis.clients.jedis.resps;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class AccessControlLogEntry implements Serializable {
   private static final long serialVersionUID = 1L;
   public static final String COUNT = "count";
   public static final String REASON = "reason";
   public static final String CONTEXT = "context";
   public static final String OBJECT = "object";
   public static final String USERNAME = "username";
   public static final String AGE_SECONDS = "age-seconds";
   public static final String CLIENT_INFO = "client-info";
   public static final String ENTRY_ID = "entry-id";
   public static final String TIMESTAMP_CREATED = "timestamp-created";
   public static final String TIMESTAMP_LAST_UPDATED = "timestamp-last-updated";
   private final long count;
   private final String reason;
   private final String context;
   private final String object;
   private final String username;
   private final Double ageSeconds;
   private final Map<String, String> clientInfo;
   private final Map<String, Object> logEntry;
   private final long entryId;
   private final long timestampCreated;
   private final long timestampLastUpdated;

   public AccessControlLogEntry(Map<String, Object> map) {
      this.count = (Long)map.get("count");
      this.reason = (String)map.get("reason");
      this.context = (String)map.get("context");
      this.object = (String)map.get("object");
      this.username = (String)map.get("username");
      this.ageSeconds = (Double)map.get("age-seconds");
      this.clientInfo = this.getMapFromRawClientInfo((String)map.get("client-info"));
      this.logEntry = map;
      this.entryId = (Long)map.get("entry-id");
      this.timestampCreated = (Long)map.get("timestamp-created");
      this.timestampLastUpdated = (Long)map.get("timestamp-last-updated");
   }

   public long getCount() {
      return this.count;
   }

   public String getReason() {
      return this.reason;
   }

   public String getContext() {
      return this.context;
   }

   public String getObject() {
      return this.object;
   }

   public String getUsername() {
      return this.username;
   }

   public Double getAgeSeconds() {
      return this.ageSeconds;
   }

   public Map<String, String> getClientInfo() {
      return this.clientInfo;
   }

   public Map<String, Object> getlogEntry() {
      return this.logEntry;
   }

   public long getEntryId() {
      return this.entryId;
   }

   public long getTimestampCreated() {
      return this.timestampCreated;
   }

   public long getTimestampLastUpdated() {
      return this.timestampLastUpdated;
   }

   private Map<String, String> getMapFromRawClientInfo(String clientInfo) {
      String[] entries = clientInfo.split(" ");
      Map<String, String> clientInfoMap = new LinkedHashMap<>(entries.length);

      for (String entry : entries) {
         String[] kvArray = entry.split("=");
         clientInfoMap.put(kvArray[0], kvArray.length == 2 ? kvArray[1] : "");
      }

      return clientInfoMap;
   }

   @Override
   public String toString() {
      return "AccessControlLogEntry{count="
         + this.count
         + ", reason='"
         + this.reason
         + '\''
         + ", context='"
         + this.context
         + '\''
         + ", object='"
         + this.object
         + '\''
         + ", username='"
         + this.username
         + '\''
         + ", ageSeconds='"
         + this.ageSeconds
         + '\''
         + ", clientInfo="
         + this.clientInfo
         + ", entryId="
         + this.entryId
         + ", timestampCreated="
         + this.timestampCreated
         + ", timestampLastUpdated="
         + this.timestampLastUpdated
         + '}';
   }
}
