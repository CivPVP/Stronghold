package me.neznamy.tab.libs.redis.clients.jedis;

import java.net.URI;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import me.neznamy.tab.libs.org.apache.commons.pool2.PooledObjectFactory;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import me.neznamy.tab.libs.redis.clients.jedis.util.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JedisPool extends Pool<Jedis> {
   private static final Logger log = LoggerFactory.getLogger(JedisPool.class);

   public JedisPool() {
      this("127.0.0.1", 6379);
   }

   public JedisPool(String url) {
      this(URI.create(url));
   }

   public JedisPool(String url, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
      this(new GenericObjectPoolConfig<>(), new JedisFactory(URI.create(url), 2000, 2000, null, sslSocketFactory, sslParameters, hostnameVerifier));
   }

   public JedisPool(String host, int port) {
      this(new HostAndPort(host, port), DefaultJedisClientConfig.builder().build());
   }

   public JedisPool(String host, int port, boolean ssl) {
      this(new HostAndPort(host, port), DefaultJedisClientConfig.builder().ssl(ssl).build());
   }

   public JedisPool(String host, int port, boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
      this(
         new HostAndPort(host, port),
         DefaultJedisClientConfig.builder().ssl(ssl).sslSocketFactory(sslSocketFactory).sslParameters(sslParameters).hostnameVerifier(hostnameVerifier).build()
      );
   }

   public JedisPool(String host, int port, String user, String password) {
      this(new HostAndPort(host, port), DefaultJedisClientConfig.builder().user(user).password(password).build());
   }

   public JedisPool(HostAndPort hostAndPort, JedisClientConfig clientConfig) {
      this(new JedisFactory(hostAndPort, clientConfig));
   }

   public JedisPool(PooledObjectFactory<Jedis> factory) {
      super(factory);
   }

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig) {
      this(poolConfig, "127.0.0.1", 6379);
   }

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, String url) {
      this(poolConfig, URI.create(url));
   }

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, String host, int port) {
      this(poolConfig, host, port, 2000);
   }

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, String host, int port, boolean ssl) {
      this(poolConfig, host, port, 2000, ssl);
   }

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig,
      String host,
      int port,
      boolean ssl,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(poolConfig, host, port, 2000, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, String host, int port, String user, String password) {
      this(poolConfig, host, port, 2000, user, password, 0);
   }

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, String host, int port, int timeout) {
      this(poolConfig, host, port, timeout, null);
   }

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, String host, int port, int timeout, boolean ssl) {
      this(poolConfig, host, port, timeout, null, ssl);
   }

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig,
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

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, String host, int port, int timeout, String password) {
      this(poolConfig, host, port, timeout, password, 0);
   }

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, String host, int port, int timeout, String password, boolean ssl) {
      this(poolConfig, host, port, timeout, password, 0, ssl);
   }

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig,
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

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, String host, int port, int timeout, String user, String password) {
      this(poolConfig, host, port, timeout, user, password, 0);
   }

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, String host, int port, int timeout, String user, String password, boolean ssl) {
      this(poolConfig, host, port, timeout, user, password, 0, ssl);
   }

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, String host, int port, int timeout, String password, int database) {
      this(poolConfig, host, port, timeout, password, database, null);
   }

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, String host, int port, int timeout, String password, int database, boolean ssl) {
      this(poolConfig, host, port, timeout, password, database, null, ssl);
   }

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig,
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

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, String host, int port, int timeout, String user, String password, int database) {
      this(poolConfig, host, port, timeout, user, password, database, null);
   }

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, String host, int port, int timeout, String user, String password, int database, boolean ssl) {
      this(poolConfig, host, port, timeout, user, password, database, null, ssl);
   }

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, String host, int port, int timeout, String password, int database, String clientName) {
      this(poolConfig, host, port, timeout, timeout, password, database, clientName);
   }

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig, String host, int port, int timeout, String password, int database, String clientName, boolean ssl
   ) {
      this(poolConfig, host, port, timeout, timeout, password, database, clientName, ssl);
   }

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig,
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

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig, String host, int port, int timeout, String user, String password, int database, String clientName
   ) {
      this(poolConfig, host, port, timeout, timeout, user, password, database, clientName);
   }

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig, String host, int port, int timeout, String user, String password, int database, String clientName, boolean ssl
   ) {
      this(poolConfig, host, port, timeout, timeout, user, password, database, clientName, ssl);
   }

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig, String host, int port, int connectionTimeout, int soTimeout, String password, int database, String clientName
   ) {
      this(poolConfig, new JedisFactory(host, port, connectionTimeout, soTimeout, password, database, clientName));
   }

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig,
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

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig,
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
      this(
         poolConfig,
         new JedisFactory(host, port, connectionTimeout, soTimeout, password, database, clientName, ssl, sslSocketFactory, sslParameters, hostnameVerifier)
      );
   }

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig,
      String host,
      int port,
      int connectionTimeout,
      int soTimeout,
      String user,
      String password,
      int database,
      String clientName
   ) {
      this(poolConfig, new JedisFactory(host, port, connectionTimeout, soTimeout, user, password, database, clientName));
   }

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig,
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

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig,
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

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig,
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

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig,
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
      this(poolConfig, new JedisFactory(host, port, connectionTimeout, soTimeout, infiniteSoTimeout, user, password, database, clientName));
   }

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig,
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
         poolConfig,
         new JedisFactory(
            host,
            port,
            connectionTimeout,
            soTimeout,
            infiniteSoTimeout,
            user,
            password,
            database,
            clientName,
            ssl,
            sslSocketFactory,
            sslParameters,
            hostnameVerifier
         )
      );
   }

   public JedisPool(URI uri) {
      this(new GenericObjectPoolConfig<>(), uri);
   }

   public JedisPool(URI uri, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
      this(new GenericObjectPoolConfig<>(), uri, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   public JedisPool(URI uri, int timeout) {
      this(new GenericObjectPoolConfig<>(), uri, timeout);
   }

   public JedisPool(URI uri, int timeout, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
      this(new GenericObjectPoolConfig<>(), uri, timeout, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, URI uri) {
      this(poolConfig, uri, 2000);
   }

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig, URI uri, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier
   ) {
      this(poolConfig, uri, 2000, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, URI uri, int timeout) {
      this(poolConfig, uri, timeout, timeout);
   }

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig,
      URI uri,
      int timeout,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(poolConfig, uri, timeout, timeout, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, URI uri, int connectionTimeout, int soTimeout) {
      this(poolConfig, uri, connectionTimeout, soTimeout, null, null, null);
   }

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig,
      URI uri,
      int connectionTimeout,
      int soTimeout,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(poolConfig, new JedisFactory(uri, connectionTimeout, soTimeout, null, sslSocketFactory, sslParameters, hostnameVerifier));
   }

   public JedisPool(
      GenericObjectPoolConfig<Jedis> poolConfig,
      URI uri,
      int connectionTimeout,
      int soTimeout,
      int infiniteSoTimeout,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(poolConfig, new JedisFactory(uri, connectionTimeout, soTimeout, infiniteSoTimeout, null, sslSocketFactory, sslParameters, hostnameVerifier));
   }

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, HostAndPort hostAndPort, JedisClientConfig clientConfig) {
      this(poolConfig, new JedisFactory(hostAndPort, clientConfig));
   }

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, JedisSocketFactory jedisSocketFactory, JedisClientConfig clientConfig) {
      this(poolConfig, new JedisFactory(jedisSocketFactory, clientConfig));
   }

   public JedisPool(GenericObjectPoolConfig<Jedis> poolConfig, PooledObjectFactory<Jedis> factory) {
      super(poolConfig, factory);
   }

   public Jedis getResource() {
      Jedis jedis = (Jedis)super.getResource();
      jedis.setDataSource(this);
      return jedis;
   }

   public void returnResource(Jedis resource) {
      if (resource != null) {
         try {
            resource.resetState();
            super.returnResource(resource);
         } catch (RuntimeException e) {
            super.returnBrokenResource(resource);
            log.warn("Resource is returned to the pool as broken", e);
         }
      }
   }
}
