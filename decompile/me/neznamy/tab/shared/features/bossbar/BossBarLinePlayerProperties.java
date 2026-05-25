package me.neznamy.tab.shared.features.bossbar;

import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.Property;

public class BossBarLinePlayerProperties {
   @NonNull
   public final Property textProperty;
   @NonNull
   public final Property progressProperty;
   @NonNull
   public final Property colorProperty;
   @NonNull
   public final Property styleProperty;

   @Generated
   public BossBarLinePlayerProperties(
      @NonNull Property textProperty, @NonNull Property progressProperty, @NonNull Property colorProperty, @NonNull Property styleProperty
   ) {
      if (textProperty == null) {
         throw new NullPointerException("textProperty is marked non-null but is null");
      }

      if (progressProperty == null) {
         throw new NullPointerException("progressProperty is marked non-null but is null");
      }

      if (colorProperty == null) {
         throw new NullPointerException("colorProperty is marked non-null but is null");
      }

      if (styleProperty == null) {
         throw new NullPointerException("styleProperty is marked non-null but is null");
      }

      this.textProperty = textProperty;
      this.progressProperty = progressProperty;
      this.colorProperty = colorProperty;
      this.styleProperty = styleProperty;
   }
}
