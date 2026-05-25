package me.neznamy.tab.libs.redis.clients.jedis.resps;

import java.util.ArrayList;
import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.BuilderFactory;
import me.neznamy.tab.libs.redis.clients.jedis.HostAndPort;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public class Slowlog {
   private final long id;
   private final long timeStamp;
   private final long executionTime;
   private final List<String> args;
   private HostAndPort clientIpPort;
   private String clientName;
   private static final String COMMA = ",";

   private Slowlog(List<Object> properties) {
      this.id = (Long)properties.get(0);
      this.timeStamp = (Long)properties.get(1);
      this.executionTime = (Long)properties.get(2);
      this.args = BuilderFactory.STRING_LIST.build(properties.get(3));
      if (properties.size() != 4) {
         this.clientIpPort = HostAndPort.from(SafeEncoder.encode((byte[])properties.get(4)));
         this.clientName = SafeEncoder.encode((byte[])properties.get(5));
      }
   }

   public static List<Slowlog> from(List<Object> nestedMultiBulkReply) {
      List<Slowlog> logs = new ArrayList<>(nestedMultiBulkReply.size());

      for (Object obj : nestedMultiBulkReply) {
         List<Object> properties = (List<Object>)obj;
         logs.add(new Slowlog(properties));
      }

      return logs;
   }

   public long getId() {
      return this.id;
   }

   public long getTimeStamp() {
      return this.timeStamp;
   }

   public long getExecutionTime() {
      return this.executionTime;
   }

   public List<String> getArgs() {
      return this.args;
   }

   public HostAndPort getClientIpPort() {
      return this.clientIpPort;
   }

   public String getClientName() {
      return this.clientName;
   }

   @Override
   public String toString() {
      return this.id + "," + this.timeStamp + "," + this.executionTime + "," + this.args;
   }
}
