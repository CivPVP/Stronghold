package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

public interface NioQueue {
   boolean offer(WriteRequest var1) throws InterruptedException;

   int size();

   WriteRequest poll();

   boolean isEmpty();
}
