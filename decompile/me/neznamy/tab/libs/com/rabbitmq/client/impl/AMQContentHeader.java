package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import me.neznamy.tab.libs.com.rabbitmq.client.ContentHeader;

public abstract class AMQContentHeader implements ContentHeader {
   private long bodySize;

   protected AMQContentHeader() {
      this.bodySize = 0L;
   }

   protected AMQContentHeader(DataInputStream in) throws IOException {
      in.readShort();
      this.bodySize = in.readLong();
   }

   public long getBodySize() {
      return this.bodySize;
   }

   private void writeTo(DataOutputStream out, long bodySize) throws IOException {
      out.writeShort(0);
      out.writeLong(bodySize);
      this.writePropertiesTo(new ContentHeaderPropertyWriter(out));
   }

   public abstract void writePropertiesTo(ContentHeaderPropertyWriter var1) throws IOException;

   @Override
   public void appendPropertyDebugStringTo(StringBuilder acc) {
      acc.append("(?)");
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("#contentHeader<").append(this.getClassName()).append(">");
      this.appendPropertyDebugStringTo(sb);
      return sb.toString();
   }

   public Frame toFrame(int channelNumber, long bodySize) throws IOException {
      Frame frame = new Frame(2, channelNumber);
      DataOutputStream bodyOut = frame.getOutputStream();
      bodyOut.writeShort(this.getClassId());
      this.writeTo(bodyOut, bodySize);
      return frame;
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }
}
