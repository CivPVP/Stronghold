package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.List;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;

public class DisabledCommand {
   public List<String> execute(@NotNull String[] args, boolean hasReloadPermission, boolean hasAdminPermission) {
      List<String> messages = new ArrayList<>();
      if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
         if (hasReloadPermission) {
            messages.add(TAB.getInstance().load());
         } else {
            messages.add(
               "&cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error."
            );
         }
      } else if (hasAdminPermission) {
         messages.add("&m                                                                                ");
         messages.add(" &cPlugin is disabled due to an error. Check console for more details.");
         messages.add(" &8>> &3&l/" + TAB.getInstance().getPlatform().getCommand() + " reload");
         messages.add("      - &7Reloads plugin and config");
         messages.add("&m                                                                                ");
      }

      return messages;
   }
}
