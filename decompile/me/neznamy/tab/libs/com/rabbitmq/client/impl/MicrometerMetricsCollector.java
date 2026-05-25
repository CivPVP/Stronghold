package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import me.neznamy.tab.libs.com.rabbitmq.client.Channel;
import me.neznamy.tab.libs.com.rabbitmq.client.Connection;

public class MicrometerMetricsCollector extends AbstractMetricsCollector {
   private final AtomicLong connections;
   private final AtomicLong channels;
   private final Counter publishedMessages;
   private final Counter failedToPublishMessages;
   private final Counter ackedPublishedMessages;
   private final Counter nackedPublishedMessages;
   private final Counter unroutedPublishedMessages;
   private final Counter consumedMessages;
   private final Counter acknowledgedMessages;
   private final Counter rejectedMessages;
   private final Counter requeuedMessages;

   public MicrometerMetricsCollector(MeterRegistry registry) {
      this(registry, "rabbitmq");
   }

   public MicrometerMetricsCollector(MeterRegistry registry, String prefix) {
      this(registry, prefix, Collections.emptyList());
   }

   public MicrometerMetricsCollector(MeterRegistry registry, String prefix, String... tags) {
      this(registry, prefix, Tags.of(tags));
   }

   public MicrometerMetricsCollector(MeterRegistry registry, String prefix, Iterable<Tag> tags) {
      this(metric -> metric.create(registry, prefix, tags));
   }

   public MicrometerMetricsCollector(Function<MicrometerMetricsCollector.Metrics, Object> metricsCreator) {
      this.connections = (AtomicLong)metricsCreator.apply(MicrometerMetricsCollector.Metrics.CONNECTIONS);
      this.channels = (AtomicLong)metricsCreator.apply(MicrometerMetricsCollector.Metrics.CHANNELS);
      this.publishedMessages = (Counter)metricsCreator.apply(MicrometerMetricsCollector.Metrics.PUBLISHED_MESSAGES);
      this.consumedMessages = (Counter)metricsCreator.apply(MicrometerMetricsCollector.Metrics.CONSUMED_MESSAGES);
      this.acknowledgedMessages = (Counter)metricsCreator.apply(MicrometerMetricsCollector.Metrics.ACKNOWLEDGED_MESSAGES);
      this.rejectedMessages = (Counter)metricsCreator.apply(MicrometerMetricsCollector.Metrics.REJECTED_MESSAGES);
      this.failedToPublishMessages = (Counter)metricsCreator.apply(MicrometerMetricsCollector.Metrics.FAILED_TO_PUBLISH_MESSAGES);
      this.ackedPublishedMessages = (Counter)metricsCreator.apply(MicrometerMetricsCollector.Metrics.ACKED_PUBLISHED_MESSAGES);
      this.nackedPublishedMessages = (Counter)metricsCreator.apply(MicrometerMetricsCollector.Metrics.NACKED_PUBLISHED_MESSAGES);
      this.unroutedPublishedMessages = (Counter)metricsCreator.apply(MicrometerMetricsCollector.Metrics.UNROUTED_PUBLISHED_MESSAGES);
      this.requeuedMessages = (Counter)metricsCreator.apply(MicrometerMetricsCollector.Metrics.REQUEUED_MESSAGES);
   }

   @Override
   protected void incrementConnectionCount(Connection connection) {
      this.connections.incrementAndGet();
   }

   @Override
   protected void decrementConnectionCount(Connection connection) {
      this.connections.decrementAndGet();
   }

   @Override
   protected void incrementChannelCount(Channel channel) {
      this.channels.incrementAndGet();
   }

   @Override
   protected void decrementChannelCount(Channel channel) {
      this.channels.decrementAndGet();
   }

   @Override
   protected void markPublishedMessage() {
      this.publishedMessages.increment();
   }

   @Override
   protected void markMessagePublishFailed() {
      this.failedToPublishMessages.increment();
   }

   @Override
   protected void markConsumedMessage() {
      this.consumedMessages.increment();
   }

   @Override
   protected void markAcknowledgedMessage() {
      this.acknowledgedMessages.increment();
   }

   @Override
   protected void markRejectedMessage() {
   }

