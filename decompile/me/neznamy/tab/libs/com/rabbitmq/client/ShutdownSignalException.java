package me.neznamy.tab.libs.com.rabbitmq.client;

import me.neznamy.tab.libs.com.rabbitmq.utility.SensibleClone;

public class ShutdownSignalException extends RuntimeException implements SensibleClone<ShutdownSignalException> {
   private static final long serialVersionUID = 1L;
   private final boolean _hardError;
   private final boolean _initiatedByApplication;
   private final Method _reason;
   private final Object _ref;

   public ShutdownSignalException(boolean hardError, boolean initiatedByApplication, Method reason, Object ref) {
      this(hardError, initiatedByApplication, reason, ref, "", null);
   }

   public ShutdownSignalException(boolean hardError, boolean initiatedByApplication, Method reason, Object ref, String messagePrefix, Throwable cause) {
      super(composeMessage(hardError, initiatedByApplication, reason, messagePrefix, cause));
      this._hardError = hardError;
      this._initiatedByApplication = initiatedByApplication;
      this._reason = reason;
      this._ref = ref;
   }

   private static String composeMessage(boolean hardError, boolean initiatedByApplication, Method reason, String messagePrefix, Throwable cause) {
      String connectionOrChannel = hardError ? "connection" : "channel";
      String appInitiated = "clean " + connectionOrChannel + " shutdown";
      String nonAppInitiated = connectionOrChannel + " error";
      String explanation = initiatedByApplication ? appInitiated : nonAppInitiated;
      StringBuilder result = new StringBuilder(messagePrefix).append(explanation);
      if (reason != null) {
         result.append("; protocol method: ").append(reason);
      }

      if (cause != null) {
         result.append("; cause: ").append(cause);
      }

      return result.toString();
   }

   public boolean isHardError() {
      return this._hardError;
   }

   public boolean isInitiatedByApplication() {
      return this._initiatedByApplication;
   }

   public Method getReason() {
      return this._reason;
   }

   public Object getReference() {
      return this._ref;
   }

   public ShutdownSignalException sensibleClone() {
      try {
         return (ShutdownSignalException)super.clone();
      } catch (CloneNotSupportedException e) {
         throw new RuntimeException(e);
      }
   }
}
