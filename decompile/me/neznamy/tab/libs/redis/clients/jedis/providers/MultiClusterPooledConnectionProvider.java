package me.neznamy.tab.libs.redis.clients.jedis.providers;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker.State;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.Retry.EventPublisher;
import io.github.resilience4j.retry.RetryConfig.Builder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Connection;
import me.neznamy.tab.libs.redis.clients.jedis.ConnectionPool;
import me.neznamy.tab.libs.redis.clients.jedis.MultiClusterClientConfig;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Experimental;
import me.neznamy.tab.libs.redis.clients.jedis.annots.VisibleForTesting;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisConnectionException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisValidationException;
import me.neznamy.tab.libs.redis.clients.jedis.util.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Experimental
public class MultiClusterPooledConnectionProvider implements ConnectionProvider {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final Map<Integer, MultiClusterPooledConnectionProvider.Cluster> multiClusterMap = new ConcurrentHashMap<>();
   private volatile Integer activeMultiClusterIndex = 1;
   private final Lock activeClusterIndexLock = new ReentrantLock(true);
   private volatile boolean lastClusterCircuitBreakerForcedOpen = false;
   private Consumer<String> clusterFailoverPostProcessor;
   private List<Class<? extends Throwable>> fallbackExceptionList;

   public MultiClusterPooledConnectionProvider(MultiClusterClientConfig multiClusterClientConfig) {
      if (multiClusterClientConfig == null) {
         throw new JedisValidationException("MultiClusterClientConfig must not be NULL for MultiClusterPooledConnectionProvider");
      }

      Builder retryConfigBuilder = RetryConfig.custom();
      retryConfigBuilder.maxAttempts(multiClusterClientConfig.getRetryMaxAttempts());
      retryConfigBuilder.intervalFunction(
         IntervalFunction.ofExponentialBackoff(
            multiClusterClientConfig.getRetryWaitDuration(), multiClusterClientConfig.getRetryWaitDurationExponentialBackoffMultiplier()
         )
      );
      retryConfigBuilder.failAfterMaxAttempts(false);
      retryConfigBuilder.retryExceptions(multiClusterClientConfig.getRetryIncludedExceptionList().stream().toArray(Class[]::new));
      List<Class> retryIgnoreExceptionList = multiClusterClientConfig.getRetryIgnoreExceptionList();
      if (retryIgnoreExceptionList != null) {
         retryConfigBuilder.ignoreExceptions(retryIgnoreExceptionList.stream().toArray(Class[]::new));
      }

      RetryConfig retryConfig = retryConfigBuilder.build();
      io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.Builder circuitBreakerConfigBuilder = CircuitBreakerConfig.custom();
      circuitBreakerConfigBuilder.failureRateThreshold(multiClusterClientConfig.getCircuitBreakerFailureRateThreshold());
      circuitBreakerConfigBuilder.slowCallRateThreshold(multiClusterClientConfig.getCircuitBreakerSlowCallRateThreshold());
      circuitBreakerConfigBuilder.slowCallDurationThreshold(multiClusterClientConfig.getCircuitBreakerSlowCallDurationThreshold());
      circuitBreakerConfigBuilder.minimumNumberOfCalls(multiClusterClientConfig.getCircuitBreakerSlidingWindowMinCalls());
      circuitBreakerConfigBuilder.slidingWindowType(multiClusterClientConfig.getCircuitBreakerSlidingWindowType());
      circuitBreakerConfigBuilder.slidingWindowSize(multiClusterClientConfig.getCircuitBreakerSlidingWindowSize());
      circuitBreakerConfigBuilder.recordExceptions(multiClusterClientConfig.getCircuitBreakerIncludedExceptionList().stream().toArray(Class[]::new));
      circuitBreakerConfigBuilder.automaticTransitionFromOpenToHalfOpenEnabled(false);
      List<Class> circuitBreakerIgnoreExceptionList = multiClusterClientConfig.getCircuitBreakerIgnoreExceptionList();
      if (circuitBreakerIgnoreExceptionList != null) {
         circuitBreakerConfigBuilder.ignoreExceptions(circuitBreakerIgnoreExceptionList.stream().toArray(Class[]::new));
      }

      CircuitBreakerConfig circuitBreakerConfig = circuitBreakerConfigBuilder.build();
      MultiClusterClientConfig.ClusterConfig[] clusterConfigs = multiClusterClientConfig.getClusterConfigs();

      for (MultiClusterClientConfig.ClusterConfig config : clusterConfigs) {
         GenericObjectPoolConfig<Connection> poolConfig = config.getConnectionPoolConfig();
         String clusterId = "cluster:" + config.getPriority() + ":" + config.getHostAndPort();
         Retry retry = RetryRegistry.of(retryConfig).retry(clusterId);
         EventPublisher retryPublisher = retry.getEventPublisher();
         retryPublisher.onRetry(event -> this.log.warn(String.valueOf(event)));
         retryPublisher.onError(event -> this.log.error(String.valueOf(event)));
         CircuitBreaker circuitBreaker = CircuitBreakerRegistry.of(circuitBreakerConfig).circuitBreaker(clusterId);
         io.github.resilience4j.circuitbreaker.CircuitBreaker.EventPublisher circuitBreakerEventPublisher = circuitBreaker.getEventPublisher();
         circuitBreakerEventPublisher.onCallNotPermitted(event -> this.log.error(String.valueOf(event)));
         circuitBreakerEventPublisher.onError(event -> this.log.error(String.valueOf(event)));
         circuitBreakerEventPublisher.onFailureRateExceeded(event -> this.log.error(String.valueOf(event)));
         circuitBreakerEventPublisher.onSlowCallRateExceeded(event -> this.log.error(String.valueOf(event)));
         circuitBreakerEventPublisher.onStateTransition(event -> this.log.warn(String.valueOf(event)));
         if (poolConfig != null) {
            this.multiClusterMap
               .put(
                  config.getPriority(),
                  new MultiClusterPooledConnectionProvider.Cluster(
                     new ConnectionPool(config.getHostAndPort(), config.getJedisClientConfig(), poolConfig), retry, circuitBreaker
                  )
               );
         } else {
            this.multiClusterMap
               .put(
                  config.getPriority(),
                  new MultiClusterPooledConnectionProvider.Cluster(
                     new ConnectionPool(config.getHostAndPort(), config.getJedisClientConfig()), retry, circuitBreaker
                  )
               );
         }
      }

      this.fallbackExceptionList = multiClusterClientConfig.getFallbackExceptionList();
   }

