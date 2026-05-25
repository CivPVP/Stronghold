package me.neznamy.tab.libs.redis.clients.jedis;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Experimental;
import me.neznamy.tab.libs.redis.clients.jedis.csc.Cache;
import me.neznamy.tab.libs.redis.clients.jedis.csc.CacheConfig;
import me.neznamy.tab.libs.redis.clients.jedis.csc.CacheFactory;
import me.neznamy.tab.libs.redis.clients.jedis.executors.ClusterCommandExecutor;
import me.neznamy.tab.libs.redis.clients.jedis.providers.ClusterConnectionProvider;
import me.neznamy.tab.libs.redis.clients.jedis.util.JedisClusterCRC16;

public class JedisCluster extends UnifiedJedis {
   public static final String INIT_NO_ERROR_PROPERTY = "jedis.cluster.initNoError";
   public static final int DEFAULT_TIMEOUT = 2000;
   public static final int DEFAULT_MAX_ATTEMPTS = 5;

   public JedisCluster(HostAndPort node) {
      this(Collections.singleton(node));
   }

   public JedisCluster(HostAndPort node, int timeout) {
      this(Collections.singleton(node), timeout);
   }

   public JedisCluster(HostAndPort node, int timeout, int maxAttempts) {
      this(Collections.singleton(node), timeout, maxAttempts);
   }

   public JedisCluster(HostAndPort node, GenericObjectPoolConfig<Connection> poolConfig) {
      this(Collections.singleton(node), poolConfig);
   }

   public JedisCluster(HostAndPort node, int timeout, GenericObjectPoolConfig<Connection> poolConfig) {
      this(Collections.singleton(node), timeout, poolConfig);
   }

   public JedisCluster(HostAndPort node, int timeout, int maxAttempts, GenericObjectPoolConfig<Connection> poolConfig) {
      this(Collections.singleton(node), timeout, maxAttempts, poolConfig);
   }

   public JedisCluster(HostAndPort node, int connectionTimeout, int soTimeout, int maxAttempts, GenericObjectPoolConfig<Connection> poolConfig) {
      this(Collections.singleton(node), connectionTimeout, soTimeout, maxAttempts, poolConfig);
   }

   public JedisCluster(HostAndPort node, int connectionTimeout, int soTimeout, int maxAttempts, String password, GenericObjectPoolConfig<Connection> poolConfig) {
      this(Collections.singleton(node), connectionTimeout, soTimeout, maxAttempts, password, poolConfig);
   }

   public JedisCluster(
      HostAndPort node,
      int connectionTimeout,
      int soTimeout,
      int maxAttempts,
      String password,
      String clientName,
      GenericObjectPoolConfig<Connection> poolConfig
   ) {
      this(Collections.singleton(node), connectionTimeout, soTimeout, maxAttempts, password, clientName, poolConfig);
   }

   public JedisCluster(
      HostAndPort node,
      int connectionTimeout,
      int soTimeout,
      int maxAttempts,
      String user,
      String password,
      String clientName,
      GenericObjectPoolConfig<Connection> poolConfig
   ) {
      this(Collections.singleton(node), connectionTimeout, soTimeout, maxAttempts, user, password, clientName, poolConfig);
   }

   public JedisCluster(
      HostAndPort node,
      int connectionTimeout,
      int soTimeout,
      int maxAttempts,
      String password,
      String clientName,
      GenericObjectPoolConfig<Connection> poolConfig,
      boolean ssl
   ) {
      this(Collections.singleton(node), connectionTimeout, soTimeout, maxAttempts, password, clientName, poolConfig, ssl);
   }

   public JedisCluster(
      HostAndPort node,
      int connectionTimeout,
      int soTimeout,
      int maxAttempts,
      String user,
      String password,
      String clientName,
      GenericObjectPoolConfig<Connection> poolConfig,
      boolean ssl
   ) {
      this(Collections.singleton(node), connectionTimeout, soTimeout, maxAttempts, user, password, clientName, poolConfig, ssl);
   }

   public JedisCluster(HostAndPort node, JedisClientConfig clientConfig, int maxAttempts, GenericObjectPoolConfig<Connection> poolConfig) {
      this(Collections.singleton(node), clientConfig, maxAttempts, poolConfig);
   }

