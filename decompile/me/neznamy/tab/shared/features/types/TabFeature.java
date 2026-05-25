package me.neznamy.tab.shared.features.types;

import lombok.Generated;
import org.jetbrains.annotations.NotNull;

public abstract class TabFeature {
   private boolean active = true;

   @NotNull
   public abstract String getFeatureName();

   public void deactivate() {
      this.active = false;
   }

   public void ensureActive() {
      if (!this.active) {
         throw new IllegalStateException("This instance got discarded because plugin was reloaded. Obtain a new instance.");
      }
   }

   @Generated
   public boolean isActive() {
      return this.active;
   }
}
