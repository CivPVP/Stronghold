package me.neznamy.tab.platforms.fabric.hook;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import java.util.Arrays;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import org.jetbrains.annotations.NotNull;

public class FabricTabExpansion implements TabExpansion {
   public FabricTabExpansion() {
      for (String placeholder : Arrays.asList(
         "tabprefix",
         "tabsuffix",
         "tagprefix",
         "tagsuffix",
         "customtabname",
         "tabprefix_raw",
         "tabsuffix_raw",
         "tagprefix_raw",
         "tagsuffix_raw",
         "customtabname_raw",
         "scoreboard_name",
         "scoreboard_visible",
         "bossbar_visible",
         "nametag_visibility"
      )) {
         this.registerPlaceholder(placeholder, (ctx, arg) -> {
            if (!ctx.hasPlayer()) {
               return PlaceholderResult.invalid("No player!");
            }

            TabPlayer player = TAB.getInstance().getPlayer(ctx.player().method_5667());
            return PlaceholderResult.value(player.expansionValues.get(placeholder));
         });
      }

      this.registerPlaceholder(
         "replace",
         (ctx, arg) -> {
            if (!ctx.hasPlayer()) {
               return PlaceholderResult.invalid("No player!");
            }

            if (arg == null) {
               return PlaceholderResult.invalid("No placeholder!");
            }

            String text = "%" + arg + "%";

            String textBefore;
            do {
               textBefore = text;

               for (String placeholderx : PlaceholderManagerImpl.detectPlaceholders(text)) {
                  text = text.replace(
                     placeholderx,
                     TAB.getInstance()
                        .getPlaceholderManager()
                        .findReplacement(
                           placeholderx, Placeholders.parseText(class_2561.method_43470(placeholderx), PlaceholderContext.of(ctx.player())).getString()
                        )
                  );
               }
            } while (!textBefore.equals(text));

            return PlaceholderResult.value(text);
         }
      );
      this.registerPlaceholder("placeholder", (ctx, arg) -> {
         if (arg == null) {
            return PlaceholderResult.invalid("No placeholder!");
         }

         TabPlayer player = ctx.hasPlayer() ? TAB.getInstance().getPlayer(ctx.player().method_5667()) : null;
         String placeholderx = "%" + arg + "%";
         PlaceholderManagerImpl manager = TAB.getInstance().getPlaceholderManager();
         manager.addUsedPlaceholder(placeholderx, manager);
         return PlaceholderResult.value(manager.getPlaceholder(placeholderx).getLastValue(player));
      });
   }

   private void registerPlaceholder(String identifier, PlaceholderHandler handler) {
      Placeholders.register(class_2960.method_12829("tab:" + identifier), handler);
   }

   @Override
   public void setValue(@NotNull TabPlayer player, @NotNull String key, @NotNull String value) {
      player.expansionValues.put(key, value);
   }

   @Override
   public void unregisterExpansion() {
   }
}
