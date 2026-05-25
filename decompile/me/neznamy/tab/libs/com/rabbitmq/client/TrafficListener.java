package me.neznamy.tab.libs.com.rabbitmq.client;

public interface TrafficListener {
   TrafficListener NO_OP = new TrafficListener() {
      @Override
      public void write(Command outboundCommand) {
      }

      @Override
      public void read(Command inboundCommand) {
      }
   };

   void write(Command var1);

   void read(Command var1);
}
