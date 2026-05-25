package me.neznamy.tab.shared.features.pingspoof;

import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.features.types.UnLoadable;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import org.jetbrains.annotations.NotNull;

public class PingSpoof extends TabFeature implements JoinListener, Loadable, UnLoadable, CustomThreaded {
   private final ThreadExecutor customThread = new ThreadExecutor("TAB Ping Spoof Thread");
   private final PingSpoofConfiguration configuration;

   @Override
   public void load() {
      TrackedTabList.setForcedLatency(this.configuration.getValue());
      this.updateAll(false);
   }

   @Override
   public void unload() {
      TrackedTabList.setForcedLatency(null);
      this.updateAll(true);
   }

   @Override
   public void onJoin(@NotNull TabPlayer connectedPlayer) {
      for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
         connectedPlayer.getTabList().updateLatency(all, this.configuration.getValue());
         all.getTabList().updateLatency(connectedPlayer, this.configuration.getValue());
      }
   }

   private void updateAll(boolean realPing) {
      for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
         for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
            viewer.getTabList().updateLatency(target, realPing ? target.getPing() : this.configuration.getValue());
         }
      }
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return "Ping spoof";
   }

   @Generated
   public PingSpoofConfiguration getConfiguration() {
      return this.configuration;
   }

   @Generated
   public PingSpoof(PingSpoofConfiguration configuration) {
      this.configuration = configuration;
   }

   @Generated
   @Override
   public ThreadExecutor getCustomThread() {
      return this.customThread;
   }
}
