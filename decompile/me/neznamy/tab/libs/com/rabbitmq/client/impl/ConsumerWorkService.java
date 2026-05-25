package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import me.neznamy.tab.libs.com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConsumerWorkService {
   private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerWorkService.class);
   private static final int MAX_RUNNABLE_BLOCK_SIZE = 256;
   private static final int DEFAULT_NUM_THREADS = Math.max(1, Utils.availableProcessors());
   private final ExecutorService executor;
   private final boolean privateExecutor;
   private final WorkPool<Channel, Runnable> workPool;
   private final int shutdownTimeout;

   public ConsumerWorkService(ExecutorService executor, ThreadFactory threadFactory, int queueingTimeout, int shutdownTimeout) {
      this.privateExecutor = executor == null;
      if (executor == null) {
         LOGGER.debug("Creating executor service with {} thread(s) for consumer work service", DEFAULT_NUM_THREADS);
         this.executor = Executors.newFixedThreadPool(DEFAULT_NUM_THREADS, threadFactory);
      } else {
         this.executor = executor;
      }

      this.workPool = new WorkPool<>(queueingTimeout);
      this.shutdownTimeout = shutdownTimeout;
   }

   public ConsumerWorkService(ExecutorService executor, ThreadFactory threadFactory, int shutdownTimeout) {
      this(executor, threadFactory, -1, shutdownTimeout);
   }

   public int getShutdownTimeout() {
      return this.shutdownTimeout;
   }

   public void shutdown() {
      this.workPool.unregisterAllKeys();
      if (this.privateExecutor) {
         this.executor.shutdown();
      }
   }

   public void stopWork(Channel channel) {
      this.workPool.unregisterKey(channel);
   }

   public void registerKey(Channel channel) {
      this.workPool.registerKey(channel);
   }

   public void setUnlimited(Channel channel, boolean unlimited) {
      if (unlimited) {
         this.workPool.unlimit(channel);
      } else {
         this.workPool.limit(channel);
      }
   }

   public void addWork(Channel channel, Runnable runnable) {
      if (this.workPool.addWorkItem(channel, runnable)) {
         this.executor.execute(new ConsumerWorkService.WorkPoolRunnable());
      }
   }

   public boolean usesPrivateExecutor() {
      return this.privateExecutor;
   }

   private final class WorkPoolRunnable implements Runnable {
      private WorkPoolRunnable() {
      }

      @Override
      public void run() {
         int size = 256;
         List<Runnable> block = new ArrayList<>(size);

         try {
            Channel key = ConsumerWorkService.this.workPool.nextWorkBlock(block, size);
            if (key == null) {
               return;
            }

            try {
               for (Runnable runnable : block) {
                  runnable.run();
               }
            } finally {
               if (ConsumerWorkService.this.workPool.finishWorkBlock(key)) {
                  ConsumerWorkService.this.executor.execute(ConsumerWorkService.this.new WorkPoolRunnable());
               }
            }
         } catch (RuntimeException e) {
            Thread.currentThread().interrupt();
         }
      }
   }
}
