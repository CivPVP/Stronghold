package me.neznamy.tab.libs.redis.clients.jedis.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Connection;
import me.neznamy.tab.libs.redis.clients.jedis.ConnectionPool;
import me.neznamy.tab.libs.redis.clients.jedis.HostAndPort;
import me.neznamy.tab.libs.redis.clients.jedis.Jedis;
import me.neznamy.tab.libs.redis.clients.jedis.JedisClientConfig;
import me.neznamy.tab.libs.redis.clients.jedis.JedisPubSub;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Experimental;
import me.neznamy.tab.libs.redis.clients.jedis.csc.Cache;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisConnectionException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;
import me.neznamy.tab.libs.redis.clients.jedis.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SentineledConnectionProvider implements ConnectionProvider {
   private static final Logger LOG = LoggerFactory.getLogger(SentineledConnectionProvider.class);
   protected static final long DEFAULT_SUBSCRIBE_RETRY_WAIT_TIME_MILLIS = 5000L;
   private volatile HostAndPort currentMaster;
   private volatile ConnectionPool pool;
   private final String masterName;
   private final JedisClientConfig masterClientConfig;
   private final Cache clientSideCache;
   private final GenericObjectPoolConfig<Connection> masterPoolConfig;
   protected final Collection<SentineledConnectionProvider.SentinelListener> sentinelListeners = new ArrayList<>();
   private final JedisClientConfig sentinelClientConfig;
   private final long subscribeRetryWaitTimeMillis;
   private final Lock initPoolLock = new ReentrantLock(true);

   public SentineledConnectionProvider(
      String masterName, JedisClientConfig masterClientConfig, Set<HostAndPort> sentinels, JedisClientConfig sentinelClientConfig
   ) {
      this(masterName, masterClientConfig, null, null, sentinels, sentinelClientConfig);
   }

   @Experimental
   public SentineledConnectionProvider(
      String masterName, JedisClientConfig masterClientConfig, Cache clientSideCache, Set<HostAndPort> sentinels, JedisClientConfig sentinelClientConfig
   ) {
      this(masterName, masterClientConfig, clientSideCache, null, sentinels, sentinelClientConfig);
   }

   public SentineledConnectionProvider(
      String masterName,
      JedisClientConfig masterClientConfig,
      GenericObjectPoolConfig<Connection> poolConfig,
      Set<HostAndPort> sentinels,
      JedisClientConfig sentinelClientConfig
   ) {
      this(masterName, masterClientConfig, poolConfig, sentinels, sentinelClientConfig, 5000L);
   }

   @Experimental
   public SentineledConnectionProvider(
      String masterName,
      JedisClientConfig masterClientConfig,
      Cache clientSideCache,
      GenericObjectPoolConfig<Connection> poolConfig,
      Set<HostAndPort> sentinels,
      JedisClientConfig sentinelClientConfig
   ) {
      this(masterName, masterClientConfig, clientSideCache, poolConfig, sentinels, sentinelClientConfig, 5000L);
   }

   public SentineledConnectionProvider(
      String masterName,
      JedisClientConfig masterClientConfig,
      GenericObjectPoolConfig<Connection> poolConfig,
      Set<HostAndPort> sentinels,
      JedisClientConfig sentinelClientConfig,
      long subscribeRetryWaitTimeMillis
   ) {
      this(masterName, masterClientConfig, null, poolConfig, sentinels, sentinelClientConfig, subscribeRetryWaitTimeMillis);
   }

   @Experimental
   public SentineledConnectionProvider(
      String masterName,
      JedisClientConfig masterClientConfig,
      Cache clientSideCache,
      GenericObjectPoolConfig<Connection> poolConfig,
      Set<HostAndPort> sentinels,
      JedisClientConfig sentinelClientConfig,
      long subscribeRetryWaitTimeMillis
   ) {
      this.masterName = masterName;
      this.masterClientConfig = masterClientConfig;
      this.clientSideCache = clientSideCache;
      this.masterPoolConfig = poolConfig;
      this.sentinelClientConfig = sentinelClientConfig;
      this.subscribeRetryWaitTimeMillis = subscribeRetryWaitTimeMillis;
      HostAndPort master = this.initSentinels(sentinels);
      this.initMaster(master);
   }

   @Override
   public Connection getConnection() {
      return this.pool.getResource();
   }

   @Override
   public Connection getConnection(CommandArguments args) {
      return this.pool.getResource();
   }

   @Override
   public void close() {
      this.sentinelListeners.forEach(SentineledConnectionProvider.SentinelListener::shutdown);
      this.pool.close();
   }

   public HostAndPort getCurrentMaster() {
      return this.currentMaster;
   }

   private void initMaster(HostAndPort master) {
      this.initPoolLock.lock();

      try {
         if (!master.equals(this.currentMaster)) {
            this.currentMaster = master;
            ConnectionPool newPool = this.createNodePool(this.currentMaster);
            ConnectionPool existingPool = this.pool;
            this.pool = newPool;
            LOG.info("Created connection pool to master at {}.", master);
            if (this.clientSideCache != null) {
               this.clientSideCache.flush();
            }

            if (existingPool != null) {
               existingPool.close();
            }
         }
      } finally {
         this.initPoolLock.unlock();
      }
   }

   private ConnectionPool createNodePool(HostAndPort master) {
      if (this.masterPoolConfig == null) {
         return this.clientSideCache == null
            ? new ConnectionPool(master, this.masterClientConfig)
            : new ConnectionPool(master, this.masterClientConfig, this.clientSideCache);
      } else {
         return this.clientSideCache == null
            ? new ConnectionPool(master, this.masterClientConfig, this.masterPoolConfig)
            : new ConnectionPool(master, this.masterClientConfig, this.clientSideCache, this.masterPoolConfig);
      }
   }

   private HostAndPort initSentinels(Set<HostAndPort> sentinels) {
      HostAndPort master = null;
      boolean sentinelAvailable = false;
      LOG.debug("Trying to find master from available sentinels...");

      for (HostAndPort sentinel : sentinels) {
         LOG.debug("Connecting to Sentinel {}...", sentinel);

         try (Jedis jedis = new Jedis(sentinel, this.sentinelClientConfig)) {
            List<String> masterAddr = jedis.sentinelGetMasterAddrByName(this.masterName);
            sentinelAvailable = true;
            if (masterAddr != null && masterAddr.size() == 2) {
               master = toHostAndPort(masterAddr);
               LOG.debug("Redis master reported at {}.", master);
               break;
            }

            LOG.warn("Sentinel {} is not monitoring master {}.", sentinel, this.masterName);
         } catch (JedisException e) {
            LOG.warn("Could not get master address from {}.", sentinel, e);
         }
      }

      if (master == null) {
         if (sentinelAvailable) {
            throw new JedisException("Can connect to sentinel, but " + this.masterName + " seems to be not monitored.");
         } else {
            throw new JedisConnectionException("All sentinels down, cannot determine where " + this.masterName + " is running.");
         }
      } else {
         LOG.info("Redis master running at {}. Starting sentinel listeners...", master);

         for (HostAndPort sentinel : sentinels) {
            SentineledConnectionProvider.SentinelListener listener = new SentineledConnectionProvider.SentinelListener(sentinel);
            listener.setDaemon(true);
            this.sentinelListeners.add(listener);
            listener.start();
         }

         return master;
      }
   }

   private static HostAndPort toHostAndPort(List<String> masterAddr) {
      return toHostAndPort(masterAddr.get(0), masterAddr.get(1));
   }

   private static HostAndPort toHostAndPort(String hostStr, String portStr) {
      return new HostAndPort(hostStr, Integer.parseInt(portStr));
   }

   protected class SentinelListener extends Thread {
      protected final HostAndPort node;
      protected volatile Jedis sentinelJedis;
      protected AtomicBoolean running = new AtomicBoolean(false);

      public SentinelListener(HostAndPort node) {
         super(String.format("%s-SentinelListener-[%s]", SentineledConnectionProvider.this.masterName, node.toString()));
         this.node = node;
      }

      @Override
      public void run() {
         this.running.set(true);

         while (this.running.get()) {
            try {
               if (!this.running.get()) {
                  break;
               }

               this.sentinelJedis = new Jedis(this.node, SentineledConnectionProvider.this.sentinelClientConfig);
               List<String> masterAddr = this.sentinelJedis.sentinelGetMasterAddrByName(SentineledConnectionProvider.this.masterName);
               if (masterAddr != null && masterAddr.size() == 2) {
                  SentineledConnectionProvider.this.initMaster(SentineledConnectionProvider.toHostAndPort(masterAddr));
               } else {
                  SentineledConnectionProvider.LOG
                     .warn("Can not get master {} address. Sentinel: {}.", SentineledConnectionProvider.this.masterName, this.node);
               }

               this.sentinelJedis
                  .subscribe(
                     new JedisPubSub() {
                        public void onMessage(String channel, String message) {
                           SentineledConnectionProvider.LOG.debug("Sentinel {} published: {}.", SentinelListener.this.node, message);
                           String[] switchMasterMsg = message.split(" ");
                           if (switchMasterMsg.length > 3) {
                              if (SentineledConnectionProvider.this.masterName.equals(switchMasterMsg[0])) {
                                 SentineledConnectionProvider.this.initMaster(
                                    SentineledConnectionProvider.toHostAndPort(switchMasterMsg[3], switchMasterMsg[4])
                                 );
                              } else {
                                 SentineledConnectionProvider.LOG
                                    .debug(
                                       "Ignoring message on +switch-master for master {}. Our master is {}.",
                                       switchMasterMsg[0],
                                       SentineledConnectionProvider.this.masterName
                                    );
                              }
                           } else {
                              SentineledConnectionProvider.LOG
                                 .error("Invalid message received on sentinel {} on channel +switch-master: {}.", SentinelListener.this.node, message);
                           }
                        }
                     },
                     "+switch-master"
                  );
            } catch (JedisException e) {
               if (this.running.get()) {
                  SentineledConnectionProvider.LOG
                     .error(
                        "Lost connection to sentinel {}. Sleeping {}ms and retrying.",
                        new Object[]{this.node, SentineledConnectionProvider.this.subscribeRetryWaitTimeMillis, e}
                     );

                  try {
                     Thread.sleep(SentineledConnectionProvider.this.subscribeRetryWaitTimeMillis);
                  } catch (InterruptedException se) {
                     SentineledConnectionProvider.LOG.error("Sleep interrupted.", se);
                  }
               } else {
                  SentineledConnectionProvider.LOG.debug("Unsubscribing from sentinel {}.", this.node);
               }
            } finally {
               IOUtils.closeQuietly(this.sentinelJedis);
            }
         }
      }

      public void shutdown() {
         try {
            SentineledConnectionProvider.LOG.debug("Shutting down listener on {}.", this.node);
            this.running.set(false);
            if (this.sentinelJedis != null) {
               this.sentinelJedis.close();
            }
         } catch (RuntimeException e) {
            SentineledConnectionProvider.LOG.error("Error while shutting down.", e);
         }
      }
   }
}