   public JedisCluster(Set<HostAndPort> nodes) {
      this(nodes, 2000);
   }

   public JedisCluster(Set<HostAndPort> nodes, int timeout) {
      this(nodes, DefaultJedisClientConfig.builder().timeoutMillis(timeout).build());
   }

   public JedisCluster(Set<HostAndPort> nodes, int timeout, int maxAttempts) {
      this(nodes, DefaultJedisClientConfig.builder().timeoutMillis(timeout).build(), maxAttempts);
   }

   public JedisCluster(Set<HostAndPort> nodes, String user, String password) {
      this(nodes, DefaultJedisClientConfig.builder().user(user).password(password).build());
   }

   public JedisCluster(Set<HostAndPort> nodes, String user, String password, HostAndPortMapper hostAndPortMap) {
      this(nodes, DefaultJedisClientConfig.builder().user(user).password(password).hostAndPortMapper(hostAndPortMap).build());
   }

   public JedisCluster(Set<HostAndPort> nodes, GenericObjectPoolConfig<Connection> poolConfig) {
      this(nodes, 2000, 5, poolConfig);
   }

   public JedisCluster(Set<HostAndPort> nodes, int timeout, GenericObjectPoolConfig<Connection> poolConfig) {
      this(nodes, timeout, 5, poolConfig);
   }

   public JedisCluster(Set<HostAndPort> clusterNodes, int timeout, int maxAttempts, GenericObjectPoolConfig<Connection> poolConfig) {
      this(clusterNodes, timeout, timeout, maxAttempts, poolConfig);
   }

   public JedisCluster(Set<HostAndPort> clusterNodes, int connectionTimeout, int soTimeout, int maxAttempts, GenericObjectPoolConfig<Connection> poolConfig) {
      this(clusterNodes, connectionTimeout, soTimeout, maxAttempts, null, poolConfig);
   }

   public JedisCluster(
      Set<HostAndPort> clusterNodes, int connectionTimeout, int soTimeout, int maxAttempts, String password, GenericObjectPoolConfig<Connection> poolConfig
   ) {
      this(clusterNodes, connectionTimeout, soTimeout, maxAttempts, password, null, poolConfig);
   }

   public JedisCluster(
      Set<HostAndPort> clusterNodes,
      int connectionTimeout,
      int soTimeout,
      int maxAttempts,
      String password,
      String clientName,
      GenericObjectPoolConfig<Connection> poolConfig
   ) {
      this(clusterNodes, connectionTimeout, soTimeout, maxAttempts, null, password, clientName, poolConfig);
   }

   public JedisCluster(
      Set<HostAndPort> clusterNodes,
      int connectionTimeout,
      int soTimeout,
      int maxAttempts,
      String user,
      String password,
      String clientName,
      GenericObjectPoolConfig<Connection> poolConfig
   ) {
      this(
         clusterNodes,
         DefaultJedisClientConfig.builder()
            .connectionTimeoutMillis(connectionTimeout)
            .socketTimeoutMillis(soTimeout)
            .user(user)
            .password(password)
            .clientName(clientName)
            .build(),
         maxAttempts,
         poolConfig
      );
   }

   public JedisCluster(
      Set<HostAndPort> clusterNodes,
      int connectionTimeout,
      int soTimeout,
      int infiniteSoTimeout,
      int maxAttempts,
      String user,
      String password,
      String clientName,
      GenericObjectPoolConfig<Connection> poolConfig
   ) {
      this(
         clusterNodes,
         DefaultJedisClientConfig.builder()
            .connectionTimeoutMillis(connectionTimeout)
            .socketTimeoutMillis(soTimeout)
            .blockingSocketTimeoutMillis(infiniteSoTimeout)
            .user(user)
            .password(password)
            .clientName(clientName)
            .build(),
         maxAttempts,
         poolConfig
      );
   }

   public JedisCluster(
      Set<HostAndPort> clusterNodes,
      int connectionTimeout,
      int soTimeout,
      int maxAttempts,
      String password,
      String clientName,
      GenericObjectPoolConfig<Connection> poolConfig,
      boolean ssl
   ) {
      this(clusterNodes, connectionTimeout, soTimeout, maxAttempts, null, password, clientName, poolConfig, ssl);
   }

