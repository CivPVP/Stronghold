package me.neznamy.tab.libs.redis.clients.jedis.csc;

import java.util.concurrent.atomic.AtomicLong;

public class CacheStats {
   private AtomicLong hits = new AtomicLong(0L);
   private AtomicLong misses = new AtomicLong(0L);
   private AtomicLong loads = new AtomicLong(0L);
   private AtomicLong evicts = new AtomicLong(0L);
   private AtomicLong nonCacheable = new AtomicLong(0L);
   private AtomicLong flush = new AtomicLong(0L);
   private AtomicLong invalidationsByServer = new AtomicLong(0L);
   private AtomicLong invalidationMessages = new AtomicLong(0L);

   protected void hit() {
      this.hits.incrementAndGet();
   }

   protected void miss() {
      this.misses.incrementAndGet();
   }

   protected void load() {
      this.loads.incrementAndGet();
   }

   protected void evict() {
      this.evicts.incrementAndGet();
   }

   protected void nonCacheable() {
      this.nonCacheable.incrementAndGet();
   }

   protected void flush() {
      this.flush.incrementAndGet();
   }

   protected void invalidationByServer(int size) {
      this.invalidationsByServer.addAndGet(size);
   }

   protected void invalidationMessages() {
      this.invalidationMessages.incrementAndGet();
   }

   public long getHitCount() {
      return this.hits.get();
   }

   public long getMissCount() {
      return this.misses.get();
   }

   public long getLoadCount() {
      return this.loads.get();
   }

   public long getEvictCount() {
      return this.evicts.get();
   }

   public long getNonCacheableCount() {
      return this.nonCacheable.get();
   }

   public long getFlushCount() {
      return this.flush.get();
   }

   public long getInvalidationCount() {
      return this.invalidationsByServer.get();
   }

   @Override
   public String toString() {
      return "CacheStats{hits="
         + this.hits
         + ", misses="
         + this.misses
         + ", loads="
         + this.loads
         + ", evicts="
         + this.evicts
         + ", nonCacheable="
         + this.nonCacheable
         + ", flush="
         + this.flush
         + ", invalidationsByServer="
         + this.invalidationsByServer
         + ", invalidationMessages="
         + this.invalidationMessages
         + '}';
   }
}
