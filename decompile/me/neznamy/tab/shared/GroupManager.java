package me.neznamy.tab.shared;

import java.util.function.Function;
import lombok.Generated;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.task.GroupRefreshTask;
import org.jetbrains.annotations.NotNull;

public class GroupManager {
   @NotNull
   private final String permissionPlugin;
   @NotNull
   private final Function<TabPlayer, String> groupFunction;
   private final Function<TabPlayer, String> detectGroup = TAB.getInstance().getConfiguration().getConfig().isGroupsByPermissions()
      ? this::getByPermission
      : this::getByPrimary;

   public GroupManager(@NotNull String permissionPlugin, @NotNull Function<TabPlayer, String> groupFunction) {
      this.permissionPlugin = permissionPlugin;
      this.groupFunction = groupFunction;
      TAB.getInstance()
         .getCpu()
         .getGroupRefreshingThread()
         .repeatTask(
            new TimedCaughtTask(TAB.getInstance().getCpu(), new GroupRefreshTask(this.detectGroup), "Permission group refreshing", "Periodic task"),
            TAB.getInstance().getConfiguration().getConfig().getPermissionRefreshInterval()
         );
   }

   @NotNull
   public String detectPermissionGroup(@NotNull TabPlayer player) {
      return this.detectGroup.apply(player);
   }

   @NotNull
   private String getByPrimary(@NotNull TabPlayer player) {
      try {
         String group = this.groupFunction.apply(player);
         if (group != null) {
            return group;
         }

         TAB.getInstance().getErrorManager().nullGroupReturned(this.permissionPlugin, player);
      } catch (Exception e) {
         TAB.getInstance().getErrorManager().groupRetrieveException(this.permissionPlugin, player, e);
      }

      return "NONE";
   }

   @NotNull
   private String getByPermission(@NotNull TabPlayer player) {
      for (String group : TAB.getInstance().getConfiguration().getConfig().getPrimaryGroupFindingList()) {
         if (player.hasPermission("tab.group." + group)) {
            return group;
         }
      }

      return "NONE";
   }

   @NotNull
   @Generated
   public String getPermissionPlugin() {
      return this.permissionPlugin;
   }

   @NotNull
   @Generated
   public Function<TabPlayer, String> getGroupFunction() {
      return this.groupFunction;
   }

   @Generated
   public Function<TabPlayer, String> getDetectGroup() {
      return this.detectGroup;
   }
}
