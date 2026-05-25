package me.neznamy.tab.shared.platform.decorators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SafeScoreboard<T extends TabPlayer> implements Scoreboard {
   private static String lastTeamOverrideMessage;
   protected final T player;
   private final Map<String, String> blockedTeamAdds = new HashMap<>();
   private final Map<String, String> allowedTeamAdds = new HashMap<>();
   private boolean frozen;
   private final Map<String, SafeScoreboard.Objective> objectives = new ConcurrentHashMap<>();
   private final Map<String, SafeScoreboard.Team> teams = new ConcurrentHashMap<>();

   @Override
   public synchronized void registerObjective(
      @NonNull String objectiveName, @NonNull TabComponent title, @NonNull Scoreboard.HealthDisplay display, @Nullable TabComponent numberFormat
   ) {
      if (objectiveName == null) {
         throw new NullPointerException("objectiveName is marked non-null but is null");
      }

      if (title == null) {
         throw new NullPointerException("title is marked non-null but is null");
      }

      if (display == null) {
         throw new NullPointerException("display is marked non-null but is null");
      }

      SafeScoreboard.Objective existing = this.objectives.get(objectiveName);
      if (existing != null) {
         this.error("Tried to register duplicated objective %s to player ", objectiveName);
      } else {
         SafeScoreboard.Objective objective = new SafeScoreboard.Objective(objectiveName, title, display, numberFormat, null);
         this.objectives.put(objectiveName, objective);
         if (!this.frozen) {
            this.registerObjective(objective);
         }
      }
   }

   @Override
   public synchronized void setDisplaySlot(@NonNull String objectiveName, @NonNull Scoreboard.DisplaySlot displaySlot) {
      if (objectiveName == null) {
         throw new NullPointerException("objectiveName is marked non-null but is null");
      }

      if (displaySlot == null) {
         throw new NullPointerException("displaySlot is marked non-null but is null");
      }

      SafeScoreboard.Objective objective = this.objectives.get(objectiveName);
      if (objective == null) {
         this.error("Tried to set display slot for non-existing objective %s to player ", objectiveName);
      } else {
         objective.setDisplaySlot(displaySlot);
         if (!this.frozen) {
            this.setDisplaySlot(objective);
         }
      }
   }

   @Override
   public synchronized void unregisterObjective(@NonNull String objectiveName) {
      if (objectiveName == null) {
         throw new NullPointerException("objectiveName is marked non-null but is null");
      }

      SafeScoreboard.Objective objective = this.objectives.remove(objectiveName);
      if (objective == null) {
         this.error("Tried to unregister non-existing objective %s for player ", objectiveName);
      } else if (!this.frozen) {
         this.unregisterObjective(objective);
      }
   }

   @Override
   public synchronized void updateObjective(
      @NonNull String objectiveName, @NonNull TabComponent title, @NonNull Scoreboard.HealthDisplay display, @Nullable TabComponent numberFormat
   ) {
      if (objectiveName == null) {
         throw new NullPointerException("objectiveName is marked non-null but is null");
      }

      if (title == null) {
         throw new NullPointerException("title is marked non-null but is null");
      }

      if (display == null) {
         throw new NullPointerException("display is marked non-null but is null");
      }

      SafeScoreboard.Objective objective = this.objectives.get(objectiveName);
      if (objective == null) {
         this.error("Tried to modify non-existing objective %s for player ", objectiveName);
      } else {
         objective.update(title, display, numberFormat);
         if (!this.frozen) {
            this.updateObjective(objective);
         }
      }
   }

   @Override
   public synchronized void setScore(
      @NonNull String objectiveName, @NonNull String scoreHolder, int value, @Nullable TabComponent displayName, @Nullable TabComponent numberFormat
   ) {
      if (objectiveName == null) {
         throw new NullPointerException("objectiveName is marked non-null but is null");
      }

      if (scoreHolder == null) {
         throw new NullPointerException("scoreHolder is marked non-null but is null");
      }

      SafeScoreboard.Objective objective = this.objectives.get(objectiveName);
      if (objective == null) {
         this.error("Tried to update score (%s) without the existence of its requested objective '%s' to player ", scoreHolder, objectiveName);
      } else {
         SafeScoreboard.Score score = objective.getScores().get(scoreHolder);
         if (score == null) {
            score = new SafeScoreboard.Score(objective, scoreHolder, value, displayName, numberFormat);
            objective.getScores().put(scoreHolder, score);
         } else {
            score.update(value, displayName, numberFormat);
         }

         if (!this.frozen) {
            this.setScore(score);
         }
      }
   }

   @Override
   public synchronized void removeScore(@NonNull String objectiveName, @NonNull String scoreHolder) {
      if (objectiveName == null) {
         throw new NullPointerException("objectiveName is marked non-null but is null");
      }

      if (scoreHolder == null) {
         throw new NullPointerException("scoreHolder is marked non-null but is null");
      }

      SafeScoreboard.Objective objective = this.objectives.get(objectiveName);
      if (objective == null) {
         this.error("Tried to remove score (%s) without the existence of its requested objective '%s' to player ", scoreHolder, objectiveName);
      } else {
         SafeScoreboard.Score score = objective.getScores().remove(scoreHolder);
         if (score != null) {
            if (!this.frozen) {
               this.removeScore(score);
            }
         }
      }
   }

   @Override
   public synchronized void registerTeam(
      @NonNull String name,
      @NonNull TabComponent prefix,
      @NonNull TabComponent suffix,
      @NonNull Scoreboard.NameVisibility visibility,
      @NonNull Scoreboard.CollisionRule collision,
      @NonNull Collection<String> players,
      int options,
      @NonNull EnumChatFormat color
   ) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      if (prefix == null) {
         throw new NullPointerException("prefix is marked non-null but is null");
      }

      if (suffix == null) {
         throw new NullPointerException("suffix is marked non-null but is null");
      }

      if (visibility == null) {
         throw new NullPointerException("visibility is marked non-null but is null");
      }

      if (collision == null) {
         throw new NullPointerException("collision is marked non-null but is null");
      }

      if (players == null) {
         throw new NullPointerException("players is marked non-null but is null");
      }

      if (color == null) {
         throw new NullPointerException("color is marked non-null but is null");
      }

      SafeScoreboard.Team existing = this.teams.get(name);
      if (existing != null) {
         this.error(
            "Tried to register duplicated team %s with entry %s, while this team already exists with entry %s to player ",
            name,
            players.toString(),
            existing.players.toString()
         );
      } else {
         SafeScoreboard.Team team = new SafeScoreboard.Team(this.createTeam(name), name, prefix, suffix, visibility, collision, players, options, color);
         this.teams.put(name, team);
         if (!this.frozen) {
            this.registerTeam(team);
         }
      }
   }

   @Override
   public synchronized void unregisterTeam(@NonNull String teamName) {
      if (teamName == null) {
         throw new NullPointerException("teamName is marked non-null but is null");
      }

      SafeScoreboard.Team team = this.teams.remove(teamName);
      if (team == null) {
         this.error("Tried to unregister non-existing team %s for player ", teamName);
      } else if (!this.frozen) {
         this.unregisterTeam(team);
      }
   }

   @Override
   public synchronized void updateTeam(
      @NonNull String name,
      @NonNull TabComponent prefix,
      @NonNull TabComponent suffix,
      @NonNull Scoreboard.NameVisibility visibility,
      @NonNull Scoreboard.CollisionRule collision,
      int options,
      @NonNull EnumChatFormat color
   ) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      if (prefix == null) {
         throw new NullPointerException("prefix is marked non-null but is null");
      }

      if (suffix == null) {
         throw new NullPointerException("suffix is marked non-null but is null");
      }

      if (visibility == null) {
         throw new NullPointerException("visibility is marked non-null but is null");
      }

      if (collision == null) {
         throw new NullPointerException("collision is marked non-null but is null");
      }

      if (color == null) {
         throw new NullPointerException("color is marked non-null but is null");
      }

      SafeScoreboard.Team team = this.teams.get(name);
      if (team == null) {
         this.error("Tried to modify non-existing team %s for player ", name);
      } else {
         team.update(prefix, suffix, visibility, collision, options, color);
         if (!this.frozen) {
            this.updateTeam(team);
         }
      }
   }

   @Override
   public synchronized void updateTeam(@NonNull String name, @NonNull TabComponent prefix, @NonNull TabComponent suffix, @NonNull EnumChatFormat color) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      if (prefix == null) {
         throw new NullPointerException("prefix is marked non-null but is null");
      }

      if (suffix == null) {
         throw new NullPointerException("suffix is marked non-null but is null");
      }

      if (color == null) {
         throw new NullPointerException("color is marked non-null but is null");
      }

      SafeScoreboard.Team team = this.teams.get(name);
      if (team != null) {
         team.update(prefix, suffix, color);
         if (!this.frozen) {
            this.updateTeam(team);
         }
      }
   }

   @Override
   public synchronized void updateTeam(@NonNull String name, @NonNull Scoreboard.CollisionRule collision) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      if (collision == null) {
         throw new NullPointerException("collision is marked non-null but is null");
      }

      SafeScoreboard.Team team = this.teams.get(name);
      if (team != null) {
         team.collision = collision;
         if (!this.frozen) {
            this.updateTeam(team);
         }
      }
   }

   @Override
   public synchronized void updateTeam(@NonNull String name, @NonNull Scoreboard.NameVisibility visibility) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      if (visibility == null) {
         throw new NullPointerException("visibility is marked non-null but is null");
      }

      SafeScoreboard.Team team = this.teams.get(name);
      if (team != null) {
         team.visibility = visibility;
         if (!this.frozen) {
            this.updateTeam(team);
         }
      }
   }

   @Override
   public synchronized void renameTeam(@NonNull String oldName, @NonNull String newName) {
      if (oldName == null) {
         throw new NullPointerException("oldName is marked non-null but is null");
      }

      if (newName == null) {
         throw new NullPointerException("newName is marked non-null but is null");
      }

      SafeScoreboard.Team team = this.teams.get(oldName);
      if (team != null) {
         this.unregisterTeam(oldName);
         this.registerTeam(newName, team.prefix, team.suffix, team.visibility, team.collision, team.players, team.options, team.color);
      }
   }

   @Override
   public synchronized void resend() {
      for (SafeScoreboard.Objective objective : this.objectives.values()) {
         this.registerObjective(objective);
         if (objective.getDisplaySlot() != null) {
            this.setDisplaySlot(objective);
         }

         for (SafeScoreboard.Score score : objective.getScores().values()) {
            this.setScore(score);
         }
      }

      for (SafeScoreboard.Team team : this.teams.values()) {
         this.registerTeam(team);
      }
   }

   @Override
   public synchronized void clear() {
      for (String objective : this.objectives.keySet()) {
         this.unregisterObjective(objective);
      }

      for (String team : this.teams.keySet()) {
         this.unregisterTeam(team);
      }
   }

   public synchronized void unregisterTeamSafe(@NonNull String teamName) {
      if (teamName == null) {
         throw new NullPointerException("teamName is marked non-null but is null");
      }

      SafeScoreboard.Team team = this.teams.remove(teamName);
      if (team != null && !this.frozen) {
         this.unregisterTeam(team);
      }
   }

   private void error(@NonNull String format, @NonNull Object... args) {
      if (format == null) {
         throw new NullPointerException("format is marked non-null but is null");
      }

      if (args == null) {
         throw new NullPointerException("args is marked non-null but is null");
      }

      TAB.getInstance().getErrorManager().printError(String.format(format, args) + this.player.getName(), null);
   }

   public static String cutTo(@Nullable String string, int length) {
      if (string == null) {
         return "";
      } else if (string.length() <= length) {
         return string;
      } else {
         return string.charAt(length - 1) == 167 ? string.substring(0, length - 1) : string.substring(0, length);
      }
   }

   @NotNull
   public Object onPacketSend(@NonNull Object packet) {
      return packet;
   }

   @NotNull
   public Collection<String> onTeamPacket(int action, @NonNull String teamName, @NonNull Collection<String> players) {
      if (teamName == null) {
         throw new NullPointerException("teamName is marked non-null but is null");
      }

      if (players == null) {
         throw new NullPointerException("players is marked non-null but is null");
      }

      Collection<String> newList = new ArrayList<>();
      if (action == 0 || action == 3) {
         for (String entry : players) {
            SafeScoreboard.Team expectedTeam = this.getExpectedTeam(entry);
            if (expectedTeam == null) {
               this.blockedTeamAdds.remove(entry);
               this.allowedTeamAdds.put(entry, teamName);
               newList.add(entry);
            } else if (teamName.equals(expectedTeam.getName())) {
               newList.add(entry);
               this.allowedTeamAdds.remove(entry);
            } else {
               this.blockedTeamAdds.put(entry, teamName);
               logTeamOverride(teamName, entry, expectedTeam);
            }
         }
      }

      if (action == 4) {
         for (String entry : players) {
            SafeScoreboard.Team expectedTeam = this.getExpectedTeam(entry);
            if (expectedTeam != null) {
               this.allowedTeamAdds.remove(entry);
               this.blockedTeamAdds.remove(entry);
            } else if (this.allowedTeamAdds.containsKey(entry)) {
               this.allowedTeamAdds.remove(entry);
               newList.add(entry);
            } else {
               this.blockedTeamAdds.remove(entry);
            }
         }
      }

      if (action == 1) {
         this.allowedTeamAdds.entrySet().removeIf(entryx -> ((String)entryx.getValue()).equals(teamName));
         this.blockedTeamAdds.entrySet().removeIf(entryx -> ((String)entryx.getValue()).equals(teamName));
      }

      return newList;
   }

   @Nullable
   private SafeScoreboard.Team getExpectedTeam(@NotNull String player) {
      for (SafeScoreboard.Team team : this.teams.values()) {
         if (team.getPlayers().contains(player)) {
            return team;
         }
      }

      return null;
   }

   public static void logTeamOverride(@NonNull String team, @NonNull String player, @NonNull SafeScoreboard.Team expectedTeam) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (expectedTeam == null) {
         throw new NullPointerException("expectedTeam is marked non-null but is null");
      }

      String source = null;
      String fix = null;
      if (team.startsWith("collideRule_")) {
         source = "Paper";
         fix = "set \"enable-player-collisions: true\" in paper config. To keep collisions disabled, set collision to false in TAB config.";
      } else if (team.startsWith("CMINP")) {
         source = "CMI";
         fix = "set \"DisableTeamManagement: true\" in \"plugins/CMI/config.yml\".";
      } else if (team.startsWith("CIT-")) {
         source = "Citizens";
         fix = "use NPC names that do not match names of online players.";
      }

      String message = "Blocked attempt to add player " + player + " into team " + team + " (expected team: " + expectedTeam.getName() + ").";
      if (source != null) {
         message = message + " Source of the team: " + source + ". To fix this, " + fix;
      }

      if (!message.equals(lastTeamOverrideMessage)) {
         lastTeamOverrideMessage = message;
         TAB.getInstance().getErrorManager().logAntiOverride(message);
      }
   }

   public abstract void registerObjective(@NonNull SafeScoreboard.Objective var1);

   public abstract void setDisplaySlot(@NonNull SafeScoreboard.Objective var1);

   public abstract void unregisterObjective(@NonNull SafeScoreboard.Objective var1);

   public abstract void updateObjective(@NonNull SafeScoreboard.Objective var1);

   public abstract void setScore(@NonNull SafeScoreboard.Score var1);

   public abstract void removeScore(@NonNull SafeScoreboard.Score var1);

   @NotNull
   public abstract Object createTeam(@NonNull String var1);

   public abstract void registerTeam(@NonNull SafeScoreboard.Team var1);

   public abstract void unregisterTeam(@NonNull SafeScoreboard.Team var1);

   public abstract void updateTeam(@NonNull SafeScoreboard.Team var1);

   @Generated
   public SafeScoreboard(T player) {
      this.player = player;
   }

   @Generated
   public void setFrozen(boolean frozen) {
      this.frozen = frozen;
   }

   @Generated
   public T getPlayer() {
      return this.player;
   }

   public static class Objective {
      @Nullable
      private Scoreboard.DisplaySlot displaySlot;
      @NonNull
      private final String name;
      @NonNull
      private TabComponent title;
      @NonNull
      private Scoreboard.HealthDisplay healthDisplay;
      @Nullable
      private TabComponent numberFormat;
      @NonNull
      private final Map<String, SafeScoreboard.Score> scores = new HashMap<>();
      @Nullable
      private Object platformObjective;

      private Objective(
         @NonNull String name,
         @NonNull TabComponent title,
         @NonNull Scoreboard.HealthDisplay healthDisplay,
         @Nullable TabComponent numberFormat,
         @Nullable Object platformObjective
      ) {
         if (name == null) {
            throw new NullPointerException("name is marked non-null but is null");
         }

         if (title == null) {
            throw new NullPointerException("title is marked non-null but is null");
         }

         if (healthDisplay == null) {
            throw new NullPointerException("healthDisplay is marked non-null but is null");
         }

         this.name = name;
         this.title = title;
         this.healthDisplay = healthDisplay;
         this.numberFormat = numberFormat;
         this.platformObjective = platformObjective;
      }

      private void update(@NonNull TabComponent title, @NonNull Scoreboard.HealthDisplay healthDisplay, @Nullable TabComponent numberFormat) {
         if (title == null) {
            throw new NullPointerException("title is marked non-null but is null");
         }

         if (healthDisplay == null) {
            throw new NullPointerException("healthDisplay is marked non-null but is null");
         }

         this.title = title;
         this.healthDisplay = healthDisplay;
         this.numberFormat = numberFormat;
      }

      @Nullable
      @Generated
      public Scoreboard.DisplaySlot getDisplaySlot() {
         return this.displaySlot;
      }

      @NonNull
      @Generated
      public String getName() {
         return this.name;
      }

      @NonNull
      @Generated
      public TabComponent getTitle() {
         return this.title;
      }

      @NonNull
      @Generated
      public Scoreboard.HealthDisplay getHealthDisplay() {
         return this.healthDisplay;
      }

      @Nullable
      @Generated
      public TabComponent getNumberFormat() {
         return this.numberFormat;
      }

      @NonNull
      @Generated
      public Map<String, SafeScoreboard.Score> getScores() {
         return this.scores;
      }

      @Nullable
      @Generated
      public Object getPlatformObjective() {
         return this.platformObjective;
      }

      @Generated
      public void setDisplaySlot(@Nullable Scoreboard.DisplaySlot displaySlot) {
         this.displaySlot = displaySlot;
      }

      @Generated
      public void setTitle(@NonNull TabComponent title) {
         if (title == null) {
            throw new NullPointerException("title is marked non-null but is null");
         }

         this.title = title;
      }

      @Generated
      public void setHealthDisplay(@NonNull Scoreboard.HealthDisplay healthDisplay) {
         if (healthDisplay == null) {
            throw new NullPointerException("healthDisplay is marked non-null but is null");
         }

         this.healthDisplay = healthDisplay;
      }

      @Generated
      public void setNumberFormat(@Nullable TabComponent numberFormat) {
         this.numberFormat = numberFormat;
      }

      @Generated
      public void setPlatformObjective(@Nullable Object platformObjective) {
         this.platformObjective = platformObjective;
      }
   }

   public static class Score {
      @NonNull
      private final SafeScoreboard.Objective objective;
      @NonNull
      private final String holder;
      private int value;
      @Nullable
      private TabComponent displayName;
      @Nullable
      private TabComponent numberFormat;

      private void update(int value, @Nullable TabComponent displayName, @Nullable TabComponent numberFormat) {
         this.value = value;
         this.displayName = displayName;
         this.numberFormat = numberFormat;
      }

      @Generated
      public Score(
         @NonNull SafeScoreboard.Objective objective,
         @NonNull String holder,
         int value,
         @Nullable TabComponent displayName,
         @Nullable TabComponent numberFormat
      ) {
         if (objective == null) {
            throw new NullPointerException("objective is marked non-null but is null");
         }

         if (holder == null) {
            throw new NullPointerException("holder is marked non-null but is null");
         }

         this.objective = objective;
         this.holder = holder;
         this.value = value;
         this.displayName = displayName;
         this.numberFormat = numberFormat;
      }

      @NonNull
      @Generated
      public SafeScoreboard.Objective getObjective() {
         return this.objective;
      }

      @NonNull
      @Generated
      public String getHolder() {
         return this.holder;
      }

      @Generated
      public int getValue() {
         return this.value;
      }

      @Nullable
      @Generated
      public TabComponent getDisplayName() {
         return this.displayName;
      }

      @Nullable
      @Generated
      public TabComponent getNumberFormat() {
         return this.numberFormat;
      }

      @Generated
      public void setValue(int value) {
         this.value = value;
      }

      @Generated
      public void setDisplayName(@Nullable TabComponent displayName) {
         this.displayName = displayName;
      }

      @Generated
      public void setNumberFormat(@Nullable TabComponent numberFormat) {
         this.numberFormat = numberFormat;
      }
   }

   public static class Team {
      @NotNull
      private Object platformTeam;
      @NonNull
      private final String name;
      @NonNull
      private TabComponent prefix;
      @NonNull
      private TabComponent suffix;
      @NonNull
      private Scoreboard.NameVisibility visibility;
      @NonNull
      private Scoreboard.CollisionRule collision;
      @NonNull
      private Collection<String> players;
      private int options;
      @NonNull
      private EnumChatFormat color;

      private void update(
         @NonNull TabComponent prefix,
         @NonNull TabComponent suffix,
         @NonNull Scoreboard.NameVisibility visibility,
         @NonNull Scoreboard.CollisionRule collision,
         int options,
         @NonNull EnumChatFormat color
      ) {
         if (prefix == null) {
            throw new NullPointerException("prefix is marked non-null but is null");
         }

         if (suffix == null) {
            throw new NullPointerException("suffix is marked non-null but is null");
         }

         if (visibility == null) {
            throw new NullPointerException("visibility is marked non-null but is null");
         }

         if (collision == null) {
            throw new NullPointerException("collision is marked non-null but is null");
         }

         if (color == null) {
            throw new NullPointerException("color is marked non-null but is null");
         }

         this.prefix = prefix;
         this.suffix = suffix;
         this.visibility = visibility;
         this.collision = collision;
         this.options = options;
         this.color = color;
      }

      private void update(@NonNull TabComponent prefix, @NonNull TabComponent suffix, @NonNull EnumChatFormat color) {
         if (prefix == null) {
            throw new NullPointerException("prefix is marked non-null but is null");
         }

         if (suffix == null) {
            throw new NullPointerException("suffix is marked non-null but is null");
         }

         if (color == null) {
            throw new NullPointerException("color is marked non-null but is null");
         }

         this.prefix = prefix;
         this.suffix = suffix;
         this.color = color;
      }

      @Generated
      public Team(
         @NotNull Object platformTeam,
         @NonNull String name,
         @NonNull TabComponent prefix,
         @NonNull TabComponent suffix,
         @NonNull Scoreboard.NameVisibility visibility,
         @NonNull Scoreboard.CollisionRule collision,
         @NonNull Collection<String> players,
         int options,
         @NonNull EnumChatFormat color
      ) {
         if (platformTeam == null) {
            throw new NullPointerException("platformTeam is marked non-null but is null");
         }

         if (name == null) {
            throw new NullPointerException("name is marked non-null but is null");
         }

         if (prefix == null) {
            throw new NullPointerException("prefix is marked non-null but is null");
         }

         if (suffix == null) {
            throw new NullPointerException("suffix is marked non-null but is null");
         }

         if (visibility == null) {
            throw new NullPointerException("visibility is marked non-null but is null");
         }

         if (collision == null) {
            throw new NullPointerException("collision is marked non-null but is null");
         }

         if (players == null) {
            throw new NullPointerException("players is marked non-null but is null");
         }

         if (color == null) {
            throw new NullPointerException("color is marked non-null but is null");
         }

         this.platformTeam = platformTeam;
         this.name = name;
         this.prefix = prefix;
         this.suffix = suffix;
         this.visibility = visibility;
         this.collision = collision;
         this.players = players;
         this.options = options;
         this.color = color;
      }

      @NotNull
      @Generated
      public Object getPlatformTeam() {
         return this.platformTeam;
      }

      @NonNull
      @Generated
      public String getName() {
         return this.name;
      }

      @NonNull
      @Generated
      public TabComponent getPrefix() {
         return this.prefix;
      }

      @NonNull
      @Generated
      public TabComponent getSuffix() {
         return this.suffix;
      }

      @NonNull
      @Generated
      public Scoreboard.NameVisibility getVisibility() {
         return this.visibility;
      }

      @NonNull
      @Generated
      public Scoreboard.CollisionRule getCollision() {
         return this.collision;
      }

      @NonNull
      @Generated
      public Collection<String> getPlayers() {
         return this.players;
      }

      @Generated
      public int getOptions() {
         return this.options;
      }

      @NonNull
      @Generated
      public EnumChatFormat getColor() {
         return this.color;
      }

      @Generated
      public void setPlatformTeam(@NotNull Object platformTeam) {
         if (platformTeam == null) {
            throw new NullPointerException("platformTeam is marked non-null but is null");
         }

         this.platformTeam = platformTeam;
      }

      @Generated
      public void setPrefix(@NonNull TabComponent prefix) {
         if (prefix == null) {
            throw new NullPointerException("prefix is marked non-null but is null");
         }

         this.prefix = prefix;
      }

      @Generated
      public void setSuffix(@NonNull TabComponent suffix) {
         if (suffix == null) {
            throw new NullPointerException("suffix is marked non-null but is null");
         }

         this.suffix = suffix;
      }

      @Generated
      public void setVisibility(@NonNull Scoreboard.NameVisibility visibility) {
         if (visibility == null) {
            throw new NullPointerException("visibility is marked non-null but is null");
         }

         this.visibility = visibility;
      }

      @Generated
      public void setCollision(@NonNull Scoreboard.CollisionRule collision) {
         if (collision == null) {
            throw new NullPointerException("collision is marked non-null but is null");
         }

         this.collision = collision;
      }

      @Generated
      public void setPlayers(@NonNull Collection<String> players) {
         if (players == null) {
            throw new NullPointerException("players is marked non-null but is null");
         }

         this.players = players;
      }

      @Generated
      public void setOptions(int options) {
         this.options = options;
      }

      @Generated
      public void setColor(@NonNull EnumChatFormat color) {
         if (color == null) {
            throw new NullPointerException("color is marked non-null but is null");
         }

         this.color = color;
      }
   }
}
