package me.neznamy.tab.libs.redis.clients.jedis;

import java.net.URI;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import me.neznamy.tab.libs.org.apache.commons.pool2.PooledObject;
import me.neznamy.tab.libs.org.apache.commons.pool2.PooledObjectFactory;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.DefaultPooledObject;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.InvalidURIException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;
import me.neznamy.tab.libs.redis.clients.jedis.util.JedisURIHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JedisFactory implements PooledObjectFactory<Jedis> {
   private static final Logger logger = LoggerFactory.getLogger(JedisFactory.class);
   private final JedisSocketFactory jedisSocketFactory;
   private final JedisClientConfig clientConfig;

   protected JedisFactory(String host, int port, int connectionTimeout, int soTimeout, String password, int database, String clientName) {
      this(host, port, connectionTimeout, soTimeout, password, database, clientName, false, null, null, null);
   }

   protected JedisFactory(String host, int port, int connectionTimeout, int soTimeout, String user, String password, int database, String clientName) {
      this(host, port, connectionTimeout, soTimeout, 0, user, password, database, clientName);
   }

   protected JedisFactory(
      String host, int port, int connectionTimeout, int soTimeout, int infiniteSoTimeout, String user, String password, int database, String clientName
   ) {
      this(host, port, connectionTimeout, soTimeout, infiniteSoTimeout, user, password, database, clientName, false, null, null, null);
   }

   JedisFactory(int connectionTimeout, int soTimeout, int infiniteSoTimeout, String user, String password, int database, String clientName) {
      this(connectionTimeout, soTimeout, infiniteSoTimeout, user, password, database, clientName, false, null, null, null);
   }

   protected JedisFactory(
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
      this(host, port, connectionTimeout, soTimeout, null, password, database, clientName, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   protected JedisFactory(
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
      this(host, port, connectionTimeout, soTimeout, 0, user, password, database, clientName, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   protected JedisFactory(HostAndPort hostAndPort, JedisClientConfig clientConfig) {
      this.clientConfig = clientConfig;
      this.jedisSocketFactory = new DefaultJedisSocketFactory(hostAndPort, this.clientConfig);
   }

   protected JedisFactory(
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
      this.clientConfig = DefaultJedisClientConfig.builder()
         .connectionTimeoutMillis(connectionTimeout)
         .socketTimeoutMillis(soTimeout)
         .blockingSocketTimeoutMillis(infiniteSoTimeout)
         .user(user)
         .password(password)
         .database(database)
         .clientName(clientName)
         .ssl(ssl)
         .sslSocketFactory(sslSocketFactory)
         .sslParameters(sslParameters)
         .hostnameVerifier(hostnameVerifier)
         .build();
      this.jedisSocketFactory = new DefaultJedisSocketFactory(new HostAndPort(host, port), this.clientConfig);
   }

   protected JedisFactory(JedisSocketFactory jedisSocketFactory, JedisClientConfig clientConfig) {
      this.clientConfig = clientConfig;
      this.jedisSocketFactory = jedisSocketFactory;
   }

   JedisFactory(
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
         DefaultJedisClientConfig.builder()
            .connectionTimeoutMillis(connectionTimeout)
            .socketTimeoutMillis(soTimeout)
            .blockingSocketTimeoutMillis(infiniteSoTimeout)
            .user(user)
            .password(password)
            .database(database)
            .clientName(clientName)
            .ssl(ssl)
            .sslSocketFactory(sslSocketFactory)
            .sslParameters(sslParameters)
            .hostnameVerifier(hostnameVerifier)
            .build()
      );
   }

   JedisFactory(JedisClientConfig clientConfig) {
      this.clientConfig = clientConfig;
      this.jedisSocketFactory = new DefaultJedisSocketFactory(clientConfig);
   }

   protected JedisFactory(URI uri, int connectionTimeout, int soTimeout, String clientName) {
      this(uri, connectionTimeout, soTimeout, clientName, null, null, null);
   }

   protected JedisFactory(
      URI uri,
      int connectionTimeout,
      int soTimeout,
      String clientName,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      this(uri, connectionTimeout, soTimeout, 0, clientName, sslSocketFactory, sslParameters, hostnameVerifier);
   }

   protected JedisFactory(
      URI uri,
      int connectionTimeout,
      int soTimeout,
      int infiniteSoTimeout,
      String clientName,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier
   ) {
      if (!JedisURIHelper.isValid(uri)) {
         throw new InvalidURIException(String.format("Cannot open Redis connection due invalid URI. %s", uri.toString()));
      }

      this.clientConfig = DefaultJedisClientConfig.builder()
         .connectionTimeoutMillis(connectionTimeout)
         .socketTimeoutMillis(soTimeout)
         .blockingSocketTimeoutMillis(infiniteSoTimeout)
         .user(JedisURIHelper.getUser(uri))
         .password(JedisURIHelper.getPassword(uri))
         .database(JedisURIHelper.getDBIndex(uri))
         .clientName(clientName)
         .protocol(JedisURIHelper.getRedisProtocol(uri))
         .ssl(JedisURIHelper.isRedisSSLScheme(uri))
         .sslSocketFactory(sslSocketFactory)
         .sslParameters(sslParameters)
         .hostnameVerifier(hostnameVerifier)
         .build();
      this.jedisSocketFactory = new DefaultJedisSocketFactory(new HostAndPort(uri.getHost(), uri.getPort()), this.clientConfig);
   }

   void setHostAndPort(HostAndPort hostAndPort) {
      if (!(this.jedisSocketFactory instanceof DefaultJedisSocketFactory)) {
         throw new IllegalStateException("setHostAndPort method has limited capability.");
      }

      ((DefaultJedisSocketFactory)this.jedisSocketFactory).updateHostAndPort(hostAndPort);
   }

   @Override
   public void activateObject(PooledObject<Jedis> pooledJedis) throws Exception {
      Jedis jedis = pooledJedis.getObject();
      if (jedis.getDB() != this.clientConfig.getDatabase()) {
         jedis.select(this.clientConfig.getDatabase());
      }
   }

   @Override
   public void destroyObject(PooledObject<Jedis> pooledJedis) throws Exception {
      Jedis jedis = pooledJedis.getObject();
      if (jedis.isConnected()) {
         try {
            jedis.close();
         } catch (RuntimeException e) {
            logger.debug("Error while close", e);
         }
      }
   }

   @Override
   public PooledObject<Jedis> makeObject() throws Exception {
      Jedis jedis = null;

      try {
         jedis = new Jedis(this.jedisSocketFactory, this.clientConfig);
         return new DefaultPooledObject<>(jedis);
      } catch (JedisException je) {
         logger.debug("Error while makeObject", je);
         throw je;
      }
   }

   @Override
   public void passivateObject(PooledObject<Jedis> pooledJedis) throws Exception {
   }

   @Override
   public boolean validateObject(PooledObject<Jedis> pooledJedis) {
      Jedis jedis = pooledJedis.getObject();

      try {
         boolean targetHasNotChanged = true;
         if (this.jedisSocketFactory instanceof DefaultJedisSocketFactory) {
            HostAndPort targetAddress = ((DefaultJedisSocketFactory)this.jedisSocketFactory).getHostAndPort();
            HostAndPort objectAddress = jedis.getConnection().getHostAndPort();
            targetHasNotChanged = targetAddress.getHost().equals(objectAddress.getHost()) && targetAddress.getPort() == objectAddress.getPort();
         }

         return targetHasNotChanged && jedis.getConnection().isConnected() && jedis.ping().equals("PONG");
      } catch (Exception e) {
         logger.warn("Error while validating pooled Jedis object.", e);
         return false;
      }
   }
}
