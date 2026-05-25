package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

import java.util.concurrent.ThreadFactory;
import me.neznamy.tab.libs.com.rabbitmq.client.MetricsCollector;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.AMQConnection;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.ConnectionParams;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.FrameHandler;
import me.neznamy.tab.libs.com.rabbitmq.client.observation.ObservationCollector;

public class RecoveryAwareAMQConnection extends AMQConnection {
   public RecoveryAwareAMQConnection(
      ConnectionParams params, FrameHandler handler, MetricsCollector metricsCollector, ObservationCollector observationCollector
   ) {
      super(params, handler, metricsCollector, observationCollector);
   }

   public RecoveryAwareAMQConnection(ConnectionParams params, FrameHandler handler) {
      super(params, handler);
   }

   protected RecoveryAwareChannelManager instantiateChannelManager(int channelMax, ThreadFactory threadFactory) {
      RecoveryAwareChannelManager recoveryAwareChannelManager = new RecoveryAwareChannelManager(
         super._workService, channelMax, threadFactory, this.metricsCollector, this.observationCollector
      );
      this.configureChannelManager(recoveryAwareChannelManager);
      return recoveryAwareChannelManager;
   }
}
