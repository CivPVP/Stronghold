package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ChannelContinuationTimeoutException extends IOException {
   private final Object channel;
   private final int channelNumber;
   private final Method method;

   public ChannelContinuationTimeoutException(TimeoutException cause, Object channel, int channelNumber, Method method) {
      super("Continuation call for method " + method + " on channel " + channel + " (#" + channelNumber + ") timed out", cause);
      this.channel = channel;
      this.channelNumber = channelNumber;
      this.method = method;
   }

   public Method getMethod() {
      return this.method;
   }

   public Object getChannel() {
      return this.channel;
   }

   public int getChannelNumber() {
      return this.channelNumber;
   }
}
