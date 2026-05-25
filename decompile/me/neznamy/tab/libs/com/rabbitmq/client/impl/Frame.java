package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.neznamy.tab.libs.com.rabbitmq.client.LongString;
import me.neznamy.tab.libs.com.rabbitmq.client.MalformedFrameException;

public class Frame {
   public final int type;
   public final int channel;
   private final byte[] payload;
   private final ByteArrayOutputStream accumulator;
   private static final int NON_BODY_SIZE = 8;

   public Frame(int type, int channel) {
      this.type = type;
      this.channel = channel;
      this.payload = null;
      this.accumulator = new ByteArrayOutputStream();
   }

   public Frame(int type, int channel, byte[] payload) {
      this.type = type;
      this.channel = channel;
      this.payload = payload;
      this.accumulator = null;
   }

   public static Frame fromBodyFragment(int channelNumber, byte[] body, int offset, int length) throws IOException {
      Frame frame = new Frame(3, channelNumber);
      DataOutputStream bodyOut = frame.getOutputStream();
      bodyOut.write(body, offset, length);
      return frame;
   }

   public static Frame readFrom(DataInputStream is, int maxPayloadSize) throws IOException {
      int type;
      try {
         type = is.readUnsignedByte();
      } catch (SocketTimeoutException ste) {
         return null;
      }

      if (type == 65) {
         protocolVersionMismatch(is);
      }

      int channel = is.readUnsignedShort();
      int payloadSize = is.readInt();
      if (payloadSize >= maxPayloadSize) {
         throw new IllegalStateException(
            String.format(
               "Frame body is too large (%d), maximum configured size is %d. See ConnectionFactory#setMaxInboundMessageBodySize if you need to increase the limit.",
               payloadSize,
               maxPayloadSize
            )
         );
      } else {
         byte[] payload = new byte[payloadSize];
         is.readFully(payload);
         int frameEndMarker = is.readUnsignedByte();
         if (frameEndMarker != 206) {
            throw new MalformedFrameException("Bad frame end marker: " + frameEndMarker);
         } else {
            return new Frame(type, channel, payload);
         }
      }
   }

   public static void protocolVersionMismatch(DataInputStream is) throws IOException {
      byte[] expectedBytes = new byte[]{77, 81, 80};

      for (byte expectedByte : expectedBytes) {
         int nextByte = is.readUnsignedByte();
         if (nextByte != expectedByte) {
            throw new MalformedFrameException("Invalid AMQP protocol header from server: expected character " + expectedByte + ", got " + nextByte);
         }
      }

      MalformedFrameException x;
      try {
         int[] signature = new int[4];

         for (int i = 0; i < 4; i++) {
            signature[i] = is.readUnsignedByte();
         }

         if (signature[0] == 1 && signature[1] == 1 && signature[2] == 8 && signature[3] == 0) {
            x = new MalformedFrameException("AMQP protocol version mismatch; we are version 0-9-1, server is 0-8");
         } else {
            String sig = "";

            for (int i = 0; i < 4; i++) {
               if (i != 0) {
                  sig = sig + ",";
               }

               sig = sig + signature[i];
            }

            x = new MalformedFrameException("AMQP protocol version mismatch; we are version 0-9-1, server sent signature " + sig);
         }
      } catch (IOException ex) {
         x = new MalformedFrameException("Invalid AMQP protocol header from server");
      }

      throw x;
   }

   public void writeTo(DataOutputStream os) throws IOException {
      os.writeByte(this.type);
      os.writeShort(this.channel);
      if (this.accumulator != null) {
         os.writeInt(this.accumulator.size());
         this.accumulator.writeTo(os);
      } else {
         os.writeInt(this.payload.length);
         os.write(this.payload);
      }

      os.write(206);
   }

   public int size() {
      return this.accumulator != null ? this.accumulator.size() + 8 : this.payload.length + 8;
   }

   public byte[] getPayload() {
      return this.payload != null ? this.payload : this.accumulator.toByteArray();
   }

   public DataInputStream getInputStream() {
      return new DataInputStream(new ByteArrayInputStream(this.getPayload()));
   }

   public DataOutputStream getOutputStream() {
      return new DataOutputStream(this.accumulator);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Frame(type=").append(this.type).append(", channel=").append(this.channel).append(", ");
      if (this.accumulator == null) {
         sb.append(this.payload.length).append(" bytes of payload)");
      } else {
         sb.append(this.accumulator.size()).append(" bytes of accumulator)");
      }

      return sb.toString();
   }

   public static long tableSize(Map<String, Object> table) throws UnsupportedEncodingException {
      long acc = 0L;

      for (Entry<String, Object> entry : table.entrySet()) {
         acc += shortStrSize(entry.getKey());
         acc += fieldValueSize(entry.getValue());
      }

      return acc;
   }

   private static long fieldValueSize(Object value) throws UnsupportedEncodingException {
      long acc = 1L;
      if (value instanceof String) {
         acc += longStrSize((String)value);
      } else if (value instanceof LongString) {
         acc += 4L + ((LongString)value).length();
      } else if (value instanceof Integer) {
         acc += 4L;
      } else if (value instanceof BigDecimal) {
         acc += 5L;
      } else if (value instanceof Date) {
         acc += 8L;
      } else if (value instanceof Map) {
         Map<String, Object> map = (Map<String, Object>)value;
         acc += 4L + tableSize(map);
      } else if (value instanceof Byte) {
         acc++;
      } else if (value instanceof Double) {
         acc += 8L;
      } else if (value instanceof Float) {
         acc += 4L;
      } else if (value instanceof Long) {
         acc += 8L;
      } else if (value instanceof Short) {
         acc += 2L;
      } else if (value instanceof Boolean) {
         acc++;
      } else if (value instanceof byte[]) {
         acc += 4 + ((byte[])value).length;
      } else if (value instanceof List) {
         acc += 4L + arraySize((List<?>)value);
      } else if (value instanceof Object[]) {
         acc += 4L + arraySize((Object[])value);
      } else if (value != null) {
         throw new IllegalArgumentException("invalid value in table");
      }

      return acc;
   }

   public static long arraySize(List<?> values) throws UnsupportedEncodingException {
      long acc = 0L;

      for (Object value : values) {
         acc += fieldValueSize(value);
      }

      return acc;
   }

   public static long arraySize(Object[] values) throws UnsupportedEncodingException {
      long acc = 0L;

      for (Object value : values) {
         acc += fieldValueSize(value);
      }

      return acc;
   }

   private static int longStrSize(String str) throws UnsupportedEncodingException {
      return str.getBytes("utf-8").length + 4;
   }

   private static int shortStrSize(String str) throws UnsupportedEncodingException {
      return str.getBytes("utf-8").length + 1;
   }
}
