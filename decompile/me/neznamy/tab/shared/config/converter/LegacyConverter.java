package me.neznamy.tab.shared.config.converter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LegacyConverter {
   public void convert2810to290(@NonNull ConfigurationFile animations) {
      if (animations == null) {
         throw new NullPointerException("animations is marked non-null but is null");
      }

      if (animations.getValues().size() == 1 && animations.getValues().containsKey("animations")) {
         TAB.getInstance().getPlatform().logInfo(new TabTextComponent("Performing configuration conversion from 2.8.10 to 2.9.0", TabTextColor.YELLOW));
         animations.setValues(animations.getMap("animations"));
         animations.save();
      }
   }

   public void convert292to300(@NonNull ConfigurationFile currentConfig) throws IOException {
      if (currentConfig == null) {
         throw new NullPointerException("currentConfig is marked non-null but is null");
      }

      if (currentConfig.hasConfigOption("change-nametag-prefix-suffix")) {
         TAB.getInstance().getPlatform().logInfo(new TabTextComponent("Performing configuration conversion from 2.9.2 to 3.0.0", TabTextColor.YELLOW));
         File folder = TAB.getInstance().getDataFolder();
         this.moveOldFiles();
         Files.createFile(new File(folder, "groups.yml").toPath());
         Files.createFile(new File(folder, "users.yml").toPath());
         Files.createFile(new File(folder, "config.yml").toPath());
         ConfigurationFile groups = new YamlConfigurationFile(null, new File(folder, "groups.yml"));
         ConfigurationFile users = new YamlConfigurationFile(null, new File(folder, "users.yml"));
         File oldConfigsFolder = new File(folder, "old_configs");
         File oldAnimations = new File(oldConfigsFolder, "animations.yml");
         if (oldAnimations.exists()) {
            Files.copy(oldAnimations.toPath(), new File(folder, "animations.yml").toPath());
         }

         File premiumFile = new File(oldConfigsFolder, "premiumconfig.yml");
         ConfigurationFile premiumConfig = premiumFile.exists() ? new YamlConfigurationFile(null, premiumFile) : null;
         File bossBarFile = new File(oldConfigsFolder, "bossbar.yml");
         if (!bossBarFile.exists()) {
            throw new IllegalStateException("Failed to convert configuration to v3: File bossbar.yml does not exist");
         }

         ConfigurationFile bossBar = new YamlConfigurationFile(null, bossBarFile);
         ConfigurationFile oldConfig = new YamlConfigurationFile(null, new File(oldConfigsFolder, "config.yml"));
         ConfigurationFile newConfig = new YamlConfigurationFile(null, new File(folder, "config.yml"));
         this.convertHeaderFooter(oldConfig, newConfig);
         this.convertTabListFormatting(oldConfig, newConfig);
         this.convertTeamOptions(oldConfig, newConfig, premiumConfig);
         this.convertYellowNumber(oldConfig, newConfig);
         this.convertBelowName(oldConfig, newConfig);
         this.convertBossBar(bossBar, newConfig);
         if (premiumConfig != null) {
            this.convertScoreboard(newConfig, premiumConfig);
         } else {
            this.createDefaultScoreboard(newConfig);
         }

         this.convertOtherOptions(oldConfig, newConfig, premiumConfig);
         this.convertGroupsAndUsers(oldConfig, groups, users);
         currentConfig.setValues(newConfig.getValues());
      }
   }

   private void moveOldFiles() throws IOException {
      File folder = TAB.getInstance().getDataFolder();
      File oldFolder = new File(folder, "old_configs");
      Files.createDirectories(oldFolder.toPath());

      for (File file : Objects.requireNonNull(folder.listFiles())) {
         if (file.isFile()) {
            Files.move(
               file.toPath(),
               new File(folder.getPath() + File.separator + "old_configs" + File.separator + file.getName()).toPath(),
               StandardCopyOption.REPLACE_EXISTING
            );
         }
      }
   }

   private void convertTeamOptions(@NonNull ConfigurationFile oldConfig, @NonNull ConfigurationFile newConfig, @Nullable ConfigurationFile premiumConfig) {
      if (oldConfig == null) {
         throw new NullPointerException("oldConfig is marked non-null but is null");
      }

      if (newConfig == null) {
         throw new NullPointerException("newConfig is marked non-null but is null");
      }

      newConfig.set("scoreboard-teams.enabled", oldConfig.getBoolean("change-nametag-prefix-suffix", true));
      newConfig.set("scoreboard-teams.invisible-nametags", oldConfig.getBoolean("invisible-nametags", false));
      newConfig.set("scoreboard-teams.enable-collision", oldConfig.getBoolean("enable-collision", true));
      newConfig.set(
         "scoreboard-teams.disable-in-worlds", oldConfig.getStringList("disable-features-in-worlds.nametag", Collections.singletonList("disabledworld"))
      );
      if (TAB.getInstance().getPlatform().isProxy()) {
         newConfig.set(
            "scoreboard-teams.disable-in-servers", oldConfig.getStringList("disable-features-in-servers.nametag", Collections.singletonList("disabledserver"))
         );
      }

      List<String> placeholderOrder = new ArrayList<>();
      String sortingType;
      String sortingPlaceholder;
      if (premiumConfig != null) {
         newConfig.set("scoreboard-teams.case-sensitive-sorting", premiumConfig.getBoolean("case-sensitive-sorting", true));
         sortingType = premiumConfig.getString("sorting-type", "GROUPS");
         sortingPlaceholder = premiumConfig.getString("sorting-placeholder", "%some_level_maybe?%");
         placeholderOrder = premiumConfig.getStringList("placeholder-order", Arrays.asList("value1", "value2"));
      } else {
         newConfig.set("scoreboard-teams.case-sensitive-sorting", true);
         sortingType = oldConfig.getBoolean("sort-players-by-permissions", false) ? "GROUP_PERMISSIONS" : "GROUPS";
         sortingPlaceholder = "";
      }

      List<String> sortingTypes = new ArrayList<>();

      for (String type : sortingType.split("_THEN_")) {
         if (type.equalsIgnoreCase("GROUPS") || type.equalsIgnoreCase("GROUP_PERMISSIONS")) {
            List<String> sortingList = oldConfig.getStringList(
               "group-sorting-priority-list", Arrays.asList("owner", "admin", "mod", "helper", "builder", "premium", "player", "default")
            );
            StringBuilder groups = new StringBuilder(("GROUP_PERMISSIONS".equals(type) ? "PERMISSIONS" : "GROUPS") + ":");

            for (String group : sortingList) {
               groups.append("GROUP_PERMISSIONS".equals(type) ? "tab.sort." : "");
               groups.append(group.replace(" ", "|"));
               if (sortingList.indexOf(group) != sortingList.size() - 1) {
                  groups.append(",");
               }
            }

            sortingTypes.add(groups.toString());
         } else if (type.equalsIgnoreCase("PLACEHOLDER")) {
            sortingTypes.add("PLACEHOLDER:" + sortingPlaceholder + ":" + String.join(",", placeholderOrder));
         } else {
            sortingTypes.add(type + ":" + sortingPlaceholder);
         }
      }

      sortingTypes.add("PLACEHOLDER_A_TO_Z:%player%");
      newConfig.set("scoreboard-teams.sorting-types", sortingTypes);
   }

   private void convertTabListFormatting(@NonNull ConfigurationFile oldConfig, @NonNull ConfigurationFile newConfig) {
      if (oldConfig == null) {
         throw new NullPointerException("oldConfig is marked non-null but is null");
      }

      if (newConfig == null) {
         throw new NullPointerException("newConfig is marked non-null but is null");
      }

      newConfig.set("tablist-name-formatting.enabled", oldConfig.getBoolean("change-tablist-prefix-suffix", true));
      newConfig.set(
         "tablist-name-formatting.disable-in-worlds",
         oldConfig.getStringList("disable-features-in-worlds.tablist-names", Collections.singletonList("disabledworld"))
      );
      if (TAB.getInstance().getPlatform().isProxy()) {
         newConfig.set(
            "tablist-name-formatting.disable-in-servers",
            oldConfig.getStringList("disable-features-in-servers.tablist-names", Collections.singletonList("disabledserver"))
         );
      }
   }

   private void convertYellowNumber(@NonNull ConfigurationFile oldConfig, @NonNull ConfigurationFile newConfig) {
      if (oldConfig == null) {
         throw new NullPointerException("oldConfig is marked non-null but is null");
      }

      if (newConfig == null) {
         throw new NullPointerException("newConfig is marked non-null but is null");
      }

      newConfig.set("yellow-number-in-tablist.enabled", !oldConfig.getString("yellow-number-in-tablist", "%ping%").isEmpty());
      newConfig.set("yellow-number-in-tablist.value", oldConfig.getString("yellow-number-in-tablist", "%ping%"));
      newConfig.set(
         "yellow-number-in-tablist.disable-in-worlds",
         oldConfig.getStringList("disable-features-in-worlds.yellow-number", Collections.singletonList("disabledworld"))
      );
      if (TAB.getInstance().getPlatform().isProxy()) {
         newConfig.set(
            "yellow-number-in-tablist.disable-in-servers",
            oldConfig.getStringList("disable-features-in-servers.yellow-number", Collections.singletonList("disabledserver"))
         );
      }
   }

   private void convertBelowName(@NonNull ConfigurationFile oldConfig, @NonNull ConfigurationFile newConfig) {
      if (oldConfig == null) {
         throw new NullPointerException("oldConfig is marked non-null but is null");
      }

      if (newConfig == null) {
         throw new NullPointerException("newConfig is marked non-null but is null");
      }

      newConfig.set("belowname-objective", oldConfig.getMap("classic-vanilla-belowname"));
      newConfig.set(
         "belowname-objective.disable-in-worlds", oldConfig.getStringList("disable-features-in-worlds.belowname", Collections.singletonList("disabledworld"))
      );
      if (TAB.getInstance().getPlatform().isProxy()) {
         newConfig.set(
            "belowname-objective.disable-in-servers",
            oldConfig.getStringList("disable-features-in-servers.belowname", Collections.singletonList("disabledserver"))
         );
      }
   }

   private void convertBossBar(@NonNull ConfigurationFile bossBar, @NonNull ConfigurationFile newConfig) {
      if (bossBar == null) {
         throw new NullPointerException("bossBar is marked non-null but is null");
      }

      if (newConfig == null) {
         throw new NullPointerException("newConfig is marked non-null but is null");
      }

      newConfig.set("bossbar.enabled", bossBar.getBoolean("bossbar-enabled", false));
      newConfig.set("bossbar.toggle-command", bossBar.getString("bossbar-toggle-command", "/bossbar"));
      newConfig.set("bossbar.remember-toggle-choice", bossBar.getBoolean("remember-toggle-choice", false));
      newConfig.set("bossbar.hidden-by-default", bossBar.getBoolean("hidden-by-default", false));
      Map<Object, Map<String, Object>> bars = bossBar.getMap("bars");
      Map<String, List<Object>> perWorldBossBars = bossBar.getMap("per-world");
      List<Object> activeBossBars = new ArrayList<>(bossBar.getStringList("default-bars", new ArrayList<>()));
      String separator = TAB.getInstance().getPlatform().isProxy() ? "server" : "world";

      for (Entry<String, List<Object>> entry : perWorldBossBars.entrySet()) {
         for (Object bar : entry.getValue()) {
            if (bars.containsKey(bar)) {
               activeBossBars.add(bar);
               if (bars.get(bar).containsKey("display-condition")) {
                  bars.get(bar).put("display-condition", bars.get(bar).get("display-condition") + ";%" + separator + "%=" + entry.getKey());
               } else {
                  bars.get(bar).put("display-condition", "%" + separator + "%=" + entry.getKey());
               }
            }
         }
      }

      for (Entry<Object, Map<String, Object>> entry : bars.entrySet()) {
         entry.getValue().put("announcement-bar", !activeBossBars.contains(entry.getKey()));
         entry.getValue().remove("permission-required");
      }

      newConfig.set("bossbar.default-bars", null);
      newConfig.set("bossbar.bars", bars);
   }

   private void convertScoreboard(@NonNull ConfigurationFile newConfig, @NonNull ConfigurationFile premiumConfig) {
      if (newConfig == null) {
         throw new NullPointerException("newConfig is marked non-null but is null");
      }

      if (premiumConfig == null) {
         throw new NullPointerException("premiumConfig is marked non-null but is null");
      }

      String separator = TAB.getInstance().getPlatform().isProxy() ? "server" : "world";
      newConfig.set("scoreboard", premiumConfig.getObject("scoreboard"));
      newConfig.set("scoreboard.permission-required-to-toggle", null);
      Map<String, Map<String, Object>> scoreboards = premiumConfig.getMap("scoreboards");
      Map<String, String> perWorldScoreboards = premiumConfig.getMap("scoreboard.per-world");
      newConfig.set("scoreboard.default-scoreboard", null);
      newConfig.set("scoreboard.per-world", null);

      for (Entry<String, String> entry : perWorldScoreboards.entrySet()) {
         String world = entry.getKey();
         String sb = entry.getValue();
         if (scoreboards.containsKey(sb)) {
            Map<String, Object> scoreboard = scoreboards.get(sb);
            if (scoreboard.containsKey("display-condition")) {
               scoreboard.put("display-condition", scoreboards.get(sb).get("display-condition") + ";%" + separator + "%=" + world);
            } else {
               scoreboard.put("display-condition", "%" + separator + "%=" + world);
               scoreboards.remove(sb);
               Map<String, Map<String, Object>> reordered = new HashMap<>();
               reordered.put(sb, scoreboard);
               reordered.putAll(scoreboards);
               scoreboards = reordered;
            }
         }
      }

      newConfig.set("scoreboard.scoreboards", scoreboards);
   }

   private void createDefaultScoreboard(@NonNull ConfigurationFile newConfig) {
      if (newConfig == null) {
         throw new NullPointerException("newConfig is marked non-null but is null");
      }

      newConfig.set("scoreboard.enabled", false);
      newConfig.set("scoreboard.toggle-command", "/sb");
      newConfig.set("scoreboard.remember-toggle-choice", false);
      newConfig.set("scoreboard.hidden-by-default", false);
      newConfig.set("scoreboard.use-numbers", false);
      newConfig.set("scoreboard.static-number", 0);
      newConfig.set("scoreboard.delay-on-join-milliseconds", 0);
      newConfig.set("scoreboard.scoreboards.admin.display-condition", "permission:tab.scoreboard.admin");
      newConfig.set("scoreboard.scoreboards.admin.title", "Admin scoreboard");
      newConfig.set(
         "scoreboard.scoreboards.admin.lines",
         Arrays.asList(
            "%animation:MyAnimation1%",
            "&6Online:",
            "* &eOnline&7: &f%online%&7",
            "* &eCurrent World&7: &f%worldonline%",
            "* &eStaff&7: &f%staffonline%",
            " ",
            "&6Server Info:",
            "* &bTPS&7: %tps%",
            "* &bUptime&7: &f%server_uptime%",
            "* &bMemory&7: &f%memory-used%&7/&4%memory-max%",
            "%animation:MyAnimation1%"
         )
      );
      newConfig.set("scoreboard.scoreboards.scoreboard1.title", "Default");
      newConfig.set(
         "scoreboard.scoreboards.scoreboard1.lines",
         Arrays.asList(
            "%animation:MyAnimation1%",
            "&6My Stats:",
            "* &eKills&7: &f%statistic_player_kills%",
            "* &eDeaths&7: &f%statistic_deaths%",
            "* &eHealth&7: &f%health%",
            " ",
            "&6Personal Info:",
            "* &bRank&7: &f%group%",
            "* &bPing&7: &f%ping%&7ms",
            "* &bWorld&7: &f%world%",
            "%animation:MyAnimation1%"
         )
      );
   }

   private void convertHeaderFooter(@NonNull ConfigurationFile oldConfig, @NonNull ConfigurationFile newConfig) {
      if (oldConfig == null) {
         throw new NullPointerException("oldConfig is marked non-null but is null");
      }

      if (newConfig == null) {
         throw new NullPointerException("newConfig is marked non-null but is null");
      }

      newConfig.set("header-footer.enabled", oldConfig.getBoolean("enable-header-footer", true));
      newConfig.set("header-footer.header", oldConfig.getStringList("header", Collections.emptyList()));
      newConfig.set("header-footer.footer", oldConfig.getStringList("footer", Collections.emptyList()));
      newConfig.set(
         "header-footer.disable-in-worlds", oldConfig.getStringList("disable-features-in-worlds.header-footer", Collections.singletonList("disabledworld"))
      );
      if (TAB.getInstance().getPlatform().isProxy()) {
         newConfig.set(
            "header-footer.disable-in-servers",
            oldConfig.getStringList("disable-features-in-servers.header-footer", Collections.singletonList("disabledserver"))
         );
      }

      String separator = TAB.getInstance().getPlatform().isProxy() ? "server" : "world";
      Map<String, Map<String, Object>> perWorldSettings = oldConfig.getMap("per-" + separator + "-settings");
      Map<String, Object> headerFooterMap = new LinkedHashMap<>();

      for (Entry<String, Map<String, Object>> worldEntry : new LinkedHashMap<>(perWorldSettings).entrySet()) {
         Map<String, Object> headerFooter = new LinkedHashMap<>();

         for (Entry<String, Object> propertyValueEntry : new LinkedHashMap<>(worldEntry.getValue()).entrySet()) {
            if (propertyValueEntry.getKey().equalsIgnoreCase("header") || propertyValueEntry.getKey().equalsIgnoreCase("footer")) {
               headerFooter.put(propertyValueEntry.getKey(), propertyValueEntry.getValue());
            }
         }

         headerFooterMap.put(this.translateWorldGroup(oldConfig, worldEntry.getKey()), headerFooter);
      }

      newConfig.set("header-footer.per-" + separator, headerFooterMap);
   }

   private void convertOtherOptions(@NonNull ConfigurationFile oldConfig, @NonNull ConfigurationFile newConfig, @Nullable ConfigurationFile premiumConfig) {
      if (oldConfig == null) {
         throw new NullPointerException("oldConfig is marked non-null but is null");
      }

      if (newConfig == null) {
         throw new NullPointerException("newConfig is marked non-null but is null");
      }

      newConfig.set("prevent-spectator-effect.enabled", oldConfig.getBoolean("do-not-move-spectators", false));
      Map<String, Object> placeholders = oldConfig.getMap("placeholders");
      if (premiumConfig != null) {
         newConfig.set("placeholder-output-replacements", premiumConfig.getMap("placeholder-output-replacements"));
         newConfig.set("conditions", premiumConfig.getMap("conditions"));
      } else {
         newConfig.set("placeholder-output-replacements.%essentials_vanished%.yes", "&7| Vanished");
         newConfig.set("placeholder-output-replacements.%essentials_vanished%.no", "");
         newConfig.set("conditions.nick.conditions", Collections.singletonList("%player%=%essentials_nickname%"));
         newConfig.set("conditions.nick.yes", "%player%");
         newConfig.set("conditions.nick.no", "~%essentials_nickname%");
      }

      newConfig.set("placeholders", placeholders);
      newConfig.set("placeholderapi-refresh-intervals", oldConfig.getMap("placeholderapi-refresh-intervals"));
      newConfig.set("assign-groups-by-permissions", oldConfig.getBoolean("assign-groups-by-permissions", false));
      newConfig.set(
         "primary-group-finding-list", oldConfig.getStringList("primary-group-finding-list", Arrays.asList("Owner", "Admin", "Mod", "Helper", "default"))
      );
      newConfig.set("debug", oldConfig.getBoolean("debug", false));
      newConfig.set("mysql.enabled", false);
      newConfig.set("mysql.host", "127.0.0.1");
      newConfig.set("mysql.port", 3306);
      newConfig.set("mysql.database", "tab");
      newConfig.set("mysql.username", "user");
      newConfig.set("mysql.password", "password");
      if (TAB.getInstance().getPlatform().isProxy()) {
         newConfig.set("global-playerlist", oldConfig.getMap("global-playerlist"));
         newConfig.set("global-playerlist.update-latency", false);
         newConfig.set("use-bukkit-permissions-manager", false);
      } else {
         newConfig.set("per-world-playerlist", oldConfig.getMap("per-world-playerlist"));
      }
   }

   private void convertGroupsAndUsers(@NonNull ConfigurationFile oldConfig, @NonNull ConfigurationFile groups, @NonNull ConfigurationFile users) {
      if (oldConfig == null) {
         throw new NullPointerException("oldConfig is marked non-null but is null");
      }

      if (groups == null) {
         throw new NullPointerException("groups is marked non-null but is null");
      }

      if (users == null) {
         throw new NullPointerException("users is marked non-null but is null");
      }

      groups.setValues(oldConfig.getMap("Groups"));
      users.setValues(oldConfig.getMap("Users"));
      String separator = TAB.getInstance().getPlatform().isProxy() ? "server" : "world";
      Map<String, Map<String, Object>> perWorldSettings = oldConfig.getMap("per-" + separator + "-settings");
      Map<String, Object> groupMap = new LinkedHashMap<>();
      Map<String, Object> userMap = new LinkedHashMap<>();
      Map<String, Map<String, Object>> worldMap = new LinkedHashMap<>(perWorldSettings);

      for (Entry<String, Map<String, Object>> worldEntry : worldMap.entrySet()) {
         for (Entry<String, Object> entry2 : new LinkedHashMap<>(worldEntry.getValue()).entrySet()) {
            if (entry2.getKey().equalsIgnoreCase("Groups")) {
               groupMap.put(this.translateWorldGroup(oldConfig, worldEntry.getKey()), entry2.getValue());
            } else if (entry2.getKey().equalsIgnoreCase("Users")) {
               userMap.put(this.translateWorldGroup(oldConfig, worldEntry.getKey()), entry2.getValue());
            }
         }
      }

      groups.set("per-" + separator, groupMap);
      groups.set("_DEFAULT_", groups.getMap("_OTHER_"));
      groups.set("_OTHER_", null);
      users.set("per-" + separator, userMap);

      for (Object world : groups.getMap("per-" + separator).keySet()) {
         String gPath = "per-" + separator + "." + world;
         if (groups.hasConfigOption(gPath + "._OTHER_")) {
            groups.set(gPath + "." + "_DEFAULT_", groups.getObject(gPath + "._OTHER_"));
            groups.set(gPath + "._OTHER_", null);
         }
      }
   }

   @NotNull
   private String translateWorldGroup(@NonNull ConfigurationFile oldConfig, @NonNull String group) {
      if (oldConfig == null) {
         throw new NullPointerException("oldConfig is marked non-null but is null");
      }

      if (group == null) {
         throw new NullPointerException("group is marked non-null but is null");
      }

      String oldSeparator = oldConfig.getString("multi-world-separator", "-");
      return group.replace(oldSeparator, ";");
   }

   public void convert301to302(@NonNull ConfigurationFile config) {
      if (config == null) {
         throw new NullPointerException("config is marked non-null but is null");
      }

      if (config.removeOption("placeholders.remove-strings")) {
         TAB.getInstance().getPlatform().logInfo(new TabTextComponent("Performing configuration conversion from 3.0.1 to 3.0.2", TabTextColor.YELLOW));
      }
   }

   public void convert332to400(@NonNull ConfigurationFile config) throws IOException {
      if (config == null) {
         throw new NullPointerException("config is marked non-null but is null");
      }

      if (config.hasConfigOption("fix-pet-names")) {
         TAB.getInstance().getPlatform().logInfo(new TabTextComponent("Performing configuration conversion from 3.3.2 to 4.0.0", TabTextColor.YELLOW));
         config.set("fix-pet-names", null);
         config.set("bossbar.disable-in-worlds", null);
         config.set("bossbar.disable-in-servers", null);
         config.set("scoreboard.disable-in-worlds", null);
         config.set("scoreboard.disable-in-servers", null);
         config.set("remove-ghost-players", null);
         config.set("global-playerlist.fill-profile-key", null);
      }

      Map<Object, Object> intervals = config.getMap("placeholderapi-refresh-intervals");
      boolean updated = false;

      for (Entry<?, ?> entry : new ArrayList<>(intervals.entrySet())) {
         Object value = entry.getValue();
         if (value instanceof Map) {
            intervals.remove(entry.getKey());
            intervals.putAll((Map<? extends Object, ? extends Object>)value);
            updated = true;
         }
      }

      if (updated) {
         config.save();
      }

      File layoutFile = new File(TAB.getInstance().getDataFolder(), "layout.yml");
      if (layoutFile.exists()) {
         ConfigurationFile layout = new YamlConfigurationFile(null, layoutFile);
         config.set("layout", layout.getValues());
         Files.delete(layoutFile.toPath());
      }

      Consumer<Map<String, Object>> disabledConditionConverter = map -> {
         List<String> newConditions = new ArrayList<>();
         boolean update = false;
         if (map.containsKey("disable-in-worlds") && map.get("disable-in-worlds") instanceof List) {
            update = true;
            List<String> worlds = (List<String>)map.get("disable-in-worlds");
            newConditions.addAll(worlds.stream().map(world -> "%world%=" + world).collect(Collectors.toList()));
         }

         if (map.containsKey("disable-in-servers") && map.get("disable-in-servers") instanceof List) {
            update = true;
            List<String> worlds = (List<String>)map.get("disable-in-servers");
            newConditions.addAll(worlds.stream().map(server -> "%server%=" + server).collect(Collectors.toList()));
         }

         if (update) {
            map.remove("disable-in-worlds");
            map.remove("disable-in-servers");
            map.put("disable-condition", String.join("|", newConditions));
            config.save();
         }
      };
      disabledConditionConverter.accept(config.getMap("header-footer"));
      disabledConditionConverter.accept(config.getMap("tablist-name-formatting"));
      disabledConditionConverter.accept(config.getMap("scoreboard-teams"));
      disabledConditionConverter.accept(config.getMap("yellow-number-in-tablist"));
      disabledConditionConverter.accept(config.getMap("belowname-objective"));
      config.removeOption("layout.hide-vanished-players");
   }

   public void convert409to410(@NonNull ConfigurationFile config) {
      if (config == null) {
         throw new NullPointerException("config is marked non-null but is null");
      }

      if (config.hasConfigOption("yellow-number-in-tablist")) {
         TAB.getInstance().getPlatform().logInfo(new TabTextComponent("Performing configuration conversion from 4.0.9 to 4.1.0", TabTextColor.YELLOW));
         Map<Object, Object> section = config.getMap("yellow-number-in-tablist");
         section.put("fancy-value", "&7Ping: %ping%");
         config.set("yellow-number-in-tablist", null);
         config.set("playerlist-objective", section);
      }

      config.setIfMissing("belowname-objective.fancy-value-default", "NPC");
      config.setIfMissing("belowname-objective.fancy-value", "&c%health%");
      config.removeOption("tablist-name-formatting.align-tabsuffix-on-the-right");
      config.removeOption("tablist-name-formatting.character-width-overrides");
   }

   public void convert412to413(@NonNull ConfigurationFile config) {
      if (config == null) {
         throw new NullPointerException("config is marked non-null but is null");
      }

      if (config.setIfMissing("placeholders.register-tab-expansion", false)) {
         TAB.getInstance().getPlatform().logInfo(new TabTextComponent("Performing configuration conversion from 4.1.2 to 4.1.3", TabTextColor.YELLOW));
      }
   }

   public void convert419to500(@NonNull ConfigurationFile config) {
      if (config == null) {
         throw new NullPointerException("config is marked non-null but is null");
      }

      if (config.removeOption("scoreboard-teams.unlimited-nametag-mode")) {
         TAB.getInstance().getPlatform().logInfo(new TabTextComponent("Performing configuration conversion from 4.1.9 to 5.0.0", TabTextColor.YELLOW));
         config.removeOption("scoreboard.respect-other-plugins");
      }

      if (!config.hasConfigOption("global-playerlist.update-latency")) {
         config.set("global-playerlist.update-latency", false);
      }
   }

   public void convert501to502(@NonNull ConfigurationFile config) {
      if (config == null) {
         throw new NullPointerException("config is marked non-null but is null");
      }

      if (config.rename("belowname-objective.number", "belowname-objective.value")) {
         TAB.getInstance().getPlatform().logInfo(new TabTextComponent("Performing configuration conversion from 5.0.1 to 5.0.2", TabTextColor.YELLOW));
      }

      config.rename("belowname-objective.text", "belowname-objective.title");
      config.rename("belowname-objective.fancy-display-players", "belowname-objective.fancy-value");
      config.rename("belowname-objective.fancy-display-default", "belowname-objective.fancy-value-default");
   }

   public void convert507to510(@NonNull ConfigurationFile config) {
      if (config == null) {
         throw new NullPointerException("config is marked non-null but is null");
      }

      if (config.rename("enable-redisbungee-support", "proxy-support.enabled")) {
         TAB.getInstance().getPlatform().logInfo(new TabTextComponent("Performing configuration conversion from 5.0.7 to 5.1.0", TabTextColor.YELLOW));
         config.set("proxy-support.type", "PLUGIN");
         config.set("proxy-support.plugin.name", "RedisBungee");
         config.set("proxy-support.redis.url", "redis://:password@localhost:6379/0");
         config.set("proxy-support.rabbitmq.exchange", "plugin");
         config.set("proxy-support.rabbitmq.url", "amqp://guest:guest@localhost:5672/%2F");
      }

      config.rename("placeholderapi-refresh-intervals", "placeholder-refresh-intervals");
      config.setIfMissing("playerlist-objective.title", "TAB");
      config.setIfMissing(
         "playerlist-objective.render-type",
         Arrays.asList("%health%", "%player_health%", "%player_health_rounded%").contains(config.getString("playerlist-objective.value", ""))
            ? "HEARTS"
            : "INTEGER"
      );
   }

   public void convert521to522(@NonNull ConfigurationFile config) {
      if (config == null) {
         throw new NullPointerException("config is marked non-null but is null");
      }

      if (config.removeOption("scoreboard-teams.anti-override")) {
         TAB.getInstance().getPlatform().logInfo(new TabTextComponent("Performing configuration conversion from 5.2.1 to 5.2.2", TabTextColor.YELLOW));
      }

      config.removeOption("tablist-name-formatting.anti-override");
   }
}
