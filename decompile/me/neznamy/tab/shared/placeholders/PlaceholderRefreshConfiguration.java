package me.neznamy.tab.shared.placeholders;

import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class PlaceholderRefreshConfiguration {
   private final int defaultInterval;
   @NotNull
   private final Map<String, Integer> refreshIntervals;

   public int getRefreshInterval(@NotNull String identifier) {
      return this.refreshIntervals.getOrDefault(identifier, this.defaultInterval);
   }

   public int getRefreshInterval(@NotNull String identifier, int defaultInterval) {
      return this.refreshIntervals.getOrDefault(identifier, defaultInterval);
   }

   @NotNull
   public static PlaceholderRefreshConfiguration fromSection(@NotNull ConfigurationSection section) {
      int defaultInterval = section.getInt("default-refresh-interval", 500);
      Map<String, Integer> refreshIntervals = new HashMap<>();

      for (Object placeholder : section.getKeys()) {
         String identifier = placeholder.toString();
         if (!identifier.equals("default-refresh-interval")) {
            if (identifier.startsWith("%") && identifier.endsWith("%")) {
               refreshIntervals.put(identifier, fixInterval(section, identifier, defaultInterval));
            } else {
               section.startupWarn(
                  "PlaceholderAPI refresh intervals have a value for \""
                     + identifier
                     + "\", which is not a valid placeholder pattern (placeholders must start and end with %)"
               );
            }
         }
      }

      return new PlaceholderRefreshConfiguration(defaultInterval, refreshIntervals);
   }

   private static int fixInterval(@NotNull ConfigurationSection section, @NotNull String identifier, int defaultInterval) {
      Object value = section.getObject(identifier);
      if (value == null) {
         section.startupWarn(
            "Refresh interval of " + identifier + " is set to null. Define a valid value or remove it if you don't want to override default value."
         );
         return 50;
      } else if (!(value instanceof Integer)) {
         section.startupWarn("Refresh interval configured for \"" + identifier + "\" is not a valid number (" + value.getClass().getSimpleName() + ").");
         return 500;
      } else {
         int interval = (Integer)value;
         if (interval == defaultInterval) {
            section.hint("Refresh interval of " + identifier + " is same as default interval, therefore there is no need to override it.");
            return (Integer)value;
         } else if (interval == -1) {
            return (Integer)value;
         } else if (interval <= 0) {
            section.startupWarn("Invalid refresh interval configured for " + identifier + " (" + interval + "). Value cannot be zero or negative (except -1).");
            return defaultInterval;
         } else if (interval % 50 != 0) {
            section.startupWarn("Invalid refresh interval configured for " + identifier + " (" + interval + "). Value must be divisible by " + 50 + ".");
            return defaultInterval;
         } else {
            return (Integer)value;
         }
      }
   }

   @Generated
   public int getDefaultInterval() {
      return this.defaultInterval;
   }

   @NotNull
   @Generated
   public Map<String, Integer> getRefreshIntervals() {
      return this.refreshIntervals;
   }

   @Generated
   public PlaceholderRefreshConfiguration(int defaultInterval, @NotNull Map<String, Integer> refreshIntervals) {
      if (refreshIntervals == null) {
         throw new NullPointerException("refreshIntervals is marked non-null but is null");
      }

      this.defaultInterval = defaultInterval;
      this.refreshIntervals = refreshIntervals;
   }
}
