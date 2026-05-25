package me.neznamy.tab.libs.org.apache.commons.pool2.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import me.neznamy.tab.libs.org.apache.commons.pool2.DestroyMode;
import me.neznamy.tab.libs.org.apache.commons.pool2.KeyedObjectPool;
import me.neznamy.tab.libs.org.apache.commons.pool2.KeyedPooledObjectFactory;
import me.neznamy.tab.libs.org.apache.commons.pool2.PoolUtils;
import me.neznamy.tab.libs.org.apache.commons.pool2.PooledObject;
import me.neznamy.tab.libs.org.apache.commons.pool2.PooledObjectState;
import me.neznamy.tab.libs.org.apache.commons.pool2.UsageTracking;

public class GenericKeyedObjectPool<K, T> extends BaseGenericObjectPool<T> implements KeyedObjectPool<K, T>, GenericKeyedObjectPoolMXBean<K>, UsageTracking<T> {
   private static final Integer ZERO = 0;
   private static final String ONAME_BASE = "me.neznamy.tab.libs.org.apache.commons.pool2:type=GenericKeyedObjectPool,name=";
   private volatile int maxIdlePerKey = 8;
   private volatile int minIdlePerKey = 0;
   private volatile int maxTotalPerKey = 8;
   private final KeyedPooledObjectFactory<K, T> factory;
   private final boolean fairness;
   private final Map<K, GenericKeyedObjectPool.ObjectDeque<T>> poolMap = new ConcurrentHashMap<>();
   private final ArrayList<K> poolKeyList = new ArrayList<>();
   private final ReadWriteLock keyLock = new ReentrantReadWriteLock(true);
   private final AtomicInteger numTotal = new AtomicInteger(0);
   private Iterator<K> evictionKeyIterator;
   private K evictionKey;

   public GenericKeyedObjectPool(KeyedPooledObjectFactory<K, T> factory) {
      this(factory, new GenericKeyedObjectPoolConfig<>());
   }

   public GenericKeyedObjectPool(KeyedPooledObjectFactory<K, T> factory, GenericKeyedObjectPoolConfig<T> config) {
      super(config, "me.neznamy.tab.libs.org.apache.commons.pool2:type=GenericKeyedObjectPool,name=", config.getJmxNamePrefix());
      if (factory == null) {
         this.jmxUnregister();
         throw new IllegalArgumentException("Factory may not be null");
      }

      this.factory = factory;
      this.fairness = config.getFairness();
      this.setConfig(config);
   }

   public GenericKeyedObjectPool(KeyedPooledObjectFactory<K, T> factory, GenericKeyedObjectPoolConfig<T> config, AbandonedConfig abandonedConfig) {
      this(factory, config);
      this.setAbandonedConfig(abandonedConfig);
   }

   private void addIdleObject(K key, PooledObject<T> p) throws Exception {
      if (!PooledObject.isNull(p)) {
         this.factory.passivateObject(key, p);
         LinkedBlockingDeque<PooledObject<T>> idleObjects = this.poolMap.get(key).getIdleObjects();
         if (this.getLifo()) {
            idleObjects.addFirst(p);
         } else {
            idleObjects.addLast(p);
         }
      }
   }

   @Override
   public void addObject(K key) throws Exception {
      this.assertOpen();
      this.register(key);

      try {
         this.addIdleObject(key, this.create(key));
      } finally {
         this.deregister(key);
      }
   }

   @Override
   public T borrowObject(K key) throws Exception {
      return this.borrowObject(key, this.getMaxWaitDuration().toMillis());
   }

