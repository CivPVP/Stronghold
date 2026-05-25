package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

public class SslEngineFrameBuilder extends FrameBuilder {
   private final SSLEngine sslEngine;
   private final ByteBuffer cipherBuffer;
   private boolean isUnderflowHandlingEnabled = false;

   public SslEngineFrameBuilder(SSLEngine sslEngine, ByteBuffer plainIn, ByteBuffer cipherIn, ReadableByteChannel channel, int maxPayloadSize) {
      super(channel, plainIn, maxPayloadSize);
      this.sslEngine = sslEngine;
      this.cipherBuffer = cipherIn;
   }

   @Override
   protected boolean somethingToRead() throws IOException {
      if (this.applicationBuffer.hasRemaining() && !this.isUnderflowHandlingEnabled) {
         return true;
      }

      ((Buffer)this.applicationBuffer).clear();
      boolean underflowHandling = false;

      try {
         SSLEngineResult result = this.sslEngine.unwrap(this.cipherBuffer, this.applicationBuffer);
         switch (result.getStatus()) {
            case OK:
               ((Buffer)this.applicationBuffer).flip();
               if (this.applicationBuffer.hasRemaining()) {
                  return true;
               }

               ((Buffer)this.applicationBuffer).clear();
               return false;
            case BUFFER_OVERFLOW:
               throw new SSLException("buffer overflow in read");
            case BUFFER_UNDERFLOW:
               this.cipherBuffer.compact();
               underflowHandling = true;
               return false;
            case CLOSED:
               throw new SSLException("closed in read");
            default:
               throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
         }
      } finally {
         this.isUnderflowHandlingEnabled = underflowHandling;
      }
   }

   @Override
   public boolean isUnderflowHandlingEnabled() {
      return this.isUnderflowHandlingEnabled;
   }
}
