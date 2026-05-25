package me.neznamy.tab.libs.org.apache.commons.pool2.impl;

import java.time.Duration;
import me.neznamy.tab.libs.org.apache.commons.pool2.BaseObject;

public abstract class BaseObjectPoolConfig<T> extends BaseObject implements Cloneable {
   public static final boolean DEFAULT_LIFO = true;
   public static final boolean DEFAULT_FAIRNESS = false;
   @Deprecated
   public static final long DEFAULT_MAX_WAIT_MILLIS = -1L;
   public static final Duration DEFAULT_MAX_WAIT = Duration.ofMillis(-1L);
   @Deprecated
   public static final long DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS = 1800000L;
   public static final Duration DEFAULT_MIN_EVICTABLE_IDLE_DURATION = Duration.ofMillis(1800000L);
   @Deprecated
   public static final Duration DEFAULT_MIN_EVICTABLE_IDLE_TIME = Duration.ofMillis(1800000L);
   @Deprecated
   public static final long DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS = -1L;
   @Deprecated
   public static final Duration DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME = Duration.ofMillis(-1L);
   public static final Duration DEFAULT_SOFT_MIN_EVICTABLE_IDLE_DURATION = Duration.ofMillis(-1L);
   @Deprecated
   public static final long DEFAULT_EVICTOR_SHUTDOWN_TIMEOUT_MILLIS = 10000L;
   public static final Duration DEFAULT_EVICTOR_SHUTDOWN_TIMEOUT = Duration.ofMillis(10000L);
   public static final int DEFAULT_NUM_TESTS_PER_EVICTION_RUN = 3;
   public static final boolean DEFAULT_TEST_ON_CREATE = false;
   public static final boolean DEFAULT_TEST_ON_BORROW = false;
   public static final boolean DEFAULT_TEST_ON_RETURN = false;
   public static final boolean DEFAULT_TEST_WHILE_IDLE = false;
   @Deprecated
   public static final long DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS = -1L;
   public static final Duration DEFAULT_DURATION_BETWEEN_EVICTION_RUNS = Duration.ofMillis(-1L);
   @Deprecated
   public static final Duration DEFAULT_TIME_BETWEEN_EVICTION_RUNS = Duration.ofMillis(-1L);
   public static final boolean DEFAULT_BLOCK_WHEN_EXHAUSTED = true;
   public static final boolean DEFAULT_JMX_ENABLE = true;
   public static final String DEFAULT_JMX_NAME_PREFIX = "pool";
   public static final String DEFAULT_JMX_NAME_BASE = null;
   public static final String DEFAULT_EVICTION_POLICY_CLASS_NAME = DefaultEvictionPolicy.class.getName();
   private boolean lifo = true;
   private boolean fairness = false;
   private Duration maxWaitDuration = DEFAULT_MAX_WAIT;
   private Duration minEvictableIdleDuration = DEFAULT_MIN_EVICTABLE_IDLE_TIME;
   private Duration evictorShutdownTimeoutDuration = DEFAULT_EVICTOR_SHUTDOWN_TIMEOUT;
   private Duration softMinEvictableIdleDuration = DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME;
   private int numTestsPerEvictionRun = 3;
   private EvictionPolicy<T> evictionPolicy;
   private String evictionPolicyClassName = DEFAULT_EVICTION_POLICY_CLASS_NAME;
   private boolean testOnCreate = false;
   private boolean testOnBorrow = false;
   private boolean testOnReturn = false;
   private boolean testWhileIdle = false;
   private Duration durationBetweenEvictionRuns = DEFAULT_DURATION_BETWEEN_EVICTION_RUNS;
   private boolean blockWhenExhausted = true;
   private boolean jmxEnabled = true;
   private String jmxNamePrefix = "pool";
   private String jmxNameBase = DEFAULT_JMX_NAME_BASE;

   public boolean getBlockWhenExhausted() {
      return this.blockWhenExhausted;
   }

   public Duration getDurationBetweenEvictionRuns() {
      return this.durationBetweenEvictionRuns;
   }

   public EvictionPolicy<T> getEvictionPolicy() {
      return this.evictionPolicy;
   }

   public String getEvictionPolicyClassName() {
      return this.evictionPolicyClassName;
   }

