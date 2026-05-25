package me.neznamy.tab.libs.com.rabbitmq.client.observation.micrometer;

import io.micrometer.observation.transport.ReceiverContext;
import java.util.Map;

public class DeliverContext extends ReceiverContext<Map<String, Object>> {
   private final String exchange;
   private final String routingKey;
   private final int payloadSizeBytes;
   private final String queue;

   DeliverContext(String exchange, String routingKey, String queue, Map<String, Object> headers, int payloadSizeBytes) {
      super((hdrs, key) -> {
         Object result = hdrs.get(key);
         return result == null ? null : String.valueOf(result);
      });
      this.exchange = exchange;
      this.routingKey = routingKey;
      this.payloadSizeBytes = payloadSizeBytes;
      this.queue = queue;
      this.setCarrier(headers);
   }

   public String getExchange() {
      return this.exchange;
   }

   public String getRoutingKey() {
      return this.routingKey;
   }

   public int getPayloadSizeBytes() {
      return this.payloadSizeBytes;
   }

   public String getQueue() {
      return this.queue;
   }
}
