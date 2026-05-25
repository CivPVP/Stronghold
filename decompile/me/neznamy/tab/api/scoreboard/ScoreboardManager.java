package me.neznamy.tab.api.scoreboard;

import java.util.List;
import java.util.Map;
import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ScoreboardManager {
   @NotNull
   Scoreboard createScoreboard(@NonNull String var1, @NonNull String var2, @NonNull List<String> var3);

   @NotNull
   Map<String, Scoreboard> getRegisteredScoreboards();

   void removeScoreboard(@NonNull String var1);

   void removeScoreboard(@NonNull Scoreboard var1);

   void showScoreboard(@NonNull TabPlayer var1, @NonNull Scoreboard var2);

   boolean hasCustomScoreboard(@NonNull TabPlayer var1);

   void resetScoreboard(@NonNull TabPlayer var1);

   boolean hasScoreboardVisible(@NonNull TabPlayer var1);

   void setScoreboardVisible(@NonNull TabPlayer var1, boolean var2, boolean var3);

   void toggleScoreboard(@NonNull TabPlayer var1, boolean var2);

   void announceScoreboard(@NonNull String var1, int var2);

   @Nullable
   Scoreboard getActiveScoreboard(@NonNull TabPlayer var1);
}
