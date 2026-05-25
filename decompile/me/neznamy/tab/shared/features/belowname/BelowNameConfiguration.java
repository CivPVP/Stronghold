package me.neznamy.tab.shared.features.belowname;

import java.util.Arrays;
import lombok.Generated;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import org.jetbrains.annotations.NotNull;

public class BelowNameConfiguration {
   @NotNull
   private final String value;
   @NotNull
   private final String title;
   @NotNull
   private final String fancyValue;
   @NotNull
   private final String fancyValueDefault;
   @NotNull
   private final String disableCondition;

   @NotNull
   public static BelowNameConfiguration fromSection(@NotNull ConfigurationSection section) {
      section.checkForUnknownKey(Arrays.asList("enabled", "value", "title", "fancy-value-default", "fancy-value", "disable-condition"));
      String title = section.getString("title", "Health");
      if (title.contains("%") && !title.contains("%animation") && !title.contains("%condition")) {
         section.startupWarn(
            "Belowname title is set to \""
               + title
               + "\", however, the feature cannot display different title on different players due to a minecraft limitation. Placeholders will be parsed for viewing player. To display per-player placeholders correctly, move them to fancy-value and only keep static text in title (this only works on 1.20.3+, on older versions you will need to use \"value\", which only supports numbers)."
         );
      }

      String value = section.getObject("value", "%health%").toString();
      if (value.isEmpty()) {
         section.startupWarn("Belowname value is set to be empty, but the configured value must evaluate to a number. Using 0.");
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

      return new BelowNameConfiguration(
         value,
         title,
         section.getString("fancy-value", "&c%health%"),
         section.getString("fancy-value-default", "NPC"),
         section.getString("disable-condition", "%world%=disabledworld")
      );
   }

   @NotNull
   @Generated
   public String getValue() {
      return this.value;
   }

   @NotNull
   @Generated
   public String getTitle() {
      return this.title;
   }

   @NotNull
   @Generated
   public String getFancyValue() {
      return this.fancyValue;
   }

   @NotNull
   @Generated
   public String getFancyValueDefault() {
      return this.fancyValueDefault;
   }

   @NotNull
   @Generated
   public String getDisableCondition() {
      return this.disableCondition;
   }

   @Generated
   public BelowNameConfiguration(
      @NotNull String value, @NotNull String title, @NotNull String fancyValue, @NotNull String fancyValueDefault, @NotNull String disableCondition
   ) {
      if (value == null) {
         throw new NullPointerException("value is marked non-null but is null");
      }

      if (title == null) {
         throw new NullPointerException("title is marked non-null but is null");
      }

      if (fancyValue == null) {
         throw new NullPointerException("fancyValue is marked non-null but is null");
      }

      if (fancyValueDefault == null) {
         throw new NullPointerException("fancyValueDefault is marked non-null but is null");
      }

      if (disableCondition == null) {
         throw new NullPointerException("disableCondition is marked non-null but is null");
      }

      this.value = value;
      this.title = title;
      this.fancyValue = fancyValue;
      this.fancyValueDefault = fancyValueDefault;
      this.disableCondition = disableCondition;
   }
}
