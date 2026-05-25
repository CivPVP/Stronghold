package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

import java.io.IOException;

public class RecordedQueueBinding extends RecordedBinding {
   public RecordedQueueBinding(AutorecoveringChannel channel) {
      super(channel);
   }

   @Override
   public void recover() throws IOException {
      this.channel.getDelegate().queueBind(this.destination, this.source, this.routingKey, this.arguments);
   }

   @Override
   public String toString() {
      return "RecordedQueueBinding[source="
         + this.source
         + ", destination="
         + this.destination
         + ", routingKey="
         + this.routingKey
         + ", arguments="
         + this.arguments
         + ", channel="
         + this.channel
         + "]";
   }
}
