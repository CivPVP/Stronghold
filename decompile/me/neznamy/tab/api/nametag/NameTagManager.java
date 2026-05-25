package me.neznamy.tab.api.nametag;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NameTagManager {
   void hideNameTag(@NonNull TabPlayer var1);

   void hideNameTag(@NonNull TabPlayer var1, @NonNull TabPlayer var2);

   void showNameTag(@NonNull TabPlayer var1);

   void showNameTag(@NonNull TabPlayer var1, @NonNull TabPlayer var2);

   boolean hasHiddenNameTag(@NonNull TabPlayer var1);

   boolean hasHiddenNameTag(@NonNull TabPlayer var1, @NonNull TabPlayer var2);

   void pauseTeamHandling(@NonNull TabPlayer var1);

   void resumeTeamHandling(@NonNull TabPlayer var1);

   boolean hasTeamHandlingPaused(@NonNull TabPlayer var1);

   void setCollisionRule(@NonNull TabPlayer var1, @Nullable Boolean var2);

   @Nullable
   Boolean getCollisionRule(@NonNull TabPlayer var1);

   void setPrefix(@NonNull TabPlayer var1, @Nullable String var2);

   void setSuffix(@NonNull TabPlayer var1, @Nullable String var2);

   @Nullable
   String getCustomPrefix(@NonNull TabPlayer var1);

   @Nullable
   String getCustomSuffix(@NonNull TabPlayer var1);

   @NotNull
   String getOriginalRawPrefix(@NonNull TabPlayer var1);

   @NotNull
   String getOriginalRawSuffix(@NonNull TabPlayer var1);

   @NotNull
   String getOriginalReplacedPrefix(@NonNull TabPlayer var1);

   @NotNull
   String getOriginalReplacedSuffix(@NonNull TabPlayer var1);

   @Deprecated
   @NotNull
   String getOriginalPrefix(@NonNull TabPlayer var1);

   @Deprecated
   @NotNull
   String getOriginalSuffix(@NonNull TabPlayer var1);

   void toggleNameTagVisibilityView(@NonNull TabPlayer var1, boolean var2);

   void showNameTagVisibilityView(@NonNull TabPlayer var1, boolean var2);

   void hideNameTagVisibilityView(@NonNull TabPlayer var1, boolean var2);

   boolean hasHiddenNameTagVisibilityView(@NonNull TabPlayer var1);
}
