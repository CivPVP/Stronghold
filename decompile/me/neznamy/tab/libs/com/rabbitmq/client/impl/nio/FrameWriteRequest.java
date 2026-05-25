package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import java.io.DataOutputStream;
import java.io.IOException;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.Frame;

public class FrameWriteRequest implements WriteRequest {
   final Frame frame;

   public FrameWriteRequest(Frame frame) {
      this.frame = frame;
   }

   @Override
   public void handle(DataOutputStream outputStream) throws IOException {
      this.frame.writeTo(outputStream);
   }
}
