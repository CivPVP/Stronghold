package me.neznamy.tab.libs.redis.clients.jedis;

import java.io.Closeable;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import me.neznamy.tab.libs.redis.clients.jedis.args.BitCountOption;
import me.neznamy.tab.libs.redis.clients.jedis.args.BitOP;
import me.neznamy.tab.libs.redis.clients.jedis.args.ClientAttributeOption;
import me.neznamy.tab.libs.redis.clients.jedis.args.ClientPauseMode;
import me.neznamy.tab.libs.redis.clients.jedis.args.ClientType;
import me.neznamy.tab.libs.redis.clients.jedis.args.ClusterFailoverOption;
import me.neznamy.tab.libs.redis.clients.jedis.args.ClusterResetType;
import me.neznamy.tab.libs.redis.clients.jedis.args.ExpiryOption;
import me.neznamy.tab.libs.redis.clients.jedis.args.FlushMode;
import me.neznamy.tab.libs.redis.clients.jedis.args.FunctionRestorePolicy;
import me.neznamy.tab.libs.redis.clients.jedis.args.GeoUnit;
import me.neznamy.tab.libs.redis.clients.jedis.args.LatencyEvent;
import me.neznamy.tab.libs.redis.clients.jedis.args.ListDirection;
import me.neznamy.tab.libs.redis.clients.jedis.args.ListPosition;
import me.neznamy.tab.libs.redis.clients.jedis.args.Rawable;
import me.neznamy.tab.libs.redis.clients.jedis.args.SortedSetOption;
import me.neznamy.tab.libs.redis.clients.jedis.args.UnblockType;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ClusterCommands;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ControlBinaryCommands;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ControlCommands;
import me.neznamy.tab.libs.redis.clients.jedis.commands.DatabaseCommands;
import me.neznamy.tab.libs.redis.clients.jedis.commands.GenericControlCommands;
import me.neznamy.tab.libs.redis.clients.jedis.commands.JedisBinaryCommands;
import me.neznamy.tab.libs.redis.clients.jedis.commands.JedisCommands;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ModuleCommands;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ProtocolCommand;
import me.neznamy.tab.libs.redis.clients.jedis.commands.SentinelCommands;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ServerCommands;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.InvalidURIException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisConnectionException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;
import me.neznamy.tab.libs.redis.clients.jedis.params.BitPosParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ClientKillParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.CommandListFilterByParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.FailoverParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.GeoAddParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.GeoRadiusParam;
import me.neznamy.tab.libs.redis.clients.jedis.params.GeoRadiusStoreParam;
import me.neznamy.tab.libs.redis.clients.jedis.params.GeoSearchParam;
import me.neznamy.tab.libs.redis.clients.jedis.params.GetExParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.LCSParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.LPosParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.LolwutParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.MigrateParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ModuleLoadExParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.RestoreParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ScanParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.SetParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ShutdownParams;
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
import me.neznamy.tab.libs.redis.clients.jedis.resps.AccessControlLogEntry;
import me.neznamy.tab.libs.redis.clients.jedis.resps.AccessControlUser;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ClusterShardInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.CommandDocument;
import me.neznamy.tab.libs.redis.clients.jedis.resps.CommandInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.FunctionStats;
import me.neznamy.tab.libs.redis.clients.jedis.resps.GeoRadiusResponse;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LCSMatchResult;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LatencyHistoryInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LatencyLatestInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LibraryInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ScanResult;
import me.neznamy.tab.libs.redis.clients.jedis.resps.Slowlog;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamConsumerInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamConsumersInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamEntry;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamFullInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamGroupInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamPendingEntry;
import me.neznamy.tab.libs.redis.clients.jedis.resps.StreamPendingSummary;
import me.neznamy.tab.libs.redis.clients.jedis.resps.TrackingInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.Tuple;
import me.neznamy.tab.libs.redis.clients.jedis.util.JedisURIHelper;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;
import me.neznamy.tab.libs.redis.clients.jedis.util.Pool;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public class Jedis
   implements ServerCommands,
   DatabaseCommands,
   JedisCommands,
   JedisBinaryCommands,
   ControlCommands,
   ControlBinaryCommands,
   ClusterCommands,
   ModuleCommands,
   GenericControlCommands,
   SentinelCommands,
   Closeable {
   protected final Connection connection;
   private final CommandObjects commandObjects = new CommandObjects();
   private int db = 0;
   private Transaction transaction = null;
   private boolean isInMulti = false;
   private boolean isInWatch = false;
   private Pipeline pipeline = null;
   protected static final byte[][] DUMMY_ARRAY = new byte[0][];
   private Pool<Jedis> dataSource = null;

   public Jedis() {
      this.connection = new Connection();
   }

   public Jedis(String url) {
      this(URI.create(url));
   }

   public Jedis(HostAndPort hp) {
      this.connection = new Connection(hp);
   }

   public Jedis(String host, int port) {
      this.connection = new Connection(host, port);
   }

   public Jedis(String host, int port, JedisClientConfig config) {
      this(new HostAndPort(host, port), config);
   }

   public Jedis(HostAndPort hostPort, JedisClientConfig config) {
      this.connection = new Connection(hostPort, config);
      RedisProtocol proto = config.getRedisProtocol();
      if (proto != null) {
         this.commandObjects.setProtocol(proto);
      }
   }

   public Jedis(String host, int port, boolean ssl) {
      this(host, port, DefaultJedisClientConfig.builder().ssl(ssl).build());
   }

   public Jedis(String host, int port, boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
      this(
         host,
         port,
         DefaultJedisClientConfig.builder().ssl(ssl).sslSocketFactory(sslSocketFactory).sslParameters(sslParameters).hostnameVerifier(hostnameVerifier).build()
      );
   }

   public Jedis(String host, int port, int timeout) {
      this(host, port, timeout, timeout);
   }

   public Jedis(String host, int port, int timeout, boolean ssl) {
      this(host, port, timeout, timeout, ssl);
   }

   public Jedis(
      String host, int port, int timeout, boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier
   ) {
      this(host, port, timeout, timeout, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   public Jedis(String host, int port, int connectionTimeout, int soTimeout) {
      this(host, port, DefaultJedisClientConfig.builder().connectionTimeoutMillis(connectionTimeout).socketTimeoutMillis(soTimeout).build());
   }

   public Jedis(String host, int port, int connectionTimeout, int soTimeout, int infiniteSoTimeout) {
      this(
         host,
         port,
         DefaultJedisClientConfig.builder()
            .connectionTimeoutMillis(connectionTimeout)
            .socketTimeoutMillis(soTimeout)
            .blockingSocketTimeoutMillis(infiniteSoTimeout)
            .build()
      );
   }

   public Jedis(String host, int port, int connectionTimeout, int soTimeout, boolean ssl) {
      this(host, port, DefaultJedisClientConfig.builder().connectionTimeoutMillis(connectionTimeout).socketTimeoutMillis(soTimeout).ssl(ssl).build());
   }

   public Jedis(
      String host,
      int port,
      int connectionTimeout,
      int soTimeout,
      boolean ssl,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(
         host,
         port,
         DefaultJedisClientConfig.builder()
            .connectionTimeoutMillis(connectionTimeout)
            .socketTimeoutMillis(soTimeout)
            .ssl(ssl)
            .sslSocketFactory(sslSocketFactory)
            .sslParameters(sslParameters)
            .hostnameVerifier(hostnameVerifier)
            .build()
      );
   }

   public Jedis(
      String host,
      int port,
      int connectionTimeout,
      int soTimeout,
      int infiniteSoTimeout,
      boolean ssl,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(
         host,
         port,
         DefaultJedisClientConfig.builder()
            .connectionTimeoutMillis(connectionTimeout)
            .socketTimeoutMillis(soTimeout)
            .blockingSocketTimeoutMillis(infiniteSoTimeout)
            .ssl(ssl)
            .sslSocketFactory(sslSocketFactory)
            .sslParameters(sslParameters)
            .hostnameVerifier(hostnameVerifier)
            .build()
      );
   }

   public Jedis(URI uri) {
      if (!JedisURIHelper.isValid(uri)) {
         throw new InvalidURIException(String.format("Cannot open Redis connection due invalid URI \"%s\".", uri.toString()));
      }

      this.connection = new Connection(
         new HostAndPort(uri.getHost(), uri.getPort()),
         DefaultJedisClientConfig.builder()
            .user(JedisURIHelper.getUser(uri))
            .password(JedisURIHelper.getPassword(uri))
            .database(JedisURIHelper.getDBIndex(uri))
            .protocol(JedisURIHelper.getRedisProtocol(uri))
            .ssl(JedisURIHelper.isRedisSSLScheme(uri))
            .build()
      );
   }

   public Jedis(URI uri, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
      this(uri, DefaultJedisClientConfig.builder().sslSocketFactory(sslSocketFactory).sslParameters(sslParameters).hostnameVerifier(hostnameVerifier).build());
   }

   public Jedis(URI uri, int timeout) {
      this(uri, timeout, timeout);
   }

   public Jedis(URI uri, int timeout, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
      this(uri, timeout, timeout, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   public Jedis(URI uri, int connectionTimeout, int soTimeout) {
      this(uri, DefaultJedisClientConfig.builder().connectionTimeoutMillis(connectionTimeout).socketTimeoutMillis(soTimeout).build());
   }

   public Jedis(
      URI uri, int connectionTimeout, int soTimeout, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier
   ) {
      this(
         uri,
         DefaultJedisClientConfig.builder()
            .connectionTimeoutMillis(connectionTimeout)
            .socketTimeoutMillis(soTimeout)
            .sslSocketFactory(sslSocketFactory)
            .sslParameters(sslParameters)
            .hostnameVerifier(hostnameVerifier)
            .build()
      );
   }

   public Jedis(
      URI uri,
      int connectionTimeout,
      int soTimeout,
      int infiniteSoTimeout,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(
         uri,
         DefaultJedisClientConfig.builder()
            .connectionTimeoutMillis(connectionTimeout)
            .socketTimeoutMillis(soTimeout)
            .blockingSocketTimeoutMillis(infiniteSoTimeout)
            .sslSocketFactory(sslSocketFactory)
            .sslParameters(sslParameters)
            .hostnameVerifier(hostnameVerifier)
            .build()
      );
   }

   public Jedis(URI uri, JedisClientConfig config) {
      if (!JedisURIHelper.isValid(uri)) {
         throw new InvalidURIException(String.format("Cannot open Redis connection due invalid URI \"%s\".", uri.toString()));
      }

      this.connection = new Connection(
         new HostAndPort(uri.getHost(), uri.getPort()),
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
      RedisProtocol proto = config.getRedisProtocol();
      if (proto != null) {
         this.commandObjects.setProtocol(proto);
      }
   }

   public Jedis(JedisSocketFactory jedisSocketFactory) {
      this.connection = new Connection(jedisSocketFactory);
   }

   public Jedis(JedisSocketFactory jedisSocketFactory, JedisClientConfig clientConfig) {
      this.connection = new Connection(jedisSocketFactory, clientConfig);
      RedisProtocol proto = clientConfig.getRedisProtocol();
      if (proto != null) {
         this.commandObjects.setProtocol(proto);
      }
   }

   public Jedis(Connection connection) {
      this.connection = connection;
   }

   @Override
   public String toString() {
      return "Jedis{" + this.connection + '}';
   }

   public Connection getClient() {
      return this.getConnection();
   }

   public Connection getConnection() {
      return this.connection;
   }

   public void connect() {
      this.connection.connect();
   }

   public void disconnect() {
      this.connection.disconnect();
   }

   public boolean isConnected() {
      return this.connection.isConnected();
   }

   public boolean isBroken() {
      return this.connection.isBroken();
   }

   public void resetState() {
      if (this.isConnected()) {
         if (this.transaction != null) {
            this.transaction.close();
         }

         if (this.pipeline != null) {
            this.pipeline.close();
         }

         if (this.isInWatch) {
            this.connection.sendCommand(Protocol.Command.UNWATCH);
            this.connection.getStatusCodeReply();
            this.isInWatch = false;
         }
      }

      this.transaction = null;
      this.pipeline = null;
   }

   protected void setDataSource(Pool<Jedis> jedisPool) {
      this.dataSource = jedisPool;
   }

   @Override
   public void close() {
      if (this.dataSource != null) {
         Pool<Jedis> pool = this.dataSource;
         this.dataSource = null;
         if (this.isBroken()) {
            pool.returnBrokenResource(this);
         } else {
            pool.returnResource(this);
         }
      } else {
         this.connection.close();
      }
   }

   public Transaction multi() {
      this.transaction = new Transaction(this);
      return this.transaction;
   }

   public Pipeline pipelined() {
      this.pipeline = new Pipeline(this);
      return this.pipeline;
   }

   protected void checkIsInMultiOrPipeline() {
      if (this.transaction != null) {
         throw new IllegalStateException("Cannot use Jedis when in Multi. Please use Transaction or reset jedis state.");
      }

      if (this.pipeline != null && this.pipeline.hasPipelinedResponse()) {
         throw new IllegalStateException("Cannot use Jedis when in Pipeline. Please use Pipeline or reset jedis state.");
      }
   }

   public int getDB() {
      return this.db;
   }

   @Override
   public String ping() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.PING);
      return this.connection.getStatusCodeReply();
   }

   public byte[] ping(byte[] message) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.PING, message);
      return this.connection.getBinaryBulkReply();
   }

   @Override
   public String select(int index) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.SELECT, Protocol.toByteArray(index));
      String statusCodeReply = this.connection.getStatusCodeReply();
      this.db = index;
      return statusCodeReply;
   }

   @Override
   public String swapDB(int index1, int index2) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.SWAPDB, Protocol.toByteArray(index1), Protocol.toByteArray(index2));
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String flushDB() {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.flushDB());
   }

   @Override
   public String flushDB(FlushMode flushMode) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.FLUSHDB, flushMode.getRaw());
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String flushAll() {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.flushAll());
   }

   @Override
   public String flushAll(FlushMode flushMode) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.FLUSHALL, flushMode.getRaw());
      return this.connection.getStatusCodeReply();
   }

   @Override
   public boolean copy(byte[] srcKey, byte[] dstKey, int db, boolean replace) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.copy(srcKey, dstKey, db, replace));
   }

   @Override
   public boolean copy(byte[] srcKey, byte[] dstKey, boolean replace) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.copy(srcKey, dstKey, replace));
   }

   @Override
   public String set(byte[] key, byte[] value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.set(key, value));
   }

   @Override
   public String set(byte[] key, byte[] value, SetParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.set(key, value, params));
   }

   @Override
   public byte[] get(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.get(key));
   }

   @Override
   public byte[] setGet(byte[] key, byte[] value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.setGet(key, value));
   }

   @Override
   public byte[] setGet(byte[] key, byte[] value, SetParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.setGet(key, value, params));
   }

   @Override
   public byte[] getDel(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.getDel(key));
   }

   @Override
   public byte[] getEx(byte[] key, GetExParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.getEx(key, params));
   }

   @Override
   public long exists(byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.exists(keys));
   }

   @Override
   public boolean exists(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.exists(key));
   }

   @Override
   public long del(byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.del(keys));
   }

   @Override
   public long del(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.del(key));
   }

   @Override
   public long unlink(byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.unlink(keys));
   }

   @Override
   public long unlink(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.unlink(key));
   }

   @Override
   public String type(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.type(key));
   }

   @Override
   public Set<byte[]> keys(byte[] pattern) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.keys(pattern));
   }

   @Override
   public byte[] randomBinaryKey() {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.randomBinaryKey());
   }

   @Override
   public String rename(byte[] oldkey, byte[] newkey) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.rename(oldkey, newkey));
   }

   @Override
   public long renamenx(byte[] oldkey, byte[] newkey) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.renamenx(oldkey, newkey));
   }

   @Override
   public long dbSize() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.DBSIZE);
      return this.connection.getIntegerReply();
   }

   @Override
   public long expire(byte[] key, long seconds) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.expire(key, seconds));
   }

   @Override
   public long expire(byte[] key, long seconds, ExpiryOption expiryOption) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.expire(key, seconds, expiryOption));
   }

   @Override
   public long pexpire(byte[] key, long milliseconds) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pexpire(key, milliseconds));
   }

   @Override
   public long pexpire(byte[] key, long milliseconds, ExpiryOption expiryOption) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pexpire(key, milliseconds, expiryOption));
   }

   @Override
   public long expireTime(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.expireTime(key));
   }

   @Override
   public long pexpireTime(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pexpireTime(key));
   }

   @Override
   public long expireAt(byte[] key, long unixTime) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.expireAt(key, unixTime));
   }

   @Override
   public long expireAt(byte[] key, long unixTime, ExpiryOption expiryOption) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.expireAt(key, unixTime, expiryOption));
   }

   @Override
   public long pexpireAt(byte[] key, long millisecondsTimestamp) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pexpireAt(key, millisecondsTimestamp));
   }

   @Override
   public long pexpireAt(byte[] key, long millisecondsTimestamp, ExpiryOption expiryOption) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pexpireAt(key, millisecondsTimestamp, expiryOption));
   }

   @Override
   public long ttl(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.ttl(key));
   }

   @Override
   public long touch(byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.touch(keys));
   }

   @Override
   public long touch(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.touch(key));
   }

   @Override
   public long move(byte[] key, int dbIndex) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.MOVE, key, Protocol.toByteArray(dbIndex));
      return this.connection.getIntegerReply();
   }

   @Deprecated
   @Override
   public byte[] getSet(byte[] key, byte[] value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.getSet(key, value));
   }

   @Override
   public List<byte[]> mget(byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.mget(keys));
   }

   @Override
   public long setnx(byte[] key, byte[] value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.setnx(key, value));
   }

   @Override
   public String setex(byte[] key, long seconds, byte[] value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.setex(key, seconds, value));
   }

   @Override
   public String mset(byte[]... keysvalues) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.mset(keysvalues));
   }

   @Override
   public long msetnx(byte[]... keysvalues) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.msetnx(keysvalues));
   }

   @Override
   public long decrBy(byte[] key, long decrement) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.decrBy(key, decrement));
   }

   @Override
   public long decr(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.decr(key));
   }

   @Override
   public long incrBy(byte[] key, long increment) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.incrBy(key, increment));
   }

   @Override
   public double incrByFloat(byte[] key, double increment) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.incrByFloat(key, increment));
   }

   @Override
   public long incr(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.incr(key));
   }

   @Override
   public long append(byte[] key, byte[] value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.append(key, value));
   }

   @Override
   public byte[] substr(byte[] key, int start, int end) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.substr(key, start, end));
   }

   @Override
   public long hset(byte[] key, byte[] field, byte[] value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hset(key, field, value));
   }

   @Override
   public long hset(byte[] key, Map<byte[], byte[]> hash) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hset(key, hash));
   }

   @Override
   public byte[] hget(byte[] key, byte[] field) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hget(key, field));
   }

   @Override
   public long hsetnx(byte[] key, byte[] field, byte[] value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hsetnx(key, field, value));
   }

   @Override
   public String hmset(byte[] key, Map<byte[], byte[]> hash) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hmset(key, hash));
   }

   @Override
   public List<byte[]> hmget(byte[] key, byte[]... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hmget(key, fields));
   }

   @Override
   public long hincrBy(byte[] key, byte[] field, long value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hincrBy(key, field, value));
   }

   @Override
   public double hincrByFloat(byte[] key, byte[] field, double value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hincrByFloat(key, field, value));
   }

   @Override
   public boolean hexists(byte[] key, byte[] field) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hexists(key, field));
   }

   @Override
   public long hdel(byte[] key, byte[]... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hdel(key, fields));
   }

   @Override
   public long hlen(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hlen(key));
   }

   @Override
   public Set<byte[]> hkeys(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hkeys(key));
   }

   @Override
   public List<byte[]> hvals(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hvals(key));
   }

   @Override
   public Map<byte[], byte[]> hgetAll(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hgetAll(key));
   }

   @Override
   public byte[] hrandfield(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hrandfield(key));
   }

   @Override
   public List<byte[]> hrandfield(byte[] key, long count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hrandfield(key, count));
   }

   @Override
   public List<Entry<byte[], byte[]>> hrandfieldWithValues(byte[] key, long count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hrandfieldWithValues(key, count));
   }

   @Override
   public long rpush(byte[] key, byte[]... strings) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.rpush(key, strings));
   }

   @Override
   public long lpush(byte[] key, byte[]... strings) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lpush(key, strings));
   }

   @Override
   public long llen(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.llen(key));
   }

   @Override
   public List<byte[]> lrange(byte[] key, long start, long stop) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lrange(key, start, stop));
   }

   @Override
   public String ltrim(byte[] key, long start, long stop) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.ltrim(key, start, stop));
   }

   @Override
   public byte[] lindex(byte[] key, long index) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lindex(key, index));
   }

   @Override
   public String lset(byte[] key, long index, byte[] value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lset(key, index, value));
   }

   @Override
   public long lrem(byte[] key, long count, byte[] value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lrem(key, count, value));
   }

   @Override
   public byte[] lpop(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lpop(key));
   }

   @Override
   public List<byte[]> lpop(byte[] key, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lpop(key, count));
   }

   @Override
   public Long lpos(byte[] key, byte[] element) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lpos(key, element));
   }

   @Override
   public Long lpos(byte[] key, byte[] element, LPosParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lpos(key, element, params));
   }

   @Override
   public List<Long> lpos(byte[] key, byte[] element, LPosParams params, long count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lpos(key, element, params, count));
   }

   @Override
   public byte[] rpop(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.rpop(key));
   }

   @Override
   public List<byte[]> rpop(byte[] key, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.rpop(key, count));
   }

   @Override
   public byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.rpoplpush(srckey, dstkey));
   }

   @Override
   public long sadd(byte[] key, byte[]... members) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sadd(key, members));
   }

   @Override
   public Set<byte[]> smembers(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.smembers(key));
   }

   @Override
   public long srem(byte[] key, byte[]... members) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.srem(key, members));
   }

   @Override
   public byte[] spop(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.spop(key));
   }

   @Override
   public Set<byte[]> spop(byte[] key, long count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.spop(key, count));
   }

   @Override
   public long smove(byte[] srckey, byte[] dstkey, byte[] member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.smove(srckey, dstkey, member));
   }

   @Override
   public long scard(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.scard(key));
   }

   @Override
   public boolean sismember(byte[] key, byte[] member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sismember(key, member));
   }

   @Override
   public List<Boolean> smismember(byte[] key, byte[]... members) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.smismember(key, members));
   }

   @Override
   public Set<byte[]> sinter(byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sinter(keys));
   }

   @Override
   public long sinterstore(byte[] dstkey, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sinterstore(dstkey, keys));
   }

   @Override
   public long sintercard(byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sintercard(keys));
   }

   @Override
   public long sintercard(int limit, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sintercard(limit, keys));
   }

   @Override
   public Set<byte[]> sunion(byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sunion(keys));
   }

   @Override
   public long sunionstore(byte[] dstkey, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sunionstore(dstkey, keys));
   }

   @Override
   public Set<byte[]> sdiff(byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sdiff(keys));
   }

   @Override
   public long sdiffstore(byte[] dstkey, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sdiffstore(dstkey, keys));
   }

   @Override
   public byte[] srandmember(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.srandmember(key));
   }

   @Override
   public List<byte[]> srandmember(byte[] key, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.srandmember(key, count));
   }

   @Override
   public long zadd(byte[] key, double score, byte[] member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zadd(key, score, member));
   }

   @Override
   public long zadd(byte[] key, double score, byte[] member, ZAddParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zadd(key, score, member, params));
   }

   @Override
   public long zadd(byte[] key, Map<byte[], Double> scoreMembers) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zadd(key, scoreMembers));
   }

   @Override
   public long zadd(byte[] key, Map<byte[], Double> scoreMembers, ZAddParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zadd(key, scoreMembers, params));
   }

   @Override
   public Double zaddIncr(byte[] key, double score, byte[] member, ZAddParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zaddIncr(key, score, member, params));
   }

   @Override
   public List<byte[]> zrange(byte[] key, long start, long stop) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrange(key, start, stop));
   }

   @Override
   public long zrem(byte[] key, byte[]... members) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrem(key, members));
   }

   @Override
   public double zincrby(byte[] key, double increment, byte[] member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zincrby(key, increment, member));
   }

   @Override
   public Double zincrby(byte[] key, double increment, byte[] member, ZIncrByParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zincrby(key, increment, member, params));
   }

   @Override
   public Long zrank(byte[] key, byte[] member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrank(key, member));
   }

   @Override
   public Long zrevrank(byte[] key, byte[] member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrank(key, member));
   }

   @Override
   public KeyValue<Long, Double> zrankWithScore(byte[] key, byte[] member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrankWithScore(key, member));
   }

   @Override
   public KeyValue<Long, Double> zrevrankWithScore(byte[] key, byte[] member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrankWithScore(key, member));
   }

   @Override
   public List<byte[]> zrevrange(byte[] key, long start, long stop) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrange(key, start, stop));
   }

   @Override
   public List<Tuple> zrangeWithScores(byte[] key, long start, long stop) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeWithScores(key, start, stop));
   }

   @Override
   public List<Tuple> zrevrangeWithScores(byte[] key, long start, long stop) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeWithScores(key, start, stop));
   }

   @Override
   public List<byte[]> zrange(byte[] key, ZRangeParams zRangeParams) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrange(key, zRangeParams));
   }

   @Override
   public List<Tuple> zrangeWithScores(byte[] key, ZRangeParams zRangeParams) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeWithScores(key, zRangeParams));
   }

   @Override
   public long zrangestore(byte[] dest, byte[] src, ZRangeParams zRangeParams) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangestore(dest, src, zRangeParams));
   }

   @Override
   public byte[] zrandmember(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrandmember(key));
   }

   @Override
   public List<byte[]> zrandmember(byte[] key, long count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrandmember(key, count));
   }

   @Override
   public List<Tuple> zrandmemberWithScores(byte[] key, long count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrandmemberWithScores(key, count));
   }

   @Override
   public long zcard(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zcard(key));
   }

   @Override
   public Double zscore(byte[] key, byte[] member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zscore(key, member));
   }

   @Override
   public List<Double> zmscore(byte[] key, byte[]... members) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zmscore(key, members));
   }

   @Override
   public Tuple zpopmax(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zpopmax(key));
   }

   @Override
   public List<Tuple> zpopmax(byte[] key, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zpopmax(key, count));
   }

   @Override
   public Tuple zpopmin(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zpopmin(key));
   }

   @Override
   public List<Tuple> zpopmin(byte[] key, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zpopmin(key, count));
   }

   public String watch(byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.WATCH, keys);
      String status = this.connection.getStatusCodeReply();
      this.isInWatch = true;
      return status;
   }

   public String unwatch() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.UNWATCH);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public List<byte[]> sort(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sort(key));
   }

   @Override
   public List<byte[]> sort(byte[] key, SortingParams sortingParams) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sort(key, sortingParams));
   }

   @Override
   public long sort(byte[] key, SortingParams sortingParams, byte[] dstkey) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sort(key, sortingParams, dstkey));
   }

   @Override
   public long sort(byte[] key, byte[] dstkey) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sort(key, dstkey));
   }

   @Override
   public List<byte[]> sortReadonly(byte[] key, SortingParams sortingParams) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sortReadonly(key, sortingParams));
   }

   @Override
   public byte[] lmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lmove(srcKey, dstKey, from, to));
   }

   @Override
   public byte[] blmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to, double timeout) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.blmove(srcKey, dstKey, from, to, timeout));
   }

   @Override
   public List<byte[]> blpop(int timeout, byte[]... keys) {
      return this.connection.executeCommand(this.commandObjects.blpop(timeout, keys));
   }

   @Override
   public KeyValue<byte[], byte[]> blpop(double timeout, byte[]... keys) {
      return this.connection.executeCommand(this.commandObjects.blpop(timeout, keys));
   }

   @Override
   public List<byte[]> brpop(int timeout, byte[]... keys) {
      return this.connection.executeCommand(this.commandObjects.brpop(timeout, keys));
   }

   @Override
   public KeyValue<byte[], byte[]> brpop(double timeout, byte[]... keys) {
      return this.connection.executeCommand(this.commandObjects.brpop(timeout, keys));
   }

   @Override
   public KeyValue<byte[], List<byte[]>> lmpop(ListDirection direction, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lmpop(direction, keys));
   }

   @Override
   public KeyValue<byte[], List<byte[]>> lmpop(ListDirection direction, int count, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lmpop(direction, count, keys));
   }

   @Override
   public KeyValue<byte[], List<byte[]>> blmpop(double timeout, ListDirection direction, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.blmpop(timeout, direction, keys));
   }

   @Override
   public KeyValue<byte[], List<byte[]>> blmpop(double timeout, ListDirection direction, int count, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.blmpop(timeout, direction, count, keys));
   }

   @Override
   public KeyValue<byte[], Tuple> bzpopmax(double timeout, byte[]... keys) {
      return this.connection.executeCommand(this.commandObjects.bzpopmax(timeout, keys));
   }

   @Override
   public KeyValue<byte[], Tuple> bzpopmin(double timeout, byte[]... keys) {
      return this.connection.executeCommand(this.commandObjects.bzpopmin(timeout, keys));
   }

   @Override
   public String auth(String password) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.AUTH, password);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String auth(String user, String password) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.AUTH, user, password);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public long zcount(byte[] key, double min, double max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zcount(key, min, max));
   }

   @Override
   public long zcount(byte[] key, byte[] min, byte[] max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zcount(key, min, max));
   }

   @Override
   public List<byte[]> zdiff(byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zdiff(keys));
   }

   @Override
   public List<Tuple> zdiffWithScores(byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zdiffWithScores(keys));
   }

   @Deprecated
   @Override
   public long zdiffStore(byte[] dstkey, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zdiffStore(dstkey, keys));
   }

   @Override
   public long zdiffstore(byte[] dstkey, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zdiffstore(dstkey, keys));
   }

   @Override
   public List<byte[]> zrangeByScore(byte[] key, double min, double max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByScore(key, min, max));
   }

   @Override
   public List<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByScore(key, min, max));
   }

   @Override
   public List<byte[]> zrangeByScore(byte[] key, double min, double max, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByScore(key, min, max, offset, count));
   }

   @Override
   public List<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByScore(key, min, max, offset, count));
   }

   @Override
   public List<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max));
   }

   @Override
   public List<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max));
   }

   @Override
   public List<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max, offset, count));
   }

   @Override
   public List<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max, offset, count));
   }

   @Override
   public List<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByScore(key, max, min));
   }

   @Override
   public List<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByScore(key, max, min));
   }

   @Override
   public List<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByScore(key, max, min, offset, count));
   }

   @Override
   public List<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByScore(key, max, min, offset, count));
   }

   @Override
   public List<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min));
   }

   @Override
   public List<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min, offset, count));
   }

   @Override
   public List<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min));
   }

   @Override
   public List<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min, offset, count));
   }

   @Override
   public long zremrangeByRank(byte[] key, long start, long stop) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zremrangeByRank(key, start, stop));
   }

   @Override
   public long zremrangeByScore(byte[] key, double min, double max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zremrangeByScore(key, min, max));
   }

   @Override
   public long zremrangeByScore(byte[] key, byte[] min, byte[] max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zremrangeByScore(key, min, max));
   }

   @Override
   public List<byte[]> zunion(ZParams params, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zunion(params, keys));
   }

   @Override
   public List<Tuple> zunionWithScores(ZParams params, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zunionWithScores(params, keys));
   }

   @Override
   public long zunionstore(byte[] dstkey, byte[]... sets) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zunionstore(dstkey, sets));
   }

   @Override
   public long zunionstore(byte[] dstkey, ZParams params, byte[]... sets) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zunionstore(dstkey, params, sets));
   }

   @Override
   public List<byte[]> zinter(ZParams params, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zinter(params, keys));
   }

   @Override
   public List<Tuple> zinterWithScores(ZParams params, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zinterWithScores(params, keys));
   }

   @Override
   public long zinterstore(byte[] dstkey, byte[]... sets) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zinterstore(dstkey, sets));
   }

   @Override
   public long zinterstore(byte[] dstkey, ZParams params, byte[]... sets) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zinterstore(dstkey, params, sets));
   }

   @Override
   public long zintercard(byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zintercard(keys));
   }

   @Override
   public long zintercard(long limit, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zintercard(limit, keys));
   }

   @Override
   public long zlexcount(byte[] key, byte[] min, byte[] max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zlexcount(key, min, max));
   }

   @Override
   public List<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByLex(key, min, max));
   }

   @Override
   public List<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByLex(key, min, max, offset, count));
   }

   @Override
   public List<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByLex(key, max, min));
   }

   @Override
   public List<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByLex(key, max, min, offset, count));
   }

   @Override
   public long zremrangeByLex(byte[] key, byte[] min, byte[] max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zremrangeByLex(key, min, max));
   }

   @Override
   public KeyValue<byte[], List<Tuple>> zmpop(SortedSetOption option, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zmpop(option, keys));
   }

   @Override
   public KeyValue<byte[], List<Tuple>> zmpop(SortedSetOption option, int count, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zmpop(option, count, keys));
   }

   @Override
   public KeyValue<byte[], List<Tuple>> bzmpop(double timeout, SortedSetOption option, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bzmpop(timeout, option, keys));
   }

   @Override
   public KeyValue<byte[], List<Tuple>> bzmpop(double timeout, SortedSetOption option, int count, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bzmpop(timeout, option, count, keys));
   }

   @Override
   public String save() {
      this.connection.sendCommand(Protocol.Command.SAVE);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String bgsave() {
      this.connection.sendCommand(Protocol.Command.BGSAVE);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String bgsaveSchedule() {
      this.connection.sendCommand(Protocol.Command.BGSAVE, Protocol.Keyword.SCHEDULE);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String bgrewriteaof() {
      this.connection.sendCommand(Protocol.Command.BGREWRITEAOF);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public long lastsave() {
      this.connection.sendCommand(Protocol.Command.LASTSAVE);
      return this.connection.getIntegerReply();
   }

   @Override
   public void shutdown() throws JedisException {
      this.connection.sendCommand(Protocol.Command.SHUTDOWN);

      try {
         throw new JedisException(this.connection.getStatusCodeReply());
      } catch (JedisConnectionException jce) {
         this.connection.setBroken();
      }
   }

   @Override
   public void shutdown(ShutdownParams shutdownParams) throws JedisException {
      this.connection.sendCommand(new CommandArguments(Protocol.Command.SHUTDOWN).addParams(shutdownParams));

      try {
         throw new JedisException(this.connection.getStatusCodeReply());
      } catch (JedisConnectionException jce) {
         this.connection.setBroken();
      }
   }

   @Override
   public String shutdownAbort() {
      this.connection.sendCommand(Protocol.Command.SHUTDOWN, Protocol.Keyword.ABORT);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String info() {
      this.connection.sendCommand(Protocol.Command.INFO);
      return this.connection.getBulkReply();
   }

   @Override
   public String info(String section) {
      this.connection.sendCommand(Protocol.Command.INFO, section);
      return this.connection.getBulkReply();
   }

   public void monitor(JedisMonitor jedisMonitor) {
      this.connection.sendCommand(Protocol.Command.MONITOR);
      this.connection.getStatusCodeReply();
      jedisMonitor.proceed(this.connection);
   }

   @Deprecated
   @Override
   public String slaveof(String host, int port) {
      this.connection.sendCommand(Protocol.Command.SLAVEOF, SafeEncoder.encode(host), Protocol.toByteArray(port));
      return this.connection.getStatusCodeReply();
   }

   @Deprecated
   @Override
   public String slaveofNoOne() {
      this.connection.sendCommand(Protocol.Command.SLAVEOF, Protocol.Keyword.NO.getRaw(), Protocol.Keyword.ONE.getRaw());
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String replicaof(String host, int port) {
      this.connection.sendCommand(Protocol.Command.REPLICAOF, SafeEncoder.encode(host), Protocol.toByteArray(port));
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String replicaofNoOne() {
      this.connection.sendCommand(Protocol.Command.REPLICAOF, Protocol.Keyword.NO.getRaw(), Protocol.Keyword.ONE.getRaw());
      return this.connection.getStatusCodeReply();
   }

   @Override
   public List<Object> roleBinary() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ROLE);
      return BuilderFactory.RAW_OBJECT_LIST.build(this.connection.getOne());
   }

   @Override
   public Map<byte[], byte[]> configGet(byte[] pattern) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CONFIG, Protocol.Keyword.GET.getRaw(), pattern);
      return BuilderFactory.BINARY_MAP.build(this.connection.getOne());
   }

   @Override
   public Map<byte[], byte[]> configGet(byte[]... patterns) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CONFIG, joinParameters(Protocol.Keyword.GET.getRaw(), patterns));
      return BuilderFactory.BINARY_MAP.build(this.connection.getOne());
   }

   @Override
   public String configResetStat() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CONFIG, Protocol.Keyword.RESETSTAT);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String configRewrite() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CONFIG, Protocol.Keyword.REWRITE);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String configSet(byte[] parameter, byte[] value) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CONFIG, Protocol.Keyword.SET.getRaw(), parameter, value);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String configSet(byte[]... parameterValues) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CONFIG, joinParameters(Protocol.Keyword.SET.getRaw(), parameterValues));
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String configSetBinary(Map<byte[], byte[]> parameterValues) {
      this.checkIsInMultiOrPipeline();
      CommandArguments args = new CommandArguments(Protocol.Command.CONFIG).add(Protocol.Keyword.SET);
      parameterValues.forEach((k, v) -> args.add(k).add(v));
      this.connection.sendCommand(args);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public long strlen(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.strlen(key));
   }

   @Override
   public LCSMatchResult lcs(byte[] keyA, byte[] keyB, LCSParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lcs(keyA, keyB, params));
   }

   @Override
   public long lpushx(byte[] key, byte[]... strings) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lpushx(key, strings));
   }

   @Override
   public long persist(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.persist(key));
   }

   @Override
   public long rpushx(byte[] key, byte[]... strings) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.rpushx(key, strings));
   }

   @Override
   public byte[] echo(byte[] string) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ECHO, string);
      return this.connection.getBinaryBulkReply();
   }

   @Override
   public long linsert(byte[] key, ListPosition where, byte[] pivot, byte[] value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.linsert(key, where, pivot, value));
   }

   @Override
   public byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.brpoplpush(source, destination, timeout));
   }

   @Override
   public boolean setbit(byte[] key, long offset, boolean value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.setbit(key, offset, value));
   }

   @Override
   public boolean getbit(byte[] key, long offset) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.getbit(key, offset));
   }

   @Override
   public long bitpos(byte[] key, boolean value) {
      return this.bitpos(key, value, new BitPosParams());
   }

   @Override
   public long bitpos(byte[] key, boolean value, BitPosParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bitpos(key, value, params));
   }

   @Override
   public long setrange(byte[] key, long offset, byte[] value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.setrange(key, offset, value));
   }

   @Override
   public byte[] getrange(byte[] key, long startOffset, long endOffset) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.getrange(key, startOffset, endOffset));
   }

   public long publish(byte[] channel, byte[] message) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.publish(channel, message));
   }

   public void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels) {
      jedisPubSub.proceed(this.connection, channels);
   }

   public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
      jedisPubSub.proceedWithPatterns(this.connection, patterns);
   }

   @Override
   public Object eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.eval(script, keys, args));
   }

   @Override
   public Object evalReadonly(byte[] script, List<byte[]> keys, List<byte[]> args) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.evalReadonly(script, keys, args));
   }

   protected static byte[][] getParamsWithBinary(List<byte[]> keys, List<byte[]> args) {
      int keyCount = keys.size();
      int argCount = args.size();
      byte[][] params = new byte[keyCount + argCount][];

      for (int i = 0; i < keyCount; i++) {
         params[i] = keys.get(i);
      }

      for (int i = 0; i < argCount; i++) {
         params[keyCount + i] = args.get(i);
      }

      return params;
   }

   @Override
   public Object eval(byte[] script, int keyCount, byte[]... params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.eval(script, keyCount, params));
   }

   @Override
   public Object eval(byte[] script) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.eval(script));
   }

   @Override
   public Object evalsha(byte[] sha1) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.evalsha(sha1));
   }

   @Override
   public Object evalsha(byte[] sha1, List<byte[]> keys, List<byte[]> args) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.evalsha(sha1, keys, args));
   }

   @Override
   public Object evalshaReadonly(byte[] sha1, List<byte[]> keys, List<byte[]> args) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.evalshaReadonly(sha1, keys, args));
   }

   @Override
   public Object evalsha(byte[] sha1, int keyCount, byte[]... params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.evalsha(sha1, keyCount, params));
   }

   @Override
   public String scriptFlush() {
      this.connection.sendCommand(Protocol.Command.SCRIPT, Protocol.Keyword.FLUSH);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String scriptFlush(FlushMode flushMode) {
      this.connection.sendCommand(Protocol.Command.SCRIPT, Protocol.Keyword.FLUSH.getRaw(), flushMode.getRaw());
      return this.connection.getStatusCodeReply();
   }

   @Override
   public Boolean scriptExists(byte[] sha1) {
      byte[][] a = new byte[][]{sha1};
      return this.scriptExists(a).get(0);
   }

   @Override
   public List<Boolean> scriptExists(byte[]... sha1) {
      this.connection.sendCommand(Protocol.Command.SCRIPT, joinParameters(Protocol.Keyword.EXISTS.getRaw(), sha1));
      return BuilderFactory.BOOLEAN_LIST.build(this.connection.getOne());
   }

   @Override
   public byte[] scriptLoad(byte[] script) {
      this.connection.sendCommand(Protocol.Command.SCRIPT, Protocol.Keyword.LOAD.getRaw(), script);
      return this.connection.getBinaryBulkReply();
   }

   @Override
   public String scriptKill() {
      return this.connection.executeCommand(this.commandObjects.scriptKill());
   }

   @Override
   public String slowlogReset() {
      return this.connection.executeCommand(this.commandObjects.slowlogReset());
   }

   @Override
   public long slowlogLen() {
      this.connection.sendCommand(Protocol.Command.SLOWLOG, Protocol.Keyword.LEN);
      return this.connection.getIntegerReply();
   }

   @Override
   public List<Object> slowlogGetBinary() {
      this.connection.sendCommand(Protocol.Command.SLOWLOG, Protocol.Keyword.GET);
      return this.connection.getObjectMultiBulkReply();
   }

   @Override
   public List<Object> slowlogGetBinary(long entries) {
      this.connection.sendCommand(Protocol.Command.SLOWLOG, Protocol.Keyword.GET.getRaw(), Protocol.toByteArray(entries));
      return this.connection.getObjectMultiBulkReply();
   }

   @Override
   public Long objectRefcount(byte[] key) {
      this.connection.sendCommand(Protocol.Command.OBJECT, Protocol.Keyword.REFCOUNT.getRaw(), key);
      return this.connection.getIntegerReply();
   }

   @Override
   public byte[] objectEncoding(byte[] key) {
      this.connection.sendCommand(Protocol.Command.OBJECT, Protocol.Keyword.ENCODING.getRaw(), key);
      return this.connection.getBinaryBulkReply();
   }

   @Override
   public Long objectIdletime(byte[] key) {
      this.connection.sendCommand(Protocol.Command.OBJECT, Protocol.Keyword.IDLETIME.getRaw(), key);
      return this.connection.getIntegerReply();
   }

   @Override
   public List<byte[]> objectHelpBinary() {
      this.connection.sendCommand(Protocol.Command.OBJECT, Protocol.Keyword.HELP);
      return this.connection.getBinaryMultiBulkReply();
   }

   @Override
   public Long objectFreq(byte[] key) {
      this.connection.sendCommand(Protocol.Command.OBJECT, Protocol.Keyword.FREQ.getRaw(), key);
      return this.connection.getIntegerReply();
   }

   @Override
   public long bitcount(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bitcount(key));
   }

   @Override
   public long bitcount(byte[] key, long start, long end) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bitcount(key, start, end));
   }

   @Override
   public long bitcount(byte[] key, long start, long end, BitCountOption option) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bitcount(key, start, end, option));
   }

   @Override
   public long bitop(BitOP op, byte[] destKey, byte[]... srcKeys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bitop(op, destKey, srcKeys));
   }

   @Override
   public byte[] dump(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.dump(key));
   }

   @Override
   public String restore(byte[] key, long ttl, byte[] serializedValue) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.restore(key, ttl, serializedValue));
   }

   @Override
   public String restore(byte[] key, long ttl, byte[] serializedValue, RestoreParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.restore(key, ttl, serializedValue, params));
   }

   @Override
   public long pttl(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pttl(key));
   }

   @Override
   public String psetex(byte[] key, long milliseconds, byte[] value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.psetex(key, milliseconds, value));
   }

   @Override
   public byte[] memoryDoctorBinary() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.MEMORY, Protocol.Keyword.DOCTOR);
      return this.connection.getBinaryBulkReply();
   }

   @Override
   public Long memoryUsage(byte[] key) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.MEMORY, Protocol.Keyword.USAGE.getRaw(), key);
      return this.connection.getIntegerReply();
   }

   @Override
   public Long memoryUsage(byte[] key, int samples) {
      this.checkIsInMultiOrPipeline();
      this.connection
         .sendCommand(Protocol.Command.MEMORY, Protocol.Keyword.USAGE.getRaw(), key, Protocol.Keyword.SAMPLES.getRaw(), Protocol.toByteArray(samples));
      return this.connection.getIntegerReply();
   }

   @Override
   public String failover() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.FAILOVER);
      this.connection.setTimeoutInfinite();

      try {
         return this.connection.getStatusCodeReply();
      } finally {
         this.connection.rollbackTimeout();
      }
   }

   @Override
   public String failover(FailoverParams failoverParams) {
      this.checkIsInMultiOrPipeline();
      CommandArguments args = new ClusterCommandArguments(Protocol.Command.FAILOVER).addParams(failoverParams);
      this.connection.sendCommand(args);
      this.connection.setTimeoutInfinite();

      try {
         return this.connection.getStatusCodeReply();
      } finally {
         this.connection.rollbackTimeout();
      }
   }

   @Override
   public String failoverAbort() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.FAILOVER, Protocol.Keyword.ABORT);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public byte[] aclWhoAmIBinary() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.WHOAMI);
      return this.connection.getBinaryBulkReply();
   }

   @Override
   public byte[] aclGenPassBinary() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.GENPASS);
      return this.connection.getBinaryBulkReply();
   }

   @Override
   public byte[] aclGenPassBinary(int bits) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.GENPASS.getRaw(), Protocol.toByteArray(bits));
      return this.connection.getBinaryBulkReply();
   }

   @Override
   public List<byte[]> aclListBinary() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.LIST);
      return this.connection.getBinaryMultiBulkReply();
   }

   @Override
   public List<byte[]> aclUsersBinary() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.USERS);
      return this.connection.getBinaryMultiBulkReply();
   }

   @Override
   public AccessControlUser aclGetUser(byte[] name) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.GETUSER.getRaw(), name);
      return BuilderFactory.ACCESS_CONTROL_USER.build(this.connection.getObjectMultiBulkReply());
   }

   @Override
   public String aclSetUser(byte[] name) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.SETUSER.getRaw(), name);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String aclSetUser(byte[] name, byte[]... rules) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, joinParameters(Protocol.Keyword.SETUSER.getRaw(), name, rules));
      return this.connection.getStatusCodeReply();
   }

   @Override
   public long aclDelUser(byte[]... names) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, joinParameters(Protocol.Keyword.DELUSER.getRaw(), names));
      return this.connection.getIntegerReply();
   }

   @Override
   public List<byte[]> aclCatBinary() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.CAT);
      return this.connection.getBinaryMultiBulkReply();
   }

   @Override
   public List<byte[]> aclCat(byte[] category) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.CAT.getRaw(), category);
      return this.connection.getBinaryMultiBulkReply();
   }

   @Override
   public List<byte[]> aclLogBinary() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.LOG);
      return this.connection.getBinaryMultiBulkReply();
   }

   @Override
   public List<byte[]> aclLogBinary(int limit) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.LOG.getRaw(), Protocol.toByteArray(limit));
      return this.connection.getBinaryMultiBulkReply();
   }

   @Override
   public String aclLogReset() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.LOG.getRaw(), Protocol.Keyword.RESET.getRaw());
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clientKill(byte[] ipPort) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.KILL.getRaw(), ipPort);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clientKill(String ip, int port) {
      return this.clientKill(ip + ':' + port);
   }

   @Override
   public long clientKill(ClientKillParams params) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(new CommandArguments(Protocol.Command.CLIENT).add(Protocol.Keyword.KILL).addParams(params));
      return this.connection.getIntegerReply();
   }

   @Override
   public byte[] clientGetnameBinary() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.GETNAME);
      return this.connection.getBinaryBulkReply();
   }

   @Override
   public byte[] clientListBinary() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.LIST);
      return this.connection.getBinaryBulkReply();
   }

   @Override
   public byte[] clientListBinary(ClientType type) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.LIST.getRaw(), type.getRaw());
      return this.connection.getBinaryBulkReply();
   }

   @Override
   public byte[] clientListBinary(long... clientIds) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, this.clientListParams(clientIds));
      return this.connection.getBinaryBulkReply();
   }

   private byte[][] clientListParams(long... clientIds) {
      byte[][] params = new byte[2 + clientIds.length][];
      int index = 0;
      params[index++] = Protocol.Keyword.LIST.getRaw();
      params[index++] = Protocol.Keyword.ID.getRaw();

      for (long clientId : clientIds) {
         params[index++] = Protocol.toByteArray(clientId);
      }

      return params;
   }

   @Override
   public byte[] clientInfoBinary() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.INFO);
      return this.connection.getBinaryBulkReply();
   }

   @Override
   public String clientSetInfo(ClientAttributeOption attr, byte[] value) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.SETINFO.getRaw(), attr.getRaw(), value);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clientSetname(byte[] name) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.SETNAME.getRaw(), name);
      return this.connection.getBulkReply();
   }

   @Override
   public long clientId() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.ID);
      return this.connection.getIntegerReply();
   }

   @Override
   public long clientUnblock(long clientId) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.UNBLOCK.getRaw(), Protocol.toByteArray(clientId));
      return this.connection.getIntegerReply();
   }

   @Override
   public long clientUnblock(long clientId, UnblockType unblockType) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.UNBLOCK.getRaw(), Protocol.toByteArray(clientId), unblockType.getRaw());
      return this.connection.getIntegerReply();
   }

   @Override
   public String clientPause(long timeout) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.PAUSE.getRaw(), Protocol.toByteArray(timeout));
      return this.connection.getBulkReply();
   }

   @Override
   public String clientPause(long timeout, ClientPauseMode mode) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.PAUSE.getRaw(), Protocol.toByteArray(timeout), mode.getRaw());
      return this.connection.getBulkReply();
   }

   @Override
   public String clientUnpause() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.UNPAUSE);
      return this.connection.getBulkReply();
   }

   @Override
   public String clientNoEvictOn() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, "NO-EVICT", "ON");
      return this.connection.getBulkReply();
   }

   @Override
   public String clientNoEvictOff() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, "NO-EVICT", "OFF");
      return this.connection.getBulkReply();
   }

   @Override
   public String clientNoTouchOn() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, "NO-TOUCH", "ON");
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clientNoTouchOff() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, "NO-TOUCH", "OFF");
      return this.connection.getStatusCodeReply();
   }

   @Override
   public TrackingInfo clientTrackingInfo() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, "TRACKINGINFO");
      return TrackingInfo.TRACKING_INFO_BUILDER.build(this.connection.getOne());
   }

   public List<String> time() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.TIME);
      return this.connection.getMultiBulkReply();
   }

   @Override
   public String migrate(String host, int port, byte[] key, int destinationDb, int timeout) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.migrate(host, port, key, destinationDb, timeout));
   }

   @Override
   public String migrate(String host, int port, int destinationDB, int timeout, MigrateParams params, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.migrate(host, port, destinationDB, timeout, params, keys));
   }

   @Override
   public String migrate(String host, int port, byte[] key, int timeout) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.migrate(host, port, key, timeout));
   }

   @Override
   public String migrate(String host, int port, int timeout, MigrateParams params, byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.migrate(host, port, timeout, params, keys));
   }

   @Override
   public long waitReplicas(int replicas, long timeout) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.WAIT, Protocol.toByteArray(replicas), Protocol.toByteArray(timeout));
      return this.connection.getIntegerReply();
   }

   @Override
   public KeyValue<Long, Long> waitAOF(long numLocal, long numReplicas, long timeout) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.WAITAOF, Protocol.toByteArray(numLocal), Protocol.toByteArray(numReplicas), Protocol.toByteArray(timeout));
      return BuilderFactory.LONG_LONG_PAIR.build(this.connection.getOne());
   }

   @Override
   public long pfadd(byte[] key, byte[]... elements) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pfadd(key, elements));
   }

   @Override
   public long pfcount(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pfcount(key));
   }

   @Override
   public String pfmerge(byte[] destkey, byte[]... sourcekeys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pfmerge(destkey, sourcekeys));
   }

   @Override
   public long pfcount(byte[]... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pfcount(keys));
   }

   @Override
   public ScanResult<byte[]> scan(byte[] cursor) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.scan(cursor));
   }

   @Override
   public ScanResult<byte[]> scan(byte[] cursor, ScanParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.scan(cursor, params));
   }

   @Override
   public ScanResult<byte[]> scan(byte[] cursor, ScanParams params, byte[] type) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.scan(cursor, params, type));
   }

   @Override
   public ScanResult<Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor, ScanParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hscan(key, cursor, params));
   }

   @Override
   public ScanResult<byte[]> hscanNoValues(byte[] key, byte[] cursor, ScanParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hscanNoValues(key, cursor, params));
   }

   @Override
   public ScanResult<byte[]> sscan(byte[] key, byte[] cursor) {
      return this.sscan(key, cursor, new ScanParams());
   }

   @Override
   public ScanResult<byte[]> sscan(byte[] key, byte[] cursor, ScanParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sscan(key, cursor, params));
   }

   @Override
   public ScanResult<Tuple> zscan(byte[] key, byte[] cursor) {
      return this.zscan(key, cursor, new ScanParams());
   }

   @Override
   public ScanResult<Tuple> zscan(byte[] key, byte[] cursor, ScanParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zscan(key, cursor, params));
   }

   @Override
   public long geoadd(byte[] key, double longitude, double latitude, byte[] member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geoadd(key, longitude, latitude, member));
   }

   @Override
   public long geoadd(byte[] key, Map<byte[], GeoCoordinate> memberCoordinateMap) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geoadd(key, memberCoordinateMap));
   }

   @Override
   public long geoadd(byte[] key, GeoAddParams params, Map<byte[], GeoCoordinate> memberCoordinateMap) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geoadd(key, params, memberCoordinateMap));
   }

   @Override
   public Double geodist(byte[] key, byte[] member1, byte[] member2) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geodist(key, member1, member2));
   }

   @Override
   public Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geodist(key, member1, member2, unit));
   }

   @Override
   public List<byte[]> geohash(byte[] key, byte[]... members) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geohash(key, members));
   }

   @Override
   public List<GeoCoordinate> geopos(byte[] key, byte[]... members) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geopos(key, members));
   }

   @Override
   public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadius(key, longitude, latitude, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadiusReadonly(key, longitude, latitude, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadius(key, longitude, latitude, radius, unit, param));
   }

   @Override
   public long georadiusStore(byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadiusStore(key, longitude, latitude, radius, unit, param, storeParam));
   }

   @Override
   public List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadiusReadonly(key, longitude, latitude, radius, unit, param));
   }

   @Override
   public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadiusByMember(key, member, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadiusByMemberReadonly(key, member, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadiusByMember(key, member, radius, unit, param));
   }

   @Override
   public long georadiusByMemberStore(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadiusByMemberStore(key, member, radius, unit, param, storeParam));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(byte[] key, byte[] member, double radius, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearch(key, member, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(byte[] key, GeoCoordinate coord, double radius, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearch(key, coord, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(byte[] key, byte[] member, double width, double height, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearch(key, member, width, height, unit));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(byte[] key, GeoCoordinate coord, double width, double height, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearch(key, coord, width, height, unit));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(byte[] key, GeoSearchParam params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearch(key, params));
   }

   @Override
   public long geosearchStore(byte[] dest, byte[] src, byte[] member, double radius, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearchStore(dest, src, member, radius, unit));
   }

   @Override
   public long geosearchStore(byte[] dest, byte[] src, GeoCoordinate coord, double radius, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearchStore(dest, src, coord, radius, unit));
   }

   @Override
   public long geosearchStore(byte[] dest, byte[] src, byte[] member, double width, double height, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearchStore(dest, src, member, width, height, unit));
   }

   @Override
   public long geosearchStore(byte[] dest, byte[] src, GeoCoordinate coord, double width, double height, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearchStore(dest, src, coord, width, height, unit));
   }

   @Override
   public long geosearchStore(byte[] dest, byte[] src, GeoSearchParam params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearchStore(dest, src, params));
   }

   @Override
   public long geosearchStoreStoreDist(byte[] dest, byte[] src, GeoSearchParam params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearchStoreStoreDist(dest, src, params));
   }

   @Override
   public List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadiusByMemberReadonly(key, member, radius, unit, param));
   }

   @Override
   public List<Long> bitfield(byte[] key, byte[]... arguments) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bitfield(key, arguments));
   }

   @Override
   public List<Long> bitfieldReadonly(byte[] key, byte[]... arguments) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bitfieldReadonly(key, arguments));
   }

   @Override
   public long hstrlen(byte[] key, byte[] field) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hstrlen(key, field));
   }

   @Override
   public List<Long> hexpire(byte[] key, long seconds, byte[]... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hexpire(key, seconds, fields));
   }

   @Override
   public List<Long> hexpire(byte[] key, long seconds, ExpiryOption condition, byte[]... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hexpire(key, seconds, condition, fields));
   }

   @Override
   public List<Long> hpexpire(byte[] key, long milliseconds, byte[]... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hpexpire(key, milliseconds, fields));
   }

   @Override
   public List<Long> hpexpire(byte[] key, long milliseconds, ExpiryOption condition, byte[]... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hpexpire(key, milliseconds, condition, fields));
   }

   @Override
   public List<Long> hexpireAt(byte[] key, long unixTimeSeconds, byte[]... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hexpireAt(key, unixTimeSeconds, fields));
   }

   @Override
   public List<Long> hexpireAt(byte[] key, long unixTimeSeconds, ExpiryOption condition, byte[]... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hexpireAt(key, unixTimeSeconds, condition, fields));
   }

   @Override
   public List<Long> hpexpireAt(byte[] key, long unixTimeMillis, byte[]... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hpexpireAt(key, unixTimeMillis, fields));
   }

   @Override
   public List<Long> hpexpireAt(byte[] key, long unixTimeMillis, ExpiryOption condition, byte[]... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hpexpireAt(key, unixTimeMillis, condition, fields));
   }

   @Override
   public List<Long> hexpireTime(byte[] key, byte[]... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hexpireTime(key, fields));
   }

   @Override
   public List<Long> hpexpireTime(byte[] key, byte[]... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hpexpireTime(key, fields));
   }

   @Override
   public List<Long> httl(byte[] key, byte[]... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.httl(key, fields));
   }

   @Override
   public List<Long> hpttl(byte[] key, byte[]... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hpttl(key, fields));
   }

   @Override
   public List<Long> hpersist(byte[] key, byte[]... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hpersist(key, fields));
   }

   @Override
   public List<Object> xread(XReadParams xReadParams, Entry<byte[], byte[]>... streams) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xread(xReadParams, streams));
   }

   @Override
   public List<Object> xreadGroup(byte[] groupName, byte[] consumer, XReadGroupParams xReadGroupParams, Entry<byte[], byte[]>... streams) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xreadGroup(groupName, consumer, xReadGroupParams, streams));
   }

   @Override
   public byte[] xadd(byte[] key, XAddParams params, Map<byte[], byte[]> hash) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xadd(key, params, hash));
   }

   @Override
   public long xlen(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xlen(key));
   }

   @Override
   public List<Object> xrange(byte[] key, byte[] start, byte[] end) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xrange(key, start, end));
   }

   @Override
   public List<Object> xrange(byte[] key, byte[] start, byte[] end, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xrange(key, start, end, count));
   }

   @Override
   public List<Object> xrevrange(byte[] key, byte[] end, byte[] start) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xrevrange(key, end, start));
   }

   @Override
   public List<Object> xrevrange(byte[] key, byte[] end, byte[] start, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xrevrange(key, end, start, count));
   }

   @Override
   public long xack(byte[] key, byte[] group, byte[]... ids) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xack(key, group, ids));
   }

   @Override
   public String xgroupCreate(byte[] key, byte[] consumer, byte[] id, boolean makeStream) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xgroupCreate(key, consumer, id, makeStream));
   }

   @Override
   public String xgroupSetID(byte[] key, byte[] consumer, byte[] id) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xgroupSetID(key, consumer, id));
   }

   @Override
   public long xgroupDestroy(byte[] key, byte[] consumer) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xgroupDestroy(key, consumer));
   }

   @Override
   public boolean xgroupCreateConsumer(byte[] key, byte[] groupName, byte[] consumerName) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xgroupCreateConsumer(key, groupName, consumerName));
   }

   @Override
   public long xgroupDelConsumer(byte[] key, byte[] groupName, byte[] consumerName) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xgroupDelConsumer(key, groupName, consumerName));
   }

   @Override
   public long xdel(byte[] key, byte[]... ids) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xdel(key, ids));
   }

   @Override
   public long xtrim(byte[] key, long maxLen, boolean approximateLength) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xtrim(key, maxLen, approximateLength));
   }

   @Override
   public long xtrim(byte[] key, XTrimParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xtrim(key, params));
   }

   @Override
   public Object xpending(byte[] key, byte[] groupName) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xpending(key, groupName));
   }

   @Override
   public List<Object> xpending(byte[] key, byte[] groupName, XPendingParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xpending(key, groupName, params));
   }

   @Override
   public List<byte[]> xclaim(byte[] key, byte[] group, byte[] consumerName, long minIdleTime, XClaimParams params, byte[]... ids) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xclaim(key, group, consumerName, minIdleTime, params, ids));
   }

   @Override
   public List<byte[]> xclaimJustId(byte[] key, byte[] group, byte[] consumerName, long minIdleTime, XClaimParams params, byte[]... ids) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xclaimJustId(key, group, consumerName, minIdleTime, params, ids));
   }

   @Override
   public List<Object> xautoclaim(byte[] key, byte[] groupName, byte[] consumerName, long minIdleTime, byte[] start, XAutoClaimParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xautoclaim(key, groupName, consumerName, minIdleTime, start, params));
   }

   @Override
   public List<Object> xautoclaimJustId(byte[] key, byte[] groupName, byte[] consumerName, long minIdleTime, byte[] start, XAutoClaimParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xautoclaimJustId(key, groupName, consumerName, minIdleTime, start, params));
   }

   @Override
   public Object xinfoStream(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xinfoStream(key));
   }

   @Override
   public Object xinfoStreamFull(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xinfoStreamFull(key));
   }

   @Override
   public Object xinfoStreamFull(byte[] key, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xinfoStreamFull(key, count));
   }

   @Override
   public List<Object> xinfoGroups(byte[] key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xinfoGroups(key));
   }

   @Override
   public List<Object> xinfoConsumers(byte[] key, byte[] group) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xinfoConsumers(key, group));
   }

   public Object sendCommand(ProtocolCommand cmd, byte[]... args) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(cmd, args);
      return this.connection.getOne();
   }

   public Object sendBlockingCommand(ProtocolCommand cmd, byte[]... args) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(cmd, args);
      this.connection.setTimeoutInfinite();

      try {
         return this.connection.getOne();
      } finally {
         this.connection.rollbackTimeout();
      }
   }

   public Object sendCommand(ProtocolCommand cmd) {
      return this.sendCommand(cmd, DUMMY_ARRAY);
   }

   @Override
   public boolean copy(String srcKey, String dstKey, int db, boolean replace) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.copy(srcKey, dstKey, db, replace));
   }

   @Override
   public boolean copy(String srcKey, String dstKey, boolean replace) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.copy(srcKey, dstKey, replace));
   }

   @Override
   public String ping(String message) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.PING, message);
      return this.connection.getBulkReply();
   }

   @Override
   public String set(String key, String value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.set(key, value));
   }

   @Override
   public String set(String key, String value, SetParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.set(key, value, params));
   }

   @Override
   public String get(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.get(key));
   }

   @Override
   public String setGet(String key, String value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.setGet(key, value));
   }

   @Override
   public String setGet(String key, String value, SetParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.setGet(key, value, params));
   }

   @Override
   public String getDel(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.getDel(key));
   }

   @Override
   public String getEx(String key, GetExParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.getEx(key, params));
   }

   @Override
   public long exists(String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.exists(keys));
   }

   @Override
   public boolean exists(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.exists(key));
   }

   @Override
   public long del(String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.del(keys));
   }

   @Override
   public long del(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.del(key));
   }

   @Override
   public long unlink(String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.unlink(keys));
   }

   @Override
   public long unlink(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.unlink(key));
   }

   @Override
   public String type(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.type(key));
   }

   @Override
   public Set<String> keys(String pattern) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.keys(pattern));
   }

   @Override
   public String randomKey() {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.randomKey());
   }

   @Override
   public String rename(String oldkey, String newkey) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.rename(oldkey, newkey));
   }

   @Override
   public long renamenx(String oldkey, String newkey) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.renamenx(oldkey, newkey));
   }

   @Override
   public long expire(String key, long seconds) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.expire(key, seconds));
   }

   @Override
   public long expire(String key, long seconds, ExpiryOption expiryOption) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.expire(key, seconds, expiryOption));
   }

   @Override
   public long pexpire(String key, long milliseconds) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pexpire(key, milliseconds));
   }

   @Override
   public long pexpire(String key, long milliseconds, ExpiryOption expiryOption) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pexpire(key, milliseconds, expiryOption));
   }

   @Override
   public long expireTime(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.expireTime(key));
   }

   @Override
   public long pexpireTime(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pexpireTime(key));
   }

   @Override
   public long expireAt(String key, long unixTime) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.expireAt(key, unixTime));
   }

   @Override
   public long expireAt(String key, long unixTime, ExpiryOption expiryOption) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.expireAt(key, unixTime, expiryOption));
   }

   @Override
   public long pexpireAt(String key, long millisecondsTimestamp) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pexpireAt(key, millisecondsTimestamp));
   }

   @Override
   public long pexpireAt(String key, long millisecondsTimestamp, ExpiryOption expiryOption) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pexpireAt(key, millisecondsTimestamp, expiryOption));
   }

   @Override
   public long ttl(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.ttl(key));
   }

   @Override
   public long touch(String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.touch(keys));
   }

   @Override
   public long touch(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.touch(key));
   }

   @Override
   public long move(String key, int dbIndex) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.MOVE, SafeEncoder.encode(key), Protocol.toByteArray(dbIndex));
      return this.connection.getIntegerReply();
   }

   @Deprecated
   @Override
   public String getSet(String key, String value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.getSet(key, value));
   }

   @Override
   public List<String> mget(String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.mget(keys));
   }

   @Override
   public long setnx(String key, String value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.setnx(key, value));
   }

   @Override
   public String setex(String key, long seconds, String value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.setex(key, seconds, value));
   }

   @Override
   public String mset(String... keysvalues) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.mset(keysvalues));
   }

   @Override
   public long msetnx(String... keysvalues) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.msetnx(keysvalues));
   }

   @Override
   public long decrBy(String key, long decrement) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.decrBy(key, decrement));
   }

   @Override
   public long decr(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.decr(key));
   }

   @Override
   public long incrBy(String key, long increment) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.incrBy(key, increment));
   }

   @Override
   public double incrByFloat(String key, double increment) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.incrByFloat(key, increment));
   }

   @Override
   public long incr(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.incr(key));
   }

   @Override
   public long append(String key, String value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.append(key, value));
   }

   @Override
   public String substr(String key, int start, int end) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.substr(key, start, end));
   }

   @Override
   public long hset(String key, String field, String value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hset(key, field, value));
   }

   @Override
   public long hset(String key, Map<String, String> hash) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hset(key, hash));
   }

   @Override
   public String hget(String key, String field) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hget(key, field));
   }

   @Override
   public long hsetnx(String key, String field, String value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hsetnx(key, field, value));
   }

   @Override
   public String hmset(String key, Map<String, String> hash) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hmset(key, hash));
   }

   @Override
   public List<String> hmget(String key, String... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hmget(key, fields));
   }

   @Override
   public long hincrBy(String key, String field, long value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hincrBy(key, field, value));
   }

   @Override
   public double hincrByFloat(String key, String field, double value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hincrByFloat(key, field, value));
   }

   @Override
   public boolean hexists(String key, String field) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hexists(key, field));
   }

   @Override
   public long hdel(String key, String... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hdel(key, fields));
   }

   @Override
   public long hlen(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hlen(key));
   }

   @Override
   public Set<String> hkeys(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hkeys(key));
   }

   @Override
   public List<String> hvals(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hvals(key));
   }

   @Override
   public Map<String, String> hgetAll(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hgetAll(key));
   }

   @Override
   public String hrandfield(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hrandfield(key));
   }

   @Override
   public List<String> hrandfield(String key, long count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hrandfield(key, count));
   }

   @Override
   public List<Entry<String, String>> hrandfieldWithValues(String key, long count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hrandfieldWithValues(key, count));
   }

   @Override
   public long rpush(String key, String... strings) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.rpush(key, strings));
   }

   @Override
   public long lpush(String key, String... strings) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lpush(key, strings));
   }

   @Override
   public long llen(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.llen(key));
   }

   @Override
   public List<String> lrange(String key, long start, long stop) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lrange(key, start, stop));
   }

   @Override
   public String ltrim(String key, long start, long stop) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.ltrim(key, start, stop));
   }

   @Override
   public String lindex(String key, long index) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lindex(key, index));
   }

   @Override
   public String lset(String key, long index, String value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lset(key, index, value));
   }

   @Override
   public long lrem(String key, long count, String value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lrem(key, count, value));
   }

   @Override
   public String lpop(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lpop(key));
   }

   @Override
   public List<String> lpop(String key, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lpop(key, count));
   }

   @Override
   public Long lpos(String key, String element) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lpos(key, element));
   }

   @Override
   public Long lpos(String key, String element, LPosParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lpos(key, element, params));
   }

   @Override
   public List<Long> lpos(String key, String element, LPosParams params, long count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lpos(key, element, params, count));
   }

   @Override
   public String rpop(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.rpop(key));
   }

   @Override
   public List<String> rpop(String key, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.rpop(key, count));
   }

   @Override
   public String rpoplpush(String srckey, String dstkey) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.rpoplpush(srckey, dstkey));
   }

   @Override
   public long sadd(String key, String... members) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sadd(key, members));
   }

   @Override
   public Set<String> smembers(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.smembers(key));
   }

   @Override
   public long srem(String key, String... members) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.srem(key, members));
   }

   @Override
   public String spop(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.spop(key));
   }

   @Override
   public Set<String> spop(String key, long count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.spop(key, count));
   }

   @Override
   public long smove(String srckey, String dstkey, String member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.smove(srckey, dstkey, member));
   }

   @Override
   public long scard(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.scard(key));
   }

   @Override
   public boolean sismember(String key, String member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sismember(key, member));
   }

   @Override
   public List<Boolean> smismember(String key, String... members) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.smismember(key, members));
   }

   @Override
   public Set<String> sinter(String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sinter(keys));
   }

   @Override
   public long sinterstore(String dstkey, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sinterstore(dstkey, keys));
   }

   @Override
   public long sintercard(String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sintercard(keys));
   }

   @Override
   public long sintercard(int limit, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sintercard(limit, keys));
   }

   @Override
   public Set<String> sunion(String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sunion(keys));
   }

   @Override
   public long sunionstore(String dstkey, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sunionstore(dstkey, keys));
   }

   @Override
   public Set<String> sdiff(String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sdiff(keys));
   }

   @Override
   public long sdiffstore(String dstkey, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sdiffstore(dstkey, keys));
   }

   @Override
   public String srandmember(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.srandmember(key));
   }

   @Override
   public List<String> srandmember(String key, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.srandmember(key, count));
   }

   @Override
   public long zadd(String key, double score, String member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zadd(key, score, member));
   }

   @Override
   public long zadd(String key, double score, String member, ZAddParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zadd(key, score, member, params));
   }

   @Override
   public long zadd(String key, Map<String, Double> scoreMembers) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zadd(key, scoreMembers));
   }

   @Override
   public long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zadd(key, scoreMembers, params));
   }

   @Override
   public Double zaddIncr(String key, double score, String member, ZAddParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zaddIncr(key, score, member, params));
   }

   @Override
   public List<String> zdiff(String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zdiff(keys));
   }

   @Override
   public List<Tuple> zdiffWithScores(String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zdiffWithScores(keys));
   }

   @Deprecated
   @Override
   public long zdiffStore(String dstkey, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zdiffStore(dstkey, keys));
   }

   @Override
   public long zdiffstore(String dstkey, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zdiffstore(dstkey, keys));
   }

   @Override
   public List<String> zrange(String key, long start, long stop) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrange(key, start, stop));
   }

   @Override
   public long zrem(String key, String... members) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrem(key, members));
   }

   @Override
   public double zincrby(String key, double increment, String member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zincrby(key, increment, member));
   }

   @Override
   public Double zincrby(String key, double increment, String member, ZIncrByParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zincrby(key, increment, member, params));
   }

   @Override
   public Long zrank(String key, String member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrank(key, member));
   }

   @Override
   public Long zrevrank(String key, String member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrank(key, member));
   }

   @Override
   public KeyValue<Long, Double> zrankWithScore(String key, String member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrankWithScore(key, member));
   }

   @Override
   public KeyValue<Long, Double> zrevrankWithScore(String key, String member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrankWithScore(key, member));
   }

   @Override
   public List<String> zrevrange(String key, long start, long stop) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrange(key, start, stop));
   }

   @Override
   public List<Tuple> zrangeWithScores(String key, long start, long stop) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeWithScores(key, start, stop));
   }

   @Override
   public List<Tuple> zrevrangeWithScores(String key, long start, long stop) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeWithScores(key, start, stop));
   }

   @Override
   public List<String> zrange(String key, ZRangeParams zRangeParams) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrange(key, zRangeParams));
   }

   @Override
   public List<Tuple> zrangeWithScores(String key, ZRangeParams zRangeParams) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeWithScores(key, zRangeParams));
   }

   @Override
   public long zrangestore(String dest, String src, ZRangeParams zRangeParams) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangestore(dest, src, zRangeParams));
   }

   @Override
   public String zrandmember(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrandmember(key));
   }

   @Override
   public List<String> zrandmember(String key, long count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrandmember(key, count));
   }

   @Override
   public List<Tuple> zrandmemberWithScores(String key, long count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrandmemberWithScores(key, count));
   }

   @Override
   public long zcard(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zcard(key));
   }

   @Override
   public Double zscore(String key, String member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zscore(key, member));
   }

   @Override
   public List<Double> zmscore(String key, String... members) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zmscore(key, members));
   }

   @Override
   public Tuple zpopmax(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zpopmax(key));
   }

   @Override
   public List<Tuple> zpopmax(String key, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zpopmax(key, count));
   }

   @Override
   public Tuple zpopmin(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zpopmin(key));
   }

   @Override
   public List<Tuple> zpopmin(String key, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zpopmin(key, count));
   }

   public String watch(String... keys) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.WATCH, keys);
      String status = this.connection.getStatusCodeReply();
      this.isInWatch = true;
      return status;
   }

   @Override
   public List<String> sort(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sort(key));
   }

   @Override
   public List<String> sort(String key, SortingParams sortingParams) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sort(key, sortingParams));
   }

   @Override
   public long sort(String key, SortingParams sortingParams, String dstkey) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sort(key, sortingParams, dstkey));
   }

   @Override
   public List<String> sortReadonly(String key, SortingParams sortingParams) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sortReadonly(key, sortingParams));
   }

   @Override
   public long sort(String key, String dstkey) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sort(key, dstkey));
   }

   @Override
   public String lmove(String srcKey, String dstKey, ListDirection from, ListDirection to) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lmove(srcKey, dstKey, from, to));
   }

   @Override
   public String blmove(String srcKey, String dstKey, ListDirection from, ListDirection to, double timeout) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.blmove(srcKey, dstKey, from, to, timeout));
   }

   @Override
   public List<String> blpop(int timeout, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.blpop(timeout, keys));
   }

   @Override
   public KeyValue<String, String> blpop(double timeout, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.blpop(timeout, keys));
   }

   @Override
   public List<String> brpop(int timeout, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.brpop(timeout, keys));
   }

   @Override
   public KeyValue<String, String> brpop(double timeout, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.brpop(timeout, keys));
   }

   @Override
   public KeyValue<String, List<String>> lmpop(ListDirection direction, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lmpop(direction, keys));
   }

   @Override
   public KeyValue<String, List<String>> lmpop(ListDirection direction, int count, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lmpop(direction, count, keys));
   }

   @Override
   public KeyValue<String, List<String>> blmpop(double timeout, ListDirection direction, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.blmpop(timeout, direction, keys));
   }

   @Override
   public KeyValue<String, List<String>> blmpop(double timeout, ListDirection direction, int count, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.blmpop(timeout, direction, count, keys));
   }

   @Override
   public KeyValue<String, Tuple> bzpopmax(double timeout, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bzpopmax(timeout, keys));
   }

   @Override
   public KeyValue<String, Tuple> bzpopmin(double timeout, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bzpopmin(timeout, keys));
   }

   @Override
   public List<String> blpop(int timeout, String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.blpop(timeout, key));
   }

   @Override
   public KeyValue<String, String> blpop(double timeout, String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.blpop(timeout, key));
   }

   @Override
   public List<String> brpop(int timeout, String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.brpop(timeout, key));
   }

   @Override
   public KeyValue<String, String> brpop(double timeout, String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.brpop(timeout, key));
   }

   @Override
   public long zcount(String key, double min, double max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zcount(key, min, max));
   }

   @Override
   public long zcount(String key, String min, String max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zcount(key, min, max));
   }

   @Override
   public List<String> zrangeByScore(String key, double min, double max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByScore(key, min, max));
   }

   @Override
   public List<String> zrangeByScore(String key, String min, String max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByScore(key, min, max));
   }

   @Override
   public List<String> zrangeByScore(String key, double min, double max, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByScore(key, min, max, offset, count));
   }

   @Override
   public List<String> zrangeByScore(String key, String min, String max, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByScore(key, min, max, offset, count));
   }

   @Override
   public List<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max));
   }

   @Override
   public List<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max));
   }

   @Override
   public List<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max, offset, count));
   }

   @Override
   public List<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByScoreWithScores(key, min, max, offset, count));
   }

   @Override
   public List<String> zrevrangeByScore(String key, double max, double min) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByScore(key, max, min));
   }

   @Override
   public List<String> zrevrangeByScore(String key, String max, String min) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByScore(key, max, min));
   }

   @Override
   public List<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByScore(key, max, min, offset, count));
   }

   @Override
   public List<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min));
   }

   @Override
   public List<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min, offset, count));
   }

   @Override
   public List<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min, offset, count));
   }

   @Override
   public List<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByScore(key, max, min, offset, count));
   }

   @Override
   public List<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByScoreWithScores(key, max, min));
   }

   @Override
   public long zremrangeByRank(String key, long start, long stop) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zremrangeByRank(key, start, stop));
   }

   @Override
   public long zremrangeByScore(String key, double min, double max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zremrangeByScore(key, min, max));
   }

   @Override
   public long zremrangeByScore(String key, String min, String max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zremrangeByScore(key, min, max));
   }

   @Override
   public List<String> zunion(ZParams params, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zunion(params, keys));
   }

   @Override
   public List<Tuple> zunionWithScores(ZParams params, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zunionWithScores(params, keys));
   }

   @Override
   public long zunionstore(String dstkey, String... sets) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zunionstore(dstkey, sets));
   }

   @Override
   public long zunionstore(String dstkey, ZParams params, String... sets) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zunionstore(dstkey, params, sets));
   }

   @Override
   public List<String> zinter(ZParams params, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zinter(params, keys));
   }

   @Override
   public List<Tuple> zinterWithScores(ZParams params, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zinterWithScores(params, keys));
   }

   @Override
   public long zintercard(String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zintercard(keys));
   }

   @Override
   public long zintercard(long limit, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zintercard(limit, keys));
   }

   @Override
   public long zinterstore(String dstkey, String... sets) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zinterstore(dstkey, sets));
   }

   @Override
   public long zinterstore(String dstkey, ZParams params, String... sets) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zinterstore(dstkey, params, sets));
   }

   @Override
   public long zlexcount(String key, String min, String max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zlexcount(key, min, max));
   }

   @Override
   public List<String> zrangeByLex(String key, String min, String max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByLex(key, min, max));
   }

   @Override
   public List<String> zrangeByLex(String key, String min, String max, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrangeByLex(key, min, max, offset, count));
   }

   @Override
   public List<String> zrevrangeByLex(String key, String max, String min) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByLex(key, max, min));
   }

   @Override
   public List<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zrevrangeByLex(key, max, min, offset, count));
   }

   @Override
   public long zremrangeByLex(String key, String min, String max) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zremrangeByLex(key, min, max));
   }

   @Override
   public KeyValue<String, List<Tuple>> zmpop(SortedSetOption option, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zmpop(option, keys));
   }

   @Override
   public KeyValue<String, List<Tuple>> zmpop(SortedSetOption option, int count, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zmpop(option, count, keys));
   }

   @Override
   public KeyValue<String, List<Tuple>> bzmpop(double timeout, SortedSetOption option, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bzmpop(timeout, option, keys));
   }

   @Override
   public KeyValue<String, List<Tuple>> bzmpop(double timeout, SortedSetOption option, int count, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bzmpop(timeout, option, count, keys));
   }

   @Override
   public long strlen(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.strlen(key));
   }

   @Override
   public LCSMatchResult lcs(String keyA, String keyB, LCSParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lcs(keyA, keyB, params));
   }

   @Override
   public long lpushx(String key, String... strings) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.lpushx(key, strings));
   }

   @Override
   public long persist(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.persist(key));
   }

   @Override
   public long rpushx(String key, String... strings) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.rpushx(key, strings));
   }

   @Override
   public String echo(String string) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ECHO, string);
      return this.connection.getBulkReply();
   }

   @Override
   public long linsert(String key, ListPosition where, String pivot, String value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.linsert(key, where, pivot, value));
   }

   @Override
   public String brpoplpush(String source, String destination, int timeout) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.brpoplpush(source, destination, timeout));
   }

   @Override
   public boolean setbit(String key, long offset, boolean value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.setbit(key, offset, value));
   }

   @Override
   public boolean getbit(String key, long offset) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.getbit(key, offset));
   }

   @Override
   public long setrange(String key, long offset, String value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.setrange(key, offset, value));
   }

   @Override
   public String getrange(String key, long startOffset, long endOffset) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.getrange(key, startOffset, endOffset));
   }

   @Override
   public long bitpos(String key, boolean value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bitpos(key, value));
   }

   @Override
   public long bitpos(String key, boolean value, BitPosParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bitpos(key, value, params));
   }

   @Override
   public List<Object> role() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ROLE);
      return BuilderFactory.ENCODED_OBJECT_LIST.build(this.connection.getOne());
   }

   @Override
   public Map<String, String> configGet(String pattern) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CONFIG, Protocol.Keyword.GET.name(), pattern);
      return BuilderFactory.STRING_MAP.build(this.connection.getOne());
   }

   @Override
   public Map<String, String> configGet(String... patterns) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CONFIG, joinParameters(Protocol.Keyword.GET.name(), patterns));
      return BuilderFactory.STRING_MAP.build(this.connection.getOne());
   }

   @Override
   public String configSet(String parameter, String value) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CONFIG, Protocol.Keyword.SET.name(), parameter, value);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String configSet(String... parameterValues) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CONFIG, joinParameters(Protocol.Keyword.SET.name(), parameterValues));
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String configSet(Map<String, String> parameterValues) {
      this.checkIsInMultiOrPipeline();
      CommandArguments args = new CommandArguments(Protocol.Command.CONFIG).add(Protocol.Keyword.SET);
      parameterValues.forEach((k, v) -> args.add(k).add(v));
      this.connection.sendCommand(args);
      return this.connection.getStatusCodeReply();
   }

   public long publish(String channel, String message) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.PUBLISH, channel, message);
      return this.connection.getIntegerReply();
   }

   public void subscribe(JedisPubSub jedisPubSub, String... channels) {
      jedisPubSub.proceed(this.connection, channels);
   }

   public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
      jedisPubSub.proceedWithPatterns(this.connection, patterns);
   }

   public List<String> pubsubChannels() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.PUBSUB, Protocol.Keyword.CHANNELS);
      return this.connection.getMultiBulkReply();
   }

   public List<String> pubsubChannels(String pattern) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.PUBSUB, Protocol.Keyword.CHANNELS.name(), pattern);
      return this.connection.getMultiBulkReply();
   }

   public Long pubsubNumPat() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.PUBSUB, Protocol.Keyword.NUMPAT);
      return this.connection.getIntegerReply();
   }

   public Map<String, Long> pubsubNumSub(String... channels) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.PUBSUB, joinParameters(Protocol.Keyword.NUMSUB.name(), channels));
      return BuilderFactory.PUBSUB_NUMSUB_MAP.build(this.connection.getOne());
   }

   public List<String> pubsubShardChannels() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.PUBSUB, Protocol.Keyword.SHARDCHANNELS);
      return this.connection.getMultiBulkReply();
   }

   public List<String> pubsubShardChannels(String pattern) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.PUBSUB, Protocol.Keyword.SHARDCHANNELS.name(), pattern);
      return this.connection.getMultiBulkReply();
   }

   public Map<String, Long> pubsubShardNumSub(String... channels) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.PUBSUB, joinParameters(Protocol.Keyword.SHARDNUMSUB.name(), channels));
      return BuilderFactory.PUBSUB_NUMSUB_MAP.build(this.connection.getOne());
   }

   @Override
   public Object eval(String script, int keyCount, String... params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.eval(script, keyCount, params));
   }

   @Override
   public Object eval(String script, List<String> keys, List<String> args) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.eval(script, keys, args));
   }

   @Override
   public Object evalReadonly(String script, List<String> keys, List<String> args) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.evalReadonly(script, keys, args));
   }

   @Override
   public Object eval(String script) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.eval(script));
   }

   @Override
   public Object evalsha(String sha1) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.evalsha(sha1));
   }

   @Override
   public Object evalsha(String sha1, List<String> keys, List<String> args) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.evalsha(sha1, keys, args));
   }

   @Override
   public Object evalshaReadonly(String sha1, List<String> keys, List<String> args) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.evalshaReadonly(sha1, keys, args));
   }

   @Override
   public Object evalsha(String sha1, int keyCount, String... params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.evalsha(sha1, keyCount, params));
   }

   @Override
   public Boolean scriptExists(String sha1) {
      String[] a = new String[]{sha1};
      return this.scriptExists(a).get(0);
   }

   @Override
   public List<Boolean> scriptExists(String... sha1) {
      this.connection.sendCommand(Protocol.Command.SCRIPT, joinParameters(Protocol.Keyword.EXISTS.name(), sha1));
      return BuilderFactory.BOOLEAN_LIST.build(this.connection.getOne());
   }

   @Override
   public String scriptLoad(String script) {
      this.connection.sendCommand(Protocol.Command.SCRIPT, Protocol.Keyword.LOAD.name(), script);
      return this.connection.getBulkReply();
   }

   @Override
   public List<Slowlog> slowlogGet() {
      this.connection.sendCommand(Protocol.Command.SLOWLOG, Protocol.Keyword.GET);
      return Slowlog.from(this.connection.getObjectMultiBulkReply());
   }

   @Override
   public List<Slowlog> slowlogGet(long entries) {
      this.connection.sendCommand(Protocol.Command.SLOWLOG, Protocol.Keyword.GET.getRaw(), Protocol.toByteArray(entries));
      return Slowlog.from(this.connection.getObjectMultiBulkReply());
   }

   @Override
   public Long objectRefcount(String key) {
      this.connection.sendCommand(Protocol.Command.OBJECT, Protocol.Keyword.REFCOUNT.name(), key);
      return this.connection.getIntegerReply();
   }

   @Override
   public String objectEncoding(String key) {
      this.connection.sendCommand(Protocol.Command.OBJECT, Protocol.Keyword.ENCODING.name(), key);
      return this.connection.getBulkReply();
   }

   @Override
   public Long objectIdletime(String key) {
      this.connection.sendCommand(Protocol.Command.OBJECT, Protocol.Keyword.IDLETIME.name(), key);
      return this.connection.getIntegerReply();
   }

   @Override
   public List<String> objectHelp() {
      this.connection.sendCommand(Protocol.Command.OBJECT, Protocol.Keyword.HELP);
      return this.connection.getMultiBulkReply();
   }

   @Override
   public Long objectFreq(String key) {
      this.connection.sendCommand(Protocol.Command.OBJECT, Protocol.Keyword.FREQ.name(), key);
      return this.connection.getIntegerReply();
   }

   @Override
   public long bitcount(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bitcount(key));
   }

   @Override
   public long bitcount(String key, long start, long end) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bitcount(key, start, end));
   }

   @Override
   public long bitcount(String key, long start, long end, BitCountOption option) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bitcount(key, start, end, option));
   }

   @Override
   public long bitop(BitOP op, String destKey, String... srcKeys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bitop(op, destKey, srcKeys));
   }

   public long commandCount() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.COMMAND, Protocol.Keyword.COUNT);
      return this.connection.getIntegerReply();
   }

   public Map<String, CommandDocument> commandDocs(String... commands) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.COMMAND, joinParameters(Protocol.Keyword.DOCS.name(), commands));
      return BuilderFactory.COMMAND_DOCS_RESPONSE.build(this.connection.getOne());
   }

   public List<String> commandGetKeys(String... command) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.COMMAND, joinParameters(Protocol.Keyword.GETKEYS.name(), command));
      return BuilderFactory.STRING_LIST.build(this.connection.getOne());
   }

   public List<KeyValue<String, List<String>>> commandGetKeysAndFlags(String... command) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.COMMAND, joinParameters(Protocol.Keyword.GETKEYSANDFLAGS.name(), command));
      return BuilderFactory.KEYED_STRING_LIST_LIST.build(this.connection.getOne());
   }

   public Map<String, CommandInfo> commandInfo(String... commands) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.COMMAND, joinParameters(Protocol.Keyword.INFO.name(), commands));
      return BuilderFactory.COMMAND_INFO_RESPONSE.build(this.connection.getOne());
   }

   public List<String> commandList() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.COMMAND, Protocol.Keyword.LIST);
      return BuilderFactory.STRING_LIST.build(this.connection.getOne());
   }

   public List<String> commandListFilterBy(CommandListFilterByParams filterByParams) {
      this.checkIsInMultiOrPipeline();
      CommandArguments args = new CommandArguments(Protocol.Command.COMMAND).add(Protocol.Keyword.LIST).addParams(filterByParams);
      this.connection.sendCommand(args);
      return BuilderFactory.STRING_LIST.build(this.connection.getOne());
   }

   @Override
   public String sentinelMyId() {
      this.connection.sendCommand(Protocol.Command.SENTINEL, Protocol.SentinelKeyword.MYID);
      return this.connection.getBulkReply();
   }

   @Override
   public List<Map<String, String>> sentinelMasters() {
      this.connection.sendCommand(Protocol.Command.SENTINEL, Protocol.SentinelKeyword.MASTERS);
      return this.connection.getObjectMultiBulkReply().stream().map(BuilderFactory.STRING_MAP::build).collect(Collectors.toList());
   }

   @Override
   public Map<String, String> sentinelMaster(String masterName) {
      this.connection.sendCommand(Protocol.Command.SENTINEL, Protocol.SentinelKeyword.MASTER.name(), masterName);
      return BuilderFactory.STRING_MAP.build(this.connection.getOne());
   }

   @Override
   public List<Map<String, String>> sentinelSentinels(String masterName) {
      this.connection.sendCommand(Protocol.Command.SENTINEL, Protocol.SentinelKeyword.SENTINELS.name(), masterName);
      return this.connection.getObjectMultiBulkReply().stream().map(BuilderFactory.STRING_MAP::build).collect(Collectors.toList());
   }

   @Override
   public List<String> sentinelGetMasterAddrByName(String masterName) {
      this.connection.sendCommand(Protocol.Command.SENTINEL, Protocol.SentinelKeyword.GET_MASTER_ADDR_BY_NAME.getRaw(), SafeEncoder.encode(masterName));
      return this.connection.getMultiBulkReply();
   }

   @Override
   public Long sentinelReset(String pattern) {
      this.connection.sendCommand(Protocol.Command.SENTINEL, Protocol.SentinelKeyword.RESET.name(), pattern);
      return this.connection.getIntegerReply();
   }

   @Deprecated
   @Override
   public List<Map<String, String>> sentinelSlaves(String masterName) {
      this.connection.sendCommand(Protocol.Command.SENTINEL, Protocol.SentinelKeyword.SLAVES.name(), masterName);
      return this.connection.getObjectMultiBulkReply().stream().map(BuilderFactory.STRING_MAP::build).collect(Collectors.toList());
   }

   @Override
   public List<Map<String, String>> sentinelReplicas(String masterName) {
      this.connection.sendCommand(Protocol.Command.SENTINEL, Protocol.SentinelKeyword.REPLICAS.name(), masterName);
      return this.connection.getObjectMultiBulkReply().stream().map(BuilderFactory.STRING_MAP::build).collect(Collectors.toList());
   }

   @Override
   public String sentinelFailover(String masterName) {
      this.connection.sendCommand(Protocol.Command.SENTINEL, Protocol.SentinelKeyword.FAILOVER.name(), masterName);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String sentinelMonitor(String masterName, String ip, int port, int quorum) {
      CommandArguments args = new CommandArguments(Protocol.Command.SENTINEL)
         .add(Protocol.SentinelKeyword.MONITOR)
         .add(masterName)
         .add(ip)
         .add(port)
         .add(quorum);
      this.connection.sendCommand(args);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String sentinelRemove(String masterName) {
      this.connection.sendCommand(Protocol.Command.SENTINEL, Protocol.SentinelKeyword.REMOVE.name(), masterName);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String sentinelSet(String masterName, Map<String, String> parameterMap) {
      CommandArguments args = new CommandArguments(Protocol.Command.SENTINEL).add(Protocol.SentinelKeyword.SET).add(masterName);
      parameterMap.entrySet().forEach(entry -> args.add(entry.getKey()).add(entry.getValue()));
      this.connection.sendCommand(args);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public byte[] dump(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.dump(key));
   }

   @Override
   public String restore(String key, long ttl, byte[] serializedValue) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.restore(key, ttl, serializedValue));
   }

   @Override
   public String restore(String key, long ttl, byte[] serializedValue, RestoreParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.restore(key, ttl, serializedValue, params));
   }

   @Override
   public long pttl(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pttl(key));
   }

   @Override
   public String psetex(String key, long milliseconds, String value) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.psetex(key, milliseconds, value));
   }

   @Override
   public String aclSetUser(String name) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.SETUSER.name(), name);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String aclSetUser(String name, String... rules) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, joinParameters(Protocol.Keyword.SETUSER.name(), name, rules));
      return this.connection.getStatusCodeReply();
   }

   @Override
   public long aclDelUser(String... names) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, joinParameters(Protocol.Keyword.DELUSER.name(), names));
      return this.connection.getIntegerReply();
   }

   @Override
   public AccessControlUser aclGetUser(String name) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.GETUSER.name(), name);
      return BuilderFactory.ACCESS_CONTROL_USER.build(this.connection.getOne());
   }

   @Override
   public List<String> aclUsers() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.USERS);
      return BuilderFactory.STRING_LIST.build(this.connection.getObjectMultiBulkReply());
   }

   @Override
   public List<String> aclList() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.LIST);
      return this.connection.getMultiBulkReply();
   }

   @Override
   public String aclWhoAmI() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.WHOAMI);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public List<String> aclCat() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.CAT);
      return BuilderFactory.STRING_LIST.build(this.connection.getOne());
   }

   @Override
   public List<String> aclCat(String category) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.CAT.name(), category);
      return BuilderFactory.STRING_LIST.build(this.connection.getOne());
   }

   @Override
   public List<AccessControlLogEntry> aclLog() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.LOG);
      return BuilderFactory.ACCESS_CONTROL_LOG_ENTRY_LIST.build(this.connection.getOne());
   }

   @Override
   public List<AccessControlLogEntry> aclLog(int limit) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.LOG.getRaw(), Protocol.toByteArray(limit));
      return BuilderFactory.ACCESS_CONTROL_LOG_ENTRY_LIST.build(this.connection.getOne());
   }

   @Override
   public String aclLoad() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.LOAD);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String aclSave() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.SAVE);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String aclGenPass() {
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.GENPASS);
      return this.connection.getBulkReply();
   }

   @Override
   public String aclGenPass(int bits) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ACL, Protocol.Keyword.GENPASS.getRaw(), Protocol.toByteArray(bits));
      return this.connection.getBulkReply();
   }

   @Override
   public String aclDryRun(String username, String command, String... args) {
      this.checkIsInMultiOrPipeline();
      String[] allArgs = new String[3 + args.length];
      allArgs[0] = Protocol.Keyword.DRYRUN.name();
      allArgs[1] = username;
      allArgs[2] = command;
      System.arraycopy(args, 0, allArgs, 3, args.length);
      this.connection.sendCommand(Protocol.Command.ACL, allArgs);
      return this.connection.getBulkReply();
   }

   @Override
   public String aclDryRun(String username, CommandArguments commandArgs) {
      this.checkIsInMultiOrPipeline();
      CommandArguments allArgs = new CommandArguments(Protocol.Command.ACL).add(Protocol.Keyword.DRYRUN).add(username);
      Iterator<Rawable> it = commandArgs.iterator();

      while (it.hasNext()) {
         allArgs.add(it.next());
      }

      this.connection.sendCommand(allArgs);
      return this.connection.getBulkReply();
   }

   @Override
   public byte[] aclDryRunBinary(byte[] username, byte[] command, byte[]... args) {
      this.checkIsInMultiOrPipeline();
      byte[][] allArgs = new byte[3 + args.length][];
      allArgs[0] = Protocol.Keyword.DRYRUN.getRaw();
      allArgs[1] = username;
      allArgs[2] = command;
      System.arraycopy(args, 0, allArgs, 3, args.length);
      this.connection.sendCommand(Protocol.Command.ACL, allArgs);
      return this.connection.getBinaryBulkReply();
   }

   @Override
   public byte[] aclDryRunBinary(byte[] username, CommandArguments commandArgs) {
      this.checkIsInMultiOrPipeline();
      CommandArguments allArgs = new CommandArguments(Protocol.Command.ACL).add(Protocol.Keyword.DRYRUN).add(username);
      Iterator<Rawable> it = commandArgs.iterator();

      while (it.hasNext()) {
         allArgs.add(it.next());
      }

      this.connection.sendCommand(allArgs);
      return this.connection.getBinaryBulkReply();
   }

   @Override
   public String clientKill(String ipPort) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.KILL.name(), ipPort);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clientGetname() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.GETNAME);
      return this.connection.getBulkReply();
   }

   @Override
   public String clientList() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.LIST);
      return this.connection.getBulkReply();
   }

   @Override
   public String clientList(ClientType type) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.LIST.getRaw(), Protocol.Keyword.TYPE.getRaw(), type.getRaw());
      return this.connection.getBulkReply();
   }

   @Override
   public String clientList(long... clientIds) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, this.clientListParams(clientIds));
      return this.connection.getBulkReply();
   }

   @Override
   public String clientInfo() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.INFO);
      return this.connection.getBulkReply();
   }

   @Override
   public String clientSetInfo(ClientAttributeOption attr, String value) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.SETINFO.getRaw(), attr.getRaw(), SafeEncoder.encode(value));
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clientSetname(String name) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLIENT, Protocol.Keyword.SETNAME.name(), name);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String migrate(String host, int port, String key, int destinationDb, int timeout) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.migrate(host, port, key, destinationDb, timeout));
   }

   @Override
   public String migrate(String host, int port, int destinationDB, int timeout, MigrateParams params, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.migrate(host, port, destinationDB, timeout, params, keys));
   }

   @Override
   public String migrate(String host, int port, String key, int timeout) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.migrate(host, port, key, timeout));
   }

   @Override
   public String migrate(String host, int port, int timeout, MigrateParams params, String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.migrate(host, port, timeout, params, keys));
   }

   @Override
   public ScanResult<String> scan(String cursor) {
      return this.connection.executeCommand(this.commandObjects.scan(cursor));
   }

   @Override
   public ScanResult<String> scan(String cursor, ScanParams params) {
      return this.connection.executeCommand(this.commandObjects.scan(cursor, params));
   }

   @Override
   public ScanResult<String> scan(String cursor, ScanParams params, String type) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.scan(cursor, params, type));
   }

   @Override
   public ScanResult<Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hscan(key, cursor, params));
   }

   @Override
   public ScanResult<String> hscanNoValues(String key, String cursor, ScanParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hscanNoValues(key, cursor, params));
   }

   @Override
   public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.sscan(key, cursor, params));
   }

   @Override
   public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.zscan(key, cursor, params));
   }

   @Override
   public String readonly() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.READONLY);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String readwrite() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.READWRITE);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clusterNodes() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.NODES);
      return this.connection.getBulkReply();
   }

   @Override
   public String clusterMeet(String ip, int port) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.MEET.name(), ip, Integer.toString(port));
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clusterReset() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.RESET);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clusterReset(ClusterResetType resetType) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.RESET.getRaw(), resetType.getRaw());
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clusterAddSlots(int... slots) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, joinParameters(Protocol.ClusterKeyword.ADDSLOTS.getRaw(), joinParameters(slots)));
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clusterDelSlots(int... slots) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, joinParameters(Protocol.ClusterKeyword.DELSLOTS.getRaw(), joinParameters(slots)));
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clusterInfo() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.INFO);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public List<String> clusterGetKeysInSlot(int slot, int count) {
      this.checkIsInMultiOrPipeline();
      this.connection
         .sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.GETKEYSINSLOT.getRaw(), Protocol.toByteArray(slot), Protocol.toByteArray(count));
      return this.connection.getMultiBulkReply();
   }

   @Override
   public List<byte[]> clusterGetKeysInSlotBinary(int slot, int count) {
      this.checkIsInMultiOrPipeline();
      this.connection
         .sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.GETKEYSINSLOT.getRaw(), Protocol.toByteArray(slot), Protocol.toByteArray(count));
      return this.connection.getBinaryMultiBulkReply();
   }

   @Override
   public String clusterSetSlotNode(int slot, String nodeId) {
      this.checkIsInMultiOrPipeline();
      this.connection
         .sendCommand(
            Protocol.Command.CLUSTER,
            Protocol.ClusterKeyword.SETSLOT.getRaw(),
            Protocol.toByteArray(slot),
            Protocol.ClusterKeyword.NODE.getRaw(),
            SafeEncoder.encode(nodeId)
         );
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clusterSetSlotMigrating(int slot, String nodeId) {
      this.checkIsInMultiOrPipeline();
      this.connection
         .sendCommand(
            Protocol.Command.CLUSTER,
            Protocol.ClusterKeyword.SETSLOT.getRaw(),
            Protocol.toByteArray(slot),
            Protocol.ClusterKeyword.MIGRATING.getRaw(),
            SafeEncoder.encode(nodeId)
         );
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clusterSetSlotImporting(int slot, String nodeId) {
      this.checkIsInMultiOrPipeline();
      this.connection
         .sendCommand(
            Protocol.Command.CLUSTER,
            Protocol.ClusterKeyword.SETSLOT.getRaw(),
            Protocol.toByteArray(slot),
            Protocol.ClusterKeyword.IMPORTING.getRaw(),
            SafeEncoder.encode(nodeId)
         );
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clusterSetSlotStable(int slot) {
      this.checkIsInMultiOrPipeline();
      this.connection
         .sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.SETSLOT.getRaw(), Protocol.toByteArray(slot), Protocol.ClusterKeyword.STABLE.getRaw());
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clusterForget(String nodeId) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.FORGET.name(), nodeId);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clusterFlushSlots() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.FLUSHSLOTS);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public long clusterKeySlot(String key) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.KEYSLOT.name(), key);
      return this.connection.getIntegerReply();
   }

   @Override
   public long clusterCountFailureReports(String nodeId) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, "COUNT-FAILURE-REPORTS", nodeId);
      return this.connection.getIntegerReply();
   }

   @Override
   public long clusterCountKeysInSlot(int slot) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.COUNTKEYSINSLOT.getRaw(), Protocol.toByteArray(slot));
      return this.connection.getIntegerReply();
   }

   @Override
   public String clusterSaveConfig() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.SAVECONFIG);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clusterSetConfigEpoch(long configEpoch) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, "SET-CONFIG-EPOCH", Long.toString(configEpoch));
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clusterBumpEpoch() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.BUMPEPOCH);
      return this.connection.getBulkReply();
   }

   @Override
   public String clusterReplicate(String nodeId) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.REPLICATE.name(), nodeId);
      return this.connection.getStatusCodeReply();
   }

   @Deprecated
   @Override
   public List<String> clusterSlaves(String nodeId) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.SLAVES.name(), nodeId);
      return this.connection.getMultiBulkReply();
   }

   @Override
   public List<String> clusterReplicas(String nodeId) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.REPLICAS.name(), nodeId);
      return this.connection.getMultiBulkReply();
   }

   @Override
   public String clusterFailover() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.FAILOVER);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clusterFailover(ClusterFailoverOption failoverOption) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.FAILOVER.getRaw(), failoverOption.getRaw());
      return this.connection.getStatusCodeReply();
   }

   @Deprecated
   @Override
   public List<Object> clusterSlots() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.SLOTS);
      return this.connection.getObjectMultiBulkReply();
   }

   @Override
   public List<ClusterShardInfo> clusterShards() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.SHARDS);
      return BuilderFactory.CLUSTER_SHARD_INFO_LIST.build(this.connection.getObjectMultiBulkReply());
   }

   @Override
   public String clusterMyId() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.MYID);
      return this.connection.getBulkReply();
   }

   @Override
   public String clusterMyShardId() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.MYSHARDID);
      return this.connection.getBulkReply();
   }

   @Override
   public List<Map<String, Object>> clusterLinks() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.LINKS);
      return this.connection.getObjectMultiBulkReply().stream().map(BuilderFactory.ENCODED_OBJECT_MAP::build).collect(Collectors.toList());
   }

   @Override
   public String clusterAddSlotsRange(int... ranges) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, joinParameters(Protocol.ClusterKeyword.ADDSLOTSRANGE.getRaw(), joinParameters(ranges)));
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String clusterDelSlotsRange(int... ranges) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.CLUSTER, joinParameters(Protocol.ClusterKeyword.DELSLOTSRANGE.getRaw(), joinParameters(ranges)));
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String asking() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.ASKING);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public long pfadd(String key, String... elements) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pfadd(key, elements));
   }

   @Override
   public long pfcount(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pfcount(key));
   }

   @Override
   public long pfcount(String... keys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pfcount(keys));
   }

   @Override
   public String pfmerge(String destkey, String... sourcekeys) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.pfmerge(destkey, sourcekeys));
   }

   @Override
   public Object fcall(String name, List<String> keys, List<String> args) {
      return this.connection.executeCommand(this.commandObjects.fcall(name, keys, args));
   }

   @Override
   public Object fcallReadonly(String name, List<String> keys, List<String> args) {
      return this.connection.executeCommand(this.commandObjects.fcallReadonly(name, keys, args));
   }

   @Override
   public String functionDelete(String libraryName) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionDelete(libraryName));
   }

   @Override
   public String functionLoad(String functionCode) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionLoad(functionCode));
   }

   @Override
   public String functionLoadReplace(String functionCode) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionLoadReplace(functionCode));
   }

   @Override
   public FunctionStats functionStats() {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionStats());
   }

   @Override
   public String functionFlush() {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionFlush());
   }

   @Override
   public String functionFlush(FlushMode mode) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionFlush(mode));
   }

   @Override
   public String functionKill() {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionKill());
   }

   @Override
   public List<LibraryInfo> functionList() {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionList());
   }

   @Override
   public List<LibraryInfo> functionList(String libraryNamePattern) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionList(libraryNamePattern));
   }

   @Override
   public List<LibraryInfo> functionListWithCode() {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionListWithCode());
   }

   @Override
   public List<LibraryInfo> functionListWithCode(String libraryNamePattern) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionListWithCode(libraryNamePattern));
   }

   @Override
   public long geoadd(String key, double longitude, double latitude, String member) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geoadd(key, longitude, latitude, member));
   }

   @Override
   public long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geoadd(key, memberCoordinateMap));
   }

   @Override
   public long geoadd(String key, GeoAddParams params, Map<String, GeoCoordinate> memberCoordinateMap) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geoadd(key, params, memberCoordinateMap));
   }

   @Override
   public Double geodist(String key, String member1, String member2) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geodist(key, member1, member2));
   }

   @Override
   public Double geodist(String key, String member1, String member2, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geodist(key, member1, member2, unit));
   }

   @Override
   public List<String> geohash(String key, String... members) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geohash(key, members));
   }

   @Override
   public List<GeoCoordinate> geopos(String key, String... members) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geopos(key, members));
   }

   @Override
   public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadius(key, longitude, latitude, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadiusReadonly(key, longitude, latitude, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadius(key, longitude, latitude, radius, unit, param));
   }

   @Override
   public long georadiusStore(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadiusStore(key, longitude, latitude, radius, unit, param, storeParam));
   }

   @Override
   public List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadiusReadonly(key, longitude, latitude, radius, unit, param));
   }

   @Override
   public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadiusByMember(key, member, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadiusByMemberReadonly(key, member, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadiusByMember(key, member, radius, unit, param));
   }

   @Override
   public long georadiusByMemberStore(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param, GeoRadiusStoreParam storeParam) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadiusByMemberStore(key, member, radius, unit, param, storeParam));
   }

   @Override
   public List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.georadiusByMemberReadonly(key, member, radius, unit, param));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(String key, String member, double radius, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearch(key, member, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(String key, GeoCoordinate coord, double radius, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearch(key, coord, radius, unit));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(String key, String member, double width, double height, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearch(key, member, width, height, unit));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(String key, GeoCoordinate coord, double width, double height, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearch(key, coord, width, height, unit));
   }

   @Override
   public List<GeoRadiusResponse> geosearch(String key, GeoSearchParam params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearch(key, params));
   }

   @Override
   public long geosearchStore(String dest, String src, String member, double radius, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearchStore(dest, src, member, radius, unit));
   }

   @Override
   public long geosearchStore(String dest, String src, GeoCoordinate coord, double radius, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearchStore(dest, src, coord, radius, unit));
   }

   @Override
   public long geosearchStore(String dest, String src, String member, double width, double height, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearchStore(dest, src, member, width, height, unit));
   }

   @Override
   public long geosearchStore(String dest, String src, GeoCoordinate coord, double width, double height, GeoUnit unit) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearchStore(dest, src, coord, width, height, unit));
   }

   @Override
   public long geosearchStore(String dest, String src, GeoSearchParam params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearchStore(dest, src, params));
   }

   @Override
   public long geosearchStoreStoreDist(String dest, String src, GeoSearchParam params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.geosearchStoreStoreDist(dest, src, params));
   }

   @Override
   public String moduleLoad(String path) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.MODULE, Protocol.Keyword.LOAD.name(), path);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String moduleLoad(String path, String... args) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.MODULE, joinParameters(Protocol.Keyword.LOAD.name(), path, args));
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String moduleLoadEx(String path, ModuleLoadExParams params) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(new CommandArguments(Protocol.Command.MODULE).add(Protocol.Keyword.LOADEX).add(path).addParams(params));
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String moduleUnload(String name) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.MODULE, Protocol.Keyword.UNLOAD.name(), name);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public List<Module> moduleList() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.MODULE, Protocol.Keyword.LIST);
      return BuilderFactory.MODULE_LIST.build(this.connection.getOne());
   }

   @Override
   public List<Long> bitfield(String key, String... arguments) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bitfield(key, arguments));
   }

   @Override
   public List<Long> bitfieldReadonly(String key, String... arguments) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.bitfieldReadonly(key, arguments));
   }

   @Override
   public long hstrlen(String key, String field) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hstrlen(key, field));
   }

   @Override
   public List<Long> hexpire(String key, long seconds, String... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hexpire(key, seconds, fields));
   }

   @Override
   public List<Long> hexpire(String key, long seconds, ExpiryOption condition, String... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hexpire(key, seconds, condition, fields));
   }

   @Override
   public List<Long> hpexpire(String key, long milliseconds, String... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hpexpire(key, milliseconds, fields));
   }

   @Override
   public List<Long> hpexpire(String key, long milliseconds, ExpiryOption condition, String... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hpexpire(key, milliseconds, condition, fields));
   }

   @Override
   public List<Long> hexpireAt(String key, long unixTimeSeconds, String... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hexpireAt(key, unixTimeSeconds, fields));
   }

   @Override
   public List<Long> hexpireAt(String key, long unixTimeSeconds, ExpiryOption condition, String... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hexpireAt(key, unixTimeSeconds, condition, fields));
   }

   @Override
   public List<Long> hpexpireAt(String key, long unixTimeMillis, String... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hpexpireAt(key, unixTimeMillis, fields));
   }

   @Override
   public List<Long> hpexpireAt(String key, long unixTimeMillis, ExpiryOption condition, String... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hpexpireAt(key, unixTimeMillis, condition, fields));
   }

   @Override
   public List<Long> hexpireTime(String key, String... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hexpireTime(key, fields));
   }

   @Override
   public List<Long> hpexpireTime(String key, String... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hpexpireTime(key, fields));
   }

   @Override
   public List<Long> httl(String key, String... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.httl(key, fields));
   }

   @Override
   public List<Long> hpttl(String key, String... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hpttl(key, fields));
   }

   @Override
   public List<Long> hpersist(String key, String... fields) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.hpersist(key, fields));
   }

   @Override
   public String memoryDoctor() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.MEMORY, Protocol.Keyword.DOCTOR);
      return this.connection.getBulkReply();
   }

   @Override
   public Long memoryUsage(String key) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.MEMORY, Protocol.Keyword.USAGE.name(), key);
      return this.connection.getIntegerReply();
   }

   @Override
   public Long memoryUsage(String key, int samples) {
      this.checkIsInMultiOrPipeline();
      this.connection
         .sendCommand(
            Protocol.Command.MEMORY, Protocol.Keyword.USAGE.getRaw(), SafeEncoder.encode(key), Protocol.Keyword.SAMPLES.getRaw(), Protocol.toByteArray(samples)
         );
      return this.connection.getIntegerReply();
   }

   @Override
   public String memoryPurge() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.MEMORY, Protocol.Keyword.PURGE);
      return this.connection.getBulkReply();
   }

   @Override
   public Map<String, Object> memoryStats() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.MEMORY, Protocol.Keyword.STATS);
      return BuilderFactory.ENCODED_OBJECT_MAP.build(this.connection.getOne());
   }

   @Override
   public String lolwut() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.LOLWUT);
      return this.connection.getBulkReply();
   }

   @Override
   public String lolwut(LolwutParams lolwutParams) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(new CommandArguments(Protocol.Command.LOLWUT).addParams(lolwutParams));
      return this.connection.getBulkReply();
   }

   @Override
   public String reset() {
      this.connection.sendCommand(Protocol.Command.RESET);
      return this.connection.getStatusCodeReply();
   }

   @Override
   public String latencyDoctor() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.LATENCY, Protocol.Keyword.DOCTOR);
      return this.connection.getBulkReply();
   }

   @Override
   public Map<String, LatencyLatestInfo> latencyLatest() {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(Protocol.Command.LATENCY, Protocol.Keyword.LATEST);
      return BuilderFactory.LATENCY_LATEST_RESPONSE.build(this.connection.getOne());
   }

   @Override
   public List<LatencyHistoryInfo> latencyHistory(LatencyEvent event) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(new CommandArguments(Protocol.Command.LATENCY).add(Protocol.Keyword.HISTORY).add(event));
      return BuilderFactory.LATENCY_HISTORY_RESPONSE.build(this.connection.getOne());
   }

   @Override
   public long latencyReset(LatencyEvent... events) {
      this.checkIsInMultiOrPipeline();
      CommandArguments arguments = new CommandArguments(Protocol.Command.LATENCY).add(Protocol.Keyword.RESET);
      Arrays.stream(events).forEach(arguments::add);
      this.connection.sendCommand(arguments);
      return this.connection.getIntegerReply();
   }

   @Override
   public StreamEntryID xadd(String key, StreamEntryID id, Map<String, String> hash) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xadd(key, id, hash));
   }

   @Override
   public StreamEntryID xadd(String key, XAddParams params, Map<String, String> hash) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xadd(key, params, hash));
   }

   @Override
   public long xlen(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xlen(key));
   }

   @Override
   public List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xrange(key, start, end));
   }

   @Override
   public List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xrange(key, start, end, count));
   }

   @Override
   public List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xrevrange(key, end, start));
   }

   @Override
   public List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xrevrange(key, end, start, count));
   }

   @Override
   public List<StreamEntry> xrange(String key, String start, String end) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xrange(key, start, end));
   }

   @Override
   public List<StreamEntry> xrange(String key, String start, String end, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xrange(key, start, end, count));
   }

   @Override
   public List<StreamEntry> xrevrange(String key, String end, String start) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xrevrange(key, end, start));
   }

   @Override
   public List<StreamEntry> xrevrange(String key, String end, String start, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xrevrange(key, end, start, count));
   }

   @Override
   public List<Entry<String, List<StreamEntry>>> xread(XReadParams xReadParams, Map<String, StreamEntryID> streams) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xread(xReadParams, streams));
   }

   @Override
   public Map<String, List<StreamEntry>> xreadAsMap(XReadParams xReadParams, Map<String, StreamEntryID> streams) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xreadAsMap(xReadParams, streams));
   }

   @Override
   public long xack(String key, String group, StreamEntryID... ids) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xack(key, group, ids));
   }

   @Override
   public String xgroupCreate(String key, String groupName, StreamEntryID id, boolean makeStream) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xgroupCreate(key, groupName, id, makeStream));
   }

   @Override
   public String xgroupSetID(String key, String groupName, StreamEntryID id) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xgroupSetID(key, groupName, id));
   }

   @Override
   public long xgroupDestroy(String key, String groupName) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xgroupDestroy(key, groupName));
   }

   @Override
   public boolean xgroupCreateConsumer(String key, String groupName, String consumerName) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xgroupCreateConsumer(key, groupName, consumerName));
   }

   @Override
   public long xgroupDelConsumer(String key, String groupName, String consumerName) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xgroupDelConsumer(key, groupName, consumerName));
   }

   @Override
   public long xdel(String key, StreamEntryID... ids) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xdel(key, ids));
   }

   @Override
   public long xtrim(String key, long maxLen, boolean approximateLength) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xtrim(key, maxLen, approximateLength));
   }

   @Override
   public long xtrim(String key, XTrimParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xtrim(key, params));
   }

   @Override
   public List<Entry<String, List<StreamEntry>>> xreadGroup(
      String groupName, String consumer, XReadGroupParams xReadGroupParams, Map<String, StreamEntryID> streams
   ) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xreadGroup(groupName, consumer, xReadGroupParams, streams));
   }

   @Override
   public Map<String, List<StreamEntry>> xreadGroupAsMap(
      String groupName, String consumer, XReadGroupParams xReadGroupParams, Map<String, StreamEntryID> streams
   ) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xreadGroupAsMap(groupName, consumer, xReadGroupParams, streams));
   }

   @Override
   public StreamPendingSummary xpending(String key, String groupName) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xpending(key, groupName));
   }

   @Override
   public List<StreamPendingEntry> xpending(String key, String groupName, XPendingParams params) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xpending(key, groupName, params));
   }

   @Override
   public List<StreamEntry> xclaim(String key, String group, String consumerName, long minIdleTime, XClaimParams params, StreamEntryID... ids) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xclaim(key, group, consumerName, minIdleTime, params, ids));
   }

   @Override
   public List<StreamEntryID> xclaimJustId(String key, String group, String consumerName, long minIdleTime, XClaimParams params, StreamEntryID... ids) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xclaimJustId(key, group, consumerName, minIdleTime, params, ids));
   }

   @Override
   public Entry<StreamEntryID, List<StreamEntry>> xautoclaim(
      String key, String group, String consumerName, long minIdleTime, StreamEntryID start, XAutoClaimParams params
   ) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xautoclaim(key, group, consumerName, minIdleTime, start, params));
   }

   @Override
   public Entry<StreamEntryID, List<StreamEntryID>> xautoclaimJustId(
      String key, String group, String consumerName, long minIdleTime, StreamEntryID start, XAutoClaimParams params
   ) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xautoclaimJustId(key, group, consumerName, minIdleTime, start, params));
   }

   @Override
   public StreamInfo xinfoStream(String key) {
      return this.connection.executeCommand(this.commandObjects.xinfoStream(key));
   }

   @Override
   public StreamFullInfo xinfoStreamFull(String key) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xinfoStreamFull(key));
   }

   @Override
   public StreamFullInfo xinfoStreamFull(String key, int count) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.xinfoStreamFull(key, count));
   }

   @Override
   public List<StreamGroupInfo> xinfoGroups(String key) {
      return this.connection.executeCommand(this.commandObjects.xinfoGroups(key));
   }

   @Override
   public List<StreamConsumersInfo> xinfoConsumers(String key, String group) {
      return this.connection.executeCommand(this.commandObjects.xinfoConsumers(key, group));
   }

   @Override
   public List<StreamConsumerInfo> xinfoConsumers2(String key, String group) {
      return this.connection.executeCommand(this.commandObjects.xinfoConsumers2(key, group));
   }

   @Override
   public Object fcall(byte[] name, List<byte[]> keys, List<byte[]> args) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.fcall(name, keys, args));
   }

   @Override
   public Object fcallReadonly(byte[] name, List<byte[]> keys, List<byte[]> args) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.fcallReadonly(name, keys, args));
   }

   @Override
   public String functionDelete(byte[] libraryName) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionDelete(libraryName));
   }

   @Override
   public byte[] functionDump() {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionDump());
   }

   @Override
   public List<Object> functionListBinary() {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionListBinary());
   }

   @Override
   public List<Object> functionList(byte[] libraryNamePattern) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionList(libraryNamePattern));
   }

   @Override
   public List<Object> functionListWithCodeBinary() {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionListWithCodeBinary());
   }

   @Override
   public List<Object> functionListWithCode(byte[] libraryNamePattern) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionListWithCode(libraryNamePattern));
   }

   @Override
   public String functionLoad(byte[] functionCode) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionLoad(functionCode));
   }

   @Override
   public String functionLoadReplace(byte[] functionCode) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionLoadReplace(functionCode));
   }

   @Override
   public String functionRestore(byte[] serializedValue) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionRestore(serializedValue));
   }

   @Override
   public String functionRestore(byte[] serializedValue, FunctionRestorePolicy policy) {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionRestore(serializedValue, policy));
   }

   @Override
   public Object functionStatsBinary() {
      this.checkIsInMultiOrPipeline();
      return this.connection.executeCommand(this.commandObjects.functionStatsBinary());
   }

   public Object sendCommand(ProtocolCommand cmd, String... args) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(cmd, args);
      return this.connection.getOne();
   }

   public Object sendBlockingCommand(ProtocolCommand cmd, String... args) {
      this.checkIsInMultiOrPipeline();
      this.connection.sendCommand(cmd, args);
      this.connection.setTimeoutInfinite();

      try {
         return this.connection.getOne();
      } finally {
         this.connection.rollbackTimeout();
      }
   }

   private static byte[][] joinParameters(int... params) {
      byte[][] result = new byte[params.length][];

      for (int i = 0; i < params.length; i++) {
         result[i] = Protocol.toByteArray(params[i]);
      }

      return result;
   }

   private static byte[][] joinParameters(byte[] first, byte[][] rest) {
      byte[][] result = new byte[rest.length + 1][];
      result[0] = first;
      System.arraycopy(rest, 0, result, 1, rest.length);
      return result;
   }

   private static byte[][] joinParameters(byte[] first, byte[] second, byte[][] rest) {
      byte[][] result = new byte[rest.length + 2][];
      result[0] = first;
      result[1] = second;
      System.arraycopy(rest, 0, result, 2, rest.length);
      return result;
   }

   private static String[] joinParameters(String first, String[] rest) {
      String[] result = new String[rest.length + 1];
      result[0] = first;
      System.arraycopy(rest, 0, result, 1, rest.length);
      return result;
   }

   private static String[] joinParameters(String first, String second, String[] rest) {
      String[] result = new String[rest.length + 2];
      result[0] = first;
      result[1] = second;
      System.arraycopy(rest, 0, result, 2, rest.length);
      return result;
   }
}
