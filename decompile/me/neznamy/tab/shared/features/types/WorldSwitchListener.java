package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public interface WorldSwitchListener {
   void onWorldChange(@NotNull TabPlayer var1, @NotNull World var2, @NotNull World var3);
}
