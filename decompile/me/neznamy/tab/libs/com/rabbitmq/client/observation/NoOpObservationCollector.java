package me.neznamy.tab.libs.com.rabbitmq.client.observation;

import java.io.IOException;
import me.neznamy.tab.libs.com.rabbitmq.client.AMQP;
import me.neznamy.tab.libs.com.rabbitmq.client.Consumer;
import me.neznamy.tab.libs.com.rabbitmq.client.GetResponse;

final class NoOpObservationCollector implements ObservationCollector {
   @Override
   public void publish(
      ObservationCollector.PublishCall call,
      AMQP.Basic.Publish publish,
      AMQP.BasicProperties properties,
      byte[] body,
      ObservationCollector.ConnectionInfo connectionInfo
   ) throws IOException {
      call.publish(properties);
   }

   @Override
   public Consumer basicConsume(String queue, String consumerTag, Consumer consumer) {
      return consumer;
   }

   @Override
   public GetResponse basicGet(ObservationCollector.BasicGetCall call, String queue) {
      return call.get();
   }
}
