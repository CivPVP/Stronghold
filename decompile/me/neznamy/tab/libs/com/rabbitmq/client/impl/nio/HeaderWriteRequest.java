package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import java.io.DataOutputStream;
import java.io.IOException;

public class HeaderWriteRequest implements WriteRequest {
   public static final WriteRequest SINGLETON = new HeaderWriteRequest();

   private HeaderWriteRequest() {
   }

   @Override
   public void handle(DataOutputStream outputStream) throws IOException {
      outputStream.write("AMQP".getBytes("US-ASCII"));
      outputStream.write(0);
      outputStream.write(0);
      outputStream.write(9);
      outputStream.write(1);
   }
}
