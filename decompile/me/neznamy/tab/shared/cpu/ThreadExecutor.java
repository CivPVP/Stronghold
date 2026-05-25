package me.neznamy.tab.shared.cpu;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;

public class ThreadExecutor {
   private static final int SHUTDOWN_TIMEOUT = 2000;
   private final String threadName;
   private final ScheduledExecutorService executor;

   public ThreadExecutor(@NotNull String threadName) {
      this.threadName = threadName;
      this.executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat(threadName).build());
   }

   public void shutdown() {
      this.executor.shutdown();

      try {
         if (!this.executor.awaitTermination(2000L, TimeUnit.MILLISECONDS)) {
            TAB.getInstance()
               .getErrorManager()
               .printError(
                  "Soft shutdown of thread " + this.threadName + " exceeded time limit of " + 2000 + "ms, forcing shutdown. This may cause issues.", null
               );
            this.executor.shutdownNow();
         }
      } catch (InterruptedException var2) {
      }
   }

   public void execute(@NotNull Runnable task) {
      if (!this.executor.isShutdown()) {
         this.executor.execute(new CaughtTask(task));
      }
   }

   public void execute(@NotNull TimedCaughtTask task) {
      if (!this.executor.isShutdown()) {
         this.executor.execute(task);
      }
   }

   public void executeLater(@NotNull TimedCaughtTask task, int delayMillis) {
      if (!this.executor.isShutdown()) {
         this.executor.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
      }
   }

   public void repeatTask(@NotNull TimedCaughtTask task, int intervalMilliseconds) {
      if (!this.executor.isShutdown()) {
         this.executor.scheduleAtFixedRate(task, intervalMilliseconds, intervalMilliseconds, TimeUnit.MILLISECONDS);
      }
   }
}
