package me.neznamy.tab.libs.redis.clients.jedis.csc;

import java.util.List;

public interface EvictionPolicy {
   Cache getCache();

   void setCache(Cache var1);

   EvictionPolicy.EvictionType getType();

   String getName();

   CacheKey evictNext();

   List<CacheKey> evictMany(int var1);

   void touch(CacheKey var1);

   boolean reset(CacheKey var1);

   int resetAll();

   enum EvictionType {
      AGE,
      FREQ,
      HYBR,
      MISC;
   }
}
