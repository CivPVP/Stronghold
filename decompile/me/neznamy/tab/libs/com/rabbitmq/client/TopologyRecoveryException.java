package me.neznamy.tab.libs.com.rabbitmq.client;

import me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery.RecordedEntity;

public class TopologyRecoveryException extends Exception {
   private final RecordedEntity recordedEntity;

   public TopologyRecoveryException(String message, Throwable cause) {
      this(message, cause, null);
   }

   public TopologyRecoveryException(String message, Throwable cause, RecordedEntity recordedEntity) {
      super(message, cause);
      this.recordedEntity = recordedEntity;
   }

   public RecordedEntity getRecordedEntity() {
      return this.recordedEntity;
   }
}
