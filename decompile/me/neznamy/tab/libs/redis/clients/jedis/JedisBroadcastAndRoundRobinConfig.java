package me.neznamy.tab.libs.redis.clients.jedis;

public interface JedisBroadcastAndRoundRobinConfig {
   JedisBroadcastAndRoundRobinConfig.RediSearchMode getRediSearchModeInCluster();

   enum RediSearchMode {
      DEFAULT,
      LIGHT;
   }
}
