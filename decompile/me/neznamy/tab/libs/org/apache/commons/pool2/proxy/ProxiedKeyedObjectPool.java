package me.neznamy.tab.libs.org.apache.commons.pool2.proxy;

import java.util.List;
import me.neznamy.tab.libs.org.apache.commons.pool2.KeyedObjectPool;
import me.neznamy.tab.libs.org.apache.commons.pool2.UsageTracking;

public class ProxiedKeyedObjectPool<K, V> implements KeyedObjectPool<K, V> {
   private final KeyedObjectPool<K, V> pool;
   private final ProxySource<V> proxySource;

   public ProxiedKeyedObjectPool(KeyedObjectPool<K, V> pool, ProxySource<V> proxySource) {
      this.pool = pool;
      this.proxySource = proxySource;
   }

   @Override
   public void addObject(K key) throws Exception {
      this.pool.addObject(key);
   }

   @Override
   public V borrowObject(K key) throws Exception {
      UsageTracking<V> usageTracking = null;
      if (this.pool instanceof UsageTracking) {
         usageTracking = (UsageTracking<V>)this.pool;
      }

      return this.proxySource.createProxy(this.pool.borrowObject(key), usageTracking);
   }

   @Override
   public void clear() throws Exception {
      this.pool.clear();
   }

   @Override
   public void clear(K key) throws Exception {
      this.pool.clear(key);
   }

   @Override
   public void close() {
      this.pool.close();
   }

   @Override
   public List<K> getKeys() {
      return this.pool.getKeys();
   }

   @Override
   public int getNumActive() {
      return this.pool.getNumActive();
   }

   @Override
   public int getNumActive(K key) {
      return this.pool.getNumActive(key);
   }

   @Override
   public int getNumIdle() {
      return this.pool.getNumIdle();
   }

   @Override
   public int getNumIdle(K key) {
      return this.pool.getNumIdle(key);
   }

   @Override
   public void invalidateObject(K key, V proxy) throws Exception {
      this.pool.invalidateObject(key, this.proxySource.resolveProxy(proxy));
   }

   @Override
   public void returnObject(K key, V proxy) throws Exception {
      this.pool.returnObject(key, this.proxySource.resolveProxy(proxy));
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("ProxiedKeyedObjectPool [pool=");
      builder.append(this.pool);
      builder.append(", proxySource=");
      builder.append(this.proxySource);
      builder.append("]");
      return builder.toString();
   }
}
