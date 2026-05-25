package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

@FunctionalInterface
public interface BackoffPolicy {
   void backoff(int var1) throws InterruptedException;
}
