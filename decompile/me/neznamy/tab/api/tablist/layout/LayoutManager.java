package me.neznamy.tab.api.tablist.layout;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LayoutManager {
   @NotNull
   Layout createNewLayout(@NonNull String var1);

   @Nullable
   Layout getLayout(@NonNull String var1);

   void sendLayout(@NonNull TabPlayer var1, @Nullable Layout var2);

   void resetLayout(@NonNull TabPlayer var1);
}
