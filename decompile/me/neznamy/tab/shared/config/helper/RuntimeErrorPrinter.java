package me.neznamy.tab.shared.config.helper;

import java.util.Collection;
import lombok.NonNull;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.features.sorting.types.SortingType;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;

public class RuntimeErrorPrinter {
   public void invalidBossBarProperty(
      @NonNull BossBar bossBar,
      @NonNull String output,
      @NonNull String configuredValue,
      @NonNull TabPlayer player,
      @NonNull String property,
      @NonNull String expectation
   ) {
      if (bossBar == null) {
         throw new NullPointerException("bossBar is marked non-null but is null");
      }

      if (output == null) {
         throw new NullPointerException("output is marked non-null but is null");
      }

      if (configuredValue == null) {
         throw new NullPointerException("configuredValue is marked non-null but is null");
      }

      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (property == null) {
         throw new NullPointerException("property is marked non-null but is null");
      }

      if (expectation == null) {
         throw new NullPointerException("expectation is marked non-null but is null");
      }

      if (!(player instanceof ProxyTabPlayer) || ((ProxyTabPlayer)player).isBridgeConnected()) {
         if (configuredValue.contains("%")) {
            this.error(
               String.format(
                  "Placeholder \"%s\" used in %s of BossBar \"%s\" returned \"%s\" for player %s, which cannot be evaluated to %s.",
                  configuredValue,
                  property,
                  bossBar.getName(),
                  output,
                  player.getName(),
                  expectation
               )
            );
         } else {
            this.error(
               String.format(
                  "BossBar \"%s\" has invalid input configured for %s (\"%s\"). Expecting %s or a placeholder returning one.",
                  bossBar.getName(),
                  property,
                  configuredValue,
                  expectation
               )
            );
         }
      }
   }

   public void invalidInputForNumericSorting(@NonNull SortingType type, @NonNull String placeholder, @NonNull String output, @NonNull TabPlayer player) {
      if (type == null) {
         throw new NullPointerException("type is marked non-null but is null");
      }

      if (placeholder == null) {
         throw new NullPointerException("placeholder is marked non-null but is null");
      }

      if (output == null) {
         throw new NullPointerException("output is marked non-null but is null");
      }

      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (!(player instanceof ProxyTabPlayer) || ((ProxyTabPlayer)player).isBridgeConnected()) {
         this.error(
            String.format(
               "Placeholder %s used in sorting type %s returned \"%s\" for player %s, which is not a valid number.",
               placeholder,
               type.getDisplayName(),
               output,
               player.getName()
            )
         );
      }
   }

   public void invalidNumberForCondition(@NonNull String placeholder, @NonNull String output, @NonNull TabPlayer player) {
      if (placeholder == null) {
         throw new NullPointerException("placeholder is marked non-null but is null");
      }

      if (output == null) {
         throw new NullPointerException("output is marked non-null but is null");
      }

      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (!(player instanceof ProxyTabPlayer) || ((ProxyTabPlayer)player).isBridgeConnected()) {
         this.error(
            String.format(
               "Placeholder %s used in a numeric condition returned \"%s\" for player %s, which is not a valid number.", placeholder, output, player.getName()
            )
         );
      }
   }

   public void invalidNumberForBelowName(@NonNull TabPlayer target, @NonNull String configuredValue, @NonNull String output) {
      if (target == null) {
         throw new NullPointerException("target is marked non-null but is null");
      }

      if (configuredValue == null) {
         throw new NullPointerException("configuredValue is marked non-null but is null");
      }

      if (output == null) {
         throw new NullPointerException("output is marked non-null but is null");
      }

      if (!(target instanceof ProxyTabPlayer) || ((ProxyTabPlayer)target).isBridgeConnected()) {
         String msg = String.format(
            "Belowname value is configured to show \"%s\", but returned \"%s\" for player %s, which cannot be evaluated to a number.",
            configuredValue,
            output,
            target.getName()
         );
         if (output.contains("%")) {
            msg = msg + " Did you perhaps forget to download a PlaceholderAPI expansion?";
         }

         this.error(msg);
      }
   }

