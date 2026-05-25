package me.neznamy.tab.shared.features;

import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.features.types.UnLoadable;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import org.jetbrains.annotations.NotNull;

public class SpectatorFix extends TabFeature implements JoinListener, Loadable, UnLoadable, CustomThreaded {
   private final ThreadExecutor customThread = new ThreadExecutor("TAB Spectator Fix Thread");

   private void updatePlayer(@NotNull TabPlayer viewer, boolean realGameMode, boolean mutually) {
      for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
         if (viewer != target) {
            if (!viewer.hasPermission("tab.spectatorbypass")) {
               if (realGameMode) {
                  viewer.getTabList().unblockSpectator(target);
               } else {
                  viewer.getTabList().blockSpectator(target);
               }
            }

            if (mutually && !target.hasPermission("tab.spectatorbypass")) {
               if (realGameMode) {
                  target.getTabList().unblockSpectator(viewer);
               } else {
                  target.getTabList().blockSpectator(viewer);
               }
            }
         }
      }
   }

   @Override
   public void onJoin(@NotNull TabPlayer p) {
      this.updatePlayer(p, false, true);
   }

   @Override
   public void load() {
      TAB.getInstance().getCpu().getTablistEntryCheckThread().repeatTask(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            ((TrackedTabList)p.getTabList()).checkGameModes();
         }
      }, this.getFeatureName(), "Periodic task"), 500);

      for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
         this.updatePlayer(viewer, false, false);
      }
   }

   @Override
   public void unload() {
      for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
         this.updatePlayer(viewer, true, false);
      }
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return "Spectator fix";
   }

   @Generated
   @Override
   public ThreadExecutor getCustomThread() {
      return this.customThread;
   }
}
