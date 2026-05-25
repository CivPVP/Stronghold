package me.neznamy.tab.shared.command;

import java.util.List;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerUUIDCommand extends PropertyCommand {
   public PlayerUUIDCommand() {
      super("playeruuid");
   }

   @Override
   public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
      if (args.length <= 1) {
         this.help(sender);
      } else {
         TabPlayer player = TAB.getInstance().getPlayer(args[0]);
         if (player == null) {
            this.sendMessage(sender, this.getMessages().getPlayerNotFound(args[0]));
         } else {
            String type = args[1].toLowerCase();
            if ("remove".equals(type)) {
               this.remove(sender, player);
            } else {
               this.trySaveEntity(sender, args);
            }
         }
      }
   }

   private void remove(@Nullable TabPlayer sender, @NotNull TabPlayer changed) {
      if (this.hasPermission(sender, "tab.remove")) {
         TAB.getInstance().getConfiguration().getUsers().remove(changed.getUniqueId().toString());
         TAB.getInstance().getFeatureManager().onGroupChange(changed);
         this.sendMessage(sender, this.getMessages().getPlayerDataRemoved(changed.getName() + "(" + changed.getUniqueId() + ")"));
      } else {
         this.sendMessage(sender, this.getMessages().getNoPermission());
      }
   }

   @Override
   public void saveEntity(
      @Nullable TabPlayer sender, @NotNull String playerName, @NotNull String type, @NotNull String value, @Nullable Server server, @Nullable World world
   ) {
      TabPlayer player = TAB.getInstance().getPlayer(playerName);
      if (!value.isEmpty()) {
         this.sendMessage(sender, this.getMessages().getPlayerValueAssigned(type, value, playerName + "(" + player.getUniqueId() + ")"));
      } else {
         this.sendMessage(sender, this.getMessages().getPlayerValueRemoved(type, playerName + "(" + player.getUniqueId() + ")"));
      }

      String[] property = TAB.getInstance().getConfiguration().getUsers().getProperty(player.getUniqueId().toString(), type, server, world);
      if (property.length <= 0 || !String.valueOf(value.isEmpty() ? null : value).equals(String.valueOf(property[0]))) {
         TAB.getInstance().getConfiguration().getUsers().setProperty(player.getUniqueId().toString(), type, server, world, value.isEmpty() ? null : value);
         TAB.getInstance().getFeatureManager().onGroupChange(player);
      }
   }

   @NotNull
   @Override
   public List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
      return arguments.length == 1 ? this.getOnlinePlayers(arguments[0]) : super.complete(sender, arguments);
   }
}
