package me.neznamy.tab.libs.com.rabbitmq.client;

import me.neznamy.tab.libs.com.rabbitmq.client.impl.Frame;

public class UnexpectedFrameError extends RuntimeException {
   private static final long serialVersionUID = 1L;
   private final Frame _frame;
   private final int _expectedFrameType;

   public UnexpectedFrameError(Frame frame, int expectedFrameType) {
      super("Received frame: " + frame + ", expected type " + expectedFrameType);
      this._frame = frame;
      this._expectedFrameType = expectedFrameType;
   }

   public static long getSerialVersionUID() {
      return 1L;
   }

   public Frame getReceivedFrame() {
      return this._frame;
   }

   public int getExpectedFrameType() {
      return this._expectedFrameType;
   }
}
