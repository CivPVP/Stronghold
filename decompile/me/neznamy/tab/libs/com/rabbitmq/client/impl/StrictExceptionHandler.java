package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import me.neznamy.tab.libs.com.rabbitmq.client.AlreadyClosedException;
import me.neznamy.tab.libs.com.rabbitmq.client.Channel;
import me.neznamy.tab.libs.com.rabbitmq.client.Connection;
import me.neznamy.tab.libs.com.rabbitmq.client.Consumer;
import me.neznamy.tab.libs.com.rabbitmq.client.ExceptionHandler;

public class StrictExceptionHandler extends ForgivingExceptionHandler implements ExceptionHandler {
   @Override
   public void handleReturnListenerException(Channel channel, Throwable exception) {
      this.handleChannelKiller(channel, exception, "ReturnListener.handleReturn");
   }

   @Override
   public void handleConfirmListenerException(Channel channel, Throwable exception) {
      this.handleChannelKiller(channel, exception, "ConfirmListener.handle{N,A}ck");
   }

   @Override
   public void handleBlockedListenerException(Connection connection, Throwable exception) {
      this.handleConnectionKiller(connection, exception, "BlockedListener");
   }

   @Override
   public void handleConsumerException(Channel channel, Throwable exception, Consumer consumer, String consumerTag, String methodName) {
      String logMessage = "Consumer " + consumer + " (" + consumerTag + ") method " + methodName + " for channel " + channel;
      String closeMessage = "Consumer (" + consumerTag + ") method " + methodName + " for channel " + channel;
      this.handleChannelKiller(channel, exception, logMessage, closeMessage);
   }

   @Override
   protected void handleChannelKiller(Channel channel, Throwable exception, String what) {
      this.handleChannelKiller(channel, exception, what, what);
   }

   protected void handleChannelKiller(Channel channel, Throwable exception, String logMessage, String closeMessage) {
      this.log(logMessage + " threw an exception for channel " + channel, exception);

      try {
         channel.close(200, "Closed due to exception from " + closeMessage);
      } catch (AlreadyClosedException var6) {
      } catch (TimeoutException var7) {
      } catch (IOException ioe) {
         this.log("Failure during close of channel " + channel + " after " + exception, ioe);
         channel.getConnection().abort(541, "Internal error closing channel for " + closeMessage);
      }
   }
}
