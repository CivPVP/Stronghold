package me.neznamy.tab.libs.com.rabbitmq.client.observation.micrometer;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.common.util.StringUtils;

public class DefaultPublishObservationConvention implements PublishObservationConvention {
   private final String name;

   public DefaultPublishObservationConvention() {
      this("rabbitmq.publish");
   }

   public DefaultPublishObservationConvention(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public String getContextualName(PublishContext context) {
      return this.exchange(context.getRoutingKey()) + " publish";
   }

   private String exchange(String destination) {
      return StringUtils.isNotBlank(destination) ? destination : "amq.default";
   }

   public KeyValues getLowCardinalityKeyValues(PublishContext context) {
      return KeyValues.of(
         new KeyValue[]{
            RabbitMqObservationDocumentation.LowCardinalityTags.MESSAGING_OPERATION.withValue("publish"),
            RabbitMqObservationDocumentation.LowCardinalityTags.MESSAGING_SYSTEM.withValue("rabbitmq"),
            RabbitMqObservationDocumentation.LowCardinalityTags.NET_PROTOCOL_NAME.withValue("amqp"),
            RabbitMqObservationDocumentation.LowCardinalityTags.NET_PROTOCOL_VERSION.withValue("0.9.1")
         }
      );
   }

   public KeyValues getHighCardinalityKeyValues(PublishContext context) {
      return KeyValues.of(
         new KeyValue[]{
            RabbitMqObservationDocumentation.HighCardinalityTags.MESSAGING_ROUTING_KEY.withValue(context.getRoutingKey()),
            RabbitMqObservationDocumentation.HighCardinalityTags.MESSAGING_DESTINATION_NAME.withValue(this.exchange(context.getExchange())),
            RabbitMqObservationDocumentation.HighCardinalityTags.MESSAGING_MESSAGE_PAYLOAD_SIZE_BYTES.withValue(String.valueOf(context.getPayloadSizeBytes())),
            RabbitMqObservationDocumentation.HighCardinalityTags.NET_SOCK_PEER_ADDR.withValue(context.getConnectionInfo().getPeerAddress()),
            RabbitMqObservationDocumentation.HighCardinalityTags.NET_SOCK_PEER_PORT.withValue(String.valueOf(context.getConnectionInfo().getPeerPort()))
         }
      );
   }
}