   public int incrementActiveMultiClusterIndex() {
      this.activeClusterIndexLock.lock();

      try {
         String originalClusterName = this.getClusterCircuitBreaker().getName();
         if (this.activeMultiClusterIndex + 1 > this.multiClusterMap.size()) {
            this.lastClusterCircuitBreakerForcedOpen = true;
            throw new JedisConnectionException(
               "Cluster/database endpoint could not failover since the MultiClusterClientConfig was not provided with an additional cluster/database endpoint according to its prioritized sequence. If applicable, consider failing back OR restarting with an available cluster/database endpoint."
            );
         }

         Integer circuitBreaker = this.activeMultiClusterIndex;
         Integer var3 = this.activeMultiClusterIndex = this.activeMultiClusterIndex + 1;
         CircuitBreaker circuitBreakerx = this.getClusterCircuitBreaker();
         if (State.FORCED_OPEN.equals(circuitBreakerx.getState())) {
            this.incrementActiveMultiClusterIndex();
         } else {
            this.log.warn("Cluster/database endpoint successfully updated from '{}' to '{}'", originalClusterName, circuitBreakerx.getName());
         }
      } finally {
         this.activeClusterIndexLock.unlock();
      }

      return this.activeMultiClusterIndex;
   }

   public void validateTargetConnection(int multiClusterIndex) {
      CircuitBreaker circuitBreaker = this.getClusterCircuitBreaker(multiClusterIndex);
      State originalState = circuitBreaker.getState();

      try {
         circuitBreaker.transitionToClosedState();

         try (Connection targetConnection = this.getConnection(multiClusterIndex)) {
            targetConnection.ping();
         }
      } catch (Exception e) {
         if (State.FORCED_OPEN.equals(originalState)) {
            circuitBreaker.transitionToForcedOpenState();
         }

         throw new JedisValidationException(circuitBreaker.getName() + " failed to connect. Please check configuration and try again.", e);
      }
   }

