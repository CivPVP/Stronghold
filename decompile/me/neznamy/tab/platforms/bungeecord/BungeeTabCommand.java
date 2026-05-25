package me.neznamy.tab.platforms.bungeecord;

import java.util.Collections;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

public class BungeeTabCommand extends Command implements TabExecutor {
   public BungeeTabCommand(@NotNull String command) {
      super(command, null, new String[0]);
   }

   public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
      if (TAB.getInstance().isPluginDisabled()) {
         for (String message : TAB.getInstance().getDisabledCommand().execute(args, sender.hasPermission("tab.reload"), sender.hasPermission("tab.admin"))) {
            if (sender instanceof ProxiedPlayer) {
               sender.sendMessage(
                  ((BungeePlatform)TAB.getInstance().getPlatform())
                     .transformComponent(
                        TabComponent.fromColoredText(message), ProtocolVersion.fromNetworkId(((ProxiedPlayer)sender).getPendingConnection().getVersion())
                     )
               );
            } else {
               sender.sendMessage(TabComponent.fromColoredText(message).convert());
            }
         }
      } else {
         TabPlayer p = null;
         if (sender instanceof ProxiedPlayer) {
            p = TAB.getInstance().getPlayer(((ProxiedPlayer)sender).getUniqueId());
            if (p == null) {
               return;
            }
         }

         TAB.getInstance().getCommand().execute(p, args);
      }
   }

   @NotNull
   public Iterable<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
      TabPlayer p = null;
      if (sender instanceof ProxiedPlayer) {
         p = TAB.getInstance().getPlayer(((ProxiedPlayer)sender).getUniqueId());
         if (p == null) {
            return Collections.emptyList();
         }
      }

      return TAB.getInstance().getCommand().complete(p, args);
   }
}
