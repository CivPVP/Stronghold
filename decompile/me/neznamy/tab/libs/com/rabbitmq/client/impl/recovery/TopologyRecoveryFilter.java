package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

public interface TopologyRecoveryFilter {
   default boolean filterExchange(RecordedExchange recordedExchange) {
      return true;
   }

   default boolean filterQueue(RecordedQueue recordedQueue) {
      return true;
   }

   default boolean filterBinding(RecordedBinding recordedBinding) {
      return true;
   }

   default boolean filterConsumer(RecordedConsumer recordedConsumer) {
      return true;
   }
}
