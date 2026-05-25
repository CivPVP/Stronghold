package me.neznamy.tab.libs.com.rabbitmq.client;

public class GetResponse {
   private final Envelope envelope;
   private final AMQP.BasicProperties props;
   private final byte[] body;
   private final int messageCount;

   public GetResponse(Envelope envelope, AMQP.BasicProperties props, byte[] body, int messageCount) {
      this.envelope = envelope;
      this.props = props;
      this.body = body;
      this.messageCount = messageCount;
   }

   public Envelope getEnvelope() {
      return this.envelope;
   }

   public AMQP.BasicProperties getProps() {
      return this.props;
   }

   public byte[] getBody() {
      return this.body;
   }

   public int getMessageCount() {
      return this.messageCount;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("GetResponse(envelope=").append(this.envelope);
      sb.append(", props=").append(this.props);
      sb.append(", messageCount=").append(this.messageCount);
      sb.append(", body=(elided, ").append(this.body.length).append(" bytes long)");
      sb.append(")");
      return sb.toString();
   }
}
