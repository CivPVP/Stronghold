package me.neznamy.tab.shared.cpu;

import lombok.Generated;
import me.neznamy.tab.shared.TAB;

public class CaughtTask implements Runnable {
   private final Runnable task;

   @Override
   public void run() {
      try {
         this.task.run();
      } catch (Exception | LinkageError | StackOverflowError e) {
         TAB.getInstance().getErrorManager().taskThrewError(e);
      }
   }

   @Generated
   public CaughtTask(Runnable task) {
      this.task = task;
   }
}
