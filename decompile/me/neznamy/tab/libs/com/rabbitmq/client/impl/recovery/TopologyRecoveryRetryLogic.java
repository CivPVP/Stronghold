package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import me.neznamy.tab.libs.com.rabbitmq.client.AMQP;
import me.neznamy.tab.libs.com.rabbitmq.client.ShutdownSignalException;
import me.neznamy.tab.libs.com.rabbitmq.utility.Utility;

public abstract class TopologyRecoveryRetryLogic {
   public static final BiPredicate<RecordedEntity, Exception> CHANNEL_CLOSED_NOT_FOUND = (entity, ex) -> {
      if (ex.getCause() instanceof ShutdownSignalException) {
         ShutdownSignalException cause = (ShutdownSignalException)ex.getCause();
         if (cause.getReason() instanceof AMQP.Channel.Close) {
            return ((AMQP.Channel.Close)cause.getReason()).getReplyCode() == 404;
         }
      }

      return false;
   };
   public static final DefaultRetryHandler.RetryOperation<Void> RECOVER_CHANNEL = context -> {
      if (!context.entity().getChannel().isOpen()) {
         context.connection().recoverChannel(context.entity().getChannel());
      }

      return null;
   };
   public static final DefaultRetryHandler.RetryOperation<Void> RECOVER_QUEUE = context -> {
      if (context.entity() instanceof RecordedQueue) {
         RecordedQueue recordedQueue = context.queue();
         AutorecoveringConnection connection = context.connection();
         connection.recoverQueue(recordedQueue.getName(), recordedQueue);
      }

      return null;
   };
   public static final DefaultRetryHandler.RetryOperation<Void> RECOVER_BINDING_QUEUE = context -> {
      if (context.entity() instanceof RecordedQueueBinding) {
         RecordedBinding binding = context.binding();
         AutorecoveringConnection connection = context.connection();
         RecordedQueue recordedQueue = connection.getRecordedQueues().get(binding.getDestination());
         if (recordedQueue != null) {
            connection.recoverQueue(recordedQueue.getName(), recordedQueue);
         }
      }

      return null;
   };
   public static final DefaultRetryHandler.RetryOperation<Void> RECOVER_BINDING = context -> {
      context.binding().recover();
      return null;
   };
   public static final DefaultRetryHandler.RetryOperation<Void> RECOVER_PREVIOUS_QUEUE_BINDINGS = context -> {
      if (context.entity() instanceof RecordedQueueBinding) {
         String queue = context.binding().getDestination();

         for (RecordedBinding recordedBinding : Utility.copy(context.connection().getRecordedBindings())) {
            if (recordedBinding == context.entity()) {
               break;
            }

            if (recordedBinding instanceof RecordedQueueBinding && queue.equals(recordedBinding.getDestination())) {
               recordedBinding.recover();
            }
         }
      }

      return null;
   };
   public static final DefaultRetryHandler.RetryOperation<Void> RECOVER_CONSUMER_QUEUE = context -> {
      if (context.entity() instanceof RecordedConsumer) {
         RecordedConsumer consumer = context.consumer();
         AutorecoveringConnection connection = context.connection();
         RecordedQueue recordedQueue = connection.getRecordedQueues().get(consumer.getQueue());
         if (recordedQueue != null) {
            connection.recoverQueue(recordedQueue.getName(), recordedQueue);
         }
      }

      return null;
   };
   public static final DefaultRetryHandler.RetryOperation<Void> RECOVER_CONSUMER_QUEUE_BINDINGS = context -> {
      if (context.entity() instanceof RecordedConsumer) {
         String queue = context.consumer().getQueue();

         for (RecordedBinding recordedBinding : Utility.copy(context.connection().getRecordedBindings())) {
            if (recordedBinding instanceof RecordedQueueBinding && queue.equals(recordedBinding.getDestination())) {
               recordedBinding.recover();
            }
         }
      }

      return null;
   };
   public static final DefaultRetryHandler.RetryOperation<String> RECOVER_CONSUMER = context -> context.consumer().recover();
   public static final DefaultRetryHandler.RetryOperation<String> RECOVER_PREVIOUS_CONSUMERS = context -> {
      if (!(context.entity() instanceof RecordedConsumer)) {
         return null;
      }

      AutorecoveringChannel channel = context.consumer().getChannel();

      for (RecordedConsumer consumer : Utility.copy(context.connection().getRecordedConsumers()).values()) {
         if (consumer == context.entity()) {
            break;
         }

         if (consumer.getChannel() == channel) {
            RetryContext retryContext = new RetryContext(consumer, context.exception(), context.connection());
            RECOVER_CONSUMER_QUEUE.call(retryContext);
            context.connection().recoverConsumer(consumer.getConsumerTag(), consumer);
            RECOVER_CONSUMER_QUEUE_BINDINGS.call(retryContext);
         }
      }

      return context.consumer().getConsumerTag();
   };
   public static final DefaultRetryHandler.RetryOperation<Void> RECOVER_PREVIOUS_AUTO_DELETE_QUEUES = context -> {
      if (context.entity() instanceof RecordedQueue) {
         AutorecoveringConnection connection = context.connection();
         RecordedQueue queue = context.queue();

         for (Entry<String, RecordedQueue> entry : Utility.copy(connection.getRecordedQueues()).entrySet()) {
            if (entry.getValue() == queue) {
               break;
            }

            if (queue.getChannel() == entry.getValue().getChannel() && (entry.getValue().isAutoDelete() || entry.getValue().isExclusive())) {
               connection.recoverQueue(entry.getKey(), entry.getValue());
            }
         }
      } else if (context.entity() instanceof RecordedQueueBinding) {
         AutorecoveringConnection connection = context.connection();
         Set<String> queues = new LinkedHashSet<>();

         for (Entry<String, RecordedQueue> entry : Utility.copy(connection.getRecordedQueues()).entrySet()) {
            if (context.entity().getChannel() == entry.getValue().getChannel() && (entry.getValue().isAutoDelete() || entry.getValue().isExclusive())) {
               connection.recoverQueue(entry.getKey(), entry.getValue());
               queues.add(entry.getValue().getName());
            }
         }

         for (RecordedBinding binding : Utility.copy(connection.getRecordedBindings())) {
            if (binding instanceof RecordedQueueBinding && queues.contains(binding.getDestination())) {
               binding.recover();
            }
         }
      }

      return null;
   };
   public static final TopologyRecoveryRetryHandlerBuilder RETRY_ON_QUEUE_NOT_FOUND_RETRY_HANDLER = TopologyRecoveryRetryHandlerBuilder.builder()
      .queueRecoveryRetryCondition(CHANNEL_CLOSED_NOT_FOUND)
      .bindingRecoveryRetryCondition(CHANNEL_CLOSED_NOT_FOUND)
      .consumerRecoveryRetryCondition(CHANNEL_CLOSED_NOT_FOUND)
      .queueRecoveryRetryOperation(RECOVER_CHANNEL.andThen(RECOVER_QUEUE).andThen(RECOVER_PREVIOUS_AUTO_DELETE_QUEUES))
      .bindingRecoveryRetryOperation(
         RECOVER_CHANNEL.andThen(RECOVER_BINDING_QUEUE)
            .andThen(RECOVER_BINDING)
            .andThen(RECOVER_PREVIOUS_QUEUE_BINDINGS)
            .andThen(RECOVER_PREVIOUS_AUTO_DELETE_QUEUES)
      )
      .consumerRecoveryRetryOperation(
         RECOVER_CHANNEL.andThen(RECOVER_CONSUMER_QUEUE).andThen(RECOVER_CONSUMER).andThen(RECOVER_CONSUMER_QUEUE_BINDINGS).andThen(RECOVER_PREVIOUS_CONSUMERS)
      );
}
