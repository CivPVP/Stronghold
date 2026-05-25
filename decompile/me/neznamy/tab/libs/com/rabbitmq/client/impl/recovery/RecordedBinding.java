package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

import java.io.IOException;
import java.util.Map;

public abstract class RecordedBinding extends RecordedEntity {
   protected String source;
   protected String destination;
   protected String routingKey;
   protected Map<String, Object> arguments;

   public RecordedBinding(AutorecoveringChannel channel) {
      super(channel);
   }

   public RecordedBinding source(String value) {
      this.source = value;
      return this;
   }

   public RecordedBinding destination(String value) {
      this.destination = value;
      return this;
   }

   public RecordedBinding routingKey(String value) {
      this.routingKey = value;
      return this;
   }

   public RecordedBinding arguments(Map<String, Object> value) {
      this.arguments = value;
      return this;
   }

   public String getSource() {
      return this.source;
   }

   public String getDestination() {
      return this.destination;
   }

   public String getRoutingKey() {
      return this.routingKey;
   }

   public Map<String, Object> getArguments() {
      return this.arguments;
   }

   public void setDestination(String destination) {
      this.destination = destination;
   }

   public abstract void recover() throws IOException;

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if (o != null && this.getClass() == o.getClass()) {
         RecordedBinding that = (RecordedBinding)o;
         if (this.arguments != null ? this.arguments.equals(that.arguments) : that.arguments == null) {
            if (!this.destination.equals(that.destination)) {
               return false;
            } else {
               return !this.routingKey.equals(that.routingKey) ? false : this.source.equals(that.source);
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.source.hashCode();
      result = 31 * result + this.destination.hashCode();
      result = 31 * result + this.routingKey.hashCode();
      return 31 * result + (this.arguments != null ? this.arguments.hashCode() : 0);
   }
}
