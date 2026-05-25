package me.neznamy.tab.libs.org.apache.commons.pool2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public final class PoolUtils {
   private static final String MSG_FACTOR_NEGATIVE = "factor must be positive.";
   private static final String MSG_MIN_IDLE = "minIdle must be non-negative.";
   static final String MSG_NULL_KEY = "key must not be null.";
   private static final String MSG_NULL_KEYED_POOL = "keyedPool must not be null.";
   static final String MSG_NULL_KEYS = "keys must not be null.";
   private static final String MSG_NULL_POOL = "pool must not be null.";

   public static <K, V> Map<K, TimerTask> checkMinIdle(KeyedObjectPool<K, V> keyedPool, Collection<K> keys, int minIdle, long periodMillis) throws IllegalArgumentException {
      if (keys == null) {
         throw new IllegalArgumentException("keys must not be null.");
      }

      Map<K, TimerTask> tasks = new HashMap<>(keys.size());

      for (K key : keys) {
         TimerTask task = checkMinIdle(keyedPool, key, minIdle, periodMillis);
         tasks.put(key, task);
      }

      return tasks;
   }

   public static <K, V> TimerTask checkMinIdle(KeyedObjectPool<K, V> keyedPool, K key, int minIdle, long periodMillis) throws IllegalArgumentException {
      if (keyedPool == null) {
         throw new IllegalArgumentException("keyedPool must not be null.");
      }

      if (key == null) {
         throw new IllegalArgumentException("key must not be null.");
      }

      if (minIdle < 0) {
         throw new IllegalArgumentException("minIdle must be non-negative.");
      }

      TimerTask task = new PoolUtils.KeyedObjectPoolMinIdleTimerTask<>(keyedPool, key, minIdle);
      getMinIdleTimer().schedule(task, 0L, periodMillis);
      return task;
   }

   public static <T> TimerTask checkMinIdle(ObjectPool<T> pool, int minIdle, long periodMillis) throws IllegalArgumentException {
      if (pool == null) {
         throw new IllegalArgumentException("keyedPool must not be null.");
      }

      if (minIdle < 0) {
         throw new IllegalArgumentException("minIdle must be non-negative.");
      }

      TimerTask task = new PoolUtils.ObjectPoolMinIdleTimerTask<>(pool, minIdle);
      getMinIdleTimer().schedule(task, 0L, periodMillis);
      return task;
   }

   public static void checkRethrow(Throwable t) {
      if (t instanceof ThreadDeath) {
         throw (ThreadDeath)t;
      }

      if (t instanceof VirtualMachineError) {
         throw (VirtualMachineError)t;
      }
   }

   public static <K, V> KeyedObjectPool<K, V> erodingPool(KeyedObjectPool<K, V> keyedPool) {
      return erodingPool(keyedPool, 1.0F);
   }

   public static <K, V> KeyedObjectPool<K, V> erodingPool(KeyedObjectPool<K, V> keyedPool, float factor) {
      return erodingPool(keyedPool, factor, false);
   }

   public static <K, V> KeyedObjectPool<K, V> erodingPool(KeyedObjectPool<K, V> keyedPool, float factor, boolean perKey) {
      if (keyedPool == null) {
         throw new IllegalArgumentException("keyedPool must not be null.");
      } else if (factor <= 0.0F) {
         throw new IllegalArgumentException("factor must be positive.");
      } else {
         return perKey ? new PoolUtils.ErodingPerKeyKeyedObjectPool<>(keyedPool, factor) : new PoolUtils.ErodingKeyedObjectPool<>(keyedPool, factor);
      }
   }

   public static <T> ObjectPool<T> erodingPool(ObjectPool<T> pool) {
      return erodingPool(pool, 1.0F);
   }

   public static <T> ObjectPool<T> erodingPool(ObjectPool<T> pool, float factor) {
      if (pool == null) {
         throw new IllegalArgumentException("pool must not be null.");
      } else if (factor <= 0.0F) {
         throw new IllegalArgumentException("factor must be positive.");
      } else {
         return new PoolUtils.ErodingObjectPool<>(pool, factor);
      }
   }

   private static Timer getMinIdleTimer() {
      return PoolUtils.TimerHolder.MIN_IDLE_TIMER;
   }

   @Deprecated
   public static <K, V> void prefill(KeyedObjectPool<K, V> keyedPool, Collection<K> keys, int count) throws Exception, IllegalArgumentException {
      if (keys == null) {
         throw new IllegalArgumentException("keys must not be null.");
      }

      keyedPool.addObjects(keys, count);
   }

   @Deprecated
   public static <K, V> void prefill(KeyedObjectPool<K, V> keyedPool, K key, int count) throws Exception, IllegalArgumentException {
      if (keyedPool == null) {
         throw new IllegalArgumentException("keyedPool must not be null.");
      }

      keyedPool.addObjects(key, count);
   }

   @Deprecated
   public static <T> void prefill(ObjectPool<T> pool, int count) throws Exception {
      if (pool == null) {
         throw new IllegalArgumentException("pool must not be null.");
      }

      pool.addObjects(count);
   }

   public static <K, V> KeyedPooledObjectFactory<K, V> synchronizedKeyedPooledFactory(KeyedPooledObjectFactory<K, V> keyedFactory) {
      return new PoolUtils.SynchronizedKeyedPooledObjectFactory<>(keyedFactory);
   }

   public static <K, V> KeyedObjectPool<K, V> synchronizedPool(KeyedObjectPool<K, V> keyedPool) {
      return new PoolUtils.SynchronizedKeyedObjectPool<>(keyedPool);
   }

   public static <T> ObjectPool<T> synchronizedPool(ObjectPool<T> pool) {
      if (pool == null) {
         throw new IllegalArgumentException("pool must not be null.");
      } else {
         return new PoolUtils.SynchronizedObjectPool<>(pool);
      }
   }

   public static <T> PooledObjectFactory<T> synchronizedPooledFactory(PooledObjectFactory<T> factory) {
      return new PoolUtils.SynchronizedPooledObjectFactory<>(factory);
   }

   private static final class ErodingFactor {
      private final float factor;
      private transient volatile long nextShrinkMillis;
      private transient volatile int idleHighWaterMark;

      public ErodingFactor(float factor) {
         this.factor = factor;
         this.nextShrinkMillis = System.currentTimeMillis() + (long)(900000.0F * factor);
         this.idleHighWaterMark = 1;
      }

      public long getNextShrink() {
         return this.nextShrinkMillis;
      }

      @Override
      public String toString() {
         return "ErodingFactor{factor=" + this.factor + ", idleHighWaterMark=" + this.idleHighWaterMark + '}';
      }

      public void update(long nowMillis, int numIdle) {
         int idle = Math.max(0, numIdle);
         this.idleHighWaterMark = Math.max(idle, this.idleHighWaterMark);
         float maxInterval = 15.0F;
         float minutes = 15.0F + -14.0F / this.idleHighWaterMark * idle;
         this.nextShrinkMillis = nowMillis + (long)(minutes * 60000.0F * this.factor);
      }
   }

   private static class ErodingKeyedObjectPool<K, V> implements KeyedObjectPool<K, V> {
      private final KeyedObjectPool<K, V> keyedPool;
      private final PoolUtils.ErodingFactor erodingFactor;

      protected ErodingKeyedObjectPool(KeyedObjectPool<K, V> keyedPool, PoolUtils.ErodingFactor erodingFactor) {
         if (keyedPool == null) {
            throw new IllegalArgumentException("keyedPool must not be null.");
         }

         this.keyedPool = keyedPool;
         this.erodingFactor = erodingFactor;
      }

      public ErodingKeyedObjectPool(KeyedObjectPool<K, V> keyedPool, float factor) {
         this(keyedPool, new PoolUtils.ErodingFactor(factor));
      }

      @Override
      public void addObject(K key) throws Exception {
         this.keyedPool.addObject(key);
      }

      @Override
      public V borrowObject(K key) throws Exception {
         return this.keyedPool.borrowObject(key);
      }

      @Override
      public void clear() throws Exception {
         this.keyedPool.clear();
      }

      @Override
      public void clear(K key) throws Exception {
         this.keyedPool.clear(key);
      }

      @Override
      public void close() {
         try {
            this.keyedPool.close();
         } catch (Exception var2) {
         }
      }

      protected PoolUtils.ErodingFactor getErodingFactor(K key) {
         return this.erodingFactor;
      }

      protected KeyedObjectPool<K, V> getKeyedPool() {
         return this.keyedPool;
      }

      @Override
      public List<K> getKeys() {
         return this.keyedPool.getKeys();
      }

      @Override
      public int getNumActive() {
         return this.keyedPool.getNumActive();
      }

      @Override
      public int getNumActive(K key) {
         return this.keyedPool.getNumActive(key);
      }

      @Override
      public int getNumIdle() {
         return this.keyedPool.getNumIdle();
      }

      @Override
      public int getNumIdle(K key) {
         return this.keyedPool.getNumIdle(key);
      }

      @Override
      public void invalidateObject(K key, V obj) {
         try {
            this.keyedPool.invalidateObject(key, obj);
         } catch (Exception var4) {
         }
      }

      @Override
      public void returnObject(K key, V obj) throws Exception {
         boolean discard = false;
         long nowMillis = System.currentTimeMillis();
         PoolUtils.ErodingFactor factor = this.getErodingFactor(key);
         synchronized (this.keyedPool) {
            if (factor.getNextShrink() < nowMillis) {
               int numIdle = this.getNumIdle(key);
               if (numIdle > 0) {
                  discard = true;
               }

               factor.update(nowMillis, numIdle);
            }
         }

         try {
            if (discard) {
               this.keyedPool.invalidateObject(key, obj);
            } else {
               this.keyedPool.returnObject(key, obj);
            }
         } catch (Exception var10) {
         }
      }

      @Override
      public String toString() {
         return "ErodingKeyedObjectPool{factor=" + this.erodingFactor + ", keyedPool=" + this.keyedPool + '}';
      }
   }

   private static class ErodingObjectPool<T> implements ObjectPool<T> {
      private final ObjectPool<T> pool;
      private final PoolUtils.ErodingFactor factor;

      public ErodingObjectPool(ObjectPool<T> pool, float factor) {
         this.pool = pool;
         this.factor = new PoolUtils.ErodingFactor(factor);
      }

      @Override
      public void addObject() throws Exception {
         this.pool.addObject();
      }

      @Override
      public T borrowObject() throws Exception {
         return this.pool.borrowObject();
      }

      @Override
      public void clear() throws Exception {
         this.pool.clear();
      }

      @Override
      public void close() {
         try {
            this.pool.close();
         } catch (Exception var2) {
         }
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
      public void invalidateObject(T obj) {
         try {
            this.pool.invalidateObject(obj);
         } catch (Exception var3) {
         }
      }

      @Override
      public void returnObject(T obj) {
         boolean discard = false;
         long nowMillis = System.currentTimeMillis();
         synchronized (this.pool) {
            if (this.factor.getNextShrink() < nowMillis) {
               int numIdle = this.pool.getNumIdle();
               if (numIdle > 0) {
                  discard = true;
               }

               this.factor.update(nowMillis, numIdle);
            }
         }

         try {
            if (discard) {
               this.pool.invalidateObject(obj);
            } else {
               this.pool.returnObject(obj);
            }
         } catch (Exception var8) {
         }
      }

      @Override
      public String toString() {
         return "ErodingObjectPool{factor=" + this.factor + ", pool=" + this.pool + '}';
      }
   }

   private static final class ErodingPerKeyKeyedObjectPool<K, V> extends PoolUtils.ErodingKeyedObjectPool<K, V> {
      private final float factor;
      private final Map<K, PoolUtils.ErodingFactor> factors = Collections.synchronizedMap(new HashMap<>());

      public ErodingPerKeyKeyedObjectPool(KeyedObjectPool<K, V> keyedPool, float factor) {
         super(keyedPool, null);
         this.factor = factor;
      }

      @Override
      protected PoolUtils.ErodingFactor getErodingFactor(K key) {
         return this.factors.computeIfAbsent(key, k -> new PoolUtils.ErodingFactor(this.factor));
      }

      @Override
      public String toString() {
         return "ErodingPerKeyKeyedObjectPool{factor=" + this.factor + ", keyedPool=" + this.getKeyedPool() + '}';
      }
   }

   private static final class KeyedObjectPoolMinIdleTimerTask<K, V> extends TimerTask {
      private final int minIdle;
      private final K key;
      private final KeyedObjectPool<K, V> keyedPool;

      KeyedObjectPoolMinIdleTimerTask(KeyedObjectPool<K, V> keyedPool, K key, int minIdle) throws IllegalArgumentException {
         if (keyedPool == null) {
            throw new IllegalArgumentException("keyedPool must not be null.");
         }

         this.keyedPool = keyedPool;
         this.key = key;
         this.minIdle = minIdle;
      }

      @Override
      public void run() {
         boolean success = false;

         try {
            if (this.keyedPool.getNumIdle(this.key) < this.minIdle) {
               this.keyedPool.addObject(this.key);
            }

            success = true;
         } catch (Exception e) {
            this.cancel();
         } finally {
            if (!success) {
               this.cancel();
            }
         }
      }

      @Override
      public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("KeyedObjectPoolMinIdleTimerTask");
         sb.append("{minIdle=").append(this.minIdle);
         sb.append(", key=").append(this.key);
         sb.append(", keyedPool=").append(this.keyedPool);
         sb.append('}');
         return sb.toString();
      }
   }

   private static final class ObjectPoolMinIdleTimerTask<T> extends TimerTask {
      private final int minIdle;
      private final ObjectPool<T> pool;

      ObjectPoolMinIdleTimerTask(ObjectPool<T> pool, int minIdle) throws IllegalArgumentException {
         if (pool == null) {
            throw new IllegalArgumentException("pool must not be null.");
         }

         this.pool = pool;
         this.minIdle = minIdle;
      }

      @Override
      public void run() {
         boolean success = false;

         try {
            if (this.pool.getNumIdle() < this.minIdle) {
               this.pool.addObject();
            }

            success = true;
         } catch (Exception e) {
            this.cancel();
         } finally {
            if (!success) {
               this.cancel();
            }
         }
      }

      @Override
      public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("ObjectPoolMinIdleTimerTask");
         sb.append("{minIdle=").append(this.minIdle);
         sb.append(", pool=").append(this.pool);
         sb.append('}');
         return sb.toString();
      }
   }

   static final class SynchronizedKeyedObjectPool<K, V> implements KeyedObjectPool<K, V> {
      private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
      private final KeyedObjectPool<K, V> keyedPool;

      SynchronizedKeyedObjectPool(KeyedObjectPool<K, V> keyedPool) throws IllegalArgumentException {
         if (keyedPool == null) {
            throw new IllegalArgumentException("keyedPool must not be null.");
         }

         this.keyedPool = keyedPool;
      }

      @Override
      public void addObject(K key) throws Exception {
         WriteLock writeLock = this.readWriteLock.writeLock();
         writeLock.lock();

         try {
            this.keyedPool.addObject(key);
         } finally {
            writeLock.unlock();
         }
      }

      @Override
      public V borrowObject(K key) throws Exception {
         WriteLock writeLock = this.readWriteLock.writeLock();
         writeLock.lock();

         try {
            return this.keyedPool.borrowObject(key);
         } finally {
            writeLock.unlock();
         }
      }

      @Override
      public void clear() throws Exception {
         WriteLock writeLock = this.readWriteLock.writeLock();
         writeLock.lock();

         try {
            this.keyedPool.clear();
         } finally {
            writeLock.unlock();
         }
      }

      @Override
      public void clear(K key) throws Exception {
         WriteLock writeLock = this.readWriteLock.writeLock();
         writeLock.lock();

         try {
            this.keyedPool.clear(key);
         } finally {
            writeLock.unlock();
         }
      }

      @Override
      public void close() {
         WriteLock writeLock = this.readWriteLock.writeLock();
         writeLock.lock();

         try {
            this.keyedPool.close();
         } catch (Exception var6) {
         } finally {
            writeLock.unlock();
         }
      }

      @Override
      public List<K> getKeys() {
         ReadLock readLock = this.readWriteLock.readLock();
         readLock.lock();

         try {
            return this.keyedPool.getKeys();
         } finally {
            readLock.unlock();
         }
      }

      @Override
      public int getNumActive() {
         ReadLock readLock = this.readWriteLock.readLock();
         readLock.lock();

         try {
            return this.keyedPool.getNumActive();
         } finally {
            readLock.unlock();
         }
      }

      @Override
      public int getNumActive(K key) {
         ReadLock readLock = this.readWriteLock.readLock();
         readLock.lock();

         try {
            return this.keyedPool.getNumActive(key);
         } finally {
            readLock.unlock();
         }
      }

      @Override
      public int getNumIdle() {
         ReadLock readLock = this.readWriteLock.readLock();
         readLock.lock();

         try {
            return this.keyedPool.getNumIdle();
         } finally {
            readLock.unlock();
         }
      }

      @Override
      public int getNumIdle(K key) {
         ReadLock readLock = this.readWriteLock.readLock();
         readLock.lock();

         try {
            return this.keyedPool.getNumIdle(key);
         } finally {
            readLock.unlock();
         }
      }

      @Override
      public void invalidateObject(K key, V obj) {
         WriteLock writeLock = this.readWriteLock.writeLock();
         writeLock.lock();

         try {
            this.keyedPool.invalidateObject(key, obj);
         } catch (Exception var8) {
         } finally {
            writeLock.unlock();
         }
      }

      @Override
      public void returnObject(K key, V obj) {
         WriteLock writeLock = this.readWriteLock.writeLock();
         writeLock.lock();

         try {
            this.keyedPool.returnObject(key, obj);
         } catch (Exception var8) {
         } finally {
            writeLock.unlock();
         }
      }

      @Override
      public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("SynchronizedKeyedObjectPool");
         sb.append("{keyedPool=").append(this.keyedPool);
         sb.append('}');
         return sb.toString();
      }
   }

   private static final class SynchronizedKeyedPooledObjectFactory<K, V> implements KeyedPooledObjectFactory<K, V> {
      private final WriteLock writeLock = new ReentrantReadWriteLock().writeLock();
      private final KeyedPooledObjectFactory<K, V> keyedFactory;

      SynchronizedKeyedPooledObjectFactory(KeyedPooledObjectFactory<K, V> keyedFactory) throws IllegalArgumentException {
         if (keyedFactory == null) {
            throw new IllegalArgumentException("keyedFactory must not be null.");
         }

         this.keyedFactory = keyedFactory;
      }

      @Override
      public void activateObject(K key, PooledObject<V> p) throws Exception {
         this.writeLock.lock();

         try {
            this.keyedFactory.activateObject(key, p);
         } finally {
            this.writeLock.unlock();
         }
      }

      @Override
      public void destroyObject(K key, PooledObject<V> p) throws Exception {
         this.writeLock.lock();

         try {
            this.keyedFactory.destroyObject(key, p);
         } finally {
            this.writeLock.unlock();
         }
      }

      @Override
      public PooledObject<V> makeObject(K key) throws Exception {
         this.writeLock.lock();

         try {
            return this.keyedFactory.makeObject(key);
         } finally {
            this.writeLock.unlock();
         }
      }

      @Override
      public void passivateObject(K key, PooledObject<V> p) throws Exception {
         this.writeLock.lock();

         try {
            this.keyedFactory.passivateObject(key, p);
         } finally {
            this.writeLock.unlock();
         }
      }

      @Override
      public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("SynchronizedKeyedPooledObjectFactory");
         sb.append("{keyedFactory=").append(this.keyedFactory);
         sb.append('}');
         return sb.toString();
      }

      @Override
      public boolean validateObject(K key, PooledObject<V> p) {
         this.writeLock.lock();

         try {
            return this.keyedFactory.validateObject(key, p);
         } finally {
            this.writeLock.unlock();
         }
      }
   }

   private static final class SynchronizedObjectPool<T> implements ObjectPool<T> {
      private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
      private final ObjectPool<T> pool;

      SynchronizedObjectPool(ObjectPool<T> pool) throws IllegalArgumentException {
         if (pool == null) {
            throw new IllegalArgumentException("pool must not be null.");
         }

         this.pool = pool;
      }

      @Override
      public void addObject() throws Exception {
         WriteLock writeLock = this.readWriteLock.writeLock();
         writeLock.lock();

         try {
            this.pool.addObject();
         } finally {
            writeLock.unlock();
         }
      }

      @Override
      public T borrowObject() throws Exception {
         WriteLock writeLock = this.readWriteLock.writeLock();
         writeLock.lock();

         try {
            return this.pool.borrowObject();
         } finally {
            writeLock.unlock();
         }
      }

      @Override
      public void clear() throws Exception {
         WriteLock writeLock = this.readWriteLock.writeLock();
         writeLock.lock();

         try {
            this.pool.clear();
         } finally {
            writeLock.unlock();
         }
      }

      @Override
      public void close() {
         WriteLock writeLock = this.readWriteLock.writeLock();
         writeLock.lock();

         try {
            this.pool.close();
         } catch (Exception var6) {
         } finally {
            writeLock.unlock();
         }
      }

      @Override
      public int getNumActive() {
         ReadLock readLock = this.readWriteLock.readLock();
         readLock.lock();

         try {
            return this.pool.getNumActive();
         } finally {
            readLock.unlock();
         }
      }

      @Override
      public int getNumIdle() {
         ReadLock readLock = this.readWriteLock.readLock();
         readLock.lock();

         try {
            return this.pool.getNumIdle();
         } finally {
            readLock.unlock();
         }
      }

      @Override
      public void invalidateObject(T obj) {
         WriteLock writeLock = this.readWriteLock.writeLock();
         writeLock.lock();

         try {
            this.pool.invalidateObject(obj);
         } catch (Exception var7) {
         } finally {
            writeLock.unlock();
         }
      }

      @Override
      public void returnObject(T obj) {
         WriteLock writeLock = this.readWriteLock.writeLock();
         writeLock.lock();

         try {
            this.pool.returnObject(obj);
         } catch (Exception var7) {
         } finally {
            writeLock.unlock();
         }
      }

      @Override
      public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("SynchronizedObjectPool");
         sb.append("{pool=").append(this.pool);
         sb.append('}');
         return sb.toString();
      }
   }

   private static final class SynchronizedPooledObjectFactory<T> implements PooledObjectFactory<T> {
      private final WriteLock writeLock = new ReentrantReadWriteLock().writeLock();
      private final PooledObjectFactory<T> factory;

      SynchronizedPooledObjectFactory(PooledObjectFactory<T> factory) throws IllegalArgumentException {
         if (factory == null) {
            throw new IllegalArgumentException("factory must not be null.");
         }

         this.factory = factory;
      }

      @Override
      public void activateObject(PooledObject<T> p) throws Exception {
         this.writeLock.lock();

         try {
            this.factory.activateObject(p);
         } finally {
            this.writeLock.unlock();
         }
      }

      @Override
      public void destroyObject(PooledObject<T> p) throws Exception {
         this.writeLock.lock();

         try {
            this.factory.destroyObject(p);
         } finally {
            this.writeLock.unlock();
         }
      }

      @Override
      public PooledObject<T> makeObject() throws Exception {
         this.writeLock.lock();

         try {
            return this.factory.makeObject();
         } finally {
            this.writeLock.unlock();
         }
      }

      @Override
      public void passivateObject(PooledObject<T> p) throws Exception {
         this.writeLock.lock();

         try {
            this.factory.passivateObject(p);
         } finally {
            this.writeLock.unlock();
         }
      }

      @Override
      public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("SynchronizedPoolableObjectFactory");
         sb.append("{factory=").append(this.factory);
         sb.append('}');
         return sb.toString();
      }

      @Override
      public boolean validateObject(PooledObject<T> p) {
         this.writeLock.lock();

         try {
            return this.factory.validateObject(p);
         } finally {
            this.writeLock.unlock();
         }
      }
   }

   static class TimerHolder {
      static final Timer MIN_IDLE_TIMER = new Timer(true);
   }
}
