package me.neznamy.tab.shared.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.nametags.NameTagInvisibilityReason;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NameTagCommand extends SubCommand {
   public NameTagCommand() {
      super("nametag", null);
   }

   @Override
   public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
      if (args.length == 1 && sender == null) {
         this.sendMessage(null, this.getMessages().getNameTagNoArgFromConsole());
      } else if (args.length != 0 && args.length <= 4) {
         NameTag teams = TAB.getInstance().getNameTagManager();
         if (teams == null) {
            this.sendMessage(sender, this.getMessages().getNameTagFeatureNotEnabled());
         } else {
            String action = args[0].toLowerCase(Locale.US);
            if (action.equals("show") || action.equals("hide") || action.equals("toggle")) {
               this.processTarget(teams, sender, action, Arrays.copyOfRange(args, 1, args.length));
            } else if (!action.equals("showview") && !action.equals("hideview") && !action.equals("toggleview")) {
               this.sendMessages(sender, this.getMessages().getNameTagHelpMenu());
            } else {
               this.processView(teams, sender, action, Arrays.copyOfRange(args, 1, args.length));
            }
         }
      } else {
         this.sendMessages(sender, this.getMessages().getNameTagHelpMenu());
      }
   }

   private void processTarget(@NotNull NameTag teams, @Nullable TabPlayer sender, @NotNull String action, @NotNull String[] args) {
      boolean silent = args.length > 0 && args[args.length - 1].equals("-s");
      TabPlayer player = args.length >= 1 ? TAB.getInstance().getPlayer(args[0]) : sender;
      if (player == null) {
         this.sendMessage(sender, this.getMessages().getPlayerNotFound(args[0]));
      } else {
         TabPlayer viewer = args.length >= 2 ? TAB.getInstance().getPlayer(args[1]) : null;
         String permission = sender == viewer ? "tab.nametag.visibility" : "tab.nametag.visibility.other";
         if (!this.hasPermission(sender, permission)) {
            this.sendMessage(sender, this.getMessages().getNoPermission());
         } else {
            if (action.equals("show")) {
               if (viewer != null) {
                  teams.showNameTag(player, viewer, NameTagInvisibilityReason.HIDE_COMMAND, "Processing command (show)", !silent);
               } else {
                  teams.showNameTag(player, NameTagInvisibilityReason.HIDE_COMMAND, "Processing command (show)", !silent);
               }
            } else if (action.equals("hide")) {
               if (viewer != null) {
                  teams.hideNameTag(player, viewer, NameTagInvisibilityReason.HIDE_COMMAND, "Processing command (hide)", !silent);
               } else {
                  teams.hideNameTag(player, NameTagInvisibilityReason.HIDE_COMMAND, "Processing command (hide)", !silent);
               }
            } else if (action.equals("toggle")) {
               if (viewer != null) {
                  teams.toggleNameTag(player, viewer, NameTagInvisibilityReason.HIDE_COMMAND, "Processing command (toggle)", !silent);
               } else {
                  teams.toggleNameTag(player, NameTagInvisibilityReason.HIDE_COMMAND, "Processing command (toggle)", !silent);
               }
            }
         }
      }
   }

   private void processView(@NotNull NameTag teams, @Nullable TabPlayer sender, @NotNull String action, @NotNull String[] args) {
      boolean silent = args.length > 0 && args[args.length - 1].equals("-s");
      TabPlayer viewer = args.length >= 1 ? TAB.getInstance().getPlayer(args[0]) : sender;
      if (viewer == null) {
         this.sendMessage(sender, this.getMessages().getPlayerNotFound(args[0]));
      } else {
         String permission = sender == viewer ? "tab.nametag.view" : "tab.nametag.view.other";
         if (!this.hasPermission(sender, permission)) {
            this.sendMessage(sender, this.getMessages().getNoPermission());
         } else {
            if (action.equals("showview")) {
               teams.showNameTagVisibilityView(viewer, !silent);
            } else if (action.equals("hideview")) {
               teams.hideNameTagVisibilityView(viewer, !silent);
            } else {
               teams.toggleNameTagVisibilityView(viewer, !silent);
            }
         }
      }
   }

   @NotNull
   @Override
   public List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
      if (arguments.length == 1) {
         return this.getStartingArgument(Arrays.asList("show", "hide", "toggle", "showview", "hideview", "toggleview"), arguments[0]);
      }

      if (arguments.length == 2) {
         return this.getOnlinePlayers(arguments[1]);
      }

      String action = arguments[0].toLowerCase(Locale.US);
      boolean targeting = action.equals("show") || action.equals("hide") || action.equals("toggle");
      if (arguments.length == 3) {
         if (targeting) {
            return this.getOnlinePlayers(arguments[2]);
         } else {
            return !action.equals("showview") && !action.equals("hideview") && !action.equals("toggleview")
               ? Collections.emptyList()
               : this.getStartingArgument(Collections.singletonList("-s"), arguments[2]);
         }
      } else if (arguments.length == 4) {
         return targeting ? this.getStartingArgument(Collections.singletonList("-s"), arguments[3]) : Collections.emptyList();
      } else {
         return Collections.emptyList();
      }
   }
}
