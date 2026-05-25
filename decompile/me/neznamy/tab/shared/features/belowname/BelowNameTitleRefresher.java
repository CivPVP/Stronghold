package me.neznamy.tab.shared.features.belowname;

import lombok.Generated;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class BelowNameTitleRefresher extends RefreshableFeature implements CustomThreaded {
   @NotNull
   private final BelowName feature;
   @NotNull
   private final ThreadExecutor customThread;

   @NotNull
   @Override
   public String getFeatureName() {
      return "BelowName";
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Updating BelowName title";
   }

   @Override
   public void refresh(@NotNull TabPlayer refreshed, boolean force) {
      if (!refreshed.belowNameData.disabled.get()) {
         refreshed.getScoreboard()
            .updateObjective(
               "TAB-BelowName",
               this.feature.getCache().get(refreshed.belowNameData.text.updateAndGet()),
               Scoreboard.HealthDisplay.INTEGER,
               this.feature.getCache().get(refreshed.belowNameData.defaultNumberFormat.updateAndGet())
            );
      }
   }

   @NotNull
   @Override
   public ThreadExecutor getCustomThread() {
      return this.customThread;
   }

   @Generated
   public BelowNameTitleRefresher(@NotNull BelowName feature, @NotNull ThreadExecutor customThread) {
      if (feature == null) {
         throw new NullPointerException("feature is marked non-null but is null");
      }

      if (customThread == null) {
         throw new NullPointerException("customThread is marked non-null but is null");
      }

      this.feature = feature;
      this.customThread = customThread;
   }
}