   public JedisCluster(
      Set<HostAndPort> clusterNodes,
      int connectionTimeout,
      int soTimeout,
      int maxAttempts,
      String user,
      String password,
      String clientName,
      GenericObjectPoolConfig<Connection> poolConfig,
      boolean ssl
   ) {
      this(
         clusterNodes,
         DefaultJedisClientConfig.builder()
            .connectionTimeoutMillis(connectionTimeout)
            .socketTimeoutMillis(soTimeout)
            .user(user)
            .password(password)
            .clientName(clientName)
            .ssl(ssl)
            .build(),
         maxAttempts,
         poolConfig
      );
   }

   public JedisCluster(Set<HostAndPort> clusterNodes, JedisClientConfig clientConfig) {
      this(clusterNodes, clientConfig, 5);
   }

   public JedisCluster(Set<HostAndPort> clusterNodes, JedisClientConfig clientConfig, int maxAttempts) {
      this(clusterNodes, clientConfig, maxAttempts, Duration.ofMillis((long)clientConfig.getSocketTimeoutMillis() * maxAttempts));
   }

   public JedisCluster(Set<HostAndPort> clusterNodes, JedisClientConfig clientConfig, int maxAttempts, Duration maxTotalRetriesDuration) {
      this(new ClusterConnectionProvider(clusterNodes, clientConfig), maxAttempts, maxTotalRetriesDuration, clientConfig.getRedisProtocol());
   }

   public JedisCluster(Set<HostAndPort> clusterNodes, JedisClientConfig clientConfig, GenericObjectPoolConfig<Connection> poolConfig) {
      this(clusterNodes, clientConfig, 5, poolConfig);
   }

   public JedisCluster(Set<HostAndPort> clusterNodes, JedisClientConfig clientConfig, int maxAttempts, GenericObjectPoolConfig<Connection> poolConfig) {
      this(clusterNodes, clientConfig, maxAttempts, Duration.ofMillis((long)clientConfig.getSocketTimeoutMillis() * maxAttempts), poolConfig);
   }

   public JedisCluster(
      Set<HostAndPort> clusterNodes,
      JedisClientConfig clientConfig,
      int maxAttempts,
      Duration maxTotalRetriesDuration,
      GenericObjectPoolConfig<Connection> poolConfig
   ) {
      this(new ClusterConnectionProvider(clusterNodes, clientConfig, poolConfig), maxAttempts, maxTotalRetriesDuration, clientConfig.getRedisProtocol());
   }

   public JedisCluster(
      Set<HostAndPort> clusterNodes,
      JedisClientConfig clientConfig,
      GenericObjectPoolConfig<Connection> poolConfig,
      Duration topologyRefreshPeriod,
      int maxAttempts,
      Duration maxTotalRetriesDuration
   ) {
      this(
         new ClusterConnectionProvider(clusterNodes, clientConfig, poolConfig, topologyRefreshPeriod),
         maxAttempts,
         maxTotalRetriesDuration,
         clientConfig.getRedisProtocol()
      );
   }

   public JedisCluster(ClusterConnectionProvider provider, int maxAttempts, Duration maxTotalRetriesDuration) {
      super(provider, maxAttempts, maxTotalRetriesDuration);
   }

   private JedisCluster(ClusterConnectionProvider provider, int maxAttempts, Duration maxTotalRetriesDuration, RedisProtocol protocol) {
      super(provider, maxAttempts, maxTotalRetriesDuration, protocol);
   }

   @Experimental
   public JedisCluster(Set<HostAndPort> hnp, JedisClientConfig jedisClientConfig, CacheConfig cacheConfig) {
      this(hnp, jedisClientConfig, CacheFactory.getCache(cacheConfig));
   }

   @Experimental
   public JedisCluster(Set<HostAndPort> clusterNodes, JedisClientConfig clientConfig, Cache clientSideCache) {
      this(clusterNodes, clientConfig, clientSideCache, 5, Duration.ofMillis(5 * clientConfig.getSocketTimeoutMillis()));
   }

