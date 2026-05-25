package me.neznamy.tab.libs.redis.clients.jedis;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Experimental;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisConnectionException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisValidationException;

@Experimental
public final class MultiClusterClientConfig {
   private static final int RETRY_MAX_ATTEMPTS_DEFAULT = 3;
   private static final int RETRY_WAIT_DURATION_DEFAULT = 500;
   private static final int RETRY_WAIT_DURATION_EXPONENTIAL_BACKOFF_MULTIPLIER_DEFAULT = 2;
   private static final List<Class> RETRY_INCLUDED_EXCEPTIONS_DEFAULT = Arrays.asList(JedisConnectionException.class);
   private static final float CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD_DEFAULT = 50.0F;
   private static final int CIRCUIT_BREAKER_SLIDING_WINDOW_MIN_CALLS_DEFAULT = 100;
   private static final SlidingWindowType CIRCUIT_BREAKER_SLIDING_WINDOW_TYPE_DEFAULT = SlidingWindowType.COUNT_BASED;
   private static final int CIRCUIT_BREAKER_SLIDING_WINDOW_SIZE_DEFAULT = 100;
   private static final int CIRCUIT_BREAKER_SLOW_CALL_DURATION_THRESHOLD_DEFAULT = 60000;
   private static final float CIRCUIT_BREAKER_SLOW_CALL_RATE_THRESHOLD_DEFAULT = 100.0F;
   private static final List<Class> CIRCUIT_BREAKER_INCLUDED_EXCEPTIONS_DEFAULT = Arrays.asList(JedisConnectionException.class);
   private static final List<Class<? extends Throwable>> FALLBACK_EXCEPTIONS_DEFAULT = Arrays.asList(CallNotPermittedException.class);
   private final MultiClusterClientConfig.ClusterConfig[] clusterConfigs;
   private int retryMaxAttempts;
   private Duration retryWaitDuration;
   private int retryWaitDurationExponentialBackoffMultiplier;
   private List<Class> retryIncludedExceptionList;
   private List<Class> retryIgnoreExceptionList;
   private float circuitBreakerFailureRateThreshold;
   private int circuitBreakerSlidingWindowMinCalls;
   private SlidingWindowType circuitBreakerSlidingWindowType;
   private int circuitBreakerSlidingWindowSize;
   private Duration circuitBreakerSlowCallDurationThreshold;
   private float circuitBreakerSlowCallRateThreshold;
   private List<Class> circuitBreakerIncludedExceptionList;
   private List<Class> circuitBreakerIgnoreExceptionList;
   private List<Class<? extends Throwable>> fallbackExceptionList;

   public MultiClusterClientConfig(MultiClusterClientConfig.ClusterConfig[] clusterConfigs) {
      this.clusterConfigs = clusterConfigs;
   }

   public MultiClusterClientConfig.ClusterConfig[] getClusterConfigs() {
      return this.clusterConfigs;
   }

   public int getRetryMaxAttempts() {
      return this.retryMaxAttempts;
   }

   public Duration getRetryWaitDuration() {
      return this.retryWaitDuration;
   }

   public int getRetryWaitDurationExponentialBackoffMultiplier() {
      return this.retryWaitDurationExponentialBackoffMultiplier;
   }

   public float getCircuitBreakerFailureRateThreshold() {
      return this.circuitBreakerFailureRateThreshold;
   }

   public int getCircuitBreakerSlidingWindowMinCalls() {
      return this.circuitBreakerSlidingWindowMinCalls;
   }

   public int getCircuitBreakerSlidingWindowSize() {
      return this.circuitBreakerSlidingWindowSize;
   }

   public Duration getCircuitBreakerSlowCallDurationThreshold() {
      return this.circuitBreakerSlowCallDurationThreshold;
   }

   public float getCircuitBreakerSlowCallRateThreshold() {
      return this.circuitBreakerSlowCallRateThreshold;
   }

   public List<Class> getRetryIncludedExceptionList() {
      return this.retryIncludedExceptionList;
   }

   public List<Class> getRetryIgnoreExceptionList() {
      return this.retryIgnoreExceptionList;
   }

   public List<Class> getCircuitBreakerIncludedExceptionList() {
      return this.circuitBreakerIncludedExceptionList;
   }

   public List<Class> getCircuitBreakerIgnoreExceptionList() {
      return this.circuitBreakerIgnoreExceptionList;
   }

