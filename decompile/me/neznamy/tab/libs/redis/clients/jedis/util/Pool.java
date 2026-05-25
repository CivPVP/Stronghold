package me.neznamy.tab.libs.redis.clients.jedis.util;

import me.neznamy.tab.libs.org.apache.commons.pool2.PooledObjectFactory;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.GenericObjectPool;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;

public class Pool<T> extends GenericObjectPool<T> {
   public Pool(GenericObjectPoolConfig<T> poolConfig, PooledObjectFactory<T> factory) {
      this(factory, poolConfig);
   }

   public Pool(PooledObjectFactory<T> factory, GenericObjectPoolConfig<T> poolConfig) {
      super(factory, poolConfig);
   }

   public Pool(PooledObjectFactory<T> factory) {
      super(factory);
   }

   @Override
   public void close() {
      this.destroy();
   }

   public void destroy() {
      try {
         super.close();
      } catch (RuntimeException e) {
         throw new JedisException("Could not destroy the pool", e);
      }
   }

   public T getResource() {
      try {
         return super.borrowObject();
      } catch (JedisException je) {
         throw je;
      } catch (Exception e) {
         throw new JedisException("Could not get a resource from the pool", e);
      }
   }

   public void returnResource(T resource) {
      if (resource != null) {
         try {
            super.returnObject(resource);
         } catch (RuntimeException e) {
            throw new JedisException("Could not return the resource to the pool", e);
         }
      }
   }

   public void returnBrokenResource(T resource) {
      if (resource != null) {
         try {
            super.invalidateObject(resource);
         } catch (Exception e) {
            throw new JedisException("Could not return the broken resource to the pool", e);
         }
      }
   }

   @Override
   public void addObjects(int count) {
      try {
         for (int i = 0; i < count; i++) {
            this.addObject();
         }
      } catch (Exception e) {
         throw new JedisException("Error trying to add idle objects", e);
      }
   }
}