   @Experimental
   public JedisCluster(Set<HostAndPort> clusterNodes, JedisClientConfig clientConfig, Cache clientSideCache, int maxAttempts, Duration maxTotalRetriesDuration) {
      this(
         new ClusterConnectionProvider(clusterNodes, clientConfig, clientSideCache),
         maxAttempts,
         maxTotalRetriesDuration,
         clientConfig.getRedisProtocol(),
         clientSideCache
      );
   }

   @Experimental
   public JedisCluster(
      Set<HostAndPort> clusterNodes,
      JedisClientConfig clientConfig,
      Cache clientSideCache,
      int maxAttempts,
      Duration maxTotalRetriesDuration,
      GenericObjectPoolConfig<Connection> poolConfig
   ) {
      this(
         new ClusterConnectionProvider(clusterNodes, clientConfig, clientSideCache, poolConfig),
         maxAttempts,
         maxTotalRetriesDuration,
         clientConfig.getRedisProtocol(),
         clientSideCache
      );
   }

   @Experimental
   public JedisCluster(Set<HostAndPort> clusterNodes, JedisClientConfig clientConfig, Cache clientSideCache, GenericObjectPoolConfig<Connection> poolConfig) {
      this(
         new ClusterConnectionProvider(clusterNodes, clientConfig, clientSideCache, poolConfig),
         5,
         Duration.ofMillis(5 * clientConfig.getSocketTimeoutMillis()),
         clientConfig.getRedisProtocol(),
         clientSideCache
      );
   }

   @Experimental
   public JedisCluster(
      Set<HostAndPort> clusterNodes,
      JedisClientConfig clientConfig,
      Cache clientSideCache,
      GenericObjectPoolConfig<Connection> poolConfig,
      Duration topologyRefreshPeriod,
      int maxAttempts,
      Duration maxTotalRetriesDuration
   ) {
      this(
         new ClusterConnectionProvider(clusterNodes, clientConfig, clientSideCache, poolConfig, topologyRefreshPeriod),
         maxAttempts,
         maxTotalRetriesDuration,
         clientConfig.getRedisProtocol(),
         clientSideCache
      );
   }

   @Experimental
   private JedisCluster(ClusterConnectionProvider provider, int maxAttempts, Duration maxTotalRetriesDuration, RedisProtocol protocol, Cache clientSideCache) {
      super(provider, maxAttempts, maxTotalRetriesDuration, protocol, clientSideCache);
   }

   public Map<String, ConnectionPool> getClusterNodes() {
      return ((ClusterConnectionProvider)this.provider).getNodes();
   }

   public Connection getConnectionFromSlot(int slot) {
      return ((ClusterConnectionProvider)this.provider).getConnectionFromSlot(slot);
   }

   public long spublish(String channel, String message) {
      return this.executeCommand(this.commandObjects.spublish(channel, message));
   }

   public long spublish(byte[] channel, byte[] message) {
      return this.executeCommand(this.commandObjects.spublish(channel, message));
   }

   public void ssubscribe(JedisShardedPubSub jedisPubSub, String... channels) {
      try (Connection connection = this.getConnectionFromSlot(JedisClusterCRC16.getSlot(channels[0]))) {
         jedisPubSub.proceed(connection, channels);
      }
   }

   public void ssubscribe(BinaryJedisShardedPubSub jedisPubSub, byte[]... channels) {
      try (Connection connection = this.getConnectionFromSlot(JedisClusterCRC16.getSlot(channels[0]))) {
         jedisPubSub.proceed(connection, channels);
      }
   }

   public ClusterPipeline pipelined() {
      return new ClusterPipeline((ClusterConnectionProvider)this.provider, (ClusterCommandObjects)this.commandObjects);
   }

   @Override
   public AbstractTransaction transaction(boolean doMulti) {
      throw new UnsupportedOperationException();
   }

   public final <T> T executeCommandToReplica(CommandObject<T> commandObject) {
      if (!(this.executor instanceof ClusterCommandExecutor)) {
         throw new UnsupportedOperationException("Support only execute to replica in ClusterCommandExecutor");
      } else {
         return ((ClusterCommandExecutor)this.executor).executeCommandToReplica(commandObject);
      }
   }
}
