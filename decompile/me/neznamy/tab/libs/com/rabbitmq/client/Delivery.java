package me.neznamy.tab.libs.com.rabbitmq.client;

public class Delivery {
   private final Envelope _envelope;
   private final AMQP.BasicProperties _properties;
   private final byte[] _body;

   public Delivery(Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
      this._envelope = envelope;
      this._properties = properties;
      this._body = body;
   }

   public Envelope getEnvelope() {
      return this._envelope;
   }

   public AMQP.BasicProperties getProperties() {
      return this._properties;
   }

   public byte[] getBody() {
      return this._body;
   }
}
