package me.neznamy.tab.shared.features.playerlistobjective;

import lombok.Generated;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerListObjectiveTitleRefresher extends RefreshableFeature implements CustomThreaded {
   @NotNull
   private final YellowNumber feature;

   @NotNull
   @Override
   public String getFeatureName() {
      return this.feature.getFeatureName();
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Updating Playerlist Objective title";
   }

   @Override
   public void refresh(@NotNull TabPlayer refreshed, boolean force) {
      if (!refreshed.playerlistObjectiveData.disabled.get()) {
         refreshed.getScoreboard()
            .updateObjective(
               "TAB-PlayerList",
               this.feature.getCache().get(refreshed.playerlistObjectiveData.title.updateAndGet()),
               this.feature.getConfiguration().getHealthDisplay(),
               TabComponent.empty()
            );
      }
   }

   @NotNull
   @Override
   public ThreadExecutor getCustomThread() {
      return this.feature.getCustomThread();
   }

   @Generated
   public PlayerListObjectiveTitleRefresher(@NotNull YellowNumber feature) {
      if (feature == null) {
         throw new NullPointerException("feature is marked non-null but is null");
      }

      this.feature = feature;
   }
}
