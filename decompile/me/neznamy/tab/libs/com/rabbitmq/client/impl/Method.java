package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Method implements me.neznamy.tab.libs.com.rabbitmq.client.Method {
   @Override
   public abstract int protocolClassId();

   @Override
   public abstract int protocolMethodId();

   @Override
   public abstract String protocolMethodName();

   public abstract boolean hasContent();

   public abstract Object visit(AMQImpl.MethodVisitor var1) throws IOException;

   public abstract void writeArgumentsTo(MethodArgumentWriter var1) throws IOException;

   public void appendArgumentDebugStringTo(StringBuilder buffer) {
      buffer.append("(?)");
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("#method<").append(this.protocolMethodName()).append(">");
      this.appendArgumentDebugStringTo(sb);
      return sb.toString();
   }

   public Frame toFrame(int channelNumber) throws IOException {
      Frame frame = new Frame(1, channelNumber);
      DataOutputStream bodyOut = frame.getOutputStream();
      bodyOut.writeShort(this.protocolClassId());
      bodyOut.writeShort(this.protocolMethodId());
      MethodArgumentWriter argWriter = new MethodArgumentWriter(new ValueWriter(bodyOut));
      this.writeArgumentsTo(argWriter);
      argWriter.flush();
      return frame;
   }
}
