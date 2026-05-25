package me.neznamy.tab.libs.redis.clients.jedis.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class RedisOutputStream extends FilterOutputStream {
   private static final int OUTPUT_BUFFER_SIZE = Integer.parseInt(System.getProperty("jedis.bufferSize.output", System.getProperty("jedis.bufferSize", "8192")));
   protected final byte[] buf;
   protected int count;
   private static final int[] sizeTable = new int[]{9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};
   private static final byte[] DigitTens = new byte[]{
      48,
      48,
      48,
      48,
      48,
      48,
      48,
      48,
      48,
      48,
      49,
      49,
      49,
      49,
      49,
      49,
      49,
      49,
      49,
      49,
      50,
      50,
      50,
      50,
      50,
      50,
      50,
      50,
      50,
      50,
      51,
      51,
      51,
      51,
      51,
      51,
      51,
      51,
      51,
      51,
      52,
      52,
      52,
      52,
      52,
      52,
      52,
      52,
      52,
      52,
      53,
      53,
      53,
      53,
      53,
      53,
      53,
      53,
      53,
      53,
      54,
      54,
      54,
      54,
      54,
      54,
      54,
      54,
      54,
      54,
      55,
      55,
      55,
      55,
      55,
      55,
      55,
      55,
      55,
      55,
      56,
      56,
      56,
      56,
      56,
      56,
      56,
      56,
      56,
      56,
      57,
      57,
      57,
      57,
      57,
      57,
      57,
      57,
      57,
      57
   };
   private static final byte[] DigitOnes = new byte[]{
      48,
      49,
      50,
      51,
      52,
      53,
      54,
      55,
      56,
      57,
      48,
      49,
      50,
      51,
      52,
      53,
      54,
      55,
      56,
      57,
      48,
      49,
      50,
      51,
      52,
      53,
      54,
      55,
      56,
      57,
      48,
      49,
      50,
      51,
      52,
      53,
      54,
      55,
      56,
      57,
      48,
      49,
      50,
      51,
      52,
      53,
      54,
      55,
      56,
      57,
      48,
      49,
      50,
      51,
      52,
      53,
      54,
      55,
      56,
      57,
      48,
      49,
      50,
      51,
      52,
      53,
      54,
      55,
      56,
      57,
      48,
      49,
      50,
      51,
      52,
      53,
      54,
      55,
      56,
      57,
      48,
      49,
      50,
      51,
      52,
      53,
      54,
      55,
      56,
      57,
      48,
      49,
      50,
      51,
      52,
      53,
      54,
      55,
      56,
      57
   };
   private static final byte[] digits = new byte[]{
      48,
      49,
      50,
      51,
      52,
      53,
      54,
      55,
      56,
      57,
      97,
      98,
      99,
      100,
      101,
      102,
      103,
      104,
      105,
      106,
      107,
      108,
      109,
      110,
      111,
      112,
      113,
      114,
      115,
      116,
      117,
      118,
      119,
      120,
      121,
      122
   };

   public RedisOutputStream(OutputStream out) {
      this(out, OUTPUT_BUFFER_SIZE);
   }

   public RedisOutputStream(OutputStream out, int size) {
      super(out);
      if (size <= 0) {
         throw new IllegalArgumentException("Buffer size <= 0");
      }

      this.buf = new byte[size];
   }

   private void flushBuffer() throws IOException {
      if (this.count > 0) {
         this.out.write(this.buf, 0, this.count);
         this.count = 0;
      }
   }

   public void write(byte b) throws IOException {
      if (this.count == this.buf.length) {
         this.flushBuffer();
      }

      this.buf[this.count++] = b;
   }

   @Override
   public void write(byte[] b) throws IOException {
      this.write(b, 0, b.length);
   }

   @Override
   public void write(byte[] b, int off, int len) throws IOException {
      if (len >= this.buf.length) {
         this.flushBuffer();
         this.out.write(b, off, len);
      } else {
         if (len >= this.buf.length - this.count) {
            this.flushBuffer();
         }

         System.arraycopy(b, off, this.buf, this.count, len);
         this.count += len;
      }
   }

   public void writeCrLf() throws IOException {
      if (2 >= this.buf.length - this.count) {
         this.flushBuffer();
      }

      this.buf[this.count++] = 13;
      this.buf[this.count++] = 10;
   }

   public void writeIntCrLf(int value) throws IOException {
      if (value < 0) {
         this.write((byte)45);
         value = -value;
      }

      int size = 0;

      while (value > sizeTable[size]) {
         size++;
      }

      if (++size >= this.buf.length - this.count) {
         this.flushBuffer();
      }

      int charPos = this.count + size;

      while (value >= 65536) {
         int q = value / 100;
         int r = value - ((q << 6) + (q << 5) + (q << 2));
         value = q;
         this.buf[--charPos] = DigitOnes[r];
         this.buf[--charPos] = DigitTens[r];
      }

      do {
         int q = value * 52429 >>> 19;
         int r = value - ((q << 3) + (q << 1));
         this.buf[--charPos] = digits[r];
         value = q;
      } while (value != 0);

      this.count += size;
      this.writeCrLf();
   }

   @Override
   public void flush() throws IOException {
      this.flushBuffer();
      this.out.flush();
   }
}
