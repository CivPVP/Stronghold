package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

final class HeartbeatSender {
   private final Object monitor = new Object();
   private final FrameHandler frameHandler;
   private final ThreadFactory threadFactory;
   private ScheduledExecutorService executor;
   private final boolean privateExecutor;
   private ScheduledFuture<?> future;
   private boolean shutdown = false;
   private volatile long lastActivityTime;

   HeartbeatSender(FrameHandler frameHandler, ScheduledExecutorService heartbeatExecutor, ThreadFactory threadFactory) {
      this.frameHandler = frameHandler;
      this.privateExecutor = heartbeatExecutor == null;
      this.executor = heartbeatExecutor;
      this.threadFactory = threadFactory;
   }

   public void signalActivity() {
      this.lastActivityTime = System.nanoTime();
   }

   public void setHeartbeat(int heartbeatSeconds) {
      synchronized (this.monitor) {
         if (!this.shutdown) {
            if (this.future != null) {
               this.future.cancel(true);
               this.future = null;
            }

            if (heartbeatSeconds > 0) {
               long interval = TimeUnit.SECONDS.toNanos(heartbeatSeconds) / 2L;
               ScheduledExecutorService executor = this.createExecutorIfNecessary();
               Runnable task = new HeartbeatSender.HeartbeatRunnable(interval);
               this.future = executor.scheduleAtFixedRate(task, interval, interval, TimeUnit.NANOSECONDS);
            }
         }
      }
   }

   private ScheduledExecutorService createExecutorIfNecessary() {
      synchronized (this.monitor) {
         if (this.executor == null) {
            this.executor = Executors.newSingleThreadScheduledExecutor(this.threadFactory);
         }

         return this.executor;
      }
   }

   public void shutdown() {
      ExecutorService executorToShutdown = null;
      synchronized (this.monitor) {
         if (this.future != null) {
            this.future.cancel(true);
            this.future = null;
         }

         if (this.privateExecutor) {
            executorToShutdown = this.executor;
         }

         this.executor = null;
         this.shutdown = true;
      }

      if (executorToShutdown != null) {
         executorToShutdown.shutdown();
      }
   }

   private final class HeartbeatRunnable implements Runnable {
      private final long heartbeatNanos;

      private HeartbeatRunnable(long heartbeatNanos) {
         this.heartbeatNanos = heartbeatNanos;
      }

      @Override
      public void run() {
         try {
            long now = System.nanoTime();
            if (now > HeartbeatSender.this.lastActivityTime + this.heartbeatNanos) {
               HeartbeatSender.this.frameHandler.writeFrame(new Frame(8, 0));
               HeartbeatSender.this.frameHandler.flush();
            }
         } catch (IOException var3) {
         }
      }
   }
}
