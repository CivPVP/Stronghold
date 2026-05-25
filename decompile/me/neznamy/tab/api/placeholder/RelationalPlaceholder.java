package me.neznamy.tab.api.placeholder;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.Nullable;

public interface RelationalPlaceholder extends Placeholder {
   void updateValue(@NonNull TabPlayer var1, @NonNull TabPlayer var2, @Nullable String var3);

   void update(@NonNull TabPlayer var1, @NonNull TabPlayer var2);
}
