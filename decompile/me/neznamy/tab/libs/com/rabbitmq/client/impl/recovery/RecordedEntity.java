package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

import me.neznamy.tab.libs.com.rabbitmq.client.Channel;

public abstract class RecordedEntity {
   protected final AutorecoveringChannel channel;

   public RecordedEntity(AutorecoveringChannel channel) {
      this.channel = channel;
   }

   public AutorecoveringChannel getChannel() {
      return this.channel;
   }

   public Channel getDelegateChannel() {
      return this.channel.getDelegate();
   }
}
