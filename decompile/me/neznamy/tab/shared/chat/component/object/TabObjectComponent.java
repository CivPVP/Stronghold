package me.neznamy.tab.shared.chat.component.object;

import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.chat.component.TabComponent;
import org.jetbrains.annotations.NotNull;

public class TabObjectComponent extends TabComponent {
   public static final String ERROR_MESSAGE = "<Object components were added in 1.21.9>";
   @NonNull
   protected final ObjectInfo contents;

   @NotNull
   @Override
   public String toLegacyText() {
      return "<Object components were added in 1.21.9>";
   }

   @NonNull
   @Generated
   public ObjectInfo getContents() {
      return this.contents;
   }

   @Generated
   public TabObjectComponent(@NonNull ObjectInfo contents) {
      if (contents == null) {
         throw new NullPointerException("contents is marked non-null but is null");
      }

      this.contents = contents;
   }
}
