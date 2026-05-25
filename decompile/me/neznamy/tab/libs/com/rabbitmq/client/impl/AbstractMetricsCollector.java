package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import me.neznamy.tab.libs.com.rabbitmq.client.Channel;
import me.neznamy.tab.libs.com.rabbitmq.client.Connection;
import me.neznamy.tab.libs.com.rabbitmq.client.MetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMetricsCollector implements MetricsCollector {
   private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMetricsCollector.class);
   private final ConcurrentMap<String, AbstractMetricsCollector.ConnectionState> connectionState = new ConcurrentHashMap<>();
   private final Runnable markAcknowledgedMessageAction = () -> this.markAcknowledgedMessage();
   private final Function<Boolean, Runnable> markRejectedMessageAction;

   public AbstractMetricsCollector() {
      Runnable rejectRequeue = () -> this.markRejectedMessage(true);
      Runnable rejectNoRequeue = () -> this.markRejectedMessage(false);
      this.markRejectedMessageAction = requeue -> requeue ? rejectRequeue : rejectNoRequeue;
   }

   @Override
   public void newConnection(Connection connection) {
      try {
         if (connection.getId() == null) {
            connection.setId(UUID.randomUUID().toString());
         }

         this.incrementConnectionCount(connection);
         this.connectionState.put(connection.getId(), new AbstractMetricsCollector.ConnectionState(connection));
         connection.addShutdownListener(cause -> this.closeConnection(connection));
      } catch (Exception e) {
         LOGGER.info("Error while computing metrics in newConnection: " + e.getMessage());
      }
   }

   @Override
   public void closeConnection(Connection connection) {
      try {
         AbstractMetricsCollector.ConnectionState removed = this.connectionState.remove(connection.getId());
         if (removed != null) {
            this.decrementConnectionCount(connection);
         }
      } catch (Exception e) {
         LOGGER.info("Error while computing metrics in closeConnection: " + e.getMessage());
      }
   }

   @Override
   public void newChannel(Channel channel) {
      if (channel != null) {
         try {
            this.incrementChannelCount(channel);
            channel.addShutdownListener(cause -> this.closeChannel(channel));
            this.connectionState(channel.getConnection()).channelState.put(channel.getChannelNumber(), new AbstractMetricsCollector.ChannelState(channel));
         } catch (Exception e) {
            LOGGER.info("Error while computing metrics in newChannel: " + e.getMessage());
         }
      }
   }

   @Override
   public void closeChannel(Channel channel) {
      try {
         AbstractMetricsCollector.ChannelState removed = this.connectionState(channel.getConnection()).channelState.remove(channel.getChannelNumber());
         if (removed != null) {
            this.decrementChannelCount(channel);
         }
      } catch (Exception e) {
         LOGGER.info("Error while computing metrics in closeChannel: " + e.getMessage());
      }
   }

   @Override
   public void basicPublish(Channel channel) {
      try {
         this.markPublishedMessage();
      } catch (Exception e) {
         LOGGER.info("Error while computing metrics in basicPublish: " + e.getMessage());
      }
   }

   @Override
   public void basicPublishFailure(Channel channel, Throwable cause) {
      try {
         this.markMessagePublishFailed();
      } catch (Exception e) {
         LOGGER.info("Error while computing metrics in basicPublishFailure: " + e.getMessage());
      }
   }

   @Override
   public void basicPublishAck(Channel channel, long deliveryTag, boolean multiple) {
      if (!multiple) {
         try {
            this.markMessagePublishAcknowledged();
         } catch (Exception e) {
            LOGGER.info("Error while computing metrics in basicPublishAck: " + e.getMessage());
         }
      }
   }

   @Override
   public void basicPublishNack(Channel channel, long deliveryTag, boolean multiple) {
      if (!multiple) {
         try {
            this.markMessagePublishNotAcknowledged();
         } catch (Exception e) {
            LOGGER.info("Error while computing metrics in basicPublishNack: " + e.getMessage());
         }
      }
   }

   @Override
   public void basicPublishUnrouted(Channel channel) {
      try {
         this.markPublishedMessageUnrouted();
      } catch (Exception e) {
         LOGGER.info("Error while computing metrics in markPublishedMessageUnrouted: " + e.getMessage());
      }
   }

   @Override
   public void basicConsume(Channel channel, String consumerTag, boolean autoAck) {
      try {
         if (!autoAck) {
            AbstractMetricsCollector.ChannelState channelState = this.channelState(channel);
            channelState.lock.lock();

            try {
               this.channelState(channel).consumersWithManualAck.add(consumerTag);
            } finally {
               channelState.lock.unlock();
            }
         }
      } catch (Exception e) {
         LOGGER.info("Error while computing metrics in basicConsume: " + e.getMessage());
      }
   }

   @Override
   public void basicCancel(Channel channel, String consumerTag) {
      try {
         AbstractMetricsCollector.ChannelState channelState = this.channelState(channel);
         channelState.lock.lock();

         try {
            this.channelState(channel).consumersWithManualAck.remove(consumerTag);
         } finally {
            channelState.lock.unlock();
         }
      } catch (Exception e) {
         LOGGER.info("Error while computing metrics in basicCancel: " + e.getMessage());
      }
   }

   @Override
   public void consumedMessage(Channel channel, long deliveryTag, boolean autoAck) {
      try {
         this.markConsumedMessage();
         if (!autoAck) {
            AbstractMetricsCollector.ChannelState channelState = this.channelState(channel);
            channelState.lock.lock();

            try {
               this.channelState(channel).unackedMessageDeliveryTags.add(deliveryTag);
            } finally {
               channelState.lock.unlock();
            }
         }
      } catch (Exception e) {
         LOGGER.info("Error while computing metrics in consumedMessage: " + e.getMessage());
      }
   }

   @Override
   public void consumedMessage(Channel channel, long deliveryTag, String consumerTag) {
      try {
         this.markConsumedMessage();
         AbstractMetricsCollector.ChannelState channelState = this.channelState(channel);
         channelState.lock.lock();

         try {
            if (channelState.consumersWithManualAck.contains(consumerTag)) {
               channelState.unackedMessageDeliveryTags.add(deliveryTag);
            }
         } finally {
            channelState.lock.unlock();
         }
      } catch (Exception e) {
         LOGGER.info("Error while computing metrics in consumedMessage: " + e.getMessage());
      }
   }

   @Override
   public void basicAck(Channel channel, long deliveryTag, boolean multiple) {
      try {
         this.updateChannelStateAfterAckReject(channel, deliveryTag, multiple, this.markAcknowledgedMessageAction);
      } catch (Exception e) {
         LOGGER.info("Error while computing metrics in basicAck: " + e.getMessage());
      }
   }

   @Override
   public void basicNack(Channel channel, long deliveryTag) {
   }

   @Override
   public void basicNack(Channel channel, long deliveryTag, boolean requeue) {
      try {
         this.updateChannelStateAfterAckReject(channel, deliveryTag, true, this.markRejectedMessageAction.apply(requeue));
      } catch (Exception e) {
         LOGGER.info("Error while computing metrics in basicNack: " + e.getMessage());
      }
   }

   @Override
   public void basicReject(Channel channel, long deliveryTag) {
   }

   @Override
   public void basicReject(Channel channel, long deliveryTag, boolean requeue) {
      try {
         this.updateChannelStateAfterAckReject(channel, deliveryTag, false, this.markRejectedMessageAction.apply(requeue));
      } catch (Exception e) {
         LOGGER.info("Error while computing metrics in basicReject: " + e.getMessage());
      }
   }

   private void updateChannelStateAfterAckReject(Channel channel, long deliveryTag, boolean multiple, Runnable action) {
      AbstractMetricsCollector.ChannelState channelState = this.channelState(channel);
      channelState.lock.lock();

      try {
         if (multiple) {
            Iterator<Long> iterator = channelState.unackedMessageDeliveryTags.iterator();

            while (iterator.hasNext()) {
               long messageDeliveryTag = iterator.next();
               if (messageDeliveryTag <= deliveryTag) {
                  iterator.remove();
                  action.run();
               }
            }
         } else if (channelState.unackedMessageDeliveryTags.remove(deliveryTag)) {
            action.run();
         }
      } finally {
         channelState.lock.unlock();
      }
   }

   private AbstractMetricsCollector.ConnectionState connectionState(Connection connection) {
      return this.connectionState.get(connection.getId());
   }

   private AbstractMetricsCollector.ChannelState channelState(Channel channel) {
      return this.connectionState(channel.getConnection()).channelState.get(channel.getChannelNumber());
   }

   public void cleanStaleState() {
      try {
         Iterator<Entry<String, AbstractMetricsCollector.ConnectionState>> connectionStateIterator = this.connectionState.entrySet().iterator();

         while (connectionStateIterator.hasNext()) {
            Entry<String, AbstractMetricsCollector.ConnectionState> connectionEntry = connectionStateIterator.next();
            Connection connection = connectionEntry.getValue().connection;
            if (connection.isOpen()) {
               Iterator<Entry<Integer, AbstractMetricsCollector.ChannelState>> channelStateIterator = connectionEntry.getValue()
                  .channelState
                  .entrySet()
                  .iterator();

               while (channelStateIterator.hasNext()) {
                  Entry<Integer, AbstractMetricsCollector.ChannelState> channelStateEntry = channelStateIterator.next();
                  Channel channel = channelStateEntry.getValue().channel;
                  if (!channel.isOpen()) {
                     channelStateIterator.remove();
                     this.decrementChannelCount(channel);
                     LOGGER.info(
                        "Ripped off state of channel {} of connection {}. This is abnormal, please report.", channel.getChannelNumber(), connection.getId()
                     );
                  }
               }
            } else {
               connectionStateIterator.remove();
               this.decrementConnectionCount(connection);

               for (int i = 0; i < connectionEntry.getValue().channelState.size(); i++) {
                  this.decrementChannelCount(null);
               }

               LOGGER.info("Ripped off state of connection {}. This is abnormal, please report.", connection.getId());
            }
         }
      } catch (Exception e) {
         LOGGER.info("Error during periodic clean of metricsCollector: " + e.getMessage());
      }
   }

   protected abstract void incrementConnectionCount(Connection var1);

   protected abstract void decrementConnectionCount(Connection var1);

   protected abstract void incrementChannelCount(Channel var1);

   protected abstract void decrementChannelCount(Channel var1);

   protected abstract void markPublishedMessage();

   protected abstract void markMessagePublishFailed();

   protected abstract void markConsumedMessage();

   protected abstract void markAcknowledgedMessage();

   /** @deprecated */
   protected abstract void markRejectedMessage();

   protected void markRejectedMessage(boolean requeue) {
      this.markRejectedMessage();
   }

   protected abstract void markMessagePublishAcknowledged();

   protected abstract void markMessagePublishNotAcknowledged();

   protected abstract void markPublishedMessageUnrouted();

   private static class ChannelState {
      final Lock lock = new ReentrantLock();
      final Set<Long> unackedMessageDeliveryTags = new HashSet<>();
      final Set<String> consumersWithManualAck = new HashSet<>();
      final Channel channel;

      private ChannelState(Channel channel) {
         this.channel = channel;
      }
   }

   private static class ConnectionState {
      final ConcurrentMap<Integer, AbstractMetricsCollector.ChannelState> channelState = new ConcurrentHashMap<>();
      final Connection connection;

      private ConnectionState(Connection connection) {
         this.connection = connection;
      }
   }
}
