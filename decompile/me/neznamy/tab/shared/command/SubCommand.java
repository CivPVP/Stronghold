package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.config.MessageFile;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SubCommand {
   private final Map<String, SubCommand> subcommands = new HashMap<>();
   private final String name;
   private final String permission;

   public void registerSubCommand(@NotNull SubCommand subcommand) {
      this.subcommands.put(subcommand.name, subcommand);
   }

   public boolean hasPermission(@Nullable TabPlayer sender) {
      return this.hasPermission(sender, this.permission);
   }

   public boolean hasPermission(@Nullable TabPlayer sender, @Nullable String permission) {
      if (permission == null) {
         return true;
      } else if (sender == null) {
         return true;
      } else {
         return sender.hasPermission("tab.admin") ? true : sender.hasPermission(permission);
      }
   }

   public void sendMessages(@Nullable TabPlayer sender, @NotNull List<String> messages) {
      for (String message : messages) {
         this.sendMessage(sender, message);
      }
   }

   public void sendMessage(@Nullable TabPlayer sender, @NotNull String message) {
      if (!message.isEmpty()) {
         if (sender != null) {
            sender.sendMessage(message);
         } else {
            TAB.getInstance().getPlatform().logInfo(TabComponent.fromColoredText(message));
         }
      }
   }

   public void sendMessage(@Nullable TabPlayer sender, @NotNull TabComponent message) {
      if (sender != null) {
         sender.sendMessage(message);
      } else {
         TAB.getInstance().getPlatform().logInfo(message);
      }
   }

   @NotNull
   public List<String> getOnlinePlayers(@NotNull String nameStart) {
      List<String> suggestions = new ArrayList<>();

      for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
         if (all.getName().toLowerCase().startsWith(nameStart.toLowerCase())) {
            suggestions.add(all.getName());
         }
      }

      return suggestions;
   }

   @NotNull
   public List<String> getStartingArgument(@NotNull Collection<String> values, @NotNull String argument) {
      return values.stream().filter(value -> value.toLowerCase().startsWith(argument.toLowerCase())).collect(Collectors.toList());
   }

   @NotNull
   public List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
      String argument;
      if (arguments.length == 0) {
         argument = "";
      } else {
         argument = arguments[0].toLowerCase();
      }

      if (arguments.length < 2) {
         List<String> suggestions = new ArrayList<>();

         for (String subcommand : this.subcommands.keySet()) {
            if (subcommand.startsWith(argument)) {
               suggestions.add(subcommand);
            }
         }

         return suggestions;
      } else {
         SubCommand subcommand = this.subcommands.get(argument);
         return subcommand != null ? subcommand.complete(sender, Arrays.copyOfRange(arguments, 1, arguments.length)) : Collections.emptyList();
      }
   }

   @NotNull
   public MessageFile getMessages() {
      return TAB.getInstance().getConfiguration().getMessages();
   }

   public abstract void execute(@Nullable TabPlayer var1, @NotNull String[] var2);

   @Generated
   public Map<String, SubCommand> getSubcommands() {
      return this.subcommands;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getPermission() {
      return this.permission;
   }

   @Generated
   public SubCommand(String name, String permission) {
      this.name = name;
      this.permission = permission;
   }
}
