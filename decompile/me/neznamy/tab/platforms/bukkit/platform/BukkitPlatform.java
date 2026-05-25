package me.neznamy.tab.platforms.bukkit.platform;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Generated;
import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.libs.org.bstats.bukkit.Metrics;
import me.neznamy.tab.libs.org.bstats.charts.SimplePie;
import me.neznamy.tab.platforms.bukkit.BukkitEventListener;
import me.neznamy.tab.platforms.bukkit.BukkitPipelineInjector;
import me.neznamy.tab.platforms.bukkit.BukkitTabCommand;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.bossbar.BukkitBossBar;
import me.neznamy.tab.platforms.bukkit.bossbar.ViaBossBar;
import me.neznamy.tab.platforms.bukkit.features.BukkitTabExpansion;
import me.neznamy.tab.platforms.bukkit.features.PerWorldPlayerList;
import me.neznamy.tab.platforms.bukkit.hook.BukkitPremiumVanishHook;
import me.neznamy.tab.platforms.bukkit.provider.ImplementationProvider;
import me.neznamy.tab.shared.GroupManager;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.TabKeybindComponent;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.chat.component.TabTranslatableComponent;
import me.neznamy.tab.shared.chat.component.object.TabObjectComponent;
import me.neznamy.tab.shared.features.PerWorldPlayerListConfiguration;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.hook.LuckPermsHook;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.impl.AdventureBossBar;
import me.neznamy.tab.shared.platform.impl.DummyBossBar;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.kyori.adventure.audience.Audience;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BukkitPlatform implements BackendPlatform {
   @NotNull
   private final JavaPlugin plugin;
   private final ProtocolVersion serverVersion = ProtocolVersion.fromFriendlyName(Bukkit.getBukkitVersion().split("-")[0]);
   private final boolean placeholderAPI = ReflectionUtils.classExists("me.clip.placeholderapi.PlaceholderAPI");
   private double[] recentTps;
   private final boolean paperTps = ReflectionUtils.methodExists(Bukkit.class, "getTPS");
   private final boolean paperMspt = ReflectionUtils.methodExists(Bukkit.class, "getAverageTickTime");
   @Nullable
   private final String serverPackage;
   @NotNull
   private ImplementationProvider implementationProvider;
   private final boolean modernOnlinePlayers;
   private final SimpleCommandMap commandMap;
   private final Map<String, Command> knownCommands;
   private final List<Command> customCommands = new ArrayList<>();

   public BukkitPlatform(@NotNull JavaPlugin plugin) {
      try {
         this.plugin = plugin;
         this.modernOnlinePlayers = Bukkit.class.getMethod("getOnlinePlayers").getReturnType() == Collection.class;
         String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();
         String[] array = CRAFTBUKKIT_PACKAGE.split("\\.");
         this.serverPackage = array.length > 3 ? array[3] : null;
         this.implementationProvider = this.findImplementationProvider();

         try {
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            this.recentTps = (double[])server.getClass().getField("recentTps").get(server);
         } catch (ReflectiveOperationException var5) {
         }

         if (Bukkit.getPluginManager().isPluginEnabled("PremiumVanish")) {
            new BukkitPremiumVanishHook().register();
         }

         this.commandMap = (SimpleCommandMap)Bukkit.getServer().getClass().getMethod("getCommandMap").invoke(Bukkit.getServer());
         this.knownCommands = (Map<String, Command>)ReflectionUtils.getField(SimpleCommandMap.class, "knownCommands").get(this.commandMap);
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   @NotNull
   private ImplementationProvider findImplementationProvider() {
      try {
         if (this.serverPackage == null) {
            String paperModule = this.getPaperModule();
            if (paperModule != null) {
               return (ImplementationProvider)Class.forName("me.neznamy.tab.platforms.paper_" + paperModule + ".PaperImplementationProvider")
                  .getConstructor()
                  .newInstance();
            } else {
               throw new UnsupportedOperationException();
            }
         } else {
            try {
               return (ImplementationProvider)Class.forName("me.neznamy.tab.platforms.bukkit." + this.serverPackage + ".NMSImplementationProvider")
                  .getConstructor()
                  .newInstance();
            } catch (ClassNotFoundException ignored) {
               throw new UnsupportedOperationException();
            }
         }
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   @Nullable
   private String getPaperModule() {
      if (!ReflectionUtils.classExists("org.bukkit.craftbukkit.CraftServer")) {
         return null;
      }

      switch (this.serverVersion) {
         case V1_20_5:
         case V1_20_6:
         case V1_21:
         case V1_21_1:
            return "1_20_5";
         case V1_21_2:
         case V1_21_3:
            return "1_21_2";
         case V1_21_4:
         case V1_21_5:
         case V1_21_6:
         case V1_21_7:
         case V1_21_8:
            return "1_21_4";
         case V1_21_9:
         case V1_21_10:
            return "1_21_9";
         case V1_21_11:
            return "1_21_11";
         default:
            return null;
      }
   }

   @Override
   public void loadPlayers() {
      for (Player p : this.getOnlinePlayers()) {
         TAB.getInstance().addPlayer(new BukkitTabPlayer(this, p));
      }
   }

   @Override
   public void registerPlaceholders() {
      PlaceholderManagerImpl manager = TAB.getInstance().getPlaceholderManager();
      manager.registerInternalServerPlaceholder("%vault-prefix%", -1, () -> "");
      manager.registerInternalServerPlaceholder("%vault-suffix%", -1, () -> "");
      if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
         RegisteredServiceProvider<Chat> rspChat = Bukkit.getServicesManager().getRegistration(Chat.class);
         if (rspChat != null) {
            Chat chat = (Chat)rspChat.getProvider();
            manager.registerInternalPlayerPlaceholder("%vault-prefix%", 1000, p -> chat.getPlayerPrefix((Player)p.getPlayer()));
            manager.registerInternalPlayerPlaceholder("%vault-suffix%", 1000, p -> chat.getPlayerSuffix((Player)p.getPlayer()));
         }
      }

      BackendPlatform.super.registerPlaceholders();
   }

   @Nullable
   @Override
   public PipelineInjector createPipelineInjector() {
      return this.implementationProvider.getChannelFunction() != null ? new BukkitPipelineInjector() : null;
   }

   @NotNull
   @Override
   public TabExpansion createTabExpansion() {
      if (this.placeholderAPI) {
         BukkitTabExpansion expansion = new BukkitTabExpansion();
         expansion.register();
         return expansion;
      } else {
         return new EmptyTabExpansion();
      }
   }

   @Nullable
   @Override
   public TabFeature getPerWorldPlayerList(@NotNull PerWorldPlayerListConfiguration configuration) {
      return new PerWorldPlayerList(this.plugin, this, configuration);
   }

   @Override
   public void registerUnknownPlaceholder(@NotNull String identifier) {
      if (!this.placeholderAPI) {
         this.registerDummyPlaceholder(identifier);
      } else {
         if (identifier.startsWith("%rel_")) {
            TAB.getInstance()
               .getPlaceholderManager()
               .registerRelationalPlaceholder(
                  identifier, (viewer, target) -> PlaceholderAPI.setRelationalPlaceholders((Player)viewer.getPlayer(), (Player)target.getPlayer(), identifier)
               );
         } else if (identifier.startsWith("%sync:")) {
            this.registerSyncPlaceholder(identifier);
         } else if (identifier.startsWith("%server_")) {
            TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier, () -> PlaceholderAPI.setPlaceholders(null, identifier));
         } else {
            TAB.getInstance()
               .getPlaceholderManager()
               .registerPlayerPlaceholder(identifier, p -> PlaceholderAPI.setPlaceholders((Player)p.getPlayer(), identifier));
         }
      }
   }

   public void registerSyncPlaceholder(@NotNull String identifier) {
      String syncedPlaceholder = "%" + identifier.substring(6);
      PlayerPlaceholderImpl[] ppl = new PlayerPlaceholderImpl[1];
      ppl[0] = TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(identifier, p -> {
         Bukkit.getScheduler().runTask(this.plugin, () -> {
            long time = System.nanoTime();
            ppl[0].updateValue(p, this.placeholderAPI ? PlaceholderAPI.setPlaceholders((Player)p.getPlayer(), syncedPlaceholder) : identifier);
            long totalTime = System.nanoTime() - time;
            TAB.getInstance().getCPUManager().addPlaceholderTime(identifier, totalTime);
            TAB.getInstance().getCpu().addTime(TAB.getInstance().getPlaceholderManager().getFeatureName(), "Phase #2 - Requesting new values", totalTime);
         });
         return null;
      });
   }

   @Override
   public void logInfo(@NotNull TabComponent message) {
      Bukkit.getConsoleSender().sendMessage("[TAB] " + this.toBukkitFormat(message));
   }

   @Override
   public void logWarn(@NotNull TabComponent message) {
      Bukkit.getConsoleSender().sendMessage("§c[TAB] [WARN] " + this.toBukkitFormat(message));
   }

   @NotNull
   @Override
   public String getServerVersionInfo() {
      return "[Bukkit] " + Bukkit.getName() + " - " + Bukkit.getBukkitVersion().split("-")[0] + " (" + this.serverPackage + ")";
   }

   @Override
   public void registerListener() {
      Bukkit.getPluginManager().registerEvents(new BukkitEventListener(), this.plugin);
   }

   @Override
   public void registerCommand() {
      PluginCommand command = Bukkit.getPluginCommand(this.getCommand());
      if (command != null) {
         BukkitTabCommand cmd = new BukkitTabCommand();
         command.setExecutor(cmd);
         command.setTabCompleter(cmd);
      } else {
         this.logWarn(new TabTextComponent("Failed to register command, is it defined in plugin.yml?", TabTextColor.RED));
      }
   }

   @Override
   public void startMetrics() {
      Metrics metrics = new Metrics(this.plugin, 5304);
      metrics.addCustomChart(new SimplePie("permission_system", () -> TAB.getInstance().getGroupManager().getPermissionPlugin()));
      String version = this.serverVersion == ProtocolVersion.UNKNOWN ? "Unknown" : "1." + this.serverVersion.getMinorVersion() + ".x";
      metrics.addCustomChart(new SimplePie("server_version", () -> version));
   }

   @NotNull
   @Override
   public File getDataFolder() {
      return this.plugin.getDataFolder();
   }

   @NotNull
   @Override
   public Object convertComponent(@NotNull TabComponent component) {
      return this.implementationProvider.getComponentConverter().convert(component);
   }

   @NotNull
   @Override
   public Scoreboard createScoreboard(@NotNull TabPlayer player) {
      return this.implementationProvider.newScoreboard((BukkitTabPlayer)player);
   }

   @NotNull
   @Override
   public BossBar createBossBar(@NotNull TabPlayer player) {
      if (AdventureBossBar.isAvailable() && Audience.class.isAssignableFrom(Player.class)) {
         return new AdventureBossBar(player);
      } else if (BukkitBossBar.isAvailable()) {
         return new BukkitBossBar((BukkitTabPlayer)player);
      } else {
         return player.getVersion().getMinorVersion() >= 9 ? new ViaBossBar((BukkitTabPlayer)player) : new DummyBossBar();
      }
   }

   @NotNull
   @Override
   public TabList createTabList(@NotNull TabPlayer player) {
      return this.implementationProvider.newTabList((BukkitTabPlayer)player);
   }

   @Override
   public boolean supportsScoreboards() {
      return true;
   }

   @Override
   public boolean supportsListOrder() {
      return this.serverVersion.getNetworkId() >= ProtocolVersion.V1_21_2.getNetworkId();
   }

   @Override
   public boolean isSafeFromPacketEventsBug() {
      return this.serverVersion.getMinorVersion() >= 13;
   }

   @Override
   public void registerCustomCommand(@NotNull String commandName, @NotNull final Consumer<TabPlayer> function) {
      Command cmd = new BukkitCommand(commandName) {
         public boolean execute(@NotNull CommandSender commandSender, @NotNull String alias, @NotNull String[] args) {
            if (commandSender instanceof ConsoleCommandSender) {
               commandSender.sendMessage(
                  BukkitPlatform.this.toBukkitFormat(TabComponent.fromColoredText(TAB.getInstance().getConfiguration().getMessages().getCommandOnlyFromGame()))
               );
               return false;
            }

            TabPlayer p = TAB.getInstance().getPlayer(((Player)commandSender).getUniqueId());
            if (p == null) {
               return false;
            }

            function.accept(p);
            return false;
         }
      };
      this.commandMap.register(commandName, cmd);
      this.customCommands.add(cmd);
   }

   @Override
   public void unregisterAllCustomCommands() {
      for (Command command : this.customCommands) {
         this.knownCommands.remove(command.getName());
         this.knownCommands.remove(command.getName() + ":" + command.getName());
         command.unregister(this.commandMap);
      }
   }

   @NotNull
   @Override
   public GroupManager detectPermissionPlugin() {
      if (LuckPermsHook.getInstance().isInstalled()) {
         return new GroupManager("LuckPerms", LuckPermsHook.getInstance().getGroupFunction());
      }

      if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
         RegisteredServiceProvider<Permission> provider = Bukkit.getServicesManager().getRegistration(Permission.class);
         if (provider != null && !((Permission)provider.getProvider()).getName().equals("SuperPerms")) {
            return new GroupManager(
               ((Permission)provider.getProvider()).getName(), p -> ((Permission)provider.getProvider()).getPrimaryGroup((Player)p.getPlayer())
            );
         }
      }

      return new GroupManager("None", p -> "NONE");
   }

   @Override
   public double getTPS() {
      if (this.recentTps != null) {
         return this.recentTps[0];
      } else {
         return this.paperTps ? Bukkit.getTPS()[0] : -1.0;
      }
   }

   @Override
   public double getMSPT() {
      return this.paperMspt ? Bukkit.getAverageTickTime() : -1.0;
   }

   public void runSync(@NotNull Entity entity, @NotNull Runnable task) {
      Bukkit.getScheduler().runTask(this.plugin, task);
   }

   @NotNull
   public String toBukkitFormat(@NotNull TabComponent component) {
      StringBuilder sb = new StringBuilder();
      if (component.getModifier().getColor() != null) {
         if (this.serverVersion.supportsRGB()) {
            String hexCode = component.getModifier().getColor().getHexCode();
            sb.append('§')
               .append("x")
               .append('§')
               .append(hexCode.charAt(0))
               .append('§')
               .append(hexCode.charAt(1))
               .append('§')
               .append(hexCode.charAt(2))
               .append('§')
               .append(hexCode.charAt(3))
               .append('§')
               .append(hexCode.charAt(4))
               .append('§')
               .append(hexCode.charAt(5));
         } else {
            sb.append('§').append(component.getModifier().getColor().getLegacyColor().getCharacter());
         }
      }

      sb.append(component.getModifier().getMagicCodes());
      if (component instanceof TabTextComponent) {
         sb.append(((TabTextComponent)component).getText());
      } else if (component instanceof TabTranslatableComponent) {
         sb.append(((TabTranslatableComponent)component).getKey());
      } else if (component instanceof TabKeybindComponent) {
         sb.append(((TabKeybindComponent)component).getKeybind());
      } else {
         if (!(component instanceof TabObjectComponent)) {
            throw new IllegalStateException("Unexpected component type: " + component.getClass().getName());
         }

         sb.append(component.toLegacyText());
      }

      for (TabComponent extra : component.getExtra()) {
         sb.append(this.toBukkitFormat(extra));
      }

      return sb.toString();
   }

   @NotNull
   public Collection<? extends Player> getOnlinePlayers() {
      try {
         return this.modernOnlinePlayers ? Bukkit.getOnlinePlayers() : Arrays.asList((Player[])Bukkit.class.getMethod("getOnlinePlayers").invoke(null));
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   @NotNull
   @Generated
   public JavaPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public ProtocolVersion getServerVersion() {
      return this.serverVersion;
   }

   @Generated
   public boolean isPlaceholderAPI() {
      return this.placeholderAPI;
   }

   @Generated
   public double[] getRecentTps() {
      return this.recentTps;
   }

   @Generated
   public boolean isPaperTps() {
      return this.paperTps;
   }

   @Generated
   public boolean isPaperMspt() {
      return this.paperMspt;
   }

   @Nullable
   @Generated
   public String getServerPackage() {
      return this.serverPackage;
   }

   @NotNull
   @Generated
   public ImplementationProvider getImplementationProvider() {
      return this.implementationProvider;
   }

   @Generated
   public boolean isModernOnlinePlayers() {
      return this.modernOnlinePlayers;
   }

   @Generated
   public SimpleCommandMap getCommandMap() {
      return this.commandMap;
   }

   @Generated
   public Map<String, Command> getKnownCommands() {
      return this.knownCommands;
   }

   @Generated
   public List<Command> getCustomCommands() {
      return this.customCommands;
   }

   @Generated
   public void setImplementationProvider(@NotNull ImplementationProvider implementationProvider) {
      if (implementationProvider == null) {
         throw new NullPointerException("implementationProvider is marked non-null but is null");
      }

      this.implementationProvider = implementationProvider;
   }
}
