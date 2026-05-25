package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

import java.io.IOException;

public abstract class RecordedNamedEntity extends RecordedEntity {
   protected String name;

   public RecordedNamedEntity(AutorecoveringChannel channel, String name) {
      super(channel);
      this.name = name;
   }

   public abstract void recover() throws IOException;

   public String getName() {
      return this.name;
   }
}
