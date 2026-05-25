package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

import java.io.IOException;
import java.util.Map;

public class RecordedExchange extends RecordedNamedEntity {
   private boolean durable;
   private boolean autoDelete;
   private Map<String, Object> arguments;
   private String type;

   public RecordedExchange(AutorecoveringChannel channel, String name) {
      super(channel, name);
   }

   @Override
   public void recover() throws IOException {
      this.channel.getDelegate().exchangeDeclare(this.name, this.type, this.durable, this.autoDelete, this.arguments);
   }

   public RecordedExchange durable(boolean value) {
      this.durable = value;
      return this;
   }

   public RecordedExchange autoDelete(boolean value) {
      this.autoDelete = value;
      return this;
   }

   public RecordedExchange type(String value) {
      this.type = value;
      return this;
   }

   public RecordedExchange arguments(Map<String, Object> value) {
      this.arguments = value;
      return this;
   }

   public boolean isAutoDelete() {
      return this.autoDelete;
   }

   @Override
   public String toString() {
      return "RecordedExchange[name="
         + this.name
         + ", type="
         + this.type
         + ", durable="
         + this.durable
         + ", autoDelete="
         + this.autoDelete
         + ", arguments="
         + this.arguments
         + ", channel="
         + this.channel
         + "]";
   }
}
