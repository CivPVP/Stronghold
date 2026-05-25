package me.neznamy.tab.libs.redis.clients.jedis.csc;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import me.neznamy.tab.libs.redis.clients.jedis.CommandObject;
import me.neznamy.tab.libs.redis.clients.jedis.Connection;
import me.neznamy.tab.libs.redis.clients.jedis.JedisClientConfig;
import me.neznamy.tab.libs.redis.clients.jedis.JedisSocketFactory;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.RedisProtocol;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;
import me.neznamy.tab.libs.redis.clients.jedis.util.RedisInputStream;

public class CacheConnection extends Connection {
   private final Cache cache;
   private ReentrantLock lock;
   private static final String REDIS = "redis";
   private static final String MIN_REDIS_VERSION = "7.4";

   public CacheConnection(JedisSocketFactory socketFactory, JedisClientConfig clientConfig, Cache cache) {
      super(socketFactory, clientConfig);
      if (this.protocol != RedisProtocol.RESP3) {
         throw new JedisException("Client side caching is only supported with RESP3.");
      }

      if (!cache.compatibilityMode()) {
         RedisVersion current = new RedisVersion(this.version);
         RedisVersion required = new RedisVersion("7.4");
         if (!"redis".equals(this.server) || current.compareTo(required) < 0) {
            throw new JedisException(String.format("Client side caching is only supported with 'Redis %s' or later.", "7.4"));
         }
      }

      this.cache = Objects.requireNonNull(cache);
      this.initializeClientSideCache();
   }

   @Override
   protected void initializeFromClientConfig(JedisClientConfig config) {
      this.lock = new ReentrantLock();
      super.initializeFromClientConfig(config);
   }

   @Override
   protected Object protocolRead(RedisInputStream inputStream) {
      this.lock.lock();

      try {
         return Protocol.read(inputStream, this.cache);
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   protected void protocolReadPushes(RedisInputStream inputStream) {
      if (this.lock.tryLock()) {
         try {
            Protocol.readPushes(inputStream, this.cache, true);
         } finally {
            this.lock.unlock();
         }
      }
   }

   @Override
   public void disconnect() {
      super.disconnect();
      this.cache.flush();
   }

   @Override
   public <T> T executeCommand(CommandObject<T> commandObject) {
      CacheKey cacheKey = new CacheKey<>(commandObject);
      if (!this.cache.isCacheable(cacheKey)) {
         this.cache.getStats().nonCacheable();
         return super.executeCommand(commandObject);
      }

      CacheEntry<T> cacheEntry = this.cache.get(cacheKey);
      if (cacheEntry != null) {
         cacheEntry = this.validateEntry(cacheEntry);
         if (cacheEntry != null) {
            this.cache.getStats().hit();
            return cacheEntry.getValue();
         }
      }

      this.cache.getStats().miss();
      T value = super.executeCommand(commandObject);
      cacheEntry = new CacheEntry<>(cacheKey, value, this);
      this.cache.set(cacheKey, cacheEntry);
      return cacheEntry.getValue();
   }

   public Cache getCache() {
      return this.cache;
   }

   private void initializeClientSideCache() {
      this.sendCommand(Protocol.Command.CLIENT, "TRACKING", "ON");
      String reply = this.getStatusCodeReply();
      if (!"OK".equals(reply)) {
         throw new JedisException("Could not enable client tracking. Reply: " + reply);
      }
   }

   private CacheEntry validateEntry(CacheEntry cacheEntry) {
      CacheConnection cacheOwner = cacheEntry.getConnection();
      if (cacheOwner != null && !cacheOwner.isBroken() && cacheOwner.isConnected()) {
         try {
            cacheOwner.readPushesWithCheckingBroken();
         } catch (JedisException e) {
            this.cache.delete(cacheEntry.getCacheKey());
            return null;
         }

         return this.cache.get(cacheEntry.getCacheKey());
      } else {
         this.cache.delete(cacheEntry.getCacheKey());
         return null;
      }
   }
}
