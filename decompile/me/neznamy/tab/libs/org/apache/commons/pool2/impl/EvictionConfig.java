package me.neznamy.tab.libs.org.apache.commons.pool2.impl;

import java.time.Duration;

public class EvictionConfig {
   private static final Duration MAX_DURATION = Duration.ofMillis(Long.MAX_VALUE);
   private final Duration idleEvictDuration;
   private final Duration idleSoftEvictDuration;
   private final int minIdle;

   public EvictionConfig(Duration idleEvictDuration, Duration idleSoftEvictDuration, int minIdle) {
      this.idleEvictDuration = PoolImplUtils.isPositive(idleEvictDuration) ? idleEvictDuration : MAX_DURATION;
      this.idleSoftEvictDuration = PoolImplUtils.isPositive(idleSoftEvictDuration) ? idleSoftEvictDuration : MAX_DURATION;
      this.minIdle = minIdle;
   }

   @Deprecated
   public EvictionConfig(long poolIdleEvictMillis, long poolIdleSoftEvictMillis, int minIdle) {
      this(Duration.ofMillis(poolIdleEvictMillis), Duration.ofMillis(poolIdleSoftEvictMillis), minIdle);
   }

   public Duration getIdleEvictDuration() {
      return this.idleEvictDuration;
   }

   @Deprecated
   public long getIdleEvictTime() {
      return this.idleEvictDuration.toMillis();
   }

   @Deprecated
   public Duration getIdleEvictTimeDuration() {
      return this.idleEvictDuration;
   }

   public Duration getIdleSoftEvictDuration() {
      return this.idleSoftEvictDuration;
   }

   @Deprecated
   public long getIdleSoftEvictTime() {
      return this.idleSoftEvictDuration.toMillis();
   }

   @Deprecated
   public Duration getIdleSoftEvictTimeDuration() {
      return this.idleSoftEvictDuration;
   }

   public int getMinIdle() {
      return this.minIdle;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("EvictionConfig [idleEvictDuration=");
      builder.append(this.idleEvictDuration);
      builder.append(", idleSoftEvictDuration=");
      builder.append(this.idleSoftEvictDuration);
      builder.append(", minIdle=");
      builder.append(this.minIdle);
      builder.append("]");
      return builder.toString();
   }
}
