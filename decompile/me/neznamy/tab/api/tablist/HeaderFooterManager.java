package me.neznamy.tab.api.tablist;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.Nullable;

public interface HeaderFooterManager {
   void setHeader(@NonNull TabPlayer var1, @Nullable String var2);

   void setFooter(@NonNull TabPlayer var1, @Nullable String var2);

   void setHeaderAndFooter(@NonNull TabPlayer var1, @Nullable String var2, @Nullable String var3);
}
