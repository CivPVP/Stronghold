package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

import java.io.IOException;

public class RecordedExchangeBinding extends RecordedBinding {
   public RecordedExchangeBinding(AutorecoveringChannel channel) {
      super(channel);
   }

   @Override
   public void recover() throws IOException {
      this.channel.getDelegate().exchangeBind(this.destination, this.source, this.routingKey, this.arguments);
   }

   @Override
   public String toString() {
      return "RecordedExchangeBinding[source="
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