   public T borrowObject(K key, long borrowMaxWaitMillis) throws Exception {
      this.assertOpen();
      AbandonedConfig ac = this.abandonedConfig;
      if (ac != null && ac.getRemoveAbandonedOnBorrow() && this.getNumIdle() < 2 && this.getNumActive() > this.getMaxTotal() - 3) {
         this.removeAbandoned(ac);
      }

      PooledObject<T> p = null;
      boolean blockWhenExhausted = this.getBlockWhenExhausted();
      Instant waitTime = Instant.now();
      GenericKeyedObjectPool.ObjectDeque<T> objectDeque = this.register(key);

      try {
         while (p == null) {
            boolean create = false;
            p = objectDeque.getIdleObjects().pollFirst();
            if (p == null) {
               p = this.create(key);
               if (!PooledObject.isNull(p)) {
                  create = true;
               }
            }

            if (blockWhenExhausted) {
               if (PooledObject.isNull(p)) {
                  p = borrowMaxWaitMillis < 0L
                     ? objectDeque.getIdleObjects().takeFirst()
                     : objectDeque.getIdleObjects().pollFirst(borrowMaxWaitMillis, TimeUnit.MILLISECONDS);
               }

               if (PooledObject.isNull(p)) {
                  throw new NoSuchElementException(this.appendStats("Timeout waiting for idle object, borrowMaxWaitMillis=" + borrowMaxWaitMillis));
               }
            } else if (PooledObject.isNull(p)) {
               throw new NoSuchElementException(this.appendStats("Pool exhausted"));
            }

            if (!p.allocate()) {
               p = null;
            }

            if (!PooledObject.isNull(p)) {
               try {
                  this.factory.activateObject(key, p);
               } catch (Exception e) {
                  try {
                     this.destroy(key, p, true, DestroyMode.NORMAL);
                  } catch (Exception var21) {
                  }

                  p = null;
                  if (create) {
                     NoSuchElementException nsee = new NoSuchElementException(this.appendStats("Unable to activate object"));
                     nsee.initCause(e);
                     throw nsee;
                  }
               }

               if (!PooledObject.isNull(p) && this.getTestOnBorrow()) {
                  boolean validate = false;
                  Throwable validationThrowable = null;

                  try {
                     validate = this.factory.validateObject(key, p);
                  } catch (Throwable t) {
                     PoolUtils.checkRethrow(t);
                     validationThrowable = t;
                  }

                  if (!validate) {
                     try {
                        this.destroy(key, p, true, DestroyMode.NORMAL);
                        this.destroyedByBorrowValidationCount.incrementAndGet();
                     } catch (Exception var19) {
                     }

                     p = null;
                     if (create) {
                        NoSuchElementException nsee = new NoSuchElementException(this.appendStats("Unable to validate object"));
                        nsee.initCause(validationThrowable);
                        throw nsee;
                     }
                  }
               }
            }
         }
      } finally {
         this.deregister(key);
      }

      this.updateStatsBorrow(p, Duration.between(waitTime, Instant.now()));
      return p.getObject();
   }

   private int calculateDeficit(GenericKeyedObjectPool.ObjectDeque<T> objectDeque) {
      if (objectDeque == null) {
         return this.getMinIdlePerKey();
      }

      int maxTotal = this.getMaxTotal();
      int maxTotalPerKeySave = this.getMaxTotalPerKey();
      int objectDefecit = this.getMinIdlePerKey() - objectDeque.getIdleObjects().size();
      if (maxTotalPerKeySave > 0) {
         int growLimit = Math.max(0, maxTotalPerKeySave - objectDeque.getIdleObjects().size());
         objectDefecit = Math.min(objectDefecit, growLimit);
      }

      if (maxTotal > 0) {
         int growLimit = Math.max(0, maxTotal - this.getNumActive() - this.getNumIdle());
         objectDefecit = Math.min(objectDefecit, growLimit);
      }

      return objectDefecit;
   }

   @Override
   public void clear() {
      this.poolMap.keySet().forEach(key -> this.clear((K)key, false));
   }

   @Override
   public void clear(K key) {
      this.clear(key, true);
   }

   public void clear(K key, boolean reuseCapacity) {
      if (this.poolMap.containsKey(key)) {
         GenericKeyedObjectPool.ObjectDeque<T> objectDeque = this.register(key);
         int freedCapacity = 0;

         try {
            LinkedBlockingDeque<PooledObject<T>> idleObjects = objectDeque.getIdleObjects();

            for (PooledObject<T> p = idleObjects.poll(); p != null; p = idleObjects.poll()) {
               try {
                  if (this.destroy(key, p, true, DestroyMode.NORMAL)) {
                     freedCapacity++;
                  }
               } catch (Exception e) {
                  this.swallowException(e);
               }
            }
         } finally {
            this.deregister(key);
         }

         if (reuseCapacity) {
            this.reuseCapacity(freedCapacity);
         }
      }
   }

