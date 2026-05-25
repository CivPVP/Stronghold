package me.neznamy.tab.shared.command.scoreboard;

import java.util.Collections;
import java.util.List;
import me.neznamy.tab.api.scoreboard.Scoreboard;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScoreboardShowCommand extends SubCommand {
   public ScoreboardShowCommand() {
      super("show", "tab.scoreboard.show");
   }

   @Override
   public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
      ScoreboardManager scoreboard = TAB.getInstance().getFeatureManager().getFeature("ScoreBoard");
      if (scoreboard == null) {
         this.sendMessage(sender, this.getMessages().getScoreboardFeatureNotEnabled());
      } else if (args.length != 0 && args.length <= 2) {
         Scoreboard sb = scoreboard.getRegisteredScoreboards().get(args[0]);
         if (sb == null) {
            this.sendMessage(sender, this.getMessages().getScoreboardNotFound(args[0]));
         } else {
            TabPlayer target;
            if (args.length == 1) {
               if (!this.hasPermission(sender, "tab.scoreboard.show")) {
                  this.sendMessage(sender, this.getMessages().getNoPermission());
                  return;
               }

               if (sender == null) {
                  this.sendMessage(null, this.getMessages().getCommandOnlyFromGame());
                  return;
               }

               target = sender;
            } else {
               if (!this.hasPermission(sender, "tab.scoreboard.show.other")) {
                  this.sendMessage(sender, this.getMessages().getNoPermission());
                  return;
               }

               target = TAB.getInstance().getPlayer(args[1]);
               if (target == null) {
                  this.sendMessage(sender, this.getMessages().getPlayerNotFound(args[1]));
                  return;
               }
            }

            scoreboard.showScoreboard(target, sb);
         }
      } else {
         this.sendMessage(sender, this.getMessages().getScoreboardShowUsage());
      }
   }

   @NotNull
   @Override
   public List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
      ScoreboardManager scoreboard = TAB.getInstance().getFeatureManager().getFeature("ScoreBoard");
      if (scoreboard == null) {
         return Collections.emptyList();
      } else if (arguments.length == 1) {
         return this.getStartingArgument(scoreboard.getRegisteredScoreboards().keySet(), arguments[0]);
      } else {
         return arguments.length == 2 ? this.getOnlinePlayers(arguments[1]) : Collections.emptyList();
      }
   }
}
