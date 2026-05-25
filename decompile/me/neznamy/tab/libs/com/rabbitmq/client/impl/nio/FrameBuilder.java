package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import me.neznamy.tab.libs.com.rabbitmq.client.MalformedFrameException;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.Frame;

public class FrameBuilder {
   private static final int PAYLOAD_OFFSET = 7;
   protected final ReadableByteChannel channel;
   protected final ByteBuffer applicationBuffer;
   private final int maxPayloadSize;
   private final int[] frameBuffer = new int[3];
   private int frameType;
   private int frameChannel;
   private byte[] framePayload;
   private int bytesRead = 0;

   public FrameBuilder(ReadableByteChannel channel, ByteBuffer buffer, int maxPayloadSize) {
      this.channel = channel;
      this.applicationBuffer = buffer;
      this.maxPayloadSize = maxPayloadSize;
   }

   public Frame readFrame() throws IOException {
      while (this.somethingToRead()) {
         if (this.bytesRead == 0) {
            this.frameType = this.readFromBuffer();
            if (this.frameType == 65) {
               this.handleProtocolVersionMismatch();
            }
         } else if (this.bytesRead == 1) {
            this.frameBuffer[0] = this.readFromBuffer();
         } else if (this.bytesRead == 2) {
            this.frameChannel = (this.frameBuffer[0] << 8) + this.readFromBuffer();
         } else if (this.bytesRead == 3) {
            this.frameBuffer[0] = this.readFromBuffer();
         } else if (this.bytesRead == 4) {
            this.frameBuffer[1] = this.readFromBuffer();
         } else if (this.bytesRead == 5) {
            this.frameBuffer[2] = this.readFromBuffer();
         } else if (this.bytesRead == 6) {
            int framePayloadSize = (this.frameBuffer[0] << 24) + (this.frameBuffer[1] << 16) + (this.frameBuffer[2] << 8) + this.readFromBuffer();
            if (framePayloadSize >= this.maxPayloadSize) {
               throw new IllegalStateException(
                  String.format(
                     "Frame body is too large (%d), maximum configured size is %d. See ConnectionFactory#setMaxInboundMessageBodySize if you need to increase the limit.",
                     framePayloadSize,
                     this.maxPayloadSize
                  )
               );
            }

            this.framePayload = new byte[framePayloadSize];
         } else {
            if (this.bytesRead < 7 || this.bytesRead >= this.framePayload.length + 7) {
               if (this.bytesRead == this.framePayload.length + 7) {
                  int frameEndMarker = this.readFromBuffer();
                  if (frameEndMarker != 206) {
                     throw new MalformedFrameException("Bad frame end marker: " + frameEndMarker);
                  }

                  this.bytesRead = 0;
                  return new Frame(this.frameType, this.frameChannel, this.framePayload);
               }

               throw new IllegalStateException("Number of read bytes incorrect: " + this.bytesRead);
            }

            this.framePayload[this.bytesRead - 7] = (byte)this.readFromBuffer();
         }

         this.bytesRead++;
      }

      return null;
   }

   protected boolean somethingToRead() throws IOException {
      if (!this.applicationBuffer.hasRemaining()) {
         ((Buffer)this.applicationBuffer).clear();
         int read = NioHelper.read(this.channel, this.applicationBuffer);
         ((Buffer)this.applicationBuffer).flip();
         return read > 0;
      } else {
         return true;
      }
   }

   private int readFromBuffer() {
      return this.applicationBuffer.get() & 0xFF;
   }

   private void handleProtocolVersionMismatch() throws IOException {
      byte[] expectedBytes = new byte[]{77, 81, 80};

      int expectedBytesCount;
      for (expectedBytesCount = 0; this.somethingToRead() && expectedBytesCount < 3; expectedBytesCount++) {
         int nextByte = this.readFromBuffer();
         if (nextByte != expectedBytes[expectedBytesCount]) {
            throw new MalformedFrameException(
               "Invalid AMQP protocol header from server: expected character " + expectedBytes[expectedBytesCount] + ", got " + nextByte
            );
         }
      }

      if (expectedBytesCount != 3) {
         throw new MalformedFrameException("Invalid AMQP protocol header from server: read only " + (expectedBytesCount + 1) + " byte(s) instead of 4");
      }

      int[] signature = new int[4];

      for (int i = 0; i < 4; i++) {
         if (!this.somethingToRead()) {
            throw new MalformedFrameException("Invalid AMQP protocol header from server");
         }

         signature[i] = this.readFromBuffer();
      }

      MalformedFrameException x;
      if (signature[0] == 1 && signature[1] == 1 && signature[2] == 8 && signature[3] == 0) {
         x = new MalformedFrameException("AMQP protocol version mismatch; we are version 0-9-1, server is 0-8");
      } else {
         String sig = "";

         for (int i = 0; i < 4; i++) {
            if (i != 0) {
               sig = sig + ",";
            }

            sig = sig + signature[i];
         }

         x = new MalformedFrameException("AMQP protocol version mismatch; we are version 0-9-1, server sent signature " + sig);
      }

      throw x;
   }

   public boolean isUnderflowHandlingEnabled() {
      return false;
   }
}
