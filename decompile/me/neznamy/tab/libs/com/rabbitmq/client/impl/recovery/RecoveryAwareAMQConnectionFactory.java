package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import me.neznamy.tab.libs.com.rabbitmq.client.Address;
import me.neznamy.tab.libs.com.rabbitmq.client.AddressResolver;
import me.neznamy.tab.libs.com.rabbitmq.client.ListAddressResolver;
import me.neznamy.tab.libs.com.rabbitmq.client.MetricsCollector;
import me.neznamy.tab.libs.com.rabbitmq.client.NoOpMetricsCollector;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.ConnectionParams;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.FrameHandler;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.FrameHandlerFactory;
import me.neznamy.tab.libs.com.rabbitmq.client.observation.ObservationCollector;

public class RecoveryAwareAMQConnectionFactory {
   private final ConnectionParams params;
   private final FrameHandlerFactory factory;
   private final AddressResolver addressResolver;
   private final MetricsCollector metricsCollector;
   private final ObservationCollector observationCollector;

   public RecoveryAwareAMQConnectionFactory(ConnectionParams params, FrameHandlerFactory factory, List<Address> addrs) {
      this(params, factory, new ListAddressResolver(addrs), new NoOpMetricsCollector(), ObservationCollector.NO_OP);
   }

   public RecoveryAwareAMQConnectionFactory(ConnectionParams params, FrameHandlerFactory factory, AddressResolver addressResolver) {
      this(params, factory, addressResolver, new NoOpMetricsCollector(), ObservationCollector.NO_OP);
   }

   public RecoveryAwareAMQConnectionFactory(
      ConnectionParams params,
      FrameHandlerFactory factory,
      AddressResolver addressResolver,
      MetricsCollector metricsCollector,
      ObservationCollector observationCollector
   ) {
      this.params = params;
      this.factory = factory;
      this.addressResolver = addressResolver;
      this.metricsCollector = metricsCollector;
      this.observationCollector = observationCollector;
   }

   public RecoveryAwareAMQConnection newConnection() throws IOException, TimeoutException {
      Exception lastException = null;
      List<Address> resolved = this.addressResolver.getAddresses();

      for (Address addr : this.addressResolver.maybeShuffle(resolved)) {
         try {
            FrameHandler frameHandler = this.factory.create(addr, this.connectionName());
            RecoveryAwareAMQConnection conn = this.createConnection(this.params, frameHandler, this.metricsCollector);
            conn.start();
            this.metricsCollector.newConnection(conn);
            return conn;
         } catch (IOException e) {
            lastException = e;
         } catch (TimeoutException te) {
            lastException = te;
         }
      }

      if (lastException != null) {
         if (lastException instanceof IOException) {
            throw (IOException)lastException;
         }

         if (lastException instanceof TimeoutException) {
            throw (TimeoutException)lastException;
         }
      }

      throw new IOException("failed to connect");
   }

   protected RecoveryAwareAMQConnection createConnection(ConnectionParams params, FrameHandler handler, MetricsCollector metricsCollector) {
      return new RecoveryAwareAMQConnection(params, handler, metricsCollector, this.observationCollector);
   }

   private String connectionName() {
      Map<String, Object> clientProperties = this.params.getClientProperties();
      if (clientProperties == null) {
         return null;
      }

      Object connectionName = clientProperties.get("connection_name");
      return connectionName == null ? null : connectionName.toString();
   }
}
