package me.neznamy.tab.shared.backend;

import me.neznamy.tab.shared.GroupManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.hook.LuckPermsHook;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import me.neznamy.tab.shared.platform.Platform;
import me.neznamy.tab.shared.util.PerformanceUtil;
import org.jetbrains.annotations.NotNull;

public interface BackendPlatform extends Platform {
   @NotNull
   @Override
   default GroupManager detectPermissionPlugin() {
      return LuckPermsHook.getInstance().isInstalled()
         ? new GroupManager("LuckPerms", LuckPermsHook.getInstance().getGroupFunction())
         : new GroupManager("None", p -> "NONE");
   }

   @Override
   default ProxySupport getProxySupport(@NotNull String plugin) {
      return null;
   }

   @Override
   default void registerPlaceholders() {
      UniversalPlaceholderRegistry registry = new UniversalPlaceholderRegistry();
      PlaceholderManagerImpl manager = TAB.getInstance().getPlaceholderManager();
      manager.registerInternalPlayerPlaceholder("%health%", 100, p -> PerformanceUtil.toString((int)Math.ceil(((BackendTabPlayer)p).getHealth())));
      manager.registerInternalPlayerPlaceholder("%displayname%", 500, p -> ((BackendTabPlayer)p).getDisplayName());
      manager.registerInternalServerPlaceholder("%tps%", 1000, () -> registry.getDecimal2().format(Math.min(20.0, this.getTPS())));
      manager.registerInternalServerPlaceholder("%mspt%", 1000, () -> registry.getDecimal2().format(this.getMSPT()));
      manager.registerInternalPlayerPlaceholder("%deaths%", 1000, p -> PerformanceUtil.toString(((BackendTabPlayer)p).getDeaths()));
      registry.registerPlaceholders(manager);
   }

   @Override
   default boolean isProxy() {
      return false;
   }

   @NotNull
   @Override
   default String getCommand() {
      return "tab";
   }

   default void registerDummyPlaceholder(@NotNull String identifier) {
      if (identifier.startsWith("%rel_")) {
         TAB.getInstance().getPlaceholderManager().registerRelationalPlaceholder(identifier, -1, (viewer, target) -> identifier);
      } else {
         TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier, -1, () -> identifier);
      }
   }

   double getTPS();

   double getMSPT();
}
