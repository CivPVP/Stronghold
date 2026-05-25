package me.neznamy.tab.platforms.bukkit;

import java.util.Collections;
import java.util.List;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BukkitTabCommand implements CommandExecutor, TabCompleter {
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (TAB.getInstance().isPluginDisabled()) {
         for (String message : TAB.getInstance().getDisabledCommand().execute(args, sender.hasPermission("tab.reload"), sender.hasPermission("tab.admin"))) {
            sender.sendMessage(((BukkitPlatform)TAB.getInstance().getPlatform()).toBukkitFormat(TabComponent.fromColoredText(message)));
         }
      } else {
         TabPlayer p = null;
         if (sender instanceof Player) {
            p = TAB.getInstance().getPlayer(((Player)sender).getUniqueId());
            if (p == null) {
               return true;
            }
         }

         TAB.getInstance().getCommand().execute(p, args);
      }

      return false;
   }

   @NotNull
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      TabPlayer p = null;
      if (sender instanceof Player) {
         p = TAB.getInstance().getPlayer(((Player)sender).getUniqueId());
         if (p == null) {
            return Collections.emptyList();
         }
      }

      return TAB.getInstance().getCommand().complete(p, args);
   }
}