   @Override
   protected void markRejectedMessage(boolean requeue) {
      if (requeue) {
         this.requeuedMessages.increment();
      }

      this.rejectedMessages.increment();
   }

   @Override
   protected void markMessagePublishAcknowledged() {
      this.ackedPublishedMessages.increment();
   }

   @Override
   protected void markMessagePublishNotAcknowledged() {
      this.nackedPublishedMessages.increment();
   }

   @Override
   protected void markPublishedMessageUnrouted() {
      this.unroutedPublishedMessages.increment();
   }

   public AtomicLong getConnections() {
      return this.connections;
   }

   public AtomicLong getChannels() {
      return this.channels;
   }

   public Counter getPublishedMessages() {
      return this.publishedMessages;
   }

   public Counter getFailedToPublishMessages() {
      return this.failedToPublishMessages;
   }

   public Counter getAckedPublishedMessages() {
      return this.ackedPublishedMessages;
   }

   public Counter getNackedPublishedMessages() {
      return this.nackedPublishedMessages;
   }

   public Counter getUnroutedPublishedMessages() {
      return this.unroutedPublishedMessages;
   }

   public Counter getConsumedMessages() {
      return this.consumedMessages;
   }

   public Counter getAcknowledgedMessages() {
      return this.acknowledgedMessages;
   }

   public Counter getRejectedMessages() {
      return this.rejectedMessages;
   }

   public Counter getRequeuedMessages() {
      return this.requeuedMessages;
   }

   public enum Metrics {
      CONNECTIONS {
         @Override
         Object create(MeterRegistry registry, String prefix, Iterable<Tag> tags) {
            return registry.gauge(prefix + ".connections", tags, new AtomicLong(0L));
         }
      },
      CHANNELS {
         @Override
         Object create(MeterRegistry registry, String prefix, Iterable<Tag> tags) {
            return registry.gauge(prefix + ".channels", tags, new AtomicLong(0L));
         }
      },
      PUBLISHED_MESSAGES {
         @Override
         Object create(MeterRegistry registry, String prefix, Iterable<Tag> tags) {
            return registry.counter(prefix + ".published", tags);
         }
      },
      CONSUMED_MESSAGES {
         @Override
         Object create(MeterRegistry registry, String prefix, Iterable<Tag> tags) {
            return registry.counter(prefix + ".consumed", tags);
         }
      },
      ACKNOWLEDGED_MESSAGES {
         @Override
         Object create(MeterRegistry registry, String prefix, Iterable<Tag> tags) {
            return registry.counter(prefix + ".acknowledged", tags);
         }
      },
      REJECTED_MESSAGES {
         @Override
         Object create(MeterRegistry registry, String prefix, Iterable<Tag> tags) {
            return registry.counter(prefix + ".rejected", tags);
         }
      },
      REQUEUED_MESSAGES {
         @Override
         Object create(MeterRegistry registry, String prefix, Iterable<Tag> tags) {
            return registry.counter(prefix + ".requeued", tags);
         }
      },
      FAILED_TO_PUBLISH_MESSAGES {
         @Override
         Object create(MeterRegistry registry, String prefix, Iterable<Tag> tags) {
            return registry.counter(prefix + ".failed_to_publish", tags);
         }
      },
      ACKED_PUBLISHED_MESSAGES {
         @Override
         Object create(MeterRegistry registry, String prefix, Iterable<Tag> tags) {
            return registry.counter(prefix + ".acknowledged_published", tags);
         }
      },
      NACKED_PUBLISHED_MESSAGES {
         @Override
         Object create(MeterRegistry registry, String prefix, Iterable<Tag> tags) {
            return registry.counter(prefix + ".not_acknowledged_published", tags);
         }
      },
      UNROUTED_PUBLISHED_MESSAGES {
         @Override
         Object create(MeterRegistry registry, String prefix, Iterable<Tag> tags) {
            return registry.counter(prefix + ".unrouted_published", tags);
         }
      };

      Metrics() {
      }

      @Deprecated
      Object create(MeterRegistry registry, String prefix) {
         return this.create(registry, prefix, Collections.emptyList());
      }

      abstract Object create(MeterRegistry var1, String var2, Iterable<Tag> var3);
   }
}
