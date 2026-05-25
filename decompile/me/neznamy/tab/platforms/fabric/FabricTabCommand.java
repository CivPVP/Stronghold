package me.neznamy.tab.platforms.fabric;

import java.util.Collections;
import java.util.List;
import me.neznamy.tab.platforms.fabric.hook.PermissionsAPIHook;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.class_2168;
import org.jetbrains.annotations.NotNull;

public class FabricTabCommand extends FabricCommand {
   public FabricTabCommand(@NotNull String commandName) {
      super(commandName);
   }

   @Override
   public int execute(@NotNull class_2168 source, @NotNull String[] args) {
      if (TAB.getInstance().isPluginDisabled()) {
         boolean hasReloadPermission = PermissionsAPIHook.hasPermission(source, "tab.reload");
         boolean hasAdminPermission = PermissionsAPIHook.hasPermission(source, "tab.admin");

         for (String message : TAB.getInstance().getDisabledCommand().execute(args, hasReloadPermission, hasAdminPermission)) {
            source.method_45068(TabComponent.fromColoredText(message).convert());
         }
      } else if (source.method_9228() == null) {
         TAB.getInstance().getCommand().execute(null, args);
      } else {
         TabPlayer player = TAB.getInstance().getPlayer(source.method_9228().method_5667());
         if (player != null) {
            TAB.getInstance().getCommand().execute(player, args);
         }
      }

      return 0;
   }

   @NotNull
   @Override
   public List<String> complete(@NotNull class_2168 sender, @NotNull String[] args) {
      TabPlayer player = null;
      if (sender.method_9228() != null) {
         player = TAB.getInstance().getPlayer(sender.method_9228().method_5667());
         if (player == null) {
            return Collections.emptyList();
         }
      }

      return TAB.getInstance().getCommand().complete(player, args);
   }
}
