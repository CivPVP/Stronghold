package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import me.neznamy.tab.libs.com.rabbitmq.client.LongString;

public class ContentHeaderPropertyReader {
   private final ValueReader in;
   private int flagWord;
   private int bitCount;

   public ContentHeaderPropertyReader(DataInputStream in) throws IOException {
      this.in = new ValueReader(in);
      this.flagWord = 1;
      this.bitCount = 15;
   }

   private boolean isContinuationBitSet() {
      return (this.flagWord & 1) != 0;
   }

   public void readFlagWord() throws IOException {
      if (!this.isContinuationBitSet()) {
         throw new IOException("Attempted to read flag word when none advertised");
      }

      this.flagWord = this.in.readShort();
      this.bitCount = 0;
   }

   public boolean readPresence() throws IOException {
      if (this.bitCount == 15) {
         this.readFlagWord();
      }

      int bit = 15 - this.bitCount;
      this.bitCount++;
      return (this.flagWord & 1 << bit) != 0;
   }

   public void finishPresence() throws IOException {
      if (this.isContinuationBitSet()) {
         throw new IOException("Unexpected continuation flag word");
      }
   }

   public String readShortstr() throws IOException {
      return this.in.readShortstr();
   }

   public LongString readLongstr() throws IOException {
      return this.in.readLongstr();
   }

   public Integer readShort() throws IOException {
      return this.in.readShort();
   }

   public Integer readLong() throws IOException {
      return this.in.readLong();
   }

   public Long readLonglong() throws IOException {
      return this.in.readLonglong();
   }

   public Map<String, Object> readTable() throws IOException {
      return this.in.readTable();
   }

   public int readOctet() throws IOException {
      return this.in.readOctet();
   }

   public Date readTimestamp() throws IOException {
      return this.in.readTimestamp();
   }
}
