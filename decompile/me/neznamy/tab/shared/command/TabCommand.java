package me.neznamy.tab.shared.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.bossbar.BossBarCommand;
import me.neznamy.tab.shared.command.scoreboard.ScoreboardCommand;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabCommand extends SubCommand {
   public TabCommand() {
      super(null, null);
      this.registerSubCommand(new BossBarCommand());
      this.registerSubCommand(new CpuCommand());
      this.registerSubCommand(new DebugCommand());
      this.registerSubCommand(new GroupCommand());
      this.registerSubCommand(new GroupsCommand());
      this.registerSubCommand(new MySQLCommand());
      this.registerSubCommand(new NameTagCommand());
      this.registerSubCommand(new ParseCommand());
      this.registerSubCommand(new PlayerCommand());
      this.registerSubCommand(new PlayerUUIDCommand());
      this.registerSubCommand(new ReloadCommand());
      this.registerSubCommand(new SetCollisionCommand());
      this.registerSubCommand(new ScoreboardCommand());
   }

   @Override
   public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
      if (args.length > 0) {
         String arg0 = args[0];
         SubCommand command = this.getSubcommands().get(arg0.toLowerCase());
         if (command != null) {
            if (command.hasPermission(sender)) {
               command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
            } else {
               this.sendMessage(sender, this.getMessages().getNoPermission());
            }
         } else {
            this.help(sender);
         }
      } else {
         this.help(sender);
      }
   }

   private void help(@Nullable TabPlayer sender) {
      if (this.hasPermission(sender, "tab.admin")) {
         this.sendMessage(sender, "&3TAB v5.4.0");

         for (String message : this.getMessages().getHelpMenu()) {
            this.sendMessage(sender, message.replace("/tab", "/" + TAB.getInstance().getPlatform().getCommand()));
         }
      }
   }

   @NotNull
   @Override
   public List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
      return !this.hasPermission(sender, "tab.tabcomplete") ? Collections.emptyList() : super.complete(sender, arguments);
   }
}
