package me.neznamy.tab.shared.features.scoreboard;

import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.scoreboard.lines.ScoreboardLine;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScoreRefresher extends RefreshableFeature implements CustomThreaded {
   private static final StringToComponentCache cache = new StringToComponentCache("Scoreboard NumberFormat", 1000);
   @NonNull
   private final ScoreboardLine line;
   @NonNull
   private final String numberFormat;

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Updating NumberFormat";
   }

   @Override
   public void refresh(@NotNull TabPlayer refreshed, boolean force) {
      if (refreshed.scoreboardData.activeScoreboard == this.line.getParent()) {
         if (refreshed.scoreboardData.numberFormatProperties.get(this.line) != null) {
            refreshed.getScoreboard()
               .setScore("TAB-Scoreboard", this.line.getPlayerName(refreshed), this.line.getNumber(refreshed), null, this.getNumberFormat(refreshed));
         }
      }
   }

   public void registerProperties(@NotNull TabPlayer player) {
      player.scoreboardData.numberFormatProperties.put(this.line, new Property(this, player, this.numberFormat));
   }

   @Nullable
   public TabComponent getNumberFormat(@NotNull TabPlayer player) {
      return cache.get(player.scoreboardData.numberFormatProperties.get(this.line).updateAndGet());
   }

   @NotNull
   @Override
   public ThreadExecutor getCustomThread() {
      return this.line.getCustomThread();
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return this.line.getFeatureName();
   }

   @Generated
   public ScoreRefresher(@NonNull ScoreboardLine line, @NonNull String numberFormat) {
      if (line == null) {
         throw new NullPointerException("line is marked non-null but is null");
      }

      if (numberFormat == null) {
         throw new NullPointerException("numberFormat is marked non-null but is null");
      }

      this.line = line;
      this.numberFormat = numberFormat;
   }
}
