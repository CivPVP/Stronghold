package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.neznamy.tab.libs.com.rabbitmq.client.UnexpectedFrameError;

final class CommandAssembler {
   private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
   private CommandAssembler.CAState state;
   private Method method;
   private AMQContentHeader contentHeader;
   private final List<byte[]> bodyN;
   private int bodyLength;
   private long remainingBodyBytes;
   private final int maxBodyLength;

   public CommandAssembler(Method method, AMQContentHeader contentHeader, byte[] body, int maxBodyLength) {
      this.method = method;
      this.contentHeader = contentHeader;
      this.bodyN = new ArrayList<>(2);
      this.bodyLength = 0;
      this.remainingBodyBytes = 0L;
      this.maxBodyLength = maxBodyLength;
      this.appendBodyFragment(body);
      if (method == null) {
         this.state = CommandAssembler.CAState.EXPECTING_METHOD;
      } else if (contentHeader == null) {
         this.state = method.hasContent() ? CommandAssembler.CAState.EXPECTING_CONTENT_HEADER : CommandAssembler.CAState.COMPLETE;
      } else {
         this.remainingBodyBytes = contentHeader.getBodySize() - this.bodyLength;
         this.updateContentBodyState();
      }
   }

   public synchronized Method getMethod() {
      return this.method;
   }

   public synchronized AMQContentHeader getContentHeader() {
      return this.contentHeader;
   }

   public synchronized boolean isComplete() {
      return this.state == CommandAssembler.CAState.COMPLETE;
   }

   private void updateContentBodyState() {
      this.state = this.remainingBodyBytes > 0L ? CommandAssembler.CAState.EXPECTING_CONTENT_BODY : CommandAssembler.CAState.COMPLETE;
   }

   private void consumeMethodFrame(Frame f) throws IOException {
      if (f.type == 1) {
         this.method = AMQImpl.readMethodFrom(f.getInputStream());
         this.state = this.method.hasContent() ? CommandAssembler.CAState.EXPECTING_CONTENT_HEADER : CommandAssembler.CAState.COMPLETE;
      } else {
         throw new UnexpectedFrameError(f, 1);
      }
   }

   private void consumeHeaderFrame(Frame f) throws IOException {
      if (f.type == 2) {
         this.contentHeader = AMQImpl.readContentHeaderFrom(f.getInputStream());
         long bodySize = this.contentHeader.getBodySize();
         if (bodySize >= this.maxBodyLength) {
            throw new IllegalStateException(
               String.format(
                  "Message body is too large (%d), maximum configured size is %d. See ConnectionFactory#setMaxInboundMessageBodySize if you need to increase the limit.",
                  bodySize,
                  this.maxBodyLength
               )
            );
         }

         this.remainingBodyBytes = bodySize;
         this.updateContentBodyState();
      } else {
         throw new UnexpectedFrameError(f, 2);
      }
   }

   private void consumeBodyFrame(Frame f) {
      if (f.type == 3) {
         byte[] fragment = f.getPayload();
         this.remainingBodyBytes -= fragment.length;
         this.updateContentBodyState();
         if (this.remainingBodyBytes < 0L) {
            throw new UnsupportedOperationException("%%%%%% FIXME unimplemented");
         }

         this.appendBodyFragment(fragment);
      } else {
         throw new UnexpectedFrameError(f, 3);
      }
   }

   private byte[] coalesceContentBody() {
      if (this.bodyLength == 0) {
         return EMPTY_BYTE_ARRAY;
      }

      if (this.bodyN.size() == 1) {
         return this.bodyN.get(0);
      }

      byte[] body = new byte[this.bodyLength];
      int offset = 0;

      for (byte[] fragment : this.bodyN) {
         System.arraycopy(fragment, 0, body, offset, fragment.length);
         offset += fragment.length;
      }

      this.bodyN.clear();
      this.bodyN.add(body);
      return body;
   }

   public synchronized byte[] getContentBody() {
      return this.coalesceContentBody();
   }

   private void appendBodyFragment(byte[] fragment) {
      if (fragment != null && fragment.length != 0) {
         this.bodyN.add(fragment);
         this.bodyLength += fragment.length;
      }
   }

   public synchronized boolean handleFrame(Frame f) throws IOException {
      switch (this.state) {
         case EXPECTING_METHOD:
            this.consumeMethodFrame(f);
            break;
         case EXPECTING_CONTENT_HEADER:
            this.consumeHeaderFrame(f);
            break;
         case EXPECTING_CONTENT_BODY:
            this.consumeBodyFrame(f);
            break;
         default:
            throw new IllegalStateException("Bad Command State " + this.state);
      }

      return this.isComplete();
   }

   private enum CAState {
      EXPECTING_METHOD,
      EXPECTING_CONTENT_HEADER,
      EXPECTING_CONTENT_BODY,
      COMPLETE;
   }
}
