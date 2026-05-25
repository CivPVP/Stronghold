package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import me.neznamy.tab.libs.com.rabbitmq.client.MetricsCollector;
import me.neznamy.tab.libs.com.rabbitmq.client.NoOpMetricsCollector;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.AMQConnection;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.ChannelManager;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.ChannelN;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.ConsumerWorkService;
import me.neznamy.tab.libs.com.rabbitmq.client.observation.ObservationCollector;

public class RecoveryAwareChannelManager extends ChannelManager {
   public RecoveryAwareChannelManager(ConsumerWorkService workService, int channelMax) {
      this(workService, channelMax, Executors.defaultThreadFactory());
   }

   public RecoveryAwareChannelManager(ConsumerWorkService workService, int channelMax, ThreadFactory threadFactory) {
      super(workService, channelMax, threadFactory, new NoOpMetricsCollector(), ObservationCollector.NO_OP);
   }

   public RecoveryAwareChannelManager(
      ConsumerWorkService workService,
      int channelMax,
      ThreadFactory threadFactory,
      MetricsCollector metricsCollector,
      ObservationCollector observationCollector
   ) {
      super(workService, channelMax, threadFactory, metricsCollector, observationCollector);
   }

   @Override
   protected ChannelN instantiateChannel(AMQConnection connection, int channelNumber, ConsumerWorkService workService) {
      return new RecoveryAwareChannelN(connection, channelNumber, workService, this.metricsCollector, this.observationCollector);
   }
}
