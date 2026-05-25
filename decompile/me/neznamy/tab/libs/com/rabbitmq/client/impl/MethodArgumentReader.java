package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import me.neznamy.tab.libs.com.rabbitmq.client.LongString;

public class MethodArgumentReader {
   private final ValueReader in;
   private int bits;
   private int nextBitMask;

   private void clearBits() {
      this.bits = 0;
      this.nextBitMask = 256;
   }

   public MethodArgumentReader(ValueReader in) {
      this.in = in;
      this.clearBits();
   }

   public final String readShortstr() throws IOException {
      this.clearBits();
      return this.in.readShortstr();
   }

   public final LongString readLongstr() throws IOException {
      this.clearBits();
      return this.in.readLongstr();
   }

   public final int readShort() throws IOException {
      this.clearBits();
      return this.in.readShort();
   }

   public final int readLong() throws IOException {
      this.clearBits();
      return this.in.readLong();
   }

   public final long readLonglong() throws IOException {
      this.clearBits();
      return this.in.readLonglong();
   }

   public final boolean readBit() throws IOException {
      if (this.nextBitMask > 128) {
         this.bits = this.in.readOctet();
         this.nextBitMask = 1;
      }

      boolean result = (this.bits & this.nextBitMask) != 0;
      this.nextBitMask <<= 1;
      return result;
   }

   public final Map<String, Object> readTable() throws IOException {
      this.clearBits();
      return this.in.readTable();
   }

   public final int readOctet() throws IOException {
      this.clearBits();
      return this.in.readOctet();
   }

   public final Date readTimestamp() throws IOException {
      this.clearBits();
      return this.in.readTimestamp();
   }
}
