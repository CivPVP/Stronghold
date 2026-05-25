package me.neznamy.tab.libs.org.apache.commons.pool2.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import me.neznamy.tab.libs.org.apache.commons.pool2.BaseObject;
import me.neznamy.tab.libs.org.apache.commons.pool2.PooledObject;
import me.neznamy.tab.libs.org.apache.commons.pool2.PooledObjectState;
import me.neznamy.tab.libs.org.apache.commons.pool2.SwallowedExceptionListener;

public abstract class BaseGenericObjectPool<T> extends BaseObject implements AutoCloseable {
   public static final int MEAN_TIMING_STATS_CACHE_SIZE = 100;
   private static final String EVICTION_POLICY_TYPE_NAME = EvictionPolicy.class.getName();
   private static final Duration DEFAULT_REMOVE_ABANDONED_TIMEOUT = Duration.ofSeconds(2147483647L);
   private volatile int maxTotal = -1;
   private volatile boolean blockWhenExhausted = true;
   private volatile Duration maxWaitDuration = BaseObjectPoolConfig.DEFAULT_MAX_WAIT;
   private volatile boolean lifo = true;
   private final boolean fairness;
   private volatile boolean testOnCreate = false;
   private volatile boolean testOnBorrow = false;
   private volatile boolean testOnReturn = false;
   private volatile boolean testWhileIdle = false;
   private volatile Duration durationBetweenEvictionRuns = BaseObjectPoolConfig.DEFAULT_DURATION_BETWEEN_EVICTION_RUNS;
   private volatile int numTestsPerEvictionRun = 3;
   private volatile Duration minEvictableIdleDuration = BaseObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_DURATION;
   private volatile Duration softMinEvictableIdleDuration = BaseObjectPoolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_DURATION;
   private volatile EvictionPolicy<T> evictionPolicy;
   private volatile Duration evictorShutdownTimeoutDuration = BaseObjectPoolConfig.DEFAULT_EVICTOR_SHUTDOWN_TIMEOUT;
   final Object closeLock = new Object();
   volatile boolean closed;
   final Object evictionLock = new Object();
   private BaseGenericObjectPool<T>.Evictor evictor;
   BaseGenericObjectPool<T>.EvictionIterator evictionIterator;
   private final WeakReference<ClassLoader> factoryClassLoader;
   private final ObjectName objectName;
   private final String creationStackTrace;
   private final AtomicLong borrowedCount = new AtomicLong();
   private final AtomicLong returnedCount = new AtomicLong();
   final AtomicLong createdCount = new AtomicLong();
   final AtomicLong destroyedCount = new AtomicLong();
   final AtomicLong destroyedByEvictorCount = new AtomicLong();
   final AtomicLong destroyedByBorrowValidationCount = new AtomicLong();
   private final BaseGenericObjectPool.StatsStore activeTimes = new BaseGenericObjectPool.StatsStore(100);
   private final BaseGenericObjectPool.StatsStore idleTimes = new BaseGenericObjectPool.StatsStore(100);
   private final BaseGenericObjectPool.StatsStore waitTimes = new BaseGenericObjectPool.StatsStore(100);
   private final AtomicReference<Duration> maxBorrowWaitDuration = new AtomicReference<>(Duration.ZERO);
   private volatile SwallowedExceptionListener swallowedExceptionListener;
   private volatile boolean messageStatistics;
   protected volatile AbandonedConfig abandonedConfig;

   public BaseGenericObjectPool(BaseObjectPoolConfig<T> config, String jmxNameBase, String jmxNamePrefix) {
      if (config.getJmxEnabled()) {
         this.objectName = this.jmxRegister(config, jmxNameBase, jmxNamePrefix);
      } else {
         this.objectName = null;
      }

      this.creationStackTrace = this.getStackTrace(new Exception());
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      if (cl == null) {
         this.factoryClassLoader = null;
      } else {
         this.factoryClassLoader = new WeakReference<>(cl);
      }

      this.fairness = config.getFairness();
   }

