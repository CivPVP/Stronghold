package me.neznamy.tab.platforms.fabric;

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
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import org.jetbrains.annotations.NotNull;

public abstract class FabricCommand {
   @NotNull
   private final String commandName;

   @NotNull
   public LiteralCommandNode<class_2168> getCommand() {
      LiteralCommandNode<class_2168> command = ((LiteralArgumentBuilder)class_2170.method_9247(this.commandName)
            .executes(context -> this.execute((class_2168)context.getSource(), new String[0])))
         .build();
      ArgumentCommandNode<class_2168, String> args = ((RequiredArgumentBuilder)class_2170.method_9244("args", StringArgumentType.greedyString())
            .suggests((context, builder) -> this.getSuggestions((class_2168)context.getSource(), this.getArguments(context), builder))
            .executes(context -> this.execute((class_2168)context.getSource(), this.getArguments(context))))
         .build();
      command.addChild(args);
      return command;
   }

   @NotNull
   private String[] getArguments(@NotNull CommandContext<class_2168> context) {
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
   private CompletableFuture<Suggestions> getSuggestions(@NotNull class_2168 source, @NotNull String[] args, @NotNull SuggestionsBuilder builder) {
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

   public abstract int execute(@NotNull class_2168 var1, @NotNull String[] var2);

   @NotNull
   public List<String> complete(@NotNull class_2168 sender, @NotNull String[] args) {
      return Collections.emptyList();
   }

   @Generated
   public FabricCommand(@NotNull String commandName) {
      if (commandName == null) {
         throw new NullPointerException("commandName is marked non-null but is null");
      }

      this.commandName = commandName;
   }
}
