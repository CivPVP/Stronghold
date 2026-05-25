package me.neznamy.tab.shared.chat.component;

import lombok.Generated;
import org.jetbrains.annotations.NotNull;

public class TabTranslatableComponent extends TabComponent {
   @NotNull
   protected final String key;

   @NotNull
   @Override
   public String toLegacyText() {
      return this.key;
   }

   @NotNull
   @Generated
   public String getKey() {
      return this.key;
   }

   @Generated
   TabTranslatableComponent(@NotNull String key) {
      if (key == null) {
         throw new NullPointerException("key is marked non-null but is null");
      }

      this.key = key;
   }
}
