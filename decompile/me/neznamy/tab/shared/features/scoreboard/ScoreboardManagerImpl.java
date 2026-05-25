package me.neznamy.tab.shared.features.scoreboard;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.api.scoreboard.Scoreboard;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.features.ToggleManager;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.DisplayObjectiveListener;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.ObjectiveListener;
import me.neznamy.tab.shared.features.types.QuitListener;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.features.types.ServerSwitchListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScoreboardManagerImpl
   extends RefreshableFeature
   implements ScoreboardManager,
   JoinListener,
   DisplayObjectiveListener,
   ObjectiveListener,
   Loadable,
   QuitListener,
   CustomThreaded,
   ServerSwitchListener {
   public static final String OBJECTIVE_NAME = "TAB-Scoreboard";
   private final StringToComponentCache cache = new StringToComponentCache("Scoreboard", 1000);
   private final ThreadExecutor customThread = new ThreadExecutor("TAB Scoreboard Thread");
   private final ScoreboardConfiguration configuration;
   private final Map<String, ScoreboardImpl> registeredScoreboards = new LinkedHashMap<>();
   private ScoreboardImpl[] definedScoreboards;
   @Nullable
   private ToggleManager toggleManager;
   @Nullable
   private Scoreboard announcement;

   public ScoreboardManagerImpl(@NotNull ScoreboardConfiguration configuration) {
      this.configuration = configuration;
      if (configuration.isRememberToggleChoice()) {
         this.toggleManager = new ToggleManager(TAB.getInstance().getConfiguration().getPlayerData(), "scoreboard-off");
      }
   }

   @Override
   public void load() {
      TAB.getInstance().getPlatform().registerCustomCommand(this.configuration.getToggleCommand().replaceFirst("/", ""), px -> {
         if (this.isActive()) {
            if (px.hasPermission("tab.scoreboard.toggle")) {
               this.toggleScoreboard(px, true);
            } else {
               px.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNoPermission());
            }
         }
      });

      for (Entry<String, ScoreboardConfiguration.ScoreboardDefinition> entry : this.configuration.getScoreboards().entrySet()) {
         String scoreboardName = entry.getKey();
         ScoreboardImpl sb = new ScoreboardImpl(this, scoreboardName, entry.getValue());
         this.registeredScoreboards.put(scoreboardName, sb);
         TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.scoreboardLine(scoreboardName), sb);
      }

      this.definedScoreboards = this.registeredScoreboards.values().stream().filter(s -> !s.isApi()).toArray(ScoreboardImpl[]::new);

      for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
         this.onJoin(p);
      }
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Switching scoreboards";
   }

   @Override
   public void refresh(@NotNull TabPlayer p, boolean force) {
      if (p.scoreboardData.forcedScoreboard == null && this.hasScoreboardVisible(p) && this.announcement == null && !p.scoreboardData.joinDelayed) {
         this.sendHighestScoreboard(p);
      }
   }

   @Override
   public void onJoin(@NotNull TabPlayer connectedPlayer) {
      TAB.getInstance().getPlaceholderManager().getTabExpansion().setScoreboardName(connectedPlayer, "");
      TAB.getInstance().getPlaceholderManager().getTabExpansion().setScoreboardVisible(connectedPlayer, false);
      if (this.toggleManager != null) {
         this.toggleManager.convert(connectedPlayer);
      }

      if (this.configuration.getJoinDelay() > 0) {
         connectedPlayer.scoreboardData.joinDelayed = true;
         this.customThread
            .executeLater(
               new TimedCaughtTask(
                  TAB.getInstance().getCpu(),
                  () -> {
                     this.setScoreboardVisible(
                        connectedPlayer,
                        this.configuration.isHiddenByDefault() == (this.toggleManager != null && this.toggleManager.contains(connectedPlayer)),
                        false
                     );
                     connectedPlayer.scoreboardData.joinDelayed = false;
                  },
                  this.getFeatureName(),
                  "Player Join"
               ),
               this.configuration.getJoinDelay()
            );
      } else {
         this.setScoreboardVisible(
            connectedPlayer, this.configuration.isHiddenByDefault() == (this.toggleManager != null && this.toggleManager.contains(connectedPlayer)), false
         );
      }
   }

   public void sendHighestScoreboard(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      if (this.hasScoreboardVisible(p)) {
         ScoreboardImpl scoreboard = (ScoreboardImpl)this.detectHighestScoreboard(p);
         ScoreboardImpl current = p.scoreboardData.activeScoreboard;
         if (scoreboard != current) {
            if (current != null) {
               current.removePlayer(p);
            }

            if (scoreboard != null) {
               scoreboard.addPlayer(p);
            }
         }
      }
   }

   public void unregisterScoreboard(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      if (p.scoreboardData.activeScoreboard != null) {
         p.scoreboardData.activeScoreboard.removePlayer(p);
         p.scoreboardData.activeScoreboard = null;
      }
   }

   @Nullable
   public Scoreboard detectHighestScoreboard(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      if (p.scoreboardData.forcedScoreboard != null) {
         return p.scoreboardData.forcedScoreboard;
      }

      for (ScoreboardImpl sb : this.definedScoreboards) {
         if (sb.isConditionMet(p)) {
            return sb;
         }
      }

      return null;
   }

   @Override
   public void onDisplayObjective(@NotNull TabPlayer receiver, int slot, @NotNull String objective) {
      if (slot == me.neznamy.tab.shared.platform.Scoreboard.DisplaySlot.SIDEBAR.ordinal() && !objective.equals("TAB-Scoreboard")) {
         TAB.getInstance().debug("Player " + receiver.getName() + " received scoreboard called " + objective + ", disabling TAB's scoreboard slotting.");
         receiver.scoreboardData.otherPluginScoreboard = objective;
      }
   }

   @Override
   public void onObjective(@NotNull TabPlayer receiver, int action, @NotNull String objective) {
      if (action == 1 && objective.equals(receiver.scoreboardData.otherPluginScoreboard)) {
         TAB.getInstance().debug("Player " + receiver.getName() + " no longer has another scoreboard, slotting TAB scoreboard.");
         receiver.scoreboardData.otherPluginScoreboard = null;
         if (receiver.scoreboardData.activeScoreboard != null) {
            receiver.getScoreboard().setDisplaySlot("TAB-Scoreboard", me.neznamy.tab.shared.platform.Scoreboard.DisplaySlot.SIDEBAR);
         }
      }
   }

   @Override
   public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
      ScoreboardImpl sb = disconnectedPlayer.scoreboardData.activeScoreboard;
      if (sb != null) {
         sb.removePlayerFromSet(disconnectedPlayer);
      }
   }

   @NotNull
   @Override
   public Scoreboard createScoreboard(@NonNull String name, @NonNull String title, @NonNull List<String> lines) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      if (title == null) {
         throw new NullPointerException("title is marked non-null but is null");
      }

      if (lines == null) {
         throw new NullPointerException("lines is marked non-null but is null");
      }

      this.ensureActive();
      ScoreboardImpl sb = new ScoreboardImpl(this, name, new ScoreboardConfiguration.ScoreboardDefinition(null, title, lines), true, true);
      this.registeredScoreboards.put(name, sb);
      return sb;
   }

   @Override
   public void removeScoreboard(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      ScoreboardImpl removed = this.registeredScoreboards.remove(name);
      if (removed == null) {
         throw new IllegalArgumentException("No registered scoreboard found with name " + name);
      }

      Set<TabPlayer> players = removed.getPlayers();
      removed.unregister();

      for (TabPlayer p : players) {
         p.scoreboardData.forcedScoreboard = null;
         this.sendHighestScoreboard(p);
      }
   }

   @Override
   public void removeScoreboard(@NonNull Scoreboard scoreboard) {
      if (scoreboard == null) {
         throw new NullPointerException("scoreboard is marked non-null but is null");
      }

      if (!this.registeredScoreboards.remove(scoreboard.getName(), (ScoreboardImpl)scoreboard)) {
         throw new IllegalArgumentException("This scoreboard (" + scoreboard.getName() + ") is not registered.");
      }

      Set<TabPlayer> players = ((ScoreboardImpl)scoreboard).getPlayers();
      scoreboard.unregister();

      for (TabPlayer p : players) {
         p.scoreboardData.forcedScoreboard = null;
         this.sendHighestScoreboard(p);
      }
   }

   @Override
   public void showScoreboard(@NonNull TabPlayer player, @NonNull Scoreboard scoreboard) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (scoreboard == null) {
         throw new NullPointerException("scoreboard is marked non-null but is null");
      }

      this.ensureActive();
      TabPlayer p = (TabPlayer)player;
      p.ensureLoaded();
      if (p.scoreboardData.forcedScoreboard != null) {
         p.scoreboardData.forcedScoreboard.removePlayer(p);
      }

      p.scoreboardData.forcedScoreboard = (ScoreboardImpl)scoreboard;
      if (p.scoreboardData.activeScoreboard != null) {
         p.scoreboardData.activeScoreboard.removePlayer(p);
         p.scoreboardData.activeScoreboard = null;
      }

      if (this.hasScoreboardVisible(player)) {
         ((ScoreboardImpl)scoreboard).addPlayer(p);
      }
   }

   @Override
   public boolean hasCustomScoreboard(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      return ((TabPlayer)player).scoreboardData.forcedScoreboard != null;
   }

   @Override
   public void resetScoreboard(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      TabPlayer p = (TabPlayer)player;
      p.ensureLoaded();
      if (p.scoreboardData.forcedScoreboard != null) {
         p.scoreboardData.forcedScoreboard.removePlayer(p);
         p.scoreboardData.forcedScoreboard = null;
         Scoreboard sb = this.detectHighestScoreboard(p);
         if (sb == null) {
            return;
         }

         ((ScoreboardImpl)sb).addPlayer(p);
      }
   }

   @Override
   public boolean hasScoreboardVisible(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      return ((TabPlayer)player).scoreboardData.visible;
   }

   @Override
   public void setScoreboardVisible(@NonNull TabPlayer p, boolean visible, boolean sendToggleMessage) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      this.ensureActive();
      TabPlayer player = (TabPlayer)p;
      if (player.scoreboardData.visible != visible) {
         if (visible) {
            player.scoreboardData.visible = true;
            this.sendHighestScoreboard(player);
            if (sendToggleMessage) {
               player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getScoreboardOn());
            }

            if (this.toggleManager != null) {
               if (this.configuration.isHiddenByDefault()) {
                  this.toggleManager.add(player);
               } else {
                  this.toggleManager.remove(player);
               }
            }
         } else {
            player.scoreboardData.visible = false;
            this.unregisterScoreboard(player);
            if (sendToggleMessage) {
               player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getScoreboardOff());
            }

            if (this.toggleManager != null) {
               if (this.configuration.isHiddenByDefault()) {
                  this.toggleManager.remove(player);
               } else {
                  this.toggleManager.add(player);
               }
            }
         }

         TAB.getInstance().getPlaceholderManager().getTabExpansion().setScoreboardVisible(player, visible);
      }
   }

   @Override
   public void toggleScoreboard(@NonNull TabPlayer player, boolean sendToggleMessage) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      this.setScoreboardVisible(player, !((TabPlayer)player).scoreboardData.visible, sendToggleMessage);
   }

   @Override
   public void announceScoreboard(@NonNull String scoreboard, int duration) {
      if (scoreboard == null) {
         throw new NullPointerException("scoreboard is marked non-null but is null");
      }

      this.ensureActive();
      if (duration < 0) {
         throw new IllegalArgumentException("Duration cannot be negative");
      }

      ScoreboardImpl sb = this.registeredScoreboards.get(scoreboard);
      if (sb == null) {
         throw new IllegalArgumentException("No registered scoreboard found with name " + scoreboard);
      }

      Map<TabPlayer, ScoreboardImpl> previous = new HashMap<>();
      this.customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         this.announcement = sb;

         for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (this.hasScoreboardVisible(all)) {
               previous.put(all, all.scoreboardData.activeScoreboard);
               if (all.scoreboardData.activeScoreboard != null) {
                  all.scoreboardData.activeScoreboard.removePlayer(all);
               }

               sb.addPlayer(all);
            }
         }
      }, this.getFeatureName(), "Adding announced Scoreboard"));
      this.customThread.executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (this.hasScoreboardVisible(all)) {
               sb.removePlayer(all);
               if (previous.get(all) != null) {
                  previous.get(all).addPlayer(all);
               }
            }
         }

         this.announcement = null;
      }, this.getFeatureName(), "Removing announced Scoreboard"), duration * 1000);
   }

   @Nullable
   public ScoreboardImpl getActiveScoreboard(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      return ((TabPlayer)player).scoreboardData.activeScoreboard;
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return "Scoreboard";
   }

   @Override
   public void onServerChange(@NotNull TabPlayer changed, @NotNull Server from, @NotNull Server to) {
      if (changed.scoreboardData.otherPluginScoreboard != null) {
         changed.scoreboardData.otherPluginScoreboard = null;
         if (changed.scoreboardData.activeScoreboard != null) {
            changed.getScoreboard().setDisplaySlot("TAB-Scoreboard", me.neznamy.tab.shared.platform.Scoreboard.DisplaySlot.SIDEBAR);
         }
      }
   }

   @NotNull
   @Override
   public Map<String, Scoreboard> getRegisteredScoreboards() {
      return Collections.unmodifiableMap(this.registeredScoreboards);
   }

   @Generated
   public StringToComponentCache getCache() {
      return this.cache;
   }

   @Generated
   @Override
   public ThreadExecutor getCustomThread() {
      return this.customThread;
   }

   @Generated
   public ScoreboardConfiguration getConfiguration() {
      return this.configuration;
   }

   @Generated
   public ScoreboardImpl[] getDefinedScoreboards() {
      return this.definedScoreboards;
   }

   @Nullable
   @Generated
   public ToggleManager getToggleManager() {
      return this.toggleManager;
   }

   @Nullable
   @Generated
   public Scoreboard getAnnouncement() {
      return this.announcement;
   }
}
