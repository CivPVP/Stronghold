package me.neznamy.tab.libs.com.rabbitmq.client.observation.micrometer;

import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.Observation.Context;

public interface DeliverObservationConvention extends ObservationConvention<DeliverContext> {
   default boolean supportsContext(Context context) {
      return context instanceof DeliverContext;
   }
}
