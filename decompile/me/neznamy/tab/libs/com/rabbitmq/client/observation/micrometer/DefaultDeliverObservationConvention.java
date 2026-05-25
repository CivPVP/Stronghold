package me.neznamy.tab.libs.com.rabbitmq.client.observation.micrometer;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.common.util.StringUtils;

abstract class DefaultDeliverObservationConvention implements DeliverObservationConvention {
   private final String operation;

   public DefaultDeliverObservationConvention(String operation) {
      this.operation = operation;
   }

   public String getContextualName(DeliverContext context) {
      return this.source(context.getQueue()) + " " + this.operation;
   }

   private String exchange(String destination) {
      return StringUtils.isNotBlank(destination) ? destination : "amq.default";
   }

   private String source(String destination) {
      return StringUtils.isNotBlank(destination) ? destination : "(anonymous)";
   }

   public KeyValues getLowCardinalityKeyValues(DeliverContext context) {
      return KeyValues.of(
         new KeyValue[]{
            RabbitMqObservationDocumentation.LowCardinalityTags.MESSAGING_OPERATION.withValue(this.operation),
            RabbitMqObservationDocumentation.LowCardinalityTags.MESSAGING_SYSTEM.withValue("rabbitmq"),
            RabbitMqObservationDocumentation.LowCardinalityTags.NET_PROTOCOL_NAME.withValue("amqp"),
            RabbitMqObservationDocumentation.LowCardinalityTags.NET_PROTOCOL_VERSION.withValue("0.9.1")
         }
      );
   }

   public KeyValues getHighCardinalityKeyValues(DeliverContext context) {
      return KeyValues.of(
         new KeyValue[]{
            RabbitMqObservationDocumentation.HighCardinalityTags.MESSAGING_ROUTING_KEY.withValue(context.getRoutingKey()),
            RabbitMqObservationDocumentation.HighCardinalityTags.MESSAGING_DESTINATION_NAME.withValue(this.exchange(context.getExchange())),
            RabbitMqObservationDocumentation.HighCardinalityTags.MESSAGING_SOURCE_NAME.withValue(context.getQueue()),
            RabbitMqObservationDocumentation.HighCardinalityTags.MESSAGING_MESSAGE_PAYLOAD_SIZE_BYTES.withValue(String.valueOf(context.getPayloadSizeBytes()))
         }
      );
   }
}
