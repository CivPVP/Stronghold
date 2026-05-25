package me.neznamy.tab.libs.com.rabbitmq.client;

public interface ExceptionHandler {
   void handleUnexpectedConnectionDriverException(Connection var1, Throwable var2);

   void handleReturnListenerException(Channel var1, Throwable var2);

   void handleConfirmListenerException(Channel var1, Throwable var2);

   void handleBlockedListenerException(Connection var1, Throwable var2);

   void handleConsumerException(Channel var1, Throwable var2, Consumer var3, String var4, String var5);

   void handleConnectionRecoveryException(Connection var1, Throwable var2);

   void handleChannelRecoveryException(Channel var1, Throwable var2);

   void handleTopologyRecoveryException(Connection var1, Channel var2, TopologyRecoveryException var3);
}
