package me.neznamy.tab.libs.com.rabbitmq.client.observation.micrometer;

import io.micrometer.observation.transport.SenderContext;
import java.util.Map;
import me.neznamy.tab.libs.com.rabbitmq.client.observation.ObservationCollector;

public class PublishContext extends SenderContext<Map<String, Object>> {
   private final String exchange;
   private final String routingKey;
   private final int payloadSizeBytes;
   private final ObservationCollector.ConnectionInfo connectionInfo;

   PublishContext(String exchange, String routingKey, Map<String, Object> headers, int payloadSizeBytes, ObservationCollector.ConnectionInfo connectionInfo) {
      super((hdrs, key, value) -> hdrs.put(key, value));
      this.exchange = exchange;
      this.routingKey = routingKey;
      this.payloadSizeBytes = payloadSizeBytes;
      this.connectionInfo = connectionInfo;
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

   public ObservationCollector.ConnectionInfo getConnectionInfo() {
      return this.connectionInfo;
   }
}