   public SlidingWindowType getCircuitBreakerSlidingWindowType() {
      return this.circuitBreakerSlidingWindowType;
   }

   public List<Class<? extends Throwable>> getFallbackExceptionList() {
      return this.fallbackExceptionList;
   }

   public static class Builder {
      private MultiClusterClientConfig.ClusterConfig[] clusterConfigs;
      private int retryMaxAttempts = 3;
      private int retryWaitDuration = 500;
      private int retryWaitDurationExponentialBackoffMultiplier = 2;
      private List<Class> retryIncludedExceptionList = MultiClusterClientConfig.RETRY_INCLUDED_EXCEPTIONS_DEFAULT;
      private List<Class> retryIgnoreExceptionList = null;
      private float circuitBreakerFailureRateThreshold = 50.0F;
      private int circuitBreakerSlidingWindowMinCalls = 100;
      private SlidingWindowType circuitBreakerSlidingWindowType = MultiClusterClientConfig.CIRCUIT_BREAKER_SLIDING_WINDOW_TYPE_DEFAULT;
      private int circuitBreakerSlidingWindowSize = 100;
      private int circuitBreakerSlowCallDurationThreshold = 60000;
      private float circuitBreakerSlowCallRateThreshold = 100.0F;
      private List<Class> circuitBreakerIncludedExceptionList = MultiClusterClientConfig.CIRCUIT_BREAKER_INCLUDED_EXCEPTIONS_DEFAULT;
      private List<Class> circuitBreakerIgnoreExceptionList = null;
      private List<Class<? extends Throwable>> fallbackExceptionList = MultiClusterClientConfig.FALLBACK_EXCEPTIONS_DEFAULT;

      public Builder(MultiClusterClientConfig.ClusterConfig[] clusterConfigs) {
         if (clusterConfigs != null && clusterConfigs.length >= 1) {
            for (int i = 0; i < clusterConfigs.length; i++) {
               clusterConfigs[i].setPriority(i + 1);
            }

            this.clusterConfigs = clusterConfigs;
         } else {
            throw new JedisValidationException("ClusterClientConfigs are required for MultiClusterPooledConnectionProvider");
         }
      }

      public Builder(List<MultiClusterClientConfig.ClusterConfig> clusterConfigs) {
         this(clusterConfigs.toArray(new MultiClusterClientConfig.ClusterConfig[0]));
      }

      public MultiClusterClientConfig.Builder retryMaxAttempts(int retryMaxAttempts) {
         this.retryMaxAttempts = retryMaxAttempts;
         return this;
      }

      public MultiClusterClientConfig.Builder retryWaitDuration(int retryWaitDuration) {
         this.retryWaitDuration = retryWaitDuration;
         return this;
      }

      public MultiClusterClientConfig.Builder retryWaitDurationExponentialBackoffMultiplier(int retryWaitDurationExponentialBackoffMultiplier) {
         this.retryWaitDurationExponentialBackoffMultiplier = retryWaitDurationExponentialBackoffMultiplier;
         return this;
      }

      public MultiClusterClientConfig.Builder retryIncludedExceptionList(List<Class> retryIncludedExceptionList) {
         this.retryIncludedExceptionList = retryIncludedExceptionList;
         return this;
      }

      public MultiClusterClientConfig.Builder retryIgnoreExceptionList(List<Class> retryIgnoreExceptionList) {
         this.retryIgnoreExceptionList = retryIgnoreExceptionList;
         return this;
      }

      public MultiClusterClientConfig.Builder circuitBreakerFailureRateThreshold(float circuitBreakerFailureRateThreshold) {
         this.circuitBreakerFailureRateThreshold = circuitBreakerFailureRateThreshold;
         return this;
      }

      public MultiClusterClientConfig.Builder circuitBreakerSlidingWindowMinCalls(int circuitBreakerSlidingWindowMinCalls) {
         this.circuitBreakerSlidingWindowMinCalls = circuitBreakerSlidingWindowMinCalls;
         return this;
      }

      public MultiClusterClientConfig.Builder circuitBreakerSlidingWindowType(SlidingWindowType circuitBreakerSlidingWindowType) {
         this.circuitBreakerSlidingWindowType = circuitBreakerSlidingWindowType;
         return this;
      }

      public MultiClusterClientConfig.Builder circuitBreakerSlidingWindowSize(int circuitBreakerSlidingWindowSize) {
         this.circuitBreakerSlidingWindowSize = circuitBreakerSlidingWindowSize;
         return this;
      }

