package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.neznamy.tab.libs.com.rabbitmq.client.LongString;

public class ValueWriter {
   private final DataOutputStream out;
   private static final int COPY_BUFFER_SIZE = 4096;

   public ValueWriter(DataOutputStream out) {
      this.out = out;
   }

   public final void writeShortstr(String str) throws IOException {
      byte[] bytes = str.getBytes("utf-8");
      int length = bytes.length;
      if (length > 255) {
         throw new IllegalArgumentException("Short string too long; utf-8 encoded length = " + length + ", max = 255.");
      }

      this.out.writeByte(bytes.length);
      this.out.write(bytes);
   }

   public final void writeLongstr(LongString str) throws IOException {
      this.writeLong((int)str.length());
      copy(str.getStream(), this.out);
   }

   private static void copy(InputStream input, OutputStream output) throws IOException {
      byte[] buffer = new byte[4096];

      for (int biteSize = input.read(buffer); -1 != biteSize; biteSize = input.read(buffer)) {
         output.write(buffer, 0, biteSize);
      }
   }

   public final void writeLongstr(String str) throws IOException {
      byte[] bytes = str.getBytes("utf-8");
      this.writeLong(bytes.length);
      this.out.write(bytes);
   }

   public final void writeShort(int s) throws IOException {
      this.out.writeShort(s);
   }

   public final void writeLong(int l) throws IOException {
      this.out.writeInt(l);
   }

   public final void writeLonglong(long ll) throws IOException {
      this.out.writeLong(ll);
   }

   public final void writeTable(Map<String, Object> table) throws IOException {
      if (table == null) {
         this.out.writeInt(0);
      } else {
         this.out.writeInt((int)Frame.tableSize(table));

         for (Entry<String, Object> entry : table.entrySet()) {
            this.writeShortstr(entry.getKey());
            Object value = entry.getValue();
            this.writeFieldValue(value);
         }
      }
   }

   public final void writeFieldValue(Object value) throws IOException {
      if (value instanceof String) {
         this.writeOctet(83);
         this.writeLongstr((String)value);
      } else if (value instanceof LongString) {
         this.writeOctet(83);
         this.writeLongstr((LongString)value);
      } else if (value instanceof Integer) {
         this.writeOctet(73);
         this.writeLong((Integer)value);
      } else if (value instanceof BigDecimal) {
         this.writeOctet(68);
         BigDecimal decimal = (BigDecimal)value;
         if (decimal.scale() > 255 || decimal.scale() < 0) {
            throw new IllegalArgumentException("BigDecimal has too large of a scale to be encoded. The scale was: " + decimal.scale());
         }

         this.writeOctet(decimal.scale());
         BigInteger unscaled = decimal.unscaledValue();
         if (unscaled.bitLength() > 31) {
            throw new IllegalArgumentException("BigDecimal too large to be encoded");
         }

         this.writeLong(decimal.unscaledValue().intValue());
      } else if (value instanceof Date) {
         this.writeOctet(84);
         this.writeTimestamp((Date)value);
      } else if (value instanceof Map) {
         this.writeOctet(70);
         Map<String, Object> map = (Map<String, Object>)value;
         this.writeTable(map);
      } else if (value instanceof Byte) {
         this.writeOctet(98);
         this.out.writeByte((Byte)value);
      } else if (value instanceof Double) {
         this.writeOctet(100);
         this.out.writeDouble((Double)value);
      } else if (value instanceof Float) {
         this.writeOctet(102);
         this.out.writeFloat((Float)value);
      } else if (value instanceof Long) {
         this.writeOctet(108);
         this.out.writeLong((Long)value);
      } else if (value instanceof Short) {
         this.writeOctet(115);
         this.out.writeShort((Short)value);
      } else if (value instanceof Boolean) {
         this.writeOctet(116);
         this.out.writeBoolean((Boolean)value);
      } else if (value instanceof byte[]) {
         this.writeOctet(120);
         this.writeLong(((byte[])value).length);
         this.out.write((byte[])value);
      } else if (value == null) {
         this.writeOctet(86);
      } else if (value instanceof List) {
         this.writeOctet(65);
         this.writeArray((List<?>)value);
      } else {
         if (!(value instanceof Object[])) {
            throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
         }

         this.writeOctet(65);
         this.writeArray((Object[])value);
      }
   }

   public final void writeArray(List<?> value) throws IOException {
      if (value == null) {
         this.out.write(0);
      } else {
         this.out.writeInt((int)Frame.arraySize(value));

         for (Object item : value) {
            this.writeFieldValue(item);
         }
      }
   }

   public final void writeArray(Object[] value) throws IOException {
      if (value == null) {
         this.out.write(0);
      } else {
         this.out.writeInt((int)Frame.arraySize(value));

         for (Object item : value) {
            this.writeFieldValue(item);
         }
      }
   }

   public final void writeOctet(int octet) throws IOException {
      this.out.writeByte(octet);
   }

   public final void writeOctet(byte octet) throws IOException {
      this.out.writeByte(octet);
   }

   public final void writeTimestamp(Date timestamp) throws IOException {
      this.writeLonglong(timestamp.getTime() / 1000L);
   }

   public void flush() throws IOException {
      this.out.flush();
   }
}
