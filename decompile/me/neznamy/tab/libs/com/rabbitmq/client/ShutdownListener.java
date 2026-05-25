package me.neznamy.tab.libs.com.rabbitmq.client;

import java.util.EventListener;

@FunctionalInterface
public interface ShutdownListener extends EventListener {
   void shutdownCompleted(ShutdownSignalException var1);
}
