package me.neznamy.tab.shared.proxy;

import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.GroupManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PerWorldPlayerListConfiguration;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.hook.LuckPermsHook;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.platform.Platform;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.proxy.message.outgoing.RegisterPlaceholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ProxyPlatform implements Platform {
   @NotNull
   @Override
   public GroupManager detectPermissionPlugin() {
      return LuckPermsHook.getInstance().isInstalled() && !TAB.getInstance().getConfiguration().getConfig().isBukkitPermissions()
         ? new GroupManager("LuckPerms", LuckPermsHook.getInstance().getGroupFunction())
         : new GroupManager("Vault through Bridge", TabPlayer::getGroup);
   }

   @Override
   public void registerUnknownPlaceholder(@NotNull String identifier) {
      PlaceholderManagerImpl pl = TAB.getInstance().getPlaceholderManager();
      int refresh = pl.getConfiguration().getRefreshInterval(identifier);
      Placeholder placeholder;
      if (identifier.startsWith("%rel_")) {
         placeholder = pl.registerRelationalBridgePlaceholder(identifier, refresh);
      } else {
         placeholder = pl.registerBridgePlaceholder(identifier, refresh);
      }

      for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
         ((ProxyTabPlayer)all).sendPluginMessage(new RegisterPlaceholder(placeholder.getIdentifier(), refresh));
      }
   }

   @Override
   public void registerPlaceholders() {
      TAB.getInstance()
         .getPlaceholderManager()
         .registerInternalServerPlaceholder(
            "%tps%",
            -1,
            () -> "\"tps\" is a backend-only placeholder as the proxy does not tick anything. If you wish to display TPS of the server player is connected to, use placeholders from PlaceholderAPI and install TAB-Bridge for forwarding support to the proxy."
         );
      new UniversalPlaceholderRegistry().registerPlaceholders(TAB.getInstance().getPlaceholderManager());
   }

   @Nullable
   @Override
   public TabFeature getPerWorldPlayerList(@NotNull PerWorldPlayerListConfiguration configuration) {
      return null;
   }

   @NotNull
   @Override
   public TabExpansion createTabExpansion() {
      return new ProxyTabExpansion();
   }

   @Override
   public boolean isProxy() {
      return true;
   }

   public abstract void registerChannel();
}
