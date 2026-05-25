package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import me.neznamy.tab.libs.com.rabbitmq.client.Command;

public class AMQCommand implements Command {
   public static final int EMPTY_FRAME_SIZE = 8;
   private final CommandAssembler assembler;
   private final Lock assemblerLock = new ReentrantLock();

   AMQCommand(int maxBodyLength) {
      this(null, null, null, maxBodyLength);
   }

   public AMQCommand() {
      this(null, null, null, Integer.MAX_VALUE);
   }

   public AMQCommand(me.neznamy.tab.libs.com.rabbitmq.client.Method method) {
      this(method, null, null, Integer.MAX_VALUE);
   }

   public AMQCommand(me.neznamy.tab.libs.com.rabbitmq.client.Method method, AMQContentHeader contentHeader, byte[] body) {
      this.assembler = new CommandAssembler((Method)method, contentHeader, body, Integer.MAX_VALUE);
   }

   public AMQCommand(me.neznamy.tab.libs.com.rabbitmq.client.Method method, AMQContentHeader contentHeader, byte[] body, int maxBodyLength) {
      this.assembler = new CommandAssembler((Method)method, contentHeader, body, maxBodyLength);
   }

   public Method getMethod() {
      return this.assembler.getMethod();
   }

   public AMQContentHeader getContentHeader() {
      return this.assembler.getContentHeader();
   }

   @Override
   public byte[] getContentBody() {
      return this.assembler.getContentBody();
   }

   public boolean handleFrame(Frame f) throws IOException {
      return this.assembler.handleFrame(f);
   }

   public void transmit(AMQChannel channel) throws IOException {
      int channelNumber = channel.getChannelNumber();
      AMQConnection connection = channel.getConnection();
      this.assemblerLock.lock();

      try {
         Method m = this.assembler.getMethod();
         if (m.hasContent()) {
            byte[] body = this.assembler.getContentBody();
            Frame headerFrame = this.assembler.getContentHeader().toFrame(channelNumber, body.length);
            int frameMax = connection.getFrameMax();
            boolean cappedFrameMax = frameMax > 0;
            int bodyPayloadMax = cappedFrameMax ? frameMax - 8 : body.length;
            if (cappedFrameMax && headerFrame.size() > frameMax) {
               String msg = String.format("Content headers exceeded max frame size: %d > %d", headerFrame.size(), frameMax);
               throw new IllegalArgumentException(msg);
            }

            connection.writeFrame(m.toFrame(channelNumber));
            connection.writeFrame(headerFrame);

            for (int offset = 0; offset < body.length; offset += bodyPayloadMax) {
               int remaining = body.length - offset;
               int fragmentLength = remaining < bodyPayloadMax ? remaining : bodyPayloadMax;
               Frame frame = Frame.fromBodyFragment(channelNumber, body, offset, fragmentLength);
               connection.writeFrame(frame);
            }
         } else {
            connection.writeFrame(m.toFrame(channelNumber));
         }
      } finally {
         this.assemblerLock.unlock();
      }

      connection.flush();
   }

   @Override
   public String toString() {
      return this.toString(false);
   }

   public String toString(boolean suppressBody) {
      this.assemblerLock.lock();

      try {
         return new StringBuilder()
            .append('{')
            .append(this.assembler.getMethod())
            .append(", ")
            .append(this.assembler.getContentHeader())
            .append(", ")
            .append(contentBodyStringBuilder(this.assembler.getContentBody(), suppressBody))
            .append('}')
            .toString();
      } finally {
         this.assemblerLock.unlock();
      }
   }

   private static StringBuilder contentBodyStringBuilder(byte[] body, boolean suppressBody) {
      try {
         return suppressBody
            ? new StringBuilder().append(body.length).append(" bytes of payload")
            : new StringBuilder().append('"').append(new String(body, "UTF-8")).append('"');
      } catch (Exception e) {
         return new StringBuilder().append('|').append(body.length).append('|');
      }
   }

   public static void checkPreconditions() {
      checkEmptyFrameSize();
   }

   private static void checkEmptyFrameSize() {
      Frame f = new Frame(3, 0, new byte[0]);
      ByteArrayOutputStream s = new ByteArrayOutputStream();

      try {
         f.writeTo(new DataOutputStream(s));
      } catch (IOException ioe) {
         throw new IllegalStateException("IOException while checking EMPTY_FRAME_SIZE");
      }

      int actualLength = s.toByteArray().length;
      if (8 != actualLength) {
         throw new IllegalStateException("Internal error: expected EMPTY_FRAME_SIZE(8) is not equal to computed value: " + actualLength);
      }
   }
}
