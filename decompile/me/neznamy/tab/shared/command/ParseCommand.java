package me.neznamy.tab.shared.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParseCommand extends SubCommand {
   public ParseCommand() {
      super("parse", "tab.parse");
   }

   @Override
   public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
      if (args.length < 2) {
         this.sendMessage(sender, this.getMessages().getParseCommandUsage());
      } else {
         TabPlayer target;
         if (args[0].equals("me")) {
            if (sender == null) {
               this.sendMessage(
                  null,
                  "&cThe \"me\" argument instead of player name is only available in-game and parses the placeholder for player who ran the command. If you wish to use the parse command from the console, use name of an online player instead of \"me\"."
               );
               return;
            }

            target = sender;
         } else {
            target = TAB.getInstance().getPlayer(args[0]);
            if (target == null) {
               this.sendMessage(sender, this.getMessages().getPlayerNotFound(args[0]));
               return;
            }
         }

         String textToParse = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
         if (!textToParse.contains("%")) {
            this.sendMessage(sender, "&cThe provided input (" + textToParse + ") does not contain any placeholders, therefore there's nothing to test.");
         } else {
            this.sendMessage(
               sender,
               new TabTextComponent(
                  "",
                  Arrays.asList(
                     new TabTextComponent("Replacing placeholder ", TabTextColor.GOLD),
                     new TabTextComponent(textToParse, TabTextColor.YELLOW),
                     new TabTextComponent(" for player ", TabTextColor.GOLD),
                     new TabTextComponent(target.getName(), TabTextColor.YELLOW)
                  )
               )
            );

            try {
               String replaced = new Property(null, null, target, textToParse, null).get();
               TabComponent colored = TabComponent.fromColoredText("&3Colored output: &e\"&r" + replaced + "&e\"");
               if (sender != null) {
                  sender.sendMessage(colored);
               } else {
                  TAB.getInstance().getPlatform().logInfo(colored);
               }

               this.sendMessage(
                  sender,
                  new TabTextComponent(
                     "",
                     Arrays.asList(
                        new TabTextComponent("Raw colors: ", TabTextColor.DARK_AQUA),
                        new TabTextComponent("\"", TabTextColor.YELLOW),
                        new TabTextComponent(replaced.replace('§', '&'), TabTextColor.WHITE),
                        new TabTextComponent("\"", TabTextColor.YELLOW)
                     )
                  )
               );
               this.sendMessage(sender, "&3Output length: &e" + replaced.length() + " &3characters");
            } catch (Exception e) {
               this.sendMessage(sender, "&cThe placeholder threw an exception when parsing. Check console for more info.");
               TAB.getInstance().getErrorManager().parseCommandError(textToParse, target, e);
            }
         }
      }
   }

   @NotNull
   @Override
   public List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
      if (arguments.length == 1) {
         List<String> suggestions = this.getOnlinePlayers(arguments[0]);
         if ("me".startsWith(arguments[0].toLowerCase())) {
            suggestions.add("me");
         }

         return suggestions;
      } else {
         return arguments.length == 2
            ? TAB.getInstance()
               .getPlaceholderManager()
               .getAllPlaceholders()
               .stream()
               .map(Placeholder::getIdentifier)
               .filter(placeholder -> placeholder.toLowerCase().startsWith(arguments[1].toLowerCase()))
               .collect(Collectors.toList())
            : Collections.emptyList();
      }
   }
}
