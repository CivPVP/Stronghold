package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

public class RetryResult {
   private final RecordedEntity recordedEntity;
   private final Object result;

   public RetryResult(RecordedEntity recordedEntity, Object result) {
      this.recordedEntity = recordedEntity;
      this.result = result;
   }

   public RecordedEntity getRecordedEntity() {
      return this.recordedEntity;
   }

   public Object getResult() {
      return this.result;
   }
}
