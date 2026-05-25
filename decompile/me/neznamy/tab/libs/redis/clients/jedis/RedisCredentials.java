package me.neznamy.tab.libs.redis.clients.jedis;

public interface RedisCredentials {
   default String getUser() {
      return null;
   }

   default char[] getPassword() {
      return null;
   }
}
