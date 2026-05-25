package me.neznamy.tab.libs.com.rabbitmq.client.observation.micrometer;

import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.Observation.Context;

public interface PublishObservationConvention extends ObservationConvention<PublishContext> {
   default boolean supportsContext(Context context) {
      return context instanceof PublishContext;
   }
}
