package me.neznamy.tab.libs.org.apache.commons.pool2.impl;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.time.Duration;

public class AbandonedConfig {
   private static final Duration DEFAULT_REMOVE_ABANDONED_TIMEOUT_DURATION = Duration.ofMinutes(5L);
   private boolean removeAbandonedOnBorrow;
   private boolean removeAbandonedOnMaintenance;
   private Duration removeAbandonedTimeoutDuration = DEFAULT_REMOVE_ABANDONED_TIMEOUT_DURATION;
   private boolean logAbandoned;
   private boolean requireFullStackTrace = true;
   private PrintWriter logWriter = new PrintWriter(new OutputStreamWriter(System.out, Charset.defaultCharset()));
   private boolean useUsageTracking;

   public static AbandonedConfig copy(AbandonedConfig abandonedConfig) {
      return abandonedConfig == null ? null : new AbandonedConfig(abandonedConfig);
   }

   public AbandonedConfig() {
   }

   private AbandonedConfig(AbandonedConfig abandonedConfig) {
      this.setLogAbandoned(abandonedConfig.getLogAbandoned());
      this.setLogWriter(abandonedConfig.getLogWriter());
      this.setRemoveAbandonedOnBorrow(abandonedConfig.getRemoveAbandonedOnBorrow());
      this.setRemoveAbandonedOnMaintenance(abandonedConfig.getRemoveAbandonedOnMaintenance());
      this.setRemoveAbandonedTimeout(abandonedConfig.getRemoveAbandonedTimeoutDuration());
      this.setUseUsageTracking(abandonedConfig.getUseUsageTracking());
      this.setRequireFullStackTrace(abandonedConfig.getRequireFullStackTrace());
   }

   public boolean getLogAbandoned() {
      return this.logAbandoned;
   }

   public PrintWriter getLogWriter() {
      return this.logWriter;
   }

   public boolean getRemoveAbandonedOnBorrow() {
      return this.removeAbandonedOnBorrow;
   }

   public boolean getRemoveAbandonedOnMaintenance() {
      return this.removeAbandonedOnMaintenance;
   }

   @Deprecated
   public int getRemoveAbandonedTimeout() {
      return (int)this.removeAbandonedTimeoutDuration.getSeconds();
   }

   public Duration getRemoveAbandonedTimeoutDuration() {
      return this.removeAbandonedTimeoutDuration;
   }

   public boolean getRequireFullStackTrace() {
      return this.requireFullStackTrace;
   }

   public boolean getUseUsageTracking() {
      return this.useUsageTracking;
   }

   public void setLogAbandoned(boolean logAbandoned) {
      this.logAbandoned = logAbandoned;
   }

   public void setLogWriter(PrintWriter logWriter) {
      this.logWriter = logWriter;
   }

   public void setRemoveAbandonedOnBorrow(boolean removeAbandonedOnBorrow) {
      this.removeAbandonedOnBorrow = removeAbandonedOnBorrow;
   }

   public void setRemoveAbandonedOnMaintenance(boolean removeAbandonedOnMaintenance) {
      this.removeAbandonedOnMaintenance = removeAbandonedOnMaintenance;
   }

   public void setRemoveAbandonedTimeout(Duration removeAbandonedTimeout) {
      this.removeAbandonedTimeoutDuration = PoolImplUtils.nonNull(removeAbandonedTimeout, DEFAULT_REMOVE_ABANDONED_TIMEOUT_DURATION);
   }

   @Deprecated
   public void setRemoveAbandonedTimeout(int removeAbandonedTimeoutSeconds) {
      this.setRemoveAbandonedTimeout(Duration.ofSeconds(removeAbandonedTimeoutSeconds));
   }

   public void setRequireFullStackTrace(boolean requireFullStackTrace) {
      this.requireFullStackTrace = requireFullStackTrace;
   }

   public void setUseUsageTracking(boolean useUsageTracking) {
      this.useUsageTracking = useUsageTracking;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("AbandonedConfig [removeAbandonedOnBorrow=");
      builder.append(this.removeAbandonedOnBorrow);
      builder.append(", removeAbandonedOnMaintenance=");
      builder.append(this.removeAbandonedOnMaintenance);
      builder.append(", removeAbandonedTimeoutDuration=");
      builder.append(this.removeAbandonedTimeoutDuration);
      builder.append(", logAbandoned=");
      builder.append(this.logAbandoned);
      builder.append(", logWriter=");
      builder.append(this.logWriter);
      builder.append(", useUsageTracking=");
      builder.append(this.useUsageTracking);
      builder.append("]");
      return builder.toString();
   }
}