   public void clearOldest() {
      TreeMap<PooledObject<T>, K> map = new TreeMap<>();
      this.poolMap.forEach((keyx, value) -> value.getIdleObjects().forEach(px -> map.put(px, (K)keyx)));
      int itemsToRemove = (int)(map.size() * 0.15) + 1;
      Iterator<Entry<PooledObject<T>, K>> iter = map.entrySet().iterator();

      while (iter.hasNext() && itemsToRemove > 0) {
         Entry<PooledObject<T>, K> entry = iter.next();
         K key = entry.getValue();
         PooledObject<T> p = entry.getKey();
         boolean destroyed = true;

         try {
            destroyed = this.destroy(key, p, false, DestroyMode.NORMAL);
         } catch (Exception e) {
            this.swallowException(e);
         }

         if (destroyed) {
            itemsToRemove--;
         }
      }
   }

   @Override
   public void close() {
      if (!this.isClosed()) {
         synchronized (this.closeLock) {
            if (!this.isClosed()) {
               this.stopEvictor();
               this.closed = true;
               this.clear();
               this.jmxUnregister();
               this.poolMap.values().forEach(e -> e.getIdleObjects().interuptTakeWaiters());
               this.clear();
            }
         }
      }
   }

   private PooledObject<T> create(K key) throws Exception {
      int maxTotalPerKeySave = this.getMaxTotalPerKey();
      if (maxTotalPerKeySave < 0) {
         maxTotalPerKeySave = Integer.MAX_VALUE;
      }

      int maxTotal = this.getMaxTotal();
      GenericKeyedObjectPool.ObjectDeque<T> objectDeque = this.poolMap.get(key);
      boolean loop = true;

      while (loop) {
         int newNumTotal = this.numTotal.incrementAndGet();
         if (maxTotal > -1 && newNumTotal > maxTotal) {
            this.numTotal.decrementAndGet();
            if (this.getNumIdle() == 0) {
               return null;
            }

            this.clearOldest();
         } else {
            loop = false;
         }
      }

      Boolean create = null;

      while (create == null) {
         synchronized (objectDeque.makeObjectCountLock) {
            long newCreateCount = objectDeque.getCreateCount().incrementAndGet();
            if (newCreateCount > maxTotalPerKeySave) {
               objectDeque.getCreateCount().decrementAndGet();
               if (objectDeque.makeObjectCount == 0L) {
                  create = Boolean.FALSE;
               } else {
                  objectDeque.makeObjectCountLock.wait();
               }
            } else {
               objectDeque.makeObjectCount++;
               create = Boolean.TRUE;
            }
         }
      }

      if (!create) {
         this.numTotal.decrementAndGet();
         return null;
      }

      PooledObject<T> p = null;

      try {
         p = this.factory.makeObject(key);
         if (PooledObject.isNull(p)) {
            this.numTotal.decrementAndGet();
            objectDeque.getCreateCount().decrementAndGet();
            throw new NullPointerException(String.format("%s.makeObject() = null", this.factory.getClass().getSimpleName()));
         }

         if (this.getTestOnCreate() && !this.factory.validateObject(key, p)) {
            this.numTotal.decrementAndGet();
            objectDeque.getCreateCount().decrementAndGet();
            return null;
         }
      } catch (Exception e) {
         this.numTotal.decrementAndGet();
         objectDeque.getCreateCount().decrementAndGet();
         throw e;
      } finally {
         synchronized (objectDeque.makeObjectCountLock) {
            objectDeque.makeObjectCount--;
            objectDeque.makeObjectCountLock.notifyAll();
         }
      }

      AbandonedConfig ac = this.abandonedConfig;
      if (ac != null && ac.getLogAbandoned()) {
         p.setLogAbandoned(true);
         p.setRequireFullStackTrace(ac.getRequireFullStackTrace());
      }

      this.createdCount.incrementAndGet();
      objectDeque.getAllObjects().put(new BaseGenericObjectPool.IdentityWrapper<>(p.getObject()), p);
      return p;
   }