   public void floatInBelowName(@NonNull TabPlayer target, @NonNull String configuredValue, @NonNull String output) {
      if (target == null) {
         throw new NullPointerException("target is marked non-null but is null");
      }

      if (configuredValue == null) {
         throw new NullPointerException("configuredValue is marked non-null but is null");
      }

      if (output == null) {
         throw new NullPointerException("output is marked non-null but is null");
      }

      if (!(target instanceof ProxyTabPlayer) || ((ProxyTabPlayer)target).isBridgeConnected()) {
         this.error(
            String.format(
               "Belowname value is configured to show \"%s\", but returned \"%s\" for player %s, which is a decimal number. Truncating to an integer.",
               configuredValue,
               output,
               target.getName()
            )
         );
      }
   }

   public void invalidNumberForPlayerlistObjective(@NonNull TabPlayer target, @NonNull String configuredValue, @NonNull String output) {
      if (target == null) {
         throw new NullPointerException("target is marked non-null but is null");
      }

      if (configuredValue == null) {
         throw new NullPointerException("configuredValue is marked non-null but is null");
      }

      if (output == null) {
         throw new NullPointerException("output is marked non-null but is null");
      }

      if (!(target instanceof ProxyTabPlayer) || ((ProxyTabPlayer)target).isBridgeConnected()) {
         String msg = String.format(
            "Playerlist objective value is configured to show \"%s\", but returned \"%s\" for player %s, which cannot be evaluated to a number.",
            configuredValue,
            output,
            target.getName()
         );
         if (output.contains("%")) {
            msg = msg + " Did you perhaps forget to download a PlaceholderAPI expansion?";
         }

         this.error(msg);
      }
   }

   public void floatInPlayerlistObjective(@NonNull TabPlayer target, @NonNull String configuredValue, @NonNull String output) {
      if (target == null) {
         throw new NullPointerException("target is marked non-null but is null");
      }

      if (configuredValue == null) {
         throw new NullPointerException("configuredValue is marked non-null but is null");
      }

      if (output == null) {
         throw new NullPointerException("output is marked non-null but is null");
      }

      if (!(target instanceof ProxyTabPlayer) || ((ProxyTabPlayer)target).isBridgeConnected()) {
         this.error(
            String.format(
               "Playerlist objective value is configured to show \"%s\", but returned \"%s\" for player %s, which is a decimal number. Truncating to an integer.",
               configuredValue,
               output,
               target.getName()
            )
         );
      }
   }

   public void groupNotInSortingList(@NonNull Collection<String> list, @NonNull String group, @NonNull TabPlayer player) {
      if (list == null) {
         throw new NullPointerException("list is marked non-null but is null");
      }

      if (group == null) {
         throw new NullPointerException("group is marked non-null but is null");
      }

      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (!(player instanceof ProxyTabPlayer) || ((ProxyTabPlayer)player).isBridgeConnected()) {
         this.error(
            String.format(
               "Player %s's group (%s) is not in sorting list! Sorting list: %s. Player will be sorted on the bottom.",
               player.getName(),
               group,
               String.join(",", list)
            )
         );
      }
   }

   public void noPermissionFromSortingList(@NonNull Collection<String> list, @NonNull TabPlayer player) {
      if (list == null) {
         throw new NullPointerException("list is marked non-null but is null");
      }

      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.error(
         String.format(
            "Player %s does not have any of the defined permissions in sorting list! Sorting list: %s. Player will be sorted on the bottom.",
            player.getName(),
            String.join(",", list)
         )
      );
   }

   public void valueNotInPredefinedValues(@NonNull String placeholder, @NonNull Collection<String> list, @NonNull String output, @NonNull TabPlayer player) {
      if (placeholder == null) {
         throw new NullPointerException("placeholder is marked non-null but is null");
      }

      if (list == null) {
         throw new NullPointerException("list is marked non-null but is null");
      }

      if (output == null) {
         throw new NullPointerException("output is marked non-null but is null");
      }

      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.error(
         String.format(
            "Sorting placeholder %s with pre-defined values [%s] returned \"%s\" for player %s, which is not defined. Player will be sorted on the bottom.",
            placeholder,
            String.join(",", list),
            output,
            player.getName()
         )
      );
   }

   public void unknownPlayerSkin(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      this.error("Failed to load skin by player: No user with the name '" + name + "' was found");
   }

   public void error(@NonNull String message) {
      if (message == null) {
         throw new NullPointerException("message is marked non-null but is null");
      }

      TAB.getInstance().getPlatform().logWarn(new TabTextComponent(message.replace('§', '&'), TabTextColor.RED));
   }
}
