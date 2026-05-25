package me.neznamy.tab.shared.config.files;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.ComponentConfiguration;
import me.neznamy.tab.shared.config.converter.LegacyConverter;
import me.neznamy.tab.shared.config.converter.ModernConverter;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import me.neznamy.tab.shared.config.mysql.MySQLConfiguration;
import me.neznamy.tab.shared.features.PerWorldPlayerListConfiguration;
import me.neznamy.tab.shared.features.belowname.BelowNameConfiguration;
import me.neznamy.tab.shared.features.bossbar.BossBarConfiguration;
import me.neznamy.tab.shared.features.globalplayerlist.GlobalPlayerListConfiguration;
import me.neznamy.tab.shared.features.header.HeaderFooterConfiguration;
import me.neznamy.tab.shared.features.layout.LayoutConfiguration;
import me.neznamy.tab.shared.features.nametags.TeamConfiguration;
import me.neznamy.tab.shared.features.pingspoof.PingSpoofConfiguration;
import me.neznamy.tab.shared.features.playerlist.TablistFormattingConfiguration;
import me.neznamy.tab.shared.features.playerlistobjective.PlayerListObjectiveConfiguration;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardConfiguration;
import me.neznamy.tab.shared.features.sorting.SortingConfiguration;
import me.neznamy.tab.shared.placeholders.PlaceholderRefreshConfiguration;
import me.neznamy.tab.shared.placeholders.PlaceholderReplacementsConfiguration;
import me.neznamy.tab.shared.placeholders.PlaceholdersConfiguration;
import me.neznamy.tab.shared.placeholders.conditions.ConditionsSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Config {
   @NotNull
   private final ConfigurationFile config = new YamlConfigurationFile(
      this.getClass().getClassLoader().getResourceAsStream("config/config.yml"), new File(TAB.getInstance().getDataFolder(), "config.yml")
   );
   @Nullable
   private BelowNameConfiguration belowname;
   @Nullable
   private BossBarConfiguration bossbar;
   @NotNull
   private final ConditionsSection conditions;
   @Nullable
   private GlobalPlayerListConfiguration globalPlayerList;
   @Nullable
   private HeaderFooterConfiguration headerFooter;
   @Nullable
   private LayoutConfiguration layout;
   @Nullable
   private MySQLConfiguration mysql;
   @Nullable
   private PerWorldPlayerListConfiguration perWorldPlayerList;
   @Nullable
   private PingSpoofConfiguration pingSpoof;
   @NotNull
   private final PlaceholderRefreshConfiguration refresh;
   @NotNull
   private final PlaceholderReplacementsConfiguration replacements;
   @NotNull
   private final PlaceholdersConfiguration placeholders;
   @Nullable
   private PlayerListObjectiveConfiguration playerlistObjective;
   @Nullable
   private ScoreboardConfiguration scoreboard;
   @Nullable
   private SortingConfiguration sorting;
   @Nullable
   private TablistFormattingConfiguration tablistFormatting;
   @Nullable
   private TeamConfiguration teams;
   @NotNull
   private final ComponentConfiguration components;
   private final boolean preventSpectatorEffect = this.config.getBoolean("prevent-spectator-effect.enabled", false);
   private final boolean bukkitPermissions = TAB.getInstance().getPlatform().isProxy() && this.config.getBoolean("use-bukkit-permissions-manager", false);
   private final boolean debugMode = this.config.getBoolean("debug", false);
   private final boolean onlineUuidInTabList = this.config.getBoolean("use-online-uuid-in-tablist", true);
   private final boolean pipelineInjection = this.getSecretOption("pipeline-injection", true);
   @NotNull
   private final String serverName = this.getSecretOption("server-name", "N/A");
   private final int permissionRefreshInterval = this.config.getInt("permission-refresh-interval", 1000);
   private final boolean enableProxySupport = this.config.getBoolean("proxy-support.enabled", true);
   private final boolean packetEventsCompensation = this.config.getBoolean("compensate-for-packetevents-bug", false)
      && !TAB.getInstance().getPlatform().isSafeFromPacketEventsBug();
   private final boolean groupsByPermissions = this.config.getBoolean("assign-groups-by-permissions", false);
   @NotNull
   private final List<String> primaryGroupFindingList = this.config
      .getStringList("primary-group-finding-list", Arrays.asList("Owner", "Admin", "Helper", "default"));

   public Config() throws IOException {
      LegacyConverter converter = new LegacyConverter();
      converter.convert292to300(this.config);
      converter.convert301to302(this.config);
      converter.convert332to400(this.config);
      converter.convert409to410(this.config);
      converter.convert412to413(this.config);
      converter.convert419to500(this.config);
      converter.convert501to502(this.config);
      converter.convert507to510(this.config);
      converter.convert521to522(this.config);
      ModernConverter modernConverter = new ModernConverter();
      modernConverter.convert(this.config);
      this.conditions = ConditionsSection.fromSection(this.config.getConfigurationSection("conditions"));
      this.refresh = PlaceholderRefreshConfiguration.fromSection(this.config.getConfigurationSection("placeholder-refresh-intervals"));
      this.replacements = PlaceholderReplacementsConfiguration.fromSection(this.config.getConfigurationSection("placeholder-output-replacements"));
      this.placeholders = PlaceholdersConfiguration.fromSection(this.config.getConfigurationSection("placeholders"));
      this.components = ComponentConfiguration.fromSection(this.config.getConfigurationSection("components"));
      if (this.config.getBoolean("belowname-objective.enabled", false)) {
         this.belowname = BelowNameConfiguration.fromSection(this.config.getConfigurationSection("belowname-objective"));
      }

      if (this.config.getBoolean("bossbar.enabled", false)) {
         this.bossbar = BossBarConfiguration.fromSection(this.config.getConfigurationSection("bossbar"));
      }

      if (this.config.getBoolean("global-playerlist.enabled", false)) {
         this.globalPlayerList = GlobalPlayerListConfiguration.fromSection(this.config.getConfigurationSection("global-playerlist"));
      }

      if (this.config.getBoolean("header-footer.enabled", true)) {
         this.headerFooter = HeaderFooterConfiguration.fromSection(this.config.getConfigurationSection("header-footer"));
      }

      if (this.config.getBoolean("layout.enabled", false)) {
         this.layout = LayoutConfiguration.fromSection(this.config.getConfigurationSection("layout"));
      }

      if (this.config.getBoolean("mysql.enabled", false)) {
         this.mysql = MySQLConfiguration.fromSection(this.config.getConfigurationSection("mysql"));
      }

      if (this.config.getBoolean("per-world-playerlist.enabled", false)) {
         this.perWorldPlayerList = PerWorldPlayerListConfiguration.fromSection(this.config.getConfigurationSection("per-world-playerlist"));
      }

      if (this.config.getBoolean("ping-spoof.enabled", false)) {
         this.pingSpoof = PingSpoofConfiguration.fromSection(this.config.getConfigurationSection("ping-spoof"));
      }

      if (this.config.getBoolean("playerlist-objective.enabled", true)) {
         this.playerlistObjective = PlayerListObjectiveConfiguration.fromSection(this.config.getConfigurationSection("playerlist-objective"));
      }

      if (this.config.getBoolean("scoreboard.enabled", false)) {
         this.scoreboard = ScoreboardConfiguration.fromSection(this.config.getConfigurationSection("scoreboard"));
      }

      if (this.config.getBoolean("scoreboard-teams.enabled", true) || this.config.getBoolean("layout.enabled", false)) {
         this.sorting = SortingConfiguration.fromSection(this.config.getConfigurationSection("scoreboard-teams"));
      }

      if (this.config.getBoolean("tablist-name-formatting.enabled", false)) {
         this.tablistFormatting = TablistFormattingConfiguration.fromSection(this.config.getConfigurationSection("tablist-name-formatting"));
      }

      if (this.config.getBoolean("scoreboard-teams.enabled", false)) {
         this.teams = TeamConfiguration.fromSection(this.config.getConfigurationSection("scoreboard-teams"));
      }

      if (this.layout != null) {
         if (this.perWorldPlayerList != null) {
            TAB.getInstance()
               .getConfigHelper()
               .startup()
               .startupWarn(
                  this.config.getFile(),
                  "Both per world playerlist and layout features are enabled, but layout makes per world playerlist redundant. Layout automatically works with all connected players and replaces real player entries with fake players, making per world playerlist completely useless as real players are pushed out of the playerlist. Disable per world playerlist for the same result, but with better performance."
               );
         }

         if (this.playerlistObjective != null) {
            TAB.getInstance()
               .getConfigHelper()
               .startup()
               .startupWarn(
                  this.config.getFile(),
                  "Layout feature breaks playerlist-objective feature, because it replaces real player with fake slots with different usernames for more reliable functionality. Disable playerlist-objective feature, as it will only look bad and consume resources."
               );
         }

         if (this.preventSpectatorEffect) {
            TAB.getInstance()
               .getConfigHelper()
               .hint(
                  this.config.getFile(),
                  "Layout feature automatically includes prevent-spectator-effect, therefore the feature can be disabled for better performance, as it is not needed at all (assuming it is configured to always display some layout)."
               );
         }

         if (this.globalPlayerList != null) {
            TAB.getInstance()
               .getConfigHelper()
               .startup()
               .startupWarn(
                  this.config.getFile(),
                  "Both global playerlist and layout features are enabled, but layout makes global playerlist redundant. Layout automatically works with all connected players on the proxy and replaces real player entries with fake players, making global playerlist completely useless. Disable global playerlist for the same result, but with better performance."
               );
         }
      }
   }

   @NotNull
   private <T> T getSecretOption(@NonNull String path, @NonNull T defaultValue) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      }

      if (defaultValue == null) {
         throw new NullPointerException("defaultValue is marked non-null but is null");
      }

      Object value = this.config.getObject(path);
      return (T)(value == null ? defaultValue : value);
   }

   @NotNull
   @Generated
   public ConfigurationFile getConfig() {
      return this.config;
   }

   @Nullable
   @Generated
   public BelowNameConfiguration getBelowname() {
      return this.belowname;
   }

   @Nullable
   @Generated
   public BossBarConfiguration getBossbar() {
      return this.bossbar;
   }

   @NotNull
   @Generated
   public ConditionsSection getConditions() {
      return this.conditions;
   }

   @Nullable
   @Generated
   public GlobalPlayerListConfiguration getGlobalPlayerList() {
      return this.globalPlayerList;
   }

   @Nullable
   @Generated
   public HeaderFooterConfiguration getHeaderFooter() {
      return this.headerFooter;
   }

   @Nullable
   @Generated
   public LayoutConfiguration getLayout() {
      return this.layout;
   }

   @Nullable
   @Generated
   public MySQLConfiguration getMysql() {
      return this.mysql;
   }

   @Nullable
   @Generated
   public PerWorldPlayerListConfiguration getPerWorldPlayerList() {
      return this.perWorldPlayerList;
   }

   @Nullable
   @Generated
   public PingSpoofConfiguration getPingSpoof() {
      return this.pingSpoof;
   }

   @NotNull
   @Generated
   public PlaceholderRefreshConfiguration getRefresh() {
      return this.refresh;
   }

   @NotNull
   @Generated
   public PlaceholderReplacementsConfiguration getReplacements() {
      return this.replacements;
   }

   @NotNull
   @Generated
   public PlaceholdersConfiguration getPlaceholders() {
      return this.placeholders;
   }

   @Nullable
   @Generated
   public PlayerListObjectiveConfiguration getPlayerlistObjective() {
      return this.playerlistObjective;
   }

   @Nullable
   @Generated
   public ScoreboardConfiguration getScoreboard() {
      return this.scoreboard;
   }

   @Nullable
   @Generated
   public SortingConfiguration getSorting() {
      return this.sorting;
   }

   @Nullable
   @Generated
   public TablistFormattingConfiguration getTablistFormatting() {
      return this.tablistFormatting;
   }

   @Nullable
   @Generated
   public TeamConfiguration getTeams() {
      return this.teams;
   }

   @NotNull
   @Generated
   public ComponentConfiguration getComponents() {
      return this.components;
   }

   @Generated
   public boolean isPreventSpectatorEffect() {
      return this.preventSpectatorEffect;
   }

   @Generated
   public boolean isBukkitPermissions() {
      return this.bukkitPermissions;
   }

   @Generated
   public boolean isDebugMode() {
      return this.debugMode;
   }

   @Generated
   public boolean isOnlineUuidInTabList() {
      return this.onlineUuidInTabList;
   }

   @Generated
   public boolean isPipelineInjection() {
      return this.pipelineInjection;
   }

   @NotNull
   @Generated
   public String getServerName() {
      return this.serverName;
   }

   @Generated
   public int getPermissionRefreshInterval() {
      return this.permissionRefreshInterval;
   }

   @Generated
   public boolean isEnableProxySupport() {
      return this.enableProxySupport;
   }

   @Generated
   public boolean isPacketEventsCompensation() {
      return this.packetEventsCompensation;
   }

   @Generated
   public boolean isGroupsByPermissions() {
      return this.groupsByPermissions;
   }

   @NotNull
   @Generated
   public List<String> getPrimaryGroupFindingList() {
      return this.primaryGroupFindingList;
   }
}
