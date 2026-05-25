package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

import java.io.IOException;
import me.neznamy.tab.libs.com.rabbitmq.client.Command;
import me.neznamy.tab.libs.com.rabbitmq.client.MetricsCollector;
import me.neznamy.tab.libs.com.rabbitmq.client.NoOpMetricsCollector;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.AMQConnection;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.AMQImpl;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.ChannelN;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.ConsumerWorkService;
import me.neznamy.tab.libs.com.rabbitmq.client.observation.ObservationCollector;

public class RecoveryAwareChannelN extends ChannelN {
   private volatile long maxSeenDeliveryTag = 0L;
   private volatile long activeDeliveryTagOffset = 0L;

   public RecoveryAwareChannelN(AMQConnection connection, int channelNumber, ConsumerWorkService workService) {
      this(connection, channelNumber, workService, new NoOpMetricsCollector(), ObservationCollector.NO_OP);
   }

   public RecoveryAwareChannelN(
      AMQConnection connection,
      int channelNumber,
      ConsumerWorkService workService,
      MetricsCollector metricsCollector,
      ObservationCollector observationCollector
   ) {
      super(connection, channelNumber, workService, metricsCollector, observationCollector);
   }

   @Override
   protected void processDelivery(Command command, AMQImpl.Basic.Deliver method) {
      long tag = method.getDeliveryTag();
      if (tag > this.maxSeenDeliveryTag) {
         this.maxSeenDeliveryTag = tag;
      }

      super.processDelivery(command, this.offsetDeliveryTag(method));
   }

   private AMQImpl.Basic.Deliver offsetDeliveryTag(AMQImpl.Basic.Deliver method) {
      return new AMQImpl.Basic.Deliver(
         method.getConsumerTag(), method.getDeliveryTag() + this.activeDeliveryTagOffset, method.getRedelivered(), method.getExchange(), method.getRoutingKey()
      );
   }

   @Override
   public void basicAck(long deliveryTag, boolean multiple) throws IOException {
      long realTag = deliveryTag - this.activeDeliveryTagOffset;
      if (multiple && deliveryTag == 0L) {
         realTag = 0L;
      } else if (realTag <= 0L) {
         return;
      }

      this.transmit(new AMQImpl.Basic.Ack(realTag, multiple));
      this.metricsCollector.basicAck(this, deliveryTag, multiple);
   }

   @Override
   public void basicNack(long deliveryTag, boolean multiple, boolean requeue) throws IOException {
      long realTag = deliveryTag - this.activeDeliveryTagOffset;
      if (multiple && deliveryTag == 0L) {
         realTag = 0L;
      } else if (realTag <= 0L) {
         return;
      }

      this.transmit(new AMQImpl.Basic.Nack(realTag, multiple, requeue));
      this.metricsCollector.basicNack(this, deliveryTag, requeue);
   }

   @Override
   public void basicReject(long deliveryTag, boolean requeue) throws IOException {
      long realTag = deliveryTag - this.activeDeliveryTagOffset;
      if (realTag > 0L) {
         this.transmit(new AMQImpl.Basic.Reject(realTag, requeue));
         this.metricsCollector.basicReject(this, deliveryTag, requeue);
      }
   }

   void inheritOffsetFrom(RecoveryAwareChannelN other) {
      this.activeDeliveryTagOffset = other.getActiveDeliveryTagOffset() + other.getMaxSeenDeliveryTag();
      this.maxSeenDeliveryTag = 0L;
   }

   public long getMaxSeenDeliveryTag() {
      return this.maxSeenDeliveryTag;
   }

   public long getActiveDeliveryTagOffset() {
      return this.activeDeliveryTagOffset;
   }
}
