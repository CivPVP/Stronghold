package me.neznamy.tab.libs.org.apache.commons.pool2.proxy;

import me.neznamy.tab.libs.org.apache.commons.pool2.ObjectPool;
import me.neznamy.tab.libs.org.apache.commons.pool2.UsageTracking;

public class ProxiedObjectPool<T> implements ObjectPool<T> {
   private final ObjectPool<T> pool;
   private final ProxySource<T> proxySource;

   public ProxiedObjectPool(ObjectPool<T> pool, ProxySource<T> proxySource) {
      this.pool = pool;
      this.proxySource = proxySource;
   }

   @Override
   public void addObject() throws Exception {
      this.pool.addObject();
   }

   @Override
   public T borrowObject() throws Exception {
      UsageTracking<T> usageTracking = null;
      if (this.pool instanceof UsageTracking) {
         usageTracking = (UsageTracking<T>)this.pool;
      }

      return this.proxySource.createProxy(this.pool.borrowObject(), usageTracking);
   }

   @Override
   public void clear() throws Exception {
      this.pool.clear();
   }

   @Override
   public void close() {
      this.pool.close();
   }

   @Override
   public int getNumActive() {
      return this.pool.getNumActive();
   }

   @Override
   public int getNumIdle() {
      return this.pool.getNumIdle();
   }

   @Override
   public void invalidateObject(T proxy) throws Exception {
      this.pool.invalidateObject(this.proxySource.resolveProxy(proxy));
   }

   @Override
   public void returnObject(T proxy) throws Exception {
      this.pool.returnObject(this.proxySource.resolveProxy(proxy));
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("ProxiedObjectPool [pool=");
      builder.append(this.pool);
      builder.append(", proxySource=");
      builder.append(this.proxySource);
      builder.append("]");
      return builder.toString();
   }
}
