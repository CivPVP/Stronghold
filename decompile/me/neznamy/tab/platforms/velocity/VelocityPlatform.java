package me.neznamy.tab.platforms.velocity;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.scoreboard.ObjectiveEvent.Display;
import com.velocitypowered.api.event.scoreboard.ObjectiveEvent.Unregister;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scoreboard.ScoreboardManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Generated;
import me.neznamy.tab.libs.org.bstats.charts.SimplePie;
import me.neznamy.tab.platforms.velocity.features.VelocityRedisSupport;
import me.neznamy.tab.platforms.velocity.hook.VelocityPremiumVanishHook;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.impl.AdventureBossBar;
import me.neznamy.tab.shared.platform.impl.DummyScoreboard;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import me.neznamy.tab.shared.util.PerformanceUtil;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VelocityPlatform extends ProxyPlatform {
   @NotNull
   private final VelocityTAB plugin;
   private boolean scoreboardAPI;
   private final MinecraftChannelIdentifier MCI = MinecraftChannelIdentifier.from("tab:bridge-6");
   private final ComponentLogger logger = ComponentLogger.logger("TAB");
   private final List<String> customCommands = new ArrayList<>();

   public VelocityPlatform(@NotNull VelocityTAB plugin) {
      this.plugin = plugin;
      if (plugin.getServer().getPluginManager().isLoaded("velocity-scoreboard-api")) {
         try {
            ScoreboardManager.getInstance();
            this.scoreboardAPI = true;
            plugin.getServer().getEventManager().register(plugin, Display.class, e -> {
               TAB tab = TAB.getInstance();
               if (!tab.isPluginDisabled()) {
                  tab.getCPUManager().runTask(() -> {
                     TabPlayer player = tab.getPlayer(e.getPlayer().getUniqueId());
                     if (player != null) {
                        tab.getFeatureManager().onDisplayObjective(player, e.getNewSlot().ordinal(), e.getObjective().getName());
                     }
                  });
               }
            });
            plugin.getServer().getEventManager().register(plugin, Unregister.class, e -> {
               TAB tab = TAB.getInstance();
               if (!tab.isPluginDisabled()) {
                  tab.getCPUManager().runTask(() -> {
                     TabPlayer player = tab.getPlayer(e.getPlayer().getUniqueId());
                     if (player != null) {
                        tab.getFeatureManager().onObjective(player, 1, e.getObjective().getName());
                     }
                  });
               }
            });
         } catch (IllegalStateException var3) {
         }
      } else {
         this.logInfo(new TabTextComponent("==============================================================================", TabTextColor.RED));
         this.logInfo(new TabTextComponent("Velocity does not have any sort of scoreboard API.", TabTextColor.RED));
         this.logInfo(new TabTextComponent("As a result, many features cannot be implemented using the standard Velocity API.", TabTextColor.RED));
         this.logInfo(
            new TabTextComponent(
               "In order to enhance your experience, please consider installing VelocityScoreboardAPI (https://github.com/NEZNAMY/VelocityScoreboardAPI/releases/) plugin.",
               TabTextColor.RED
            )
         );
         this.logInfo(new TabTextComponent("Until then, the following features will not work:", TabTextColor.RED));
         this.logInfo(new TabTextComponent("- scoreboard-teams", TabTextColor.RED));
         this.logInfo(new TabTextComponent("- belowname-objective", TabTextColor.RED));
         this.logInfo(new TabTextComponent("- playerlist-objective", TabTextColor.RED));
         this.logInfo(new TabTextComponent("- scoreboard", TabTextColor.RED));
         this.logInfo(new TabTextComponent("==============================================================================", TabTextColor.RED));
      }

      if (plugin.getServer().getPluginManager().isLoaded("premiumvanish")) {
         new VelocityPremiumVanishHook().register();
      }
   }

   @Override
   public void loadPlayers() {
      for (Player p : this.plugin.getServer().getAllPlayers()) {
         TAB.getInstance().addPlayer(new VelocityTabPlayer(this, p));
      }
   }

   @Override
   public void registerPlaceholders() {
      super.registerPlaceholders();

      for (RegisteredServer registeredServer : this.plugin.getServer().getAllServers()) {
         Server server = Server.byName(registeredServer.getServerInfo().getName());
         TAB.getInstance().getPlaceholderManager().registerInternalServerPlaceholder("%online_" + server.getName() + "%", 1000, () -> {
            int count = 0;

            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
               if (player.server == server && !player.isVanished()) {
                  count++;
               }
            }

            ProxySupport proxySupport = TAB.getInstance().getFeatureManager().getFeature("ProxySupport");
            if (proxySupport != null) {
               for (ProxyPlayer player : proxySupport.getProxyPlayers().values()) {
                  if (player.server == server && !player.isVanished()) {
                     count++;
                  }
               }
            }

            return PerformanceUtil.toString(count);
         });
      }
   }

   @Nullable
   @Override
   public ProxySupport getProxySupport(@NotNull String plugin) {
      return plugin.equalsIgnoreCase("RedisBungee")
            && ReflectionUtils.classExists("com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI")
            && RedisBungeeAPI.getRedisBungeeApi() != null
         ? new VelocityRedisSupport(this.plugin)
         : null;
   }

   @Override
   public void logInfo(@NotNull TabComponent message) {
      this.logger.info(message.toAdventure());
   }

   @Override
   public void logWarn(@NotNull TabComponent message) {
      this.logger.warn(message.toAdventure());
   }

   @NotNull
   @Override
   public String getServerVersionInfo() {
      return "[Velocity] " + this.plugin.getServer().getVersion().getName() + " - " + this.plugin.getServer().getVersion().getVersion();
   }

   @Override
   public void registerListener() {
      this.plugin.getServer().getEventManager().register(this.plugin, new VelocityEventListener());
   }

   @Override
   public void registerCommand() {
      CommandManager cmd = this.plugin.getServer().getCommandManager();
      cmd.register(cmd.metaBuilder(this.getCommand()).build(), new VelocityTabCommand());
   }

   @Override
   public void startMetrics() {
      this.plugin
         .getMetricsFactory()
         .make(this.plugin, 10533)
         .addCustomChart(
            new SimplePie("global_playerlist_enabled", () -> TAB.getInstance().getFeatureManager().isFeatureEnabled("GlobalPlayerList") ? "Yes" : "No")
         );
   }

   @NotNull
   @Override
   public File getDataFolder() {
      return this.plugin.getDataFolder().toFile();
   }

   @NotNull
   public Component convertComponent(@NotNull TabComponent component) {
      return component.toAdventure();
   }

   @NotNull
   @Override
   public Scoreboard createScoreboard(@NotNull TabPlayer player) {
      return this.scoreboardAPI ? new VelocityScoreboard((VelocityTabPlayer)player) : new DummyScoreboard(player);
   }

   @NotNull
   @Override
   public BossBar createBossBar(@NotNull TabPlayer player) {
      return new AdventureBossBar(player);
   }

   @NotNull
   @Override
   public TabList createTabList(@NotNull TabPlayer player) {
      return new VelocityTabList((VelocityTabPlayer)player);
   }

   @Override
   public boolean supportsScoreboards() {
      return this.scoreboardAPI;
   }

   @Nullable
   @Override
   public PipelineInjector createPipelineInjector() {
      return null;
   }

   @Override
   public void registerChannel() {
      this.plugin.getServer().getChannelRegistrar().register(new ChannelIdentifier[]{this.MCI});
   }

   @NotNull
   @Override
   public String getCommand() {
      return "btab";
   }

   @Override
   public void registerCustomCommand(@NotNull String commandName, @NotNull Consumer<TabPlayer> function) {
      CommandManager cmd = this.plugin.getServer().getCommandManager();
      CommandMeta meta = cmd.metaBuilder(commandName).build();
      this.customCommands.add(commandName);
      cmd.register(
         meta,
         (SimpleCommand)invocation -> {
            if (invocation.source() instanceof ConsoleCommandSource) {
               invocation.source()
                  .sendMessage(TabComponent.fromColoredText(TAB.getInstance().getConfiguration().getMessages().getCommandOnlyFromGame()).toAdventure());
            } else {
               TabPlayer p = TAB.getInstance().getPlayer(((Player)invocation.source()).getUniqueId());
               if (p != null) {
                  function.accept(p);
               }
            }
         }
      );
   }

   @Override
   public void unregisterAllCustomCommands() {
      for (String cmd : this.customCommands) {
         this.plugin.getServer().getCommandManager().unregister(cmd);
      }
   }

   @NotNull
   @Generated
   public VelocityTAB getPlugin() {
      return this.plugin;
   }

   @Generated
   public boolean isScoreboardAPI() {
      return this.scoreboardAPI;
   }

   @Generated
   public MinecraftChannelIdentifier getMCI() {
      return this.MCI;
   }

   @Generated
   public ComponentLogger getLogger() {
      return this.logger;
   }

   @Generated
   public List<String> getCustomCommands() {
      return this.customCommands;
   }
}
