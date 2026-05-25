package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import me.neznamy.tab.libs.com.rabbitmq.client.LongString;

public class MethodArgumentWriter {
   private final ValueWriter out;
   private boolean needBitFlush;
   private byte bitAccumulator;
   private int bitMask;

   public MethodArgumentWriter(ValueWriter out) {
      this.out = out;
      this.resetBitAccumulator();
   }

   private void resetBitAccumulator() {
      this.needBitFlush = false;
      this.bitAccumulator = 0;
      this.bitMask = 1;
   }

   private final void bitflush() throws IOException {
      if (this.needBitFlush) {
         this.out.writeOctet(this.bitAccumulator);
         this.resetBitAccumulator();
      }
   }

   public final void writeShortstr(String str) throws IOException {
      this.bitflush();
      this.out.writeShortstr(str);
   }

   public final void writeLongstr(LongString str) throws IOException {
      this.bitflush();
      this.out.writeLongstr(str);
   }

   public final void writeLongstr(String str) throws IOException {
      this.bitflush();
      this.out.writeLongstr(str);
   }

   public final void writeShort(int s) throws IOException {
      this.bitflush();
      this.out.writeShort(s);
   }

   public final void writeLong(int l) throws IOException {
      this.bitflush();
      this.out.writeLong(l);
   }

   public final void writeLonglong(long ll) throws IOException {
      this.bitflush();
      this.out.writeLonglong(ll);
   }

   public final void writeBit(boolean b) throws IOException {
      if (this.bitMask > 128) {
         this.bitflush();
      }

      if (b) {
         this.bitAccumulator = (byte)(this.bitAccumulator | this.bitMask);
      }

      this.bitMask <<= 1;
      this.needBitFlush = true;
   }

   public final void writeTable(Map<String, Object> table) throws IOException {
      this.bitflush();
      this.out.writeTable(table);
   }

   public final void writeOctet(int octet) throws IOException {
      this.bitflush();
      this.out.writeOctet(octet);
   }

   public final void writeOctet(byte octet) throws IOException {
      this.bitflush();
      this.out.writeOctet(octet);
   }

   public final void writeTimestamp(Date timestamp) throws IOException {
      this.bitflush();
      this.out.writeTimestamp(timestamp);
   }

   public void flush() throws IOException {
      this.bitflush();
      this.out.flush();
   }
}
