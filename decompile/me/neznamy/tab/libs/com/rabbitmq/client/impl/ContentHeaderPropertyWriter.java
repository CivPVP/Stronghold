package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import me.neznamy.tab.libs.com.rabbitmq.client.LongString;

public class ContentHeaderPropertyWriter {
   private final ValueWriter out;
   private int flagWord;
   private int bitCount;

   public ContentHeaderPropertyWriter(DataOutputStream out) {
      this.out = new ValueWriter(out);
      this.flagWord = 0;
      this.bitCount = 0;
   }

   private void emitFlagWord(boolean continuationBit) throws IOException {
      this.out.writeShort(continuationBit ? this.flagWord | 1 : this.flagWord);
      this.flagWord = 0;
      this.bitCount = 0;
   }

   public void writePresence(boolean present) throws IOException {
      if (this.bitCount == 15) {
         this.emitFlagWord(true);
      }

      if (present) {
         int bit = 15 - this.bitCount;
         this.flagWord |= 1 << bit;
      }

      this.bitCount++;
   }

   public void finishPresence() throws IOException {
      this.emitFlagWord(false);
   }

   public void writeShortstr(String str) throws IOException {
      this.out.writeShortstr(str);
   }

   public void writeLongstr(String str) throws IOException {
      this.out.writeLongstr(str);
   }

   public void writeLongstr(LongString str) throws IOException {
      this.out.writeLongstr(str);
   }

   public void writeShort(Integer s) throws IOException {
      this.out.writeShort(s);
   }

   public void writeLong(Integer l) throws IOException {
      this.out.writeLong(l);
   }

   public void writeLonglong(Long ll) throws IOException {
      this.out.writeLonglong(ll);
   }

   public void writeTable(Map<String, Object> table) throws IOException {
      this.out.writeTable(table);
   }

   public void writeOctet(Integer octet) throws IOException {
      this.out.writeOctet(octet);
   }

   public void writeOctet(int octet) throws IOException {
      this.out.writeOctet(octet);
   }

   public void writeTimestamp(Date timestamp) throws IOException {
      this.out.writeTimestamp(timestamp);
   }
}
