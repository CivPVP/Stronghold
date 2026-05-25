package me.neznamy.tab.shared.command;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DebugCommand extends SubCommand {
   public DebugCommand() {
      super("debug", "tab.debug");
   }

   @Override
   public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
      TabPlayer analyzed = null;
      if (args.length > 0) {
         analyzed = TAB.getInstance().getPlayer(args[0]);
         if (analyzed == null) {
            this.sendMessage(sender, this.getMessages().getPlayerNotFound(args[0]));
            return;
         }
      }

      if (analyzed == null && sender != null) {
         analyzed = sender;
      }

      this.debug(sender, analyzed);
   }

   private void debug(@Nullable TabPlayer sender, @Nullable TabPlayer analyzed) {
      TAB tab = TAB.getInstance();
      String separator = "&7&m>-------------------------------<";
      this.sendMessage(sender, "&3[TAB] &a&lShowing debug information");
      this.sendMessage(sender, separator);
      this.sendMessage(sender, "&6Server version: &b" + tab.getPlatform().getServerVersionInfo());
      this.sendMessage(sender, "&6Plugin version: &b5.4.0");
      this.sendMessage(sender, "&6Permission plugin: &b" + TAB.getInstance().getGroupManager().getPermissionPlugin());
      this.sendMessage(sender, "&6Permission group choice logic: &b" + this.getGroupChoiceLogic());
      this.sendMessage(sender, "&6Sorting system: &b" + this.getSortingType());
      this.sendMessage(sender, "&6Storage type: &b" + (tab.getConfiguration().getGroups() instanceof ConfigurationFile ? "File" : "MySQL"));
      this.sendMessage(sender, separator);
      if (analyzed != null) {
         if (!analyzed.isLoaded()) {
            this.sendMessage(
               sender,
               "&cThe specified player is not loaded. This is either because player failed to load due to an error (see TAB's folder for errors.log file) or the plugin is overloaded (see /tab cpu)."
            );
         } else {
            this.sendMessage(sender, "&ePlayer: &a" + analyzed.getName());
            this.sendMessage(sender, "&eInternal UUID: &a" + analyzed.getUniqueId());
            this.sendMessage(sender, "&eOffline UUID: &a" + UUID.nameUUIDFromBytes(("OfflinePlayer:" + analyzed.getName()).getBytes(StandardCharsets.UTF_8)));
            this.sendMessage(sender, "&eTablist UUID: &a" + analyzed.getTablistId());
            this.sendMessage(sender, "&ePlayer version: &a" + analyzed.getVersion().getFriendlyName() + " (" + analyzed.getVersionId() + ")");
            if (analyzed instanceof ProxyTabPlayer) {
               char versionRequired = "tab:bridge-6".charAt("tab:bridge-6".length() - 1);
               this.sendMessage(
                  sender,
                  "&eBridge connection: "
                     + (
                        ((ProxyTabPlayer)analyzed).isBridgeConnected()
                           ? "&aConnected"
                           : "&cNot connected (requires Bridge version " + versionRequired + ".x.x installed)"
                     )
               );
            }

            this.sendMessage(sender, this.getGroup(analyzed));
            this.sendMessage(sender, this.getTeamName(analyzed));
            this.sendMessage(sender, this.getTeamNameNote(analyzed));
            if (tab.getFeatureManager().isFeatureEnabled("PlayerList")) {
               this.showProperty(sender, analyzed.tablistData.prefix, analyzed.tablistData.disabled.get());
               this.showProperty(sender, analyzed.tablistData.name, analyzed.tablistData.disabled.get());
               this.showProperty(sender, analyzed.tablistData.suffix, analyzed.tablistData.disabled.get());
            } else {
               this.sendMessage(sender, "&atabprefix: &cDisabled");
               this.sendMessage(sender, "&acustomtabname: &cDisabled");
               this.sendMessage(sender, "&atabsuffix: &cDisabled");
            }

            if (tab.getNameTagManager() != null) {
               this.showProperty(sender, analyzed.teamData.prefix, analyzed.teamData.disabled.get());
               this.showProperty(sender, analyzed.teamData.suffix, analyzed.teamData.disabled.get());
            } else {
               this.sendMessage(sender, "&atagprefix: &cDisabled");
               this.sendMessage(sender, "&atagsuffix: &cDisabled");
            }

            this.sendMessage(sender, separator);
         }
      }
   }

   @NotNull
   private String getGroupChoiceLogic() {
      return TAB.getInstance().getConfiguration().getConfig().isGroupsByPermissions() ? "Permissions" : "Primary group";
   }

   @NotNull
   private String getSortingType() {
      Sorting sorting = TAB.getInstance().getFeatureManager().getFeature("sorting");
      return sorting != null ? sorting.typesToString() : "&cDISABLED";
   }

   @NotNull
   private String getGroup(@NotNull TabPlayer analyzed) {
      if (TAB.getInstance().getConfiguration().getConfig().isGroupsByPermissions()) {
         if (analyzed.getGroup().equals("NONE")) {
            return "&cPlayer does not have tab.group.<name> permission for any of the listed groups";
         }

         String s = "&eHighest group permission: &8tab.group.&a" + analyzed.getGroup();
         if (analyzed.hasPermission("tab.testpermission")) {
            s = s + " &c| This user appears to have all permissions. Are they OP? &r";
         }

         return s;
      } else {
         return "&ePrimary permission group: &a" + analyzed.getGroup();
      }
   }

   @NotNull
   private String getTeamName(@NotNull TabPlayer analyzed) {
      Sorting sorting = TAB.getInstance().getFeatureManager().getFeature("sorting");
      if (sorting == null) {
         return "";
      } else {
         return TAB.getInstance().getNameTagManager() != null && analyzed.teamData.disabled.get()
            ? "&eTeam name: &cSorting is disabled in player's world/server"
            : "&eTeam name: &a"
               + (
                  TAB.getInstance().getFeatureManager().isFeatureEnabled("layout")
                     ? analyzed.sortingData.getFullTeamName()
                     : analyzed.sortingData.getShortTeamName()
               );
      }
   }

   @NotNull
   private String getTeamNameNote(@NotNull TabPlayer analyzed) {
      return TAB.getInstance().getNameTagManager() != null && analyzed.teamData.disabled.get() ? "" : "&eSorting note: &r" + analyzed.sortingData.teamNameNote;
   }

   private void showProperty(@Nullable TabPlayer sender, @NotNull Property property, boolean disabled) {
      if (disabled) {
         this.sendMessage(sender, "&a" + property.getName() + ": &cDisabled for player with condition");
      } else {
         this.sendMessage(
            sender,
            new TabTextComponent(
               "",
               Arrays.asList(
                  new TabTextComponent(property.getName() + ": ", TabTextColor.GREEN),
                  new TabTextComponent("\"", TabTextColor.YELLOW),
                  new TabTextComponent(property.getCurrentRawValue().replace('§', '&'), TabTextColor.WHITE),
                  new TabTextComponent("\" ", TabTextColor.YELLOW),
                  new TabTextComponent("(Source: " + property.getSource() + ")", TabTextColor.GRAY)
               )
            )
         );
      }
   }

   @NotNull
   @Override
   public List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
      return arguments.length == 1 ? this.getOnlinePlayers(arguments[0]) : new ArrayList<>();
   }
}
