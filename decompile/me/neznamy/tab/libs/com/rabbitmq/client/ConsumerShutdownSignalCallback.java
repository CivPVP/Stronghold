package me.neznamy.tab.libs.com.rabbitmq.client;

@FunctionalInterface
public interface ConsumerShutdownSignalCallback {
   void handleShutdownSignal(String var1, ShutdownSignalException var2);
}
