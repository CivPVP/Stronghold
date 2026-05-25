package me.neznamy.tab.shared.features.playerlist;

import java.util.Arrays;
import lombok.Generated;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class TablistFormattingConfiguration {
   @NotNull
   private final String disableCondition;

   @NotNull
   public static TablistFormattingConfiguration fromSection(@NotNull ConfigurationSection section) {
      section.checkForUnknownKey(Arrays.asList("enabled", "disable-condition"));
      return new TablistFormattingConfiguration(section.getString("disable-condition", "%world%=disabledworld"));
   }

   @NotNull
   @Generated
   public String getDisableCondition() {
      return this.disableCondition;
   }

   @Generated
   public TablistFormattingConfiguration(@NotNull String disableCondition) {
      if (disableCondition == null) {
         throw new NullPointerException("disableCondition is marked non-null but is null");
      }

      this.disableCondition = disableCondition;
   }
}
