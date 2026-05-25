package me.neznamy.tab.shared.task;

import java.util.function.Function;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.CpuManager;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class GroupRefreshTask implements Runnable {
   @NotNull
   private final Function<TabPlayer, String> detectGroup;

   @Override
   public void run() {
      for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
         String oldGroup = all.getPermissionGroup();
         String newGroup = this.detectGroup.apply(all);
         if (!oldGroup.equals(newGroup)) {
            CpuManager cpu = TAB.getInstance().getCpu();
            cpu.getProcessingThread().execute(new TimedCaughtTask(cpu, () -> all.setGroup(newGroup), "Permission group refreshing", "Applying changes"));
         }
      }
   }

   @Generated
   public GroupRefreshTask(@NotNull Function<TabPlayer, String> detectGroup) {
      if (detectGroup == null) {
         throw new NullPointerException("detectGroup is marked non-null but is null");
      }

      this.detectGroup = detectGroup;
   }
}
