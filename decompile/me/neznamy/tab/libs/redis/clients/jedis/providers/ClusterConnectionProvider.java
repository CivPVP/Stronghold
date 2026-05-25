package me.neznamy.tab.libs.redis.clients.jedis.providers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import me.neznamy.tab.libs.redis.clients.jedis.ClusterCommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Connection;
import me.neznamy.tab.libs.redis.clients.jedis.ConnectionPool;
import me.neznamy.tab.libs.redis.clients.jedis.HostAndPort;
import me.neznamy.tab.libs.redis.clients.jedis.JedisClientConfig;
import me.neznamy.tab.libs.redis.clients.jedis.JedisClusterInfoCache;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Experimental;
import me.neznamy.tab.libs.redis.clients.jedis.csc.Cache;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisClusterOperationException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;

public class ClusterConnectionProvider implements ConnectionProvider {
   protected final JedisClusterInfoCache cache;

   public ClusterConnectionProvider(Set<HostAndPort> clusterNodes, JedisClientConfig clientConfig) {
      this.cache = new JedisClusterInfoCache(clientConfig, clusterNodes);
      this.initializeSlotsCache(clusterNodes, clientConfig);
   }

   @Experimental
   public ClusterConnectionProvider(Set<HostAndPort> clusterNodes, JedisClientConfig clientConfig, Cache clientSideCache) {
      this.cache = new JedisClusterInfoCache(clientConfig, clientSideCache, clusterNodes);
      this.initializeSlotsCache(clusterNodes, clientConfig);
   }

   public ClusterConnectionProvider(Set<HostAndPort> clusterNodes, JedisClientConfig clientConfig, GenericObjectPoolConfig<Connection> poolConfig) {
      this.cache = new JedisClusterInfoCache(clientConfig, poolConfig, clusterNodes);
      this.initializeSlotsCache(clusterNodes, clientConfig);
   }

   @Experimental
   public ClusterConnectionProvider(
      Set<HostAndPort> clusterNodes, JedisClientConfig clientConfig, Cache clientSideCache, GenericObjectPoolConfig<Connection> poolConfig
   ) {
      this.cache = new JedisClusterInfoCache(clientConfig, clientSideCache, poolConfig, clusterNodes);
      this.initializeSlotsCache(clusterNodes, clientConfig);
   }

   public ClusterConnectionProvider(
      Set<HostAndPort> clusterNodes, JedisClientConfig clientConfig, GenericObjectPoolConfig<Connection> poolConfig, Duration topologyRefreshPeriod
   ) {
      this.cache = new JedisClusterInfoCache(clientConfig, poolConfig, clusterNodes, topologyRefreshPeriod);
      this.initializeSlotsCache(clusterNodes, clientConfig);
   }

   @Experimental
   public ClusterConnectionProvider(
      Set<HostAndPort> clusterNodes,
      JedisClientConfig clientConfig,
      Cache clientSideCache,
      GenericObjectPoolConfig<Connection> poolConfig,
      Duration topologyRefreshPeriod
   ) {
      this.cache = new JedisClusterInfoCache(clientConfig, clientSideCache, poolConfig, clusterNodes, topologyRefreshPeriod);
      this.initializeSlotsCache(clusterNodes, clientConfig);
   }

   private void initializeSlotsCache(Set<HostAndPort> startNodes, JedisClientConfig clientConfig) {
      if (startNodes.isEmpty()) {
         throw new JedisClusterOperationException("No nodes to initialize cluster slots cache.");
      }

      ArrayList<HostAndPort> startNodeList = new ArrayList<>(startNodes);
      Collections.shuffle(startNodeList);
      JedisException firstException = null;

      for (HostAndPort hostAndPort : startNodeList) {
         try {
            try (Connection jedis = new Connection(hostAndPort, clientConfig)) {
               this.cache.discoverClusterNodesAndSlots(jedis);
            }

            return;
         } catch (JedisException e) {
            if (firstException == null) {
               firstException = e;
            }
         }
      }

      if (System.getProperty("jedis.cluster.initNoError") == null) {
         JedisClusterOperationException uninitializedException = new JedisClusterOperationException("Could not initialize cluster slots cache.");
         uninitializedException.addSuppressed(firstException);
         throw uninitializedException;
      }
   }

   @Override
   public void close() {
      this.cache.close();
   }

   public void renewSlotCache() {
      this.cache.renewClusterSlots(null);
   }

   public void renewSlotCache(Connection jedis) {
      this.cache.renewClusterSlots(jedis);
   }

   public Map<String, ConnectionPool> getNodes() {
      return this.cache.getNodes();
   }

   public HostAndPort getNode(int slot) {
      return slot >= 0 ? this.cache.getSlotNode(slot) : null;
   }

   public Connection getConnection(HostAndPort node) {
      return node != null ? this.cache.setupNodeIfNotExist(node).getResource() : this.getConnection();
   }

   @Override
   public Connection getConnection(CommandArguments args) {
      int slot = ((ClusterCommandArguments)args).getCommandHashSlot();
      return slot >= 0 ? this.getConnectionFromSlot(slot) : this.getConnection();
   }

   public Connection getReplicaConnection(CommandArguments args) {
      int slot = ((ClusterCommandArguments)args).getCommandHashSlot();
      return slot >= 0 ? this.getReplicaConnectionFromSlot(slot) : this.getConnection();
   }

   @Override
   public Connection getConnection() {
      List<ConnectionPool> pools = this.cache.getShuffledNodesPool();
      JedisException suppressed = null;

      for (ConnectionPool pool : pools) {
         Connection jedis = null;

         try {
            jedis = pool.getResource();
            if (jedis != null) {
               jedis.ping();
               return jedis;
            }
         } catch (JedisException ex) {
            if (suppressed == null) {
               suppressed = ex;
            }

            if (jedis != null) {
               jedis.close();
            }
         }
      }

      JedisClusterOperationException noReachableNode = new JedisClusterOperationException("No reachable node in cluster.");
      if (suppressed != null) {
         noReachableNode.addSuppressed(suppressed);
      }

      throw noReachableNode;
   }

   public Connection getConnectionFromSlot(int slot) {
      ConnectionPool connectionPool = this.cache.getSlotPool(slot);
      if (connectionPool != null) {
         return connectionPool.getResource();
      }

      this.renewSlotCache();
      connectionPool = this.cache.getSlotPool(slot);
      return connectionPool != null ? connectionPool.getResource() : this.getConnection();
   }

   public Connection getReplicaConnectionFromSlot(int slot) {
      List<ConnectionPool> connectionPools = this.cache.getSlotReplicaPools(slot);
      ThreadLocalRandom random = ThreadLocalRandom.current();
      if (connectionPools != null && !connectionPools.isEmpty()) {
         int idx = random.nextInt(connectionPools.size());
         return connectionPools.get(idx).getResource();
      } else {
         this.renewSlotCache();
         connectionPools = this.cache.getSlotReplicaPools(slot);
         if (connectionPools != null && !connectionPools.isEmpty()) {
            int idx = random.nextInt(connectionPools.size());
            return connectionPools.get(idx).getResource();
         } else {
            return this.getConnectionFromSlot(slot);
         }
      }
   }

   @Override
   public Map<String, ConnectionPool> getConnectionMap() {
      return Collections.unmodifiableMap(this.getNodes());
   }
}
