package me.neznamy.tab.shared.util.cache;

import me.neznamy.tab.shared.chat.component.TabComponent;
import org.jetbrains.annotations.NotNull;

public class LastColorCache extends StringToComponentCache {
   public LastColorCache(String name, int cacheSize) {
      super(name, cacheSize);
   }

   @NotNull
   @Override
   public TabComponent convert(@NotNull String text) {
      return super.convert(text + "extra text");
   }
}
