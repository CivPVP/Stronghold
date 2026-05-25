package me.neznamy.tab.api.placeholder;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.NotNull;

public interface PlaceholderManager {
   @NotNull
   ServerPlaceholder registerServerPlaceholder(@NonNull String var1, int var2, @NonNull Supplier<String> var3);

   @NotNull
   PlayerPlaceholder registerPlayerPlaceholder(@NonNull String var1, int var2, @NonNull Function<TabPlayer, String> var3);

   @NotNull
   RelationalPlaceholder registerRelationalPlaceholder(@NonNull String var1, int var2, @NonNull BiFunction<TabPlayer, TabPlayer, String> var3);

   @NotNull
   Placeholder getPlaceholder(@NonNull String var1);

   void unregisterPlaceholder(@NonNull Placeholder var1);

   void unregisterPlaceholder(@NonNull String var1);
}
