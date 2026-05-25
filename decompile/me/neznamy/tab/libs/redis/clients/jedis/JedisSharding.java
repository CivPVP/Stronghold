package me.neznamy.tab.libs.redis.clients.jedis;

import java.util.List;
import java.util.regex.Pattern;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import me.neznamy.tab.libs.redis.clients.jedis.providers.ShardedConnectionProvider;
import me.neznamy.tab.libs.redis.clients.jedis.util.Hashing;

@Deprecated
public class JedisSharding extends UnifiedJedis {
   public static final Pattern DEFAULT_KEY_TAG_PATTERN = Pattern.compile("\\{(.+?)\\}");

   public JedisSharding(List<HostAndPort> shards) {
      this(new ShardedConnectionProvider(shards));
   }

   public JedisSharding(List<HostAndPort> shards, JedisClientConfig clientConfig) {
      this(new ShardedConnectionProvider(shards, clientConfig));
      this.setProtocol(clientConfig);
   }

   public JedisSharding(List<HostAndPort> shards, JedisClientConfig clientConfig, GenericObjectPoolConfig<Connection> poolConfig) {
      this(new ShardedConnectionProvider(shards, clientConfig, poolConfig));
      this.setProtocol(clientConfig);
   }

   public JedisSharding(List<HostAndPort> shards, JedisClientConfig clientConfig, Hashing algo) {
      this(new ShardedConnectionProvider(shards, clientConfig, algo));
      this.setProtocol(clientConfig);
   }

   public JedisSharding(List<HostAndPort> shards, JedisClientConfig clientConfig, GenericObjectPoolConfig<Connection> poolConfig, Hashing algo) {
      this(new ShardedConnectionProvider(shards, clientConfig, poolConfig, algo));
      this.setProtocol(clientConfig);
   }

   public JedisSharding(ShardedConnectionProvider provider) {
      super(provider);
   }

   public JedisSharding(ShardedConnectionProvider provider, Pattern tagPattern) {
      super(provider, tagPattern);
   }

   private void setProtocol(JedisClientConfig clientConfig) {
      RedisProtocol proto = clientConfig.getRedisProtocol();
      if (proto == RedisProtocol.RESP3) {
         this.commandObjects.setProtocol(proto);
      }
   }

   public ShardedPipeline pipelined() {
      return new ShardedPipeline((ShardedConnectionProvider)this.provider);
   }

   @Override
   public AbstractTransaction transaction(boolean doMulti) {
      throw new UnsupportedOperationException();
   }
}
