package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import org.jetbrains.annotations.NotNull;

public interface ProxyFeature {
   default void onProxyLoadRequest() {
   }

   default void onJoin(@NotNull ProxyPlayer player) {
   }

   default void onQuit(@NotNull ProxyPlayer player) {
   }

   default void onVanishStatusChange(@NotNull ProxyPlayer player) {
   }

   default void onServerSwitch(@NotNull ProxyPlayer player) {
   }
}
