package me.neznamy.tab.libs.com.rabbitmq.client.impl;

class UnknownChannelException extends RuntimeException {
   private static final long serialVersionUID = 1L;
   private final int channelNumber;

   public UnknownChannelException(int channelNumber) {
      super("Unknown channel number " + channelNumber);
      this.channelNumber = channelNumber;
   }

   public int getChannelNumber() {
      return this.channelNumber;
   }
}
