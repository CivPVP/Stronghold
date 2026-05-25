package me.neznamy.tab.api.tablist;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SortingManager {
   void forceTeamName(@NonNull TabPlayer var1, @Nullable String var2);

   @Nullable
   String getForcedTeamName(@NonNull TabPlayer var1);

   @NotNull
   String getOriginalTeamName(@NonNull TabPlayer var1);
}
