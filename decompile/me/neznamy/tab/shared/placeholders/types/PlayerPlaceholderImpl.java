package me.neznamy.tab.shared.placeholders.types;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerPlaceholderImpl extends TabPlaceholder implements PlayerPlaceholder {
   @NonNull
   private final Function<TabPlayer, String> function;

   public PlayerPlaceholderImpl(@NonNull String identifier, int refresh, @NonNull Function<TabPlayer, String> function) {
      super(identifier, refresh);
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      }

      if (function == null) {
         throw new NullPointerException("function is marked non-null but is null");
      }

      if (identifier.startsWith("%rel_")) {
         throw new IllegalArgumentException("\"rel_\" is reserved for relational placeholder identifiers");
      }

      this.function = function;
   }

   @Override
   public void update(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.updateValue(player, this.request((me.neznamy.tab.shared.platform.TabPlayer)player));
   }

   @Override
   public void updateValue(@NonNull TabPlayer player, @Nullable String value) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (this.hasValueChanged((me.neznamy.tab.shared.platform.TabPlayer)player, value, true)) {
         if (!player.isLoaded()) {
            return;
         }

         for (RefreshableFeature r : TAB.getInstance().getPlaceholderManager().getPlaceholderUsage(this.identifier)) {
            TimedCaughtTask task = new TimedCaughtTask(
               TAB.getInstance().getCpu(),
               () -> r.refresh((me.neznamy.tab.shared.platform.TabPlayer)player, false),
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

   public static void bulkUpdateValues(@NotNull TabPlayer player, @NotNull Map<PlayerPlaceholderImpl, String> values) {
      Set<RefreshableFeature> features = new HashSet<>();

      for (Entry<PlayerPlaceholderImpl, String> entry : values.entrySet()) {
         if (entry.getKey().hasValueChanged(player, entry.getValue(), true)) {
            features.addAll(TAB.getInstance().getPlaceholderManager().getPlaceholderUsage(entry.getKey().identifier));
         }
      }

      if (player.isLoaded()) {
         for (RefreshableFeature r : features) {
            TimedCaughtTask task = new TimedCaughtTask(
               TAB.getInstance().getCpu(), () -> r.refresh(player, false), r.getFeatureName(), r.getRefreshDisplayName()
            );
            if (r instanceof CustomThreaded) {
               ((CustomThreaded)r).getCustomThread().execute(task);
            } else {
               task.run();
            }
         }
      }
   }

   public boolean hasValueChanged(@NotNull TabPlayer p, @Nullable String value, boolean updateParents) {
      if (value == null) {
         return false;
      }

      if ("<ERROR>".equals(value)) {
         return false;
      }

      String newValue = this.replacements.findReplacement(this.setPlaceholders(value, p));
      String lastValue = p.lastPlaceholderValues.put(this, newValue);
      if (!newValue.equals(lastValue)) {
         if (updateParents) {
            this.updateParents(p);
         }

         TAB.getInstance().getPlaceholderManager().getTabExpansion().setPlaceholderValue(p, this.identifier, newValue);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void updateFromNested(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.hasValueChanged(player, this.request(player), true);
   }

   @NotNull
   @Override
   public synchronized String getLastValue(@Nullable TabPlayer p) {
      if (p == null) {
         return this.identifier;
      }

      String value = p.lastPlaceholderValues.get(this);
      if (value != null) {
         return value;
      }

      p.lastPlaceholderValues.put(this, this.replacements.findReplacement(this.identifier));
      this.hasValueChanged(p, this.request(p), false);
      return p.lastPlaceholderValues.get(this);
   }

   @NotNull
   @Override
   public String getLastValueSafe(@NotNull TabPlayer player) {
      return player.lastPlaceholderValues.getOrDefault(this, this.identifier);
   }

   public String request(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      long time = System.currentTimeMillis();

      try {
         return this.function.apply(p);
      } catch (Throwable t) {
         TAB.getInstance()
            .getErrorManager()
            .placeholderError("Player placeholder " + this.identifier + " generated an error when setting for player " + p.getName(), t);
         return "<ERROR>";
      } finally {
         long timeDiff = System.currentTimeMillis() - time;
         if (timeDiff > 50L) {
            TAB.getInstance().debug("Placeholder " + this.identifier + " took " + timeDiff + "ms to return value for player " + p.getName());
         }
      }
   }
}
