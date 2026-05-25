package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class BlockingQueueNioQueue implements NioQueue {
   private final BlockingQueue<WriteRequest> delegate;
   private final int writeEnqueuingTimeoutInMs;

   public BlockingQueueNioQueue(BlockingQueue<WriteRequest> delegate, int writeEnqueuingTimeoutInMs) {
      this.delegate = delegate;
      this.writeEnqueuingTimeoutInMs = writeEnqueuingTimeoutInMs;
   }

   @Override
   public boolean offer(WriteRequest writeRequest) throws InterruptedException {
      return this.delegate.offer(writeRequest, this.writeEnqueuingTimeoutInMs, TimeUnit.MILLISECONDS);
   }

   @Override
   public int size() {
      return this.delegate.size();
   }

   @Override
   public WriteRequest poll() {
      return this.delegate.poll();
   }

   @Override
   public boolean isEmpty() {
      return this.delegate.isEmpty();
   }
}
