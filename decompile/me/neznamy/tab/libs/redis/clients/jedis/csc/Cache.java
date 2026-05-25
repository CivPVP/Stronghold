package me.neznamy.tab.libs.redis.clients.jedis.csc;

import java.util.Collection;
import java.util.List;

public interface Cache {
   int getMaxSize();

   int getSize();

   Collection<CacheEntry> getCacheEntries();

   CacheEntry get(CacheKey var1);

   CacheEntry set(CacheKey var1, CacheEntry var2);

   boolean delete(CacheKey var1);

   List<Boolean> delete(List<CacheKey> var1);

   List<CacheKey> deleteByRedisKey(Object var1);

   List<CacheKey> deleteByRedisKeys(List var1);

   int flush();

   boolean isCacheable(CacheKey var1);

   boolean hasCacheKey(CacheKey var1);

   EvictionPolicy getEvictionPolicy();

   CacheStats getStats();

   CacheStats getAndResetStats();

   boolean compatibilityMode();
}
