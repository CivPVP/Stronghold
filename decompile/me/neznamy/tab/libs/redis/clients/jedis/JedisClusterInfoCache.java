package me.neznamy.tab.libs.redis.clients.jedis;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Experimental;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Internal;
import me.neznamy.tab.libs.redis.clients.jedis.csc.Cache;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisClusterOperationException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public class JedisClusterInfoCache {
   private static final Logger logger = LoggerFactory.getLogger(JedisClusterInfoCache.class);
   private final Map<String, ConnectionPool> nodes = new HashMap<>();
   private final ConnectionPool[] slots = new ConnectionPool[16384];
   private final HostAndPort[] slotNodes = new HostAndPort[16384];
   private final List<ConnectionPool>[] replicaSlots;
   private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
   private final Lock r = this.rwl.readLock();
   private final Lock w = this.rwl.writeLock();
   private final Lock rediscoverLock = new ReentrantLock();
   private final GenericObjectPoolConfig<Connection> poolConfig;
   private final JedisClientConfig clientConfig;
   private final Cache clientSideCache;
   private final Set<HostAndPort> startNodes;
   private static final int MASTER_NODE_INDEX = 2;
   private ScheduledExecutorService topologyRefreshExecutor = null;

   public JedisClusterInfoCache(JedisClientConfig clientConfig, Set<HostAndPort> startNodes) {
      this(clientConfig, null, null, startNodes);
   }

   @Experimental
   public JedisClusterInfoCache(JedisClientConfig clientConfig, Cache clientSideCache, Set<HostAndPort> startNodes) {
      this(clientConfig, clientSideCache, null, startNodes);
   }

   public JedisClusterInfoCache(JedisClientConfig clientConfig, GenericObjectPoolConfig<Connection> poolConfig, Set<HostAndPort> startNodes) {
      this(clientConfig, null, poolConfig, startNodes);
   }

   @Experimental
   public JedisClusterInfoCache(
      JedisClientConfig clientConfig, Cache clientSideCache, GenericObjectPoolConfig<Connection> poolConfig, Set<HostAndPort> startNodes
   ) {
      this(clientConfig, clientSideCache, poolConfig, startNodes, null);
   }

   public JedisClusterInfoCache(
      JedisClientConfig clientConfig, GenericObjectPoolConfig<Connection> poolConfig, Set<HostAndPort> startNodes, Duration topologyRefreshPeriod
   ) {
      this(clientConfig, null, poolConfig, startNodes, topologyRefreshPeriod);
   }

   @Experimental
   public JedisClusterInfoCache(
      JedisClientConfig clientConfig,
      Cache clientSideCache,
      GenericObjectPoolConfig<Connection> poolConfig,
      Set<HostAndPort> startNodes,
      Duration topologyRefreshPeriod
   ) {
      this.poolConfig = poolConfig;
      this.clientConfig = clientConfig;
      this.clientSideCache = clientSideCache;
      this.startNodes = startNodes;
      if (topologyRefreshPeriod != null) {
         logger.info("Cluster topology refresh start, period: {}, startNodes: {}", topologyRefreshPeriod, startNodes);
         this.topologyRefreshExecutor = Executors.newSingleThreadScheduledExecutor();
         this.topologyRefreshExecutor
            .scheduleWithFixedDelay(
               new JedisClusterInfoCache.TopologyRefreshTask(), topologyRefreshPeriod.toMillis(), topologyRefreshPeriod.toMillis(), TimeUnit.MILLISECONDS
            );
      }

      if (clientConfig.isReadOnlyForRedisClusterReplicas()) {
         this.replicaSlots = new ArrayList[16384];
      } else {
         this.replicaSlots = null;
      }
   }

   private boolean checkClusterSlotSequence(List<Object> slotsInfo) {
      List<Integer> slots = new ArrayList<>();

      for (Object slotInfoObj : slotsInfo) {
         List<Object> slotInfo = (List<Object>)slotInfoObj;
         slots.addAll(this.getAssignedSlotArray(slotInfo));
      }

      Collections.sort(slots);
      if (slots.size() != 16384) {
         return false;
      }

      for (int i = 0; i < 16384; i++) {
         if (i != slots.get(i)) {
            return false;
         }
      }

      return true;
   }

   public void discoverClusterNodesAndSlots(Connection jedis) {
      List<Object> slotsInfo = this.executeClusterSlots(jedis);
      if (System.getProperty("jedis.cluster.initNoError") == null) {
         if (slotsInfo.isEmpty()) {
            throw new JedisClusterOperationException("Cluster slots list is empty.");
         }

         if (!this.checkClusterSlotSequence(slotsInfo)) {
            throw new JedisClusterOperationException("Cluster slots have holes.");
         }
      }

      this.w.lock();

      try {
         this.reset();

         for (Object slotInfoObj : slotsInfo) {
            List<Object> slotInfo = (List<Object>)slotInfoObj;
            if (slotInfo.size() > 2) {
               List<Integer> slotNums = this.getAssignedSlotArray(slotInfo);
               int size = slotInfo.size();

               for (int i = 2; i < size; i++) {
                  List<Object> hostInfos = (List<Object>)slotInfo.get(i);
                  if (!hostInfos.isEmpty()) {
                     HostAndPort targetNode = this.generateHostAndPort(hostInfos);
                     this.setupNodeIfNotExist(targetNode);
                     if (i == 2) {
                        this.assignSlotsToNode(slotNums, targetNode);
                     } else if (this.clientConfig.isReadOnlyForRedisClusterReplicas()) {
                        this.assignSlotsToReplicaNode(slotNums, targetNode);
                     }
                  }
               }
            }
         }
      } finally {
         this.w.unlock();
      }
   }

   public void renewClusterSlots(Connection jedis) {
      if (this.rediscoverLock.tryLock()) {
         try {
            if (jedis != null) {
               try {
                  this.discoverClusterSlots(jedis);
                  return;
               } catch (JedisException var56) {
               }
            }

            if (this.startNodes != null) {
               for (HostAndPort hostAndPort : this.startNodes) {
                  try (Connection j = new Connection(hostAndPort, this.clientConfig)) {
                     this.discoverClusterSlots(j);
                     return;
                  } catch (JedisException var57) {
                  }
               }
            }

            for (ConnectionPool jp : this.getShuffledNodesPool()) {
               try (Connection j = jp.getResource()) {
                  if (this.startNodes == null || !this.startNodes.contains(j.getHostAndPort())) {
                     this.discoverClusterSlots(j);
                     return;
                  }
               } catch (JedisException var55) {
               }
            }
         } finally {
            this.rediscoverLock.unlock();
         }
      }
   }

   private void discoverClusterSlots(Connection jedis) {
      List<Object> slotsInfo = this.executeClusterSlots(jedis);
      if (System.getProperty("jedis.cluster.initNoError") == null) {
         if (slotsInfo.isEmpty()) {
            throw new JedisClusterOperationException("Cluster slots list is empty.");
         }

         if (!this.checkClusterSlotSequence(slotsInfo)) {
            throw new JedisClusterOperationException("Cluster slots have holes.");
         }
      }

      this.w.lock();

      try {
         Arrays.fill(this.slots, null);
         Arrays.fill(this.slotNodes, null);
         if (this.clientSideCache != null) {
            this.clientSideCache.flush();
         }

         Set<String> hostAndPortKeys = new HashSet<>();

         for (Object slotInfoObj : slotsInfo) {
            List<Object> slotInfo = (List<Object>)slotInfoObj;
            if (slotInfo.size() > 2) {
               List<Integer> slotNums = this.getAssignedSlotArray(slotInfo);
               int size = slotInfo.size();

               for (int i = 2; i < size; i++) {
                  List<Object> hostInfos = (List<Object>)slotInfo.get(i);
                  if (!hostInfos.isEmpty()) {
                     HostAndPort targetNode = this.generateHostAndPort(hostInfos);
                     hostAndPortKeys.add(getNodeKey(targetNode));
                     this.setupNodeIfNotExist(targetNode);
                     if (i == 2) {
                        this.assignSlotsToNode(slotNums, targetNode);
                     } else if (this.clientConfig.isReadOnlyForRedisClusterReplicas()) {
                        this.assignSlotsToReplicaNode(slotNums, targetNode);
                     }
                  }
               }
            }
         }

         Iterator<Entry<String, ConnectionPool>> entryIt = this.nodes.entrySet().iterator();

         while (entryIt.hasNext()) {
            Entry<String, ConnectionPool> entry = entryIt.next();
            if (!hostAndPortKeys.contains(entry.getKey())) {
               ConnectionPool pool = entry.getValue();

               try {
                  if (pool != null) {
                     pool.destroy();
                  }
               } catch (Exception var15) {
               }

               entryIt.remove();
            }
         }
      } finally {
         this.w.unlock();
      }
   }

   private HostAndPort generateHostAndPort(List<Object> hostInfos) {
      String host = SafeEncoder.encode((byte[])hostInfos.get(0));
      int port = ((Long)hostInfos.get(1)).intValue();
      return new HostAndPort(host, port);
   }

   public ConnectionPool setupNodeIfNotExist(HostAndPort node) {
      this.w.lock();

      try {
         String nodeKey = getNodeKey(node);
         ConnectionPool existingPool = this.nodes.get(nodeKey);
         if (existingPool != null) {
            return existingPool;
         }

         ConnectionPool nodePool = this.createNodePool(node);
         this.nodes.put(nodeKey, nodePool);
         return nodePool;
      } finally {
         this.w.unlock();
      }
   }

   private ConnectionPool createNodePool(HostAndPort node) {
      if (this.poolConfig == null) {
         return this.clientSideCache == null ? new ConnectionPool(node, this.clientConfig) : new ConnectionPool(node, this.clientConfig, this.clientSideCache);
      } else {
         return this.clientSideCache == null
            ? new ConnectionPool(node, this.clientConfig, this.poolConfig)
            : new ConnectionPool(node, this.clientConfig, this.clientSideCache, this.poolConfig);
      }
   }

   public void assignSlotToNode(int slot, HostAndPort targetNode) {
      this.w.lock();

      try {
         ConnectionPool targetPool = this.setupNodeIfNotExist(targetNode);
         this.slots[slot] = targetPool;
         this.slotNodes[slot] = targetNode;
      } finally {
         this.w.unlock();
      }
   }

   public void assignSlotsToNode(List<Integer> targetSlots, HostAndPort targetNode) {
      this.w.lock();

      try {
         ConnectionPool targetPool = this.setupNodeIfNotExist(targetNode);

         for (Integer slot : targetSlots) {
            this.slots[slot] = targetPool;
            this.slotNodes[slot] = targetNode;
         }
      } finally {
         this.w.unlock();
      }
   }

   public void assignSlotsToReplicaNode(List<Integer> targetSlots, HostAndPort targetNode) {
      this.w.lock();

      try {
         ConnectionPool targetPool = this.setupNodeIfNotExist(targetNode);

         for (Integer slot : targetSlots) {
            if (this.replicaSlots[slot] == null) {
               this.replicaSlots[slot] = new ArrayList<>();
            }

            this.replicaSlots[slot].add(targetPool);
         }
      } finally {
         this.w.unlock();
      }
   }

   public ConnectionPool getNode(String nodeKey) {
      this.r.lock();

      try {
         return this.nodes.get(nodeKey);
      } finally {
         this.r.unlock();
      }
   }

   public ConnectionPool getNode(HostAndPort node) {
      return this.getNode(getNodeKey(node));
   }

   public ConnectionPool getSlotPool(int slot) {
      this.r.lock();

      try {
         return this.slots[slot];
      } finally {
         this.r.unlock();
      }
   }

   public HostAndPort getSlotNode(int slot) {
      this.r.lock();

      try {
         return this.slotNodes[slot];
      } finally {
         this.r.unlock();
      }
   }

   public List<ConnectionPool> getSlotReplicaPools(int slot) {
      this.r.lock();

      try {
         return this.replicaSlots[slot];
      } finally {
         this.r.unlock();
      }
   }

   public Map<String, ConnectionPool> getNodes() {
      this.r.lock();

      try {
         return new HashMap<>(this.nodes);
      } finally {
         this.r.unlock();
      }
   }

   public List<ConnectionPool> getShuffledNodesPool() {
      this.r.lock();

      try {
         List<ConnectionPool> pools = new ArrayList<>(this.nodes.values());
         Collections.shuffle(pools);
         return pools;
      } finally {
         this.r.unlock();
      }
   }

   public void reset() {
      this.w.lock();

      try {
         for (ConnectionPool pool : this.nodes.values()) {
            try {
               if (pool != null) {
                  pool.destroy();
               }
            } catch (RuntimeException var7) {
            }
         }

         this.nodes.clear();
         Arrays.fill(this.slots, null);
         Arrays.fill(this.slotNodes, null);
      } finally {
         this.w.unlock();
      }
   }

   public void close() {
      this.reset();
      if (this.topologyRefreshExecutor != null) {
         logger.info("Cluster topology refresh shutdown, startNodes: {}", this.startNodes);
         this.topologyRefreshExecutor.shutdownNow();
      }
   }

   public static String getNodeKey(HostAndPort hnp) {
      return hnp.toString();
   }

   private List<Object> executeClusterSlots(Connection jedis) {
      jedis.sendCommand(Protocol.Command.CLUSTER, "SLOTS");
      return jedis.getObjectMultiBulkReply();
   }

   private List<Integer> getAssignedSlotArray(List<Object> slotInfo) {
      List<Integer> slotNums = new ArrayList<>();

      for (int slot = ((Long)slotInfo.get(0)).intValue(); slot <= ((Long)slotInfo.get(1)).intValue(); slot++) {
         slotNums.add(slot);
      }

      return slotNums;
   }

   class TopologyRefreshTask implements Runnable {
      @Override
      public void run() {
         JedisClusterInfoCache.logger.debug("Cluster topology refresh run, old nodes: {}", JedisClusterInfoCache.this.nodes.keySet());
         JedisClusterInfoCache.this.renewClusterSlots(null);
         JedisClusterInfoCache.logger.debug("Cluster topology refresh run, new nodes: {}", JedisClusterInfoCache.this.nodes.keySet());
      }
   }
}
