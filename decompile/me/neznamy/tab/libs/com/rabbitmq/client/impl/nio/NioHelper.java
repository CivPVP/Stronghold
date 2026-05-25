package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class NioHelper {
   static int read(ReadableByteChannel channel, ByteBuffer buffer) throws IOException {
      int read = channel.read(buffer);
      if (read < 0) {
         throw new IOException("I/O thread: reached EOF");
      } else {
         return read;
      }
   }
}
