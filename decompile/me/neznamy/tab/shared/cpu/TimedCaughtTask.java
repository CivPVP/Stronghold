package me.neznamy.tab.shared.cpu;

import lombok.Generated;
import me.neznamy.tab.shared.TAB;

public class TimedCaughtTask implements Runnable {
   private final CpuManager cpu;
   private final Runnable task;
   private final String feature;
   private final String usageType;

   @Override
   public void run() {
      try {
         long time = System.nanoTime();
         this.task.run();
         this.cpu.addTime(this.feature, this.usageType, System.nanoTime() - time);
      } catch (Exception | LinkageError | StackOverflowError e) {
         TAB.getInstance().getErrorManager().taskThrewError(e);
      }
   }

   @Generated
   public TimedCaughtTask(CpuManager cpu, Runnable task, String feature, String usageType) {
      this.cpu = cpu;
      this.task = task;
      this.feature = feature;
      this.usageType = usageType;
   }
}
