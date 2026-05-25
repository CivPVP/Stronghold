package me.neznamy.tab.shared.features.types;

import java.util.Collection;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class RefreshableFeature extends TabFeature {
   @NotNull
   public abstract String getRefreshDisplayName();

   public abstract void refresh(@NotNull TabPlayer var1, boolean var2);

   public void addUsedPlaceholders(@NotNull Collection<String> placeholders) {
      if (!placeholders.isEmpty()) {
         for (String p : placeholders) {
            TAB.getInstance().getPlaceholderManager().addUsedPlaceholder(p, this);
         }
      }
   }

   public void addUsedPlaceholder(@NotNull String placeholder) {
      TAB.getInstance().getPlaceholderManager().addUsedPlaceholder(placeholder, this);
   }
}
