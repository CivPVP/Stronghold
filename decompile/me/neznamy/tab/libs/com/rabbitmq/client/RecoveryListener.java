package me.neznamy.tab.libs.com.rabbitmq.client;

public interface RecoveryListener {
   void handleRecovery(Recoverable var1);

   void handleRecoveryStarted(Recoverable var1);

   default void handleTopologyRecoveryStarted(Recoverable recoverable) {
   }
}
