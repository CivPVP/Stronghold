package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

@FunctionalInterface
public interface RecoveredQueueNameSupplier {
   String getNameToUseForRecovery(RecordedQueue var1);
}
