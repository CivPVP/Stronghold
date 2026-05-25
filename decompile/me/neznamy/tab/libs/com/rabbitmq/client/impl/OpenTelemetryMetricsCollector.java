package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import java.util.concurrent.atomic.AtomicLong;
import me.neznamy.tab.libs.com.rabbitmq.client.Channel;
import me.neznamy.tab.libs.com.rabbitmq.client.Connection;

public class OpenTelemetryMetricsCollector extends AbstractMetricsCollector {
   private final Attributes attributes;
   private final AtomicLong connections = new AtomicLong(0L);
   private final AtomicLong channels = new AtomicLong(0L);
   private final LongCounter publishedMessagesCounter;
   private final LongCounter consumedMessagesCounter;
   private final LongCounter acknowledgedMessagesCounter;
   private final LongCounter rejectedMessagesCounter;
   private final LongCounter failedToPublishMessagesCounter;
   private final LongCounter ackedPublishedMessagesCounter;
   private final LongCounter nackedPublishedMessagesCounter;
   private final LongCounter unroutedPublishedMessagesCounter;
   private final LongCounter requeuedMessagesCounter;

   public OpenTelemetryMetricsCollector(OpenTelemetry openTelemetry) {
      this(openTelemetry, "rabbitmq");
   }

   public OpenTelemetryMetricsCollector(OpenTelemetry openTelemetry, String prefix) {
      this(openTelemetry, prefix, Attributes.empty());
   }

   public OpenTelemetryMetricsCollector(OpenTelemetry openTelemetry, String prefix, Attributes attributes) {
      Meter meter = openTelemetry.getMeter("amqp-client");
      this.attributes = attributes;
      meter.gaugeBuilder(prefix + ".connections")
         .setUnit("{connections}")
         .setDescription("The number of connections to the RabbitMQ server")
         .ofLongs()
         .buildWithCallback(measurement -> measurement.record(this.connections.get(), attributes));
      meter.gaugeBuilder(prefix + ".channels")
         .setUnit("{channels}")
         .setDescription("The number of channels to the RabbitMQ server")
         .ofLongs()
         .buildWithCallback(measurement -> measurement.record(this.channels.get(), attributes));
      this.publishedMessagesCounter = meter.counterBuilder(prefix + ".published")
         .setUnit("{messages}")
         .setDescription("The number of messages published to the RabbitMQ server")
         .build();
      this.consumedMessagesCounter = meter.counterBuilder(prefix + ".consumed")
         .setUnit("{messages}")
         .setDescription("The number of messages consumed from the RabbitMQ server")
         .build();
      this.acknowledgedMessagesCounter = meter.counterBuilder(prefix + ".acknowledged")
         .setUnit("{messages}")
         .setDescription("The number of messages acknowledged from the RabbitMQ server")
         .build();
      this.rejectedMessagesCounter = meter.counterBuilder(prefix + ".rejected")
         .setUnit("{messages}")
         .setDescription("The number of messages rejected from the RabbitMQ server")
         .build();
      this.requeuedMessagesCounter = meter.counterBuilder(prefix + ".requeued")
         .setUnit("{messages}")
         .setDescription("The number of re-queued messages to the RabbitMQ server")
         .build();
      this.failedToPublishMessagesCounter = meter.counterBuilder(prefix + ".failed_to_publish")
         .setUnit("{messages}")
         .setDescription("The number of messages failed to publish to the RabbitMQ server")
         .build();
      this.ackedPublishedMessagesCounter = meter.counterBuilder(prefix + ".acknowledged_published")
         .setUnit("{messages}")
         .setDescription("The number of published messages acknowledged by the RabbitMQ server")
         .build();
      this.nackedPublishedMessagesCounter = meter.counterBuilder(prefix + ".not_acknowledged_published")
         .setUnit("{messages}")
         .setDescription("The number of published messages not acknowledged by the RabbitMQ server")
         .build();
      this.unroutedPublishedMessagesCounter = meter.counterBuilder(prefix + ".unrouted_published")
         .setUnit("{messages}")
         .setDescription("The number of un-routed published messages to the RabbitMQ server")
         .build();
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
      this.publishedMessagesCounter.add(1L, this.attributes);
   }

   @Override
   protected void markMessagePublishFailed() {
      this.failedToPublishMessagesCounter.add(1L, this.attributes);
   }

   @Override
   protected void markConsumedMessage() {
      this.consumedMessagesCounter.add(1L, this.attributes);
   }

   @Override
   protected void markAcknowledgedMessage() {
      this.acknowledgedMessagesCounter.add(1L, this.attributes);
   }

   @Override
   protected void markRejectedMessage() {
   }

   @Override
   protected void markRejectedMessage(boolean requeue) {
      if (requeue) {
         this.requeuedMessagesCounter.add(1L, this.attributes);
      }

      this.rejectedMessagesCounter.add(1L, this.attributes);
   }

   @Override
   protected void markMessagePublishAcknowledged() {
      this.ackedPublishedMessagesCounter.add(1L, this.attributes);
   }

   @Override
   protected void markMessagePublishNotAcknowledged() {
      this.nackedPublishedMessagesCounter.add(1L, this.attributes);
   }

   @Override
   protected void markPublishedMessageUnrouted() {
      this.unroutedPublishedMessagesCounter.add(1L, this.attributes);
   }

   public AtomicLong getConnections() {
      return this.connections;
   }

   public AtomicLong getChannels() {
      return this.channels;
   }
}
