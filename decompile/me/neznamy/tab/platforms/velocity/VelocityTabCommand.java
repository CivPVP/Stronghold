package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.SimpleCommand.Invocation;
import com.velocitypowered.api.proxy.Player;
import java.util.Collections;
import java.util.List;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class VelocityTabCommand implements SimpleCommand {
   public void execute(@NotNull Invocation invocation) {
      CommandSource sender = invocation.source();
      if (TAB.getInstance().isPluginDisabled()) {
         for (String message : TAB.getInstance()
            .getDisabledCommand()
            .execute((String[])invocation.arguments(), sender.hasPermission("tab.reload"), sender.hasPermission("tab.admin"))) {
            sender.sendMessage(TabComponent.fromColoredText(message).toAdventure());
         }
      } else {
         TabPlayer p = null;
         if (sender instanceof Player) {
            p = TAB.getInstance().getPlayer(((Player)sender).getUniqueId());
            if (p == null) {
               return;
            }
         }

         TAB.getInstance().getCommand().execute(p, (String[])invocation.arguments());
      }
   }

   @NotNull
   public List<String> suggest(@NotNull Invocation invocation) {
      TabPlayer p = null;
      if (invocation.source() instanceof Player) {
         p = TAB.getInstance().getPlayer(((Player)invocation.source()).getUniqueId());
         if (p == null) {
            return Collections.emptyList();
         }
      }

      return TAB.getInstance().getCommand().complete(p, (String[])invocation.arguments());
   }
}
