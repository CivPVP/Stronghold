package me.neznamy.tab.shared.command;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GroupCommand extends PropertyCommand {
   public GroupCommand() {
      super("group");
   }

   @Override
   public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
      if (args.length == 0) {
         this.help(sender);
      } else if (args.length == 1) {
         if (this.hasPermission(sender, "tab.groupinfo")) {
            this.sendGroupInfo(sender, args[0]);
         } else {
            this.sendMessage(sender, this.getMessages().getNoPermission());
         }
      } else if ("remove".equalsIgnoreCase(args[1])) {
         this.remove(sender, args[0]);
      } else {
         this.trySaveEntity(sender, args);
      }
   }

   private void remove(@Nullable TabPlayer sender, @NotNull String group) {
      if (this.hasPermission(sender, "tab.remove")) {
         TAB.getInstance().getConfiguration().getGroups().remove(group);

         for (TabPlayer pl : TAB.getInstance().getOnlinePlayers()) {
            if (pl.getGroup().equals(group) || "_DEFAULT_".equals(group)) {
               TAB.getInstance().getFeatureManager().onGroupChange(pl);
            }
         }

         this.sendMessage(sender, this.getMessages().getGroupDataRemoved(group));
      } else {
         this.sendMessage(sender, this.getMessages().getNoPermission());
      }
   }

   private void sendGroupInfo(@Nullable TabPlayer sender, @NotNull String group) {
      this.sendMessage(sender, "&f=== Group &9" + group + "&f ===");

      for (Entry<String, Object> entry : TAB.getInstance().getConfiguration().getGroups().getGlobalSettings(group).entrySet()) {
         this.sendMessage(sender, "  " + entry.getKey() + ": " + entry.getValue());
      }

      for (Entry<String, Map<String, Object>> entry : TAB.getInstance().getConfiguration().getGroups().getPerWorldSettings(group).entrySet()) {
         if (entry.getValue() != null) {
            this.sendMessage(sender, "&6World " + entry.getKey() + ":&e");

            for (Entry<String, Object> properties : entry.getValue().entrySet()) {
               this.sendMessage(sender, "  " + properties.getKey() + ": " + properties.getValue());
            }
         }
      }

      for (Entry<String, Map<String, Object>> entry : TAB.getInstance().getConfiguration().getGroups().getPerServerSettings(group).entrySet()) {
         if (entry.getValue() != null) {
            this.sendMessage(sender, "&3Server " + entry.getKey() + ":&b");

            for (Entry<String, Object> properties : entry.getValue().entrySet()) {
               this.sendMessage(sender, "  " + properties.getKey() + ": " + properties.getValue());
            }
         }
      }
   }

   @Override
   public void saveEntity(
      @Nullable TabPlayer sender, @NotNull String group, @NotNull String type, @NotNull String value, @Nullable Server server, @Nullable World world
   ) {
      if (!value.isEmpty()) {
         this.sendMessage(sender, this.getMessages().getGroupValueAssigned(type, value, group));
      } else {
         this.sendMessage(sender, this.getMessages().getGroupValueRemoved(type, group));
      }

      String[] property = TAB.getInstance().getConfiguration().getGroups().getProperty(group, type, server, world);
      if (property.length <= 0 || !String.valueOf(value.isEmpty() ? null : value).equals(String.valueOf(property[0]))) {
         TAB.getInstance().getConfiguration().getGroups().setProperty(group, type, server, world, value.isEmpty() ? null : value);

         for (TabPlayer pl : TAB.getInstance().getOnlinePlayers()) {
            if (pl.getGroup().equals(group) || "_DEFAULT_".equals(group)) {
               TAB.getInstance().getFeatureManager().onGroupChange(pl);
            }
         }
      }
   }

   @NotNull
   @Override
   public List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
      if (arguments.length == 1) {
         Set<String> groups = new HashSet<>(TAB.getInstance().getConfiguration().getGroups().getAllEntries());
         groups.add("_DEFAULT_");
         return groups.stream().filter(group -> group.toLowerCase().startsWith(arguments[0].toLowerCase())).collect(Collectors.toList());
      } else {
         return super.complete(sender, arguments);
      }
   }
}