   private void deregister(K k) {
      Lock lock = this.keyLock.readLock();

      try {
         lock.lock();
         GenericKeyedObjectPool.ObjectDeque<T> objectDeque = this.poolMap.get(k);
         if (objectDeque == null) {
            throw new IllegalStateException("Attempt to de-register a key for a non-existent pool");
         }

         long numInterested = objectDeque.getNumInterested().decrementAndGet();
         if (numInterested < 0L) {
            throw new IllegalStateException("numInterested count for key " + k + " is less than zero");
         }

         if (numInterested == 0L && objectDeque.getCreateCount().get() == 0) {
            lock.unlock();
            lock = this.keyLock.writeLock();
            lock.lock();
            objectDeque = this.poolMap.get(k);
            if (null != objectDeque && objectDeque.getNumInterested().get() == 0L && objectDeque.getCreateCount().get() == 0) {
               this.poolMap.remove(k);
               this.poolKeyList.remove(k);
            }
         }
      } finally {
         lock.unlock();
      }
   }

   private boolean destroy(K key, PooledObject<T> toDestroy, boolean always, DestroyMode destroyMode) throws Exception {
      GenericKeyedObjectPool.ObjectDeque<T> objectDeque = this.register(key);

      try {
         boolean isIdle;
         synchronized (toDestroy) {
            isIdle = toDestroy.getState().equals(PooledObjectState.IDLE);
            if (isIdle || always) {
               isIdle = objectDeque.getIdleObjects().remove(toDestroy);
            }
         }

         if (!isIdle && !always) {
            return false;
         }

         objectDeque.getAllObjects().remove(new BaseGenericObjectPool.IdentityWrapper(toDestroy.getObject()));
         toDestroy.invalidate();

         try {
            this.factory.destroyObject(key, toDestroy, destroyMode);
         } finally {
            objectDeque.getCreateCount().decrementAndGet();
            this.destroyedCount.incrementAndGet();
            this.numTotal.decrementAndGet();
         }

         return true;
      } finally {
         this.deregister(key);
      }
   }

   @Override
   void ensureMinIdle() throws Exception {
      int minIdlePerKeySave = this.getMinIdlePerKey();
      if (minIdlePerKeySave >= 1) {
         for (K k : this.poolMap.keySet()) {
            this.ensureMinIdle(k);
         }
      }
   }

   private void ensureMinIdle(K key) throws Exception {
      GenericKeyedObjectPool.ObjectDeque<T> objectDeque = this.poolMap.get(key);
      int deficit = this.calculateDeficit(objectDeque);

      for (int i = 0; i < deficit && this.calculateDeficit(objectDeque) > 0; i++) {
         this.addObject(key);
         if (objectDeque == null) {
            objectDeque = this.poolMap.get(key);
         }
      }
   }

