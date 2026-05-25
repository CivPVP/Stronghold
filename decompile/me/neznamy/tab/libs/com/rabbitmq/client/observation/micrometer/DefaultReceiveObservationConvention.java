package me.neznamy.tab.libs.com.rabbitmq.client.observation.micrometer;

public class DefaultReceiveObservationConvention extends DefaultDeliverObservationConvention {
   public DefaultReceiveObservationConvention(String operation) {
      super(operation);
   }

   public String getName() {
      return "rabbitmq.receive";
   }
}
