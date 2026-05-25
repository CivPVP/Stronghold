package me.neznamy.tab.api.event.plugin;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.TabEvent;
import org.jetbrains.annotations.NotNull;

public interface PlaceholderRegisterEvent extends TabEvent {
   @NotNull
   String getIdentifier();

   void setServerPlaceholder(@NonNull Supplier<String> var1);

   void setPlayerPlaceholder(@NonNull Function<TabPlayer, String> var1);

   void setRelationalPlaceholder(@NonNull BiFunction<TabPlayer, TabPlayer, String> var1);
}
