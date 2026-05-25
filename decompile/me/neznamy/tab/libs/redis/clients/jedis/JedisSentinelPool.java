package me.neznamy.tab.libs.redis.clients.jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisConnectionException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;
import me.neznamy.tab.libs.redis.clients.jedis.util.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JedisSentinelPool extends Pool<Jedis> {
   private static final Logger LOG = LoggerFactory.getLogger(JedisSentinelPool.class);
   private final JedisFactory factory;
   private final JedisClientConfig sentinelClientConfig;
   protected final Collection<JedisSentinelPool.MasterListener> masterListeners = new ArrayList<>();
   private volatile HostAndPort currentHostMaster;
   private final Lock initPoolLock = new ReentrantLock(true);

   public JedisSentinelPool(String masterName, Set<HostAndPort> sentinels, JedisClientConfig masterClientConfig, JedisClientConfig sentinelClientConfig) {
      this(masterName, sentinels, new JedisFactory(masterClientConfig), sentinelClientConfig);
   }

   public JedisSentinelPool(String masterName, Set<String> sentinels, GenericObjectPoolConfig<Jedis> poolConfig) {
      this(masterName, sentinels, poolConfig, 2000, null, 0);
   }

   public JedisSentinelPool(String masterName, Set<String> sentinels) {
      this(masterName, sentinels, new GenericObjectPoolConfig<>(), 2000, null, 0);
   }

   public JedisSentinelPool(String masterName, Set<String> sentinels, String password) {
      this(masterName, sentinels, new GenericObjectPoolConfig<>(), 2000, password);
   }

   public JedisSentinelPool(String masterName, Set<String> sentinels, String password, String sentinelPassword) {
      this(masterName, sentinels, new GenericObjectPoolConfig<>(), 2000, 2000, password, 0, null, 2000, 2000, sentinelPassword, null);
   }

   public JedisSentinelPool(String masterName, Set<String> sentinels, GenericObjectPoolConfig<Jedis> poolConfig, int timeout, String password) {
      this(masterName, sentinels, poolConfig, timeout, password, 0);
   }

   public JedisSentinelPool(String masterName, Set<String> sentinels, GenericObjectPoolConfig<Jedis> poolConfig, int timeout) {
      this(masterName, sentinels, poolConfig, timeout, null, 0);
   }

   public JedisSentinelPool(String masterName, Set<String> sentinels, GenericObjectPoolConfig<Jedis> poolConfig, String password) {
      this(masterName, sentinels, poolConfig, 2000, password);
   }

   public JedisSentinelPool(String masterName, Set<String> sentinels, GenericObjectPoolConfig<Jedis> poolConfig, int timeout, String password, int database) {
      this(masterName, sentinels, poolConfig, timeout, timeout, null, password, database);
   }

   public JedisSentinelPool(
      String masterName, Set<String> sentinels, GenericObjectPoolConfig<Jedis> poolConfig, int timeout, String user, String password, int database
   ) {
      this(masterName, sentinels, poolConfig, timeout, timeout, user, password, database);
   }

   public JedisSentinelPool(
      String masterName, Set<String> sentinels, GenericObjectPoolConfig<Jedis> poolConfig, int timeout, String password, int database, String clientName
   ) {
      this(masterName, sentinels, poolConfig, timeout, timeout, password, database, clientName);
   }

   public JedisSentinelPool(
      String masterName,
      Set<String> sentinels,
      GenericObjectPoolConfig<Jedis> poolConfig,
      int timeout,
      String user,
      String password,
      int database,
      String clientName
   ) {
      this(masterName, sentinels, poolConfig, timeout, timeout, user, password, database, clientName);
   }

   public JedisSentinelPool(
      String masterName, Set<String> sentinels, GenericObjectPoolConfig<Jedis> poolConfig, int connectionTimeout, int soTimeout, String password, int database
   ) {
      this(masterName, sentinels, poolConfig, connectionTimeout, soTimeout, null, password, database, null);
   }

   public JedisSentinelPool(
      String masterName,
      Set<String> sentinels,
      GenericObjectPoolConfig<Jedis> poolConfig,
      int connectionTimeout,
      int soTimeout,
      String user,
      String password,
      int database
   ) {
      this(masterName, sentinels, poolConfig, connectionTimeout, soTimeout, user, password, database, null);
   }

   public JedisSentinelPool(
      String masterName,
      Set<String> sentinels,
      GenericObjectPoolConfig<Jedis> poolConfig,
      int connectionTimeout,
      int soTimeout,
      String password,
      int database,
      String clientName
   ) {
      this(masterName, sentinels, poolConfig, connectionTimeout, soTimeout, null, password, database, clientName);
   }

   public JedisSentinelPool(
      String masterName,
      Set<String> sentinels,
      GenericObjectPoolConfig<Jedis> poolConfig,
      int connectionTimeout,
      int soTimeout,
      String user,
      String password,
      int database,
      String clientName
   ) {
      this(masterName, sentinels, poolConfig, connectionTimeout, soTimeout, user, password, database, clientName, 2000, 2000, null, null, null);
   }

   public JedisSentinelPool(
      String masterName,
      Set<String> sentinels,
      GenericObjectPoolConfig<Jedis> poolConfig,
      int connectionTimeout,
      int soTimeout,
      int infiniteSoTimeout,
      String user,
      String password,
      int database,
      String clientName
   ) {
      this(
         masterName, sentinels, poolConfig, connectionTimeout, soTimeout, infiniteSoTimeout, user, password, database, clientName, 2000, 2000, null, null, null
      );
   }

   public JedisSentinelPool(
      String masterName,
      Set<String> sentinels,
      GenericObjectPoolConfig<Jedis> poolConfig,
      int connectionTimeout,
      int soTimeout,
      String password,
      int database,
      String clientName,
      int sentinelConnectionTimeout,
      int sentinelSoTimeout,
      String sentinelPassword,
      String sentinelClientName
   ) {
      this(
         masterName,
         sentinels,
         poolConfig,
         connectionTimeout,
         soTimeout,
         null,
         password,
         database,
         clientName,
         sentinelConnectionTimeout,
         sentinelSoTimeout,
         null,
         sentinelPassword,
         sentinelClientName
      );
   }

   public JedisSentinelPool(
      String masterName,
      Set<String> sentinels,
      GenericObjectPoolConfig<Jedis> poolConfig,
      int connectionTimeout,
      int soTimeout,
      String user,
      String password,
      int database,
      String clientName,
      int sentinelConnectionTimeout,
      int sentinelSoTimeout,
      String sentinelUser,
      String sentinelPassword,
      String sentinelClientName
   ) {
      this(
         masterName,
         sentinels,
         poolConfig,
         connectionTimeout,
         soTimeout,
         0,
         user,
         password,
         database,
         clientName,
         sentinelConnectionTimeout,
         sentinelSoTimeout,
         sentinelUser,
         sentinelPassword,
         sentinelClientName
      );
   }

   public JedisSentinelPool(
      String masterName,
      Set<String> sentinels,
      GenericObjectPoolConfig<Jedis> poolConfig,
      int connectionTimeout,
      int soTimeout,
      int infiniteSoTimeout,
      String user,
      String password,
      int database,
      String clientName,
      int sentinelConnectionTimeout,
      int sentinelSoTimeout,
      String sentinelUser,
      String sentinelPassword,
      String sentinelClientName
   ) {
      this(
         masterName,
         parseHostAndPorts(sentinels),
         poolConfig,
         DefaultJedisClientConfig.builder()
            .connectionTimeoutMillis(connectionTimeout)
            .socketTimeoutMillis(soTimeout)
            .blockingSocketTimeoutMillis(infiniteSoTimeout)
            .user(user)
            .password(password)
            .database(database)
            .clientName(clientName)
            .build(),
         DefaultJedisClientConfig.builder()
            .connectionTimeoutMillis(sentinelConnectionTimeout)
            .socketTimeoutMillis(sentinelSoTimeout)
            .user(sentinelUser)
            .password(sentinelPassword)
            .clientName(sentinelClientName)
            .build()
      );
   }

   public JedisSentinelPool(String masterName, Set<String> sentinels, GenericObjectPoolConfig<Jedis> poolConfig, JedisFactory factory) {
      this(masterName, parseHostAndPorts(sentinels), poolConfig, factory, DefaultJedisClientConfig.builder().build());
   }

   public JedisSentinelPool(
      String masterName,
      Set<HostAndPort> sentinels,
      GenericObjectPoolConfig<Jedis> poolConfig,
      JedisClientConfig masterClientConfig,
      JedisClientConfig sentinelClientConfig
   ) {
      this(masterName, sentinels, poolConfig, new JedisFactory(masterClientConfig), sentinelClientConfig);
   }

   public JedisSentinelPool(String masterName, Set<HostAndPort> sentinels, JedisFactory factory, JedisClientConfig sentinelClientConfig) {
      super(factory);
      this.factory = factory;
      this.sentinelClientConfig = sentinelClientConfig;
      HostAndPort master = this.initSentinels(sentinels, masterName);
      this.initMaster(master);
   }

   public JedisSentinelPool(
      String masterName, Set<HostAndPort> sentinels, GenericObjectPoolConfig<Jedis> poolConfig, JedisFactory factory, JedisClientConfig sentinelClientConfig
   ) {
      super(poolConfig, factory);
      this.factory = factory;
      this.sentinelClientConfig = sentinelClientConfig;
      HostAndPort master = this.initSentinels(sentinels, masterName);
      this.initMaster(master);
   }

   private static Set<HostAndPort> parseHostAndPorts(Set<String> strings) {
      return strings.stream().map(HostAndPort::from).collect(Collectors.toSet());
   }

   @Override
   public void destroy() {
      for (JedisSentinelPool.MasterListener m : this.masterListeners) {
         m.shutdown();
      }

      super.destroy();
   }

   public HostAndPort getCurrentHostMaster() {
      return this.currentHostMaster;
   }

   private void initMaster(HostAndPort master) {
      this.initPoolLock.lock();

      try {
         if (!master.equals(this.currentHostMaster)) {
            this.currentHostMaster = master;
            this.factory.setHostAndPort(this.currentHostMaster);
            super.clear();
            LOG.info("Created JedisSentinelPool to master at {}", master);
         }
      } finally {
         this.initPoolLock.unlock();
      }
   }

   private HostAndPort initSentinels(Set<HostAndPort> sentinels, String masterName) {
      HostAndPort master = null;
      boolean sentinelAvailable = false;
      LOG.info("Trying to find master from available Sentinels...");

      for (HostAndPort sentinel : sentinels) {
         LOG.debug("Connecting to Sentinel {}", sentinel);

         try (Jedis jedis = new Jedis(sentinel, this.sentinelClientConfig)) {
            List<String> masterAddr = jedis.sentinelGetMasterAddrByName(masterName);
            sentinelAvailable = true;
            if (masterAddr != null && masterAddr.size() == 2) {
               master = this.toHostAndPort(masterAddr);
               LOG.debug("Found Redis master at {}", master);
               break;
            }

            LOG.warn("Can not get master addr, master name: {}. Sentinel: {}", masterName, sentinel);
         } catch (JedisException e) {
            LOG.warn("Cannot get master address from sentinel running @ {}. Reason: {}. Trying next one.", sentinel, e);
         }
      }

      if (master == null) {
         if (sentinelAvailable) {
            throw new JedisException("Can connect to sentinel, but " + masterName + " seems to be not monitored...");
         } else {
            throw new JedisConnectionException("All sentinels down, cannot determine where is " + masterName + " master is running...");
         }
      } else {
         LOG.info("Redis master running at {}, starting Sentinel listeners...", master);

         for (HostAndPort sentinel : sentinels) {
            JedisSentinelPool.MasterListener masterListener = new JedisSentinelPool.MasterListener(masterName, sentinel.getHost(), sentinel.getPort());
            masterListener.setDaemon(true);
            this.masterListeners.add(masterListener);
            masterListener.start();
         }

         return master;
      }
   }

   private HostAndPort toHostAndPort(List<String> getMasterAddrByNameResult) {
      String host = getMasterAddrByNameResult.get(0);
      int port = Integer.parseInt(getMasterAddrByNameResult.get(1));
      return new HostAndPort(host, port);
   }

   public Jedis getResource() {
      while (true) {
         Jedis jedis = (Jedis)super.getResource();
         jedis.setDataSource(this);
         HostAndPort master = this.currentHostMaster;
         HostAndPort connection = jedis.getClient().getHostAndPort();
         if (master.equals(connection)) {
            return jedis;
         }

         this.returnBrokenResource(jedis);
      }
   }

   public void returnResource(Jedis resource) {
      if (resource != null) {
         try {
            resource.resetState();
            super.returnResource(resource);
         } catch (RuntimeException e) {
            this.returnBrokenResource(resource);
            LOG.debug("Resource is returned to the pool as broken", e);
         }
      }
   }

   protected class MasterListener extends Thread {
      protected String masterName;
      protected String host;
      protected int port;
      protected long subscribeRetryWaitTimeMillis = 5000L;
      protected volatile Jedis j;
      protected AtomicBoolean running = new AtomicBoolean(false);

      protected MasterListener() {
      }

      public MasterListener(String masterName, String host, int port) {
         super(String.format("MasterListener-%s-[%s:%d]", masterName, host, port));
         this.masterName = masterName;
         this.host = host;
         this.port = port;
      }

      public MasterListener(String masterName, String host, int port, long subscribeRetryWaitTimeMillis) {
         this(masterName, host, port);
         this.subscribeRetryWaitTimeMillis = subscribeRetryWaitTimeMillis;
      }

      @Override
      public void run() {
         this.running.set(true);

         while (this.running.get()) {
            try {
               if (!this.running.get()) {
                  break;
               }

               final HostAndPort hostPort = new HostAndPort(this.host, this.port);
               this.j = new Jedis(hostPort, JedisSentinelPool.this.sentinelClientConfig);
               List<String> masterAddr = this.j.sentinelGetMasterAddrByName(this.masterName);
               if (masterAddr != null && masterAddr.size() == 2) {
                  JedisSentinelPool.this.initMaster(JedisSentinelPool.this.toHostAndPort(masterAddr));
               } else {
                  JedisSentinelPool.LOG.warn("Can not get master addr, master name: {}. Sentinel: {}.", this.masterName, hostPort);
               }

               this.j
                  .subscribe(
                     new JedisPubSub() {
                        public void onMessage(String channel, String message) {
                           JedisSentinelPool.LOG.debug("Sentinel {} published: {}.", hostPort, message);
                           String[] switchMasterMsg = message.split(" ");
                           if (switchMasterMsg.length > 3) {
                              if (MasterListener.this.masterName.equals(switchMasterMsg[0])) {
                                 JedisSentinelPool.this.initMaster(JedisSentinelPool.this.toHostAndPort(Arrays.asList(switchMasterMsg[3], switchMasterMsg[4])));
                              } else {
                                 JedisSentinelPool.LOG
                                    .debug(
                                       "Ignoring message on +switch-master for master name {}, our master name is {}",
                                       switchMasterMsg[0],
                                       MasterListener.this.masterName
                                    );
                              }
                           } else {
                              JedisSentinelPool.LOG.error("Invalid message received on Sentinel {} on channel +switch-master: {}", hostPort, message);
                           }
                        }
                     },
                     "+switch-master"
                  );
            } catch (JedisException e) {
               if (this.running.get()) {
                  JedisSentinelPool.LOG.error("Lost connection to Sentinel at {}:{}. Sleeping 5000ms and retrying.", new Object[]{this.host, this.port, e});

                  try {
                     Thread.sleep(this.subscribeRetryWaitTimeMillis);
                  } catch (InterruptedException e1) {
                     JedisSentinelPool.LOG.error("Sleep interrupted: ", e1);
                  }
               } else {
                  JedisSentinelPool.LOG.debug("Unsubscribing from Sentinel at {}:{}", this.host, this.port);
               }
            } finally {
               if (this.j != null) {
                  this.j.close();
               }
            }
         }
      }

      public void shutdown() {
         try {
            JedisSentinelPool.LOG.debug("Shutting down listener on {}:{}", this.host, this.port);
            this.running.set(false);
            if (this.j != null) {
               this.j.close();
            }
         } catch (RuntimeException e) {
            JedisSentinelPool.LOG.error("Caught exception while shutting down: ", e);
         }
      }
   }
}
