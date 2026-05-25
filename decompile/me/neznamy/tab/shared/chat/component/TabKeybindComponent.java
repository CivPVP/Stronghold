package me.neznamy.tab.shared.chat.component;

import lombok.Generated;
import org.jetbrains.annotations.NotNull;

public class TabKeybindComponent extends TabComponent {
   @NotNull
   protected final String keybind;

   @NotNull
   @Override
   public String toLegacyText() {
      return this.keybind;
   }

   @NotNull
   @Generated
   public String getKeybind() {
      return this.keybind;
   }

   @Generated
   TabKeybindComponent(@NotNull String keybind) {
      if (keybind == null) {
         throw new NullPointerException("keybind is marked non-null but is null");
      }

      this.keybind = keybind;
   }
}