   String appendStats(String string) {
      return this.messageStatistics ? string + ", " + this.getStatsString() : string;
   }

   final void assertOpen() throws IllegalStateException {
      if (this.isClosed()) {
         throw new IllegalStateException("Pool not open");
      }
   }

   @Override
   public abstract void close();

   ArrayList<PooledObject<T>> createRemoveList(AbandonedConfig abandonedConfig, Map<BaseGenericObjectPool.IdentityWrapper<T>, PooledObject<T>> allObjects) {
      Instant timeout = Instant.now().minus(abandonedConfig.getRemoveAbandonedTimeoutDuration());
      ArrayList<PooledObject<T>> remove = new ArrayList<>();
      allObjects.values().forEach(pooledObject -> {
         synchronized (pooledObject) {
            if (pooledObject.getState() == PooledObjectState.ALLOCATED && pooledObject.getLastUsedInstant().compareTo(timeout) <= 0) {
               pooledObject.markAbandoned();
               remove.add((PooledObject<T>)pooledObject);
            }
         }
      });
      return remove;
   }

   abstract void ensureMinIdle() throws Exception;

   public abstract void evict() throws Exception;

   public final boolean getBlockWhenExhausted() {
      return this.blockWhenExhausted;
   }

   public final long getBorrowedCount() {
      return this.borrowedCount.get();
   }

   public final long getCreatedCount() {
      return this.createdCount.get();
   }

   public final String getCreationStackTrace() {
      return this.creationStackTrace;
   }

   public final long getDestroyedByBorrowValidationCount() {
      return this.destroyedByBorrowValidationCount.get();
   }

   public final long getDestroyedByEvictorCount() {
      return this.destroyedByEvictorCount.get();
   }

   public final long getDestroyedCount() {
      return this.destroyedCount.get();
   }

   public final Duration getDurationBetweenEvictionRuns() {
      return this.durationBetweenEvictionRuns;
   }

   public EvictionPolicy<T> getEvictionPolicy() {
      return this.evictionPolicy;
   }

   public final String getEvictionPolicyClassName() {
      return this.evictionPolicy.getClass().getName();
   }

   @Deprecated
   public final Duration getEvictorShutdownTimeout() {
      return this.evictorShutdownTimeoutDuration;
   }

   public final Duration getEvictorShutdownTimeoutDuration() {
      return this.evictorShutdownTimeoutDuration;
   }

   @Deprecated
   public final long getEvictorShutdownTimeoutMillis() {
      return this.evictorShutdownTimeoutDuration.toMillis();
   }

   public final boolean getFairness() {
      return this.fairness;
   }

   public final ObjectName getJmxName() {
      return this.objectName;
   }

   public final boolean getLifo() {
      return this.lifo;
   }

   public boolean getLogAbandoned() {
      AbandonedConfig ac = this.abandonedConfig;
      return ac != null && ac.getLogAbandoned();
   }

   public final Duration getMaxBorrowWaitDuration() {
      return this.maxBorrowWaitDuration.get();
   }

   @Deprecated
   public final long getMaxBorrowWaitTimeMillis() {
      return this.maxBorrowWaitDuration.get().toMillis();
   }

   public final int getMaxTotal() {
      return this.maxTotal;
   }

   public final Duration getMaxWaitDuration() {
      return this.maxWaitDuration;
   }

   @Deprecated
   public final long getMaxWaitMillis() {
      return this.maxWaitDuration.toMillis();
   }

   public final Duration getMeanActiveDuration() {
      return this.activeTimes.getMeanDuration();
   }

   @Deprecated
   public final long getMeanActiveTimeMillis() {
      return this.activeTimes.getMean();
   }

   public final Duration getMeanBorrowWaitDuration() {
      return this.waitTimes.getMeanDuration();
   }

   @Deprecated
   public final long getMeanBorrowWaitTimeMillis() {
      return this.waitTimes.getMean();
   }

