package me.neznamy.tab.libs.com.rabbitmq.client;

public class Return {
   private final int replyCode;
   private final String replyText;
   private final String exchange;
   private final String routingKey;
   private final AMQP.BasicProperties properties;
   private final byte[] body;

   public Return(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) {
      this.replyCode = replyCode;
      this.replyText = replyText;
      this.exchange = exchange;
      this.routingKey = routingKey;
      this.properties = properties;
      this.body = body;
   }

   public int getReplyCode() {
      return this.replyCode;
   }

   public String getReplyText() {
      return this.replyText;
   }

   public String getExchange() {
      return this.exchange;
   }

   public String getRoutingKey() {
      return this.routingKey;
   }

   public AMQP.BasicProperties getProperties() {
      return this.properties;
   }

   public byte[] getBody() {
      return this.body;
   }
}