   @Override
   public void evict() throws Exception {
      this.assertOpen();
      if (this.getNumIdle() > 0) {
         PooledObject<T> underTest = null;
         EvictionPolicy<T> evictionPolicy = this.getEvictionPolicy();
         synchronized (this.evictionLock) {
            EvictionConfig evictionConfig = new EvictionConfig(
               this.getMinEvictableIdleDuration(), this.getSoftMinEvictableIdleDuration(), this.getMinIdlePerKey()
            );
            boolean testWhileIdle = this.getTestWhileIdle();
            int i = 0;

            for (int m = this.getNumTests(); i < m; i++) {
               if (this.evictionIterator == null || !this.evictionIterator.hasNext()) {
                  if (this.evictionKeyIterator == null || !this.evictionKeyIterator.hasNext()) {
                     List<K> keyCopy = new ArrayList<>();
                     Lock readLock = this.keyLock.readLock();
                     readLock.lock();

                     try {
                        keyCopy.addAll(this.poolKeyList);
                     } finally {
                        readLock.unlock();
                     }

                     this.evictionKeyIterator = keyCopy.iterator();
                  }

                  while (this.evictionKeyIterator.hasNext()) {
                     this.evictionKey = this.evictionKeyIterator.next();
                     GenericKeyedObjectPool.ObjectDeque<T> objectDeque = this.poolMap.get(this.evictionKey);
                     if (objectDeque != null) {
                        Deque<PooledObject<T>> idleObjects = objectDeque.getIdleObjects();
                        this.evictionIterator = new BaseGenericObjectPool.EvictionIterator(idleObjects);
                        if (this.evictionIterator.hasNext()) {
                           break;
                        }

                        this.evictionIterator = null;
                     }
                  }
               }

               if (this.evictionIterator == null) {
                  return;
               }

               Deque<PooledObject<T>> idleObjects;
               try {
                  underTest = this.evictionIterator.next();
                  idleObjects = this.evictionIterator.getIdleObjects();
               } catch (NoSuchElementException nsee) {
                  i--;
                  this.evictionIterator = null;
                  continue;
               }

               if (!underTest.startEvictionTest()) {
                  i--;
               } else {
                  boolean evict;
                  try {
                     evict = evictionPolicy.evict(evictionConfig, underTest, this.poolMap.get(this.evictionKey).getIdleObjects().size());
                  } catch (Throwable t) {
                     PoolUtils.checkRethrow(t);
                     this.swallowException(new Exception(t));
                     evict = false;
                  }

                  if (evict) {
                     this.destroy(this.evictionKey, underTest, true, DestroyMode.NORMAL);
                     this.destroyedByEvictorCount.incrementAndGet();
                  } else {
                     if (testWhileIdle) {
                        boolean active = false;

                        try {
                           this.factory.activateObject(this.evictionKey, underTest);
                           active = true;
                        } catch (Exception e) {
                           this.destroy(this.evictionKey, underTest, true, DestroyMode.NORMAL);
                           this.destroyedByEvictorCount.incrementAndGet();
                        }

                        if (active) {
                           boolean validate = false;
                           Throwable validationThrowable = null;

                           try {
                              validate = this.factory.validateObject(this.evictionKey, underTest);
                           } catch (Throwable t) {
                              PoolUtils.checkRethrow(t);
                              validationThrowable = t;
                           }

                           if (!validate) {
                              this.destroy(this.evictionKey, underTest, true, DestroyMode.NORMAL);
                              this.destroyedByEvictorCount.incrementAndGet();
                              if (validationThrowable != null) {
                                 if (validationThrowable instanceof RuntimeException) {
                                    throw (RuntimeException)validationThrowable;
                                 }

                                 throw (Error)validationThrowable;
                              }
                           } else {
                              try {
                                 this.factory.passivateObject(this.evictionKey, underTest);
                              } catch (Exception e) {
                                 this.destroy(this.evictionKey, underTest, true, DestroyMode.NORMAL);
                                 this.destroyedByEvictorCount.incrementAndGet();
                              }
                           }
                        }
                     }

                     underTest.endEvictionTest(idleObjects);
                  }
               }
            }
         }
      }

      AbandonedConfig ac = this.abandonedConfig;
      if (ac != null && ac.getRemoveAbandonedOnMaintenance()) {
         this.removeAbandoned(ac);
      }
   }

   public KeyedPooledObjectFactory<K, T> getFactory() {
      return this.factory;
   }

   @Override
   public List<K> getKeys() {
      return (List<K>)this.poolKeyList.clone();
   }

   @Override
   public int getMaxIdlePerKey() {
      return this.maxIdlePerKey;
   }

   @Override
   public int getMaxTotalPerKey() {
      return this.maxTotalPerKey;
   }

   @Override
   public int getMinIdlePerKey() {
      int maxIdlePerKeySave = this.getMaxIdlePerKey();
      return Math.min(this.minIdlePerKey, maxIdlePerKeySave);
   }

   @Override
   public int getNumActive() {
      return this.numTotal.get() - this.getNumIdle();
   }

   @Override
   public int getNumActive(K key) {
      GenericKeyedObjectPool.ObjectDeque<T> objectDeque = this.poolMap.get(key);
      return objectDeque != null ? objectDeque.getAllObjects().size() - objectDeque.getIdleObjects().size() : 0;
   }

