package me.neznamy.tab.shared.command.scoreboard;

import java.util.Arrays;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScoreboardCommand extends SubCommand {
   public ScoreboardCommand() {
      super("scoreboard", null);
      this.registerSubCommand(new ScoreboardAnnounceCommand());
      this.registerSubCommand(new ScoreboardShowCommand());
      this.registerSubCommand(new ScoreboardToggleCommand());
      this.registerSubCommand(new ScoreboardOnCommand());
      this.registerSubCommand(new ScoreboardOffCommand());
   }

   @Override
   public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
      if (args.length == 0) {
         SubCommand command = this.getSubcommands().get("toggle");
         if (command.hasPermission(sender)) {
            command.execute(sender, new String[0]);
         } else {
            this.sendMessage(sender, this.getMessages().getNoPermission());
         }
      } else {
         SubCommand command = this.getSubcommands().get(args[0].toLowerCase());
         if (command != null) {
            if (command.hasPermission(sender)) {
               command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
            } else {
               this.sendMessage(sender, this.getMessages().getNoPermission());
            }
         } else {
            this.sendMessages(sender, this.getMessages().getScoreboardHelpMenu());
         }
      }
   }
}
