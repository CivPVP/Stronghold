package me.neznamy.tab.shared.chat.rgb.format;

import java.util.function.Function;
import me.neznamy.tab.shared.chat.TabTextColor;
import org.jetbrains.annotations.NotNull;

public interface RGBFormatter {
   @NotNull
   String reformat(@NotNull String var1, @NotNull Function<TabTextColor, String> var2);
}