   @Deprecated
   public Duration getEvictorShutdownTimeout() {
      return this.evictorShutdownTimeoutDuration;
   }

   public Duration getEvictorShutdownTimeoutDuration() {
      return this.evictorShutdownTimeoutDuration;
   }

   @Deprecated
   public long getEvictorShutdownTimeoutMillis() {
      return this.evictorShutdownTimeoutDuration.toMillis();
   }

   public boolean getFairness() {
      return this.fairness;
   }

   public boolean getJmxEnabled() {
      return this.jmxEnabled;
   }

   public String getJmxNameBase() {
      return this.jmxNameBase;
   }

   public String getJmxNamePrefix() {
      return this.jmxNamePrefix;
   }

   public boolean getLifo() {
      return this.lifo;
   }

   public Duration getMaxWaitDuration() {
      return this.maxWaitDuration;
   }

   @Deprecated
   public long getMaxWaitMillis() {
      return this.maxWaitDuration.toMillis();
   }

   public Duration getMinEvictableIdleDuration() {
      return this.minEvictableIdleDuration;
   }

   @Deprecated
   public Duration getMinEvictableIdleTime() {
      return this.minEvictableIdleDuration;
   }

   @Deprecated
   public long getMinEvictableIdleTimeMillis() {
      return this.minEvictableIdleDuration.toMillis();
   }

   public int getNumTestsPerEvictionRun() {
      return this.numTestsPerEvictionRun;
   }

   public Duration getSoftMinEvictableIdleDuration() {
      return this.softMinEvictableIdleDuration;
   }

   @Deprecated
   public Duration getSoftMinEvictableIdleTime() {
      return this.softMinEvictableIdleDuration;
   }

   @Deprecated
   public long getSoftMinEvictableIdleTimeMillis() {
      return this.softMinEvictableIdleDuration.toMillis();
   }

   public boolean getTestOnBorrow() {
      return this.testOnBorrow;
   }

   public boolean getTestOnCreate() {
      return this.testOnCreate;
   }

   public boolean getTestOnReturn() {
      return this.testOnReturn;
   }

   public boolean getTestWhileIdle() {
      return this.testWhileIdle;
   }

   @Deprecated
   public Duration getTimeBetweenEvictionRuns() {
      return this.durationBetweenEvictionRuns;
   }

   @Deprecated
   public long getTimeBetweenEvictionRunsMillis() {
      return this.durationBetweenEvictionRuns.toMillis();
   }

   public void setBlockWhenExhausted(boolean blockWhenExhausted) {
      this.blockWhenExhausted = blockWhenExhausted;
   }

   public void setEvictionPolicy(EvictionPolicy<T> evictionPolicy) {
      this.evictionPolicy = evictionPolicy;
   }

   public void setEvictionPolicyClassName(String evictionPolicyClassName) {
      this.evictionPolicyClassName = evictionPolicyClassName;
   }

   public void setEvictorShutdownTimeout(Duration evictorShutdownTimeoutDuration) {
      this.evictorShutdownTimeoutDuration = PoolImplUtils.nonNull(evictorShutdownTimeoutDuration, DEFAULT_EVICTOR_SHUTDOWN_TIMEOUT);
   }

   @Deprecated
   public void setEvictorShutdownTimeoutMillis(Duration evictorShutdownTimeout) {
      this.setEvictorShutdownTimeout(evictorShutdownTimeout);
   }

   @Deprecated
   public void setEvictorShutdownTimeoutMillis(long evictorShutdownTimeoutMillis) {
      this.setEvictorShutdownTimeout(Duration.ofMillis(evictorShutdownTimeoutMillis));
   }

   public void setFairness(boolean fairness) {
      this.fairness = fairness;
   }

   public void setJmxEnabled(boolean jmxEnabled) {
      this.jmxEnabled = jmxEnabled;
   }

   public void setJmxNameBase(String jmxNameBase) {
      this.jmxNameBase = jmxNameBase;
   }

   public void setJmxNamePrefix(String jmxNamePrefix) {
      this.jmxNamePrefix = jmxNamePrefix;
   }

   public void setLifo(boolean lifo) {
      this.lifo = lifo;
   }

