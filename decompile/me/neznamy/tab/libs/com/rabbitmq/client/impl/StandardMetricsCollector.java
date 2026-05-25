package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import me.neznamy.tab.libs.com.rabbitmq.client.Channel;
import me.neznamy.tab.libs.com.rabbitmq.client.Connection;

public class StandardMetricsCollector extends AbstractMetricsCollector {
   private final MetricRegistry registry;
   private final Counter connections;
   private final Counter channels;
   private final Meter publishedMessages;
   private final Meter consumedMessages;
   private final Meter acknowledgedMessages;
   private final Meter rejectedMessages;
   private final Meter requeuedMessages;
   private final Meter failedToPublishMessages;
   private final Meter publishAcknowledgedMessages;
   private final Meter publishNacknowledgedMessages;
   private final Meter publishUnroutedMessages;

   public StandardMetricsCollector(MetricRegistry registry, String metricsPrefix) {
      this.registry = registry;
      this.connections = registry.counter(metricsPrefix + ".connections");
      this.channels = registry.counter(metricsPrefix + ".channels");
      this.publishedMessages = registry.meter(metricsPrefix + ".published");
      this.failedToPublishMessages = registry.meter(metricsPrefix + ".failed_to_publish");
      this.publishAcknowledgedMessages = registry.meter(metricsPrefix + ".publish_ack");
      this.publishNacknowledgedMessages = registry.meter(metricsPrefix + ".publish_nack");
      this.publishUnroutedMessages = registry.meter(metricsPrefix + ".publish_unrouted");
      this.consumedMessages = registry.meter(metricsPrefix + ".consumed");
      this.acknowledgedMessages = registry.meter(metricsPrefix + ".acknowledged");
      this.rejectedMessages = registry.meter(metricsPrefix + ".rejected");
      this.requeuedMessages = registry.meter(metricsPrefix + ".requeued");
   }

   public StandardMetricsCollector() {
      this(new MetricRegistry());
   }

   public StandardMetricsCollector(MetricRegistry metricRegistry) {
      this(metricRegistry, "rabbitmq");
   }

   @Override
   protected void incrementConnectionCount(Connection connection) {
      this.connections.inc();
   }

   @Override
   protected void decrementConnectionCount(Connection connection) {
      this.connections.dec();
   }

   @Override
   protected void incrementChannelCount(Channel channel) {
      this.channels.inc();
   }

   @Override
   protected void decrementChannelCount(Channel channel) {
      this.channels.dec();
   }

   @Override
   protected void markPublishedMessage() {
      this.publishedMessages.mark();
   }

   @Override
   protected void markMessagePublishFailed() {
      this.failedToPublishMessages.mark();
   }

   @Override
   protected void markConsumedMessage() {
      this.consumedMessages.mark();
   }

   @Override
   protected void markAcknowledgedMessage() {
      this.acknowledgedMessages.mark();
   }

   @Override
   protected void markRejectedMessage() {
   }

   @Override
   protected void markRejectedMessage(boolean requeue) {
      if (requeue) {
         this.requeuedMessages.mark();
      }

      this.rejectedMessages.mark();
   }

   @Override
   protected void markMessagePublishAcknowledged() {
      this.publishAcknowledgedMessages.mark();
   }

   @Override
   protected void markMessagePublishNotAcknowledged() {
      this.publishNacknowledgedMessages.mark();
   }

   @Override
   protected void markPublishedMessageUnrouted() {
      this.publishUnroutedMessages.mark();
   }

   public MetricRegistry getMetricRegistry() {
      return this.registry;
   }

   public Counter getConnections() {
      return this.connections;
   }

   public Counter getChannels() {
      return this.channels;
   }

   public Meter getPublishedMessages() {
      return this.publishedMessages;
   }

   public Meter getConsumedMessages() {
      return this.consumedMessages;
   }

   public Meter getAcknowledgedMessages() {
      return this.acknowledgedMessages;
   }

   public Meter getRejectedMessages() {
      return this.rejectedMessages;
   }

   public Meter getRequeuedMessages() {
      return this.requeuedMessages;
   }

   public Meter getFailedToPublishMessages() {
      return this.failedToPublishMessages;
   }

   public Meter getPublishAcknowledgedMessages() {
      return this.publishAcknowledgedMessages;
   }

   public Meter getPublishNotAcknowledgedMessages() {
      return this.publishNacknowledgedMessages;
   }

   public Meter getPublishUnroutedMessages() {
      return this.publishUnroutedMessages;
   }
}
