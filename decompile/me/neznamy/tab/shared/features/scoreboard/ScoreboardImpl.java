package me.neznamy.tab.shared.features.scoreboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.api.scoreboard.Line;
import me.neznamy.tab.api.scoreboard.Scoreboard;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.scoreboard.lines.LongLine;
import me.neznamy.tab.shared.features.scoreboard.lines.ScoreboardLine;
import me.neznamy.tab.shared.features.scoreboard.lines.StableDynamicLine;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScoreboardImpl extends RefreshableFeature implements Scoreboard, CustomThreaded {
   private final ScoreboardManagerImpl manager;
   private final String name;
   private String title;
   private Condition displayCondition;
   private final boolean api;
   private final List<Line> lines = new ArrayList<>();
   private boolean containsNumberFormat;
   private final Set<TabPlayer> players = Collections.newSetFromMap(new ConcurrentHashMap<>());

   public ScoreboardImpl(@NonNull ScoreboardManagerImpl manager, @NonNull String name, @NonNull ScoreboardConfiguration.ScoreboardDefinition definition) {
      this(manager, name, definition, false, false);
      if (manager == null) {
         throw new NullPointerException("manager is marked non-null but is null");
      }

      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      if (definition == null) {
         throw new NullPointerException("definition is marked non-null but is null");
      }

      this.displayCondition = TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(definition.getDisplayCondition());
      if (this.displayCondition != null) {
         manager.addUsedPlaceholder(TabConstants.Placeholder.condition(this.displayCondition.getName()));
      }
   }

   public ScoreboardImpl(
      @NonNull ScoreboardManagerImpl manager,
      @NonNull String name,
      @NonNull ScoreboardConfiguration.ScoreboardDefinition definition,
      boolean dynamicLinesOnly,
      boolean api
   ) {
      if (manager == null) {
         throw new NullPointerException("manager is marked non-null but is null");
      }

      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      if (definition == null) {
         throw new NullPointerException("definition is marked non-null but is null");
      }

      this.manager = manager;
      this.name = name;
      this.api = api;
      this.title = definition.getTitle();

      for (int i = 0; i < definition.getLines().size(); i++) {
         String line = definition.getLines().get(i);
         if (line == null) {
            line = "";
         }

         if (line.contains("||")) {
            this.containsNumberFormat = true;
         }

         ScoreboardLine score;
         if (dynamicLinesOnly) {
            score = new StableDynamicLine(this, i + 1, line);
         } else {
            score = this.registerLine(i + 1, line);
         }

         this.lines.add(score);
         TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.scoreboardLine(name, i), score);
      }
   }

   @NotNull
   private ScoreboardLine registerLine(int lineNumber, @Nullable String text) {
      if (text == null) {
         return new LongLine(this, lineNumber, "");
      } else if (text.startsWith("Long|")) {
         return new LongLine(this, lineNumber, text.substring(5));
      } else {
         return text.contains("%") ? new StableDynamicLine(this, lineNumber, text) : new LongLine(this, lineNumber, text);
      }
   }

   public boolean isConditionMet(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      } else {
         return this.displayCondition == null || this.displayCondition.isMet(p);
      }
   }

   public void addPlayer(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      if (p.scoreboardData.activeScoreboard != this) {
         p.scoreboardData.titleProperty = new Property(this, p, this.title);
         p.getScoreboard()
            .registerObjective(
               "TAB-Scoreboard",
               this.manager.getCache().get(p.scoreboardData.titleProperty.get()),
               me.neznamy.tab.shared.platform.Scoreboard.HealthDisplay.INTEGER,
               TabComponent.empty()
            );
         if (p.scoreboardData.otherPluginScoreboard == null) {
            p.getScoreboard().setDisplaySlot("TAB-Scoreboard", me.neznamy.tab.shared.platform.Scoreboard.DisplaySlot.SIDEBAR);
         }

         for (Line s : this.lines) {
            ((ScoreboardLine)s).register(p);
         }

         this.players.add(p);
         p.scoreboardData.activeScoreboard = this;
         this.recalculateScores(p);
         TAB.getInstance().getPlaceholderManager().getTabExpansion().setScoreboardName(p, this.name);
         if (this.containsNumberFormat && p.getVersionId() < ProtocolVersion.V1_20_3.getNetworkId()) {
            TAB.getInstance()
               .getConfigHelper()
               .runtime()
               .error(
                  "Scoreboard \""
                     + this.name
                     + "\" contains right-side text alignment (using ||), however, this feature was added in 1.20.3, but player \""
                     + p.getName()
                     + "\" is using version "
                     + p.getVersion().getFriendlyName()
                     + ". Right-side text will not be visible for them."
               );
         }
      }
   }

   public void removePlayer(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      if (p.scoreboardData.activeScoreboard == this) {
         p.getScoreboard().unregisterObjective("TAB-Scoreboard");

         for (Line l : this.lines) {
            ScoreboardLine line = (ScoreboardLine)l;
            if (line.isShownTo(p)) {
               p.getScoreboard().unregisterTeam(line.getTeamName());
               line.removePlayerSilently(p);
            }
         }

         this.players.remove(p);
         p.scoreboardData.activeScoreboard = null;
         p.scoreboardData.titleProperty = null;
         p.scoreboardData.lineProperties.clear();
         p.scoreboardData.lineNameProperties.clear();
         p.scoreboardData.numberFormatProperties.clear();
         TAB.getInstance().getPlaceholderManager().getTabExpansion().setScoreboardName(p, "");
      }
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Updating Scoreboard title";
   }

   @Override
   public void refresh(@NotNull TabPlayer refreshed, boolean force) {
      if (refreshed.scoreboardData.activeScoreboard == this) {
         refreshed.getScoreboard()
            .updateObjective(
               "TAB-Scoreboard",
               this.manager.getCache().get(refreshed.scoreboardData.titleProperty.updateAndGet()),
               me.neznamy.tab.shared.platform.Scoreboard.HealthDisplay.INTEGER,
               TabComponent.empty()
            );
      }
   }

   public void recalculateScores(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      if (this.manager.getConfiguration().isUseNumbers()) {
         List<Line> linesReversed = new ArrayList<>(this.lines);
         Collections.reverse(linesReversed);
         int score = this.manager.getConfiguration().getStaticNumber();

         for (Line line : linesReversed) {
            Property pr = p.scoreboardData.lineProperties.get((ScoreboardLine)line);
            if (pr.getCurrentRawValue().isEmpty() || !pr.getCurrentRawValue().isEmpty() && !pr.get().isEmpty()) {
               p.getScoreboard()
                  .setScore(
                     "TAB-Scoreboard", ((ScoreboardLine)line).getPlayerName(p), score++, null, ((ScoreboardLine)line).getScoreRefresher().getNumberFormat(p)
                  );
            }
         }
      }
   }

   public void removePlayerFromSet(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.players.remove(player);

      for (Line line : this.lines) {
         ((ScoreboardLine)line).removePlayerSilently(player);
      }
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return this.manager.getFeatureName();
   }

   @Override
   public void setTitle(@NonNull String title) {
      if (title == null) {
         throw new NullPointerException("title is marked non-null but is null");
      }

      this.ensureActive();
      this.title = title;

      for (TabPlayer p : this.players) {
         p.scoreboardData.titleProperty.changeRawValue(title);
         p.getScoreboard()
            .updateObjective(
               "TAB-Scoreboard",
               this.manager.getCache().get(p.scoreboardData.titleProperty.get()),
               me.neznamy.tab.shared.platform.Scoreboard.HealthDisplay.INTEGER,
               TabComponent.empty()
            );
      }
   }

   @Override
   public void addLine(@NonNull String text) {
      if (text == null) {
         throw new NullPointerException("text is marked non-null but is null");
      }

      this.ensureActive();
      StableDynamicLine line = new StableDynamicLine(this, this.lines.size() + 1, text);
      TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.scoreboardLine(this.name, this.lines.size()), line);
      this.lines.add(line);

      for (TabPlayer p : this.players) {
         line.register(p);
         this.recalculateScores(p);
      }
   }

   @Override
   public void removeLine(int index) {
      this.ensureActive();
      if (index >= 0 && index < this.lines.size()) {
         ScoreboardLine line = (ScoreboardLine)this.lines.get(index);
         this.lines.remove(line);

         for (TabPlayer p : this.players) {
            line.unregister(p);
            this.recalculateScores(p);
         }

         TAB.getInstance().getFeatureManager().unregisterFeature(TabConstants.Feature.scoreboardLine(this.name, index));
      } else {
         throw new IndexOutOfBoundsException("Index " + index + " is out of range (0 - " + (this.lines.size() - 1) + ")");
      }
   }

   @Override
   public void unregister() {
      this.ensureActive();

      for (TabPlayer all : this.players.toArray(new TabPlayer[0])) {
         this.removePlayer(all);
      }

      this.players.clear();
   }

   @NotNull
   @Override
   public ThreadExecutor getCustomThread() {
      return this.manager.getCustomThread();
   }

   @Generated
   public ScoreboardManagerImpl getManager() {
      return this.manager;
   }

   @Generated
   @Override
   public String getName() {
      return this.name;
   }

   @Generated
   @Override
   public String getTitle() {
      return this.title;
   }

   @Generated
   public Condition getDisplayCondition() {
      return this.displayCondition;
   }

   @Generated
   public boolean isApi() {
      return this.api;
   }

   @Generated
   @Override
   public List<Line> getLines() {
      return this.lines;
   }

   @Generated
   public boolean isContainsNumberFormat() {
      return this.containsNumberFormat;
   }

   @Generated
   public Set<TabPlayer> getPlayers() {
      return this.players;
   }
}
