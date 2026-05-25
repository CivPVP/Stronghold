package me.neznamy.tab.libs.com.rabbitmq.client.observation;

import java.io.IOException;
import me.neznamy.tab.libs.com.rabbitmq.client.AMQP;
import me.neznamy.tab.libs.com.rabbitmq.client.Consumer;
import me.neznamy.tab.libs.com.rabbitmq.client.GetResponse;

public interface ObservationCollector {
   ObservationCollector NO_OP = new NoOpObservationCollector();

   void publish(
      ObservationCollector.PublishCall var1, AMQP.Basic.Publish var2, AMQP.BasicProperties var3, byte[] var4, ObservationCollector.ConnectionInfo var5
   ) throws IOException;

   Consumer basicConsume(String var1, String var2, Consumer var3);

   GetResponse basicGet(ObservationCollector.BasicGetCall var1, String var2);

   interface BasicGetCall {
      GetResponse get();
   }

   interface ConnectionInfo {
      String getPeerAddress();

      int getPeerPort();
   }

   interface PublishCall {
      void publish(AMQP.BasicProperties var1) throws IOException;
   }
}
