package me.neznamy.tab.shared.command.bossbar;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BossBarSendCommand extends SubCommand {
   public BossBarSendCommand() {
      super("send", "tab.scoreboard.show");
   }

   @Override
   public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
      BossBarManager feature = TAB.getInstance().getFeatureManager().getFeature("BossBar");
      if (feature == null) {
         this.sendMessage(sender, this.getMessages().getBossBarNotEnabled());
      } else if (args.length != 3) {
         this.sendMessage(sender, this.getMessages().getSendBarCommandUsage());
      } else {
         TabPlayer target = TAB.getInstance().getPlayer(args[0]);
         if (target == null) {
            this.sendMessage(sender, this.getMessages().getPlayerNotFound(args[0]));
         } else {
            String barName = args[1];

            int duration;
            try {
               duration = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
               this.sendMessage(sender, this.getMessages().getInvalidNumber(args[1]));
               return;
            }

            BossBar bar = feature.getBossBar(barName);
            if (bar == null) {
               this.sendMessage(sender, this.getMessages().getBossBarNotFound(barName));
            } else if (!bar.isAnnouncementBar()) {
               this.sendMessage(sender, this.getMessages().getBossBarNotMarkedAsAnnouncement());
            } else {
               feature.sendBossBarTemporarily(target, bar.getName(), duration);
               this.sendMessage(sender, this.getMessages().getBossBarSendSuccess(target.getName(), bar.getName(), duration));
            }
         }
      }
   }

   @NotNull
   @Override
   public List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
      BossBarManager b = TAB.getInstance().getFeatureManager().getFeature("BossBar");
      if (b == null) {
         return Collections.emptyList();
      } else if (arguments.length == 1) {
         return this.getOnlinePlayers(arguments[0]);
      } else if (arguments.length == 2) {
         return this.getStartingArgument(b.getRegisteredBossBars().keySet(), arguments[1]);
      } else {
         return arguments.length == 3 && b.getBossBar(arguments[1]) != null
            ? this.getStartingArgument(Arrays.asList("5", "10", "30", "60", "120"), arguments[2])
            : Collections.emptyList();
      }
   }
}
