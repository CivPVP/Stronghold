package me.neznamy.tab.libs.org.apache.commons.pool2.impl;

import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

class EvictionTimer {
   private static ScheduledThreadPoolExecutor executor;
   private static final HashMap<WeakReference<BaseGenericObjectPool<?>.Evictor>, EvictionTimer.WeakRunner<BaseGenericObjectPool<?>.Evictor>> TASK_MAP = new HashMap<>();

   static synchronized void cancel(BaseGenericObjectPool<?>.Evictor evictor, Duration timeout, boolean restarting) {
      if (evictor != null) {
         evictor.cancel();
         remove(evictor);
      }

      if (!restarting && executor != null && TASK_MAP.isEmpty()) {
         executor.shutdown();

         try {
            executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
         } catch (InterruptedException var4) {
         }

         executor.setCorePoolSize(0);
         executor = null;
      }
   }

   static ScheduledThreadPoolExecutor getExecutor() {
      return executor;
   }

   static synchronized int getNumTasks() {
      return TASK_MAP.size();
   }

   static HashMap<WeakReference<BaseGenericObjectPool<?>.Evictor>, EvictionTimer.WeakRunner<BaseGenericObjectPool<?>.Evictor>> getTaskMap() {
      return TASK_MAP;
   }

   private static void remove(BaseGenericObjectPool<?>.Evictor evictor) {
      for (Entry<WeakReference<BaseGenericObjectPool<?>.Evictor>, EvictionTimer.WeakRunner<BaseGenericObjectPool<?>.Evictor>> entry : TASK_MAP.entrySet()) {
         if (entry.getKey().get() == evictor) {
            executor.remove(entry.getValue());
            TASK_MAP.remove(entry.getKey());
            break;
         }
      }
   }

   static synchronized void schedule(BaseGenericObjectPool<?>.Evictor task, Duration delay, Duration period) {
      if (null == executor) {
         executor = new ScheduledThreadPoolExecutor(1, new EvictionTimer.EvictorThreadFactory());
         executor.setRemoveOnCancelPolicy(true);
         executor.scheduleAtFixedRate(new EvictionTimer.Reaper(), delay.toMillis(), period.toMillis(), TimeUnit.MILLISECONDS);
      }

      WeakReference<BaseGenericObjectPool<?>.Evictor> ref = new WeakReference<>(task);
      EvictionTimer.WeakRunner<BaseGenericObjectPool<?>.Evictor> runner = new EvictionTimer.WeakRunner<>(ref);
      ScheduledFuture<?> scheduledFuture = executor.scheduleWithFixedDelay(runner, delay.toMillis(), period.toMillis(), TimeUnit.MILLISECONDS);
      task.setScheduledFuture(scheduledFuture);
      TASK_MAP.put(ref, runner);
   }

   private EvictionTimer() {
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("EvictionTimer []");
      return builder.toString();
   }

   private static class EvictorThreadFactory implements ThreadFactory {
      private EvictorThreadFactory() {
      }

      @Override
      public Thread newThread(Runnable runnable) {
         Thread thread = new Thread(null, runnable, "commons-pool-evictor");
         thread.setDaemon(true);
         AccessController.doPrivileged(() -> {
            thread.setContextClassLoader(EvictionTimer.EvictorThreadFactory.class.getClassLoader());
            return null;
         });
         return thread;
      }
   }

   private static class Reaper implements Runnable {
      private Reaper() {
      }

      @Override
      public void run() {
         synchronized (EvictionTimer.class) {
            for (Entry<WeakReference<BaseGenericObjectPool<?>.Evictor>, EvictionTimer.WeakRunner<BaseGenericObjectPool<?>.Evictor>> entry : EvictionTimer.TASK_MAP
               .entrySet()) {
               if (entry.getKey().get() == null) {
                  EvictionTimer.executor.remove(entry.getValue());
                  EvictionTimer.TASK_MAP.remove(entry.getKey());
               }
            }

            if (EvictionTimer.TASK_MAP.isEmpty() && EvictionTimer.executor != null) {
               EvictionTimer.executor.shutdown();
               EvictionTimer.executor.setCorePoolSize(0);
               EvictionTimer.executor = null;
            }
         }
      }
   }

   private static class WeakRunner<R extends Runnable> implements Runnable {
      private final WeakReference<R> ref;

      private WeakRunner(WeakReference<R> ref) {
         this.ref = ref;
      }

      @Override
      public void run() {
         Runnable task = this.ref.get();
         if (task != null) {
            task.run();
         } else {
            EvictionTimer.executor.remove(this);
            EvictionTimer.TASK_MAP.remove(this.ref);
         }
      }
   }
}
