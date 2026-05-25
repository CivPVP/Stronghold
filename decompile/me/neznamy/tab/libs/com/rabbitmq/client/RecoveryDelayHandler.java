package me.neznamy.tab.libs.com.rabbitmq.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface RecoveryDelayHandler {
   long getDelay(int var1);

   class DefaultRecoveryDelayHandler implements RecoveryDelayHandler {
      private final long networkRecoveryInterval;

      public DefaultRecoveryDelayHandler(long networkRecoveryInterval) {
         this.networkRecoveryInterval = networkRecoveryInterval;
      }

      @Override
      public long getDelay(int recoveryAttempts) {
         return this.networkRecoveryInterval;
      }
   }

   class ExponentialBackoffDelayHandler implements RecoveryDelayHandler {
      private final List<Long> sequence;

      public ExponentialBackoffDelayHandler() {
         this.sequence = Arrays.asList(2000L, 3000L, 5000L, 8000L, 13000L, 21000L, 34000L);
      }

      public ExponentialBackoffDelayHandler(List<Long> sequence) {
         if (sequence != null && !sequence.isEmpty()) {
            this.sequence = Collections.unmodifiableList(sequence);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public long getDelay(int recoveryAttempts) {
         int index = recoveryAttempts >= this.sequence.size() ? this.sequence.size() - 1 : recoveryAttempts;
         return this.sequence.get(index);
      }
   }
}