      public MultiClusterClientConfig.Builder circuitBreakerSlowCallDurationThreshold(int circuitBreakerSlowCallDurationThreshold) {
         this.circuitBreakerSlowCallDurationThreshold = circuitBreakerSlowCallDurationThreshold;
         return this;
      }

      public MultiClusterClientConfig.Builder circuitBreakerSlowCallRateThreshold(float circuitBreakerSlowCallRateThreshold) {
         this.circuitBreakerSlowCallRateThreshold = circuitBreakerSlowCallRateThreshold;
         return this;
      }

      public MultiClusterClientConfig.Builder circuitBreakerIncludedExceptionList(List<Class> circuitBreakerIncludedExceptionList) {
         this.circuitBreakerIncludedExceptionList = circuitBreakerIncludedExceptionList;
         return this;
      }

      public MultiClusterClientConfig.Builder circuitBreakerIgnoreExceptionList(List<Class> circuitBreakerIgnoreExceptionList) {
         this.circuitBreakerIgnoreExceptionList = circuitBreakerIgnoreExceptionList;
         return this;
      }

      @Deprecated
      public MultiClusterClientConfig.Builder circuitBreakerFallbackExceptionList(List<Class<? extends Throwable>> circuitBreakerFallbackExceptionList) {
         return this.fallbackExceptionList(circuitBreakerFallbackExceptionList);
      }

      public MultiClusterClientConfig.Builder fallbackExceptionList(List<Class<? extends Throwable>> fallbackExceptionList) {
         this.fallbackExceptionList = fallbackExceptionList;
         return this;
      }

      public MultiClusterClientConfig build() {
         MultiClusterClientConfig config = new MultiClusterClientConfig(this.clusterConfigs);
         config.retryMaxAttempts = this.retryMaxAttempts;
         config.retryWaitDuration = Duration.ofMillis(this.retryWaitDuration);
         config.retryWaitDurationExponentialBackoffMultiplier = this.retryWaitDurationExponentialBackoffMultiplier;
         config.retryIncludedExceptionList = this.retryIncludedExceptionList;
         config.retryIgnoreExceptionList = this.retryIgnoreExceptionList;
         config.circuitBreakerFailureRateThreshold = this.circuitBreakerFailureRateThreshold;
         config.circuitBreakerSlidingWindowMinCalls = this.circuitBreakerSlidingWindowMinCalls;
         config.circuitBreakerSlidingWindowType = this.circuitBreakerSlidingWindowType;
         config.circuitBreakerSlidingWindowSize = this.circuitBreakerSlidingWindowSize;
         config.circuitBreakerSlowCallDurationThreshold = Duration.ofMillis(this.circuitBreakerSlowCallDurationThreshold);
         config.circuitBreakerSlowCallRateThreshold = this.circuitBreakerSlowCallRateThreshold;
         config.circuitBreakerIncludedExceptionList = this.circuitBreakerIncludedExceptionList;
         config.circuitBreakerIgnoreExceptionList = this.circuitBreakerIgnoreExceptionList;
         config.fallbackExceptionList = this.fallbackExceptionList;
         return config;
      }
   }

   public static class ClusterConfig {
      private int priority;
      private HostAndPort hostAndPort;
      private JedisClientConfig clientConfig;
      private GenericObjectPoolConfig<Connection> connectionPoolConfig;

      public ClusterConfig(HostAndPort hostAndPort, JedisClientConfig clientConfig) {
         this.hostAndPort = hostAndPort;
         this.clientConfig = clientConfig;
      }

      public ClusterConfig(HostAndPort hostAndPort, JedisClientConfig clientConfig, GenericObjectPoolConfig<Connection> connectionPoolConfig) {
         this.hostAndPort = hostAndPort;
         this.clientConfig = clientConfig;
         this.connectionPoolConfig = connectionPoolConfig;
      }

      public int getPriority() {
         return this.priority;
      }

      private void setPriority(int priority) {
         this.priority = priority;
      }

      public HostAndPort getHostAndPort() {
         return this.hostAndPort;
      }

      public JedisClientConfig getJedisClientConfig() {
         return this.clientConfig;
      }

      public GenericObjectPoolConfig<Connection> getConnectionPoolConfig() {
         return this.connectionPoolConfig;
      }
   }
}
