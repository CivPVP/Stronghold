package me.neznamy.tab.libs.redis.clients.jedis.mcf;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateSupplier;
import me.neznamy.tab.libs.redis.clients.jedis.CommandObject;
import me.neznamy.tab.libs.redis.clients.jedis.Connection;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Experimental;
import me.neznamy.tab.libs.redis.clients.jedis.executors.CommandExecutor;
import me.neznamy.tab.libs.redis.clients.jedis.providers.MultiClusterPooledConnectionProvider;

@Experimental
public class CircuitBreakerCommandExecutor extends CircuitBreakerFailoverBase implements CommandExecutor {
   public CircuitBreakerCommandExecutor(MultiClusterPooledConnectionProvider provider) {
      super(provider);
   }

   @Override
   public <T> T executeCommand(CommandObject<T> commandObject) {
      MultiClusterPooledConnectionProvider.Cluster cluster = this.provider.getCluster();
      DecorateSupplier<T> supplier = Decorators.ofSupplier(() -> this.handleExecuteCommand(commandObject, cluster));
      supplier.withRetry(cluster.getRetry());
      supplier.withCircuitBreaker(cluster.getCircuitBreaker());
      supplier.withFallback(this.provider.getFallbackExceptionList(), e -> this.handleClusterFailover(commandObject, cluster.getCircuitBreaker()));
      return (T)supplier.decorate().get();
   }

   private <T> T handleExecuteCommand(CommandObject<T> commandObject, MultiClusterPooledConnectionProvider.Cluster cluster) {
      try (Connection connection = cluster.getConnection()) {
         return connection.executeCommand(commandObject);
      }
   }

   private <T> T handleClusterFailover(CommandObject<T> commandObject, CircuitBreaker circuitBreaker) {
      this.clusterFailover(circuitBreaker);
      return this.executeCommand(commandObject);
   }
}
