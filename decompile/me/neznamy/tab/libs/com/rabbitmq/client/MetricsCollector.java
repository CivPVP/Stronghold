package me.neznamy.tab.libs.com.rabbitmq.client;

public interface MetricsCollector {
   void newConnection(Connection var1);

   void closeConnection(Connection var1);

   void newChannel(Channel var1);

   void closeChannel(Channel var1);

   void basicPublish(Channel var1);

   default void basicPublishFailure(Channel channel, Throwable cause) {
   }

   default void basicPublishAck(Channel channel, long deliveryTag, boolean multiple) {
   }

   default void basicPublishNack(Channel channel, long deliveryTag, boolean multiple) {
   }

   default void basicPublishUnrouted(Channel channel) {
   }

   void consumedMessage(Channel var1, long var2, boolean var4);

   void consumedMessage(Channel var1, long var2, String var4);

   void basicAck(Channel var1, long var2, boolean var4);

   void basicNack(Channel var1, long var2);

   default void basicNack(Channel channel, long deliveryTag, boolean requeue) {
      this.basicNack(channel, deliveryTag);
   }

   void basicReject(Channel var1, long var2);

   default void basicReject(Channel channel, long deliveryTag, boolean requeue) {
      this.basicReject(channel, deliveryTag);
   }

   void basicConsume(Channel var1, String var2, boolean var3);

   void basicCancel(Channel var1, String var2);
}
