package me.neznamy.tab.shared.chat;

import java.util.Arrays;
import lombok.Generated;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class ComponentConfiguration {
   private final boolean minimessageSupport;
   private final boolean disableShadowForHeads;

   @NotNull
   public static ComponentConfiguration fromSection(@NotNull ConfigurationSection section) {
      section.checkForUnknownKey(Arrays.asList("minimessage-support", "disable-shadow-for-heads"));
      return new ComponentConfiguration(section.getBoolean("minimessage-support", true), section.getBoolean("disable-shadow-for-heads", true));
   }

   @Generated
   public boolean isMinimessageSupport() {
      return this.minimessageSupport;
   }

   @Generated
   public boolean isDisableShadowForHeads() {
      return this.disableShadowForHeads;
   }

   @Generated
   public ComponentConfiguration(boolean minimessageSupport, boolean disableShadowForHeads) {
      this.minimessageSupport = minimessageSupport;
      this.disableShadowForHeads = disableShadowForHeads;
   }
}
