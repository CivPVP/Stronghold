package me.neznamy.tab.shared.chat.rgb.gradient;

import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.util.function.TriFunction;
import org.jetbrains.annotations.NotNull;

public interface GradientPattern {
   @NotNull
   String applyPattern(@NotNull String var1, @NotNull TriFunction<TabTextColor, String, TabTextColor, String> var2);
}
