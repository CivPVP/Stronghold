package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SslEngineHelper {
   private static final Logger LOGGER = LoggerFactory.getLogger(SslEngineHelper.class);

   public static boolean doHandshake(WritableByteChannel writeChannel, ReadableByteChannel readChannel, SSLEngine engine) throws IOException {
      ByteBuffer plainOut = ByteBuffer.allocate(engine.getSession().getApplicationBufferSize());
      ByteBuffer plainIn = ByteBuffer.allocate(engine.getSession().getApplicationBufferSize());
      ByteBuffer cipherOut = ByteBuffer.allocate(engine.getSession().getPacketBufferSize());
      ByteBuffer cipherIn = ByteBuffer.allocate(engine.getSession().getPacketBufferSize());
      LOGGER.debug("Starting TLS handshake");
      HandshakeStatus handshakeStatus = engine.getHandshakeStatus();
      LOGGER.debug("Initial handshake status is {}", handshakeStatus);

      while (handshakeStatus != HandshakeStatus.FINISHED && handshakeStatus != HandshakeStatus.NOT_HANDSHAKING) {
         LOGGER.debug("Handshake status is {}", handshakeStatus);
         switch (handshakeStatus) {
            case NEED_TASK:
               LOGGER.debug("Running tasks");
               handshakeStatus = runDelegatedTasks(engine);
               break;
            case NEED_UNWRAP:
               LOGGER.debug("Unwrapping...");
               handshakeStatus = unwrap(cipherIn, plainIn, readChannel, engine);
               break;
            case NEED_WRAP:
               LOGGER.debug("Wrapping...");
               handshakeStatus = wrap(plainOut, cipherOut, writeChannel, engine);
            case FINISHED:
            case NOT_HANDSHAKING:
               break;
            default:
               throw new SSLException("Unexpected handshake status " + handshakeStatus);
         }
      }

      LOGGER.debug("TLS handshake completed");
      return true;
   }

   private static HandshakeStatus runDelegatedTasks(SSLEngine sslEngine) {
      Runnable runnable;
      while ((runnable = sslEngine.getDelegatedTask()) != null) {
         LOGGER.debug("Running delegated task");
         runnable.run();
      }

      return sslEngine.getHandshakeStatus();
   }

   private static HandshakeStatus unwrap(ByteBuffer cipherIn, ByteBuffer plainIn, ReadableByteChannel channel, SSLEngine sslEngine) throws IOException {
      HandshakeStatus handshakeStatus = sslEngine.getHandshakeStatus();
      LOGGER.debug("Handshake status is {} before unwrapping", handshakeStatus);
      LOGGER.debug("Cipher in position {}", cipherIn.position());
      if (cipherIn.position() == 0) {
         LOGGER.debug("Reading from channel");
         int read = channel.read(cipherIn);
         LOGGER.debug("Read {} byte(s) from channel", read);
         if (read < 0) {
            throw new SSLException("Could not read from socket channel");
         }

         ((Buffer)cipherIn).flip();
      } else {
         LOGGER.debug("Not reading");
      }

      SSLEngineResult unwrapResult;
      do {
         int positionBeforeUnwrapping = cipherIn.position();
         LOGGER.debug("Before unwrapping cipherIn is {}, with {} remaining byte(s)", cipherIn, cipherIn.remaining());
         unwrapResult = sslEngine.unwrap(cipherIn, plainIn);
         LOGGER.debug("SSL engine result is {} after unwrapping", unwrapResult);
         Status status = unwrapResult.getStatus();
         switch (status) {
            case OK:
               ((Buffer)plainIn).clear();
               if (unwrapResult.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
                  handshakeStatus = runDelegatedTasks(sslEngine);
                  ((Buffer)cipherIn).position(positionBeforeUnwrapping + unwrapResult.bytesConsumed());
               } else {
                  handshakeStatus = unwrapResult.getHandshakeStatus();
               }
               break;
            case BUFFER_OVERFLOW:
               throw new SSLException("Buffer overflow during handshake");
            case BUFFER_UNDERFLOW:
               LOGGER.debug("Buffer underflow");
               cipherIn.compact();
               LOGGER.debug("Reading from channel...");
               int read = NioHelper.read(channel, cipherIn);
               if (read <= 0) {
                  retryRead(channel, cipherIn);
               }

               LOGGER.debug("Done reading from channel...");
               ((Buffer)cipherIn).flip();
               break;
            case CLOSED:
               sslEngine.closeInbound();
               break;
            default:
               throw new SSLException("Unexpected status from " + unwrapResult);
         }
      } while (unwrapResult.getHandshakeStatus() != HandshakeStatus.NEED_WRAP && unwrapResult.getHandshakeStatus() != HandshakeStatus.FINISHED);

      LOGGER.debug("cipherIn position after unwrap {}", cipherIn.position());
      return handshakeStatus;
   }

   private static int retryRead(ReadableByteChannel channel, ByteBuffer buffer) throws IOException {
      int attempt = 0;
      int read = 0;

      while (attempt < 3) {
         try {
            Thread.sleep(100L);
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         }

         read = NioHelper.read(channel, buffer);
         if (read > 0) {
            break;
         }

         attempt++;
      }

      return read;
   }

   private static HandshakeStatus wrap(ByteBuffer plainOut, ByteBuffer cipherOut, WritableByteChannel channel, SSLEngine sslEngine) throws IOException {
      HandshakeStatus handshakeStatus = sslEngine.getHandshakeStatus();
      LOGGER.debug("Handshake status is {} before wrapping", handshakeStatus);
      SSLEngineResult result = sslEngine.wrap(plainOut, cipherOut);
      LOGGER.debug("SSL engine result is {} after wrapping", result);
      switch (result.getStatus()) {
         case OK:
            ((Buffer)cipherOut).flip();

            while (cipherOut.hasRemaining()) {
               int written = channel.write(cipherOut);
               LOGGER.debug("Wrote {} byte(s)", written);
            }

            ((Buffer)cipherOut).clear();
            if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
               handshakeStatus = runDelegatedTasks(sslEngine);
            } else {
               handshakeStatus = result.getHandshakeStatus();
            }

            return handshakeStatus;
         case BUFFER_OVERFLOW:
            throw new SSLException("Buffer overflow during handshake");
         default:
            throw new SSLException("Unexpected status " + result.getStatus());
      }
   }

   public static void write(WritableByteChannel socketChannel, SSLEngine engine, ByteBuffer plainOut, ByteBuffer cypherOut) throws IOException {
      while (plainOut.hasRemaining()) {
         ((Buffer)cypherOut).clear();
         SSLEngineResult result = engine.wrap(plainOut, cypherOut);
         switch (result.getStatus()) {
            case OK:
               ((Buffer)cypherOut).flip();

               while (cypherOut.hasRemaining()) {
                  socketChannel.write(cypherOut);
               }
               break;
            case BUFFER_OVERFLOW:
               throw new SSLException("Buffer overflow occured after a wrap.");
            case BUFFER_UNDERFLOW:
               throw new SSLException("Buffer underflow occured after a wrap.");
            case CLOSED:
               throw new SSLException("Buffer closed");
            default:
               throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
         }
      }
   }

   public static void close(WritableByteChannel channel, SSLEngine engine) throws IOException {
      ByteBuffer plainOut = ByteBuffer.allocate(engine.getSession().getApplicationBufferSize());
      ByteBuffer cipherOut = ByteBuffer.allocate(engine.getSession().getPacketBufferSize());
      engine.closeOutbound();

      for (; !engine.isOutboundDone(); ((Buffer)cipherOut).clear()) {
         engine.wrap(plainOut, cipherOut);
         ((Buffer)cipherOut).flip();

         while (cipherOut.hasRemaining()) {
            int num = channel.write(cipherOut);
            if (num == -1) {
               break;
            }
         }
      }
   }
}
