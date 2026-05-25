package me.neznamy.tab.libs.com.saicone.delivery4j.util;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public interface DelayedExecutor<T> {
   DelayedExecutor<Thread> JAVA = new DelayedExecutor<Thread>() {
      @NotNull
      public Thread execute(@NotNull Runnable command) {
         Thread thread = new Thread(command);
         thread.start();
         return thread;
      }

      @NotNull
      public Thread execute(@NotNull Runnable command, long delay, @NotNull TimeUnit unit) {
         Thread thread = new Thread(() -> {
            try {
               Thread.sleep(unit.toMillis(delay));
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
            }

            if (!Thread.interrupted()) {
               command.run();
            }
         });
         thread.start();
         return thread;
      }

      @NotNull
      public Thread execute(@NotNull Runnable command, long delay, long period, @NotNull TimeUnit unit) {
         Thread thread = new Thread(() -> {
            if (delay > 0L) {
               try {
                  Thread.sleep(unit.toMillis(delay));
               } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
               }
            }

            while (!Thread.interrupted()) {
               command.run();

               try {
                  Thread.sleep(unit.toMillis(period));
               } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
               }
            }
         });
         thread.start();
         return thread;
      }

      public void cancel(@NotNull Thread thread) {
         thread.interrupt();
      }
   };

   @NotNull
   T execute(@NotNull Runnable var1);

   @NotNull
   T execute(@NotNull Runnable var1, long var2, @NotNull TimeUnit var4);

   @NotNull
   T execute(@NotNull Runnable var1, long var2, long var4, @NotNull TimeUnit var6);

   void cancel(@NotNull T var1);

   @NotNull
   default Executor asExecutor() {
      return this::execute;
   }
}
