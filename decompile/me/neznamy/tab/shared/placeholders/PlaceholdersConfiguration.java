package me.neznamy.tab.shared.placeholders;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class PlaceholdersConfiguration {
   @NotNull
   private final SimpleDateFormat dateFormat;
   @NotNull
   private final SimpleDateFormat timeFormat;
   private final double timeOffset;
   private final boolean registerTabExpansion;

   @NotNull
   public static PlaceholdersConfiguration fromSection(@NotNull ConfigurationSection section) {
      section.checkForUnknownKey(Arrays.asList("date-format", "time-format", "time-offset", "register-tab-expansion"));
      return new PlaceholdersConfiguration(
         parseDateFormat(section.getString("date-format", "dd.MM.yyyy"), "dd.MM.yyyy"),
         parseDateFormat(section.getString("time-format", "[HH:mm:ss / h:mm a]"), "[HH:mm:ss / h:mm a]"),
         section.getNumber("time-offset", 0).doubleValue(),
         section.getBoolean("register-tab-expansion", false)
      );
   }

   private static SimpleDateFormat parseDateFormat(@NonNull String value, @NonNull String defaultValue) {
      if (value == null) {
         throw new NullPointerException("value is marked non-null but is null");
      }

      if (defaultValue == null) {
         throw new NullPointerException("defaultValue is marked non-null but is null");
      }

      try {
         return new SimpleDateFormat(value, Locale.ENGLISH);
      } catch (IllegalArgumentException e) {
         TAB.getInstance().getConfigHelper().startup().startupWarn("Format \"" + value + "\" is not a valid date/time format. Did you try to use color codes?");
         return new SimpleDateFormat(defaultValue);
      }
   }

   @NotNull
   @Generated
   public SimpleDateFormat getDateFormat() {
      return this.dateFormat;
   }

   @NotNull
   @Generated
   public SimpleDateFormat getTimeFormat() {
      return this.timeFormat;
   }

   @Generated
   public double getTimeOffset() {
      return this.timeOffset;
   }

   @Generated
   public boolean isRegisterTabExpansion() {
      return this.registerTabExpansion;
   }

   @Generated
   public PlaceholdersConfiguration(@NotNull SimpleDateFormat dateFormat, @NotNull SimpleDateFormat timeFormat, double timeOffset, boolean registerTabExpansion) {
      if (dateFormat == null) {
         throw new NullPointerException("dateFormat is marked non-null but is null");
      }

      if (timeFormat == null) {
         throw new NullPointerException("timeFormat is marked non-null but is null");
      }

      this.dateFormat = dateFormat;
      this.timeFormat = timeFormat;
      this.timeOffset = timeOffset;
      this.registerTabExpansion = registerTabExpansion;
   }
}
