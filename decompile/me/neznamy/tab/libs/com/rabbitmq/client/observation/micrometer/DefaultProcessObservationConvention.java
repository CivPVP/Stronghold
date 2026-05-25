package me.neznamy.tab.libs.com.rabbitmq.client.observation.micrometer;

public class DefaultProcessObservationConvention extends DefaultDeliverObservationConvention {
   public DefaultProcessObservationConvention(String operation) {
      super(operation);
   }

   public String getName() {
      return "rabbitmq.process";
   }
}
