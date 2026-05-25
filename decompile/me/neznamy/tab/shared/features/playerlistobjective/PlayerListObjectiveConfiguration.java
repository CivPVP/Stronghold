package me.neznamy.tab.shared.features.playerlistobjective;

import java.util.Arrays;
import lombok.Generated;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.NotNull;

public class PlayerListObjectiveConfiguration {
   @NotNull
   private final String value;
   @NotNull
   private final String fancyValue;
   @NotNull
   private final String title;
   @NotNull
   private final String disableCondition;
   @NotNull
   private final Scoreboard.HealthDisplay healthDisplay;

   @NotNull
   public static PlayerListObjectiveConfiguration fromSection(@NotNull ConfigurationSection section) {
      section.checkForUnknownKey(Arrays.asList("enabled", "value", "fancy-value", "title", "render-type", "disable-condition"));
      String value = section.getObject("value", "%ping%").toString();
      if (value.isEmpty()) {
         section.startupWarn("Playerlist objective value is set to be empty, but the configured value must evaluate to a number. Using 0.");
         value = "0";
      }

      String strippedValue = value;

      for (String placeholder : PlaceholderManagerImpl.detectPlaceholders(strippedValue)) {
         strippedValue = strippedValue.replace(placeholder, "");
      }

      if (!strippedValue.isEmpty()) {
         try {
            Integer.parseInt(strippedValue);
         } catch (NumberFormatException e) {
            section.startupWarn(
               "\"value\" is set to \""
                  + value
                  + "\", but this will never evaluate to a number. If you want text without limits, update to 1.20.3+ and use fancy-value. If you already did, set \"value\" to 0 as it is not displayed anyway."
            );
         }
      }

      String renderTypeString = section.getString("render-type", "INTEGER");

      Scoreboard.HealthDisplay healthDisplay;
      try {
         healthDisplay = Scoreboard.HealthDisplay.valueOf(renderTypeString);
      } catch (IllegalArgumentException e) {
         section.startupWarn(
            "\""
               + renderTypeString
               + "\" is not a valid render type. Valid options are: "
               + Arrays.deepToString(Scoreboard.HealthDisplay.values())
               + ". Using INTEGER"
         );
         healthDisplay = Scoreboard.HealthDisplay.INTEGER;
      }

      return new PlayerListObjectiveConfiguration(
         value,
         section.getString("fancy-value", "&7Ping: %ping%"),
         section.getString("title", "TAB"),
         section.getString("disable-condition", "%world%=disabledworld"),
         healthDisplay
      );
   }

   @NotNull
   @Generated
   public String getValue() {
      return this.value;
   }

   @NotNull
   @Generated
   public String getFancyValue() {
      return this.fancyValue;
   }

   @NotNull
   @Generated
   public String getTitle() {
      return this.title;
   }

   @NotNull
   @Generated
   public String getDisableCondition() {
      return this.disableCondition;
   }

   @NotNull
   @Generated
   public Scoreboard.HealthDisplay getHealthDisplay() {
      return this.healthDisplay;
   }

   @Generated
   public PlayerListObjectiveConfiguration(
      @NotNull String value,
      @NotNull String fancyValue,
      @NotNull String title,
      @NotNull String disableCondition,
      @NotNull Scoreboard.HealthDisplay healthDisplay
   ) {
      if (value == null) {
         throw new NullPointerException("value is marked non-null but is null");
      }

      if (fancyValue == null) {
         throw new NullPointerException("fancyValue is marked non-null but is null");
      }

      if (title == null) {
         throw new NullPointerException("title is marked non-null but is null");
      }

      if (disableCondition == null) {
         throw new NullPointerException("disableCondition is marked non-null but is null");
      }

      if (healthDisplay == null) {
         throw new NullPointerException("healthDisplay is marked non-null but is null");
      }

      this.value = value;
      this.fancyValue = fancyValue;
      this.title = title;
      this.disableCondition = disableCondition;
      this.healthDisplay = healthDisplay;
   }
}
