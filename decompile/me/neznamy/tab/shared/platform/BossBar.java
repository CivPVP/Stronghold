package me.neznamy.tab.shared.platform;

import java.util.UUID;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.component.TabComponent;
import org.jetbrains.annotations.NotNull;

public interface BossBar {
   void create(@NotNull UUID var1, @NotNull TabComponent var2, float var3, @NotNull BarColor var4, @NotNull BarStyle var5);

   void update(@NotNull UUID var1, @NotNull TabComponent var2);

   void update(@NotNull UUID var1, float var2);

   void update(@NotNull UUID var1, @NotNull BarStyle var2);

   void update(@NotNull UUID var1, @NotNull BarColor var2);

   void remove(@NotNull UUID var1);

   void clear();
}
