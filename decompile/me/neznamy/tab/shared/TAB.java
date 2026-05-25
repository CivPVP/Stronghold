package me.neznamy.tab.shared;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.api.tablist.HeaderFooterManager;
import me.neznamy.tab.api.tablist.SortingManager;
import me.neznamy.tab.api.tablist.TabListFormatManager;
import me.neznamy.tab.api.tablist.layout.LayoutManager;
import me.neznamy.tab.libs.org.yaml.snakeyaml.error.YAMLException;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.command.DisabledCommand;
import me.neznamy.tab.shared.command.TabCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.config.helper.ConfigHelper;
import me.neznamy.tab.shared.cpu.CpuManager;
import me.neznamy.tab.shared.data.DataManager;
import me.neznamy.tab.shared.event.EventBusImpl;
import me.neznamy.tab.shared.event.impl.TabLoadEventImpl;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.platform.Platform;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TAB extends TabAPI {
   private static TAB instance;
   private final Map<UUID, TabPlayer> data = new ConcurrentHashMap<>();
   private final Map<String, TabPlayer> playersByName = new ConcurrentHashMap<>();
   private final Map<UUID, TabPlayer> playersByTabListId = new ConcurrentHashMap<>();
   private volatile TabPlayer[] onlinePlayers = new TabPlayer[0];
   private TabCommand command;
   private final DisabledCommand disabledCommand = new DisabledCommand();
   private final Platform platform;
   private CpuManager cpu;
   private EventBusImpl eventBus;
   private final ErrorManager errorManager;
   private FeatureManager featureManager;
   private PlaceholderManagerImpl placeholderManager;
   private GroupManager groupManager;
   private Configs configuration;
   private boolean pluginDisabled;
   private final File dataFolder;
   private String brokenFile;
   private final ConfigHelper configHelper = new ConfigHelper();
   private DataManager dataManager;

   public static void create(@NotNull Platform platform) {
      instance = new TAB(platform);
      instance.load();
   }

   private TAB(@NotNull Platform platform) {
      this.platform = platform;
      this.dataFolder = platform.getDataFolder();
      this.errorManager = new ErrorManager(this.dataFolder);

      try {
         this.eventBus = new EventBusImpl();
      } catch (NoSuchMethodError var3) {
      }

      TabAPI.setInstance(this);
      platform.registerListener();
      platform.registerCommand();
      platform.startMetrics();
      if (platform instanceof ProxyPlatform) {
         ((ProxyPlatform)platform).registerChannel();
      }
   }

   public boolean isPlayerConnected(UUID tabListId) {
      return this.playersByTabListId.containsKey(tabListId);
   }

   @Nullable
   public TabPlayer getPlayerByTabListUUID(UUID tabListId) {
      return this.playersByTabListId.get(tabListId);
   }

   public String load() {
      try {
         long time = System.currentTimeMillis();
         this.cpu = new CpuManager();
         this.dataManager = new DataManager();
         this.configuration = new Configs();
         this.featureManager = new FeatureManager();
         this.placeholderManager = new PlaceholderManagerImpl(this.cpu, this.configuration.getConfig().getRefresh());
         this.featureManager.registerFeature("PlaceholderManager", this.placeholderManager);
         this.groupManager = this.platform.detectPermissionPlugin();
         this.platform.registerPlaceholders();
         this.featureManager.loadFeaturesFromConfig();
         this.pluginDisabled = false;
         this.platform.loadPlayers();
         this.command = new TabCommand();
         this.featureManager.load();

         for (TabPlayer p : this.onlinePlayers) {
            p.markAsLoaded(false);
         }

         if (this.eventBus != null) {
            this.eventBus.fire(TabLoadEventImpl.getInstance());
         }

         this.cpu.enable();
         this.configHelper.startup().printWarnCount();
         this.platform.logInfo(new TabTextComponent("Enabled in " + (System.currentTimeMillis() - time) + "ms", TabTextColor.GREEN));
         return this.configuration.getMessages().getReloadSuccess();
      } catch (YAMLException e) {
         this.platform.logWarn(new TabTextComponent("Did not enable due to a broken configuration file.", TabTextColor.RED));
         this.kill();
         return (this.configuration == null
               ? "&4Failed to reload, file %file% has broken syntax. Check console for more info."
               : this.configuration.getMessages().getReloadFailBrokenFile())
            .replace("%file%", this.brokenFile);
      } catch (Throwable e) {
         this.errorManager.criticalError("Failed to enable. Did you just invent a new way to break the plugin by misconfiguring it?", e);
         this.kill();
         return "&cFailed to enable due to an internal plugin error. Check console for more info.";
      }
   }

   public void unload() {
      if (!this.pluginDisabled) {
         try {
            long time = System.currentTimeMillis();
            if (this.configuration.getMysql() != null) {
               this.configuration.getMysql().closeConnection();
            }

            this.featureManager.unload();
            this.platform.logInfo(new TabTextComponent("Disabled in " + (System.currentTimeMillis() - time) + "ms", TabTextColor.GREEN));
         } catch (Throwable e) {
            this.errorManager.criticalError("Failed to disable", e);
         }

         this.kill();
      }
   }

   private void kill() {
      this.pluginDisabled = true;
      this.data.clear();
      this.playersByName.clear();
      this.playersByTabListId.clear();
      this.onlinePlayers = new TabPlayer[0];
      this.cpu.cancelAllTasks();
   }

   public void addPlayer(@NotNull TabPlayer player) {
      this.data.put(player.getUniqueId(), player);
      this.playersByName.put(player.getName(), player);
      this.playersByTabListId.put(player.getTablistId(), player);
      this.onlinePlayers = this.data.values().toArray(new TabPlayer[0]);
   }

   public void removePlayer(@NotNull TabPlayer player) {
      this.data.remove(player.getUniqueId());
      this.playersByName.remove(player.getName());
      this.playersByTabListId.remove(player.getTablistId());
      this.onlinePlayers = this.data.values().toArray(new TabPlayer[0]);
   }

   @NotNull
   public CpuManager getCPUManager() {
      return this.cpu;
   }

   @Nullable
   @Override
   public BossBarManager getBossBarManager() {
      return this.featureManager.getFeature("BossBar");
   }

   @Nullable
   @Override
   public ScoreboardManager getScoreboardManager() {
      return this.featureManager.getFeature("ScoreBoard");
   }

   @Nullable
   public NameTag getNameTagManager() {
      return this.featureManager.getFeature("NameTag16");
   }

   @Nullable
   public TabPlayer getPlayer(@NotNull String name) {
      return this.playersByName.get(name);
   }

   @Nullable
   public TabPlayer getPlayer(@NotNull UUID uniqueId) {
      return this.data.get(uniqueId);
   }

   @Nullable
   @Override
   public HeaderFooterManager getHeaderFooterManager() {
      return this.featureManager.getFeature("HeaderFooter");
   }

   @Nullable
   @Override
   public TabListFormatManager getTabListFormatManager() {
      return this.featureManager.getFeature("PlayerList");
   }

   @Nullable
   @Override
   public LayoutManager getLayoutManager() {
      return this.featureManager.getFeature("layout");
   }

   @Nullable
   @Override
   public SortingManager getSortingManager() {
      return this.featureManager.getFeature("sorting");
   }

   public void debug(@NotNull String message) {
      if (this.configuration != null && this.configuration.getConfig().isDebugMode()) {
         this.platform.logInfo(new TabTextComponent("[DEBUG] " + message, TabTextColor.BLUE));
      }
   }

   @Generated
   public Map<UUID, TabPlayer> getData() {
      return this.data;
   }

   @Generated
   public Map<String, TabPlayer> getPlayersByName() {
      return this.playersByName;
   }

   @Generated
   public Map<UUID, TabPlayer> getPlayersByTabListId() {
      return this.playersByTabListId;
   }

   @Generated
   public TabPlayer[] getOnlinePlayers() {
      return this.onlinePlayers;
   }

   @Generated
   public TabCommand getCommand() {
      return this.command;
   }

   @Generated
   public DisabledCommand getDisabledCommand() {
      return this.disabledCommand;
   }

   @Generated
   public Platform getPlatform() {
      return this.platform;
   }

   @Generated
   public CpuManager getCpu() {
      return this.cpu;
   }

   @Generated
   public EventBusImpl getEventBus() {
      return this.eventBus;
   }

   @Generated
   public ErrorManager getErrorManager() {
      return this.errorManager;
   }

   @Generated
   public FeatureManager getFeatureManager() {
      return this.featureManager;
   }

   @Generated
   public PlaceholderManagerImpl getPlaceholderManager() {
      return this.placeholderManager;
   }

   @Generated
   public GroupManager getGroupManager() {
      return this.groupManager;
   }

   @Generated
   public Configs getConfiguration() {
      return this.configuration;
   }

   @Generated
   public boolean isPluginDisabled() {
      return this.pluginDisabled;
   }

   @Generated
   public File getDataFolder() {
      return this.dataFolder;
   }

   @Generated
   public String getBrokenFile() {
      return this.brokenFile;
   }

   @Generated
   public ConfigHelper getConfigHelper() {
      return this.configHelper;
   }

   @Generated
   public DataManager getDataManager() {
      return this.dataManager;
   }

   @Generated
   public static TAB getInstance() {
      return instance;
   }

   @Generated
   public void setBrokenFile(String brokenFile) {
      this.brokenFile = brokenFile;
   }
}
