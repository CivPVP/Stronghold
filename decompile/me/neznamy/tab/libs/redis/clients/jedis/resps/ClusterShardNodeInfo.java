package me.neznamy.tab.libs.redis.clients.jedis.resps;

import java.util.Map;

public class ClusterShardNodeInfo {
   public static final String ID = "id";
   public static final String ENDPOINT = "endpoint";
   public static final String IP = "ip";
   public static final String HOSTNAME = "hostname";
   public static final String PORT = "port";
   public static final String TLS_PORT = "tls-port";
   public static final String ROLE = "role";
   public static final String REPLICATION_OFFSET = "replication-offset";
   public static final String HEALTH = "health";
   private final String id;
   private final String endpoint;
   private final String ip;
   private final String hostname;
   private final Long port;
   private final Long tlsPort;
   private final String role;
   private final Long replicationOffset;
   private final String health;
   private final Map<String, Object> clusterShardNodeInfo;

   public ClusterShardNodeInfo(Map<String, Object> map) {
      this.id = (String)map.get("id");
      this.endpoint = (String)map.get("endpoint");
      this.ip = (String)map.get("ip");
      this.hostname = (String)map.get("hostname");
      this.port = (Long)map.get("port");
      this.tlsPort = (Long)map.get("tls-port");
      this.role = (String)map.get("role");
      this.replicationOffset = (Long)map.get("replication-offset");
      this.health = (String)map.get("health");
      this.clusterShardNodeInfo = map;
   }

   public String getId() {
      return this.id;
   }

   public String getEndpoint() {
      return this.endpoint;
   }

   public String getIp() {
      return this.ip;
   }

   public String getHostname() {
      return this.hostname;
   }

   public Long getPort() {
      return this.port;
   }

   public Long getTlsPort() {
      return this.tlsPort;
   }

   public String getRole() {
      return this.role;
   }

   public Long getReplicationOffset() {
      return this.replicationOffset;
   }

   public String getHealth() {
      return this.health;
   }

   public Map<String, Object> getClusterShardNodeInfo() {
      return this.clusterShardNodeInfo;
   }

   public boolean isSsl() {
      return this.tlsPort != null;
   }
}
