package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.IOException;
import java.net.ConnectException;
import me.neznamy.tab.libs.com.rabbitmq.client.AlreadyClosedException;
import me.neznamy.tab.libs.com.rabbitmq.client.Channel;
import me.neznamy.tab.libs.com.rabbitmq.client.Connection;
import me.neznamy.tab.libs.com.rabbitmq.client.Consumer;
import me.neznamy.tab.libs.com.rabbitmq.client.ExceptionHandler;
import me.neznamy.tab.libs.com.rabbitmq.client.TopologyRecoveryException;
import org.slf4j.LoggerFactory;

public class ForgivingExceptionHandler implements ExceptionHandler {
   @Override
   public void handleUnexpectedConnectionDriverException(Connection conn, Throwable exception) {
      this.log("An unexpected connection driver error occurred", exception);
   }

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
      this.handleChannelKiller(channel, exception, "Consumer " + consumer + " (" + consumerTag + ") method " + methodName + " for channel " + channel);
   }

   @Override
   public void handleConnectionRecoveryException(Connection conn, Throwable exception) {
      if (!(exception instanceof ConnectException)) {
         this.log("Caught an exception during connection recovery!", exception);
      }
   }

   @Override
   public void handleChannelRecoveryException(Channel ch, Throwable exception) {
      this.log("Caught an exception when recovering channel " + ch.getChannelNumber(), exception);
   }

   @Override
   public void handleTopologyRecoveryException(Connection conn, Channel ch, TopologyRecoveryException exception) {
      this.log("Caught an exception when recovering topology " + exception.getMessage(), exception);
   }

   protected void handleChannelKiller(Channel channel, Throwable exception, String what) {
      this.log(what + "threw an exception for channel " + channel, exception);
   }

   protected void handleConnectionKiller(Connection connection, Throwable exception, String what) {
      this.log(what + " threw an exception for connection " + connection, exception);

      try {
         connection.close(200, "Closed due to exception from " + what);
      } catch (AlreadyClosedException var5) {
      } catch (IOException ioe) {
         this.log("Failure during close of connection " + connection + " after " + exception, ioe);
         connection.abort(541, "Internal error closing connection for " + what);
      }
   }

   protected void log(String message, Throwable e) {
      if (isSocketClosedOrConnectionReset(e)) {
         LoggerFactory.getLogger(ForgivingExceptionHandler.class).warn(message + " (Exception message: " + e.getMessage() + ")");
      } else {
         LoggerFactory.getLogger(ForgivingExceptionHandler.class).error(message, e);
      }
   }

   private static boolean isSocketClosedOrConnectionReset(Throwable e) {
      return e instanceof IOException
         && ("Connection reset".equals(e.getMessage()) || "Socket closed".equals(e.getMessage()) || "Connection reset by peer".equals(e.getMessage()));
   }
}
