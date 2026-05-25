package me.neznamy.tab.shared.placeholders.types;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiFunction;
import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.RelationalPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RelationalPlaceholderImpl extends TabPlaceholder implements RelationalPlaceholder {
   @NonNull
   private final BiFunction<TabPlayer, TabPlayer, String> function;

   public RelationalPlaceholderImpl(@NonNull String identifier, int refresh, @NonNull BiFunction<TabPlayer, TabPlayer, String> function) {
      super(identifier, refresh);
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      }

      if (function == null) {
         throw new NullPointerException("function is marked non-null but is null");
      }

      if (!identifier.startsWith("%rel_")) {
         throw new IllegalArgumentException("Relational placeholder identifiers must start with \"rel_\"");
      }

      this.function = function;
   }

   @Override
   public void update(@NonNull TabPlayer viewer, @NonNull TabPlayer target) {
      if (viewer == null) {
         throw new NullPointerException("viewer is marked non-null but is null");
      }

      if (target == null) {
         throw new NullPointerException("target is marked non-null but is null");
      }

      this.updateValue(viewer, target, this.request((me.neznamy.tab.shared.platform.TabPlayer)viewer, (me.neznamy.tab.shared.platform.TabPlayer)target));
   }

   @Override
   public void updateValue(@NonNull TabPlayer viewer, @NonNull TabPlayer target, @Nullable String value) {
      if (viewer == null) {
         throw new NullPointerException("viewer is marked non-null but is null");
      }

      if (target == null) {
         throw new NullPointerException("target is marked non-null but is null");
      }

      if (this.hasValueChanged((me.neznamy.tab.shared.platform.TabPlayer)viewer, (me.neznamy.tab.shared.platform.TabPlayer)target, value)) {
         for (RefreshableFeature r : TAB.getInstance().getPlaceholderManager().getPlaceholderUsage(this.identifier)) {
            TimedCaughtTask task = new TimedCaughtTask(
               TAB.getInstance().getCpu(),
               () -> r.refresh((me.neznamy.tab.shared.platform.TabPlayer)target, true),
               r.getFeatureName(),
               r.getRefreshDisplayName()
            );
            if (r instanceof CustomThreaded) {
               ((CustomThreaded)r).getCustomThread().execute(task);
            } else {
               task.run();
            }
         }
      }
   }

   public boolean hasValueChanged(@NonNull TabPlayer viewer, @NonNull TabPlayer target, @Nullable String value) {
      if (viewer == null) {
         throw new NullPointerException("viewer is marked non-null but is null");
      } else if (target == null) {
         throw new NullPointerException("target is marked non-null but is null");
      } else if (value == null) {
         return false;
      } else {
         String newValue = this.replacements.findReplacement(value);
         Map<me.neznamy.tab.shared.platform.TabPlayer, String> viewerMap = viewer.lastRelationalValues
            .computeIfAbsent(this, v -> Collections.synchronizedMap(new WeakHashMap<>()));
         if (!viewerMap.getOrDefault(target, this.identifier).equals(newValue)) {
            viewerMap.put(target, newValue);
            this.updateParents(viewer);
            this.updateParents(target);
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public void updateFromNested(@NonNull TabPlayer viewer) {
      if (viewer == null) {
         throw new NullPointerException("viewer is marked non-null but is null");
      }

      Set<RefreshableFeature> usage = TAB.getInstance().getPlaceholderManager().getPlaceholderUsage(this.identifier);

      for (me.neznamy.tab.shared.platform.TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
         String value = this.request(viewer, target);
         String s = this.replacements.findReplacement(String.valueOf(value));
         viewer.lastRelationalValues.computeIfAbsent(this, v -> Collections.synchronizedMap(new WeakHashMap<>())).put(target, s);
         if (!target.isLoaded()) {
            return;
         }

         for (RefreshableFeature f : usage) {
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(), () -> f.refresh(target, true), f.getFeatureName(), f.getRefreshDisplayName());
            if (f instanceof CustomThreaded) {
               ((CustomThreaded)f).getCustomThread().execute(task);
            } else {
               task.run();
            }
         }

         this.updateParents(target);
      }

      if (viewer.isLoaded()) {
         for (RefreshableFeature f : usage) {
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(), () -> f.refresh(viewer, true), f.getFeatureName(), f.getRefreshDisplayName());
            if (f instanceof CustomThreaded) {
               ((CustomThreaded)f).getCustomThread().execute(task);
            } else {
               task.run();
            }
         }

         this.updateParents(viewer);
      }
   }

   public String getLastValue(@NonNull TabPlayer viewer, @NonNull TabPlayer target) {
      if (viewer == null) {
         throw new NullPointerException("viewer is marked non-null but is null");
      } else if (target == null) {
         throw new NullPointerException("target is marked non-null but is null");
      } else {
         return this.setPlaceholders(
            EnumChatFormat.color(
               viewer.lastRelationalValues
                  .computeIfAbsent(this, v -> Collections.synchronizedMap(new WeakHashMap<>()))
                  .computeIfAbsent(target, t -> this.retrieveValue(viewer, target))
            ),
            target
         );
      }
   }

   @NotNull
   private String retrieveValue(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
      String output = this.request(viewer, target);
      if (output == null) {
         output = this.identifier;
      }

      return this.replacements.findReplacement(output);
   }

   @NotNull
   @Override
   public String getLastValue(@Nullable TabPlayer p) {
      return this.identifier;
   }

   @NotNull
   @Override
   public String getLastValueSafe(@NotNull TabPlayer player) {
      return this.identifier;
   }

   @Nullable
   public String request(@NonNull TabPlayer viewer, @NonNull TabPlayer target) {
      if (viewer == null) {
         throw new NullPointerException("viewer is marked non-null but is null");
      }

      if (target == null) {
         throw new NullPointerException("target is marked non-null but is null");
      }

      long time = System.currentTimeMillis();

      try {
         return this.function.apply(viewer, target);
      } catch (Throwable t) {
         TAB.getInstance()
            .getErrorManager()
            .placeholderError(
               "Relational placeholder " + this.identifier + " generated an error when setting for players " + viewer.getName() + " and " + target.getName(), t
            );
         return "<ERROR>";
      } finally {
         long timeDiff = System.currentTimeMillis() - time;
         if (timeDiff > 50L) {
            TAB.getInstance()
               .debug("Placeholder " + this.identifier + " took " + timeDiff + "ms to return value for " + viewer.getName() + " and " + target.getName());
         }
      }
   }
}