   @Override
   public Map<String, Integer> getNumActivePerKey() {
      return this.poolMap
         .entrySet()
         .stream()
         .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().getAllObjects().size() - e.getValue().getIdleObjects().size(), (t, u) -> u));
   }

   @Override
   public int getNumIdle() {
      return this.poolMap.values().stream().mapToInt(e -> e.getIdleObjects().size()).sum();
   }

   @Override
   public int getNumIdle(K key) {
      GenericKeyedObjectPool.ObjectDeque<T> objectDeque = this.poolMap.get(key);
      return objectDeque != null ? objectDeque.getIdleObjects().size() : 0;
   }

   private int getNumTests() {
      int totalIdle = this.getNumIdle();
      int numTests = this.getNumTestsPerEvictionRun();
      return numTests >= 0 ? Math.min(numTests, totalIdle) : (int)Math.ceil(totalIdle / Math.abs((double)numTests));
   }

   @Override
   public int getNumWaiters() {
      return this.getBlockWhenExhausted() ? this.poolMap.values().stream().mapToInt(e -> e.getIdleObjects().getTakeQueueLength()).sum() : 0;
   }

   @Override
   public Map<String, Integer> getNumWaitersByKey() {
      Map<String, Integer> result = new HashMap<>();
      this.poolMap.forEach((k, deque) -> result.put(k.toString(), this.getBlockWhenExhausted() ? deque.getIdleObjects().getTakeQueueLength() : ZERO));
      return result;
   }

   @Override
   String getStatsString() {
      return super.getStatsString()
         + String.format(
            ", fairness=%s, maxIdlePerKey%,d, maxTotalPerKey=%,d, minIdlePerKey=%,d, numTotal=%,d",
            this.fairness,
            this.maxIdlePerKey,
            this.maxTotalPerKey,
            this.minIdlePerKey,
            this.numTotal.get()
         );
   }

   private boolean hasBorrowWaiters() {
      return this.getBlockWhenExhausted() && this.poolMap.values().stream().anyMatch(deque -> deque.getIdleObjects().hasTakeWaiters());
   }

   @Override
   public void invalidateObject(K key, T obj) throws Exception {
      this.invalidateObject(key, obj, DestroyMode.NORMAL);
   }

   @Override
   public void invalidateObject(K key, T obj, DestroyMode destroyMode) throws Exception {
      GenericKeyedObjectPool.ObjectDeque<T> objectDeque = this.poolMap.get(key);
      PooledObject<T> p = objectDeque != null ? objectDeque.getAllObjects().get(new BaseGenericObjectPool.IdentityWrapper(obj)) : null;
      if (p == null) {
         throw new IllegalStateException(this.appendStats("Object not currently part of this pool"));
      }

      synchronized (p) {
         if (p.getState() != PooledObjectState.INVALID) {
            this.destroy(key, p, true, destroyMode);
            this.reuseCapacity();
         }
      }
   }

   @Override
   public Map<String, List<DefaultPooledObjectInfo>> listAllObjects() {
      return this.poolMap
         .entrySet()
         .stream()
         .collect(
            Collectors.toMap(
               e -> e.getKey().toString(), e -> e.getValue().getAllObjects().values().stream().map(DefaultPooledObjectInfo::new).collect(Collectors.toList())
            )
         );
   }

   public void preparePool(K key) throws Exception {
      int minIdlePerKeySave = this.getMinIdlePerKey();
      if (minIdlePerKeySave >= 1) {
         this.ensureMinIdle(key);
      }
   }

   private GenericKeyedObjectPool.ObjectDeque<T> register(K k) {
      Lock lock = this.keyLock.readLock();
      GenericKeyedObjectPool.ObjectDeque<T> objectDeque = null;

      try {
         lock.lock();
         objectDeque = this.poolMap.get(k);
         if (objectDeque == null) {
            lock.unlock();
            lock = this.keyLock.writeLock();
            lock.lock();
            AtomicBoolean allocated = new AtomicBoolean();
            objectDeque = this.poolMap.computeIfAbsent(k, key -> {
               allocated.set(true);
               GenericKeyedObjectPool.ObjectDeque<T> deque = new GenericKeyedObjectPool.ObjectDeque<>(this.fairness);
               deque.getNumInterested().incrementAndGet();
               this.poolKeyList.add(k);
               return deque;
            });
            if (!allocated.get()) {
               objectDeque = this.poolMap.get(k);
               objectDeque.getNumInterested().incrementAndGet();
            }
         } else {
            objectDeque.getNumInterested().incrementAndGet();
         }
      } finally {
         lock.unlock();
      }

      return objectDeque;
   }

   private void removeAbandoned(AbandonedConfig abandonedConfig) {
      this.poolMap.forEach((key, value) -> {
         ArrayList<PooledObject<T>> remove = this.createRemoveList(abandonedConfig, value.getAllObjects());
         remove.forEach(pooledObject -> {
            if (abandonedConfig.getLogAbandoned()) {
               pooledObject.printStackTrace(abandonedConfig.getLogWriter());
            }

            try {
               this.invalidateObject((K)key, pooledObject.getObject(), DestroyMode.ABANDONED);
            } catch (Exception e) {
               this.swallowException(e);
            }
         });
      });
   }

   @Override
   public void returnObject(K key, T obj) {
      GenericKeyedObjectPool.ObjectDeque<T> objectDeque = this.poolMap.get(key);
      if (objectDeque == null) {
         throw new IllegalStateException("No keyed pool found under the given key.");
      }

      PooledObject<T> p = objectDeque.getAllObjects().get(new BaseGenericObjectPool.IdentityWrapper(obj));
      if (PooledObject.isNull(p)) {
         throw new IllegalStateException("Returned object not currently part of this pool");
      }

      this.markReturningState(p);
      Duration activeTime = p.getActiveDuration();

      try {
         if (!this.getTestOnReturn() || this.factory.validateObject(key, p)) {
            try {
               this.factory.passivateObject(key, p);
            } catch (Exception e1) {
               this.swallowException(e1);

               try {
                  this.destroy(key, p, true, DestroyMode.NORMAL);
               } catch (Exception e) {
                  this.swallowException(e);
               }

               this.whenWaitersAddObject(key, objectDeque.idleObjects);
               return;
            }

            if (!p.deallocate()) {
               throw new IllegalStateException("Object has already been returned to this pool");
            }

            int maxIdle = this.getMaxIdlePerKey();
            LinkedBlockingDeque<PooledObject<T>> idleObjects = objectDeque.getIdleObjects();
            if (this.isClosed() || maxIdle > -1 && maxIdle <= idleObjects.size()) {
               try {
                  this.destroy(key, p, true, DestroyMode.NORMAL);
               } catch (Exception e) {
                  this.swallowException(e);
               }

               return;
            }

            if (this.getLifo()) {
               idleObjects.addFirst(p);
            } else {
               idleObjects.addLast(p);
            }

            if (this.isClosed()) {
               this.clear(key);
            }

            return;
         }

         try {
            this.destroy(key, p, true, DestroyMode.NORMAL);
         } catch (Exception e) {
            this.swallowException(e);
         }

         this.whenWaitersAddObject(key, objectDeque.idleObjects);
      } finally {
         if (this.hasBorrowWaiters()) {
            this.reuseCapacity();
         }

         this.updateStatsReturn(activeTime);
      }
   }

   private void reuseCapacity() {
      int maxTotalPerKeySave = this.getMaxTotalPerKey();
      int maxQueueLength = 0;
      LinkedBlockingDeque<PooledObject<T>> mostLoadedPool = null;
      K mostLoadedKey = null;

      for (Entry<K, GenericKeyedObjectPool.ObjectDeque<T>> entry : this.poolMap.entrySet()) {
         K k = entry.getKey();
         LinkedBlockingDeque<PooledObject<T>> pool = entry.getValue().getIdleObjects();
         int queueLength = pool.getTakeQueueLength();
         if (this.getNumActive(k) < maxTotalPerKeySave && queueLength > maxQueueLength) {
            maxQueueLength = queueLength;
            mostLoadedPool = pool;
            mostLoadedKey = k;
         }
      }

      if (mostLoadedPool != null) {
         this.register(mostLoadedKey);

         try {
            this.addIdleObject(mostLoadedKey, this.create(mostLoadedKey));
         } catch (Exception e) {
            this.swallowException(e);
         } finally {
            this.deregister(mostLoadedKey);
         }
      }
   }

   private void reuseCapacity(int newCapacity) {
      int bound = newCapacity < 1 ? 1 : newCapacity;

      for (int i = 0; i < bound; i++) {
         this.reuseCapacity();
      }
   }

   public void setConfig(GenericKeyedObjectPoolConfig<T> conf) {
      super.setConfig(conf);
      this.setMaxIdlePerKey(conf.getMaxIdlePerKey());
      this.setMaxTotalPerKey(conf.getMaxTotalPerKey());
      this.setMaxTotal(conf.getMaxTotal());
      this.setMinIdlePerKey(conf.getMinIdlePerKey());
   }

   public void setMaxIdlePerKey(int maxIdlePerKey) {
      this.maxIdlePerKey = maxIdlePerKey;
   }

   public void setMaxTotalPerKey(int maxTotalPerKey) {
      this.maxTotalPerKey = maxTotalPerKey;
   }

   public void setMinIdlePerKey(int minIdlePerKey) {
      this.minIdlePerKey = minIdlePerKey;
   }

   @Override
   protected void toStringAppendFields(StringBuilder builder) {
      super.toStringAppendFields(builder);
      builder.append(", maxIdlePerKey=");
      builder.append(this.maxIdlePerKey);
      builder.append(", minIdlePerKey=");
      builder.append(this.minIdlePerKey);
      builder.append(", maxTotalPerKey=");
      builder.append(this.maxTotalPerKey);
      builder.append(", factory=");
      builder.append(this.factory);
      builder.append(", fairness=");
      builder.append(this.fairness);
      builder.append(", poolMap=");
      builder.append(this.poolMap);
      builder.append(", poolKeyList=");
      builder.append(this.poolKeyList);
      builder.append(", keyLock=");
      builder.append(this.keyLock);
      builder.append(", numTotal=");
      builder.append(this.numTotal);
      builder.append(", evictionKeyIterator=");
      builder.append(this.evictionKeyIterator);
      builder.append(", evictionKey=");
      builder.append(this.evictionKey);
      builder.append(", abandonedConfig=");
      builder.append(this.abandonedConfig);
   }

   @Override
   public void use(T pooledObject) {
      AbandonedConfig abandonedCfg = this.abandonedConfig;
      if (abandonedCfg != null && abandonedCfg.getUseUsageTracking()) {
         this.poolMap
            .values()
            .stream()
            .map(pool -> pool.getAllObjects().get(new BaseGenericObjectPool.IdentityWrapper(pooledObject)))
            .filter(Objects::nonNull)
            .findFirst()
            .ifPresent(PooledObject::use);
      }
   }

   private void whenWaitersAddObject(K key, LinkedBlockingDeque<PooledObject<T>> idleObjects) {
      if (idleObjects.hasTakeWaiters()) {
         try {
            this.addObject(key);
         } catch (Exception e) {
            this.swallowException(e);
         }
      }
   }

   private static class ObjectDeque<S> {
      private final LinkedBlockingDeque<PooledObject<S>> idleObjects;
      private final AtomicInteger createCount = new AtomicInteger(0);
      private long makeObjectCount;
      private final Object makeObjectCountLock = new Object();
      private final Map<BaseGenericObjectPool.IdentityWrapper<S>, PooledObject<S>> allObjects = new ConcurrentHashMap<>();
      private final AtomicLong numInterested = new AtomicLong();

      public ObjectDeque(boolean fairness) {
         this.idleObjects = new LinkedBlockingDeque<>(fairness);
      }

      public Map<BaseGenericObjectPool.IdentityWrapper<S>, PooledObject<S>> getAllObjects() {
         return this.allObjects;
      }

      public AtomicInteger getCreateCount() {
         return this.createCount;
      }

      public LinkedBlockingDeque<PooledObject<S>> getIdleObjects() {
         return this.idleObjects;
      }

      public AtomicLong getNumInterested() {
         return this.numInterested;
      }

      @Override
      public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("ObjectDeque [idleObjects=");
         builder.append(this.idleObjects);
         builder.append(", createCount=");
         builder.append(this.createCount);
         builder.append(", allObjects=");
         builder.append(this.allObjects);
         builder.append(", numInterested=");
         builder.append(this.numInterested);
         builder.append("]");
         return builder.toString();
      }
   }
}
