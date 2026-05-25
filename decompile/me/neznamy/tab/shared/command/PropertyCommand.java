package me.neznamy.tab.shared.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import me.neznamy.tab.shared.config.PropertyConfiguration;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PropertyCommand extends SubCommand {
   protected PropertyCommand(String name) {
      super(name, null);
   }

   @NotNull
   @Override
   public List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
      return arguments.length != 2 ? Collections.emptyList() : this.getStartingArgument(PropertyConfiguration.VALID_PROPERTIES, arguments[1]);
   }

   protected void help(@Nullable TabPlayer sender) {
      this.sendMessage(sender, "&cSyntax&8: &3&l/tab &9group&3/&9player &3<name> &9<property> &3<value...>");
      this.sendMessage(sender, "&7Valid Properties are:");
      this.sendMessage(sender, " - &9tabprefix&3/&9customtabname&3/&9tabsuffix");
      this.sendMessage(sender, " - &9tagprefix&3/&9tagsuffix");
   }

   protected void trySaveEntity(@Nullable TabPlayer sender, @NotNull String[] args) {
      String value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
      String world = null;
      String server = null;
      if (args[args.length - 2].equals("-w")) {
         world = args[args.length - 1];
         value = value.startsWith("-w") ? "" : value.substring(0, value.length() - world.length() - 4);
      }

      if (args[args.length - 2].equals("-s")) {
         server = args[args.length - 1];
         value = value.startsWith("-s") ? "" : value.substring(0, value.length() - server.length() - 4);
      }

      if ((value.startsWith("\"") && value.endsWith("\"") || value.startsWith("'") && value.endsWith("'")) && value.length() > 1) {
         value = value.substring(1, value.length() - 1);
      }

      String property = args[1].toLowerCase();
      if (PropertyConfiguration.VALID_PROPERTIES.contains(property)) {
         if (this.hasPermission(sender, "tab.change." + property)) {
            this.saveEntity(sender, args[0], property, value, Server.byName(server), World.byName(world));
         } else {
            this.sendMessage(sender, this.getMessages().getNoPermission());
         }
      } else {
         this.help(sender);
      }
   }

   public abstract void saveEntity(
      @Nullable TabPlayer var1, @NotNull String var2, @NotNull String var3, @NotNull String var4, @Nullable Server var5, @Nullable World var6
   );
}
