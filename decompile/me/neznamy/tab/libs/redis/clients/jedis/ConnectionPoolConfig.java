package me.neznamy.tab.libs.redis.clients.jedis;

import java.time.Duration;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class ConnectionPoolConfig extends GenericObjectPoolConfig<Connection> {
   public ConnectionPoolConfig() {
      this.setTestWhileIdle(true);
      this.setMinEvictableIdleTime(Duration.ofMillis(60000L));
      this.setTimeBetweenEvictionRuns(Duration.ofMillis(30000L));
      this.setNumTestsPerEvictionRun(-1);
   }
}
