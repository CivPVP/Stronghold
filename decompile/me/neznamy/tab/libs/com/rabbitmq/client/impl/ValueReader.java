package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.neznamy.tab.libs.com.rabbitmq.client.LongString;
import me.neznamy.tab.libs.com.rabbitmq.client.MalformedFrameException;

public class ValueReader {
   private static final long INT_MASK = 4294967295L;
   private final DataInputStream in;

   private static long unsignedExtend(int value) {
      long extended = value;
      return extended & 4294967295L;
   }

   public ValueReader(DataInputStream in) {
      this.in = in;
   }

   private static String readShortstr(DataInputStream in) throws IOException {
      byte[] b = new byte[in.readUnsignedByte()];
      in.readFully(b);
      return new String(b, "utf-8");
   }

   public final String readShortstr() throws IOException {
      return readShortstr(this.in);
   }

   private static byte[] readBytes(DataInputStream in) throws IOException {
      long contentLength = unsignedExtend(in.readInt());
      if (contentLength < 2147483647L) {
         byte[] buffer = new byte[(int)contentLength];
         in.readFully(buffer);
         return buffer;
      } else {
         throw new UnsupportedOperationException("Very long byte vectors and strings not currently supported");
      }
   }

   private static LongString readLongstr(DataInputStream in) throws IOException {
      return LongStringHelper.asLongString(readBytes(in));
   }

   public final LongString readLongstr() throws IOException {
      return readLongstr(this.in);
   }

   public final int readShort() throws IOException {
      return this.in.readUnsignedShort();
   }

   public final int readLong() throws IOException {
      return this.in.readInt();
   }

   public final long readLonglong() throws IOException {
      return this.in.readLong();
   }

   private static Map<String, Object> readTable(DataInputStream in) throws IOException {
      long tableLength = unsignedExtend(in.readInt());
      if (tableLength == 0L) {
         return Collections.emptyMap();
      }

      Map<String, Object> table = new HashMap<>();
      DataInputStream tableIn = new DataInputStream(new TruncatedInputStream(in, tableLength));

      while (tableIn.available() > 0) {
         String name = readShortstr(tableIn);
         Object value = readFieldValue(tableIn);
         if (!table.containsKey(name)) {
            table.put(name, value);
         }
      }

      return table;
   }

   static Object readFieldValue(DataInputStream in) throws IOException {
      Object value = null;
      switch (in.readUnsignedByte()) {
         case 65:
            value = readArray(in);
            break;
         case 66:
            value = in.readUnsignedByte();
            break;
         case 67:
         case 69:
         case 71:
         case 72:
         case 74:
         case 75:
         case 76:
         case 77:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         case 85:
         case 87:
         case 88:
         case 89:
         case 90:
         case 91:
         case 92:
         case 93:
         case 94:
         case 95:
         case 96:
         case 97:
         case 99:
         case 101:
         case 103:
         case 104:
         case 106:
         case 107:
         case 109:
         case 110:
         case 111:
         case 112:
         case 113:
         case 114:
         case 118:
         case 119:
         default:
            throw new MalformedFrameException("Unrecognised type in table");
         case 68:
            int scale = in.readUnsignedByte();
            byte[] unscaled = new byte[4];
            in.readFully(unscaled);
            value = new BigDecimal(new BigInteger(unscaled), scale);
            break;
         case 70:
            value = readTable(in);
            break;
         case 73:
            value = in.readInt();
            break;
         case 83:
            value = readLongstr(in);
            break;
         case 84:
            value = readTimestamp(in);
            break;
         case 86:
            value = null;
            break;
         case 98:
            value = in.readByte();
            break;
         case 100:
            value = in.readDouble();
            break;
         case 102:
            value = in.readFloat();
            break;
         case 105:
            value = readUnsignedInt(in);
            break;
         case 108:
            value = in.readLong();
            break;
         case 115:
            value = in.readShort();
            break;
         case 116:
            value = in.readBoolean();
            break;
         case 117:
            value = in.readUnsignedShort();
            break;
         case 120:
            value = readBytes(in);
      }

      return value;
   }

   private static long readUnsignedInt(DataInputStream in) throws IOException {
      long ch1 = in.read();
      long ch2 = in.read();
      long ch3 = in.read();
      long ch4 = in.read();
      if ((ch1 | ch2 | ch3 | ch4) < 0L) {
         throw new EOFException();
      } else {
         return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4;
      }
   }

   private static List<Object> readArray(DataInputStream in) throws IOException {
      long length = unsignedExtend(in.readInt());
      DataInputStream arrayIn = new DataInputStream(new TruncatedInputStream(in, length));
      List<Object> array = new ArrayList<>();

      while (arrayIn.available() > 0) {
         Object value = readFieldValue(arrayIn);
         array.add(value);
      }

      return array;
   }

   public final Map<String, Object> readTable() throws IOException {
      return readTable(this.in);
   }

   public final int readOctet() throws IOException {
      return this.in.readUnsignedByte();
   }

   private static Date readTimestamp(DataInputStream in) throws IOException {
      return new Date(in.readLong() * 1000L);
   }

   public final Date readTimestamp() throws IOException {
      return readTimestamp(this.in);
   }
}
