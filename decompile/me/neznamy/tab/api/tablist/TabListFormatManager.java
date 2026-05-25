package me.neznamy.tab.api.tablist;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TabListFormatManager {
   void setPrefix(@NonNull TabPlayer var1, @Nullable String var2);

   void setName(@NonNull TabPlayer var1, @Nullable String var2);

   void setSuffix(@NonNull TabPlayer var1, @Nullable String var2);

   @Nullable
   String getCustomPrefix(@NonNull TabPlayer var1);

   @Nullable
   String getCustomName(@NonNull TabPlayer var1);

   @Nullable
   String getCustomSuffix(@NonNull TabPlayer var1);

   @Deprecated
   @NotNull
   String getOriginalPrefix(@NonNull TabPlayer var1);

   @Deprecated
   @NotNull
   String getOriginalName(@NonNull TabPlayer var1);

   @Deprecated
   @NotNull
   String getOriginalSuffix(@NonNull TabPlayer var1);

   @NotNull
   String getOriginalRawPrefix(@NonNull TabPlayer var1);

   @NotNull
   String getOriginalRawName(@NonNull TabPlayer var1);

   @NotNull
   String getOriginalRawSuffix(@NonNull TabPlayer var1);

   @NotNull
   String getOriginalReplacedPrefix(@NonNull TabPlayer var1);

   @NotNull
   String getOriginalReplacedName(@NonNull TabPlayer var1);

   @NotNull
   String getOriginalReplacedSuffix(@NonNull TabPlayer var1);
}
