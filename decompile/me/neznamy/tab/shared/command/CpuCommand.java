package me.neznamy.tab.shared.command;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.cpu.CpuReport;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CpuCommand extends SubCommand {
   private final DecimalFormat decimal3 = new DecimalFormat("#.###");
   private final char LINE_CHAR = 9553;

   public CpuCommand() {
      super("cpu", "tab.cpu");
   }

   @Override
   public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
      CpuReport report = TAB.getInstance().getCPUManager().getLastReport();
      if (report == null) {
         if (TAB.getInstance().getCPUManager().enableTracking()) {
            this.sendMessage(sender, "&aCPU usage tracking has been enabled. Run the command again in 10 seconds to see the first results.");
         } else {
            this.sendMessage(sender, "&cPlease wait at least 10 seconds since running the command for the first time.");
         }
      } else {
         Map<String, Map<String, Float>> features = report.getFeatureUsage();
         this.sendMessage(sender, " ");
         this.sendMessage(sender, "&8&l║&8&m             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ");
         this.sendMessage(sender, "&8&l║ &6CPU stats from the last 10 seconds");
         this.sendMessage(sender, "&8&l║&8&m                                                    ");
         this.sendMessage(sender, "&8&l║ &6Top 5 placeholders:");
         this.printPlaceholders(sender, report.getPlaceholderUsage());
         this.sendMessage(sender, "&8&l║&8&m                                                    ");
         if (sender != null) {
            this.sendToPlayer(sender, features);
         } else {
            this.sendToConsole(features);
         }

         this.sendMessage(sender, "&8&l║&8&m                                                    ");
         this.sendMessage(
            sender,
            String.format("&8&l%s &6&lPlaceholders Total: &a&l%s%%", '║', this.colorize(this.decimal3.format(report.getPlaceholderUsageTotal()), 10.0F, 5.0F))
         );
         this.sendMessage(
            sender,
            String.format(
               "&8&l%s &6&lPlugin internals: &a&l%s%%",
               '║',
               this.colorize(this.decimal3.format(report.getFeatureUsageTotal() - report.getPlaceholderUsageTotal()), 10.0F, 5.0F)
            )
         );
         this.sendMessage(
            sender, String.format("&8&l%s &6&lTotal: &e&l%s%%", '║', this.colorize(this.decimal3.format(report.getFeatureUsageTotal()), 10.0F, 5.0F))
         );
         this.sendMessage(sender, "&8&l║&8&m             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ");
         this.sendMessage(sender, " ");
      }
   }

   private void printPlaceholders(@Nullable TabPlayer sender, @NotNull Map<String, Float> map) {
      int printCounter = 0;

      for (Entry<String, Float> entry : map.entrySet()) {
         if (printCounter++ == 5) {
            break;
         }

         Placeholder p = TAB.getInstance().getPlaceholderManager().getPlaceholder(entry.getKey());
         Integer configuredRefresh = TAB.getInstance().getPlaceholderManager().getConfiguration().getRefreshIntervals().get(entry.getKey());
         String refresh = String.format(" %s (%d)&7", configuredRefresh == null ? "&8" : "&3", p.getRefresh());
         String colorized = entry.getKey().startsWith("%sync:")
            ? "&c" + this.decimal3.format(entry.getValue())
            : this.colorize(this.decimal3.format(entry.getValue()), 1.0F, 0.3F);
         this.sendMessage(sender, String.format("&8&l%s &7%s - %s%%", '║', entry.getKey() + refresh, colorized));
      }
   }

   private void sendToConsole(@NotNull Map<String, Map<String, Float>> features) {
      TAB.getInstance().getPlatform().logInfo(TabComponent.fromColoredText("&8&l║ &6Features:"));

      for (Entry<String, Map<String, Float>> entry : features.entrySet()) {
         TAB.getInstance()
            .getPlatform()
            .logInfo(
               TabComponent.fromColoredText(
                  String.format(
                     "&8&l%s &7%s &7(%s%%&7):",
                     '║',
                     entry.getKey(),
                     this.colorize(this.decimal3.format(entry.getValue().values().stream().mapToDouble(Float::floatValue).sum()), 5.0F, 1.0F)
                  )
               )
            );

         for (Entry<String, Float> type : entry.getValue().entrySet()) {
            TAB.getInstance()
               .getPlatform()
               .logInfo(
                  TabComponent.fromColoredText(
                     String.format("&8&l%s     &7%s - %s%%", '║', type.getKey(), this.colorize(this.decimal3.format(type.getValue()), 5.0F, 1.0F))
                  )
               );
         }
      }
   }

   private void sendToPlayer(@NotNull TabPlayer sender, @NotNull Map<String, Map<String, Float>> features) {
      this.sendMessage(sender, "&8&l║ &6Features (execute from console for more info):");

      for (Entry<String, Map<String, Float>> entry : features.entrySet()) {
         double featureTotal = entry.getValue().values().stream().mapToDouble(Float::floatValue).sum();
         sender.sendMessage(String.format("&8&l%s &7%s &7(%s%%&7):", '║', entry.getKey(), this.colorize(this.decimal3.format(featureTotal), 5.0F, 1.0F)));
      }
   }

   private String colorize(@NotNull String usage, float threshold1, float threshold2) {
      float percent = Float.parseFloat(usage.replace(",", "."));
      if (percent > threshold1) {
         return "&c" + usage;
      } else {
         return percent > threshold2 ? "&e" + usage : "&a" + usage;
      }
   }
}
