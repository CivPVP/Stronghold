package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

public class SocketChannelRegistration {
   final SocketChannelFrameHandlerState state;
   final int operations;

   public SocketChannelRegistration(SocketChannelFrameHandlerState state, int operations) {
      this.state = state;
      this.operations = operations;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SocketChannelRegistration that = (SocketChannelRegistration)o;
         return this.state.getChannel().equals(that.state.getChannel());
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.state.getChannel().hashCode();
   }
}