   public final Duration getMeanIdleDuration() {
      return this.idleTimes.getMeanDuration();
   }

   @Deprecated
   public final long getMeanIdleTimeMillis() {
      return this.idleTimes.getMean();
   }

   public boolean getMessageStatistics() {
      return this.messageStatistics;
   }

   public final Duration getMinEvictableIdleDuration() {
      return this.minEvictableIdleDuration;
   }

   @Deprecated
   public final Duration getMinEvictableIdleTime() {
      return this.minEvictableIdleDuration;
   }

   @Deprecated
   public final long getMinEvictableIdleTimeMillis() {
      return this.minEvictableIdleDuration.toMillis();
   }

   public abstract int getNumIdle();

   public final int getNumTestsPerEvictionRun() {
      return this.numTestsPerEvictionRun;
   }

   public boolean getRemoveAbandonedOnBorrow() {
      AbandonedConfig ac = this.abandonedConfig;
      return ac != null && ac.getRemoveAbandonedOnBorrow();
   }

   public boolean getRemoveAbandonedOnMaintenance() {
      AbandonedConfig ac = this.abandonedConfig;
      return ac != null && ac.getRemoveAbandonedOnMaintenance();
   }

   @Deprecated
   public int getRemoveAbandonedTimeout() {
      return (int)this.getRemoveAbandonedTimeoutDuration().getSeconds();
   }

   public Duration getRemoveAbandonedTimeoutDuration() {
      AbandonedConfig ac = this.abandonedConfig;
      return ac != null ? ac.getRemoveAbandonedTimeoutDuration() : DEFAULT_REMOVE_ABANDONED_TIMEOUT;
   }

   public final long getReturnedCount() {
      return this.returnedCount.get();
   }

   public final Duration getSoftMinEvictableIdleDuration() {
      return this.softMinEvictableIdleDuration;
   }

   @Deprecated
   public final Duration getSoftMinEvictableIdleTime() {
      return this.softMinEvictableIdleDuration;
   }

   @Deprecated
   public final long getSoftMinEvictableIdleTimeMillis() {
      return this.softMinEvictableIdleDuration.toMillis();
   }

   private String getStackTrace(Exception e) {
      Writer w = new StringWriter();
      PrintWriter pw = new PrintWriter(w);
      e.printStackTrace(pw);
      return w.toString();
   }

   String getStatsString() {
      return String.format(
         "activeTimes=%s, blockWhenExhausted=%s, borrowedCount=%,d, closed=%s, createdCount=%,d, destroyedByBorrowValidationCount=%,d, destroyedByEvictorCount=%,d, evictorShutdownTimeoutDuration=%s, fairness=%s, idleTimes=%s, lifo=%s, maxBorrowWaitDuration=%s, maxTotal=%s, maxWaitDuration=%s, minEvictableIdleDuration=%s, numTestsPerEvictionRun=%s, returnedCount=%s, softMinEvictableIdleDuration=%s, testOnBorrow=%s, testOnCreate=%s, testOnReturn=%s, testWhileIdle=%s, durationBetweenEvictionRuns=%s, waitTimes=%s",
         this.activeTimes.getValues(),
         this.blockWhenExhausted,
         this.borrowedCount.get(),
         this.closed,
         this.createdCount.get(),
         this.destroyedByBorrowValidationCount.get(),
         this.destroyedByEvictorCount.get(),
         this.evictorShutdownTimeoutDuration,
         this.fairness,
         this.idleTimes.getValues(),
         this.lifo,
         this.maxBorrowWaitDuration.get(),
         this.maxTotal,
         this.maxWaitDuration,
         this.minEvictableIdleDuration,
         this.numTestsPerEvictionRun,
         this.returnedCount,
         this.softMinEvictableIdleDuration,
         this.testOnBorrow,
         this.testOnCreate,
         this.testOnReturn,
         this.testWhileIdle,
         this.durationBetweenEvictionRuns,
         this.waitTimes.getValues()
      );
   }

