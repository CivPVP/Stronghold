package me.neznamy.tab.libs.redis.clients.jedis.util;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Deprecated
public class MurmurHash implements Hashing {
   public static int hash(byte[] data, int seed) {
      return hash(ByteBuffer.wrap(data), seed);
   }

   public static int hash(byte[] data, int offset, int length, int seed) {
      return hash(ByteBuffer.wrap(data, offset, length), seed);
   }

   public static int hash(ByteBuffer buf, int seed) {
      ByteOrder byteOrder = buf.order();
      buf.order(ByteOrder.LITTLE_ENDIAN);
      int m = 1540483477;
      int r = 24;
      int h = seed ^ buf.remaining();

      while (buf.remaining() >= 4) {
         int k = buf.getInt();
         k *= m;
         k ^= k >>> r;
         k *= m;
         h *= m;
         h ^= k;
      }

      if (buf.remaining() > 0) {
         ByteBuffer finish = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
         ((Buffer)finish.put(buf)).rewind();
         h ^= finish.getInt();
         h *= m;
      }

      h ^= h >>> 13;
      h *= m;
      h ^= h >>> 15;
      buf.order(byteOrder);
      return h;
   }

   public static long hash64A(byte[] data, int seed) {
      return hash64A(ByteBuffer.wrap(data), seed);
   }

   public static long hash64A(byte[] data, int offset, int length, int seed) {
      return hash64A(ByteBuffer.wrap(data, offset, length), seed);
   }

   public static long hash64A(ByteBuffer buf, int seed) {
      ByteOrder byteOrder = buf.order();
      buf.order(ByteOrder.LITTLE_ENDIAN);
      long m = -4132994306676758123L;
      int r = 47;
      long h = seed ^ buf.remaining() * m;

      while (buf.remaining() >= 8) {
         long k = buf.getLong();
         k *= m;
         k ^= k >>> r;
         k *= m;
         h ^= k;
         h *= m;
      }

      if (buf.remaining() > 0) {
         ByteBuffer finish = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
         ((Buffer)finish.put(buf)).rewind();
         h ^= finish.getLong();
         h *= m;
      }

      h ^= h >>> r;
      h *= m;
      h ^= h >>> r;
      buf.order(byteOrder);
      return h;
   }

   @Override
   public long hash(byte[] key) {
      return hash64A(key, 305441741);
   }

   @Override
   public long hash(String key) {
      return this.hash(SafeEncoder.encode(key));
   }
}
