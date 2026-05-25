package me.neznamy.tab.shared.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SetCollisionCommand extends SubCommand {
   public SetCollisionCommand() {
      super("setcollision", "tab.setcollision");
   }

   @Override
   public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
      NameTag feature = TAB.getInstance().getNameTagManager();
      if (feature == null) {
         this.sendMessage(sender, this.getMessages().getTeamFeatureRequired());
      } else {
         if (args.length == 2) {
            TabPlayer target = TAB.getInstance().getPlayer(args[0]);
            if (target == null) {
               this.sendMessage(sender, this.getMessages().getPlayerNotFound(args[0]));
               return;
            }

            feature.setCollisionRule(target, Boolean.parseBoolean(args[1]));
            feature.updateCollision(target, true);
         } else {
            this.sendMessage(sender, this.getMessages().getCollisionCommandUsage());
         }
      }
   }

   @NotNull
   @Override
   public List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
      if (arguments.length == 1) {
         return this.getOnlinePlayers(arguments[0]);
      } else {
         return arguments.length == 2 ? this.getStartingArgument(Arrays.asList("true", "false"), arguments[1]) : Collections.emptyList();
      }
   }
}