   public void setActiveMultiClusterIndex(int multiClusterIndex) {
      this.activeClusterIndexLock.lock();

      try {
         if (this.activeMultiClusterIndex == multiClusterIndex && !State.FORCED_OPEN.equals(this.getClusterCircuitBreaker(multiClusterIndex).getState())) {
            return;
         }

         if (multiClusterIndex < 1 || multiClusterIndex > this.multiClusterMap.size()) {
            throw new JedisValidationException(
               "MultiClusterIndex: "
                  + multiClusterIndex
                  + " is not within the configured range. Please choose an index between 1 and "
                  + this.multiClusterMap.size()
            );
         }

         this.validateTargetConnection(multiClusterIndex);
         String originalClusterName = this.getClusterCircuitBreaker().getName();
         if (this.activeMultiClusterIndex == multiClusterIndex) {
            this.log.warn("Cluster/database endpoint '{}' successfully closed its circuit breaker", originalClusterName);
         } else {
            this.log
               .warn(
                  "Cluster/database endpoint successfully updated from '{}' to '{}'",
                  originalClusterName,
                  this.getClusterCircuitBreaker(multiClusterIndex).getName()
               );
         }

         this.activeMultiClusterIndex = multiClusterIndex;
         this.lastClusterCircuitBreakerForcedOpen = false;
      } finally {
         this.activeClusterIndexLock.unlock();
      }
   }

   @Override
   public void close() {
      this.multiClusterMap.get(this.activeMultiClusterIndex).getConnectionPool().close();
   }

   @Override
   public Connection getConnection() {
      return this.multiClusterMap.get(this.activeMultiClusterIndex).getConnection();
   }

   public Connection getConnection(int multiClusterIndex) {
      return this.multiClusterMap.get(multiClusterIndex).getConnection();
   }

   @Override
   public Connection getConnection(CommandArguments args) {
      return this.multiClusterMap.get(this.activeMultiClusterIndex).getConnection();
   }

   @Override
   public Map<?, Pool<Connection>> getConnectionMap() {
      ConnectionPool connectionPool = this.multiClusterMap.get(this.activeMultiClusterIndex).getConnectionPool();
      return Collections.singletonMap(connectionPool.getFactory(), connectionPool);
   }

   public MultiClusterPooledConnectionProvider.Cluster getCluster() {
      return this.multiClusterMap.get(this.activeMultiClusterIndex);
   }

   @VisibleForTesting
   public MultiClusterPooledConnectionProvider.Cluster getCluster(int multiClusterIndex) {
      return this.multiClusterMap.get(multiClusterIndex);
   }

   public CircuitBreaker getClusterCircuitBreaker() {
      return this.multiClusterMap.get(this.activeMultiClusterIndex).getCircuitBreaker();
   }

   public CircuitBreaker getClusterCircuitBreaker(int multiClusterIndex) {
      return this.multiClusterMap.get(multiClusterIndex).getCircuitBreaker();
   }

   public boolean isLastClusterCircuitBreakerForcedOpen() {
      return this.lastClusterCircuitBreakerForcedOpen;
   }

   public void runClusterFailoverPostProcessor(Integer multiClusterIndex) {
      if (this.clusterFailoverPostProcessor != null) {
         this.clusterFailoverPostProcessor.accept(this.getClusterCircuitBreaker(multiClusterIndex).getName());
      }
   }

   public void setClusterFailoverPostProcessor(Consumer<String> clusterFailoverPostProcessor) {
      this.clusterFailoverPostProcessor = clusterFailoverPostProcessor;
   }

   public List<Class<? extends Throwable>> getFallbackExceptionList() {
      return this.fallbackExceptionList;
   }

   public static class Cluster {
      private final ConnectionPool connectionPool;
      private final Retry retry;
      private final CircuitBreaker circuitBreaker;

      public Cluster(ConnectionPool connectionPool, Retry retry, CircuitBreaker circuitBreaker) {
         this.connectionPool = connectionPool;
         this.retry = retry;
         this.circuitBreaker = circuitBreaker;
      }

      public Connection getConnection() {
         return this.connectionPool.getResource();
      }

      public ConnectionPool getConnectionPool() {
         return this.connectionPool;
      }

      public Retry getRetry() {
         return this.retry;
      }

      public CircuitBreaker getCircuitBreaker() {
         return this.circuitBreaker;
      }
   }
}
