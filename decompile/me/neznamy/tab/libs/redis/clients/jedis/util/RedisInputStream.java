package me.neznamy.tab.libs.redis.clients.jedis.util;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Experimental;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisInputStream extends FilterInputStream {
   private static final int INPUT_BUFFER_SIZE = Integer.parseInt(System.getProperty("jedis.bufferSize.input", System.getProperty("jedis.bufferSize", "8192")));
   protected final byte[] buf;
   protected int count;
   protected int limit;

   public RedisInputStream(InputStream in, int size) {
      super(in);
      if (size <= 0) {
         throw new IllegalArgumentException("Buffer size <= 0");
      }

      this.buf = new byte[size];
   }

   public RedisInputStream(InputStream in) {
      this(in, INPUT_BUFFER_SIZE);
   }

   @Experimental
   public boolean peek(byte b) throws JedisConnectionException {
      this.ensureFill();
      return this.buf[this.count] == b;
   }

   public byte readByte() throws JedisConnectionException {
      this.ensureFill();
      return this.buf[this.count++];
   }

   private void ensureCrLf() {
      byte[] buf = this.buf;
      this.ensureFill();
      if (buf[this.count++] == 13) {
         this.ensureFill();
         if (buf[this.count++] == 10) {
            return;
         }
      }

      throw new JedisConnectionException("Unexpected character!");
   }

   public String readLine() {
      StringBuilder sb = new StringBuilder();

      while (true) {
         this.ensureFill();
         byte b = this.buf[this.count++];
         if (b == 13) {
            this.ensureFill();
            byte c = this.buf[this.count++];
            if (c == 10) {
               String reply = sb.toString();
               if (reply.isEmpty()) {
                  throw new JedisConnectionException("It seems like server has closed the connection.");
               }

               return reply;
            }

            sb.append((char)b);
            sb.append((char)c);
         } else {
            sb.append((char)b);
         }
      }
   }

   public byte[] readLineBytes() {
      this.ensureFill();
      int pos = this.count;
      byte[] buf = this.buf;

      while (pos != this.limit) {
         if (buf[pos++] == 13) {
            if (pos == this.limit) {
               return this.readLineBytesSlowly();
            }

            if (buf[pos++] == 10) {
               int N = pos - this.count - 2;
               byte[] line = new byte[N];
               System.arraycopy(buf, this.count, line, 0, N);
               this.count = pos;
               return line;
            }
         }
      }

      return this.readLineBytesSlowly();
   }

   private byte[] readLineBytesSlowly() {
      ByteArrayOutputStream bout = null;

      while (true) {
         this.ensureFill();
         byte b = this.buf[this.count++];
         if (b == 13) {
            this.ensureFill();
            byte c = this.buf[this.count++];
            if (c == 10) {
               return bout == null ? new byte[0] : bout.toByteArray();
            }

            if (bout == null) {
               bout = new ByteArrayOutputStream(16);
            }

            bout.write(b);
            bout.write(c);
         } else {
            if (bout == null) {
               bout = new ByteArrayOutputStream(16);
            }

            bout.write(b);
         }
      }
   }

   public Object readNullCrLf() {
      this.ensureCrLf();
      return null;
   }

   public boolean readBooleanCrLf() {
      byte[] buf = this.buf;
      this.ensureFill();
      byte b = buf[this.count++];
      this.ensureCrLf();
      switch (b) {
         case 102:
            return false;
         case 116:
            return true;
         default:
            throw new JedisConnectionException("Unexpected character!");
      }
   }

   public int readIntCrLf() {
      return (int)this.readLongCrLf();
   }

   public long readLongCrLf() {
      byte[] buf = this.buf;
      this.ensureFill();
      boolean isNeg = buf[this.count] == 45;
      if (isNeg) {
         this.count++;
      }

      long value = 0L;

      while (true) {
         this.ensureFill();
         int b = buf[this.count++];
         if (b == 13) {
            this.ensureFill();
            if (buf[this.count++] != 10) {
               throw new JedisConnectionException("Unexpected character!");
            } else {
               return isNeg ? -value : value;
            }
         }

         value = value * 10L + b - 48L;
      }
   }

   public double readDoubleCrLf() {
      return DoublePrecision.parseFloatingPointNumber(this.readLine());
   }

   public BigInteger readBigIntegerCrLf() {
      return new BigInteger(this.readLine());
   }

   @Override
   public int read(byte[] b, int off, int len) throws JedisConnectionException {
      this.ensureFill();
      int length = Math.min(this.limit - this.count, len);
      System.arraycopy(this.buf, this.count, b, off, length);
      this.count += length;
      return length;
   }

   private void ensureFill() throws JedisConnectionException {
      if (this.count >= this.limit) {
         try {
            this.limit = this.in.read(this.buf);
            this.count = 0;
            if (this.limit == -1) {
               throw new JedisConnectionException("Unexpected end of stream.");
            }
         } catch (IOException e) {
            throw new JedisConnectionException(e);
         }
      }
   }

   @Override
   public int available() throws IOException {
      int availableInBuf = this.limit - this.count;
      int availableInSocket = this.in.available();
      return availableInBuf > availableInSocket ? availableInBuf : availableInSocket;
   }
}
