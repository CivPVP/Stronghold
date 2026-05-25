package me.neznamy.tab.shared.features.pingspoof;

import java.util.Arrays;
import lombok.Generated;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class PingSpoofConfiguration {
   private final int value;

   @NotNull
   public static PingSpoofConfiguration fromSection(@NotNull ConfigurationSection section) {
      section.checkForUnknownKey(Arrays.asList("enabled", "value"));
      return new PingSpoofConfiguration(section.getInt("value", 0));
   }

   @Generated
   public int getValue() {
      return this.value;
   }

   @Generated
   public PingSpoofConfiguration(int value) {
      this.value = value;
   }
}
