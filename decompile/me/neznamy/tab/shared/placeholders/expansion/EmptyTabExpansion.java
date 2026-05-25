package me.neznamy.tab.shared.placeholders.expansion;

import lombok.NonNull;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class EmptyTabExpansion implements TabExpansion {
   @Override
   public void setRawPropertyValue(@NotNull TabPlayer player, @NotNull String property, @NotNull String value) {
   }

   @Override
   public void setPlaceholderValue(@NotNull TabPlayer player, @NotNull String placeholder, @NotNull String value) {
   }

   @Override
   public void setValue(@NonNull TabPlayer player, @NonNull String key, @NonNull String value) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (key == null) {
         throw new NullPointerException("key is marked non-null but is null");
      }

      if (value == null) {
         throw new NullPointerException("value is marked non-null but is null");
      }
   }

   @Override
   public void unregisterExpansion() {
   }
}
