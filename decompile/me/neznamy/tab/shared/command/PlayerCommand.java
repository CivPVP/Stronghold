package me.neznamy.tab.shared.command;

import java.util.List;
import java.util.UUID;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerCommand extends PropertyCommand {
   public PlayerCommand() {
      super("player");
   }

   @Override
   public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
      if (args.length <= 1) {
         this.help(sender);
      } else if ("remove".equalsIgnoreCase(args[1])) {
         this.remove(sender, args[0]);
      } else {
         this.trySaveEntity(sender, args);
      }
   }

   private void remove(@Nullable TabPlayer sender, @NotNull String player) {
      if (this.hasPermission(sender, "tab.remove")) {
         TAB.getInstance().getConfiguration().getUsers().remove(player);
         TabPlayer pl = TAB.getInstance().getPlayer(player);
         if (pl != null) {
            TAB.getInstance().getFeatureManager().onGroupChange(pl);
         }

         this.sendMessage(sender, this.getMessages().getPlayerDataRemoved(player));
      } else {
         this.sendMessage(sender, this.getMessages().getNoPermission());
      }
   }

   @Override
   public void saveEntity(
      @Nullable TabPlayer sender, @NotNull String player, @NotNull String type, @NotNull String value, @Nullable Server server, @Nullable World world
   ) {
      if (!value.isEmpty()) {
         this.sendMessage(sender, this.getMessages().getPlayerValueAssigned(type, value, player));
      } else {
         this.sendMessage(sender, this.getMessages().getPlayerValueRemoved(type, player));
      }

      String[] property = TAB.getInstance().getConfiguration().getUsers().getProperty(player, type, server, world);
      if (property.length <= 0 || !String.valueOf(value.isEmpty() ? null : value).equals(String.valueOf(property[0]))) {
         TAB.getInstance().getConfiguration().getUsers().setProperty(player, type, server, world, value.isEmpty() ? null : value);
         TabPlayer pl = TAB.getInstance().getPlayer(player);

         try {
            if (pl == null) {
               pl = TAB.getInstance().getPlayer(UUID.fromString(player));
            }
         } catch (IllegalArgumentException var10) {
         }

         if (pl != null) {
            TAB.getInstance().getFeatureManager().onGroupChange(pl);
         }
      }
   }

   @NotNull
   @Override
   public List<String> complete(TabPlayer sender, String[] arguments) {
      return arguments.length == 1 ? this.getOnlinePlayers(arguments[0]) : super.complete(sender, arguments);
   }
}
