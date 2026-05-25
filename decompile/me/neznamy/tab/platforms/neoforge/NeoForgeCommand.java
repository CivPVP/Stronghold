package me.neznamy.tab.platforms.neoforge;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.NotNull;

public abstract class NeoForgeCommand {
   @NotNull
   private final String commandName;

   @NotNull
   public LiteralCommandNode<CommandSourceStack> getCommand() {
      LiteralCommandNode<CommandSourceStack> command = ((LiteralArgumentBuilder)Commands.literal(this.commandName)
            .executes(context -> this.execute((CommandSourceStack)context.getSource(), new String[0])))
         .build();
      ArgumentCommandNode<CommandSourceStack, String> args = ((RequiredArgumentBuilder)Commands.argument("args", StringArgumentType.greedyString())
            .suggests((context, builder) -> this.getSuggestions((CommandSourceStack)context.getSource(), this.getArguments(context), builder))
            .executes(context -> this.execute((CommandSourceStack)context.getSource(), this.getArguments(context))))
         .build();
      command.addChild(args);
      return command;
   }

   @NotNull
   private String[] getArguments(@NotNull CommandContext<CommandSourceStack> context) {
      String input = context.getInput();
      int firstSpace = input.indexOf(32);
      if (firstSpace == -1) {
         return new String[0];
      }

      String rawArgs = input.substring(firstSpace + 1);
      String[] args = rawArgs.split(" ");
      if (rawArgs.endsWith(" ")) {
         args = Arrays.copyOf(args, args.length + 1);
         args[args.length - 1] = "";
      }

      return args;
   }

   @NotNull
   private CompletableFuture<Suggestions> getSuggestions(@NotNull CommandSourceStack source, @NotNull String[] args, @NotNull SuggestionsBuilder builder) {
      SuggestionsBuilder newBuilder = builder;
      int lastSpace = newBuilder.getRemaining().lastIndexOf(32);
      if (lastSpace != -1) {
         newBuilder = newBuilder.createOffset(lastSpace + 1 + newBuilder.getStart());
      }

      for (String suggestion : this.complete(source, args)) {
         newBuilder.suggest(suggestion);
      }

      return newBuilder.buildFuture();
   }

   public abstract int execute(@NotNull CommandSourceStack var1, @NotNull String[] var2);

   @NotNull
   public List<String> complete(@NotNull CommandSourceStack sender, @NotNull String[] args) {
      return Collections.emptyList();
   }

   @Generated
   public NeoForgeCommand(@NotNull String commandName) {
      if (commandName == null) {
         throw new NullPointerException("commandName is marked non-null but is null");
      }

      this.commandName = commandName;
   }
}
