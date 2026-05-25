package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;

public class DefaultConsumer implements Consumer {
   private final Channel _channel;
   private volatile String _consumerTag;

   public DefaultConsumer(Channel channel) {
      this._channel = channel;
   }

   @Override
   public void handleConsumeOk(String consumerTag) {
      this._consumerTag = consumerTag;
   }

   @Override
   public void handleCancelOk(String consumerTag) {
   }

   @Override
   public void handleCancel(String consumerTag) throws IOException {
   }

   @Override
   public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
   }

   @Override
   public void handleRecoverOk(String consumerTag) {
   }

   @Override
   public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
   }

   public Channel getChannel() {
      return this._channel;
   }

   public String getConsumerTag() {
      return this._consumerTag;
   }
}
