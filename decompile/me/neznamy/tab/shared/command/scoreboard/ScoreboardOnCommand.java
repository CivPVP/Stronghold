package me.neznamy.tab.shared.command.scoreboard;

import java.util.Collections;
import java.util.List;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScoreboardOnCommand extends SubCommand {
   public ScoreboardOnCommand() {
      super("on", "tab.scoreboard.toggle");
   }

   @Override
   public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
      ScoreboardManagerImpl scoreboard = TAB.getInstance().getFeatureManager().getFeature("ScoreBoard");
      if (scoreboard == null) {
         this.sendMessage(sender, this.getMessages().getScoreboardFeatureNotEnabled());
      } else {
         TabPlayer target = sender;
         if (args.length > 0) {
            if (!this.hasPermission(sender, "tab.scoreboard.toggle.other")) {
               this.sendMessage(sender, this.getMessages().getNoPermission());
               return;
            }

            target = TAB.getInstance().getPlayer(args[0]);
            if (target == null) {
               this.sendMessage(sender, this.getMessages().getPlayerNotFound(args[0]));
               return;
            }
         } else if (target == null) {
            this.sendMessage(null, this.getMessages().getCommandOnlyFromGame());
            return;
         }

         boolean silent = args.length == 2 && args[1].equals("-s");
         scoreboard.setScoreboardVisible(target, true, !silent);
      }
   }

   @NotNull
   @Override
   public List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
      if (arguments.length == 1) {
         return this.getOnlinePlayers(arguments[0]);
      } else {
         return arguments.length == 2 ? this.getStartingArgument(Collections.singletonList("-s"), arguments[1]) : Collections.emptyList();
      }
   }
}
