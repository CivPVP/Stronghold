package me.neznamy.tab.libs.com.rabbitmq.client;

public class NoOpMetricsCollector implements MetricsCollector {
   @Override
   public void newConnection(Connection connection) {
   }

   @Override
   public void closeConnection(Connection connection) {
   }

   @Override
   public void newChannel(Channel channel) {
   }

   @Override
   public void closeChannel(Channel channel) {
   }

   @Override
   public void basicAck(Channel channel, long deliveryTag, boolean multiple) {
   }

   @Override
   public void basicNack(Channel channel, long deliveryTag) {
   }

   @Override
   public void basicNack(Channel channel, long deliveryTag, boolean requeue) {
   }

   @Override
   public void basicReject(Channel channel, long deliveryTag) {
   }

   @Override
   public void basicReject(Channel channel, long deliveryTag, boolean requeue) {
   }

   @Override
   public void basicConsume(Channel channel, String consumerTag, boolean autoAck) {
   }

   @Override
   public void basicCancel(Channel channel, String consumerTag) {
   }

   @Override
   public void basicPublish(Channel channel) {
   }

   @Override
   public void basicPublishFailure(Channel channel, Throwable cause) {
   }

   @Override
   public void basicPublishAck(Channel channel, long deliveryTag, boolean multiple) {
   }

   @Override
   public void basicPublishNack(Channel channel, long deliveryTag, boolean multiple) {
   }

   @Override
   public void basicPublishUnrouted(Channel channel) {
   }

   @Override
   public void consumedMessage(Channel channel, long deliveryTag, boolean autoAck) {
   }

   @Override
   public void consumedMessage(Channel channel, long deliveryTag, String consumerTag) {
   }
}
