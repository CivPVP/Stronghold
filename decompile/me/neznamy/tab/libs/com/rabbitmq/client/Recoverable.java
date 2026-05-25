package me.neznamy.tab.libs.com.rabbitmq.client;

public interface Recoverable {
   void addRecoveryListener(RecoveryListener var1);

   void removeRecoveryListener(RecoveryListener var1);
}
