package me.neznamy.tab.shared.features.bossbar;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import lombok.Generated;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BossBarConfiguration {
   @NotNull
   private final String toggleCommand;
   private final boolean rememberToggleChoice;
   private final boolean hiddenByDefault;
   @NotNull
   private final Map<String, BossBarConfiguration.BossBarDefinition> bars;

   @NotNull
   public static BossBarConfiguration fromSection(@NotNull ConfigurationSection section) {
      section.checkForUnknownKey(Arrays.asList("enabled", "toggle-command", "remember-toggle-choice", "hidden-by-default", "bars"));
      ConfigurationSection barsSection = section.getConfigurationSection("bars");
      Map<String, BossBarConfiguration.BossBarDefinition> bars = new LinkedHashMap<>();

      for (Object bar : barsSection.getKeys()) {
         String asString = bar.toString();
         bars.put(asString, BossBarConfiguration.BossBarDefinition.fromSection(asString, barsSection.getConfigurationSection(asString)));
      }

      return new BossBarConfiguration(
         section.getString("toggle-command", "/bossbar"),
         section.getBoolean("remember-toggle-choice", false),
         section.getBoolean("hidden-by-default", false),
         bars
      );
   }

   @NotNull
   @Generated
   public String getToggleCommand() {
      return this.toggleCommand;
   }

   @Generated
   public boolean isRememberToggleChoice() {
      return this.rememberToggleChoice;
   }

   @Generated
   public boolean isHiddenByDefault() {
      return this.hiddenByDefault;
   }

   @NotNull
   @Generated
   public Map<String, BossBarConfiguration.BossBarDefinition> getBars() {
      return this.bars;
   }

   @Generated
   public BossBarConfiguration(
      @NotNull String toggleCommand, boolean rememberToggleChoice, boolean hiddenByDefault, @NotNull Map<String, BossBarConfiguration.BossBarDefinition> bars
   ) {
      if (toggleCommand == null) {
         throw new NullPointerException("toggleCommand is marked non-null but is null");
      }

      if (bars == null) {
         throw new NullPointerException("bars is marked non-null but is null");
      }

      this.toggleCommand = toggleCommand;
      this.rememberToggleChoice = rememberToggleChoice;
      this.hiddenByDefault = hiddenByDefault;
      this.bars = bars;
   }

   public static class BossBarDefinition {
      @NotNull
      private final String style;
      @NotNull
      private final String color;
      @NotNull
      private final String progress;
      @NotNull
      private final String text;
      private final boolean announcementOnly;
      @Nullable
      private final String displayCondition;

      @NotNull
      public static BossBarConfiguration.BossBarDefinition fromSection(@NotNull String name, @NotNull ConfigurationSection section) {
         section.checkForUnknownKey(Arrays.asList("style", "color", "progress", "text", "announcement-bar", "display-condition"));
         String style = section.getString("style", "PROGRESS");
         if (!style.contains("%")) {
            try {
               BarStyle.valueOf(style.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
               section.startupWarn(
                  "Bossbar \""
                     + name
                     + " has style set to \""
                     + style
                     + "\", which is not one of the supported styles "
                     + Arrays.toString(BarStyle.values())
                     + " or a placeholder evaluating to one."
               );
            }
         }

         String color = section.getString("color", "PURPLE");
         if (!color.contains("%")) {
            try {
               BarColor.valueOf(color.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
               section.startupWarn(
                  "Bossbar \""
                     + name
                     + "\" has color set to \""
                     + color
                     + "\", which is not one of the supported colors "
                     + Arrays.toString(BarColor.values())
                     + " or a placeholder evaluating to one."
               );
            }
         }

         String progress = section.getObject("progress", "100").toString();
         if (!progress.contains("%")) {
            try {
               Float.parseFloat(progress);
            } catch (IllegalArgumentException e) {
               section.startupWarn(
                  "Bossbar \""
                     + name
                     + " has progress set to \""
                     + progress
                     + "\", which is not a valid number between 0 and 100 or a placeholder evaluating to one."
               );
            }
         }

         return new BossBarConfiguration.BossBarDefinition(
            style,
            color,
            progress,
            section.getString("text", "\"text\" is not defined!"),
            section.getBoolean("announcement-bar") == Boolean.TRUE,
            section.getString("display-condition")
         );
      }

      @NotNull
      @Generated
      public String getStyle() {
         return this.style;
      }

      @NotNull
      @Generated
      public String getColor() {
         return this.color;
      }

      @NotNull
      @Generated
      public String getProgress() {
         return this.progress;
      }

      @NotNull
      @Generated
      public String getText() {
         return this.text;
      }

      @Generated
      public boolean isAnnouncementOnly() {
         return this.announcementOnly;
      }

      @Nullable
      @Generated
      public String getDisplayCondition() {
         return this.displayCondition;
      }

      @Generated
      public BossBarDefinition(
         @NotNull String style,
         @NotNull String color,
         @NotNull String progress,
         @NotNull String text,
         boolean announcementOnly,
         @Nullable String displayCondition
      ) {
         if (style == null) {
            throw new NullPointerException("style is marked non-null but is null");
         }

         if (color == null) {
            throw new NullPointerException("color is marked non-null but is null");
         }

         if (progress == null) {
            throw new NullPointerException("progress is marked non-null but is null");
         }

         if (text == null) {
            throw new NullPointerException("text is marked non-null but is null");
         }

         this.style = style;
         this.color = color;
         this.progress = progress;
         this.text = text;
         this.announcementOnly = announcementOnly;
         this.displayCondition = displayCondition;
      }
   }
}
