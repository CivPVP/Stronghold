package me.neznamy.tab.shared.command.bossbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BossBarAnnounceCommand extends SubCommand {
   public BossBarAnnounceCommand() {
      super("announce", "tab.announce.bar");
   }

   @Override
   public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
      BossBarManager feature = TAB.getInstance().getFeatureManager().getFeature("BossBar");
      if (feature == null) {
         this.sendMessage(sender, this.getMessages().getBossBarNotEnabled());
      } else if (args.length != 2) {
         this.sendMessage(sender, this.getMessages().getBossBarAnnounceCommandUsage());
      } else {
         String barName = args[0];

         int duration;
         try {
            duration = Integer.parseInt(args[1]);
         } catch (NumberFormatException e) {
            this.sendMessage(sender, this.getMessages().getInvalidNumber(args[1]));
            return;
         }

         BossBar bar = feature.getBossBar(barName);
         if (bar == null) {
            this.sendMessage(sender, this.getMessages().getBossBarNotFound(barName));
         } else if (!bar.isAnnouncementBar()) {
            this.sendMessage(sender, this.getMessages().getBossBarNotMarkedAsAnnouncement());
         } else if (feature.getAnnouncedBossBars().contains(bar)) {
            this.sendMessage(sender, this.getMessages().getBossBarAlreadyAnnounced());
         } else {
            feature.announceBossBar(bar.getName(), duration);
            this.sendMessage(sender, this.getMessages().getBossBarAnnouncementSuccess(bar.getName(), duration));
         }
      }
   }

   @NotNull
   @Override
   public List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
      BossBarManager feature = TAB.getInstance().getFeatureManager().getFeature("BossBar");
      if (feature == null) {
         return Collections.emptyList();
      }

      List<String> suggestions = new ArrayList<>();
      if (arguments.length == 1) {
         for (String bar : feature.getRegisteredBossBars()
            .values()
            .stream()
            .filter(BossBar::isAnnouncementBar)
            .map(BossBar::getName)
            .collect(Collectors.toList())) {
            if (bar.toLowerCase().startsWith(arguments[0].toLowerCase())) {
               suggestions.add(bar);
            }
         }
      } else if (arguments.length == 2 && feature.getRegisteredBossBars().get(arguments[0]) != null) {
         for (String time : Arrays.asList("5", "10", "30", "60", "120")) {
            if (time.startsWith(arguments[1])) {
               suggestions.add(time);
            }
         }
      }

      return suggestions;
   }
}