   public final SwallowedExceptionListener getSwallowedExceptionListener() {
      return this.swallowedExceptionListener;
   }

   public final boolean getTestOnBorrow() {
      return this.testOnBorrow;
   }

   public final boolean getTestOnCreate() {
      return this.testOnCreate;
   }

   public final boolean getTestOnReturn() {
      return this.testOnReturn;
   }

   public final boolean getTestWhileIdle() {
      return this.testWhileIdle;
   }

   @Deprecated
   public final Duration getTimeBetweenEvictionRuns() {
      return this.durationBetweenEvictionRuns;
   }

   @Deprecated
   public final long getTimeBetweenEvictionRunsMillis() {
      return this.durationBetweenEvictionRuns.toMillis();
   }

   public boolean isAbandonedConfig() {
      return this.abandonedConfig != null;
   }

   public final boolean isClosed() {
      return this.closed;
   }

   private ObjectName jmxRegister(BaseObjectPoolConfig<T> config, String jmxNameBase, String jmxNamePrefix) {
      ObjectName newObjectName = null;
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      int i = 1;
      boolean registered = false;
      String base = config.getJmxNameBase();
      if (base == null) {
         base = jmxNameBase;
      }

      while (!registered) {
         try {
            ObjectName objName;
            if (i == 1) {
               objName = new ObjectName(base + jmxNamePrefix);
            } else {
               objName = new ObjectName(base + jmxNamePrefix + i);
            }

            if (!mbs.isRegistered(objName)) {
               mbs.registerMBean(this, objName);
               newObjectName = objName;
               registered = true;
            } else {
               i++;
            }
         } catch (MalformedObjectNameException e) {
            if ("pool".equals(jmxNamePrefix) && jmxNameBase.equals(base)) {
               registered = true;
            } else {
               jmxNamePrefix = "pool";
               base = jmxNameBase;
            }
         } catch (InstanceAlreadyExistsException e) {
            i++;
         } catch (MBeanRegistrationException | NotCompliantMBeanException e) {
            registered = true;
         }
      }

      return newObjectName;
   }

