package me.neznamy.tab.libs.redis.clients.jedis;

import me.neznamy.tab.libs.org.apache.commons.pool2.PooledObject;
import me.neznamy.tab.libs.org.apache.commons.pool2.PooledObjectFactory;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.DefaultPooledObject;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Experimental;
import me.neznamy.tab.libs.redis.clients.jedis.csc.Cache;
import me.neznamy.tab.libs.redis.clients.jedis.csc.CacheConnection;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionFactory implements PooledObjectFactory<Connection> {
   private static final Logger logger = LoggerFactory.getLogger(ConnectionFactory.class);
   private final JedisSocketFactory jedisSocketFactory;
   private final JedisClientConfig clientConfig;
   private Cache clientSideCache = null;

   public ConnectionFactory(HostAndPort hostAndPort) {
      this.clientConfig = DefaultJedisClientConfig.builder().build();
      this.jedisSocketFactory = new DefaultJedisSocketFactory(hostAndPort);
   }

   public ConnectionFactory(HostAndPort hostAndPort, JedisClientConfig clientConfig) {
      this.clientConfig = clientConfig;
      this.jedisSocketFactory = new DefaultJedisSocketFactory(hostAndPort, this.clientConfig);
   }

   @Experimental
   public ConnectionFactory(HostAndPort hostAndPort, JedisClientConfig clientConfig, Cache csCache) {
      this.clientConfig = clientConfig;
      this.jedisSocketFactory = new DefaultJedisSocketFactory(hostAndPort, this.clientConfig);
      this.clientSideCache = csCache;
   }

   public ConnectionFactory(JedisSocketFactory jedisSocketFactory, JedisClientConfig clientConfig) {
      this.clientConfig = clientConfig;
      this.jedisSocketFactory = jedisSocketFactory;
   }

   @Override
   public void activateObject(PooledObject<Connection> pooledConnection) throws Exception {
   }

   @Override
   public void destroyObject(PooledObject<Connection> pooledConnection) throws Exception {
      Connection jedis = pooledConnection.getObject();
      if (jedis.isConnected()) {
         try {
            jedis.close();
         } catch (RuntimeException e) {
            logger.debug("Error while close", e);
         }
      }
   }

   @Override
   public PooledObject<Connection> makeObject() throws Exception {
      try {
         Connection jedis = this.clientSideCache == null
            ? new Connection(this.jedisSocketFactory, this.clientConfig)
            : new CacheConnection(this.jedisSocketFactory, this.clientConfig, this.clientSideCache);
         return new DefaultPooledObject<>(jedis);
      } catch (JedisException je) {
         logger.debug("Error while makeObject", je);
         throw je;
      }
   }

   @Override
   public void passivateObject(PooledObject<Connection> pooledConnection) throws Exception {
   }

   @Override
   public boolean validateObject(PooledObject<Connection> pooledConnection) {
      Connection jedis = pooledConnection.getObject();

      try {
         return jedis.isConnected() && jedis.ping();
      } catch (Exception e) {
         logger.warn("Error while validating pooled Connection object.", e);
         return false;
      }
   }
}
