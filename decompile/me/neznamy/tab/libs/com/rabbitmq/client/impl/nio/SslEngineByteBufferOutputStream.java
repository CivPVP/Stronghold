package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import javax.net.ssl.SSLEngine;

public class SslEngineByteBufferOutputStream extends OutputStream {
   private final SSLEngine sslEngine;
   private final ByteBuffer plainOut;
   private final ByteBuffer cypherOut;
   private final WritableByteChannel channel;

   public SslEngineByteBufferOutputStream(SSLEngine sslEngine, ByteBuffer plainOut, ByteBuffer cypherOut, WritableByteChannel channel) {
      this.sslEngine = sslEngine;
      this.plainOut = plainOut;
      this.cypherOut = cypherOut;
      this.channel = channel;
   }

   @Override
   public void write(int b) throws IOException {
      if (!this.plainOut.hasRemaining()) {
         this.doFlush();
      }

      this.plainOut.put((byte)b);
   }

   @Override
   public void flush() throws IOException {
      if (this.plainOut.position() > 0) {
         this.doFlush();
      }
   }

   private void doFlush() throws IOException {
      ((Buffer)this.plainOut).flip();
      SslEngineHelper.write(this.channel, this.sslEngine, this.plainOut, this.cypherOut);
      ((Buffer)this.plainOut).clear();
   }
}
