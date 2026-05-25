package me.neznamy.tab.libs.com.rabbitmq.client;

public class AlreadyClosedException extends ShutdownSignalException {
   private static final long serialVersionUID = 1L;

   public AlreadyClosedException(ShutdownSignalException sse) {
      this(sse, null);
   }

   public AlreadyClosedException(ShutdownSignalException sse, Throwable cause) {
      super(
         sse.isHardError(),
         sse.isInitiatedByApplication(),
         sse.getReason(),
         sse.getReference(),
         composeMessagePrefix(sse),
         cause == null ? sse.getCause() : cause
      );
   }

   private static String composeMessagePrefix(ShutdownSignalException sse) {
      String connectionOrChannel = sse.isHardError() ? "connection " : "channel ";
      return connectionOrChannel + "is already closed due to ";
   }
}
