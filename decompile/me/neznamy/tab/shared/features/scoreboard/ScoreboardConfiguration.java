package me.neznamy.tab.shared.features.scoreboard;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScoreboardConfiguration {
   @NotNull
   private final String toggleCommand;
   private final boolean rememberToggleChoice;
   private final boolean hiddenByDefault;
   private final boolean useNumbers;
   private final int staticNumber;
   private final int joinDelay;
   @NotNull
   private final Map<String, ScoreboardConfiguration.ScoreboardDefinition> scoreboards;

   public static ScoreboardConfiguration fromSection(@NotNull ConfigurationSection section) {
      section.checkForUnknownKey(
         Arrays.asList(
            "enabled",
            "toggle-command",
            "remember-toggle-choice",
            "hidden-by-default",
            "use-numbers",
            "static-number",
            "delay-on-join-milliseconds",
            "scoreboards"
         )
      );
      ConfigurationSection scoreboardsSection = section.getConfigurationSection("scoreboards");
      Map<String, ScoreboardConfiguration.ScoreboardDefinition> scoreboards = new LinkedHashMap<>();

      for (Object scoreboard : scoreboardsSection.getKeys()) {
         String asString = scoreboard.toString();
         scoreboards.put(asString, ScoreboardConfiguration.ScoreboardDefinition.fromSection(asString, scoreboardsSection.getConfigurationSection(asString)));
      }

      checkChain(section, scoreboards);
      return new ScoreboardConfiguration(
         section.getString("toggle-command", "/sb"),
         section.getBoolean("remember-toggle-choice", false),
         section.getBoolean("hidden-by-default", false),
         section.getBoolean("use-numbers", true),
         section.getInt("static-number", 0),
         section.getInt("delay-on-join-milliseconds", 0),
         scoreboards
      );
   }

   private static void checkChain(@NotNull ConfigurationSection section, Map<String, ScoreboardConfiguration.ScoreboardDefinition> scoreboards) {
      String noConditionScoreboard = null;

      for (Entry<String, ScoreboardConfiguration.ScoreboardDefinition> entry : scoreboards.entrySet()) {
         if (noConditionScoreboard != null) {
            section.startupWarn(
               "Scoreboard \""
                  + noConditionScoreboard
                  + "\" has no display condition set, however, there is another scoreboard in the chain ("
                  + entry.getKey()
                  + "). Scoreboards are checked from top to bottom until a scoreboard with meeting condition or no condition is found. Because of this, the scoreboard ("
                  + entry.getKey()
                  + ") after the no-condition scoreboard ("
                  + noConditionScoreboard
                  + ") will never be displayed. Unless this is intentional to externally display the scoreboard (commands, API), this is a mistake."
            );
         } else if (entry.getValue().displayCondition == null) {
            noConditionScoreboard = entry.getKey();
         }
      }
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

   @Generated
   public boolean isUseNumbers() {
      return this.useNumbers;
   }

   @Generated
   public int getStaticNumber() {
      return this.staticNumber;
   }

   @Generated
   public int getJoinDelay() {
      return this.joinDelay;
   }

   @NotNull
   @Generated
   public Map<String, ScoreboardConfiguration.ScoreboardDefinition> getScoreboards() {
      return this.scoreboards;
   }

   @Generated
   public ScoreboardConfiguration(
      @NotNull String toggleCommand,
      boolean rememberToggleChoice,
      boolean hiddenByDefault,
      boolean useNumbers,
      int staticNumber,
      int joinDelay,
      @NotNull Map<String, ScoreboardConfiguration.ScoreboardDefinition> scoreboards
   ) {
      if (toggleCommand == null) {
         throw new NullPointerException("toggleCommand is marked non-null but is null");
      }

      if (scoreboards == null) {
         throw new NullPointerException("scoreboards is marked non-null but is null");
      }

      this.toggleCommand = toggleCommand;
      this.rememberToggleChoice = rememberToggleChoice;
      this.hiddenByDefault = hiddenByDefault;
      this.useNumbers = useNumbers;
      this.staticNumber = staticNumber;
      this.joinDelay = joinDelay;
      this.scoreboards = scoreboards;
   }

   public static class ScoreboardDefinition {
      @Nullable
      private final String displayCondition;
      @NotNull
      private final String title;
      @NotNull
      private final List<String> lines;

      public static ScoreboardConfiguration.ScoreboardDefinition fromSection(@NotNull String name, @NotNull ConfigurationSection section) {
         section.checkForUnknownKey(Arrays.asList("display-condition", "title", "lines"));
         List<String> lines = section.getStringList(
            "lines",
            Arrays.asList("scoreboard \"" + name + "\" is missing \"lines\" property!", "did you forget to configure it or just your spacing is wrong?")
         );
         int alwaysVisibleLines = 0;

         for (String line : lines) {
            if (line != null) {
               String withoutPlaceholders = line;

               for (String placeholder : PlaceholderManagerImpl.detectPlaceholders(line)) {
                  withoutPlaceholders = withoutPlaceholders.replace(placeholder, "");
               }

               if (!withoutPlaceholders.isEmpty()) {
                  alwaysVisibleLines++;
               }
            }
         }

         if (alwaysVisibleLines > 15) {
            section.startupWarn(
               String.format(
                  "Scoreboard \"%s\" has %d defined lines, at least %d of which are permanently visible. However, the client only displays up to 15 lines, with any lines below them not being displayed.",
                  name,
                  lines.size(),
                  alwaysVisibleLines
               )
            );
         }

         return new ScoreboardConfiguration.ScoreboardDefinition(
            section.getString("display-condition"), section.getString("title", "<Title is not defined>"), lines
         );
      }

      @Nullable
      @Generated
      public String getDisplayCondition() {
         return this.displayCondition;
      }

      @NotNull
      @Generated
      public String getTitle() {
         return this.title;
      }

      @NotNull
      @Generated
      public List<String> getLines() {
         return this.lines;
      }

      @Generated
      public ScoreboardDefinition(@Nullable String displayCondition, @NotNull String title, @NotNull List<String> lines) {
         if (title == null) {
            throw new NullPointerException("title is marked non-null but is null");
         }

         if (lines == null) {
            throw new NullPointerException("lines is marked non-null but is null");
         }

         this.displayCondition = displayCondition;
         this.title = title;
         this.lines = lines;
      }
   }
}
