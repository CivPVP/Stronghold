package me.neznamy.tab.shared.placeholders.types;

import java.util.function.Supplier;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.api.placeholder.ServerPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerPlaceholderImpl extends TabPlaceholder implements ServerPlaceholder {
   private final Supplier<String> supplier;
   @NotNull
   private String lastValue = this.identifier;

   public ServerPlaceholderImpl(@NonNull String identifier, int refresh, @NonNull Supplier<String> supplier) {
      super(identifier, refresh);
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      }

      if (supplier == null) {
         throw new NullPointerException("supplier is marked non-null but is null");
      }

      if (identifier.startsWith("%rel_")) {
         throw new IllegalArgumentException("\"rel_\" is reserved for relational placeholder identifiers");
      }

      this.supplier = supplier;
      this.hasValueChanged(this.request());
   }

   @Override
   public void update() {
      this.updateValue(this.request());
   }

   @Override
   public void updateValue(@Nullable String value) {
      if (this.hasValueChanged(value)) {
         for (RefreshableFeature r : TAB.getInstance().getPlaceholderManager().getPlaceholderUsage(this.identifier)) {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
               if (!all.isLoaded()) {
                  return;
               }

               TimedCaughtTask task = new TimedCaughtTask(
                  TAB.getInstance().getCpu(), () -> r.refresh(all, false), r.getFeatureName(), r.getRefreshDisplayName()
               );
               if (r instanceof CustomThreaded) {
                  ((CustomThreaded)r).getCustomThread().execute(task);
               } else {
                  task.run();
               }
            }
         }
      }
   }

   public boolean hasValueChanged(@Nullable String value) {
      if (value == null) {
         return false;
      }

      String newValue = this.setPlaceholders(this.replacements.findReplacement(value), null);
      if (!"<ERROR>".equals(newValue) && !this.lastValue.equals(newValue)) {
         this.lastValue = newValue;

         for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
            this.updateParents(player);
            TAB.getInstance().getPlaceholderManager().getTabExpansion().setPlaceholderValue(player, this.identifier, newValue);
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public void updateFromNested(@NonNull TabPlayer unused) {
      if (unused == null) {
         throw new NullPointerException("unused is marked non-null but is null");
      }

      this.hasValueChanged(this.request());
   }

   @NotNull
   @Override
   public String getLastValue(@Nullable TabPlayer p) {
      return this.lastValue;
   }

   @NotNull
   @Override
   public String getLastValueSafe(@NotNull TabPlayer player) {
      return this.lastValue;
   }

   @Nullable
   public String request() {
      long time = System.currentTimeMillis();

      try {
         return this.supplier.get();
      } catch (Throwable t) {
         TAB.getInstance().getErrorManager().placeholderError("Server placeholder " + this.identifier + " generated an error", t);
         return "<ERROR>";
      } finally {
         long timeDiff = System.currentTimeMillis() - time;
         if (timeDiff > 50L) {
            TAB.getInstance().debug("Placeholder " + this.identifier + " took " + timeDiff + "ms to return value");
         }
      }
   }

   @NotNull
   @Generated
   public String getLastValue() {
      return this.lastValue;
   }
}
