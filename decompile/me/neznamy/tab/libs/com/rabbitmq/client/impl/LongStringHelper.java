package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import me.neznamy.tab.libs.com.rabbitmq.client.LongString;

public class LongStringHelper {
   public static LongString asLongString(String string) {
      return string == null ? null : new LongStringHelper.ByteArrayLongString(string.getBytes(Charset.forName("utf-8")));
   }

   public static LongString asLongString(byte[] bytes) {
      return bytes == null ? null : new LongStringHelper.ByteArrayLongString(bytes);
   }

   private static class ByteArrayLongString implements LongString {
      private final byte[] bytes;

      public ByteArrayLongString(byte[] bytes) {
         this.bytes = bytes;
      }

      @Override
      public boolean equals(Object o) {
         if (o instanceof LongString) {
            LongString other = (LongString)o;
            return Arrays.equals(this.bytes, other.getBytes());
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Arrays.hashCode(this.bytes);
      }

      @Override
      public byte[] getBytes() {
         return this.bytes;
      }

      @Override
      public DataInputStream getStream() throws IOException {
         return new DataInputStream(new ByteArrayInputStream(this.bytes));
      }

      @Override
      public long length() {
         return this.bytes.length;
      }

      @Override
      public String toString() {
         return new String(this.bytes, Charset.forName("utf-8"));
      }
   }
}
