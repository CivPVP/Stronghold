package me.neznamy.tab.libs.redis.clients.jedis;

import java.util.Set;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Experimental;
import me.neznamy.tab.libs.redis.clients.jedis.csc.Cache;
import me.neznamy.tab.libs.redis.clients.jedis.csc.CacheConfig;
import me.neznamy.tab.libs.redis.clients.jedis.csc.CacheFactory;
import me.neznamy.tab.libs.redis.clients.jedis.providers.SentineledConnectionProvider;

public class JedisSentineled extends UnifiedJedis {
   public JedisSentineled(String masterName, JedisClientConfig masterClientConfig, Set<HostAndPort> sentinels, JedisClientConfig sentinelClientConfig) {
      super(new SentineledConnectionProvider(masterName, masterClientConfig, sentinels, sentinelClientConfig), masterClientConfig.getRedisProtocol());
   }

   @Experimental
   public JedisSentineled(
      String masterName, JedisClientConfig masterClientConfig, CacheConfig cacheConfig, Set<HostAndPort> sentinels, JedisClientConfig sentinelClientConfig
   ) {
      this(masterName, masterClientConfig, CacheFactory.getCache(cacheConfig), sentinels, sentinelClientConfig);
   }

   @Experimental
   public JedisSentineled(
      String masterName, JedisClientConfig masterClientConfig, Cache clientSideCache, Set<HostAndPort> sentinels, JedisClientConfig sentinelClientConfig
   ) {
      super(
         new SentineledConnectionProvider(masterName, masterClientConfig, clientSideCache, sentinels, sentinelClientConfig),
         masterClientConfig.getRedisProtocol(),
         clientSideCache
      );
   }

   public JedisSentineled(
      String masterName,
      JedisClientConfig masterClientConfig,
      GenericObjectPoolConfig<Connection> poolConfig,
      Set<HostAndPort> sentinels,
      JedisClientConfig sentinelClientConfig
   ) {
      super(
         new SentineledConnectionProvider(masterName, masterClientConfig, poolConfig, sentinels, sentinelClientConfig), masterClientConfig.getRedisProtocol()
      );
   }

   @Experimental
   public JedisSentineled(
      String masterName,
      JedisClientConfig masterClientConfig,
      Cache clientSideCache,
      GenericObjectPoolConfig<Connection> poolConfig,
      Set<HostAndPort> sentinels,
      JedisClientConfig sentinelClientConfig
   ) {
      super(
         new SentineledConnectionProvider(masterName, masterClientConfig, clientSideCache, poolConfig, sentinels, sentinelClientConfig),
         masterClientConfig.getRedisProtocol(),
         clientSideCache
      );
   }

   public JedisSentineled(SentineledConnectionProvider sentineledConnectionProvider) {
      super(sentineledConnectionProvider);
   }

   public HostAndPort getCurrentMaster() {
      return ((SentineledConnectionProvider)this.provider).getCurrentMaster();
   }

   public Pipeline pipelined() {
      return (Pipeline)super.pipelined();
   }
}
