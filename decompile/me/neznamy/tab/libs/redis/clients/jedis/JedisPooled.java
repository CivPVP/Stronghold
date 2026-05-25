package me.neznamy.tab.libs.redis.clients.jedis;

import java.net.URI;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import me.neznamy.tab.libs.org.apache.commons.pool2.PooledObjectFactory;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Experimental;
import me.neznamy.tab.libs.redis.clients.jedis.csc.Cache;
import me.neznamy.tab.libs.redis.clients.jedis.csc.CacheConfig;
import me.neznamy.tab.libs.redis.clients.jedis.csc.CacheFactory;
import me.neznamy.tab.libs.redis.clients.jedis.providers.PooledConnectionProvider;
import me.neznamy.tab.libs.redis.clients.jedis.util.JedisURIHelper;
import me.neznamy.tab.libs.redis.clients.jedis.util.Pool;

public class JedisPooled extends UnifiedJedis {
   public JedisPooled() {
      this("127.0.0.1", 6379);
   }

   public JedisPooled(String url) {
      super(url);
   }

   public JedisPooled(String url, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
      this(URI.create(url), sslSocketFactory, sslParameters, hostnameVerifier);
   }

   public JedisPooled(String host, int port) {
      this(new HostAndPort(host, port));
   }

   public JedisPooled(HostAndPort hostAndPort) {
      super(hostAndPort);
   }

   public JedisPooled(String host, int port, boolean ssl) {
      this(new HostAndPort(host, port), DefaultJedisClientConfig.builder().ssl(ssl).build());
   }

   public JedisPooled(String host, int port, boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
      this(
         new HostAndPort(host, port),
         DefaultJedisClientConfig.builder().ssl(ssl).sslSocketFactory(sslSocketFactory).sslParameters(sslParameters).hostnameVerifier(hostnameVerifier).build()
      );
   }

   public JedisPooled(String host, int port, String user, String password) {
      this(new HostAndPort(host, port), DefaultJedisClientConfig.builder().user(user).password(password).build());
   }

   public JedisPooled(HostAndPort hostAndPort, JedisClientConfig clientConfig) {
      super(hostAndPort, clientConfig);
   }

   @Experimental
   public JedisPooled(HostAndPort hostAndPort, JedisClientConfig clientConfig, CacheConfig cacheConfig) {
      this(hostAndPort, clientConfig, CacheFactory.getCache(cacheConfig));
   }

   @Experimental
   public JedisPooled(HostAndPort hostAndPort, JedisClientConfig clientConfig, Cache clientSideCache) {
      super(hostAndPort, clientConfig, clientSideCache);
   }

