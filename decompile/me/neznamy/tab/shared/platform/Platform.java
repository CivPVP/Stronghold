package me.neznamy.tab.shared.platform;

import java.io.File;
import java.util.function.Consumer;
import me.neznamy.tab.shared.GroupManager;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.features.PerWorldPlayerListConfiguration;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Platform {
   @NotNull
   GroupManager detectPermissionPlugin();

   void registerUnknownPlaceholder(@NotNull String var1);

   void loadPlayers();

   void registerPlaceholders();

   @Nullable
   PipelineInjector createPipelineInjector();

   @NotNull
   default TabExpansion createTabExpansion() {
      return new EmptyTabExpansion();
   }

   @Nullable
   ProxySupport getProxySupport(@NotNull String var1);

   @Nullable
   TabFeature getPerWorldPlayerList(@NotNull PerWorldPlayerListConfiguration var1);

   void logInfo(@NotNull TabComponent var1);

   void logWarn(@NotNull TabComponent var1);

   String getServerVersionInfo();

   void registerListener();

   void registerCommand();

   void startMetrics();

   File getDataFolder();

   boolean isProxy();

   @NotNull
   Object convertComponent(@NotNull TabComponent var1);

   @NotNull
   Scoreboard createScoreboard(@NotNull TabPlayer var1);

   @NotNull
   BossBar createBossBar(@NotNull TabPlayer var1);

   @NotNull
   TabList createTabList(@NotNull TabPlayer var1);

   boolean supportsScoreboards();

   default boolean supportsListOrder() {
      return true;
   }

   default boolean isSafeFromPacketEventsBug() {
      return true;
   }

   @NotNull
   String getCommand();

   void registerCustomCommand(@NotNull String var1, @NotNull Consumer<TabPlayer> var2);

   void unregisterAllCustomCommands();
}
