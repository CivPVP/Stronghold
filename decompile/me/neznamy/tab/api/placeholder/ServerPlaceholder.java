package me.neznamy.tab.api.placeholder;

import org.jetbrains.annotations.Nullable;

public interface ServerPlaceholder extends Placeholder {
   void updateValue(@Nullable String var1);

   void update();
}
