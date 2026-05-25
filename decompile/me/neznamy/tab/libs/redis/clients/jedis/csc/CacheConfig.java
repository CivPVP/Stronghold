package me.neznamy.tab.libs.redis.clients.jedis.csc;

public class CacheConfig {
   private int maxSize;
   private Cacheable cacheable;
   private EvictionPolicy evictionPolicy;
   private Class cacheClass;

   public int getMaxSize() {
      return this.maxSize;
   }

   public Cacheable getCacheable() {
      return this.cacheable;
   }

   public EvictionPolicy getEvictionPolicy() {
      return this.evictionPolicy;
   }

   public Class getCacheClass() {
      return this.cacheClass;
   }

   public static CacheConfig.Builder builder() {
      return new CacheConfig.Builder();
   }

   public static class Builder {
      private final int DEFAULT_MAX_SIZE = 10000;
      private int maxSize = 10000;
      private Cacheable cacheable = DefaultCacheable.INSTANCE;
      private EvictionPolicy evictionPolicy;
      private Class cacheClass;

      public CacheConfig.Builder maxSize(int maxSize) {
         this.maxSize = maxSize;
         return this;
      }

      public CacheConfig.Builder evictionPolicy(EvictionPolicy policy) {
         this.evictionPolicy = policy;
         return this;
      }

      public CacheConfig.Builder cacheable(Cacheable cacheable) {
         this.cacheable = cacheable;
         return this;
      }

      public CacheConfig.Builder cacheClass(Class cacheClass) {
         this.cacheClass = cacheClass;
         return this;
      }

      public CacheConfig build() {
         CacheConfig cacheConfig = new CacheConfig();
         cacheConfig.maxSize = this.maxSize;
         cacheConfig.cacheable = this.cacheable;
         cacheConfig.evictionPolicy = this.evictionPolicy;
         cacheConfig.cacheClass = this.cacheClass;
         return cacheConfig;
      }
   }
}
