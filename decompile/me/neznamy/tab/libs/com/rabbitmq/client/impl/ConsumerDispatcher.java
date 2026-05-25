package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import me.neznamy.tab.libs.com.rabbitmq.client.AMQP;
import me.neznamy.tab.libs.com.rabbitmq.client.Channel;
import me.neznamy.tab.libs.com.rabbitmq.client.Consumer;
import me.neznamy.tab.libs.com.rabbitmq.client.Envelope;
import me.neznamy.tab.libs.com.rabbitmq.client.ShutdownSignalException;
import me.neznamy.tab.libs.com.rabbitmq.utility.Utility;

final class ConsumerDispatcher {
   private final ConsumerWorkService workService;
   private final AMQConnection connection;
   private final Channel channel;
   private volatile boolean shuttingDown = false;
   private volatile boolean shutdownConsumersDriven = false;
   private volatile CountDownLatch shutdownConsumersComplete;
   private volatile ShutdownSignalException shutdownSignal = null;

   public ConsumerDispatcher(AMQConnection connection, Channel channel, ConsumerWorkService workService) {
      this.connection = connection;
      this.channel = channel;
      workService.registerKey(channel);
      this.workService = workService;
   }

   public void quiesce() {
      this.shuttingDown = true;
   }

   public void setUnlimited(boolean unlimited) {
      this.workService.setUnlimited(this.channel, unlimited);
   }

   public void handleConsumeOk(final Consumer delegate, final String consumerTag) {
      this.executeUnlessShuttingDown(
         new Runnable() {
            @Override
            public void run() {
               try {
                  delegate.handleConsumeOk(consumerTag);
               } catch (Throwable ex) {
                  ConsumerDispatcher.this.connection
                     .getExceptionHandler()
                     .handleConsumerException(ConsumerDispatcher.this.channel, ex, delegate, consumerTag, "handleConsumeOk");
               }
            }
         }
      );
   }

   public void handleCancelOk(final Consumer delegate, final String consumerTag) {
      this.executeUnlessShuttingDown(
         new Runnable() {
            @Override
            public void run() {
               try {
                  delegate.handleCancelOk(consumerTag);
               } catch (Throwable ex) {
                  ConsumerDispatcher.this.connection
                     .getExceptionHandler()
                     .handleConsumerException(ConsumerDispatcher.this.channel, ex, delegate, consumerTag, "handleCancelOk");
               }
            }
         }
      );
   }

   public void handleCancel(final Consumer delegate, final String consumerTag) {
      this.executeUnlessShuttingDown(
         new Runnable() {
            @Override
            public void run() {
               try {
                  delegate.handleCancel(consumerTag);
               } catch (Throwable ex) {
                  ConsumerDispatcher.this.connection
                     .getExceptionHandler()
                     .handleConsumerException(ConsumerDispatcher.this.channel, ex, delegate, consumerTag, "handleCancel");
               }
            }
         }
      );
   }

   public void handleRecoverOk(final Consumer delegate, final String consumerTag) {
      this.executeUnlessShuttingDown(new Runnable() {
         @Override
         public void run() {
            delegate.handleRecoverOk(consumerTag);
         }
      });
   }

   public void handleDelivery(
      final Consumer delegate, final String consumerTag, final Envelope envelope, final AMQP.BasicProperties properties, final byte[] body
   ) throws IOException {
      this.executeUnlessShuttingDown(
         new Runnable() {
            @Override
            public void run() {
               try {
                  delegate.handleDelivery(consumerTag, envelope, properties, body);
               } catch (Throwable ex) {
                  ConsumerDispatcher.this.connection
                     .getExceptionHandler()
                     .handleConsumerException(ConsumerDispatcher.this.channel, ex, delegate, consumerTag, "handleDelivery");
               }
            }
         }
      );
   }

   public CountDownLatch handleShutdownSignal(final Map<String, Consumer> consumers, final ShutdownSignalException signal) {
      if (!this.shutdownConsumersDriven) {
         final CountDownLatch latch = new CountDownLatch(1);
         this.shutdownConsumersComplete = latch;
         this.shutdownConsumersDriven = true;
         this.execute(new Runnable() {
            @Override
            public void run() {
               ConsumerDispatcher.this.notifyConsumersOfShutdown(consumers, signal);
               ConsumerDispatcher.this.shutdown(signal);
               ConsumerDispatcher.this.workService.stopWork(ConsumerDispatcher.this.channel);
               latch.countDown();
            }
         });
      }

      return this.shutdownConsumersComplete;
   }

   private void notifyConsumersOfShutdown(Map<String, Consumer> consumers, ShutdownSignalException signal) {
      for (Entry<String, Consumer> consumerEntry : consumers.entrySet()) {
         this.notifyConsumerOfShutdown(consumerEntry.getKey(), consumerEntry.getValue(), signal);
      }
   }

   private void notifyConsumerOfShutdown(String consumerTag, Consumer consumer, ShutdownSignalException signal) {
      try {
         consumer.handleShutdownSignal(consumerTag, signal);
      } catch (Throwable ex) {
         this.connection.getExceptionHandler().handleConsumerException(this.channel, ex, consumer, consumerTag, "handleShutdownSignal");
      }
   }

   private void executeUnlessShuttingDown(Runnable r) {
      if (!this.shuttingDown) {
         this.execute(r);
      }
   }

   private void execute(Runnable r) {
      this.checkShutdown();
      this.workService.addWork(this.channel, r);
   }

   private void shutdown(ShutdownSignalException signal) {
      this.shutdownSignal = signal;
   }

   private void checkShutdown() {
      if (this.shutdownSignal != null) {
         throw (ShutdownSignalException)Utility.fixStackTrace(this.shutdownSignal);
      }
   }
}
