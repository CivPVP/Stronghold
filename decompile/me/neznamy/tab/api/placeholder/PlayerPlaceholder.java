package me.neznamy.tab.api.placeholder;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.Nullable;

public interface PlayerPlaceholder extends Placeholder {
   void updateValue(@NonNull TabPlayer var1, @Nullable String var2);

   void update(@NonNull TabPlayer var1);
}
