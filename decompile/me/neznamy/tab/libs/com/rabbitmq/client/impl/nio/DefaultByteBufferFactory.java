package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import java.nio.ByteBuffer;
import java.util.function.Function;

public class DefaultByteBufferFactory implements ByteBufferFactory {
   private final Function<Integer, ByteBuffer> allocator;

   public DefaultByteBufferFactory(Function<Integer, ByteBuffer> allocator) {
      this.allocator = allocator;
   }

   public DefaultByteBufferFactory() {
      this(capacity -> ByteBuffer.allocate(capacity));
   }

   @Override
   public ByteBuffer createReadBuffer(NioContext nioContext) {
      return nioContext.getSslEngine() == null
         ? this.allocator.apply(nioContext.getNioParams().getReadByteBufferSize())
         : this.allocator.apply(nioContext.getSslEngine().getSession().getApplicationBufferSize());
   }

   @Override
   public ByteBuffer createWriteBuffer(NioContext nioContext) {
      return nioContext.getSslEngine() == null
         ? this.allocator.apply(nioContext.getNioParams().getWriteByteBufferSize())
         : this.allocator.apply(nioContext.getSslEngine().getSession().getApplicationBufferSize());
   }

   @Override
   public ByteBuffer createEncryptedReadBuffer(NioContext nioContext) {
      return this.createEncryptedByteBuffer(nioContext);
   }

   @Override
   public ByteBuffer createEncryptedWriteBuffer(NioContext nioContext) {
      return this.createEncryptedByteBuffer(nioContext);
   }

   protected ByteBuffer createEncryptedByteBuffer(NioContext nioContext) {
      if (nioContext.getSslEngine() == null) {
         throw new IllegalArgumentException("Encrypted byte buffer should be created only in SSL/TLS context");
      } else {
         return this.allocator.apply(nioContext.getSslEngine().getSession().getPacketBufferSize());
      }
   }
}
