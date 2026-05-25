package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.IOException;
import java.util.Objects;
import me.neznamy.tab.libs.com.rabbitmq.client.LongString;

abstract class UpdateSecretExtension {
   static class UpdateSecret extends Method {
      private final LongString newSecret;
      private final String reason;

      public UpdateSecret(LongString newSecret, String reason) {
         if (newSecret == null) {
            throw new IllegalStateException("Invalid configuration: 'newSecret' must be non-null.");
         }

         if (reason == null) {
            throw new IllegalStateException("Invalid configuration: 'reason' must be non-null.");
         }

         this.newSecret = newSecret;
         this.reason = reason;
      }

      public String getReason() {
         return this.reason;
      }

      @Override
      public int protocolClassId() {
         return 10;
      }

      @Override
      public int protocolMethodId() {
         return 70;
      }

      @Override
      public String protocolMethodName() {
         return "connection.update-secret";
      }

      @Override
      public boolean hasContent() {
         return false;
      }

      @Override
      public Object visit(AMQImpl.MethodVisitor visitor) throws IOException {
         return null;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            UpdateSecretExtension.UpdateSecret that = (UpdateSecretExtension.UpdateSecret)o;
            return !Objects.equals(this.newSecret, that.newSecret) ? false : Objects.equals(this.reason, that.reason);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = 0;
         result = 31 * result + (this.newSecret != null ? this.newSecret.hashCode() : 0);
         return 31 * result + (this.reason != null ? this.reason.hashCode() : 0);
      }

      @Override
      public void appendArgumentDebugStringTo(StringBuilder acc) {
         acc.append("(new-secret=").append(this.newSecret).append(", reason=").append(this.reason).append(")");
      }

      @Override
      public void writeArgumentsTo(MethodArgumentWriter writer) throws IOException {
         writer.writeLongstr(this.newSecret);
         writer.writeShortstr(this.reason);
      }
   }
}
