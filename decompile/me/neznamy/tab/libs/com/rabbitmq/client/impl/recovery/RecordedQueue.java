package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

import java.io.IOException;
import java.util.Map;

public class RecordedQueue extends RecordedNamedEntity {
   public static final String EMPTY_STRING = "";
   static final RecoveredQueueNameSupplier DEFAULT_QUEUE_NAME_SUPPLIER = q -> q.isServerNamed() ? "" : q.name;
   private RecoveredQueueNameSupplier recoveredQueueNameSupplier = DEFAULT_QUEUE_NAME_SUPPLIER;
   private boolean durable;
   private boolean autoDelete;
   private Map<String, Object> arguments;
   private boolean exclusive;
   private boolean serverNamed;

   public RecordedQueue(AutorecoveringChannel channel, String name) {
      super(channel, name);
   }

   public RecordedQueue exclusive(boolean value) {
      this.exclusive = value;
      return this;
   }

   public boolean isExclusive() {
      return this.exclusive;
   }

   public RecordedQueue serverNamed(boolean value) {
      this.serverNamed = value;
      return this;
   }

   public boolean isServerNamed() {
      return this.serverNamed;
   }

   @Override
   public void recover() throws IOException {
      this.name = this.channel
         .getDelegate()
         .queueDeclare(this.getNameToUseForRecovery(), this.durable, this.exclusive, this.autoDelete, this.arguments)
         .getQueue();
   }

   public String getNameToUseForRecovery() {
      return this.recoveredQueueNameSupplier.getNameToUseForRecovery(this);
   }

   public RecordedQueue durable(boolean value) {
      this.durable = value;
      return this;
   }

   public boolean isDurable() {
      return this.durable;
   }

   public RecordedQueue autoDelete(boolean value) {
      this.autoDelete = value;
      return this;
   }

   public boolean isAutoDelete() {
      return this.autoDelete;
   }

   public RecordedQueue arguments(Map<String, Object> value) {
      this.arguments = value;
      return this;
   }

   public Map<String, Object> getArguments() {
      return this.arguments;
   }

   public RecordedQueue recoveredQueueNameSupplier(RecoveredQueueNameSupplier recoveredQueueNameSupplier) {
      this.recoveredQueueNameSupplier = recoveredQueueNameSupplier;
      return this;
   }

   @Override
   public String toString() {
      return "RecordedQueue[name="
         + this.name
         + ", durable="
         + this.durable
         + ", autoDelete="
         + this.autoDelete
         + ", exclusive="
         + this.exclusive
         + ", arguments="
         + this.arguments
         + "serverNamed="
         + this.serverNamed
         + ", channel="
         + this.channel
         + "]";
   }
}
