package me.neznamy.tab.libs.redis.clients.jedis;

public enum RedisProtocol {
   RESP2("2"),
   RESP3("3");

   private final String version;

   RedisProtocol(String ver) {
      this.version = ver;
   }

   public String version() {
      return this.version;
   }
}
