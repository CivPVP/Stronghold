package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TruncatedInputStream extends FilterInputStream {
   private final long limit;
   private long counter = 0L;
   private long mark = 0L;

   public TruncatedInputStream(InputStream in, long limit) {
      super(in);
      this.limit = limit;
   }

   @Override
   public int available() throws IOException {
      return (int)Math.min(this.limit - this.counter, super.available());
   }

   @Override
   public synchronized void mark(int readlimit) {
      super.mark(readlimit);
      this.mark = this.counter;
   }

   @Override
   public int read() throws IOException {
      if (this.counter < this.limit) {
         int result = super.read();
         if (result >= 0) {
            this.counter++;
         }

         return result;
      } else {
         return -1;
      }
   }

   @Override
   public int read(byte[] b, int off, int len) throws IOException {
      if (this.limit > this.counter) {
         int result = super.read(b, off, (int)Math.min(len, this.limit - this.counter));
         if (result > 0) {
            this.counter += result;
         }

         return result;
      } else {
         return -1;
      }
   }

   @Override
   public synchronized void reset() throws IOException {
      super.reset();
      this.counter = this.mark;
   }

   @Override
   public long skip(long n) throws IOException {
      long result = super.skip(Math.min(n, this.limit - this.counter));
      this.counter += result;
      return result;
   }
}
