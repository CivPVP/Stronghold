package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

public class RetryContext {
   private final RecordedEntity entity;
   private final Exception exception;
   private final AutorecoveringConnection connection;

   public RetryContext(RecordedEntity entity, Exception exception, AutorecoveringConnection connection) {
      this.entity = entity;
      this.exception = exception;
      this.connection = connection;
   }

   public AutorecoveringConnection connection() {
      return this.connection;
   }

   public Exception exception() {
      return this.exception;
   }

   public RecordedEntity entity() {
      return this.entity;
   }

   public RecordedQueue queue() {
      return (RecordedQueue)this.entity;
   }

   public RecordedExchange exchange() {
      return (RecordedExchange)this.entity;
   }

   public RecordedBinding binding() {
      return (RecordedBinding)this.entity;
   }

   public RecordedConsumer consumer() {
      return (RecordedConsumer)this.entity;
   }
}
