package me.neznamy.tab.shared.features.sorting;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SortingPlayerData {
   public String shortTeamName;
   public String fullTeamName;
   public String teamNameNote;
   @Nullable
   public String forcedTeamName;

   @NotNull
   public String getShortTeamName() {
      return this.forcedTeamName != null ? this.forcedTeamName : this.shortTeamName;
   }

   @NotNull
   public String getFullTeamName() {
      return this.forcedTeamName != null ? this.forcedTeamName : this.fullTeamName;
   }
}
