package me.neznamy.tab.libs.redis.clients.jedis.csc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LRUEviction implements EvictionPolicy {
   protected Cache cache;
   protected LinkedHashMap<CacheKey, Long> accessTimes;
   protected ArrayDeque<CacheKey> pendingEvictions = new ArrayDeque<>();
   protected ConcurrentLinkedQueue msg = new ConcurrentLinkedQueue();
   private int initialCapacity;

   public LRUEviction(int initialCapacity) {
      this.initialCapacity = initialCapacity;
   }

   @Override
   public void setCache(final Cache cache) {
      this.cache = cache;
      this.accessTimes = new LinkedHashMap<CacheKey, Long>(this.initialCapacity, 1.0F, true) {
         @Override
         protected boolean removeEldestEntry(Entry<CacheKey, Long> eldest) {
            boolean evictionRequired = cache.getSize() > cache.getMaxSize() || LRUEviction.this.accessTimes.size() > cache.getMaxSize();
            if (evictionRequired && cache.hasCacheKey(eldest.getKey())) {
               LRUEviction.this.pendingEvictions.addLast(eldest.getKey());
            }

            return evictionRequired;
         }
      };
   }

   @Override
   public Cache getCache() {
      return this.cache;
   }

   @Override
   public EvictionPolicy.EvictionType getType() {
      return EvictionPolicy.EvictionType.AGE;
   }

   @Override
   public String getName() {
      return "Simple L(east) R(ecently) U(sed)";
   }

   @Override
   public synchronized CacheKey evictNext() {
      CacheKey cacheKey = this.pendingEvictions.pollFirst();

      while (cacheKey != null && !this.cache.hasCacheKey(cacheKey)) {
         cacheKey = this.pendingEvictions.pollFirst();
      }

      return cacheKey;
   }

   @Override
   public synchronized List<CacheKey> evictMany(int n) {
      List<CacheKey> result = new ArrayList<>();

      for (int i = 0; i < n; i++) {
         result.add(this.evictNext());
      }

      return result;
   }

   @Override
   public synchronized void touch(CacheKey cacheKey) {
      this.accessTimes.put(cacheKey, new Date().getTime());
   }

   @Override
   public synchronized boolean reset(CacheKey cacheKey) {
      return this.accessTimes.remove(cacheKey) != null;
   }

   @Override
   public synchronized int resetAll() {
      int result = this.accessTimes.size();
      this.accessTimes.clear();
      return result;
   }
}
