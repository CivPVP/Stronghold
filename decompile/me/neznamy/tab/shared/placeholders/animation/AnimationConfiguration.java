package me.neznamy.tab.shared.placeholders.animation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnimationConfiguration {
   @NotNull
   private final Map<String, AnimationConfiguration.AnimationDefinition> animations;

   @NotNull
   public static AnimationConfiguration fromSection(@NotNull ConfigurationSection section) {
      Map<String, AnimationConfiguration.AnimationDefinition> animations = new HashMap<>();

      for (Object animationName : section.getKeys()) {
         animations.put(
            animationName.toString(),
            AnimationConfiguration.AnimationDefinition.fromSection(section.getConfigurationSection(animationName.toString()), animationName.toString())
         );
      }

      return new AnimationConfiguration(animations);
   }

   @NotNull
   @Generated
   public Map<String, AnimationConfiguration.AnimationDefinition> getAnimations() {
      return this.animations;
   }

   @Generated
   public AnimationConfiguration(@NotNull Map<String, AnimationConfiguration.AnimationDefinition> animations) {
      if (animations == null) {
         throw new NullPointerException("animations is marked non-null but is null");
      }

      this.animations = animations;
   }

   public static class AnimationDefinition {
      private final int changeInterval;
      private final List<String> texts;

      @NotNull
      public static AnimationConfiguration.AnimationDefinition fromSection(@NotNull ConfigurationSection section, @NotNull String name) {
         section.checkForUnknownKey(Arrays.asList("change-interval", "texts"));
         return new AnimationConfiguration.AnimationDefinition(
            fixAnimationInterval(name, section.getInt("change-interval"), section),
            section.getStringList("texts", Collections.singletonList("<Animation does not have any texts>"))
         );
      }

      private static int fixAnimationInterval(@NotNull String name, @Nullable Integer interval, @NotNull ConfigurationSection section) {
         if (interval == null) {
            section.startupWarn(String.format("Animation \"%s\" does not define change-interval! Did you forget to configure it? Using 1000.", name));
            return 1000;
         }

         if (interval == 0) {
            section.startupWarn(String.format("Animation \"%s\" has refresh interval of 0 milliseconds! Using 1000.", name));
            return 1000;
         }

         if (interval < 0) {
            section.startupWarn(String.format("Animation \"%s\" has refresh interval of %s. Refresh cannot be negative! Using 1000.", name, interval));
            return 1000;
         }

         if (interval % 50 != 0) {
            int min = 50;
            int newInterval = Math.round((float)interval.intValue() / min) * min;
            if (newInterval == 0) {
               newInterval = min;
            }

            section.startupWarn(
               String.format("Animation \"%s\" has refresh interval of %s, which is not divisible by %s! Using %s.", name, interval, min, newInterval)
            );
            return newInterval;
         } else {
            return interval;
         }
      }

      @Generated
      public AnimationDefinition(int changeInterval, List<String> texts) {
         this.changeInterval = changeInterval;
         this.texts = texts;
      }

      @Generated
      public int getChangeInterval() {
         return this.changeInterval;
      }

      @Generated
      public List<String> getTexts() {
         return this.texts;
      }
   }
}
