package me.neznamy.tab.libs.redis.clients.jedis.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Connection;
import me.neznamy.tab.libs.redis.clients.jedis.ConnectionPool;
import me.neznamy.tab.libs.redis.clients.jedis.DefaultJedisClientConfig;
import me.neznamy.tab.libs.redis.clients.jedis.HostAndPort;
import me.neznamy.tab.libs.redis.clients.jedis.JedisClientConfig;
import me.neznamy.tab.libs.redis.clients.jedis.ShardedCommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;
import me.neznamy.tab.libs.redis.clients.jedis.util.Hashing;

@Deprecated
public class ShardedConnectionProvider implements ConnectionProvider {
   private final TreeMap<Long, HostAndPort> nodes = new TreeMap<>();
   private final Map<String, ConnectionPool> resources = new HashMap<>();
   private final JedisClientConfig clientConfig;
   private final GenericObjectPoolConfig<Connection> poolConfig;
   private final Hashing algo;

   public ShardedConnectionProvider(List<HostAndPort> shards) {
      this(shards, DefaultJedisClientConfig.builder().build());
   }

   public ShardedConnectionProvider(List<HostAndPort> shards, JedisClientConfig clientConfig) {
      this(shards, clientConfig, new GenericObjectPoolConfig<>());
   }

   public ShardedConnectionProvider(List<HostAndPort> shards, JedisClientConfig clientConfig, GenericObjectPoolConfig<Connection> poolConfig) {
      this(shards, clientConfig, poolConfig, Hashing.MURMUR_HASH);
   }

   public ShardedConnectionProvider(List<HostAndPort> shards, JedisClientConfig clientConfig, Hashing algo) {
      this(shards, clientConfig, null, algo);
   }

   public ShardedConnectionProvider(List<HostAndPort> shards, JedisClientConfig clientConfig, GenericObjectPoolConfig<Connection> poolConfig, Hashing algo) {
      this.clientConfig = clientConfig;
      this.poolConfig = poolConfig;
      this.algo = algo;
      this.initialize(shards);
   }

   private void initialize(List<HostAndPort> shards) {
      for (int i = 0; i < shards.size(); i++) {
         HostAndPort shard = shards.get(i);

         for (int n = 0; n < 160; n++) {
            Long hash = this.algo.hash("SHARD-" + i + "-NODE-" + n);
            this.nodes.put(hash, shard);
            this.setupNodeIfNotExist(shard);
         }
      }
   }

   private ConnectionPool setupNodeIfNotExist(HostAndPort node) {
      String nodeKey = node.toString();
      ConnectionPool existingPool = this.resources.get(nodeKey);
      if (existingPool != null) {
         return existingPool;
      }

      ConnectionPool nodePool = this.poolConfig == null
         ? new ConnectionPool(node, this.clientConfig)
         : new ConnectionPool(node, this.clientConfig, this.poolConfig);
      this.resources.put(nodeKey, nodePool);
      return nodePool;
   }

   public Hashing getHashingAlgo() {
      return this.algo;
   }

   private void reset() {
      for (ConnectionPool pool : this.resources.values()) {
         try {
            if (pool != null) {
               pool.destroy();
            }
         } catch (RuntimeException var4) {
         }
      }

      this.resources.clear();
      this.nodes.clear();
   }

   @Override
   public void close() {
      this.reset();
   }

   public HostAndPort getNode(Long hash) {
      return hash != null ? this.getNodeFromHash(hash) : null;
   }

   public Connection getConnection(HostAndPort node) {
      return node != null ? this.setupNodeIfNotExist(node).getResource() : this.getConnection();
   }

   @Override
   public Connection getConnection(CommandArguments args) {
      Long hash = ((ShardedCommandArguments)args).getKeyHash();
      return hash != null ? this.getConnection(this.getNodeFromHash(hash)) : this.getConnection();
   }

   private List<ConnectionPool> getShuffledNodesPool() {
      List<ConnectionPool> pools = new ArrayList<>(this.resources.values());
      Collections.shuffle(pools);
      return pools;
   }

   @Override
   public Connection getConnection() {
      List<ConnectionPool> pools = this.getShuffledNodesPool();
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

      JedisException noReachableNode = new JedisException("No reachable shard.");
      if (suppressed != null) {
         noReachableNode.addSuppressed(suppressed);
      }

      throw noReachableNode;
   }

   private HostAndPort getNodeFromHash(Long hash) {
      SortedMap<Long, HostAndPort> tail = this.nodes.tailMap(hash);
      return tail.isEmpty() ? this.nodes.get(this.nodes.firstKey()) : tail.get(tail.firstKey());
   }

   @Override
   public Map<String, ConnectionPool> getConnectionMap() {
      return Collections.unmodifiableMap(this.resources);
   }
}