   final void jmxUnregister() {
      if (this.objectName != null) {
         try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(this.objectName);
         } catch (MBeanRegistrationException | InstanceNotFoundException e) {
            this.swallowException(e);
         }
      }
   }

   protected void markReturningState(PooledObject<T> pooledObject) {
      synchronized (pooledObject) {
         if (pooledObject.getState() != PooledObjectState.ALLOCATED) {
            throw new IllegalStateException("Object has already been returned to this pool or is invalid");
         }

         pooledObject.markReturning();
      }
   }

   public void setAbandonedConfig(AbandonedConfig abandonedConfig) {
      this.abandonedConfig = AbandonedConfig.copy(abandonedConfig);
   }

   public final void setBlockWhenExhausted(boolean blockWhenExhausted) {
      this.blockWhenExhausted = blockWhenExhausted;
   }

   protected void setConfig(BaseObjectPoolConfig<T> config) {
      this.setLifo(config.getLifo());
      this.setMaxWait(config.getMaxWaitDuration());
      this.setBlockWhenExhausted(config.getBlockWhenExhausted());
      this.setTestOnCreate(config.getTestOnCreate());
      this.setTestOnBorrow(config.getTestOnBorrow());
      this.setTestOnReturn(config.getTestOnReturn());
      this.setTestWhileIdle(config.getTestWhileIdle());
      this.setNumTestsPerEvictionRun(config.getNumTestsPerEvictionRun());
      this.setMinEvictableIdleDuration(config.getMinEvictableIdleDuration());
      this.setDurationBetweenEvictionRuns(config.getDurationBetweenEvictionRuns());
      this.setSoftMinEvictableIdleDuration(config.getSoftMinEvictableIdleDuration());
      EvictionPolicy<T> policy = config.getEvictionPolicy();
      if (policy == null) {
         this.setEvictionPolicyClassName(config.getEvictionPolicyClassName());
      } else {
         this.setEvictionPolicy(policy);
      }

      this.setEvictorShutdownTimeout(config.getEvictorShutdownTimeoutDuration());
   }

   public final void setDurationBetweenEvictionRuns(Duration timeBetweenEvictionRuns) {
      this.durationBetweenEvictionRuns = PoolImplUtils.nonNull(timeBetweenEvictionRuns, BaseObjectPoolConfig.DEFAULT_DURATION_BETWEEN_EVICTION_RUNS);
      this.startEvictor(this.durationBetweenEvictionRuns);
   }

   public void setEvictionPolicy(EvictionPolicy<T> evictionPolicy) {
      this.evictionPolicy = evictionPolicy;
   }

   private void setEvictionPolicy(String className, ClassLoader classLoader) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
      Class<?> clazz = Class.forName(className, true, classLoader);
      Object policy = clazz.getConstructor().newInstance();
      this.evictionPolicy = (EvictionPolicy<T>)policy;
   }

   public final void setEvictionPolicyClassName(String evictionPolicyClassName) {
      this.setEvictionPolicyClassName(evictionPolicyClassName, Thread.currentThread().getContextClassLoader());
   }

   public final void setEvictionPolicyClassName(String evictionPolicyClassName, ClassLoader classLoader) {
      Class<?> epClass = EvictionPolicy.class;
      ClassLoader epClassLoader = epClass.getClassLoader();

      try {
         try {
            this.setEvictionPolicy(evictionPolicyClassName, classLoader);
         } catch (ClassCastException | ClassNotFoundException e) {
            this.setEvictionPolicy(evictionPolicyClassName, epClassLoader);
         }
      } catch (ClassCastException e) {
         throw new IllegalArgumentException(
            "Class "
               + evictionPolicyClassName
               + " from class loaders ["
               + classLoader
               + ", "
               + epClassLoader
               + "] do not implement "
               + EVICTION_POLICY_TYPE_NAME
         );
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
         throw new IllegalArgumentException("Unable to create " + EVICTION_POLICY_TYPE_NAME + " instance of type " + evictionPolicyClassName, e);
      }
   }

   public final void setEvictorShutdownTimeout(Duration evictorShutdownTimeout) {
      this.evictorShutdownTimeoutDuration = PoolImplUtils.nonNull(evictorShutdownTimeout, BaseObjectPoolConfig.DEFAULT_EVICTOR_SHUTDOWN_TIMEOUT);
   }

   @Deprecated
   public final void setEvictorShutdownTimeoutMillis(long evictorShutdownTimeoutMillis) {
      this.setEvictorShutdownTimeout(Duration.ofMillis(evictorShutdownTimeoutMillis));
   }

   public final void setLifo(boolean lifo) {
      this.lifo = lifo;
   }

   public final void setMaxTotal(int maxTotal) {
      this.maxTotal = maxTotal;
   }

   public final void setMaxWait(Duration maxWaitDuration) {
      this.maxWaitDuration = PoolImplUtils.nonNull(maxWaitDuration, BaseObjectPoolConfig.DEFAULT_MAX_WAIT);
   }

   @Deprecated
   public final void setMaxWaitMillis(long maxWaitMillis) {
      this.setMaxWait(Duration.ofMillis(maxWaitMillis));
   }

   public void setMessagesStatistics(boolean messagesDetails) {
      this.messageStatistics = messagesDetails;
   }

   @Deprecated
   public final void setMinEvictableIdle(Duration minEvictableIdleTime) {
      this.minEvictableIdleDuration = PoolImplUtils.nonNull(minEvictableIdleTime, BaseObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_DURATION);
   }

   public final void setMinEvictableIdleDuration(Duration minEvictableIdleTime) {
      this.minEvictableIdleDuration = PoolImplUtils.nonNull(minEvictableIdleTime, BaseObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_DURATION);
   }

   @Deprecated
   public final void setMinEvictableIdleTime(Duration minEvictableIdleTime) {
      this.minEvictableIdleDuration = PoolImplUtils.nonNull(minEvictableIdleTime, BaseObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_DURATION);
   }

   @Deprecated
   public final void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
      this.setMinEvictableIdleTime(Duration.ofMillis(minEvictableIdleTimeMillis));
   }

   public final void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
      this.numTestsPerEvictionRun = numTestsPerEvictionRun;
   }

   @Deprecated
   public final void setSoftMinEvictableIdle(Duration softMinEvictableIdleTime) {
      this.softMinEvictableIdleDuration = PoolImplUtils.nonNull(softMinEvictableIdleTime, BaseObjectPoolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_DURATION);
   }

   public final void setSoftMinEvictableIdleDuration(Duration softMinEvictableIdleTime) {
      this.softMinEvictableIdleDuration = PoolImplUtils.nonNull(softMinEvictableIdleTime, BaseObjectPoolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_DURATION);
   }

   @Deprecated
   public final void setSoftMinEvictableIdleTime(Duration softMinEvictableIdleTime) {
      this.softMinEvictableIdleDuration = PoolImplUtils.nonNull(softMinEvictableIdleTime, BaseObjectPoolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_DURATION);
   }

   @Deprecated
   public final void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
      this.setSoftMinEvictableIdleTime(Duration.ofMillis(softMinEvictableIdleTimeMillis));
   }

   public final void setSwallowedExceptionListener(SwallowedExceptionListener swallowedExceptionListener) {
      this.swallowedExceptionListener = swallowedExceptionListener;
   }

   public final void setTestOnBorrow(boolean testOnBorrow) {
      this.testOnBorrow = testOnBorrow;
   }

   public final void setTestOnCreate(boolean testOnCreate) {
      this.testOnCreate = testOnCreate;
   }

   public final void setTestOnReturn(boolean testOnReturn) {
      this.testOnReturn = testOnReturn;
   }

   public final void setTestWhileIdle(boolean testWhileIdle) {
      this.testWhileIdle = testWhileIdle;
   }

   @Deprecated
   public final void setTimeBetweenEvictionRuns(Duration timeBetweenEvictionRuns) {
      this.durationBetweenEvictionRuns = PoolImplUtils.nonNull(timeBetweenEvictionRuns, BaseObjectPoolConfig.DEFAULT_DURATION_BETWEEN_EVICTION_RUNS);
      this.startEvictor(this.durationBetweenEvictionRuns);
   }

   @Deprecated
   public final void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
      this.setTimeBetweenEvictionRuns(Duration.ofMillis(timeBetweenEvictionRunsMillis));
   }

   final void startEvictor(Duration delay) {
      synchronized (this.evictionLock) {
         boolean isPositiverDelay = PoolImplUtils.isPositive(delay);
         if (this.evictor == null) {
            if (isPositiverDelay) {
               this.evictor = new BaseGenericObjectPool.Evictor();
               EvictionTimer.schedule(this.evictor, delay, delay);
            }
         } else if (isPositiverDelay) {
            synchronized (EvictionTimer.class) {
               EvictionTimer.cancel(this.evictor, this.evictorShutdownTimeoutDuration, true);
               this.evictor = null;
               this.evictionIterator = null;
               this.evictor = new BaseGenericObjectPool.Evictor();
               EvictionTimer.schedule(this.evictor, delay, delay);
            }
         } else {
            EvictionTimer.cancel(this.evictor, this.evictorShutdownTimeoutDuration, false);
         }
      }
   }

   void stopEvictor() {
      this.startEvictor(Duration.ofMillis(-1L));
   }

   final void swallowException(Exception swallowException) {
      SwallowedExceptionListener listener = this.getSwallowedExceptionListener();
      if (listener != null) {
         try {
            listener.onSwallowException(swallowException);
         } catch (VirtualMachineError e) {
            throw e;
         } catch (Throwable var5) {
         }
      }
   }

   @Override
   protected void toStringAppendFields(StringBuilder builder) {
      builder.append("maxTotal=");
      builder.append(this.maxTotal);
      builder.append(", blockWhenExhausted=");
      builder.append(this.blockWhenExhausted);
      builder.append(", maxWaitDuration=");
      builder.append(this.maxWaitDuration);
      builder.append(", lifo=");
      builder.append(this.lifo);
      builder.append(", fairness=");
      builder.append(this.fairness);
      builder.append(", testOnCreate=");
      builder.append(this.testOnCreate);
      builder.append(", testOnBorrow=");
      builder.append(this.testOnBorrow);
      builder.append(", testOnReturn=");
      builder.append(this.testOnReturn);
      builder.append(", testWhileIdle=");
      builder.append(this.testWhileIdle);
      builder.append(", durationBetweenEvictionRuns=");
      builder.append(this.durationBetweenEvictionRuns);
      builder.append(", numTestsPerEvictionRun=");
      builder.append(this.numTestsPerEvictionRun);
      builder.append(", minEvictableIdleTimeDuration=");
      builder.append(this.minEvictableIdleDuration);
      builder.append(", softMinEvictableIdleTimeDuration=");
      builder.append(this.softMinEvictableIdleDuration);
      builder.append(", evictionPolicy=");
      builder.append(this.evictionPolicy);
      builder.append(", closeLock=");
      builder.append(this.closeLock);
      builder.append(", closed=");
      builder.append(this.closed);
      builder.append(", evictionLock=");
      builder.append(this.evictionLock);
      builder.append(", evictor=");
      builder.append(this.evictor);
      builder.append(", evictionIterator=");
      builder.append(this.evictionIterator);
      builder.append(", factoryClassLoader=");
      builder.append(this.factoryClassLoader);
      builder.append(", oname=");
      builder.append(this.objectName);
      builder.append(", creationStackTrace=");
      builder.append(this.creationStackTrace);
      builder.append(", borrowedCount=");
      builder.append(this.borrowedCount);
      builder.append(", returnedCount=");
      builder.append(this.returnedCount);
      builder.append(", createdCount=");
      builder.append(this.createdCount);
      builder.append(", destroyedCount=");
      builder.append(this.destroyedCount);
      builder.append(", destroyedByEvictorCount=");
      builder.append(this.destroyedByEvictorCount);
      builder.append(", destroyedByBorrowValidationCount=");
      builder.append(this.destroyedByBorrowValidationCount);
      builder.append(", activeTimes=");
      builder.append(this.activeTimes);
      builder.append(", idleTimes=");
      builder.append(this.idleTimes);
      builder.append(", waitTimes=");
      builder.append(this.waitTimes);
      builder.append(", maxBorrowWaitDuration=");
      builder.append(this.maxBorrowWaitDuration);
      builder.append(", swallowedExceptionListener=");
      builder.append(this.swallowedExceptionListener);
   }

   final void updateStatsBorrow(PooledObject<T> p, Duration waitDuration) {
      this.borrowedCount.incrementAndGet();
      this.idleTimes.add(p.getIdleDuration());
      this.waitTimes.add(waitDuration);

      Duration currentMaxDuration;
      do {
         currentMaxDuration = this.maxBorrowWaitDuration.get();
      } while (currentMaxDuration.compareTo(waitDuration) < 0 && !this.maxBorrowWaitDuration.compareAndSet(currentMaxDuration, waitDuration));
   }

   final void updateStatsReturn(Duration activeTime) {
      this.returnedCount.incrementAndGet();
      this.activeTimes.add(activeTime);
   }

   class EvictionIterator implements Iterator<PooledObject<T>> {
      private final Deque<PooledObject<T>> idleObjects;
      private final Iterator<PooledObject<T>> idleObjectIterator;

      EvictionIterator(Deque<PooledObject<T>> idleObjects) {
         this.idleObjects = idleObjects;
         if (BaseGenericObjectPool.this.getLifo()) {
            this.idleObjectIterator = idleObjects.descendingIterator();
         } else {
            this.idleObjectIterator = idleObjects.iterator();
         }
      }

      public Deque<PooledObject<T>> getIdleObjects() {
         return this.idleObjects;
      }

      @Override
      public boolean hasNext() {
         return this.idleObjectIterator.hasNext();
      }

      public PooledObject<T> next() {
         return this.idleObjectIterator.next();
      }

      @Override
      public void remove() {
         this.idleObjectIterator.remove();
      }
   }

   class Evictor implements Runnable {
      private ScheduledFuture<?> scheduledFuture;

      void cancel() {
         this.scheduledFuture.cancel(false);
      }

      BaseGenericObjectPool<T> owner() {
         return BaseGenericObjectPool.this;
      }

      @Override
      public void run() {
         ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();

         try {
            if (BaseGenericObjectPool.this.factoryClassLoader != null) {
               ClassLoader cl = BaseGenericObjectPool.this.factoryClassLoader.get();
               if (cl == null) {
                  this.cancel();
                  return;
               }

               Thread.currentThread().setContextClassLoader(cl);
            }

            try {
               BaseGenericObjectPool.this.evict();
            } catch (Exception e) {
               BaseGenericObjectPool.this.swallowException(e);
            } catch (OutOfMemoryError oome) {
               oome.printStackTrace(System.err);
            }

            try {
               BaseGenericObjectPool.this.ensureMinIdle();
            } catch (Exception e) {
               BaseGenericObjectPool.this.swallowException(e);
            }
         } finally {
            Thread.currentThread().setContextClassLoader(savedClassLoader);
         }
      }

      void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
         this.scheduledFuture = scheduledFuture;
      }

      @Override
      public String toString() {
         return this.getClass().getName() + " [scheduledFuture=" + this.scheduledFuture + "]";
      }
   }

   static class IdentityWrapper<T> {
      private final T instance;

      public IdentityWrapper(T instance) {
         this.instance = instance;
      }

      @Override
      public boolean equals(Object other) {
         return other instanceof BaseGenericObjectPool.IdentityWrapper && ((BaseGenericObjectPool.IdentityWrapper)other).instance == this.instance;
      }

      public T getObject() {
         return this.instance;
      }

      @Override
      public int hashCode() {
         return System.identityHashCode(this.instance);
      }

      @Override
      public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("IdentityWrapper [instance=");
         builder.append(this.instance);
         builder.append("]");
         return builder.toString();
      }
   }

   private static class StatsStore {
      private static final int NONE = -1;
      private final AtomicLong[] values;
      private final int size;
      private int index;

      StatsStore(int size) {
         this.size = size;
         this.values = new AtomicLong[size];
         Arrays.setAll(this.values, i -> new AtomicLong(-1L));
      }

      void add(Duration value) {
         this.add(value.toMillis());
      }

      synchronized void add(long value) {
         this.values[this.index].set(value);
         this.index++;
         if (this.index == this.size) {
            this.index = 0;
         }
      }

      public long getMean() {
         double result = 0.0;
         int counter = 0;

         for (int i = 0; i < this.size; i++) {
            long value = this.values[i].get();
            if (value != -1L) {
               counter++;
               result = result * ((double)(counter - 1) / counter) + (double)value / counter;
            }
         }

         return (long)result;
      }

      Duration getMeanDuration() {
         return Duration.ofMillis(this.getMean());
      }

      synchronized List<AtomicLong> getValues() {
         return Arrays.stream(this.values, 0, this.index).collect(Collectors.toList());
      }

      @Override
      public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("StatsStore [");
         builder.append(this.getValues());
         builder.append("], size=");
         builder.append(this.size);
         builder.append(", index=");
         builder.append(this.index);
         builder.append("]");
         return builder.toString();
      }
   }
}