   public JedisPooled(PooledObjectFactory<Connection> factory) {
      this(new PooledConnectionProvider(factory));
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig) {
      this(poolConfig, "127.0.0.1", 6379);
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, String url) {
      this(poolConfig, URI.create(url));
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, String host, int port) {
      this(poolConfig, host, port, 2000);
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, String host, int port, boolean ssl) {
      this(poolConfig, host, port, 2000, ssl);
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig,
      String host,
      int port,
      boolean ssl,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(poolConfig, host, port, 2000, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, String host, int port, String user, String password) {
      this(poolConfig, host, port, 2000, user, password, 0);
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, String host, int port, int timeout) {
      this(poolConfig, host, port, timeout, null);
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, String host, int port, int timeout, boolean ssl) {
      this(poolConfig, host, port, timeout, null, ssl);
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig,
      String host,
      int port,
      int timeout,
      boolean ssl,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(poolConfig, host, port, timeout, null, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, String host, int port, int timeout, String password) {
      this(poolConfig, host, port, timeout, password, 0);
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, String host, int port, int timeout, String password, boolean ssl) {
      this(poolConfig, host, port, timeout, password, 0, ssl);
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig,
      String host,
      int port,
      int timeout,
      String password,
      boolean ssl,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(poolConfig, host, port, timeout, password, 0, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, String host, int port, int timeout, String user, String password) {
      this(poolConfig, host, port, timeout, user, password, 0);
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, String host, int port, int timeout, String user, String password, boolean ssl) {
      this(poolConfig, host, port, timeout, user, password, 0, ssl);
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, String host, int port, int timeout, String password, int database) {
      this(poolConfig, host, port, timeout, password, database, null);
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, String host, int port, int timeout, String password, int database, boolean ssl) {
      this(poolConfig, host, port, timeout, password, database, null, ssl);
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig,
      String host,
      int port,
      int timeout,
      String password,
      int database,
      boolean ssl,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(poolConfig, host, port, timeout, password, database, null, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, String host, int port, int timeout, String user, String password, int database) {
      this(poolConfig, host, port, timeout, user, password, database, null);
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig, String host, int port, int timeout, String user, String password, int database, boolean ssl
   ) {
      this(poolConfig, host, port, timeout, user, password, database, null, ssl);
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, String host, int port, int timeout, String password, int database, String clientName) {
      this(poolConfig, host, port, timeout, timeout, password, database, clientName);
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig, String host, int port, int timeout, String password, int database, String clientName, boolean ssl
   ) {
      this(poolConfig, host, port, timeout, timeout, password, database, clientName, ssl);
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig,
      String host,
      int port,
      int timeout,
      String password,
      int database,
      String clientName,
      boolean ssl,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(poolConfig, host, port, timeout, timeout, password, database, clientName, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig, String host, int port, int timeout, String user, String password, int database, String clientName
   ) {
      this(poolConfig, host, port, timeout, timeout, user, password, database, clientName);
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig,
      String host,
      int port,
      int timeout,
      String user,
      String password,
      int database,
      String clientName,
      boolean ssl
   ) {
      this(poolConfig, host, port, timeout, timeout, user, password, database, clientName, ssl);
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig,
      String host,
      int port,
      int connectionTimeout,
      int soTimeout,
      String password,
      int database,
      String clientName
   ) {
      this(poolConfig, host, port, connectionTimeout, soTimeout, null, password, database, clientName);
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig,
      String host,
      int port,
      int connectionTimeout,
      int soTimeout,
      String password,
      int database,
      String clientName,
      boolean ssl
   ) {
      this(poolConfig, host, port, connectionTimeout, soTimeout, password, database, clientName, ssl, null, null, null);
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig,
      String host,
      int port,
      int connectionTimeout,
      int soTimeout,
      String password,
      int database,
      String clientName,
      boolean ssl,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(poolConfig, host, port, connectionTimeout, soTimeout, null, password, database, clientName, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig,
      String host,
      int port,
      int connectionTimeout,
      int soTimeout,
      String user,
      String password,
      int database,
      String clientName
   ) {
      this(poolConfig, host, port, connectionTimeout, soTimeout, 0, user, password, database, clientName);
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig,
      String host,
      int port,
      int connectionTimeout,
      int soTimeout,
      String user,
      String password,
      int database,
      String clientName,
      boolean ssl
   ) {
      this(poolConfig, host, port, connectionTimeout, soTimeout, user, password, database, clientName, ssl, null, null, null);
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig,
      String host,
      int port,
      int connectionTimeout,
      int soTimeout,
      String user,
      String password,
      int database,
      String clientName,
      boolean ssl,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(
         poolConfig, host, port, connectionTimeout, soTimeout, 0, user, password, database, clientName, ssl, sslSocketFactory, sslParameters, hostnameVerifier
      );
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig,
      String host,
      int port,
      int connectionTimeout,
      int soTimeout,
      int infiniteSoTimeout,
      String password,
      int database,
      String clientName,
      boolean ssl,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(
         poolConfig,
         host,
         port,
         connectionTimeout,
         soTimeout,
         infiniteSoTimeout,
         null,
         password,
         database,
         clientName,
         ssl,
         sslSocketFactory,
         sslParameters,
         hostnameVerifier
      );
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig,
      String host,
      int port,
      int connectionTimeout,
      int soTimeout,
      int infiniteSoTimeout,
      String user,
      String password,
      int database,
      String clientName
   ) {
      this(
         new HostAndPort(host, port),
         DefaultJedisClientConfig.create(connectionTimeout, soTimeout, infiniteSoTimeout, user, password, database, clientName, false, null, null, null, null),
         poolConfig
      );
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig,
      String host,
      int port,
      int connectionTimeout,
      int soTimeout,
      int infiniteSoTimeout,
      String user,
      String password,
      int database,
      String clientName,
      boolean ssl,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(
         new HostAndPort(host, port),
         DefaultJedisClientConfig.create(
            connectionTimeout, soTimeout, infiniteSoTimeout, user, password, database, clientName, ssl, sslSocketFactory, sslParameters, hostnameVerifier, null
         ),
         poolConfig
      );
   }

   public JedisPooled(URI uri) {
      super(uri);
   }

   public JedisPooled(URI uri, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
      this(new GenericObjectPoolConfig<>(), uri, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   public JedisPooled(URI uri, int timeout) {
      this(new GenericObjectPoolConfig<>(), uri, timeout);
   }

   public JedisPooled(URI uri, int timeout, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
      this(new GenericObjectPoolConfig<>(), uri, timeout, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, URI uri) {
      this(poolConfig, uri, 2000);
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig,
      URI uri,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(poolConfig, uri, 2000, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, URI uri, int timeout) {
      this(poolConfig, uri, timeout, timeout);
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig,
      URI uri,
      int timeout,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(poolConfig, uri, timeout, timeout, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, URI uri, int connectionTimeout, int soTimeout) {
      this(poolConfig, uri, connectionTimeout, soTimeout, null, null, null);
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig,
      URI uri,
      int connectionTimeout,
      int soTimeout,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(poolConfig, uri, connectionTimeout, soTimeout, 0, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   public JedisPooled(
      GenericObjectPoolConfig<Connection> poolConfig,
      URI uri,
      int connectionTimeout,
      int soTimeout,
      int infiniteSoTimeout,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(
         new HostAndPort(uri.getHost(), uri.getPort()),
         DefaultJedisClientConfig.builder()
            .connectionTimeoutMillis(connectionTimeout)
            .socketTimeoutMillis(soTimeout)
            .blockingSocketTimeoutMillis(infiniteSoTimeout)
            .user(JedisURIHelper.getUser(uri))
            .password(JedisURIHelper.getPassword(uri))
            .database(JedisURIHelper.getDBIndex(uri))
            .protocol(JedisURIHelper.getRedisProtocol(uri))
            .ssl(JedisURIHelper.isRedisSSLScheme(uri))
            .sslSocketFactory(sslSocketFactory)
            .sslParameters(sslParameters)
            .hostnameVerifier(hostnameVerifier)
            .build(),
         poolConfig
      );
   }

   public JedisPooled(HostAndPort hostAndPort, GenericObjectPoolConfig<Connection> poolConfig) {
      this(hostAndPort, DefaultJedisClientConfig.builder().build(), poolConfig);
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, HostAndPort hostAndPort, JedisClientConfig clientConfig) {
      this(hostAndPort, clientConfig, poolConfig);
   }

   public JedisPooled(HostAndPort hostAndPort, JedisClientConfig clientConfig, GenericObjectPoolConfig<Connection> poolConfig) {
      super(new PooledConnectionProvider(hostAndPort, clientConfig, poolConfig), clientConfig.getRedisProtocol());
   }

   @Experimental
   public JedisPooled(HostAndPort hostAndPort, JedisClientConfig clientConfig, CacheConfig cacheConfig, GenericObjectPoolConfig<Connection> poolConfig) {
      this(hostAndPort, clientConfig, CacheFactory.getCache(cacheConfig), poolConfig);
   }

   @Experimental
   public JedisPooled(HostAndPort hostAndPort, JedisClientConfig clientConfig, Cache clientSideCache, GenericObjectPoolConfig<Connection> poolConfig) {
      super(new PooledConnectionProvider(hostAndPort, clientConfig, clientSideCache, poolConfig), clientConfig.getRedisProtocol(), clientSideCache);
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, JedisSocketFactory jedisSocketFactory, JedisClientConfig clientConfig) {
      super(new PooledConnectionProvider(new ConnectionFactory(jedisSocketFactory, clientConfig), poolConfig), clientConfig.getRedisProtocol());
   }

   public JedisPooled(GenericObjectPoolConfig<Connection> poolConfig, PooledObjectFactory<Connection> factory) {
      this(factory, poolConfig);
   }

   public JedisPooled(PooledObjectFactory<Connection> factory, GenericObjectPoolConfig<Connection> poolConfig) {
      this(new PooledConnectionProvider(factory, poolConfig));
   }

   public JedisPooled(PooledConnectionProvider provider) {
      super(provider);
   }

   public final Pool<Connection> getPool() {
      return ((PooledConnectionProvider)this.provider).getPool();
   }

   public Pipeline pipelined() {
      return (Pipeline)super.pipelined();
   }
}