   public void setMaxWait(Duration maxWaitDuration) {
      this.maxWaitDuration = PoolImplUtils.nonNull(maxWaitDuration, DEFAULT_MAX_WAIT);
   }

   @Deprecated
   public void setMaxWaitMillis(long maxWaitMillis) {
      this.setMaxWait(Duration.ofMillis(maxWaitMillis));
   }

   public void setMinEvictableIdleDuration(Duration minEvictableIdleTime) {
      this.minEvictableIdleDuration = PoolImplUtils.nonNull(minEvictableIdleTime, DEFAULT_MIN_EVICTABLE_IDLE_TIME);
   }

   @Deprecated
   public void setMinEvictableIdleTime(Duration minEvictableIdleTime) {
      this.minEvictableIdleDuration = PoolImplUtils.nonNull(minEvictableIdleTime, DEFAULT_MIN_EVICTABLE_IDLE_TIME);
   }

   @Deprecated
   public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
      this.minEvictableIdleDuration = Duration.ofMillis(minEvictableIdleTimeMillis);
   }

   public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
      this.numTestsPerEvictionRun = numTestsPerEvictionRun;
   }

   public void setSoftMinEvictableIdleDuration(Duration softMinEvictableIdleTime) {
      this.softMinEvictableIdleDuration = PoolImplUtils.nonNull(softMinEvictableIdleTime, DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME);
   }

   @Deprecated
   public void setSoftMinEvictableIdleTime(Duration softMinEvictableIdleTime) {
      this.softMinEvictableIdleDuration = PoolImplUtils.nonNull(softMinEvictableIdleTime, DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME);
   }

   @Deprecated
   public void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
      this.setSoftMinEvictableIdleTime(Duration.ofMillis(softMinEvictableIdleTimeMillis));
   }

   public void setTestOnBorrow(boolean testOnBorrow) {
      this.testOnBorrow = testOnBorrow;
   }

   public void setTestOnCreate(boolean testOnCreate) {
      this.testOnCreate = testOnCreate;
   }

   public void setTestOnReturn(boolean testOnReturn) {
      this.testOnReturn = testOnReturn;
   }

   public void setTestWhileIdle(boolean testWhileIdle) {
      this.testWhileIdle = testWhileIdle;
   }

   public void setTimeBetweenEvictionRuns(Duration timeBetweenEvictionRuns) {
      this.durationBetweenEvictionRuns = PoolImplUtils.nonNull(timeBetweenEvictionRuns, DEFAULT_DURATION_BETWEEN_EVICTION_RUNS);
   }

   @Deprecated
   public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
      this.setTimeBetweenEvictionRuns(Duration.ofMillis(timeBetweenEvictionRunsMillis));
   }

   @Override
   protected void toStringAppendFields(StringBuilder builder) {
      builder.append("lifo=");
      builder.append(this.lifo);
      builder.append(", fairness=");
      builder.append(this.fairness);
      builder.append(", maxWaitDuration=");
      builder.append(this.maxWaitDuration);
      builder.append(", minEvictableIdleTime=");
      builder.append(this.minEvictableIdleDuration);
      builder.append(", softMinEvictableIdleTime=");
      builder.append(this.softMinEvictableIdleDuration);
      builder.append(", numTestsPerEvictionRun=");
      builder.append(this.numTestsPerEvictionRun);
      builder.append(", evictionPolicyClassName=");
      builder.append(this.evictionPolicyClassName);
      builder.append(", testOnCreate=");
      builder.append(this.testOnCreate);
      builder.append(", testOnBorrow=");
      builder.append(this.testOnBorrow);
      builder.append(", testOnReturn=");
      builder.append(this.testOnReturn);
      builder.append(", testWhileIdle=");
      builder.append(this.testWhileIdle);
      builder.append(", timeBetweenEvictionRuns=");
      builder.append(this.durationBetweenEvictionRuns);
      builder.append(", blockWhenExhausted=");
      builder.append(this.blockWhenExhausted);
      builder.append(", jmxEnabled=");
      builder.append(this.jmxEnabled);
      builder.append(", jmxNamePrefix=");
      builder.append(this.jmxNamePrefix);
      builder.append(", jmxNameBase=");
      builder.append(this.jmxNameBase);
   }
}
