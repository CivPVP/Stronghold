package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public interface ServerSwitchListener {
   void onServerChange(@NotNull TabPlayer var1, @NotNull Server var2, @NotNull Server var3);
}
