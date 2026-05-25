package me.neznamy.tab.libs.redis.clients.jedis;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import me.neznamy.tab.libs.org.json.JSONArray;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Experimental;
import me.neznamy.tab.libs.redis.clients.jedis.annots.VisibleForTesting;
import me.neznamy.tab.libs.redis.clients.jedis.args.BitCountOption;
import me.neznamy.tab.libs.redis.clients.jedis.args.BitOP;
import me.neznamy.tab.libs.redis.clients.jedis.args.ExpiryOption;
import me.neznamy.tab.libs.redis.clients.jedis.args.FlushMode;
import me.neznamy.tab.libs.redis.clients.jedis.args.FunctionRestorePolicy;
import me.neznamy.tab.libs.redis.clients.jedis.args.GeoUnit;
import me.neznamy.tab.libs.redis.clients.jedis.args.ListDirection;
import me.neznamy.tab.libs.redis.clients.jedis.args.ListPosition;
import me.neznamy.tab.libs.redis.clients.jedis.args.SortedSetOption;
import me.neznamy.tab.libs.redis.clients.jedis.bloom.BFInsertParams;
import me.neznamy.tab.libs.redis.clients.jedis.bloom.BFReserveParams;
import me.neznamy.tab.libs.redis.clients.jedis.bloom.CFInsertParams;
import me.neznamy.tab.libs.redis.clients.jedis.bloom.CFReserveParams;
import me.neznamy.tab.libs.redis.clients.jedis.bloom.TDigestMergeParams;
import me.neznamy.tab.libs.redis.clients.jedis.commands.JedisBinaryCommands;
import me.neznamy.tab.libs.redis.clients.jedis.commands.JedisCommands;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ProtocolCommand;
import me.neznamy.tab.libs.redis.clients.jedis.commands.RedisModuleCommands;
import me.neznamy.tab.libs.redis.clients.jedis.commands.SampleBinaryKeyedCommands;
import me.neznamy.tab.libs.redis.clients.jedis.commands.SampleKeyedCommands;
import me.neznamy.tab.libs.redis.clients.jedis.csc.Cache;
import me.neznamy.tab.libs.redis.clients.jedis.csc.CacheConfig;
import me.neznamy.tab.libs.redis.clients.jedis.csc.CacheConnection;
import me.neznamy.tab.libs.redis.clients.jedis.csc.CacheFactory;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;
import me.neznamy.tab.libs.redis.clients.jedis.executors.ClusterCommandExecutor;
import me.neznamy.tab.libs.redis.clients.jedis.executors.CommandExecutor;
import me.neznamy.tab.libs.redis.clients.jedis.executors.DefaultCommandExecutor;
import me.neznamy.tab.libs.redis.clients.jedis.executors.RetryableCommandExecutor;
import me.neznamy.tab.libs.redis.clients.jedis.executors.SimpleCommandExecutor;
import me.neznamy.tab.libs.redis.clients.jedis.gears.TFunctionListParams;
import me.neznamy.tab.libs.redis.clients.jedis.gears.TFunctionLoadParams;
import me.neznamy.tab.libs.redis.clients.jedis.gears.resps.GearsLibraryInfo;
import me.neznamy.tab.libs.redis.clients.jedis.graph.GraphCommandObjects;
import me.neznamy.tab.libs.redis.clients.jedis.graph.ResultSet;
import me.neznamy.tab.libs.redis.clients.jedis.json.JsonObjectMapper;
import me.neznamy.tab.libs.redis.clients.jedis.json.JsonSetParams;
import me.neznamy.tab.libs.redis.clients.jedis.json.Path;
import me.neznamy.tab.libs.redis.clients.jedis.json.Path2;
import me.neznamy.tab.libs.redis.clients.jedis.mcf.CircuitBreakerCommandExecutor;
import me.neznamy.tab.libs.redis.clients.jedis.mcf.MultiClusterPipeline;
import me.neznamy.tab.libs.redis.clients.jedis.mcf.MultiClusterTransaction;
import me.neznamy.tab.libs.redis.clients.jedis.params.BitPosParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.GeoAddParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.GeoRadiusParam;
import me.neznamy.tab.libs.redis.clients.jedis.params.GeoRadiusStoreParam;
import me.neznamy.tab.libs.redis.clients.jedis.params.GeoSearchParam;
import me.neznamy.tab.libs.redis.clients.jedis.params.GetExParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.LCSParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.LPosParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.MigrateParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.RestoreParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ScanParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.SetParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.SortingParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XAddParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XAutoClaimParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XClaimParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XPendingParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XReadGroupParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XReadParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.XTrimParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ZAddParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ZIncrByParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ZParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ZRangeParams;
import me.neznamy.tab.libs.redis.clients.jedis.providers.ClusterConnectionProvider;
import me.neznamy.tab.libs.redis.clients.jedis.providers.ConnectionProvider;
import me.neznamy.tab.libs.redis.clients.jedis.providers.MultiClusterPooledConnectionProvider;
import me.neznamy.tab.libs.redis.clients.jedis.providers.PooledConnectionProvider;
import me.neznamy.tab.libs.redis.clients.jedis.providers.ShardedConnectionProvider;
import me.neznamy.tab.libs.redis.clients.jedis.resps.FunctionStats;
import me.neznamy.tab.libs.redis.clients.jedis.resps.GeoRadiusResponse;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LCSMatchResult;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LibraryInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ScanResult;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamConsumerInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamConsumersInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamEntry;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamFullInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamGroupInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamPendingEntry;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamPendingSummary;
import me.neznamy.tab.libs.redis.clients.jedis.resps.Tuple;
import me.neznamy.tab.libs.redis.clients.jedis.search.FTCreateParams;
import me.neznamy.tab.libs.redis.clients.jedis.search.FTProfileParams;
import me.neznamy.tab.libs.redis.clients.jedis.search.FTSearchParams;
import me.neznamy.tab.libs.redis.clients.jedis.search.FTSpellCheckParams;
import me.neznamy.tab.libs.redis.clients.jedis.search.FtSearchIteration;
import me.neznamy.tab.libs.redis.clients.jedis.search.IndexOptions;
import me.neznamy.tab.libs.redis.clients.jedis.search.Query;
import me.neznamy.tab.libs.redis.clients.jedis.search.Schema;
import me.neznamy.tab.libs.redis.clients.jedis.search.SearchProtocol;
import me.neznamy.tab.libs.redis.clients.jedis.search.SearchResult;
import me.neznamy.tab.libs.redis.clients.jedis.search.aggr.AggregationBuilder;
import me.neznamy.tab.libs.redis.clients.jedis.search.aggr.AggregationResult;
import me.neznamy.tab.libs.redis.clients.jedis.search.aggr.FtAggregateIteration;
import me.neznamy.tab.libs.redis.clients.jedis.search.schemafields.SchemaField;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.AggregationType;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSAddParams;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSAlterParams;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSCreateParams;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSDecrByParams;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSElement;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSGetParams;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSIncrByParams;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSInfo;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSMGetElement;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSMGetParams;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSMRangeElements;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSMRangeParams;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TSRangeParams;
import me.neznamy.tab.libs.redis.clients.jedis.util.IOUtils;
import me.neznamy.tab.libs.redis.clients.jedis.util.JedisURIHelper;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public class UnifiedJedis implements JedisCommands, JedisBinaryCommands, SampleKeyedCommands, SampleBinaryKeyedCommands, RedisModuleCommands, AutoCloseable {
   @Deprecated
   protected RedisProtocol protocol = null;
   protected final ConnectionProvider provider;
   protected final CommandExecutor executor;
   protected final CommandObjects commandObjects;
   private final GraphCommandObjects graphCommandObjects;
   private JedisBroadcastAndRoundRobinConfig broadcastAndRoundRobinConfig = null;
   private final Cache cache;

   public UnifiedJedis() {
      this(new HostAndPort("127.0.0.1", 6379));
   }

   public UnifiedJedis(HostAndPort hostAndPort) {
      this(new PooledConnectionProvider(hostAndPort), (RedisProtocol)null);
   }

   public UnifiedJedis(String url) {
      this(URI.create(url));
   }

   public UnifiedJedis(URI uri) {
      this(
         JedisURIHelper.getHostAndPort(uri),
         DefaultJedisClientConfig.builder()
            .user(JedisURIHelper.getUser(uri))
            .password(JedisURIHelper.getPassword(uri))
            .database(JedisURIHelper.getDBIndex(uri))
            .protocol(JedisURIHelper.getRedisProtocol(uri))
            .ssl(JedisURIHelper.isRedisSSLScheme(uri))
            .build()
      );
   }

   public UnifiedJedis(URI uri, JedisClientConfig config) {
      this(
         JedisURIHelper.getHostAndPort(uri),
         DefaultJedisClientConfig.builder()
            .connectionTimeoutMillis(config.getConnectionTimeoutMillis())
            .socketTimeoutMillis(config.getSocketTimeoutMillis())
            .blockingSocketTimeoutMillis(config.getBlockingSocketTimeoutMillis())
            .user(JedisURIHelper.getUser(uri))
            .password(JedisURIHelper.getPassword(uri))
            .database(JedisURIHelper.getDBIndex(uri))
            .clientName(config.getClientName())
            .protocol(JedisURIHelper.getRedisProtocol(uri))
            .ssl(JedisURIHelper.isRedisSSLScheme(uri))
            .sslSocketFactory(config.getSslSocketFactory())
            .sslParameters(config.getSslParameters())
            .hostnameVerifier(config.getHostnameVerifier())
            .build()
      );
   }

   public UnifiedJedis(HostAndPort hostAndPort, JedisClientConfig clientConfig) {
      this(new PooledConnectionProvider(hostAndPort, clientConfig), clientConfig.getRedisProtocol());
   }

   @Experimental
   public UnifiedJedis(HostAndPort hostAndPort, JedisClientConfig clientConfig, CacheConfig cacheConfig) {
      this(hostAndPort, clientConfig, CacheFactory.getCache(cacheConfig));
   }

   @Experimental
   public UnifiedJedis(HostAndPort hostAndPort, JedisClientConfig clientConfig, Cache cache) {
      this(new PooledConnectionProvider(hostAndPort, clientConfig, cache), clientConfig.getRedisProtocol(), cache);
   }

   public UnifiedJedis(ConnectionProvider provider) {
      this(new DefaultCommandExecutor(provider), provider);
   }

   protected UnifiedJedis(ConnectionProvider provider, RedisProtocol protocol) {
      this(new DefaultCommandExecutor(provider), provider, new CommandObjects(), protocol);
   }

   @Experimental
   protected UnifiedJedis(ConnectionProvider provider, RedisProtocol protocol, Cache cache) {
      this(new DefaultCommandExecutor(provider), provider, new CommandObjects(), protocol, cache);
   }

   public UnifiedJedis(JedisSocketFactory socketFactory) {
      this(new Connection(socketFactory));
   }

   public UnifiedJedis(JedisSocketFactory socketFactory, JedisClientConfig clientConfig) {
      this(new Connection(socketFactory, clientConfig));
   }

   public UnifiedJedis(Connection connection) {
      this.provider = null;
      this.executor = new SimpleCommandExecutor(connection);
      this.commandObjects = new CommandObjects();
      RedisProtocol proto = connection.getRedisProtocol();
      if (proto != null) {
         this.commandObjects.setProtocol(proto);
      }

      this.graphCommandObjects = new GraphCommandObjects(this);
      if (connection instanceof CacheConnection) {
         this.cache = ((CacheConnection)connection).getCache();
      } else {
         this.cache = null;
      }
   }

   @Deprecated
   public UnifiedJedis(Set<HostAndPort> jedisClusterNodes, JedisClientConfig clientConfig, int maxAttempts) {
      this(jedisClusterNodes, clientConfig, maxAttempts, Duration.ofMillis(maxAttempts * clientConfig.getSocketTimeoutMillis()));
   }

   @Deprecated
   public UnifiedJedis(Set<HostAndPort> jedisClusterNodes, JedisClientConfig clientConfig, int maxAttempts, Duration maxTotalRetriesDuration) {
      this(new ClusterConnectionProvider(jedisClusterNodes, clientConfig), maxAttempts, maxTotalRetriesDuration, clientConfig.getRedisProtocol());
   }

   @Deprecated
   public UnifiedJedis(
      Set<HostAndPort> jedisClusterNodes,
      JedisClientConfig clientConfig,
      GenericObjectPoolConfig<Connection> poolConfig,
      int maxAttempts,
      Duration maxTotalRetriesDuration
   ) {
      this(new ClusterConnectionProvider(jedisClusterNodes, clientConfig, poolConfig), maxAttempts, maxTotalRetriesDuration, clientConfig.getRedisProtocol());
   }

   public UnifiedJedis(ClusterConnectionProvider provider, int maxAttempts, Duration maxTotalRetriesDuration) {
      this(new ClusterCommandExecutor(provider, maxAttempts, maxTotalRetriesDuration), provider, new ClusterCommandObjects());
   }

   protected UnifiedJedis(ClusterConnectionProvider provider, int maxAttempts, Duration maxTotalRetriesDuration, RedisProtocol protocol) {
      this(new ClusterCommandExecutor(provider, maxAttempts, maxTotalRetriesDuration), provider, new ClusterCommandObjects(), protocol);
   }

   @Experimental
   protected UnifiedJedis(ClusterConnectionProvider provider, int maxAttempts, Duration maxTotalRetriesDuration, RedisProtocol protocol, Cache cache) {
      this(new ClusterCommandExecutor(provider, maxAttempts, maxTotalRetriesDuration), provider, new ClusterCommandObjects(), protocol, cache);
   }

   @Deprecated
   public UnifiedJedis(ShardedConnectionProvider provider) {
      this(new DefaultCommandExecutor(provider), provider, new ShardedCommandObjects(provider.getHashingAlgo()));
   }

   @Deprecated
   public UnifiedJedis(ShardedConnectionProvider provider, Pattern tagPattern) {
      this(new DefaultCommandExecutor(provider), provider, new ShardedCommandObjects(provider.getHashingAlgo(), tagPattern));
   }

   public UnifiedJedis(ConnectionProvider provider, int maxAttempts, Duration maxTotalRetriesDuration) {
      this(new RetryableCommandExecutor(provider, maxAttempts, maxTotalRetriesDuration), provider);
   }

   @Experimental
   public UnifiedJedis(MultiClusterPooledConnectionProvider provider) {
      this(new CircuitBreakerCommandExecutor(provider), provider);
   }

   public UnifiedJedis(CommandExecutor executor) {
      this(executor, (ConnectionProvider)null);
   }

   private UnifiedJedis(CommandExecutor executor, ConnectionProvider provider) {
      this(executor, provider, new CommandObjects());
   }

   @VisibleForTesting
   public UnifiedJedis(CommandExecutor executor, ConnectionProvider provider, CommandObjects commandObjects) {
      this(executor, provider, commandObjects, null, null);
      if (this.provider != null) {
         try (Connection conn = this.provider.getConnection()) {
            if (conn != null) {
               RedisProtocol proto = conn.getRedisProtocol();
               if (proto != null) {
                  this.commandObjects.setProtocol(proto);
               }
            }
         } catch (JedisException var17) {
         }
      }
   }

   @Experimental
   private UnifiedJedis(CommandExecutor executor, ConnectionProvider provider, CommandObjects commandObjects, RedisProtocol protocol) {
      this(executor, provider, commandObjects, protocol, (Cache)null);
   }

   @Experimental
   private UnifiedJedis(CommandExecutor executor, ConnectionProvider provider, CommandObjects commandObjects, RedisProtocol protocol, Cache cache) {
      if (cache != null && protocol != RedisProtocol.RESP3) {
         throw new IllegalArgumentException("Client-side caching is only supported with RESP3.");
      }

      this.provider = provider;
      this.executor = executor;
      this.commandObjects = commandObjects;
      if (protocol != null) {
         this.commandObjects.setProtocol(protocol);
      }

      this.graphCommandObjects = new GraphCommandObjects(this);
      this.graphCommandObjects.setBaseCommandArgumentsCreator(comm -> this.commandObjects.commandArguments(comm));
      this.cache = cache;
   }

   @Override
   public void close() {
      IOUtils.closeQuietly(this.executor);
   }

   @Deprecated
   protected final void setProtocol(RedisProtocol protocol) {
      this.protocol = protocol;
      this.commandObjects.setProtocol(this.protocol);
   }

   public final <T> T executeCommand(CommandObject<T> commandObject) {
      return this.executor.executeCommand(commandObject);
   }

   public final <T> T broadcastCommand(CommandObject<T> commandObject) {
      return this.executor.broadcastCommand(commandObject);
   }

   private <T> T checkAndBroadcastCommand(CommandObject<T> commandObject) {
      boolean broadcast = true;
      if (this.broadcastAndRoundRobinConfig != null
         && commandObject.getArguments().getCommand() instanceof SearchProtocol.SearchCommand
         && this.broadcastAndRoundRobinConfig.getRediSearchModeInCluster() == JedisBroadcastAndRoundRobinConfig.RediSearchMode.LIGHT) {
         broadcast = false;
      }

      return broadcast ? this.broadcastCommand(commandObject) : this.executeCommand(commandObject);
   }

   public void setBroadcastAndRoundRobinConfig(JedisBroadcastAndRoundRobinConfig config) {
      this.broadcastAndRoundRobinConfig = config;
      this.commandObjects.setBroadcastAndRoundRobinConfig(this.broadcastAndRoundRobinConfig);
   }

   public Cache getCache() {
      return this.cache;
   }

   public String ping() {
      return this.checkAndBroadcastCommand(this.commandObjects.ping());
   }

   public String flushDB() {
      return this.checkAndBroadcastCommand(this.commandObjects.flushDB());
   }

   public String flushAll() {
      return this.checkAndBroadcastCommand(this.commandObjects.flushAll());
   }

   public String configSet(String parameter, String value) {
      return this.checkAndBroadcastCommand(this.commandObjects.configSet(parameter, value));
   }

   @Override
   public boolean exists(String key) {
      return this.executeCommand(this.commandObjects.exists(key));
   }

   @Override
   public long exists(String... keys) {
      return this.executeCommand(this.commandObjects.exists(keys));
   }

   @Override
   public long persist(String key) {
      return this.executeCommand(this.commandObjects.persist(key));
   }

   @Override
   public String type(String key) {
      return this.executeCommand(this.commandObjects.type(key));
   }

   @Override
   public boolean exists(byte[] key) {
      return this.executeCommand(this.commandObjects.exists(key));
   }

   @Override
   public long exists(byte[]... keys) {
      return this.executeCommand(this.commandObjects.exists(keys));
   }

   @Override
   public long persist(byte[] key) {
      return this.executeCommand(this.commandObjects.persist(key));
   }

   @Override
   public String type(byte[] key) {
      return this.executeCommand(this.commandObjects.type(key));
   }

   @Override
   public byte[] dump(String key) {
      return this.executeCommand(this.commandObjects.dump(key));
   }

   @Override
   public String restore(String key, long ttl, byte[] serializedValue) {
      return this.executeCommand(this.commandObjects.restore(key, ttl, serializedValue));
   }

   @Override
   public String restore(String key, long ttl, byte[] serializedValue, RestoreParams params) {
      return this.executeCommand(this.commandObjects.restore(key, ttl, serializedValue, params));
   }

   @Override
   public byte[] dump(byte[] key) {
      return this.executeCommand(this.commandObjects.dump(key));
   }

   @Override
   public String restore(byte[] key, long ttl, byte[] serializedValue) {
      return this.executeCommand(this.commandObjects.restore(key, ttl, serializedValue));
   }

   @Override
   public String restore(byte[] key, long ttl, byte[] serializedValue, RestoreParams params) {
      return this.executeCommand(this.commandObjects.restore(key, ttl, serializedValue, params));
   }

   @Override
   public long expire(String key, long seconds) {
      return this.executeCommand(this.commandObjects.expire(key, seconds));
   }

   @Override
   public long expire(String key, long seconds, ExpiryOption expiryOption) {
      return this.executeCommand(this.commandObjects.expire(key, seconds, expiryOption));
   }

   @Override
   public long pexpire(String key, long milliseconds) {
      return this.executeCommand(this.commandObjects.pexpire(key, milliseconds));
   }

   @Override
   public long pexpire(String key, long milliseconds, ExpiryOption expiryOption) {
      return this.executeCommand(this.commandObjects.pexpire(key, milliseconds, expiryOption));
   }

   @Override
   public long expireTime(String key) {
      return this.executeCommand(this.commandObjects.expireTime(key));
   }

   @Override
   public long pexpireTime(String key) {
      return this.executeCommand(this.commandObjects.pexpireTime(key));
   }

   @Override
   public long expireAt(String key, long unixTime) {
      return this.executeCommand(this.commandObjects.expireAt(key, unixTime));
   }

   @Override
   public long expireAt(String key, long unixTime, ExpiryOption expiryOption) {
      return this.executeCommand(this.commandObjects.expireAt(key, unixTime, expiryOption));
   }

   @Override
   public long pexpireAt(String key, long millisecondsTimestamp) {
      return this.executeCommand(this.commandObjects.pexpireAt(key, millisecondsTimestamp));
   }

   @Override
   public long pexpireAt(String key, long millisecondsTimestamp, ExpiryOption expiryOption) {
      return this.executeCommand(this.commandObjects.pexpireAt(key, millisecondsTimestamp, expiryOption));
   }

   @Override
   public long expire(byte[] key, long seconds) {
      return this.executeCommand(this.commandObjects.expire(key, seconds));
   }

   @Override
   public long expire(byte[] key, long seconds, ExpiryOption expiryOption) {
      return this.executeCommand(this.commandObjects.expire(key, seconds, expiryOption));
   }

   @Override
   public long pexpire(byte[] key, long milliseconds) {
      return this.executeCommand(this.commandObjects.pexpire(key, milliseconds));
   }

   @Override
   public long pexpire(byte[] key, long milliseconds, ExpiryOption expiryOption) {
      return this.executeCommand(this.commandObjects.pexpire(key, milliseconds, expiryOption));
   }

   @Override
   public long expireTime(byte[] key) {
      return this.executeCommand(this.commandObjects.expireTime(key));
   }

   @Override
   public long pexpireTime(byte[] key) {
      return this.executeCommand(this.commandObjects.pexpireTime(key));
   }

   @Override
   public long expireAt(byte[] key, long unixTime) {
      return this.executeCommand(this.commandObjects.expireAt(key, unixTime));
   }

   @Override
   public long expireAt(byte[] key, long unixTime, ExpiryOption expiryOption) {
      return this.executeCommand(this.commandObjects.expireAt(key, unixTime, expiryOption));
   }

   @Override
   public long pexpireAt(byte[] key, long millisecondsTimestamp) {
      return this.executeCommand(this.commandObjects.pexpireAt(key, millisecondsTimestamp));
   }

   @Override
   public long pexpireAt(byte[] key, long millisecondsTimestamp, ExpiryOption expiryOption) {
      return this.executeCommand(this.commandObjects.pexpireAt(key, millisecondsTimestamp, expiryOption));
   }

   @Override
   public long ttl(String key) {
      return this.executeCommand(this.commandObjects.ttl(key));
   }

   @Override
   public long pttl(String key) {
      return this.executeCommand(this.commandObjects.pttl(key));
   }

   @Override
   public long touch(String key) {
      return this.executeCommand(this.commandObjects.touch(key));
   }

   @Override
   public long touch(String... keys) {
      return this.executeCommand(this.commandObjects.touch(keys));
   }

   @Override
   public long ttl(byte[] key) {
      return this.executeCommand(this.commandObjects.ttl(key));
   }

   @Override
   public long pttl(byte[] key) {
      return this.executeCommand(this.commandObjects.pttl(key));
   }

   @Override
   public long touch(byte[] key) {
      return this.executeCommand(this.commandObjects.touch(key));
   }

   @Override
   public long touch(byte[]... keys) {
      return this.executeCommand(this.commandObjects.touch(keys));
   }

   @Override
   public List<String> sort(String key) {
      return this.executeCommand(this.commandObjects.sort(key));
   }

   @Override
   public List<String> sort(String key, SortingParams sortingParams) {
      return this.executeCommand(this.commandObjects.sort(key, sortingParams));
   }

   @Override
   public long sort(String key, String dstkey) {
      return this.executeCommand(this.commandObjects.sort(key, dstkey));
   }

   @Override
   public long sort(String key, SortingParams sortingParams, String dstkey) {
      return this.executeCommand(this.commandObjects.sort(key, sortingParams, dstkey));
   }

   @Override
   public List<String> sortReadonly(String key, SortingParams sortingParams) {
      return this.executeCommand(this.commandObjects.sortReadonly(key, sortingParams));
   }

   @Override
   public List<byte[]> sort(byte[] key) {
      return this.executeCommand(this.commandObjects.sort(key));
   }

   @Override
   public List<byte[]> sort(byte[] key, SortingParams sortingParams) {
      return this.executeCommand(this.commandObjects.sort(key, sortingParams));
   }

   @Override
   public long sort(byte[] key, byte[] dstkey) {
      return this.executeCommand(this.commandObjects.sort(key, dstkey));
   }

   @Override
   public List<byte[]> sortReadonly(byte[] key, SortingParams sortingParams) {
      return this.executeCommand(this.commandObjects.sortReadonly(key, sortingParams));
   }

   @Override
   public long sort(byte[] key, SortingParams sortingParams, byte[] dstkey) {
      return this.executeCommand(this.commandObjects.sort(key, sortingParams, dstkey));
   }

   @Override
   public long del(String key) {
      return this.executeCommand(this.commandObjects.del(key));
   }

   @Override
   public long del(String... keys) {
      return this.executeCommand(this.commandObjects.del(keys));
   }

   @Override
   public long unlink(String key) {
      return this.executeCommand(this.commandObjects.unlink(key));
   }

   @Override
   public long unlink(String... keys) {
      return this.executeCommand(this.commandObjects.unlink(keys));
   }

   @Override
   public long del(byte[] key) {
      return this.executeCommand(this.commandObjects.del(key));
   }

   @Override
   public long del(byte[]... keys) {
      return this.executeCommand(this.commandObjects.del(keys));
   }

   @Override
   public long unlink(byte[] key) {
      return this.executeCommand(this.commandObjects.unlink(key));
   }

   @Override
   public long unlink(byte[]... keys) {
      return this.executeCommand(this.commandObjects.unlink(keys));
   }

   @Override
   public Long memoryUsage(String key) {
      return this.executeCommand(this.commandObjects.memoryUsage(key));
   }

   @Override
   public Long memoryUsage(String key, int samples) {
      return this.executeCommand(this.commandObjects.memoryUsage(key, samples));
   }

   @Override
   public Long memoryUsage(byte[] key) {
      return this.executeCommand(this.commandObjects.memoryUsage(key));
   }

   @Override
   public Long memoryUsage(byte[] key, int samples) {
      return this.executeCommand(this.commandObjects.memoryUsage(key, samples));
   }

   @Override
   public boolean copy(String srcKey, String dstKey, boolean replace) {
      return this.executeCommand(this.commandObjects.copy(srcKey, dstKey, replace));
   }

   @Override
   public String rename(String oldkey, String newkey) {
      return this.executeCommand(this.commandObjects.rename(oldkey, newkey));
   }

   @Override
   public long renamenx(String oldkey, String newkey) {
      return this.executeCommand(this.commandObjects.renamenx(oldkey, newkey));
   }

   @Override
   public boolean copy(byte[] srcKey, byte[] dstKey, boolean replace) {
      return this.executeCommand(this.commandObjects.copy(srcKey, dstKey, replace));
   }

   @Override
   public String rename(byte[] oldkey, byte[] newkey) {
      return this.executeCommand(this.commandObjects.rename(oldkey, newkey));
   }

   @Override
   public long renamenx(byte[] oldkey, byte[] newkey) {
      return this.executeCommand(this.commandObjects.renamenx(oldkey, newkey));
   }

   public long dbSize() {
      return this.executeCommand(this.commandObjects.dbSize());
   }

   @Override
   public Set<String> keys(String pattern) {
      return this.executeCommand(this.commandObjects.keys(pattern));
   }

   @Override
   public ScanResult<String> scan(String cursor) {
      return this.executeCommand(this.commandObjects.scan(cursor));
   }

   @Override
   public ScanResult<String> scan(String cursor, ScanParams params) {
      return this.executeCommand(this.commandObjects.scan(cursor, params));
   }

   @Override
   public ScanResult<String> scan(String cursor, ScanParams params, String type) {
      return this.executeCommand(this.commandObjects.scan(cursor, params, type));
   }

   public ScanIteration scanIteration(int batchCount, String match) {
      return new ScanIteration(this.provider, batchCount, match);
   }

   public ScanIteration scanIteration(int batchCount, String match, String type) {
      return new ScanIteration(this.provider, batchCount, match, type);
   }

   @Override
   public Set<byte[]> keys(byte[] pattern) {
      return this.executeCommand(this.commandObjects.keys(pattern));
   }

   @Override
   public ScanResult<byte[]> scan(byte[] cursor) {
      return this.executeCommand(this.commandObjects.scan(cursor));
   }

   @Override
   public ScanResult<byte[]> scan(byte[] cursor, ScanParams params) {
      return this.executeCommand(this.commandObjects.scan(cursor, params));
   }

   @Override
   public ScanResult<byte[]> scan(byte[] cursor, ScanParams params, byte[] type) {
      return this.executeCommand(this.commandObjects.scan(cursor, params, type));
   }

   @Override
   public String randomKey() {
      return this.executeCommand(this.commandObjects.randomKey());
   }

   @Override
   public byte[] randomBinaryKey() {
      return this.executeCommand(this.commandObjects.randomBinaryKey());
   }

   @Override
   public String set(String key, String value) {
      return this.executeCommand(this.commandObjects.set(key, value));
   }

   @Override
   public String set(String key, String value, SetParams params) {
      return this.executeCommand(this.commandObjects.set(key, value, params));
   }

   @Override
   public String get(String key) {
      return this.executeCommand(this.commandObjects.get(key));
   }

   @Override
   public String setGet(String key, String value) {
      return this.executeCommand(this.commandObjects.setGet(key, value));
   }

   @Override
   public String setGet(String key, String value, SetParams params) {
      return this.executeCommand(this.commandObjects.setGet(key, value, params));
   }

   @Override
   public String getDel(String key) {
      return this.executeCommand(this.commandObjects.getDel(key));
   }

   @Override
   public String getEx(String key, GetExParams params) {
      return this.executeCommand(this.commandObjects.getEx(key, params));
   }

   @Override
   public String set(byte[] key, byte[] value) {
      return this.executeCommand(this.commandObjects.set(key, value));
   }

   @Override
   public String set(byte[] key, byte[] value, SetParams params) {
      return this.executeCommand(this.commandObjects.set(key, value, params));
   }

   @Override
   public byte[] get(byte[] key) {
      return this.executeCommand(this.commandObjects.get(key));
   }

   @Override
   public byte[] setGet(byte[] key, byte[] value) {
      return this.executeCommand(this.commandObjects.setGet(key, value));
   }

   @Override
   public byte[] setGet(byte[] key, byte[] value, SetParams params) {
      return this.executeCommand(this.commandObjects.setGet(key, value, params));
   }

   @Override
   public byte[] getDel(byte[] key) {
      return this.executeCommand(this.commandObjects.getDel(key));
   }

   @Override
   public byte[] getEx(byte[] key, GetExParams params) {
      return this.executeCommand(this.commandObjects.getEx(key, params));
   }

   @Override
   public boolean setbit(String key, long offset, boolean value) {
      return this.executeCommand(this.commandObjects.setbit(key, offset, value));
   }

   @Override
   public boolean getbit(String key, long offset) {
      return this.executeCommand(this.commandObjects.getbit(key, offset));
   }

   @Override
   public long setrange(String key, long offset, String value) {
      return this.executeCommand(this.commandObjects.setrange(key, offset, value));
   }

   @Override
   public String getrange(String key, long startOffset, long endOffset) {
      return this.executeCommand(this.commandObjects.getrange(key, startOffset, endOffset));
   }

   @Override
   public boolean setbit(byte[] key, long offset, boolean value) {
      return this.executeCommand(this.commandObjects.setbit(key, offset, value));
   }

   @Override
   public boolean getbit(byte[] key, long offset) {
      return this.executeCommand(this.commandObjects.getbit(key, offset));
   }

   @Override
   public long setrange(byte[] key, long offset, byte[] value) {
      return this.executeCommand(this.commandObjects.setrange(key, offset, value));
   }

   @Override
   public byte[] getrange(byte[] key, long startOffset, long endOffset) {
      return this.executeCommand(this.commandObjects.getrange(key, startOffset, endOffset));
   }

   @Deprecated
   @Override
   public String getSet(String key, String value) {
      return this.executeCommand(this.commandObjects.getSet(key, value));
   }

   @Override
   public long setnx(String key, String value) {
      return this.executeCommand(this.commandObjects.setnx(key, value));
   }

   @Override
   public String setex(String key, long seconds, String value) {
      return this.executeCommand(this.commandObjects.setex(key, seconds, value));
   }

   @Override
   public String psetex(String key, long milliseconds, String value) {
      return this.executeCommand(this.commandObjects.psetex(key, milliseconds, value));
   }

   @Deprecated
   @Override
   public byte[] getSet(byte[] key, byte[] value) {
      return this.executeCommand(this.commandObjects.getSet(key, value));
   }

   @Override
   public long setnx(byte[] key, byte[] value) {
      return this.executeCommand(this.commandObjects.setnx(key, value));
   }

   @Override
   public String setex(byte[] key, long seconds, byte[] value) {
      return this.executeCommand(this.commandObjects.setex(key, seconds, value));
   }

   @Override
   public String psetex(byte[] key, long milliseconds, byte[] value) {
      return this.executeCommand(this.commandObjects.psetex(key, milliseconds, value));
   }

   @Override
   public long incr(String key) {
      return this.executeCommand(this.commandObjects.incr(key));
   }

   @Override
   public long incrBy(String key, long increment) {
      return this.executeCommand(this.commandObjects.incrBy(key, increment));
   }

   @Override
   public double incrByFloat(String key, double increment) {
      return this.executeCommand(this.commandObjects.incrByFloat(key, increment));
   }

   @Override
   public long decr(String key) {
      return this.executeCommand(this.commandObjects.decr(key));
   }

   @Override
   public long decrBy(String key, long decrement) {
      return this.executeCommand(this.commandObjects.decrBy(key, decrement));
   }

   @Override
   public long incr(byte[] key) {
      return this.executeCommand(this.commandObjects.incr(key));
   }

   @Override
   public long incrBy(byte[] key, long increment) {
      return this.executeCommand(this.commandObjects.incrBy(key, increment));
   }

   @Override
   public double incrByFloat(byte[] key, double increment) {
      return this.executeCommand(this.commandObjects.incrByFloat(key, increment));
   }

   @Override
   public long decr(byte[] key) {
      return this.executeCommand(this.commandObjects.decr(key));
   }

   @Override
   public long decrBy(byte[] key, long decrement) {
      return this.executeCommand(this.commandObjects.decrBy(key, decrement));
   }

   @Override
   public List<String> mget(String... keys) {
      return this.executeCommand(this.commandObjects.mget(keys));
   }

   @Override
   public String mset(String... keysvalues) {
      return this.executeCommand(this.commandObjects.mset(keysvalues));
   }

   @Override
   public long msetnx(String... keysvalues) {
      return this.executeCommand(this.commandObjects.msetnx(keysvalues));
   }

   @Override
   public List<byte[]> mget(byte[]... keys) {
      return this.executeCommand(this.commandObjects.mget(keys));
   }

   @Override
   public String mset(byte[]... keysvalues) {
      return this.executeCommand(this.commandObjects.mset(keysvalues));
   }

   @Override
   public long msetnx(byte[]... keysvalues) {
      return this.executeCommand(this.commandObjects.msetnx(keysvalues));
   }

   @Override
   public long append(String key, String value) {
      return this.executeCommand(this.commandObjects.append(key, value));
   }

   @Override
   public String substr(String key, int start, int end) {
      return this.executeCommand(this.commandObjects.substr(key, start, end));
   }

   @Override
   public long strlen(String key) {
      return this.executeCommand(this.commandObjects.strlen(key));
   }

   @Override
   public long append(byte[] key, byte[] value) {
      return this.executeCommand(this.commandObjects.append(key, value));
   }

   @Override
   public byte[] substr(byte[] key, int start, int end) {
      return this.executeCommand(this.commandObjects.substr(key, start, end));
   }

   @Override
   public long strlen(byte[] key) {
      return this.executeCommand(this.commandObjects.strlen(key));
   }

   @Override
   public long bitcount(String key) {
      return this.executeCommand(this.commandObjects.bitcount(key));
   }

   @Override
   public long bitcount(String key, long start, long end) {
      return this.executeCommand(this.commandObjects.bitcount(key, start, end));
   }

   @Override
   public long bitcount(String key, long start, long end, BitCountOption option) {
      return this.executeCommand(this.commandObjects.bitcount(key, start, end, option));
   }

   @Override
   public long bitpos(String key, boolean value) {
      return this.executeCommand(this.commandObjects.bitpos(key, value));
   }

   @Override
   public long bitpos(String key, boolean value, BitPosParams params) {
      return this.executeCommand(this.commandObjects.bitpos(key, value, params));
   }

   @Override
   public long bitcount(byte[] key) {
      return this.executeCommand(this.commandObjects.bitcount(key));
   }

   @Override
   public long bitcount(byte[] key, long start, long end) {
      return this.executeCommand(this.commandObjects.bitcount(key, start, end));
   }

   @Override
   public long bitcount(byte[] key, long start, long end, BitCountOption option) {
      return this.executeCommand(this.commandObjects.bitcount(key, start, end, option));
   }

   @Override
   public long bitpos(byte[] key, boolean value) {
      return this.executeCommand(this.commandObjects.bitpos(key, value));
   }

   @Override
   public long bitpos(byte[] key, boolean value, BitPosParams params) {
      return this.executeCommand(this.commandObjects.bitpos(key, value, params));
   }

   @Override
   public List<Long> bitfield(String key, String... arguments) {
      return this.executeCommand(this.commandObjects.bitfield(key, arguments));
   }

   @Override
   public List<Long> bitfieldReadonly(String key, String... arguments) {
      return this.executeCommand(this.commandObjects.bitfieldReadonly(key, arguments));
   }

   @Override
   public List<Long> bitfield(byte[] key, byte[]... arguments) {
      return this.executeCommand(this.commandObjects.bitfield(key, arguments));
   }

   @Override
   public List<Long> bitfieldReadonly(byte[] key, byte[]... arguments) {
      return this.executeCommand(this.commandObjects.bitfieldReadonly(key, arguments));
   }

   @Override
   public long bitop(BitOP op, String destKey, String... srcKeys) {
      return this.executeCommand(this.commandObjects.bitop(op, destKey, srcKeys));
   }

   @Override
   public long bitop(BitOP op, byte[] destKey, byte[]... srcKeys) {
      return this.executeCommand(this.commandObjects.bitop(op, destKey, srcKeys));
   }

   @Override
   public LCSMatchResult lcs(String keyA, String keyB, LCSParams params) {
      return this.executeCommand(this.commandObjects.lcs(keyA, keyB, params));
   }

   @Override
   public LCSMatchResult lcs(byte[] keyA, byte[] keyB, LCSParams params) {
      return this.executeCommand(this.commandObjects.lcs(keyA, keyB, params));
   }

   @Override
   public long rpush(String key, String... string) {
      return this.executeCommand(this.commandObjects.rpush(key, string));
   }

   @Override
   public long lpush(String key, String... string) {
      return this.executeCommand(this.commandObjects.lpush(key, string));
   }

   @Override
   public long llen(String key) {
      return this.executeCommand(this.commandObjects.llen(key));
   }

   @Override
   public List<String> lrange(String key, long start, long stop) {
      return this.executeCommand(this.commandObjects.lrange(key, start, stop));
   }

   @Override
   public String ltrim(String key, long start, long stop) {
      return this.executeCommand(this.commandObjects.ltrim(key, start, stop));
   }

   @Override
   public String lindex(String key, long index) {
      return this.executeCommand(this.commandObjects.lindex(key, index));
   }

   @Override
   public long rpush(byte[] key, byte[]... args) {
      return this.executeCommand(this.commandObjects.rpush(key, args));
   }

   @Override
   public long lpush(byte[] key, byte[]... args) {
      return this.executeCommand(this.commandObjects.lpush(key, args));
   }

   @Override
   public long llen(byte[] key) {
      return this.executeCommand(this.commandObjects.llen(key));
   }

   @Override
   public List<byte[]> lrange(byte[] key, long start, long stop) {
      return this.executeCommand(this.commandObjects.lrange(key, start, stop));
   }

   @Override
   public String ltrim(byte[] key, long start, long stop) {
      return this.executeCommand(this.commandObjects.ltrim(key, start, stop));
   }

   @Override
   public byte[] lindex(byte[] key, long index) {
      return this.executeCommand(this.commandObjects.lindex(key, index));
   }

   @Override
   public String lset(String key, long index, String value) {
      return this.executeCommand(this.commandObjects.lset(key, index, value));
   }

   @Override
   public long lrem(String key, long count, String value) {
      return this.executeCommand(this.commandObjects.lrem(key, count, value));
   }

   @Override
   public String lpop(String key) {
      return this.executeCommand(this.commandObjects.lpop(key));
   }

   @Override
   public List<String> lpop(String key, int count) {
      return this.executeCommand(this.commandObjects.lpop(key, count));
   }

   @Override
   public String lset(byte[] key, long index, byte[] value) {
      return this.executeCommand(this.commandObjects.lset(key, index, value));
   }

   @Override
   public long lrem(byte[] key, long count, byte[] value) {
      return this.executeCommand(this.commandObjects.lrem(key, count, value));
   }

   @Override
   public byte[] lpop(byte[] key) {
      return this.executeCommand(this.commandObjects.lpop(key));
   }

   @Override
   public List<byte[]> lpop(byte[] key, int count) {
      return this.executeCommand(this.commandObjects.lpop(key, count));
   }

   @Override
   public Long lpos(String key, String element) {
      return this.executeCommand(this.commandObjects.lpos(key, element));
   }

   @Override
   public Long lpos(String key, String element, LPosParams params) {
      return this.executeCommand(this.commandObjects.lpos(key, element, params));
   }

   @Override
   public List<Long> lpos(String key, String element, LPosParams params, long count) {
      return this.executeCommand(this.commandObjects.lpos(key, element, params, count));
   }

   @Override
   public Long lpos(byte[] key, byte[] element) {
      return this.executeCommand(this.commandObjects.lpos(key, element));
   }

   @Override
   public Long lpos(byte[] key, byte[] element, LPosParams params) {
      return this.executeCommand(this.commandObjects.lpos(key, element, params));
   }

   @Override
   public List<Long> lpos(byte[] key, byte[] element, LPosParams params, long count) {
      return this.executeCommand(this.commandObjects.lpos(key, element, params, count));
   }

   @Override
   public String rpop(String key) {
      return this.executeCommand(this.commandObjects.rpop(key));
   }

   @Override
   public List<String> rpop(String key, int count) {
      return this.executeCommand(this.commandObjects.rpop(key, count));
   }

   @Override
   public byte[] rpop(byte[] key) {
      return this.executeCommand(this.commandObjects.rpop(key));
   }

   @Override
   public List<byte[]> rpop(byte[] key, int count) {
      return this.executeCommand(this.commandObjects.rpop(key, count));
   }

   @Override
   public long linsert(String key, ListPosition where, String pivot, String value) {
      return this.executeCommand(this.commandObjects.linsert(key, where, pivot, value));
   }

   @Override
   public long lpushx(String key, String... strings) {
      return this.executeCommand(this.commandObjects.lpushx(key, strings));
   }

   @Override
   public long rpushx(String key, String... strings) {
      return this.executeCommand(this.commandObjects.rpushx(key, strings));
   }

   @Override
   public long linsert(byte[] key, ListPosition where, byte[] pivot, byte[] value) {
      return this.executeCommand(this.commandObjects.linsert(key, where, pivot, value));
   }

   @Override
   public long lpushx(byte[] key, byte[]... args) {
      return this.executeCommand(this.commandObjects.lpushx(key, args));
   }

   @Override
   public long rpushx(byte[] key, byte[]... args) {
      return this.executeCommand(this.commandObjects.rpushx(key, args));
   }

   @Override
   public List<String> blpop(int timeout, String key) {
      return this.executeCommand(this.commandObjects.blpop(timeout, key));
   }

   @Override
   public KeyValue<String, String> blpop(double timeout, String key) {
      return this.executeCommand(this.commandObjects.blpop(timeout, key));
   }

   @Override
   public List<String> brpop(int timeout, String key) {
      return this.executeCommand(this.commandObjects.brpop(timeout, key));
   }

   @Override
   public KeyValue<String, String> brpop(double timeout, String key) {
      return this.executeCommand(this.commandObjects.brpop(timeout, key));
   }

   @Override
   public List<String> blpop(int timeout, String... keys) {
      return this.executeCommand(this.commandObjects.blpop(timeout, keys));
   }

   @Override
   public KeyValue<String, String> blpop(double timeout, String... keys) {
      return this.executeCommand(this.commandObjects.blpop(timeout, keys));
   }

   @Override
   public List<String> brpop(int timeout, String... keys) {
      return this.executeCommand(this.commandObjects.brpop(timeout, keys));
   }

   @Override
   public KeyValue<String, String> brpop(double timeout, String... keys) {
      return this.executeCommand(this.commandObjects.brpop(timeout, keys));
   }

   @Override
   public List<byte[]> blpop(int timeout, byte[]... keys) {
      return this.executeCommand(this.commandObjects.blpop(timeout, keys));
   }

   @Override
   public KeyValue<byte[], byte[]> blpop(double timeout, byte[]... keys) {
      return this.executeCommand(this.commandObjects.blpop(timeout, keys));
   }

   @Override
   public List<byte[]> brpop(int timeout, byte[]... keys) {
      return this.executeCommand(this.commandObjects.brpop(timeout, keys));
   }

   @Override
   public KeyValue<byte[], byte[]> brpop(double timeout, byte[]... keys) {
      return this.executeCommand(this.commandObjects.brpop(timeout, keys));
   }

   @Override
   public String rpoplpush(String srckey, String dstkey) {
      return this.executeCommand(this.commandObjects.rpoplpush(srckey, dstkey));
   }

   @Override
   public String brpoplpush(String source, String destination, int timeout) {
      return this.executeCommand(this.commandObjects.brpoplpush(source, destination, timeout));
   }

   @Override
   public byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
      return this.executeCommand(this.commandObjects.rpoplpush(srckey, dstkey));
   }

   @Override
   public byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
      return this.executeCommand(this.commandObjects.brpoplpush(source, destination, timeout));
   }

   @Override
   public String lmove(String srcKey, String dstKey, ListDirection from, ListDirection to) {
      return this.executeCommand(this.commandObjects.lmove(srcKey, dstKey, from, to));
   }

   @Override
   public String blmove(String srcKey, String dstKey, ListDirection from, ListDirection to, double timeout) {
      return this.executeCommand(this.commandObjects.blmove(srcKey, dstKey, from, to, timeout));
   }

   @Override
   public byte[] lmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to) {
      return this.executeCommand(this.commandObjects.lmove(srcKey, dstKey, from, to));
   }

   @Override
   public byte[] blmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to, double timeout) {
      return this.executeCommand(this.commandObjects.blmove(srcKey, dstKey, from, to, timeout));
   }

   @Override
   public KeyValue<String, List<String>> lmpop(ListDirection direction, String... keys) {
      return this.executeCommand(this.commandObjects.lmpop(direction, keys));
   }

   @Override
   public KeyValue<String, List<String>> lmpop(ListDirection direction, int count, String... keys) {
      return this.executeCommand(this.commandObjects.lmpop(direction, count, keys));
   }

   @Override
   public KeyValue<String, List<String>> blmpop(double timeout, ListDirection direction, String... keys) {
      return this.executeCommand(this.commandObjects.blmpop(timeout, direction, keys));
   }

   @Override
   public KeyValue<String, List<String>> blmpop(double timeout, ListDirection direction, int count, String... keys) {
      return this.executeCommand(this.commandObjects.blmpop(timeout, direction, count, keys));
   }

   @Override
   public KeyValue<byte[], List<byte[]>> lmpop(ListDirection direction, byte[]... keys) {
      return this.executeCommand(this.commandObjects.lmpop(direction, keys));
   }

   @Override
   public KeyValue<byte[], List<byte[]>> lmpop(ListDirection direction, int count, byte[]... keys) {
      return this.executeCommand(this.commandObjects.lmpop(direction, count, keys));
   }

   @Override
   public KeyValue<byte[], List<byte[]>> blmpop(double timeout, ListDirection direction, byte[]... keys) {
      return this.executeCommand(this.commandObjects.blmpop(timeout, direction, keys));
   }

   @Override
   public KeyValue<byte[], List<byte[]>> blmpop(double timeout, ListDirection direction, int count, byte[]... keys) {
      return this.executeCommand(this.commandObjects.blmpop(timeout, direction, count, keys));
   }

   @Override
   public long hset(String key, String field, String value) {
      return this.executeCommand(this.commandObjects.hset(key, field, value));
   }

   @Override
   public long hset(String key, Map<String, String> hash) {
      return this.executeCommand(this.commandObjects.hset(key, hash));
   }

   @Override
   public String hget(String key, String field) {
      return this.executeCommand(this.commandObjects.hget(key, field));
   }

   @Override
   public long hsetnx(String key, String field, String value) {
      return this.executeCommand(this.commandObjects.hsetnx(key, field, value));
   }

   @Override
   public String hmset(String key, Map<String, String> hash) {
      return this.executeCommand(this.commandObjects.hmset(key, hash));
   }

   @Override
   public List<String> hmget(String key, String... fields) {
      return this.executeCommand(this.commandObjects.hmget(key, fields));
   }

   @Override
   public long hset(byte[] key, byte[] field, byte[] value) {
      return this.executeCommand(this.commandObjects.hset(key, field, value));
   }

   @Override
   public long hset(byte[] key, Map<byte[], byte[]> hash) {
      return this.executeCommand(this.commandObjects.hset(key, hash));
   }

   @Override
   public byte[] hget(byte[] key, byte[] field) {
      return this.executeCommand(this.commandObjects.hget(key, field));
   }

   @Override
   public long hsetnx(byte[] key, byte[] field, byte[] value) {
      return this.executeCommand(this.commandObjects.hsetnx(key, field, value));
   }

   @Override
   public String hmset(byte[] key, Map<byte[], byte[]> hash) {
      return this.executeCommand(this.commandObjects.hmset(key, hash));
   }

   @Override
   public List<byte[]> hmget(byte[] key, byte[]... fields) {
      return this.executeCommand(this.commandObjects.hmget(key, fields));
   }

   @Override
   public long hincrBy(String key, String field, long value) {
      return this.executeCommand(this.commandObjects.hincrBy(key, field, value));
   }

   @Override
   public double hincrByFloat(String key, String field, double value) {
      return this.executeCommand(this.commandObjects.hincrByFloat(key, field, value));
   }

   @Override
   public boolean hexists(String key, String field) {
      return this.executeCommand(this.commandObjects.hexists(key, field));
   }

   @Override
   public long hdel(String key, String... field) {
      return this.executeCommand(this.commandObjects.hdel(key, field));
   }

   @Override
   public long hlen(String key) {
      return this.executeCommand(this.commandObjects.hlen(key));
   }

   @Override
   public long hincrBy(byte[] key, byte[] field, long value) {
      return this.executeCommand(this.commandObjects.hincrBy(key, field, value));
   }

   @Override
   public double hincrByFloat(byte[] key, byte[] field, double value) {
      return this.executeCommand(this.commandObjects.hincrByFloat(key, field, value));
   }

   @Override
   public boolean hexists(byte[] key, byte[] field) {
      return this.executeCommand(this.commandObjects.hexists(key, field));
   }

   @Override
   public long hdel(byte[] key, byte[]... field) {
      return this.executeCommand(this.commandObjects.hdel(key, field));
   }

   @Override
   public long hlen(byte[] key) {
      return this.executeCommand(this.commandObjects.hlen(key));
   }

   @Override
   public Set<String> hkeys(String key) {
      return this.executeCommand(this.commandObjects.hkeys(key));
   }

   @Override
   public List<String> hvals(String key) {
      return this.executeCommand(this.commandObjects.hvals(key));
   }

   @Override
   public Map<String, String> hgetAll(String key) {
      return this.executeCommand(this.commandObjects.hgetAll(key));
   }

   @Override
   public Set<byte[]> hkeys(byte[] key) {
      return this.executeCommand(this.commandObjects.hkeys(key));
   }

   @Override
   public List<byte[]> hvals(byte[] key) {
      return this.executeCommand(this.commandObjects.hvals(key));
   }

   @Override
   public Map<byte[], byte[]> hgetAll(byte[] key) {
      return this.executeCommand(this.commandObjects.hgetAll(key));
   }

   @Override
   public String hrandfield(String key) {
      return this.executeCommand(this.commandObjects.hrandfield(key));
   }

   @Override
   public List<String> hrandfield(String key, long count) {
      return this.executeCommand(this.commandObjects.hrandfield(key, count));
   }

   @Override
   public List<Entry<String, String>> hrandfieldWithValues(String key, long count) {
      return this.executeCommand(this.commandObjects.hrandfieldWithValues(key, count));
   }

   @Override
   public ScanResult<Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
      return this.executeCommand(this.commandObjects.hscan(key, cursor, params));
   }

   @Override
   public ScanResult<String> hscanNoValues(String key, String cursor, ScanParams params) {
      return this.executeCommand(this.commandObjects.hscanNoValues(key, cursor, params));
   }

   @Override
   public long hstrlen(String key, String field) {
      return this.executeCommand(this.commandObjects.hstrlen(key, field));
   }

   @Override
   public byte[] hrandfield(byte[] key) {
      return this.executeCommand(this.commandObjects.hrandfield(key));
   }

   @Override
   public List<byte[]> hrandfield(byte[] key, long count) {
      return this.executeCommand(this.commandObjects.hrandfield(key, count));
   }

   @Override
   public List<Entry<byte[], byte[]>> hrandfieldWithValues(byte[] key, long count) {
      return this.executeCommand(this.commandObjects.hrandfieldWithValues(key, count));
   }

   @Override
   public ScanResult<Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor, ScanParams params) {
      return this.executeCommand(this.commandObjects.hscan(key, cursor, params));
   }

   @Override
   public ScanResult<byte[]> hscanNoValues(byte[] key, byte[] cursor, ScanParams params) {
      return this.executeCommand(this.commandObjects.hscanNoValues(key, cursor, params));
   }

   @Override
   public long hstrlen(byte[] key, byte[] field) {
      return this.executeCommand(this.commandObjects.hstrlen(key, field));
   }

   @Override
   public List<Long> hexpire(String key, long seconds, String... fields) {
      return this.executeCommand(this.commandObjects.hexpire(key, seconds, fields));
   }

   @Override
   public List<Long> hexpire(String key, long seconds, ExpiryOption condition, String... fields) {
      return this.executeCommand(this.commandObjects.hexpire(key, seconds, condition, fields));
   }

   @Override
   public List<Long> hpexpire(String key, long milliseconds, String... fields) {
      return this.executeCommand(this.commandObjects.hpexpire(key, milliseconds, fields));
   }

   @Override
   public List<Long> hpexpire(String key, long milliseconds, ExpiryOption condition, String... fields) {
      return this.executeCommand(this.commandObjects.hpexpire(key, milliseconds, condition, fields));
   }

   @Override
   public List<Long> hexpireAt(String key, long unixTimeSeconds, String... fields) {
      return this.executeCommand(this.commandObjects.hexpireAt(key, unixTimeSeconds, fields));
   }

   @Override
   public List<Long> hexpireAt(String key, long unixTimeSeconds, ExpiryOption condition, String... fields) {
      return this.executeCommand(this.commandObjects.hexpireAt(key, unixTimeSeconds, condition, fields));
   }

   @Override
   public List<Long> hpexpireAt(String key, long unixTimeMillis, String... fields) {
      return this.executeCommand(this.commandObjects.hpexpireAt(key, unixTimeMillis, fields));
   }

   @Override
   public List<Long> hpexpireAt(String key, long unixTimeMillis, ExpiryOption condition, String... fields) {
      return this.executeCommand(this.commandObjects.hpexpireAt(key, unixTimeMillis, condition, fields));
   }

   @Override
   public List<Long> hexpire(byte[] key, long seconds, byte[]... fields) {
      return this.executeCommand(this.commandObjects.hexpire(key, seconds, fields));
   }

   @Override
   public List<Long> hexpire(byte[] key, long seconds, ExpiryOption condition, byte[]... fields) {
      return this.executeCommand(this.commandObjects.hexpire(key, seconds, condition, fields));
   }

   @Override
   public List<Long> hpexpire(byte[] key, long milliseconds, byte[]... fields) {
      return this.executeCommand(this.commandObjects.hpexpire(key, milliseconds, fields));
   }

   @Override
   public List<Long> hpexpire(byte[] key, long milliseconds, ExpiryOption condition, byte[]... fields) {
      return this.executeCommand(this.commandObjects.hpexpire(key, milliseconds, condition, fields));
   }

   @Override
   public List<Long> hexpireAt(byte[] key, long unixTimeSeconds, byte[]... fields) {
      return this.executeCommand(this.commandObjects.hexpireAt(key, unixTimeSeconds, fields));
   }

   @Override
   public List<Long> hexpireAt(byte[] key, long unixTimeSeconds, ExpiryOption condition, byte[]... fields) {
      return this.executeCommand(this.commandObjects.hexpireAt(key, unixTimeSeconds, condition, fields));
   }

   @Override
   public List<Long> hpexpireAt(byte[] key, long unixTimeMillis, byte[]... fields) {
      return this.executeCommand(this.commandObjects.hpexpireAt(key, unixTimeMillis, fields));
   }

   @Override
   public List<Long> hpexpireAt(byte[] key, long unixTimeMillis, ExpiryOption condition, byte[]... fields) {
      return this.executeCommand(this.commandObjects.hpexpireAt(key, unixTimeMillis, condition, fields));
   }

   @Override
   public List<Long> hexpireTime(String key, String... fields) {
      return this.executeCommand(this.commandObjects.hexpireTime(key, fields));
   }

   @Override
   public List<Long> hpexpireTime(String key, String... fields) {
      return this.executeCommand(this.commandObjects.hpexpireTime(key, fields));
   }

   @Override
   public List<Long> httl(String key, String... fields) {
      return this.executeCommand(this.commandObjects.httl(key, fields));
   }

   @Override
   public List<Long> hpttl(String key, String... fields) {
      return this.executeCommand(this.commandObjects.hpttl(key, fields));
   }

   @Override
   public List<Long> hexpireTime(byte[] key, byte[]... fields) {
      return this.executeCommand(this.commandObjects.hexpireTime(key, fields));
   }

   @Override
   public List<Long> hpexpireTime(byte[] key, byte[]... fields) {
      return this.executeCommand(this.commandObjects.hpexpireTime(key, fields));
   }

   @Override
   public List<Long> httl(byte[] key, byte[]... fields) {
      return this.executeCommand(this.commandObjects.httl(key, fields));
   }

   @Override
   public List<Long> hpttl(byte[] key, byte[]... fields) {
      return this.executeCommand(this.commandObjects.hpttl(key, fields));
   }

   @Override
   public List<Long> hpersist(String key, String... fields) {
      return this.executeCommand(this.commandObjects.hpersist(key, fields));
   }

   @Override
   public List<Long> hpersist(byte[] key, byte[]... fields) {
      return this.executeCommand(this.commandObjects.hpersist(key, fields));
   }

   @Override
   public long sadd(String key, String... members) {
      return this.executeCommand(this.commandObjects.sadd(key, members));
   }

   @Override
   public Set<String> smembers(String key) {
      return this.executeCommand(this.commandObjects.smembers(key));
   }

   @Override
   public long srem(String key, String... members) {
      return this.executeCommand(this.commandObjects.srem(key, members));
   }

   @Override
   public String spop(String key) {
      return this.executeCommand(this.commandObjects.spop(key));
   }

   @Override
   public Set<String> spop(String key, long count) {
      return this.executeCommand(this.commandObjects.spop(key, count));
   }

   @Override
   public long scard(String key) {
      return this.executeCommand(this.commandObjects.scard(key));
   }

   @Override
   public boolean sismember(String key, String member) {
      return this.executeCommand(this.commandObjects.sismember(key, member));
   }

   @Override
   public List<Boolean> smismember(String key, String... members) {
      return this.executeCommand(this.commandObjects.smismember(key, members));
   }

   @Override
   public long sadd(byte[] key, byte[]... members) {
      return this.executeCommand(this.commandObjects.sadd(key, members));
   }

   @Override
   public Set<byte[]> smembers(byte[] key) {
      return this.executeCommand(this.commandObjects.smembers(key));
   }

   @Override
   public long srem(byte[] key, byte[]... members) {
      return this.executeCommand(this.commandObjects.srem(key, members));
   }

   @Override
   public byte[] spop(byte[] key) {
      return this.executeCommand(this.commandObjects.spop(key));
   }

   @Override
   public Set<byte[]> spop(byte[] key, long count) {
      return this.executeCommand(this.commandObjects.spop(key, count));
   }

   @Override
   public long scard(byte[] key) {
      return this.executeCommand(this.commandObjects.scard(key));
   }

   @Override
   public boolean sismember(byte[] key, byte[] member) {
      return this.executeCommand(this.commandObjects.sismember(key, member));
   }

   @Override
   public List<Boolean> smismember(byte[] key, byte[]... members) {
      return this.executeCommand(this.commandObjects.smismember(key, members));
   }

   @Override
   public String srandmember(String key) {
      return this.executeCommand(this.commandObjects.srandmember(key));
   }

   @Override
   public List<String> srandmember(String key, int count) {
      return this.executeCommand(this.commandObjects.srandmember(key, count));
   }

   @Override
   public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
      return this.executeCommand(this.commandObjects.sscan(key, cursor, params));
   }

   @Override
   public byte[] srandmember(byte[] key) {
      return this.executeCommand(this.commandObjects.srandmember(key));
   }

   @Override
   public List<byte[]> srandmember(byte[] key, int count) {
      return this.executeCommand(this.commandObjects.srandmember(key, count));
   }

   @Override
   public ScanResult<byte[]> sscan(byte[] key, byte[] cursor, ScanParams params) {
      return this.executeCommand(this.commandObjects.sscan(key, cursor, params));
   }

   @Override
   public Set<String> sdiff(String... keys) {
      return this.executeCommand(this.commandObjects.sdiff(keys));
   }

   @Override
   public long sdiffstore(String dstkey, String... keys) {
      return this.executeCommand(this.commandObjects.sdiffstore(dstkey, keys));
   }

   @Override
   public Set<String> sinter(String... keys) {
      return this.executeCommand(this.commandObjects.sinter(keys));
   }

   @Override
   public long sinterstore(String dstkey, String... keys) {
      return this.executeCommand(this.commandObjects.sinterstore(dstkey, keys));
   }

   @Override
   public long sintercard(String... keys) {
      return this.executeCommand(this.commandObjects.sintercard(keys));
   }

   @Override
   public long sintercard(int limit, String... keys) {
      return this.executeCommand(this.commandObjects.sintercard(limit, keys));
   }

   @Override
   public Set<String> sunion(String... keys) {
      return this.executeCommand(this.commandObjects.sunion(keys));
   }

   @Override
   public long sunionstore(String dstkey, String... keys) {
      return this.executeCommand(this.commandObjects.sunionstore(dstkey, keys));
   }

   @Override
   public long smove(String srckey, String dstkey, String member) {
      return this.executeCommand(this.commandObjects.smove(srckey, dstkey, member));
   }

   @Override
   public Set<byte[]> sdiff(byte[]... keys) {
      return this.executeCommand(this.commandObjects.sdiff(keys));
   }

   @Override
   public long sdiffstore(byte[] dstkey, byte[]... keys) {
      return this.executeCommand(this.commandObjects.sdiffstore(dstkey, keys));
   }

   @Override
   public Set<byte[]> sinter(byte[]... keys) {
      return this.executeCommand(this.commandObjects.sinter(keys));
   }

   @Override
   public long sinterstore(byte[] dstkey, byte[]... keys) {
      return this.executeCommand(this.commandObjects.sinterstore(dstkey, keys));
   }

   @Override
   public long sintercard(byte[]... keys) {
      return this.executeCommand(this.commandObjects.sintercard(keys));
   }

   @Override
   public long sintercard(int limit, byte[]... keys) {
      return this.executeCommand(this.commandObjects.sintercard(limit, keys));
   }

   @Override
   public Set<byte[]> sunion(byte[]... keys) {
      return this.executeCommand(this.commandObjects.sunion(keys));
   }

   @Override
   public long sunionstore(byte[] dstkey, byte[]... keys) {
      return this.executeCommand(this.commandObjects.sunionstore(dstkey, keys));
   }

   @Override
   public long smove(byte[] srckey, byte[] dstkey, byte[] member) {
      return this.executeCommand(this.commandObjects.smove(srckey, dstkey, member));
   }

   @Override
   public long zadd(String key, double score, String member) {
      return this.executeCommand(this.commandObjects.zadd(key, score, member));
   }

   @Override
   public long zadd(String key, double score, String member, ZAddParams params) {
      return this.executeCommand(this.commandObjects.zadd(key, score, member, params));
   }

   @Override
   public long zadd(String key, Map<String, Double> scoreMembers) {
      return this.executeCommand(this.commandObjects.zadd(key, scoreMembers));
   }

   @Override
   public long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
      return this.executeCommand(this.commandObjects.zadd(key, scoreMembers, params));
   }

   @Override
   public Double zaddIncr(String key, double score, String member, ZAddParams params) {
      return this.executeCommand(this.commandObjects.zaddIncr(key, score, member, params));
   }

   @Override
   public long zadd(byte[] key, double score, byte[] member) {
      return this.executeCommand(this.commandObjects.zadd(key, score, member));
   }

   @Override
   public long zadd(byte[] key, double score, byte[] member, ZAddParams params) {
      return this.executeCommand(this.commandObjects.zadd(key, score, member, params));
   }

   @Override
   public long zadd(byte[] key, Map<byte[], Double> scoreMembers) {
      return this.executeCommand(this.commandObjects.zadd(key, scoreMembers));
   }

   @Override
   public long zadd(byte[] key, Map<byte[], Double> scoreMembers, ZAddParams params) {
      return this.executeCommand(this.commandObjects.zadd(key, scoreMembers, params));
   }

   @Override
   public Double zaddIncr(byte[] key, double score, byte[] member, ZAddParams params) {
      return this.executeCommand(this.commandObjects.zaddIncr(key, score, member, params));
   }

   @Override
   public long zrem(String key, String... members) {
      return this.executeCommand(this.commandObjects.zrem(key, members));
   }

   @Override
   public double zincrby(String key, double increment, String member) {
      return this.executeCommand(this.commandObjects.zincrby(key, increment, member));
   }

   @Override
   public Double zincrby(String key, double increment, String member, ZIncrByParams params) {
      return this.executeCommand(this.commandObjects.zincrby(key, increment, member, params));
   }

   @Override
   public Long zrank(String key, String member) {
      return this.executeCommand(this.commandObjects.zrank(key, member));
   }

   @Override
   public Long zrevrank(String key, String member) {
      return this.executeCommand(this.commandObjects.zrevrank(key, member));
   }

   @Override
   public KeyValue<Long, Double> zrankWithScore(String key, String member) {
      return this.executeCommand(this.commandObjects.zrankWithScore(key, member));
   }

   @Override
   public KeyValue<Long, Double> zrevrankWithScore(String key, String member) {
      return this.executeCommand(this.commandObjects.zrevrankWithScore(key, member));
   }

   @Override
   public long zrem(byte[] key, byte[]... members) {
      return this.executeCommand(this.commandObjects.zrem(key, members));
   }

   @Override
   public double zincrby(byte[] key, double increment, byte[] member) {
      return this.executeCommand(this.commandObjects.zincrby(key, increment, member));
   }

   @Override
   public Double zincrby(byte[] key, double increment, byte[] member, ZIncrByParams params) {
      return this.executeCommand(this.commandObjects.zincrby(key, increment, member, params));
   }

   @Override
   public Long zrank(byte[] key, byte[] member) {
      return this.executeCommand(this.commandObjects.zrank(key, member));
   }

   @Override
   public Long zrevrank(byte[] key, byte[] member) {
      return this.executeCommand(this.commandObjects.zrevrank(key, member));
   }

   @Override
   public KeyValue<Long, Double> zrankWithScore(byte[] key, byte[] member) {
      return this.executeCommand(this.commandObjects.zrankWithScore(key, member));
   }

   @Override
   public KeyValue<Long, Double> zrevrankWithScore(byte[] key, byte[] member) {
      return this.executeCommand(this.commandObjects.zrevrankWithScore(key, member));
   }

   @Override
   public String zrandmember(String key) {
      return this.executeCommand(this.commandObjects.zrandmember(key));
   }

   @Override
   public List<String> zrandmember(String key, long count) {
      return this.executeCommand(this.commandObjects.zrandmember(key, count));
   }

   @Override
   public List<Tuple> zrandmemberWithScores(String key, long count) {
      return this.executeCommand(this.commandObjects.zrandmemberWithScores(key, count));
   }

   @Override
   public long zcard(String key) {
      return this.executeCommand(this.commandObjects.zcard(key));
   }

   @Override
   public Double zscore(String key, String member) {
      return this.executeCommand(this.commandObjects.zscore(key, member));
   }

   @Override
   public List<Double> zmscore(String key, String... members) {
      return this.executeCommand(this.commandObjects.zmscore(key, members));
   }

   @Override
   public byte[] zrandmember(byte[] key) {
      return this.executeCommand(this.commandObjects.zrandmember(key));
   }

   @Override
   public List<byte[]> zrandmember(byte[] key, long count) {
      return this.executeCommand(this.commandObjects.zrandmember(key, count));
   }

   @Override
   public List<Tuple> zrandmemberWithScores(byte[] key, long count) {
      return this.executeCommand(this.commandObjects.zrandmemberWithScores(key, count));
   }

   @Override
   public long zcard(byte[] key) {
      return this.executeCommand(this.commandObjects.zcard(key));
   }

   @Override
   public Double zscore(byte[] key, byte[] member) {
      return this.executeCommand(this.commandObjects.zscore(key, member));
   }

   @Override
   public List<Double> zmscore(byte[] key, byte[]... members) {
      return this.executeCommand(this.commandObjects.zmscore(key, members));
   }

   @Override
   public Tuple zpopmax(String key) {
      return this.executeCommand(this.commandObjects.zpopmax(key));
   }

   @Override
   public List<Tuple> zpopmax(String key, int count) {
      return this.executeCommand(this.commandObjects.zpopmax(key, count));
   }

   @Override
   public Tuple zpopmin(String key) {
      return this.executeCommand(this.commandObjects.zpopmin(key));
   }

   @Override
   public List<Tuple> zpopmin(String key, int count) {
      return this.executeCommand(this.commandObjects.zpopmin(key, count));
   }

   @Override
   public long zcount(String key, double min, double max) {
      return this.executeCommand(this.commandObjects.zcount(key, min, max));
   }

   @Override
   public long zcount(String key, String min, String max) {
      return this.executeCommand(this.commandObjects.zcount(key, min, max));
   }

   @Override
   public Tuple zpopmax(byte[] key) {
      return this.executeCommand(this.commandObjects.zpopmax(key));
   }

   @Override
   public List<Tuple> zpopmax(byte[] key, int count) {
      return this.executeCommand(this.commandObjects.zpopmax(key, count));
   }

   @Override
   public Tuple zpopmin(byte[] key) {
      return this.executeCommand(this.commandObjects.zpopmin(key));
   }

   @Override
   public List<Tuple> zpopmin(byte[] key, int count) {
      return this.executeCommand(this.commandObjects.zpopmin(key, count));
   }

   @Override
   public long zcount(byte[] key, double min, double max) {
      return this.executeCommand(this.commandObjects.zcount(key, min, max));
   }

   @Override
   public long zcount(byte[] key, byte[] min, byte[] max) {
      return this.executeCommand(this.commandObjects.zcount(key, min, max));
   }

   @Override
   public List<String> zrange(String key, long start, long stop) {
      return this.executeCommand(this.commandObjects.zrange(key, start, stop));
   }

   @Override
   public List<String> zrevrange(String key, long start, long stop) {
      return this.executeCommand(this.commandObjects.zrevrange(key, start, stop));
   }

   @Override
   public List<Tuple> zrangeWithScores(String key, long start, long stop) {
      return this.executeCommand(this.commandObjects.zrangeWithScores(key, start, stop));
   }

   @Override
   public List<Tuple> zrevrangeWithScores(String key, long start, long stop) {
      return this.executeCommand(this.commandObjects.zrevrangeWithScores(key, start, stop));
   }

   @Override
   public List<String> zrange(String key, ZRangeParams zRangeParams) {
      return this.executeCommand(this.commandObjects.zrange(key, zRangeParams));
   }

   @Override
   public List<Tuple> zrangeWithScores(String key, ZRangeParams zRangeParams) {
      return this.executeCommand(this.commandObjects.zrangeWithScores(key, zRangeParams));
   }

   @Override
   public long zrangestore(String dest, String src, ZRangeParams zRangeParams) {
      return this.executeCommand(this.commandObjects.zrangestore(dest, src, zRangeParams));
   }

   @Override
   public List<String> zrangeByScore(String key, double min, double max) {
      return this.executeCommand(this.commandObjects.zrangeByScore(key, min, max));
   }

   @Override
   public List<String> zrangeByScore(String key, String min, String max) {
      return this.executeCommand(this.commandObjects.zrangeByScore(key, min, max));
   }

   @Override
   public List<String> zrevrangeByScore(String key, double max, double min) {
      return this.executeCommand(this.commandObjects.zrevrangeByScore(key, max, min));
   }

   @Override
   public List<String> zrangeByScore(String key, double min, double max, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrangeByScore(key, min, max, offset, count));
   }

   @Override
   public List<String> zrevrangeByScore(String key, String max, String min) {
      return this.executeCommand(this.commandObjects.zrevrangeByScore(key, max, min));
   }

   @Override
   public List<String> zrangeByScore(String key, String min, String max, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrangeByScore(key, min, max, offset, count));
   }

   @Override
   public List<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrevrangeByScore(key, max, min, offset, count));
   }

   @Override
   public List<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
      return this.executeCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max));
   }

   @Override
   public List<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
      return this.executeCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min));
   }

   @Override
   public List<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max, offset, count));
   }

   @Override
   public List<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrevrangeByScore(key, max, min, offset, count));
   }

   @Override
   public List<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
      return this.executeCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max));
   }

   @Override
   public List<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
      return this.executeCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min));
   }

   @Override
   public List<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max, offset, count));
   }

   @Override
   public List<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min, offset, count));
   }

   @Override
   public List<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min, offset, count));
   }

   @Override
   public List<byte[]> zrange(byte[] key, long start, long stop) {
      return this.executeCommand(this.commandObjects.zrange(key, start, stop));
   }

   @Override
   public List<byte[]> zrevrange(byte[] key, long start, long stop) {
      return this.executeCommand(this.commandObjects.zrevrange(key, start, stop));
   }

   @Override
   public List<Tuple> zrangeWithScores(byte[] key, long start, long stop) {
      return this.executeCommand(this.commandObjects.zrangeWithScores(key, start, stop));
   }

   @Override
   public List<Tuple> zrevrangeWithScores(byte[] key, long start, long stop) {
      return this.executeCommand(this.commandObjects.zrevrangeWithScores(key, start, stop));
   }

   @Override
   public List<byte[]> zrange(byte[] key, ZRangeParams zRangeParams) {
      return this.executeCommand(this.commandObjects.zrange(key, zRangeParams));
   }

   @Override
   public List<Tuple> zrangeWithScores(byte[] key, ZRangeParams zRangeParams) {
      return this.executeCommand(this.commandObjects.zrangeWithScores(key, zRangeParams));
   }

   @Override
   public long zrangestore(byte[] dest, byte[] src, ZRangeParams zRangeParams) {
      return this.executeCommand(this.commandObjects.zrangestore(dest, src, zRangeParams));
   }

   @Override
   public List<byte[]> zrangeByScore(byte[] key, double min, double max) {
      return this.executeCommand(this.commandObjects.zrangeByScore(key, min, max));
   }

   @Override
   public List<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
      return this.executeCommand(this.commandObjects.zrangeByScore(key, min, max));
   }

   @Override
   public List<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
      return this.executeCommand(this.commandObjects.zrevrangeByScore(key, max, min));
   }

   @Override
   public List<byte[]> zrangeByScore(byte[] key, double min, double max, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrangeByScore(key, min, max, offset, count));
   }

   @Override
   public List<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
      return this.executeCommand(this.commandObjects.zrevrangeByScore(key, max, min));
   }

   @Override
   public List<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrangeByScore(key, min, max, offset, count));
   }

   @Override
   public List<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrevrangeByScore(key, max, min, offset, count));
   }

   @Override
   public List<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
      return this.executeCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max));
   }

   @Override
   public List<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min) {
      return this.executeCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min));
   }

   @Override
   public List<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max, offset, count));
   }

   @Override
   public List<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrevrangeByScore(key, max, min, offset, count));
   }

   @Override
   public List<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
      return this.executeCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max));
   }

   @Override
   public List<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min) {
      return this.executeCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min));
   }

   @Override
   public List<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max, offset, count));
   }

   @Override
   public List<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min, offset, count));
   }

   @Override
   public List<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min, offset, count));
   }

   @Override
   public long zremrangeByRank(String key, long start, long stop) {
      return this.executeCommand(this.commandObjects.zremrangeByRank(key, start, stop));
   }

   @Override
   public long zremrangeByScore(String key, double min, double max) {
      return this.executeCommand(this.commandObjects.zremrangeByScore(key, min, max));
   }

   @Override
   public long zremrangeByScore(String key, String min, String max) {
      return this.executeCommand(this.commandObjects.zremrangeByScore(key, min, max));
   }

   @Override
   public long zremrangeByRank(byte[] key, long start, long stop) {
      return this.executeCommand(this.commandObjects.zremrangeByRank(key, start, stop));
   }

   @Override
   public long zremrangeByScore(byte[] key, double min, double max) {
      return this.executeCommand(this.commandObjects.zremrangeByScore(key, min, max));
   }

   @Override
   public long zremrangeByScore(byte[] key, byte[] min, byte[] max) {
      return this.executeCommand(this.commandObjects.zremrangeByScore(key, min, max));
   }

   @Override
   public long zlexcount(String key, String min, String max) {
      return this.executeCommand(this.commandObjects.zlexcount(key, min, max));
   }

   @Override
   public List<String> zrangeByLex(String key, String min, String max) {
      return this.executeCommand(this.commandObjects.zrangeByLex(key, min, max));
   }

   @Override
   public List<String> zrangeByLex(String key, String min, String max, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrangeByLex(key, min, max, offset, count));
   }

   @Override
   public List<String> zrevrangeByLex(String key, String max, String min) {
      return this.executeCommand(this.commandObjects.zrevrangeByLex(key, max, min));
   }

   @Override
   public List<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrevrangeByLex(key, max, min, offset, count));
   }

   @Override
   public long zremrangeByLex(String key, String min, String max) {
      return this.executeCommand(this.commandObjects.zremrangeByLex(key, min, max));
   }

   @Override
   public long zlexcount(byte[] key, byte[] min, byte[] max) {
      return this.executeCommand(this.commandObjects.zlexcount(key, min, max));
   }

   @Override
   public List<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max) {
      return this.executeCommand(this.commandObjects.zrangeByLex(key, min, max));
   }

   @Override
   public List<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrangeByLex(key, min, max, offset, count));
   }

   @Override
   public List<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min) {
      return this.executeCommand(this.commandObjects.zrevrangeByLex(key, max, min));
   }

   @Override
   public List<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min, int offset, int count) {
      return this.executeCommand(this.commandObjects.zrevrangeByLex(key, max, min, offset, count));
   }

   @Override
   public long zremrangeByLex(byte[] key, byte[] min, byte[] max) {
      return this.executeCommand(this.commandObjects.zremrangeByLex(key, min, max));
   }

   @Override
   public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
      return this.executeCommand(this.commandObjects.zscan(key, cursor, params));
   }

   @Override
   public ScanResult<Tuple> zscan(byte[] key, byte[] cursor, ScanParams params) {
      return this.executeCommand(this.commandObjects.zscan(key, cursor, params));
   }

   @Override
   public KeyValue<String, Tuple> bzpopmax(double timeout, String... keys) {
      return this.executeCommand(this.commandObjects.bzpopmax(timeout, keys));
   }

   @Override
   public KeyValue<String, Tuple> bzpopmin(double timeout, String... keys) {
      return this.executeCommand(this.commandObjects.bzpopmin(timeout, keys));
   }

   @Override
   public KeyValue<byte[], Tuple> bzpopmax(double timeout, byte[]... keys) {
      return this.executeCommand(this.commandObjects.bzpopmax(timeout, keys));
   }

   @Override
   public KeyValue<byte[], Tuple> bzpopmin(double timeout, byte[]... keys) {
      return this.executeCommand(this.commandObjects.bzpopmin(timeout, keys));
   }

   @Override
   public List<String> zdiff(String... keys) {
      return this.executeCommand(this.commandObjects.zdiff(keys));
   }

   @Override
   public List<Tuple> zdiffWithScores(String... keys) {
      return this.executeCommand(this.commandObjects.zdiffWithScores(keys));
   }

   @Deprecated
   @Override
   public long zdiffStore(String dstkey, String... keys) {
      return this.executeCommand(this.commandObjects.zdiffStore(dstkey, keys));
   }

   @Override
   public long zdiffstore(String dstkey, String... keys) {
      return this.executeCommand(this.commandObjects.zdiffstore(dstkey, keys));
   }

   @Override
   public List<byte[]> zdiff(byte[]... keys) {
      return this.executeCommand(this.commandObjects.zdiff(keys));
   }

   @Override
   public List<Tuple> zdiffWithScores(byte[]... keys) {
      return this.executeCommand(this.commandObjects.zdiffWithScores(keys));
   }

   @Deprecated
   @Override
   public long zdiffStore(byte[] dstkey, byte[]... keys) {
      return this.executeCommand(this.commandObjects.zdiffStore(dstkey, keys));
   }

   @Override
   public long zdiffstore(byte[] dstkey, byte[]... keys) {
      return this.executeCommand(this.commandObjects.zdiffstore(dstkey, keys));
   }

   @Override
   public long zinterstore(String dstkey, String... sets) {
      return this.executeCommand(this.commandObjects.zinterstore(dstkey, sets));
   }

   @Override
   public long zinterstore(String dstkey, ZParams params, String... sets) {
      return this.executeCommand(this.commandObjects.zinterstore(dstkey, params, sets));
   }

   @Override
   public List<String> zinter(ZParams params, String... keys) {
      return this.executeCommand(this.commandObjects.zinter(params, keys));
   }

   @Override
   public List<Tuple> zinterWithScores(ZParams params, String... keys) {
      return this.executeCommand(this.commandObjects.zinterWithScores(params, keys));
   }

   @Override
   public long zinterstore(byte[] dstkey, byte[]... sets) {
      return this.executeCommand(this.commandObjects.zinterstore(dstkey, sets));
   }

   @Override
   public long zinterstore(byte[] dstkey, ZParams params, byte[]... sets) {
      return this.executeCommand(this.commandObjects.zinterstore(dstkey, params, sets));
   }

   @Override
   public long zintercard(byte[]... keys) {
      return this.executeCommand(this.commandObjects.zintercard(keys));
   }

   @Override
   public long zintercard(long limit, byte[]... keys) {
      return this.executeCommand(this.commandObjects.zintercard(limit, keys));
   }

   @Override
   public long zintercard(String... keys) {
      return this.executeCommand(this.commandObjects.zintercard(keys));
   }

   @Override
   public long zintercard(long limit, String... keys) {
      return this.executeCommand(this.commandObjects.zintercard(limit, keys));
   }

   @Override
   public List<byte[]> zinter(ZParams params, byte[]... keys) {
      return this.executeCommand(this.commandObjects.zinter(params, keys));
   }

   @Override
   public List<Tuple> zinterWithScores(ZParams params, byte[]... keys) {
      return this.executeCommand(this.commandObjects.zinterWithScores(params, keys));
   }

   @Override
   public List<String> zunion(ZParams params, String... keys) {
      return this.executeCommand(this.commandObjects.zunion(params, keys));
   }

   @Override
   public List<Tuple> zunionWithScores(ZParams params, String... keys) {
      return this.executeCommand(this.commandObjects.zunionWithScores(params, keys));
   }

   @Override
   public long zunionstore(String dstkey, String... sets) {
      return this.executeCommand(this.commandObjects.zunionstore(dstkey, sets));
   }

   @Override
   public long zunionstore(String dstkey, ZParams params, String... sets) {
      return this.executeCommand(this.commandObjects.zunionstore(dstkey, params, sets));
   }

   @Override
   public List<byte[]> zunion(ZParams params, byte[]... keys) {
      return this.executeCommand(this.commandObjects.zunion(params, keys));
   }

   @Override
   public List<Tuple> zunionWithScores(ZParams params, byte[]... keys) {
      return this.executeCommand(this.commandObjects.zunionWithScores(params, keys));
   }

   @Override
   public long zunionstore(byte[] dstkey, byte[]... sets) {
      return this.executeCommand(this.commandObjects.zunionstore(dstkey, sets));
   }

   @Override
   public long zunionstore(byte[] dstkey, ZParams params, byte[]... sets) {
      return this.executeCommand(this.commandObjects.zunionstore(dstkey, params, sets));
   }

   @Override
   public KeyValue<String, List<Tuple>> zmpop(SortedSetOption option, String... keys) {
      return this.executeCommand(this.commandObjects.zmpop(option, keys));
   }

   @Override
   public KeyValue<String, List<Tuple>> zmpop(SortedSetOption option, int count, String... keys) {
      return this.executeCommand(this.commandObjects.zmpop(option, count, keys));
   }

   @Override
   public KeyValue<String, List<Tuple>> bzmpop(double timeout, SortedSetOption option, String... keys) {
      return this.executeCommand(this.commandObjects.bzmpop(timeout, option, keys));
   }

   @Override
   public KeyValue<String, List<Tuple>> bzmpop(double timeout, SortedSetOption option, int count, String... keys) {
      return this.executeCommand(this.commandObjects.bzmpop(timeout, option, count, keys));
   }

   @Override
   public KeyValue<byte[], List<Tuple>> zmpop(SortedSetOption option, byte[]... keys) {
      return this.executeCommand(this.commandObjects.zmpop(option, keys));
   }

   @Override
   public KeyValue<byte[], List<Tuple>> zmpop(SortedSetOption option, int count, byte[]... keys) {
      return this.executeCommand(this.commandObjects.zmpop(option, count, keys));
   }

   @Override
   public KeyValue<byte[], List<Tuple>> bzmpop(double timeout, SortedSetOption option, byte[]... keys) {
      return this.executeCommand(this.commandObjects.bzmpop(timeout, option, keys));
   }

   @Override
   public KeyValue<byte[], List<Tuple>> bzmpop(double timeout, SortedSetOption option, int count, byte[]... keys) {
      return this.executeCommand(this.commandObjects.bzmpop(timeout, option, count, keys));
   }

   @Override
   public long geoadd(String key, double longitude, double latitude, String member) {
      return this.executeCommand(this.commandObjects.geoadd(key, longitude, latitude, member));
   }

   @Override
   public long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
      return this.executeCommand(this.commandObjects.geoadd(key, memberCoordinateMap));
   }

   @Override
   public long geoadd(String key, GeoAddParams params, Map<String, GeoCoordinate> memberCoordinateMap) {
      return this.executeCommand(this.commandObjects.geoadd(key, params, memberCoordinateMap));
   }

   @Override
   public Double geodist(String key, String member1, String member2) {
      return this.executeCommand(this.commandObjects.geodist(key, member1, member2));
   }

   @Override
   public Double geodist(String key, String member1, String member2, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.geodist(key, member1, member2, unit));
   }

   @Override
   public List<String> geohash(String key, String... members) {
      return this.executeCommand(this.commandObjects.geohash(key, members));
   }

   @Override
   public List<GeoCoordinate> geopos(String key, String... members) {
      return this.executeCommand(this.commandObjects.geopos(key, members));
   }

   @Override
   public long geoadd(byte[] key, double longitude, double latitude, byte[] member) {
      return this.executeCommand(this.commandObjects.geoadd(key, longitude, latitude, member));
   }

   @Override
   public long geoadd(byte[] key, Map<byte[], GeoCoordinate> memberCoordinateMap) {
      return this.executeCommand(this.commandObjects.geoadd(key, memberCoordinateMap));
   }

   @Override
   public long geoadd(byte[] key, GeoAddParams params, Map<byte[], GeoCoordinate> memberCoordinateMap) {
      return this.executeCommand(this.commandObjects.geoadd(key, params, memberCoordinateMap));
   }

   @Override
   public Double geodist(byte[] key, byte[] member1, byte[] member2) {
      return this.executeCommand(this.commandObjects.geodist(key, member1, member2));
   }

   @Override
   public Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.geodist(key, member1, member2, unit));
   }

   @Override
   public List<byte[]> geohash(byte[] key, byte[]... members) {
      return this.executeCommand(this.commandObjects.geohash(key, members));
   }

   @Override
   public List<GeoCoordinate> geopos(byte[] key, byte[]... members) {
      return this.executeCommand(this.commandObjects.geopos(key, members));
   }

   @Override
   public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.georadius(key, longitude, latitude, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.georadiusReadonly(key, longitude, latitude, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
      return this.executeCommand(this.commandObjects.georadius(key, longitude, latitude, radius, unit, param));
   }

   @Override
   public List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
      return this.executeCommand(this.commandObjects.georadiusReadonly(key, longitude, latitude, radius, unit, param));
   }

   @Override
   public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.georadiusByMember(key, member, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.georadiusByMemberReadonly(key, member, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
      return this.executeCommand(this.commandObjects.georadiusByMember(key, member, radius, unit, param));
   }

   @Override
   public List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
      return this.executeCommand(this.commandObjects.georadiusByMemberReadonly(key, member, radius, unit, param));
   }

   @Override
   public long georadiusStore(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
      return this.executeCommand(this.commandObjects.georadiusStore(key, longitude, latitude, radius, unit, param, storeParam));
   }

   @Override
   public long georadiusByMemberStore(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
      return this.executeCommand(this.commandObjects.georadiusByMemberStore(key, member, radius, unit, param, storeParam));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(String key, String member, double radius, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.geosearch(key, member, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(String key, GeoCoordinate coord, double radius, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.geosearch(key, coord, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(String key, String member, double width, double height, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.geosearch(key, member, width, height, unit));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(String key, GeoCoordinate coord, double width, double height, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.geosearch(key, coord, width, height, unit));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(String key, GeoSearchParam params) {
      return this.executeCommand(this.commandObjects.geosearch(key, params));
   }

   @Override
   public long geosearchStore(String dest, String src, String member, double radius, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.geosearchStore(dest, src, member, radius, unit));
   }

   @Override
   public long geosearchStore(String dest, String src, GeoCoordinate coord, double radius, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.geosearchStore(dest, src, coord, radius, unit));
   }

   @Override
   public long geosearchStore(String dest, String src, String member, double width, double height, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.geosearchStore(dest, src, member, width, height, unit));
   }

   @Override
   public long geosearchStore(String dest, String src, GeoCoordinate coord, double width, double height, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.geosearchStore(dest, src, coord, width, height, unit));
   }

   @Override
   public long geosearchStore(String dest, String src, GeoSearchParam params) {
      return this.executeCommand(this.commandObjects.geosearchStore(dest, src, params));
   }

   @Override
   public long geosearchStoreStoreDist(String dest, String src, GeoSearchParam params) {
      return this.executeCommand(this.commandObjects.geosearchStoreStoreDist(dest, src, params));
   }

   @Override
   public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.georadius(key, longitude, latitude, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.georadiusReadonly(key, longitude, latitude, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
      return this.executeCommand(this.commandObjects.georadius(key, longitude, latitude, radius, unit, param));
   }

   @Override
   public List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
      return this.executeCommand(this.commandObjects.georadiusReadonly(key, longitude, latitude, radius, unit, param));
   }

   @Override
   public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.georadiusByMember(key, member, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.georadiusByMemberReadonly(key, member, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param) {
      return this.executeCommand(this.commandObjects.georadiusByMember(key, member, radius, unit, param));
   }

   @Override
   public List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param) {
      return this.executeCommand(this.commandObjects.georadiusByMemberReadonly(key, member, radius, unit, param));
   }

   @Override
   public long georadiusStore(byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
      return this.executeCommand(this.commandObjects.georadiusStore(key, longitude, latitude, radius, unit, param, storeParam));
   }

   @Override
   public long georadiusByMemberStore(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
      return this.executeCommand(this.commandObjects.georadiusByMemberStore(key, member, radius, unit, param, storeParam));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(byte[] key, byte[] member, double radius, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.geosearch(key, member, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(byte[] key, GeoCoordinate coord, double radius, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.geosearch(key, coord, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(byte[] key, byte[] member, double width, double height, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.geosearch(key, member, width, height, unit));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(byte[] key, GeoCoordinate coord, double width, double height, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.geosearch(key, coord, width, height, unit));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(byte[] key, GeoSearchParam params) {
      return this.executeCommand(this.commandObjects.geosearch(key, params));
   }

   @Override
   public long geosearchStore(byte[] dest, byte[] src, byte[] member, double radius, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.geosearchStore(dest, src, member, radius, unit));
   }

   @Override
   public long geosearchStore(byte[] dest, byte[] src, GeoCoordinate coord, double radius, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.geosearchStore(dest, src, coord, radius, unit));
   }

   @Override
   public long geosearchStore(byte[] dest, byte[] src, byte[] member, double width, double height, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.geosearchStore(dest, src, member, width, height, unit));
   }

   @Override
   public long geosearchStore(byte[] dest, byte[] src, GeoCoordinate coord, double width, double height, GeoUnit unit) {
      return this.executeCommand(this.commandObjects.geosearchStore(dest, src, coord, width, height, unit));
   }

   @Override
   public long geosearchStore(byte[] dest, byte[] src, GeoSearchParam params) {
      return this.executeCommand(this.commandObjects.geosearchStore(dest, src, params));
   }

   @Override
   public long geosearchStoreStoreDist(byte[] dest, byte[] src, GeoSearchParam params) {
      return this.executeCommand(this.commandObjects.geosearchStoreStoreDist(dest, src, params));
   }

   @Override
   public long pfadd(String key, String... elements) {
      return this.executeCommand(this.commandObjects.pfadd(key, elements));
   }

   @Override
   public String pfmerge(String destkey, String... sourcekeys) {
      return this.executeCommand(this.commandObjects.pfmerge(destkey, sourcekeys));
   }

   @Override
   public long pfcount(String key) {
      return this.executeCommand(this.commandObjects.pfcount(key));
   }

   @Override
   public long pfcount(String... keys) {
      return this.executeCommand(this.commandObjects.pfcount(keys));
   }

   @Override
   public long pfadd(byte[] key, byte[]... elements) {
      return this.executeCommand(this.commandObjects.pfadd(key, elements));
   }

   @Override
   public String pfmerge(byte[] destkey, byte[]... sourcekeys) {
      return this.executeCommand(this.commandObjects.pfmerge(destkey, sourcekeys));
   }

   @Override
   public long pfcount(byte[] key) {
      return this.executeCommand(this.commandObjects.pfcount(key));
   }

   @Override
   public long pfcount(byte[]... keys) {
      return this.executeCommand(this.commandObjects.pfcount(keys));
   }

   @Override
   public StreamEntryID xadd(String key, StreamEntryID id, Map<String, String> hash) {
      return this.executeCommand(this.commandObjects.xadd(key, id, hash));
   }

   @Override
   public StreamEntryID xadd(String key, XAddParams params, Map<String, String> hash) {
      return this.executeCommand(this.commandObjects.xadd(key, params, hash));
   }

   @Override
   public long xlen(String key) {
      return this.executeCommand(this.commandObjects.xlen(key));
   }

   @Override
   public List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end) {
      return this.executeCommand(this.commandObjects.xrange(key, start, end));
   }

   @Override
   public List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end, int count) {
      return this.executeCommand(this.commandObjects.xrange(key, start, end, count));
   }

   @Override
   public List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start) {
      return this.executeCommand(this.commandObjects.xrevrange(key, end, start));
   }

   @Override
   public List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start, int count) {
      return this.executeCommand(this.commandObjects.xrevrange(key, end, start, count));
   }

   @Override
   public List<StreamEntry> xrange(String key, String start, String end) {
      return this.executeCommand(this.commandObjects.xrange(key, start, end));
   }

   @Override
   public List<StreamEntry> xrange(String key, String start, String end, int count) {
      return this.executeCommand(this.commandObjects.xrange(key, start, end, count));
   }

   @Override
   public List<StreamEntry> xrevrange(String key, String end, String start) {
      return this.executeCommand(this.commandObjects.xrevrange(key, end, start));
   }

   @Override
   public List<StreamEntry> xrevrange(String key, String end, String start, int count) {
      return this.executeCommand(this.commandObjects.xrevrange(key, end, start, count));
   }

   @Override
   public long xack(String key, String group, StreamEntryID... ids) {
      return this.executeCommand(this.commandObjects.xack(key, group, ids));
   }

   @Override
   public String xgroupCreate(String key, String groupName, StreamEntryID id, boolean makeStream) {
      return this.executeCommand(this.commandObjects.xgroupCreate(key, groupName, id, makeStream));
   }

   @Override
   public String xgroupSetID(String key, String groupName, StreamEntryID id) {
      return this.executeCommand(this.commandObjects.xgroupSetID(key, groupName, id));
   }

   @Override
   public long xgroupDestroy(String key, String groupName) {
      return this.executeCommand(this.commandObjects.xgroupDestroy(key, groupName));
   }

   @Override
   public boolean xgroupCreateConsumer(String key, String groupName, String consumerName) {
      return this.executeCommand(this.commandObjects.xgroupCreateConsumer(key, groupName, consumerName));
   }

   @Override
   public long xgroupDelConsumer(String key, String groupName, String consumerName) {
      return this.executeCommand(this.commandObjects.xgroupDelConsumer(key, groupName, consumerName));
   }

   @Override
   public StreamPendingSummary xpending(String key, String groupName) {
      return this.executeCommand(this.commandObjects.xpending(key, groupName));
   }

   @Override
   public List<StreamPendingEntry> xpending(String key, String groupName, XPendingParams params) {
      return this.executeCommand(this.commandObjects.xpending(key, groupName, params));
   }

   @Override
   public long xdel(String key, StreamEntryID... ids) {
      return this.executeCommand(this.commandObjects.xdel(key, ids));
   }

   @Override
   public long xtrim(String key, long maxLen, boolean approximate) {
      return this.executeCommand(this.commandObjects.xtrim(key, maxLen, approximate));
   }

   @Override
   public long xtrim(String key, XTrimParams params) {
      return this.executeCommand(this.commandObjects.xtrim(key, params));
   }

   @Override
   public List<StreamEntry> xclaim(String key, String group, String consumerName, long minIdleTime, XClaimParams params, StreamEntryID... ids) {
      return this.executeCommand(this.commandObjects.xclaim(key, group, consumerName, minIdleTime, params, ids));
   }

   @Override
   public List<StreamEntryID> xclaimJustId(String key, String group, String consumerName, long minIdleTime, XClaimParams params, StreamEntryID... ids) {
      return this.executeCommand(this.commandObjects.xclaimJustId(key, group, consumerName, minIdleTime, params, ids));
   }

   @Override
   public Entry<StreamEntryID, List<StreamEntry>> xautoclaim(
      String key, String group, String consumerName, long minIdleTime, StreamEntryID start, XAutoClaimParams params
   ) {
      return this.executeCommand(this.commandObjects.xautoclaim(key, group, consumerName, minIdleTime, start, params));
   }

   @Override
   public Entry<StreamEntryID, List<StreamEntryID>> xautoclaimJustId(
      String key, String group, String consumerName, long minIdleTime, StreamEntryID start, XAutoClaimParams params
   ) {
      return this.executeCommand(this.commandObjects.xautoclaimJustId(key, group, consumerName, minIdleTime, start, params));
   }

   @Override
   public StreamInfo xinfoStream(String key) {
      return this.executeCommand(this.commandObjects.xinfoStream(key));
   }

   @Override
   public StreamFullInfo xinfoStreamFull(String key) {
      return this.executeCommand(this.commandObjects.xinfoStreamFull(key));
   }

   @Override
   public StreamFullInfo xinfoStreamFull(String key, int count) {
      return this.executeCommand(this.commandObjects.xinfoStreamFull(key, count));
   }

   @Override
   public List<StreamGroupInfo> xinfoGroups(String key) {
      return this.executeCommand(this.commandObjects.xinfoGroups(key));
   }

   @Override
   public List<StreamConsumersInfo> xinfoConsumers(String key, String group) {
      return this.executeCommand(this.commandObjects.xinfoConsumers(key, group));
   }

   @Override
   public List<StreamConsumerInfo> xinfoConsumers2(String key, String group) {
      return this.executeCommand(this.commandObjects.xinfoConsumers2(key, group));
   }

   @Override
   public List<Entry<String, List<StreamEntry>>> xread(XReadParams xReadParams, Map<String, StreamEntryID> streams) {
      return this.executeCommand(this.commandObjects.xread(xReadParams, streams));
   }

   @Override
   public Map<String, List<StreamEntry>> xreadAsMap(XReadParams xReadParams, Map<String, StreamEntryID> streams) {
      return this.executeCommand(this.commandObjects.xreadAsMap(xReadParams, streams));
   }

   @Override
   public List<Entry<String, List<StreamEntry>>> xreadGroup(
      String groupName, String consumer, XReadGroupParams xReadGroupParams, Map<String, StreamEntryID> streams
   ) {
      return this.executeCommand(this.commandObjects.xreadGroup(groupName, consumer, xReadGroupParams, streams));
   }

   @Override
   public Map<String, List<StreamEntry>> xreadGroupAsMap(
      String groupName, String consumer, XReadGroupParams xReadGroupParams, Map<String, StreamEntryID> streams
   ) {
      return this.executeCommand(this.commandObjects.xreadGroupAsMap(groupName, consumer, xReadGroupParams, streams));
   }

   @Override
   public byte[] xadd(byte[] key, XAddParams params, Map<byte[], byte[]> hash) {
      return this.executeCommand(this.commandObjects.xadd(key, params, hash));
   }

   @Override
   public long xlen(byte[] key) {
      return this.executeCommand(this.commandObjects.xlen(key));
   }

   @Override
   public List<Object> xrange(byte[] key, byte[] start, byte[] end) {
      return this.executeCommand(this.commandObjects.xrange(key, start, end));
   }

   @Override
   public List<Object> xrange(byte[] key, byte[] start, byte[] end, int count) {
      return this.executeCommand(this.commandObjects.xrange(key, start, end, count));
   }

   @Override
   public List<Object> xrevrange(byte[] key, byte[] end, byte[] start) {
      return this.executeCommand(this.commandObjects.xrevrange(key, end, start));
   }

   @Override
   public List<Object> xrevrange(byte[] key, byte[] end, byte[] start, int count) {
      return this.executeCommand(this.commandObjects.xrevrange(key, end, start, count));
   }

   @Override
   public long xack(byte[] key, byte[] group, byte[]... ids) {
      return this.executeCommand(this.commandObjects.xack(key, group, ids));
   }

   @Override
   public String xgroupCreate(byte[] key, byte[] groupName, byte[] id, boolean makeStream) {
      return this.executeCommand(this.commandObjects.xgroupCreate(key, groupName, id, makeStream));
   }

   @Override
   public String xgroupSetID(byte[] key, byte[] groupName, byte[] id) {
      return this.executeCommand(this.commandObjects.xgroupSetID(key, groupName, id));
   }

   @Override
   public long xgroupDestroy(byte[] key, byte[] groupName) {
      return this.executeCommand(this.commandObjects.xgroupDestroy(key, groupName));
   }

   @Override
   public boolean xgroupCreateConsumer(byte[] key, byte[] groupName, byte[] consumerName) {
      return this.executeCommand(this.commandObjects.xgroupCreateConsumer(key, groupName, consumerName));
   }

   @Override
   public long xgroupDelConsumer(byte[] key, byte[] groupName, byte[] consumerName) {
      return this.executeCommand(this.commandObjects.xgroupDelConsumer(key, groupName, consumerName));
   }

   @Override
   public long xdel(byte[] key, byte[]... ids) {
      return this.executeCommand(this.commandObjects.xdel(key, ids));
   }

   @Override
   public long xtrim(byte[] key, long maxLen, boolean approximateLength) {
      return this.executeCommand(this.commandObjects.xtrim(key, maxLen, approximateLength));
   }

   @Override
   public long xtrim(byte[] key, XTrimParams params) {
      return this.executeCommand(this.commandObjects.xtrim(key, params));
   }

   @Override
   public Object xpending(byte[] key, byte[] groupName) {
      return this.executeCommand(this.commandObjects.xpending(key, groupName));
   }

   @Override
   public List<Object> xpending(byte[] key, byte[] groupName, XPendingParams params) {
      return this.executeCommand(this.commandObjects.xpending(key, groupName, params));
   }

   @Override
   public List<byte[]> xclaim(byte[] key, byte[] group, byte[] consumerName, long minIdleTime, XClaimParams params, byte[]... ids) {
      return this.executeCommand(this.commandObjects.xclaim(key, group, consumerName, minIdleTime, params, ids));
   }

   @Override
   public List<byte[]> xclaimJustId(byte[] key, byte[] group, byte[] consumerName, long minIdleTime, XClaimParams params, byte[]... ids) {
      return this.executeCommand(this.commandObjects.xclaimJustId(key, group, consumerName, minIdleTime, params, ids));
   }

   @Override
   public List<Object> xautoclaim(byte[] key, byte[] groupName, byte[] consumerName, long minIdleTime, byte[] start, XAutoClaimParams params) {
      return this.executeCommand(this.commandObjects.xautoclaim(key, groupName, consumerName, minIdleTime, start, params));
   }

   @Override
   public List<Object> xautoclaimJustId(byte[] key, byte[] groupName, byte[] consumerName, long minIdleTime, byte[] start, XAutoClaimParams params) {
      return this.executeCommand(this.commandObjects.xautoclaimJustId(key, groupName, consumerName, minIdleTime, start, params));
   }

   @Override
   public Object xinfoStream(byte[] key) {
      return this.executeCommand(this.commandObjects.xinfoStream(key));
   }

   @Override
   public Object xinfoStreamFull(byte[] key) {
      return this.executeCommand(this.commandObjects.xinfoStreamFull(key));
   }

   @Override
   public Object xinfoStreamFull(byte[] key, int count) {
      return this.executeCommand(this.commandObjects.xinfoStreamFull(key, count));
   }

   @Override
   public List<Object> xinfoGroups(byte[] key) {
      return this.executeCommand(this.commandObjects.xinfoGroups(key));
   }

   @Override
   public List<Object> xinfoConsumers(byte[] key, byte[] group) {
      return this.executeCommand(this.commandObjects.xinfoConsumers(key, group));
   }

   @Override
   public List<Object> xread(XReadParams xReadParams, Entry<byte[], byte[]>... streams) {
      return this.executeCommand(this.commandObjects.xread(xReadParams, streams));
   }

   @Override
   public List<Object> xreadGroup(byte[] groupName, byte[] consumer, XReadGroupParams xReadGroupParams, Entry<byte[], byte[]>... streams) {
      return this.executeCommand(this.commandObjects.xreadGroup(groupName, consumer, xReadGroupParams, streams));
   }

   @Override
   public Object eval(String script) {
      return this.executeCommand(this.commandObjects.eval(script));
   }

   @Override
   public Object eval(String script, int keyCount, String... params) {
      return this.executeCommand(this.commandObjects.eval(script, keyCount, params));
   }

   @Override
   public Object eval(String script, List<String> keys, List<String> args) {
      return this.executeCommand(this.commandObjects.eval(script, keys, args));
   }

   @Override
   public Object evalReadonly(String script, List<String> keys, List<String> args) {
      return this.executeCommand(this.commandObjects.evalReadonly(script, keys, args));
   }

   @Override
   public Object evalsha(String sha1) {
      return this.executeCommand(this.commandObjects.evalsha(sha1));
   }

   @Override
   public Object evalsha(String sha1, int keyCount, String... params) {
      return this.executeCommand(this.commandObjects.evalsha(sha1, keyCount, params));
   }

   @Override
   public Object evalsha(String sha1, List<String> keys, List<String> args) {
      return this.executeCommand(this.commandObjects.evalsha(sha1, keys, args));
   }

   @Override
   public Object evalshaReadonly(String sha1, List<String> keys, List<String> args) {
      return this.executeCommand(this.commandObjects.evalshaReadonly(sha1, keys, args));
   }

   @Override
   public Object eval(byte[] script) {
      return this.executeCommand(this.commandObjects.eval(script));
   }

   @Override
   public Object eval(byte[] script, int keyCount, byte[]... params) {
      return this.executeCommand(this.commandObjects.eval(script, keyCount, params));
   }

   @Override
   public Object eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
      return this.executeCommand(this.commandObjects.eval(script, keys, args));
   }

   @Override
   public Object evalReadonly(byte[] script, List<byte[]> keys, List<byte[]> args) {
      return this.executeCommand(this.commandObjects.evalReadonly(script, keys, args));
   }

   @Override
   public Object evalsha(byte[] sha1) {
      return this.executeCommand(this.commandObjects.evalsha(sha1));
   }

   @Override
   public Object evalsha(byte[] sha1, int keyCount, byte[]... params) {
      return this.executeCommand(this.commandObjects.evalsha(sha1, keyCount, params));
   }

   @Override
   public Object evalsha(byte[] sha1, List<byte[]> keys, List<byte[]> args) {
      return this.executeCommand(this.commandObjects.evalsha(sha1, keys, args));
   }

   @Override
   public Object evalshaReadonly(byte[] sha1, List<byte[]> keys, List<byte[]> args) {
      return this.executeCommand(this.commandObjects.evalshaReadonly(sha1, keys, args));
   }

   @Override
   public Object fcall(String name, List<String> keys, List<String> args) {
      return this.executeCommand(this.commandObjects.fcall(name, keys, args));
   }

   @Override
   public Object fcallReadonly(String name, List<String> keys, List<String> args) {
      return this.executeCommand(this.commandObjects.fcallReadonly(name, keys, args));
   }

   @Override
   public String functionDelete(String libraryName) {
      return this.checkAndBroadcastCommand(this.commandObjects.functionDelete(libraryName));
   }

   @Override
   public String functionFlush() {
      return this.checkAndBroadcastCommand(this.commandObjects.functionFlush());
   }

   @Override
   public String functionFlush(FlushMode mode) {
      return this.checkAndBroadcastCommand(this.commandObjects.functionFlush(mode));
   }

   @Override
   public String functionKill() {
      return this.checkAndBroadcastCommand(this.commandObjects.functionKill());
   }

   @Override
   public List<LibraryInfo> functionList() {
      return this.executeCommand(this.commandObjects.functionList());
   }

   @Override
   public List<LibraryInfo> functionList(String libraryNamePattern) {
      return this.executeCommand(this.commandObjects.functionList(libraryNamePattern));
   }

   @Override
   public List<LibraryInfo> functionListWithCode() {
      return this.executeCommand(this.commandObjects.functionListWithCode());
   }

   @Override
   public List<LibraryInfo> functionListWithCode(String libraryNamePattern) {
      return this.executeCommand(this.commandObjects.functionListWithCode(libraryNamePattern));
   }

   @Override
   public String functionLoad(String functionCode) {
      return this.checkAndBroadcastCommand(this.commandObjects.functionLoad(functionCode));
   }

   @Override
   public String functionLoadReplace(String functionCode) {
      return this.checkAndBroadcastCommand(this.commandObjects.functionLoadReplace(functionCode));
   }

   @Override
   public FunctionStats functionStats() {
      return this.executeCommand(this.commandObjects.functionStats());
   }

   @Override
   public Object fcall(byte[] name, List<byte[]> keys, List<byte[]> args) {
      return this.executeCommand(this.commandObjects.fcall(name, keys, args));
   }

   @Override
   public Object fcallReadonly(byte[] name, List<byte[]> keys, List<byte[]> args) {
      return this.executeCommand(this.commandObjects.fcallReadonly(name, keys, args));
   }

   @Override
   public String functionDelete(byte[] libraryName) {
      return this.checkAndBroadcastCommand(this.commandObjects.functionDelete(libraryName));
   }

   @Override
   public byte[] functionDump() {
      return this.executeCommand(this.commandObjects.functionDump());
   }

   @Override
   public List<Object> functionListBinary() {
      return this.executeCommand(this.commandObjects.functionListBinary());
   }

   @Override
   public List<Object> functionList(byte[] libraryNamePattern) {
      return this.executeCommand(this.commandObjects.functionList(libraryNamePattern));
   }

   @Override
   public List<Object> functionListWithCodeBinary() {
      return this.executeCommand(this.commandObjects.functionListWithCodeBinary());
   }

   @Override
   public List<Object> functionListWithCode(byte[] libraryNamePattern) {
      return this.executeCommand(this.commandObjects.functionListWithCode(libraryNamePattern));
   }

   @Override
   public String functionLoad(byte[] functionCode) {
      return this.checkAndBroadcastCommand(this.commandObjects.functionLoad(functionCode));
   }

   @Override
   public String functionLoadReplace(byte[] functionCode) {
      return this.checkAndBroadcastCommand(this.commandObjects.functionLoadReplace(functionCode));
   }

   @Override
   public String functionRestore(byte[] serializedValue) {
      return this.checkAndBroadcastCommand(this.commandObjects.functionRestore(serializedValue));
   }

   @Override
   public String functionRestore(byte[] serializedValue, FunctionRestorePolicy policy) {
      return this.checkAndBroadcastCommand(this.commandObjects.functionRestore(serializedValue, policy));
   }

   @Override
   public Object functionStatsBinary() {
      return this.executeCommand(this.commandObjects.functionStatsBinary());
   }

   @Override
   public Long objectRefcount(String key) {
      return this.executeCommand(this.commandObjects.objectRefcount(key));
   }

   @Override
   public String objectEncoding(String key) {
      return this.executeCommand(this.commandObjects.objectEncoding(key));
   }

   @Override
   public Long objectIdletime(String key) {
      return this.executeCommand(this.commandObjects.objectIdletime(key));
   }

   @Override
   public Long objectFreq(String key) {
      return this.executeCommand(this.commandObjects.objectFreq(key));
   }

   @Override
   public Long objectRefcount(byte[] key) {
      return this.executeCommand(this.commandObjects.objectRefcount(key));
   }

   @Override
   public byte[] objectEncoding(byte[] key) {
      return this.executeCommand(this.commandObjects.objectEncoding(key));
   }

   @Override
   public Long objectIdletime(byte[] key) {
      return this.executeCommand(this.commandObjects.objectIdletime(key));
   }

   @Override
   public Long objectFreq(byte[] key) {
      return this.executeCommand(this.commandObjects.objectFreq(key));
   }

   @Override
   public String migrate(String host, int port, String key, int timeout) {
      return this.executeCommand(this.commandObjects.migrate(host, port, key, timeout));
   }

   @Override
   public String migrate(String host, int port, int timeout, MigrateParams params, String... keys) {
      return this.executeCommand(this.commandObjects.migrate(host, port, timeout, params, keys));
   }

   @Override
   public String migrate(String host, int port, byte[] key, int timeout) {
      return this.executeCommand(this.commandObjects.migrate(host, port, key, timeout));
   }

   @Override
   public String migrate(String host, int port, int timeout, MigrateParams params, byte[]... keys) {
      return this.executeCommand(this.commandObjects.migrate(host, port, timeout, params, keys));
   }

   @Override
   public long waitReplicas(String sampleKey, int replicas, long timeout) {
      return this.executeCommand(this.commandObjects.waitReplicas(sampleKey, replicas, timeout));
   }

   @Override
   public long waitReplicas(byte[] sampleKey, int replicas, long timeout) {
      return this.executeCommand(this.commandObjects.waitReplicas(sampleKey, replicas, timeout));
   }

   @Override
   public KeyValue<Long, Long> waitAOF(String sampleKey, long numLocal, long numReplicas, long timeout) {
      return this.executeCommand(this.commandObjects.waitAOF(sampleKey, numLocal, numReplicas, timeout));
   }

   @Override
   public KeyValue<Long, Long> waitAOF(byte[] sampleKey, long numLocal, long numReplicas, long timeout) {
      return this.executeCommand(this.commandObjects.waitAOF(sampleKey, numLocal, numReplicas, timeout));
   }

   @Override
   public Object eval(String script, String sampleKey) {
      return this.executeCommand(this.commandObjects.eval(script, sampleKey));
   }

   @Override
   public Object evalsha(String sha1, String sampleKey) {
      return this.executeCommand(this.commandObjects.evalsha(sha1, sampleKey));
   }

   @Override
   public Object eval(byte[] script, byte[] sampleKey) {
      return this.executeCommand(this.commandObjects.eval(script, sampleKey));
   }

   @Override
   public Object evalsha(byte[] sha1, byte[] sampleKey) {
      return this.executeCommand(this.commandObjects.evalsha(sha1, sampleKey));
   }

   public List<Boolean> scriptExists(List<String> sha1s) {
      return this.checkAndBroadcastCommand(this.commandObjects.scriptExists(sha1s));
   }

   @Override
   public Boolean scriptExists(String sha1, String sampleKey) {
      return this.scriptExists(sampleKey, new String[]{sha1}).get(0);
   }

   @Override
   public List<Boolean> scriptExists(String sampleKey, String... sha1s) {
      return this.executeCommand(this.commandObjects.scriptExists(sampleKey, sha1s));
   }

   @Override
   public Boolean scriptExists(byte[] sha1, byte[] sampleKey) {
      return this.scriptExists(sampleKey, new byte[][]{sha1}).get(0);
   }

   @Override
   public List<Boolean> scriptExists(byte[] sampleKey, byte[]... sha1s) {
      return this.executeCommand(this.commandObjects.scriptExists(sampleKey, sha1s));
   }

   public String scriptLoad(String script) {
      return this.checkAndBroadcastCommand(this.commandObjects.scriptLoad(script));
   }

   @Override
   public String scriptLoad(String script, String sampleKey) {
      return this.executeCommand(this.commandObjects.scriptLoad(script, sampleKey));
   }

   public String scriptFlush() {
      return this.checkAndBroadcastCommand(this.commandObjects.scriptFlush());
   }

   @Override
   public String scriptFlush(String sampleKey) {
      return this.executeCommand(this.commandObjects.scriptFlush(sampleKey));
   }

   @Override
   public String scriptFlush(String sampleKey, FlushMode flushMode) {
      return this.executeCommand(this.commandObjects.scriptFlush(sampleKey, flushMode));
   }

   public String scriptKill() {
      return this.checkAndBroadcastCommand(this.commandObjects.scriptKill());
   }

   @Override
   public String scriptKill(String sampleKey) {
      return this.executeCommand(this.commandObjects.scriptKill(sampleKey));
   }

   @Override
   public byte[] scriptLoad(byte[] script, byte[] sampleKey) {
      return this.executeCommand(this.commandObjects.scriptLoad(script, sampleKey));
   }

   @Override
   public String scriptFlush(byte[] sampleKey) {
      return this.executeCommand(this.commandObjects.scriptFlush(sampleKey));
   }

   @Override
   public String scriptFlush(byte[] sampleKey, FlushMode flushMode) {
      return this.executeCommand(this.commandObjects.scriptFlush(sampleKey, flushMode));
   }

   @Override
   public String scriptKill(byte[] sampleKey) {
      return this.executeCommand(this.commandObjects.scriptKill(sampleKey));
   }

   public String slowlogReset() {
      return this.checkAndBroadcastCommand(this.commandObjects.slowlogReset());
   }

   public long publish(String channel, String message) {
      return this.executeCommand(this.commandObjects.publish(channel, message));
   }

   public long publish(byte[] channel, byte[] message) {
      return this.executeCommand(this.commandObjects.publish(channel, message));
   }

   public void subscribe(JedisPubSub jedisPubSub, String... channels) {
      try (Connection connection = this.provider.getConnection()) {
         jedisPubSub.proceed(connection, channels);
      }
   }

   public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
      try (Connection connection = this.provider.getConnection()) {
         jedisPubSub.proceedWithPatterns(connection, patterns);
      }
   }

   public void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels) {
      try (Connection connection = this.provider.getConnection()) {
         jedisPubSub.proceed(connection, channels);
      }
   }

   public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
      try (Connection connection = this.provider.getConnection()) {
         jedisPubSub.proceedWithPatterns(connection, patterns);
      }
   }

   public long hsetObject(String key, String field, Object value) {
      return this.executeCommand(this.commandObjects.hsetObject(key, field, value));
   }

   public long hsetObject(String key, Map<String, Object> hash) {
      return this.executeCommand(this.commandObjects.hsetObject(key, hash));
   }

   @Override
   public String ftCreate(String indexName, IndexOptions indexOptions, Schema schema) {
      return this.checkAndBroadcastCommand(this.commandObjects.ftCreate(indexName, indexOptions, schema));
   }

   @Override
   public String ftCreate(String indexName, FTCreateParams createParams, Iterable<SchemaField> schemaFields) {
      return this.checkAndBroadcastCommand(this.commandObjects.ftCreate(indexName, createParams, schemaFields));
   }

   @Override
   public String ftAlter(String indexName, Schema schema) {
      return this.checkAndBroadcastCommand(this.commandObjects.ftAlter(indexName, schema));
   }

   @Override
   public String ftAlter(String indexName, Iterable<SchemaField> schemaFields) {
      return this.checkAndBroadcastCommand(this.commandObjects.ftAlter(indexName, schemaFields));
   }

   @Override
   public String ftAliasAdd(String aliasName, String indexName) {
      return this.checkAndBroadcastCommand(this.commandObjects.ftAliasAdd(aliasName, indexName));
   }

   @Override
   public String ftAliasUpdate(String aliasName, String indexName) {
      return this.checkAndBroadcastCommand(this.commandObjects.ftAliasUpdate(aliasName, indexName));
   }

   @Override
   public String ftAliasDel(String aliasName) {
      return this.checkAndBroadcastCommand(this.commandObjects.ftAliasDel(aliasName));
   }

   @Override
   public String ftDropIndex(String indexName) {
      return this.checkAndBroadcastCommand(this.commandObjects.ftDropIndex(indexName));
   }

   @Override
   public String ftDropIndexDD(String indexName) {
      return this.checkAndBroadcastCommand(this.commandObjects.ftDropIndexDD(indexName));
   }

   @Override
   public SearchResult ftSearch(String indexName, String query) {
      return this.executeCommand(this.commandObjects.ftSearch(indexName, query));
   }

   @Override
   public SearchResult ftSearch(String indexName, String query, FTSearchParams params) {
      return this.executeCommand(this.commandObjects.ftSearch(indexName, query, params));
   }

   public FtSearchIteration ftSearchIteration(int batchSize, String indexName, String query, FTSearchParams params) {
      return new FtSearchIteration(this.provider, this.commandObjects.getProtocol(), batchSize, indexName, query, params);
   }

   @Override
   public SearchResult ftSearch(String indexName, Query query) {
      return this.executeCommand(this.commandObjects.ftSearch(indexName, query));
   }

   public FtSearchIteration ftSearchIteration(int batchSize, String indexName, Query query) {
      return new FtSearchIteration(this.provider, this.commandObjects.getProtocol(), batchSize, indexName, query);
   }

   @Deprecated
   @Override
   public SearchResult ftSearch(byte[] indexName, Query query) {
      return this.executeCommand(this.commandObjects.ftSearch(indexName, query));
   }

   @Override
   public String ftExplain(String indexName, Query query) {
      return this.executeCommand(this.commandObjects.ftExplain(indexName, query));
   }

   @Override
   public List<String> ftExplainCLI(String indexName, Query query) {
      return this.executeCommand(this.commandObjects.ftExplainCLI(indexName, query));
   }

   @Override
   public AggregationResult ftAggregate(String indexName, AggregationBuilder aggr) {
      return this.executeCommand(this.commandObjects.ftAggregate(indexName, aggr));
   }

   @Override
   public AggregationResult ftCursorRead(String indexName, long cursorId, int count) {
      return this.executeCommand(this.commandObjects.ftCursorRead(indexName, cursorId, count));
   }

   @Override
   public String ftCursorDel(String indexName, long cursorId) {
      return this.executeCommand(this.commandObjects.ftCursorDel(indexName, cursorId));
   }

   public FtAggregateIteration ftAggregateIteration(String indexName, AggregationBuilder aggr) {
      return new FtAggregateIteration(this.provider, indexName, aggr);
   }

   @Override
   public Entry<AggregationResult, Map<String, Object>> ftProfileAggregate(String indexName, FTProfileParams profileParams, AggregationBuilder aggr) {
      return this.executeCommand(this.commandObjects.ftProfileAggregate(indexName, profileParams, aggr));
   }

   @Override
   public Entry<SearchResult, Map<String, Object>> ftProfileSearch(String indexName, FTProfileParams profileParams, Query query) {
      return this.executeCommand(this.commandObjects.ftProfileSearch(indexName, profileParams, query));
   }

   @Override
   public Entry<SearchResult, Map<String, Object>> ftProfileSearch(String indexName, FTProfileParams profileParams, String query, FTSearchParams searchParams) {
      return this.executeCommand(this.commandObjects.ftProfileSearch(indexName, profileParams, query, searchParams));
   }

   @Override
   public String ftSynUpdate(String indexName, String synonymGroupId, String... terms) {
      return this.executeCommand(this.commandObjects.ftSynUpdate(indexName, synonymGroupId, terms));
   }

   @Override
   public Map<String, List<String>> ftSynDump(String indexName) {
      return this.executeCommand(this.commandObjects.ftSynDump(indexName));
   }

   @Override
   public long ftDictAdd(String dictionary, String... terms) {
      return this.executeCommand(this.commandObjects.ftDictAdd(dictionary, terms));
   }

   @Override
   public long ftDictDel(String dictionary, String... terms) {
      return this.executeCommand(this.commandObjects.ftDictDel(dictionary, terms));
   }

   @Override
   public Set<String> ftDictDump(String dictionary) {
      return this.executeCommand(this.commandObjects.ftDictDump(dictionary));
   }

   @Override
   public long ftDictAddBySampleKey(String indexName, String dictionary, String... terms) {
      return this.executeCommand(this.commandObjects.ftDictAddBySampleKey(indexName, dictionary, terms));
   }

   @Override
   public long ftDictDelBySampleKey(String indexName, String dictionary, String... terms) {
      return this.executeCommand(this.commandObjects.ftDictDelBySampleKey(indexName, dictionary, terms));
   }

   @Override
   public Set<String> ftDictDumpBySampleKey(String indexName, String dictionary) {
      return this.executeCommand(this.commandObjects.ftDictDumpBySampleKey(indexName, dictionary));
   }

   @Override
   public Map<String, Map<String, Double>> ftSpellCheck(String index, String query) {
      return this.executeCommand(this.commandObjects.ftSpellCheck(index, query));
   }

   @Override
   public Map<String, Map<String, Double>> ftSpellCheck(String index, String query, FTSpellCheckParams spellCheckParams) {
      return this.executeCommand(this.commandObjects.ftSpellCheck(index, query, spellCheckParams));
   }

   @Override
   public Map<String, Object> ftInfo(String indexName) {
      return this.executeCommand(this.commandObjects.ftInfo(indexName));
   }

   @Override
   public Set<String> ftTagVals(String indexName, String fieldName) {
      return this.executeCommand(this.commandObjects.ftTagVals(indexName, fieldName));
   }

   @Override
   public Map<String, Object> ftConfigGet(String option) {
      return this.executeCommand(this.commandObjects.ftConfigGet(option));
   }

   @Override
   public Map<String, Object> ftConfigGet(String indexName, String option) {
      return this.executeCommand(this.commandObjects.ftConfigGet(indexName, option));
   }

   @Override
   public String ftConfigSet(String option, String value) {
      return this.executeCommand(this.commandObjects.ftConfigSet(option, value));
   }

   @Override
   public String ftConfigSet(String indexName, String option, String value) {
      return this.executeCommand(this.commandObjects.ftConfigSet(indexName, option, value));
   }

   @Override
   public long ftSugAdd(String key, String string, double score) {
      return this.executeCommand(this.commandObjects.ftSugAdd(key, string, score));
   }

   @Override
   public long ftSugAddIncr(String key, String string, double score) {
      return this.executeCommand(this.commandObjects.ftSugAddIncr(key, string, score));
   }

   @Override
   public List<String> ftSugGet(String key, String prefix) {
      return this.executeCommand(this.commandObjects.ftSugGet(key, prefix));
   }

   @Override
   public List<String> ftSugGet(String key, String prefix, boolean fuzzy, int max) {
      return this.executeCommand(this.commandObjects.ftSugGet(key, prefix, fuzzy, max));
   }

   @Override
   public List<Tuple> ftSugGetWithScores(String key, String prefix) {
      return this.executeCommand(this.commandObjects.ftSugGetWithScores(key, prefix));
   }

   @Override
   public List<Tuple> ftSugGetWithScores(String key, String prefix, boolean fuzzy, int max) {
      return this.executeCommand(this.commandObjects.ftSugGetWithScores(key, prefix, fuzzy, max));
   }

   @Override
   public boolean ftSugDel(String key, String string) {
      return this.executeCommand(this.commandObjects.ftSugDel(key, string));
   }

   @Override
   public long ftSugLen(String key) {
      return this.executeCommand(this.commandObjects.ftSugLen(key));
   }

   @Override
   public Set<String> ftList() {
      return this.executeCommand(this.commandObjects.ftList());
   }

   @Override
   public String jsonSet(String key, Path2 path, Object object) {
      return this.executeCommand(this.commandObjects.jsonSet(key, path, object));
   }

   @Override
   public String jsonSetWithEscape(String key, Path2 path, Object object) {
      return this.executeCommand(this.commandObjects.jsonSetWithEscape(key, path, object));
   }

   @Deprecated
   @Override
   public String jsonSet(String key, Path path, Object pojo) {
      return this.executeCommand(this.commandObjects.jsonSet(key, path, pojo));
   }

   @Deprecated
   @Override
   public String jsonSetWithPlainString(String key, Path path, String string) {
      return this.executeCommand(this.commandObjects.jsonSetWithPlainString(key, path, string));
   }

   @Override
   public String jsonSet(String key, Path2 path, Object pojo, JsonSetParams params) {
      return this.executeCommand(this.commandObjects.jsonSet(key, path, pojo, params));
   }

   @Override
   public String jsonSetWithEscape(String key, Path2 path, Object pojo, JsonSetParams params) {
      return this.executeCommand(this.commandObjects.jsonSetWithEscape(key, path, pojo, params));
   }

   @Deprecated
   @Override
   public String jsonSet(String key, Path path, Object pojo, JsonSetParams params) {
      return this.executeCommand(this.commandObjects.jsonSet(key, path, pojo, params));
   }

   @Override
   public String jsonMerge(String key, Path2 path, Object object) {
      return this.executeCommand(this.commandObjects.jsonMerge(key, path, object));
   }

   @Deprecated
   @Override
   public String jsonMerge(String key, Path path, Object pojo) {
      return this.executeCommand(this.commandObjects.jsonMerge(key, path, pojo));
   }

   @Override
   public Object jsonGet(String key) {
      return this.executeCommand(this.commandObjects.jsonGet(key));
   }

   @Deprecated
   @Override
   public <T> T jsonGet(String key, Class<T> clazz) {
      return this.executeCommand(this.commandObjects.jsonGet(key, clazz));
   }

   @Override
   public Object jsonGet(String key, Path2... paths) {
      return this.executeCommand(this.commandObjects.jsonGet(key, paths));
   }

   @Deprecated
   @Override
   public Object jsonGet(String key, Path... paths) {
      return this.executeCommand(this.commandObjects.jsonGet(key, paths));
   }

   @Deprecated
   @Override
   public String jsonGetAsPlainString(String key, Path path) {
      return this.executeCommand(this.commandObjects.jsonGetAsPlainString(key, path));
   }

   @Deprecated
   @Override
   public <T> T jsonGet(String key, Class<T> clazz, Path... paths) {
      return this.executeCommand(this.commandObjects.jsonGet(key, clazz, paths));
   }

   @Override
   public List<JSONArray> jsonMGet(Path2 path, String... keys) {
      return this.executeCommand(this.commandObjects.jsonMGet(path, keys));
   }

   @Deprecated
   @Override
   public <T> List<T> jsonMGet(Path path, Class<T> clazz, String... keys) {
      return this.executeCommand(this.commandObjects.jsonMGet(path, clazz, keys));
   }

   @Override
   public long jsonDel(String key) {
      return this.executeCommand(this.commandObjects.jsonDel(key));
   }

   @Override
   public long jsonDel(String key, Path2 path) {
      return this.executeCommand(this.commandObjects.jsonDel(key, path));
   }

   @Deprecated
   @Override
   public long jsonDel(String key, Path path) {
      return this.executeCommand(this.commandObjects.jsonDel(key, path));
   }

   @Override
   public long jsonClear(String key) {
      return this.executeCommand(this.commandObjects.jsonClear(key));
   }

   @Override
   public long jsonClear(String key, Path2 path) {
      return this.executeCommand(this.commandObjects.jsonClear(key, path));
   }

   @Deprecated
   @Override
   public long jsonClear(String key, Path path) {
      return this.executeCommand(this.commandObjects.jsonClear(key, path));
   }

   @Override
   public List<Boolean> jsonToggle(String key, Path2 path) {
      return this.executeCommand(this.commandObjects.jsonToggle(key, path));
   }

   @Deprecated
   @Override
   public String jsonToggle(String key, Path path) {
      return this.executeCommand(this.commandObjects.jsonToggle(key, path));
   }

   @Deprecated
   @Override
   public Class<?> jsonType(String key) {
      return this.executeCommand(this.commandObjects.jsonType(key));
   }

   @Override
   public List<Class<?>> jsonType(String key, Path2 path) {
      return this.executeCommand(this.commandObjects.jsonType(key, path));
   }

   @Deprecated
   @Override
   public Class<?> jsonType(String key, Path path) {
      return this.executeCommand(this.commandObjects.jsonType(key, path));
   }

   @Deprecated
   @Override
   public long jsonStrAppend(String key, Object string) {
      return this.executeCommand(this.commandObjects.jsonStrAppend(key, string));
   }

   @Override
   public List<Long> jsonStrAppend(String key, Path2 path, Object string) {
      return this.executeCommand(this.commandObjects.jsonStrAppend(key, path, string));
   }

   @Deprecated
   @Override
   public long jsonStrAppend(String key, Path path, Object string) {
      return this.executeCommand(this.commandObjects.jsonStrAppend(key, path, string));
   }

   @Deprecated
   @Override
   public Long jsonStrLen(String key) {
      return this.executeCommand(this.commandObjects.jsonStrLen(key));
   }

   @Override
   public List<Long> jsonStrLen(String key, Path2 path) {
      return this.executeCommand(this.commandObjects.jsonStrLen(key, path));
   }

   @Deprecated
   @Override
   public Long jsonStrLen(String key, Path path) {
      return this.executeCommand(this.commandObjects.jsonStrLen(key, path));
   }

   @Override
   public Object jsonNumIncrBy(String key, Path2 path, double value) {
      return this.executeCommand(this.commandObjects.jsonNumIncrBy(key, path, value));
   }

   @Deprecated
   @Override
   public double jsonNumIncrBy(String key, Path path, double value) {
      return this.executeCommand(this.commandObjects.jsonNumIncrBy(key, path, value));
   }

   @Override
   public List<Long> jsonArrAppend(String key, Path2 path, Object... objects) {
      return this.executeCommand(this.commandObjects.jsonArrAppend(key, path, objects));
   }

   @Override
   public List<Long> jsonArrAppendWithEscape(String key, Path2 path, Object... objects) {
      return this.executeCommand(this.commandObjects.jsonArrAppendWithEscape(key, path, objects));
   }

   @Deprecated
   @Override
   public Long jsonArrAppend(String key, Path path, Object... pojos) {
      return this.executeCommand(this.commandObjects.jsonArrAppend(key, path, pojos));
   }

   @Override
   public List<Long> jsonArrIndex(String key, Path2 path, Object scalar) {
      return this.executeCommand(this.commandObjects.jsonArrIndex(key, path, scalar));
   }

   @Override
   public List<Long> jsonArrIndexWithEscape(String key, Path2 path, Object scalar) {
      return this.executeCommand(this.commandObjects.jsonArrIndexWithEscape(key, path, scalar));
   }

   @Deprecated
   @Override
   public long jsonArrIndex(String key, Path path, Object scalar) {
      return this.executeCommand(this.commandObjects.jsonArrIndex(key, path, scalar));
   }

   @Override
   public List<Long> jsonArrInsert(String key, Path2 path, int index, Object... objects) {
      return this.executeCommand(this.commandObjects.jsonArrInsert(key, path, index, objects));
   }

   @Override
   public List<Long> jsonArrInsertWithEscape(String key, Path2 path, int index, Object... objects) {
      return this.executeCommand(this.commandObjects.jsonArrInsertWithEscape(key, path, index, objects));
   }

   @Deprecated
   @Override
   public long jsonArrInsert(String key, Path path, int index, Object... pojos) {
      return this.executeCommand(this.commandObjects.jsonArrInsert(key, path, index, pojos));
   }

   @Deprecated
   @Override
   public Object jsonArrPop(String key) {
      return this.executeCommand(this.commandObjects.jsonArrPop(key));
   }

   @Deprecated
   @Override
   public <T> T jsonArrPop(String key, Class<T> clazz) {
      return this.executeCommand(this.commandObjects.jsonArrPop(key, clazz));
   }

   @Override
   public List<Object> jsonArrPop(String key, Path2 path) {
      return this.executeCommand(this.commandObjects.jsonArrPop(key, path));
   }

   @Deprecated
   @Override
   public Object jsonArrPop(String key, Path path) {
      return this.executeCommand(this.commandObjects.jsonArrPop(key, path));
   }

   @Deprecated
   @Override
   public <T> T jsonArrPop(String key, Class<T> clazz, Path path) {
      return this.executeCommand(this.commandObjects.jsonArrPop(key, clazz, path));
   }

   @Override
   public List<Object> jsonArrPop(String key, Path2 path, int index) {
      return this.executeCommand(this.commandObjects.jsonArrPop(key, path, index));
   }

   @Deprecated
   @Override
   public Object jsonArrPop(String key, Path path, int index) {
      return this.executeCommand(this.commandObjects.jsonArrPop(key, path, index));
   }

   @Deprecated
   @Override
   public <T> T jsonArrPop(String key, Class<T> clazz, Path path, int index) {
      return this.executeCommand(this.commandObjects.jsonArrPop(key, clazz, path, index));
   }

   @Deprecated
   @Override
   public Long jsonArrLen(String key) {
      return this.executeCommand(this.commandObjects.jsonArrLen(key));
   }

   @Override
   public List<Long> jsonArrLen(String key, Path2 path) {
      return this.executeCommand(this.commandObjects.jsonArrLen(key, path));
   }

   @Deprecated
   @Override
   public Long jsonArrLen(String key, Path path) {
      return this.executeCommand(this.commandObjects.jsonArrLen(key, path));
   }

   @Override
   public List<Long> jsonArrTrim(String key, Path2 path, int start, int stop) {
      return this.executeCommand(this.commandObjects.jsonArrTrim(key, path, start, stop));
   }

   @Deprecated
   @Override
   public Long jsonArrTrim(String key, Path path, int start, int stop) {
      return this.executeCommand(this.commandObjects.jsonArrTrim(key, path, start, stop));
   }

   @Deprecated
   @Override
   public Long jsonObjLen(String key) {
      return this.executeCommand(this.commandObjects.jsonObjLen(key));
   }

   @Deprecated
   @Override
   public Long jsonObjLen(String key, Path path) {
      return this.executeCommand(this.commandObjects.jsonObjLen(key, path));
   }

   @Override
   public List<Long> jsonObjLen(String key, Path2 path) {
      return this.executeCommand(this.commandObjects.jsonObjLen(key, path));
   }

   @Deprecated
   @Override
   public List<String> jsonObjKeys(String key) {
      return this.executeCommand(this.commandObjects.jsonObjKeys(key));
   }

   @Deprecated
   @Override
   public List<String> jsonObjKeys(String key, Path path) {
      return this.executeCommand(this.commandObjects.jsonObjKeys(key, path));
   }

   @Override
   public List<List<String>> jsonObjKeys(String key, Path2 path) {
      return this.executeCommand(this.commandObjects.jsonObjKeys(key, path));
   }

   @Deprecated
   @Override
   public long jsonDebugMemory(String key) {
      return this.executeCommand(this.commandObjects.jsonDebugMemory(key));
   }

   @Deprecated
   @Override
   public long jsonDebugMemory(String key, Path path) {
      return this.executeCommand(this.commandObjects.jsonDebugMemory(key, path));
   }

   @Override
   public List<Long> jsonDebugMemory(String key, Path2 path) {
      return this.executeCommand(this.commandObjects.jsonDebugMemory(key, path));
   }

   @Override
   public String tsCreate(String key) {
      return this.executeCommand(this.commandObjects.tsCreate(key));
   }

   @Override
   public String tsCreate(String key, TSCreateParams createParams) {
      return this.executeCommand(this.commandObjects.tsCreate(key, createParams));
   }

   @Override
   public long tsDel(String key, long fromTimestamp, long toTimestamp) {
      return this.executeCommand(this.commandObjects.tsDel(key, fromTimestamp, toTimestamp));
   }

   @Override
   public String tsAlter(String key, TSAlterParams alterParams) {
      return this.executeCommand(this.commandObjects.tsAlter(key, alterParams));
   }

   @Override
   public long tsAdd(String key, double value) {
      return this.executeCommand(this.commandObjects.tsAdd(key, value));
   }

   @Override
   public long tsAdd(String key, long timestamp, double value) {
      return this.executeCommand(this.commandObjects.tsAdd(key, timestamp, value));
   }

   @Override
   public long tsAdd(String key, long timestamp, double value, TSCreateParams createParams) {
      return this.executeCommand(this.commandObjects.tsAdd(key, timestamp, value, createParams));
   }

   @Override
   public long tsAdd(String key, long timestamp, double value, TSAddParams addParams) {
      return this.executeCommand(this.commandObjects.tsAdd(key, timestamp, value, addParams));
   }

   @Override
   public List<Long> tsMAdd(Entry<String, TSElement>... entries) {
      return this.executeCommand(this.commandObjects.tsMAdd(entries));
   }

   @Override
   public long tsIncrBy(String key, double value) {
      return this.executeCommand(this.commandObjects.tsIncrBy(key, value));
   }

   @Override
   public long tsIncrBy(String key, double value, long timestamp) {
      return this.executeCommand(this.commandObjects.tsIncrBy(key, value, timestamp));
   }

   @Override
   public long tsIncrBy(String key, double addend, TSIncrByParams incrByParams) {
      return this.executeCommand(this.commandObjects.tsIncrBy(key, addend, incrByParams));
   }

   @Override
   public long tsDecrBy(String key, double value) {
      return this.executeCommand(this.commandObjects.tsDecrBy(key, value));
   }

   @Override
   public long tsDecrBy(String key, double value, long timestamp) {
      return this.executeCommand(this.commandObjects.tsDecrBy(key, value, timestamp));
   }

   @Override
   public long tsDecrBy(String key, double subtrahend, TSDecrByParams decrByParams) {
      return this.executeCommand(this.commandObjects.tsDecrBy(key, subtrahend, decrByParams));
   }

   @Override
   public List<TSElement> tsRange(String key, long fromTimestamp, long toTimestamp) {
      return this.executeCommand(this.commandObjects.tsRange(key, fromTimestamp, toTimestamp));
   }

   @Override
   public List<TSElement> tsRange(String key, TSRangeParams rangeParams) {
      return this.executeCommand(this.commandObjects.tsRange(key, rangeParams));
   }

   @Override
   public List<TSElement> tsRevRange(String key, long fromTimestamp, long toTimestamp) {
      return this.executeCommand(this.commandObjects.tsRevRange(key, fromTimestamp, toTimestamp));
   }

   @Override
   public List<TSElement> tsRevRange(String key, TSRangeParams rangeParams) {
      return this.executeCommand(this.commandObjects.tsRevRange(key, rangeParams));
   }

   @Override
   public Map<String, TSMRangeElements> tsMRange(long fromTimestamp, long toTimestamp, String... filters) {
      return this.executeCommand(this.commandObjects.tsMRange(fromTimestamp, toTimestamp, filters));
   }

   @Override
   public Map<String, TSMRangeElements> tsMRange(TSMRangeParams multiRangeParams) {
      return this.executeCommand(this.commandObjects.tsMRange(multiRangeParams));
   }

   @Override
   public Map<String, TSMRangeElements> tsMRevRange(long fromTimestamp, long toTimestamp, String... filters) {
      return this.executeCommand(this.commandObjects.tsMRevRange(fromTimestamp, toTimestamp, filters));
   }

   @Override
   public Map<String, TSMRangeElements> tsMRevRange(TSMRangeParams multiRangeParams) {
      return this.executeCommand(this.commandObjects.tsMRevRange(multiRangeParams));
   }

   @Override
   public TSElement tsGet(String key) {
      return this.executeCommand(this.commandObjects.tsGet(key));
   }

   @Override
   public TSElement tsGet(String key, TSGetParams getParams) {
      return this.executeCommand(this.commandObjects.tsGet(key, getParams));
   }

   @Override
   public Map<String, TSMGetElement> tsMGet(TSMGetParams multiGetParams, String... filters) {
      return this.executeCommand(this.commandObjects.tsMGet(multiGetParams, filters));
   }

   @Override
   public String tsCreateRule(String sourceKey, String destKey, AggregationType aggregationType, long timeBucket) {
      return this.executeCommand(this.commandObjects.tsCreateRule(sourceKey, destKey, aggregationType, timeBucket));
   }

   @Override
   public String tsCreateRule(String sourceKey, String destKey, AggregationType aggregationType, long bucketDuration, long alignTimestamp) {
      return this.executeCommand(this.commandObjects.tsCreateRule(sourceKey, destKey, aggregationType, bucketDuration, alignTimestamp));
   }

   @Override
   public String tsDeleteRule(String sourceKey, String destKey) {
      return this.executeCommand(this.commandObjects.tsDeleteRule(sourceKey, destKey));
   }

   @Override
   public List<String> tsQueryIndex(String... filters) {
      return this.executeCommand(this.commandObjects.tsQueryIndex(filters));
   }

   @Override
   public TSInfo tsInfo(String key) {
      return this.executeCommand(this.commandObjects.tsInfo(key));
   }

   @Override
   public TSInfo tsInfoDebug(String key) {
      return this.executeCommand(this.commandObjects.tsInfoDebug(key));
   }

   @Override
   public String bfReserve(String key, double errorRate, long capacity) {
      return this.executeCommand(this.commandObjects.bfReserve(key, errorRate, capacity));
   }

   @Override
   public String bfReserve(String key, double errorRate, long capacity, BFReserveParams reserveParams) {
      return this.executeCommand(this.commandObjects.bfReserve(key, errorRate, capacity, reserveParams));
   }

   @Override
   public boolean bfAdd(String key, String item) {
      return this.executeCommand(this.commandObjects.bfAdd(key, item));
   }

   @Override
   public List<Boolean> bfMAdd(String key, String... items) {
      return this.executeCommand(this.commandObjects.bfMAdd(key, items));
   }

   @Override
   public List<Boolean> bfInsert(String key, String... items) {
      return this.executeCommand(this.commandObjects.bfInsert(key, items));
   }

   @Override
   public List<Boolean> bfInsert(String key, BFInsertParams insertParams, String... items) {
      return this.executeCommand(this.commandObjects.bfInsert(key, insertParams, items));
   }

   @Override
   public boolean bfExists(String key, String item) {
      return this.executeCommand(this.commandObjects.bfExists(key, item));
   }

   @Override
   public List<Boolean> bfMExists(String key, String... items) {
      return this.executeCommand(this.commandObjects.bfMExists(key, items));
   }

   @Override
   public Entry<Long, byte[]> bfScanDump(String key, long iterator) {
      return this.executeCommand(this.commandObjects.bfScanDump(key, iterator));
   }

   @Override
   public String bfLoadChunk(String key, long iterator, byte[] data) {
      return this.executeCommand(this.commandObjects.bfLoadChunk(key, iterator, data));
   }

   @Override
   public long bfCard(String key) {
      return this.executeCommand(this.commandObjects.bfCard(key));
   }

   @Override
   public Map<String, Object> bfInfo(String key) {
      return this.executeCommand(this.commandObjects.bfInfo(key));
   }

   @Override
   public String cfReserve(String key, long capacity) {
      return this.executeCommand(this.commandObjects.cfReserve(key, capacity));
   }

   @Override
   public String cfReserve(String key, long capacity, CFReserveParams reserveParams) {
      return this.executeCommand(this.commandObjects.cfReserve(key, capacity, reserveParams));
   }

   @Override
   public boolean cfAdd(String key, String item) {
      return this.executeCommand(this.commandObjects.cfAdd(key, item));
   }

   @Override
   public boolean cfAddNx(String key, String item) {
      return this.executeCommand(this.commandObjects.cfAddNx(key, item));
   }

   @Override
   public List<Boolean> cfInsert(String key, String... items) {
      return this.executeCommand(this.commandObjects.cfInsert(key, items));
   }

   @Override
   public List<Boolean> cfInsert(String key, CFInsertParams insertParams, String... items) {
      return this.executeCommand(this.commandObjects.cfInsert(key, insertParams, items));
   }

   @Override
   public List<Boolean> cfInsertNx(String key, String... items) {
      return this.executeCommand(this.commandObjects.cfInsertNx(key, items));
   }

   @Override
   public List<Boolean> cfInsertNx(String key, CFInsertParams insertParams, String... items) {
      return this.executeCommand(this.commandObjects.cfInsertNx(key, insertParams, items));
   }

   @Override
   public boolean cfExists(String key, String item) {
      return this.executeCommand(this.commandObjects.cfExists(key, item));
   }

   @Override
   public List<Boolean> cfMExists(String key, String... items) {
      return this.executeCommand(this.commandObjects.cfMExists(key, items));
   }

   @Override
   public boolean cfDel(String key, String item) {
      return this.executeCommand(this.commandObjects.cfDel(key, item));
   }

   @Override
   public long cfCount(String key, String item) {
      return this.executeCommand(this.commandObjects.cfCount(key, item));
   }

   @Override
   public Entry<Long, byte[]> cfScanDump(String key, long iterator) {
      return this.executeCommand(this.commandObjects.cfScanDump(key, iterator));
   }

   @Override
   public String cfLoadChunk(String key, long iterator, byte[] data) {
      return this.executeCommand(this.commandObjects.cfLoadChunk(key, iterator, data));
   }

   @Override
   public Map<String, Object> cfInfo(String key) {
      return this.executeCommand(this.commandObjects.cfInfo(key));
   }

   @Override
   public String cmsInitByDim(String key, long width, long depth) {
      return this.executeCommand(this.commandObjects.cmsInitByDim(key, width, depth));
   }

   @Override
   public String cmsInitByProb(String key, double error, double probability) {
      return this.executeCommand(this.commandObjects.cmsInitByProb(key, error, probability));
   }

   @Override
   public List<Long> cmsIncrBy(String key, Map<String, Long> itemIncrements) {
      return this.executeCommand(this.commandObjects.cmsIncrBy(key, itemIncrements));
   }

   @Override
   public List<Long> cmsQuery(String key, String... items) {
      return this.executeCommand(this.commandObjects.cmsQuery(key, items));
   }

   @Override
   public String cmsMerge(String destKey, String... keys) {
      return this.executeCommand(this.commandObjects.cmsMerge(destKey, keys));
   }

   @Override
   public String cmsMerge(String destKey, Map<String, Long> keysAndWeights) {
      return this.executeCommand(this.commandObjects.cmsMerge(destKey, keysAndWeights));
   }

   @Override
   public Map<String, Object> cmsInfo(String key) {
      return this.executeCommand(this.commandObjects.cmsInfo(key));
   }

   @Override
   public String topkReserve(String key, long topk) {
      return this.executeCommand(this.commandObjects.topkReserve(key, topk));
   }

   @Override
   public String topkReserve(String key, long topk, long width, long depth, double decay) {
      return this.executeCommand(this.commandObjects.topkReserve(key, topk, width, depth, decay));
   }

   @Override
   public List<String> topkAdd(String key, String... items) {
      return this.executeCommand(this.commandObjects.topkAdd(key, items));
   }

   @Override
   public List<String> topkIncrBy(String key, Map<String, Long> itemIncrements) {
      return this.executeCommand(this.commandObjects.topkIncrBy(key, itemIncrements));
   }

   @Override
   public List<Boolean> topkQuery(String key, String... items) {
      return this.executeCommand(this.commandObjects.topkQuery(key, items));
   }

   @Override
   public List<String> topkList(String key) {
      return this.executeCommand(this.commandObjects.topkList(key));
   }

   @Override
   public Map<String, Long> topkListWithCount(String key) {
      return this.executeCommand(this.commandObjects.topkListWithCount(key));
   }

   @Override
   public Map<String, Object> topkInfo(String key) {
      return this.executeCommand(this.commandObjects.topkInfo(key));
   }

   @Override
   public String tdigestCreate(String key) {
      return this.executeCommand(this.commandObjects.tdigestCreate(key));
   }

   @Override
   public String tdigestCreate(String key, int compression) {
      return this.executeCommand(this.commandObjects.tdigestCreate(key, compression));
   }

   @Override
   public String tdigestReset(String key) {
      return this.executeCommand(this.commandObjects.tdigestReset(key));
   }

   @Override
   public String tdigestMerge(String destinationKey, String... sourceKeys) {
      return this.executeCommand(this.commandObjects.tdigestMerge(destinationKey, sourceKeys));
   }

   @Override
   public String tdigestMerge(TDigestMergeParams mergeParams, String destinationKey, String... sourceKeys) {
      return this.executeCommand(this.commandObjects.tdigestMerge(mergeParams, destinationKey, sourceKeys));
   }

   @Override
   public Map<String, Object> tdigestInfo(String key) {
      return this.executeCommand(this.commandObjects.tdigestInfo(key));
   }

   @Override
   public String tdigestAdd(String key, double... values) {
      return this.executeCommand(this.commandObjects.tdigestAdd(key, values));
   }

   @Override
   public List<Double> tdigestCDF(String key, double... values) {
      return this.executeCommand(this.commandObjects.tdigestCDF(key, values));
   }

   @Override
   public List<Double> tdigestQuantile(String key, double... quantiles) {
      return this.executeCommand(this.commandObjects.tdigestQuantile(key, quantiles));
   }

   @Override
   public double tdigestMin(String key) {
      return this.executeCommand(this.commandObjects.tdigestMin(key));
   }

   @Override
   public double tdigestMax(String key) {
      return this.executeCommand(this.commandObjects.tdigestMax(key));
   }

   @Override
   public double tdigestTrimmedMean(String key, double lowCutQuantile, double highCutQuantile) {
      return this.executeCommand(this.commandObjects.tdigestTrimmedMean(key, lowCutQuantile, highCutQuantile));
   }

   @Override
   public List<Long> tdigestRank(String key, double... values) {
      return this.executeCommand(this.commandObjects.tdigestRank(key, values));
   }

   @Override
   public List<Long> tdigestRevRank(String key, double... values) {
      return this.executeCommand(this.commandObjects.tdigestRevRank(key, values));
   }

   @Override
   public List<Double> tdigestByRank(String key, long... ranks) {
      return this.executeCommand(this.commandObjects.tdigestByRank(key, ranks));
   }

   @Override
   public List<Double> tdigestByRevRank(String key, long... ranks) {
      return this.executeCommand(this.commandObjects.tdigestByRevRank(key, ranks));
   }

   @Deprecated
   @Override
   public ResultSet graphQuery(String name, String query) {
      return this.executeCommand(this.graphCommandObjects.graphQuery(name, query));
   }

   @Deprecated
   @Override
   public ResultSet graphReadonlyQuery(String name, String query) {
      return this.executeCommand(this.graphCommandObjects.graphReadonlyQuery(name, query));
   }

   @Deprecated
   @Override
   public ResultSet graphQuery(String name, String query, long timeout) {
      return this.executeCommand(this.graphCommandObjects.graphQuery(name, query, timeout));
   }

   @Deprecated
   @Override
   public ResultSet graphReadonlyQuery(String name, String query, long timeout) {
      return this.executeCommand(this.graphCommandObjects.graphReadonlyQuery(name, query, timeout));
   }

   @Deprecated
   @Override
   public ResultSet graphQuery(String name, String query, Map<String, Object> params) {
      return this.executeCommand(this.graphCommandObjects.graphQuery(name, query, params));
   }

   @Deprecated
   @Override
   public ResultSet graphReadonlyQuery(String name, String query, Map<String, Object> params) {
      return this.executeCommand(this.graphCommandObjects.graphReadonlyQuery(name, query, params));
   }

   @Deprecated
   @Override
   public ResultSet graphQuery(String name, String query, Map<String, Object> params, long timeout) {
      return this.executeCommand(this.graphCommandObjects.graphQuery(name, query, params, timeout));
   }

   @Deprecated
   @Override
   public ResultSet graphReadonlyQuery(String name, String query, Map<String, Object> params, long timeout) {
      return this.executeCommand(this.graphCommandObjects.graphReadonlyQuery(name, query, params, timeout));
   }

   @Deprecated
   @Override
   public String graphDelete(String name) {
      return this.executeCommand(this.graphCommandObjects.graphDelete(name));
   }

   @Deprecated
   @Override
   public List<String> graphList() {
      return this.executeCommand(this.commandObjects.graphList());
   }

   @Deprecated
   @Override
   public List<String> graphProfile(String graphName, String query) {
      return this.executeCommand(this.commandObjects.graphProfile(graphName, query));
   }

   @Deprecated
   @Override
   public List<String> graphExplain(String graphName, String query) {
      return this.executeCommand(this.commandObjects.graphExplain(graphName, query));
   }

   @Deprecated
   @Override
   public List<List<Object>> graphSlowlog(String graphName) {
      return this.executeCommand(this.commandObjects.graphSlowlog(graphName));
   }

   @Deprecated
   @Override
   public String graphConfigSet(String configName, Object value) {
      return this.executeCommand(this.commandObjects.graphConfigSet(configName, value));
   }

   @Deprecated
   @Override
   public Map<String, Object> graphConfigGet(String configName) {
      return this.executeCommand(this.commandObjects.graphConfigGet(configName));
   }

   @Deprecated
   @Override
   public String tFunctionLoad(String libraryCode, TFunctionLoadParams params) {
      return this.executeCommand(this.commandObjects.tFunctionLoad(libraryCode, params));
   }

   @Deprecated
   @Override
   public String tFunctionDelete(String libraryName) {
      return this.executeCommand(this.commandObjects.tFunctionDelete(libraryName));
   }

   @Deprecated
   @Override
   public List<GearsLibraryInfo> tFunctionList(TFunctionListParams params) {
      return this.executeCommand(this.commandObjects.tFunctionList(params));
   }

   @Deprecated
   @Override
   public Object tFunctionCall(String library, String function, List<String> keys, List<String> args) {
      return this.executeCommand(this.commandObjects.tFunctionCall(library, function, keys, args));
   }

   @Deprecated
   @Override
   public Object tFunctionCallAsync(String library, String function, List<String> keys, List<String> args) {
      return this.executeCommand(this.commandObjects.tFunctionCallAsync(library, function, keys, args));
   }

   public PipelineBase pipelined() {
      if (this.provider == null) {
         throw new IllegalStateException("It is not allowed to create Pipeline from this " + this.getClass());
      } else {
         return this.provider instanceof MultiClusterPooledConnectionProvider
            ? new MultiClusterPipeline((MultiClusterPooledConnectionProvider)this.provider, this.commandObjects)
            : new Pipeline(this.provider.getConnection(), true, this.commandObjects);
      }
   }

   public AbstractTransaction multi() {
      return this.transaction(true);
   }

   public AbstractTransaction transaction(boolean doMulti) {
      if (this.provider == null) {
         throw new IllegalStateException("It is not allowed to create Transaction from this " + this.getClass());
      } else {
         return this.provider instanceof MultiClusterPooledConnectionProvider
            ? new MultiClusterTransaction((MultiClusterPooledConnectionProvider)this.provider, doMulti, this.commandObjects)
            : new Transaction(this.provider.getConnection(), doMulti, true, this.commandObjects);
      }
   }

   public Object sendCommand(ProtocolCommand cmd) {
      return this.executeCommand(this.commandObjects.commandArguments(cmd));
   }

   public Object sendCommand(ProtocolCommand cmd, byte[]... args) {
      return this.executeCommand(this.commandObjects.commandArguments(cmd).addObjects((Object[])args));
   }

   public Object sendBlockingCommand(ProtocolCommand cmd, byte[]... args) {
      return this.executeCommand(this.commandObjects.commandArguments(cmd).addObjects((Object[])args).blocking());
   }

   public Object sendCommand(ProtocolCommand cmd, String... args) {
      return this.executeCommand(this.commandObjects.commandArguments(cmd).addObjects(args));
   }

   public Object sendBlockingCommand(ProtocolCommand cmd, String... args) {
      return this.executeCommand(this.commandObjects.commandArguments(cmd).addObjects(args).blocking());
   }

   public Object sendCommand(byte[] sampleKey, ProtocolCommand cmd, byte[]... args) {
      return this.executeCommand(this.commandObjects.commandArguments(cmd).addObjects((Object[])args).processKey(sampleKey));
   }

   public Object sendBlockingCommand(byte[] sampleKey, ProtocolCommand cmd, byte[]... args) {
      return this.executeCommand(this.commandObjects.commandArguments(cmd).addObjects((Object[])args).blocking().processKey(sampleKey));
   }

   public Object sendCommand(String sampleKey, ProtocolCommand cmd, String... args) {
      return this.executeCommand(this.commandObjects.commandArguments(cmd).addObjects(args).processKey(sampleKey));
   }

   public Object sendBlockingCommand(String sampleKey, ProtocolCommand cmd, String... args) {
      return this.executeCommand(this.commandObjects.commandArguments(cmd).addObjects(args).blocking().processKey(sampleKey));
   }

   public Object executeCommand(CommandArguments args) {
      return this.executeCommand(new CommandObject<>(args, BuilderFactory.RAW_OBJECT));
   }

   @Experimental
   public void setKeyArgumentPreProcessor(CommandKeyArgumentPreProcessor keyPreProcessor) {
      this.commandObjects.setKeyArgumentPreProcessor(keyPreProcessor);
   }

   public void setJsonObjectMapper(JsonObjectMapper jsonObjectMapper) {
      this.commandObjects.setJsonObjectMapper(jsonObjectMapper);
   }

   public void setDefaultSearchDialect(int dialect) {
      this.commandObjects.setDefaultSearchDialect(dialect);
   }
}
