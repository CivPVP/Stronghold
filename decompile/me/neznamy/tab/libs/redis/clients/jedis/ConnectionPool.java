package me.neznamy.tab.libs.redis.clients.jedis;

import me.neznamy.tab.libs.org.apache.commons.pool2.PooledObjectFactory;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Experimental;
import me.neznamy.tab.libs.redis.clients.jedis.csc.Cache;
import me.neznamy.tab.libs.redis.clients.jedis.util.Pool;

public class ConnectionPool extends Pool<Connection> {
   public ConnectionPool(HostAndPort hostAndPort, JedisClientConfig clientConfig) {
      this(new ConnectionFactory(hostAndPort, clientConfig));
   }

   @Experimental
   public ConnectionPool(HostAndPort hostAndPort, JedisClientConfig clientConfig, Cache clientSideCache) {
      this(new ConnectionFactory(hostAndPort, clientConfig, clientSideCache));
   }

   public ConnectionPool(PooledObjectFactory<Connection> factory) {
      super(factory);
   }

   public ConnectionPool(HostAndPort hostAndPort, JedisClientConfig clientConfig, GenericObjectPoolConfig<Connection> poolConfig) {
      this(new ConnectionFactory(hostAndPort, clientConfig), poolConfig);
   }

   @Experimental
   public ConnectionPool(HostAndPort hostAndPort, JedisClientConfig clientConfig, Cache clientSideCache, GenericObjectPoolConfig<Connection> poolConfig) {
      this(new ConnectionFactory(hostAndPort, clientConfig, clientSideCache), poolConfig);
   }

   public ConnectionPool(PooledObjectFactory<Connection> factory, GenericObjectPoolConfig<Connection> poolConfig) {
      super(factory, poolConfig);
   }

   public Connection getResource() {
      Connection conn = (Connection)super.getResource();
      conn.setHandlingPool(this);
      return conn;
   }
}
