package me.neznamy.tab.libs.com.rabbitmq.client;

public class Envelope {
   private final long _deliveryTag;
   private final boolean _redeliver;
   private final String _exchange;
   private final String _routingKey;

   public Envelope(long deliveryTag, boolean redeliver, String exchange, String routingKey) {
      this._deliveryTag = deliveryTag;
      this._redeliver = redeliver;
      this._exchange = exchange;
      this._routingKey = routingKey;
   }

   public long getDeliveryTag() {
      return this._deliveryTag;
   }

   public boolean isRedeliver() {
      return this._redeliver;
   }

   public String getExchange() {
      return this._exchange;
   }

   public String getRoutingKey() {
      return this._routingKey;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Envelope(deliveryTag=").append(this._deliveryTag);
      sb.append(", redeliver=").append(this._redeliver);
      sb.append(", exchange=").append(this._exchange);
      sb.append(", routingKey=").append(this._routingKey);
      sb.append(")");
      return sb.toString();
   }
}
