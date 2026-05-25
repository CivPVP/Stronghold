package me.neznamy.tab.libs.com.rabbitmq.client;

public class MessageProperties {
   public static final AMQP.BasicProperties MINIMAL_BASIC = new AMQP.BasicProperties(
      null, null, null, null, null, null, null, null, null, null, null, null, null, null
   );
   public static final AMQP.BasicProperties MINIMAL_PERSISTENT_BASIC = new AMQP.BasicProperties(
      null, null, null, 2, null, null, null, null, null, null, null, null, null, null
   );
   public static final AMQP.BasicProperties BASIC = new AMQP.BasicProperties(
      "application/octet-stream", null, null, 1, 0, null, null, null, null, null, null, null, null, null
   );
   public static final AMQP.BasicProperties PERSISTENT_BASIC = new AMQP.BasicProperties(
      "application/octet-stream", null, null, 2, 0, null, null, null, null, null, null, null, null, null
   );
   public static final AMQP.BasicProperties TEXT_PLAIN = new AMQP.BasicProperties(
      "text/plain", null, null, 1, 0, null, null, null, null, null, null, null, null, null
   );
   public static final AMQP.BasicProperties PERSISTENT_TEXT_PLAIN = new AMQP.BasicProperties(
      "text/plain", null, null, 2, 0, null, null, null, null, null, null, null, null, null
   );
}
