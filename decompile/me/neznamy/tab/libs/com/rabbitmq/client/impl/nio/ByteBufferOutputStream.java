package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class ByteBufferOutputStream extends OutputStream {
   private final WritableByteChannel channel;
   private final ByteBuffer buffer;

   public ByteBufferOutputStream(WritableByteChannel channel, ByteBuffer buffer) {
      this.buffer = buffer;
      this.channel = channel;
   }

   @Override
   public void write(int b) throws IOException {
      if (!this.buffer.hasRemaining()) {
         drain(this.channel, this.buffer);
      }

      this.buffer.put((byte)b);
   }

   @Override
   public void flush() throws IOException {
      drain(this.channel, this.buffer);
   }

   public static void drain(WritableByteChannel channel, ByteBuffer buffer) throws IOException {
      ((Buffer)buffer).flip();

      while (buffer.hasRemaining() && channel.write(buffer) != -1) {
      }

      ((Buffer)buffer).clear();
   }
}
