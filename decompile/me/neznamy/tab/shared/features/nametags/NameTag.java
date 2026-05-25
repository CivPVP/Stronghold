package me.neznamy.tab.shared.features.nametags;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.MessageFile;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.DisableChecker;
import me.neznamy.tab.shared.features.types.GroupListener;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.ProxyFeature;
import me.neznamy.tab.shared.features.types.QuitListener;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.features.types.ServerSwitchListener;
import me.neznamy.tab.shared.features.types.VanishListener;
import me.neznamy.tab.shared.features.types.WorldSwitchListener;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.util.OnlinePlayers;
import me.neznamy.tab.shared.util.cache.LastColorCache;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NameTag
   extends RefreshableFeature
   implements NameTagManager,
   JoinListener,
   QuitListener,
   Loadable,
   WorldSwitchListener,
   ServerSwitchListener,
   VanishListener,
   CustomThreaded,
   ProxyFeature,
   GroupListener {
   private final ThreadExecutor customThread = new ThreadExecutor("TAB NameTag Thread");
   private OnlinePlayers onlinePlayers;
   private final TeamConfiguration configuration;
   private final StringToComponentCache prefixCache = new StringToComponentCache("NameTag prefix", 1000);
   private final StringToComponentCache lastColorCache = new LastColorCache("NameTag last prefix color", 1000);
   private final StringToComponentCache suffixCache = new StringToComponentCache("NameTag suffix", 1000);
   private final VisibilityRefresher visibilityRefresher;
   private final CollisionManager collisionManager;
   private final int teamOptions;
   private final DisableChecker disableChecker;
   @Nullable
   private final ProxySupport proxy = TAB.getInstance().getFeatureManager().getFeature("ProxySupport");

   public NameTag(@NotNull TeamConfiguration configuration) {
      this.configuration = configuration;
      this.teamOptions = configuration.isCanSeeFriendlyInvisibles() ? 2 : 0;
      this.disableChecker = new DisableChecker(
         this,
         TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(configuration.getDisableCondition()),
         this::onDisableConditionChange,
         p -> p.teamData.disabled
      );
      this.visibilityRefresher = new VisibilityRefresher(this);
      this.collisionManager = new CollisionManager(this);
      TAB.getInstance().getFeatureManager().registerFeature("NameTag16-Condition", this.disableChecker);
      if (this.proxy != null) {
         this.proxy.registerMessage(NameTagProxyPlayerData.class, in -> new NameTagProxyPlayerData(in, this));
      }
   }

   @Override
   public void load() {
      this.onlinePlayers = new OnlinePlayers(TAB.getInstance().getOnlinePlayers());
      TAB.getInstance().getFeatureManager().registerFeature("NameTagVisibility", this.visibilityRefresher);
      TAB.getInstance().getFeatureManager().registerFeature("NameTagCollision", this.collisionManager);

      for (TabPlayer all : this.onlinePlayers.getPlayers()) {
         this.loadProperties(all);
         all.teamData.teamName = all.sortingData.shortTeamName;
         if (this.disableChecker.isDisableConditionMet(all)) {
            all.teamData.disabled.set(true);
         } else {
            TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagVisibility(all, true);
            this.sendProxyMessage(all);
         }
      }

      for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
         for (TabPlayer target : this.onlinePlayers.getPlayers()) {
            if (target.isVanished() && !viewer.canSee(target)) {
               target.teamData.vanishedFor.add(viewer.getUniqueId());
            }

            if (!target.teamData.isDisabled()) {
               this.registerTeam(target, viewer);
            }
         }
      }

      this.visibilityRefresher.load();
      this.collisionManager.load();
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Updating prefix/suffix";
   }

   @Override
   public void refresh(@NotNull TabPlayer refreshed, boolean force) {
      if (!refreshed.teamData.isDisabled()) {
         boolean refresh;
         if (force) {
            this.updateProperties(refreshed);
            refresh = true;
         } else {
            boolean prefix = refreshed.teamData.prefix.update();
            boolean suffix = refreshed.teamData.suffix.update();
            refresh = prefix || suffix;
         }

         if (refresh) {
            this.updatePrefixSuffix(refreshed);
         }
      }
   }

   @Override
   public void onGroupChange(@NotNull TabPlayer player) {
      if (this.updateProperties(player) && !player.teamData.isDisabled()) {
         this.updatePrefixSuffix(player);
      }
   }

   @Override
   public void onJoin(@NotNull TabPlayer connectedPlayer) {
      this.onlinePlayers.addPlayer(connectedPlayer);
      this.loadProperties(connectedPlayer);
      connectedPlayer.teamData.teamName = connectedPlayer.sortingData.shortTeamName;

      for (TabPlayer all : this.onlinePlayers.getPlayers()) {
         if (all != connectedPlayer) {
            if (connectedPlayer.isVanished() && !all.canSee(connectedPlayer)) {
               connectedPlayer.teamData.vanishedFor.add(all.getUniqueId());
            }

            if (all.isVanished() && !connectedPlayer.canSee(all)) {
               all.teamData.vanishedFor.add(connectedPlayer.getUniqueId());
            }

            if (!all.teamData.isDisabled()) {
               this.registerTeam(all, connectedPlayer);
            }
         }
      }

      TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagVisibility(connectedPlayer, true);
      if (this.proxy != null) {
         ProxyPlayer proxyPlayer = this.proxy.getProxyPlayers().get(connectedPlayer.getUniqueId());
         if (proxyPlayer != null && proxyPlayer.getNametag() != null) {
            for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
               ((SafeScoreboard)viewer.getScoreboard()).unregisterTeamSafe(proxyPlayer.getNametag().getResolvedTeamName());
            }

            proxyPlayer.setNametag(null);
         }
      }

      if (this.disableChecker.isDisableConditionMet(connectedPlayer)) {
         connectedPlayer.teamData.disabled.set(true);
      } else {
         this.registerTeam(connectedPlayer);
         if (this.proxy != null) {
            for (ProxyPlayer proxied : this.proxy.getProxyPlayers().values()) {
               if (proxied.getNametag() != null) {
                  connectedPlayer.getScoreboard()
                     .registerTeam(
                        proxied.getNametag().getResolvedTeamName(),
                        this.prefixCache.get(proxied.getNametag().getPrefix()),
                        this.suffixCache.get(proxied.getNametag().getSuffix()),
                        proxied.getNametag().getNameVisibility(),
                        Scoreboard.CollisionRule.ALWAYS,
                        Collections.singletonList(proxied.getNickname()),
                        this.teamOptions,
                        this.lastColorCache.get(proxied.getNametag().getPrefix()).getLastStyle().toEnumChatFormat()
                     );
               }
            }

            this.sendProxyMessage(connectedPlayer);
         }
      }
   }

   @Override
   public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
      this.onlinePlayers.removePlayer(disconnectedPlayer);

      for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
         ((SafeScoreboard)viewer.getScoreboard()).unregisterTeamSafe(disconnectedPlayer.teamData.teamName);
      }
   }

   @Override
   public void onServerChange(@NonNull TabPlayer p, @NotNull Server from, @NotNull Server to) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      if (this.updateProperties(p) && !p.teamData.isDisabled()) {
         this.updatePrefixSuffix(p);
      }
   }

   @Override
   public void onWorldChange(@NotNull TabPlayer changed, @NotNull World from, @NotNull World to) {
      if (this.updateProperties(changed) && !changed.teamData.isDisabled()) {
         this.updatePrefixSuffix(changed);
      }
   }

   @Override
   public void onVanishStatusChange(@NotNull TabPlayer player) {
      if (player.isVanished()) {
         for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
            if (viewer != player && !viewer.canSee(player)) {
               player.teamData.vanishedFor.add(viewer.getUniqueId());
               if (!player.teamData.isDisabled()) {
                  ((SafeScoreboard)viewer.getScoreboard()).unregisterTeamSafe(player.teamData.teamName);
               }
            }
         }
      } else {
         Set<UUID> ids = new HashSet<>(player.teamData.vanishedFor);
         player.teamData.vanishedFor.clear();
         if (!player.teamData.isDisabled()) {
            for (UUID id : ids) {
               TabPlayer viewer = TAB.getInstance().getPlayer(id);
               if (viewer != null) {
                  this.registerTeam(player, viewer);
               }
            }
         }
      }
   }

   private void loadProperties(@NotNull TabPlayer player) {
      player.teamData.prefix = player.loadPropertyFromConfig(this, "tagprefix", "");
      player.teamData.suffix = player.loadPropertyFromConfig(this, "tagsuffix", "");
   }

   private boolean updateProperties(@NotNull TabPlayer p) {
      boolean changed = p.updatePropertyFromConfig(p.teamData.prefix, "");
      if (p.updatePropertyFromConfig(p.teamData.suffix, "")) {
         changed = true;
      }

      return changed;
   }

   public void onDisableConditionChange(TabPlayer p, boolean disabledNow) {
      if (disabledNow) {
         this.unregisterTeam(p.teamData.teamName);
      } else {
         this.registerTeam(p);
      }
   }

   private void updatePrefixSuffix(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
         viewer.getScoreboard()
            .updateTeam(
               player.teamData.teamName,
               this.prefixCache.get(player.teamData.prefix.getFormat(viewer)),
               this.suffixCache.get(player.teamData.suffix.getFormat(viewer)),
               this.lastColorCache.get(player.teamData.prefix.getFormat(viewer)).getLastStyle().toEnumChatFormat()
            );
      }

      this.sendProxyMessage(player);
   }

   public void updateCollision(@NonNull TabPlayer player, boolean moveToThread) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      Runnable r = () -> {
         for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
            viewer.getScoreboard()
               .updateTeam(player.teamData.teamName, player.teamData.getCollisionRule() ? Scoreboard.CollisionRule.ALWAYS : Scoreboard.CollisionRule.NEVER);
         }
      };
      if (moveToThread) {
         this.customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), r, this.getFeatureName(), "Updating collision"));
      } else {
         r.run();
      }
   }

   public void updateVisibility(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.customThread
         .execute(
            new TimedCaughtTask(
               TAB.getInstance().getCpu(),
               () -> {
                  for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
                     viewer.getScoreboard()
                        .updateTeam(
                           player.teamData.teamName,
                           this.getTeamVisibility(player, viewer) ? Scoreboard.NameVisibility.ALWAYS : Scoreboard.NameVisibility.NEVER
                        );
                  }

                  this.sendProxyMessage(player);
               },
               this.getFeatureName(),
               "Updating visibility"
            )
         );
   }

   public void updateVisibility(@NonNull TabPlayer player, @NonNull TabPlayer viewer) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (viewer == null) {
         throw new NullPointerException("viewer is marked non-null but is null");
      }

      viewer.getScoreboard()
         .updateTeam(player.teamData.teamName, this.getTeamVisibility(player, viewer) ? Scoreboard.NameVisibility.ALWAYS : Scoreboard.NameVisibility.NEVER);
   }

   private void unregisterTeam(@NonNull String teamName) {
      if (teamName == null) {
         throw new NullPointerException("teamName is marked non-null but is null");
      }

      for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
         ((SafeScoreboard)viewer.getScoreboard()).unregisterTeamSafe(teamName);
      }
   }

   private void registerTeam(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
         this.registerTeam(p, viewer);
      }
   }

   private void registerTeam(@NonNull TabPlayer p, @NonNull TabPlayer viewer) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      if (viewer == null) {
         throw new NullPointerException("viewer is marked non-null but is null");
      }

      if (!p.teamData.isDisabled() && !p.teamData.vanishedFor.contains(viewer.getUniqueId())) {
         if (viewer.canSee(p) || p == viewer) {
            viewer.getScoreboard()
               .registerTeam(
                  p.teamData.teamName,
                  this.prefixCache.get(p.teamData.prefix.getFormat(viewer)),
                  this.suffixCache.get(p.teamData.suffix.getFormat(viewer)),
                  this.getTeamVisibility(p, viewer) ? Scoreboard.NameVisibility.ALWAYS : Scoreboard.NameVisibility.NEVER,
                  p.teamData.getCollisionRule() ? Scoreboard.CollisionRule.ALWAYS : Scoreboard.CollisionRule.NEVER,
                  Collections.singletonList(p.getNickname()),
                  this.teamOptions,
                  this.lastColorCache.get(p.teamData.prefix.getFormat(viewer)).getLastStyle().toEnumChatFormat()
               );
         }
      }
   }

   public boolean getTeamVisibility(@NonNull TabPlayer p, @NonNull TabPlayer viewer) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      } else if (viewer == null) {
         throw new NullPointerException("viewer is marked non-null but is null");
      } else if (p.teamData.hasHiddenNametag()) {
         return false;
      } else if (p.teamData.hasHiddenNametag(viewer)) {
         return false;
      } else {
         return viewer.teamData.invisibleNameTagView ? false : viewer.getVersion().getMinorVersion() != 8 || !p.hasInvisibilityPotion();
      }
   }

   public void updateTeamName(@NonNull TabPlayer player, @NonNull String newTeamName) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (newTeamName == null) {
         throw new NullPointerException("newTeamName is marked non-null but is null");
      }

      this.customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         if (player.teamData.teamName != null) {
            if (player.teamData.isDisabled()) {
               player.teamData.teamName = newTeamName;
            } else {
               for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
                  viewer.getScoreboard().renameTeam(player.teamData.teamName, newTeamName);
               }

               player.teamData.teamName = newTeamName;
               this.sendProxyMessage(player);
            }
         }
      }, this.getFeatureName(), "Updating team name"));
   }

   public void hideNameTag(@NonNull TabPlayer player, @NonNull NameTagInvisibilityReason reason, @NonNull String cpuReason, boolean sendMessage) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (reason == null) {
         throw new NullPointerException("reason is marked non-null but is null");
      }

      if (cpuReason == null) {
         throw new NullPointerException("cpuReason is marked non-null but is null");
      }

      this.ensureActive();
      this.customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         if (player.teamData.hideNametag(reason)) {
            this.updateVisibility(player);
         }

         if (sendMessage) {
            player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetHidden());
         }
      }, this.getFeatureName(), cpuReason));
   }

   public void hideNameTag(
      @NonNull TabPlayer player, @NonNull TabPlayer viewer, @NonNull NameTagInvisibilityReason reason, @NonNull String cpuReason, boolean sendMessage
   ) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (viewer == null) {
         throw new NullPointerException("viewer is marked non-null but is null");
      }

      if (reason == null) {
         throw new NullPointerException("reason is marked non-null but is null");
      }

      if (cpuReason == null) {
         throw new NullPointerException("cpuReason is marked non-null but is null");
      }

      this.ensureActive();
      this.customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         if (player.teamData.hideNametag(viewer, reason)) {
            this.updateVisibility(player, viewer);
         }

         if (sendMessage) {
            player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetHidden());
         }
      }, this.getFeatureName(), cpuReason));
   }

   public void showNameTag(@NonNull TabPlayer player, @NonNull NameTagInvisibilityReason reason, @NonNull String cpuReason, boolean sendMessage) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (reason == null) {
         throw new NullPointerException("reason is marked non-null but is null");
      }

      if (cpuReason == null) {
         throw new NullPointerException("cpuReason is marked non-null but is null");
      }

      this.ensureActive();
      this.customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         if (player.teamData.showNametag(reason)) {
            this.updateVisibility(player);
         }

         if (sendMessage) {
            player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetShown());
         }
      }, this.getFeatureName(), cpuReason));
   }

   public void showNameTag(
      @NonNull TabPlayer player, @NonNull TabPlayer viewer, @NonNull NameTagInvisibilityReason reason, @NonNull String cpuReason, boolean sendMessage
   ) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (viewer == null) {
         throw new NullPointerException("viewer is marked non-null but is null");
      }

      if (reason == null) {
         throw new NullPointerException("reason is marked non-null but is null");
      }

      if (cpuReason == null) {
         throw new NullPointerException("cpuReason is marked non-null but is null");
      }

      this.ensureActive();
      this.customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         if (player.teamData.showNametag(viewer, reason)) {
            this.updateVisibility(player, viewer);
         }

         if (sendMessage) {
            player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetShown());
         }
      }, this.getFeatureName(), cpuReason));
   }

   public void toggleNameTag(@NonNull TabPlayer player, @NonNull NameTagInvisibilityReason reason, @NonNull String cpuReason, boolean sendMessage) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (reason == null) {
         throw new NullPointerException("reason is marked non-null but is null");
      }

      if (cpuReason == null) {
         throw new NullPointerException("cpuReason is marked non-null but is null");
      }

      this.ensureActive();
      this.customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         if (player.teamData.hasHiddenNametag(reason)) {
            player.teamData.showNametag(reason);
            if (sendMessage) {
               player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetShown());
            }
         } else {
            player.teamData.hideNametag(reason);
            if (sendMessage) {
               player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetHidden());
            }
         }

         this.updateVisibility(player);
      }, this.getFeatureName(), cpuReason));
   }

   public void toggleNameTag(
      @NonNull TabPlayer player, @NonNull TabPlayer viewer, @NonNull NameTagInvisibilityReason reason, @NonNull String cpuReason, boolean sendMessage
   ) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (viewer == null) {
         throw new NullPointerException("viewer is marked non-null but is null");
      }

      if (reason == null) {
         throw new NullPointerException("reason is marked non-null but is null");
      }

      if (cpuReason == null) {
         throw new NullPointerException("cpuReason is marked non-null but is null");
      }

      this.ensureActive();
      this.customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         if (player.teamData.hasHiddenNametag(viewer, reason)) {
            player.teamData.showNametag(viewer, reason);
            if (sendMessage) {
               player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetShown());
            }
         } else {
            player.teamData.hideNametag(viewer, reason);
            if (sendMessage) {
               player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagTargetHidden());
            }
         }

         this.updateVisibility(player, viewer);
      }, this.getFeatureName(), cpuReason));
   }

   private void sendProxyMessage(@NotNull TabPlayer player) {
      if (this.proxy != null) {
         this.proxy
            .sendMessage(
               new NameTagProxyPlayerData(
                  this,
                  this.proxy.getIdCounter().incrementAndGet(),
                  player.getUniqueId(),
                  player.teamData.teamName,
                  player.teamData.prefix.get(),
                  player.teamData.suffix.get(),
                  this.getTeamVisibility(player, player) ? Scoreboard.NameVisibility.ALWAYS : Scoreboard.NameVisibility.NEVER
               )
            );
      }
   }

   @Override
   public void onProxyLoadRequest() {
      for (TabPlayer all : this.onlinePlayers.getPlayers()) {
         this.sendProxyMessage(all);
      }
   }

   @Override
   public void onQuit(@NotNull ProxyPlayer player) {
      if (player.getNametag() != null) {
         for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
            ((SafeScoreboard)viewer.getScoreboard()).unregisterTeamSafe(player.getNametag().getResolvedTeamName());
         }
      }
   }

   @Override
   public void onJoin(@NotNull ProxyPlayer player) {
      if (player.getNametag() != null) {
         for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
            viewer.getScoreboard()
               .registerTeam(
                  player.getNametag().getResolvedTeamName(),
                  this.prefixCache.get(player.getNametag().getPrefix()),
                  this.suffixCache.get(player.getNametag().getSuffix()),
                  player.getNametag().getNameVisibility(),
                  Scoreboard.CollisionRule.ALWAYS,
                  Collections.singletonList(player.getNickname()),
                  this.teamOptions,
                  this.lastColorCache.get(player.getNametag().getPrefix()).getLastStyle().toEnumChatFormat()
               );
         }
      }
   }

   @Override
   public void hideNameTag(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.hideNameTag((TabPlayer)player, NameTagInvisibilityReason.API_HIDE, "Processing API call (hideNameTag)", false);
   }

   @Override
   public void hideNameTag(@NonNull TabPlayer player, @NonNull TabPlayer viewer) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (viewer == null) {
         throw new NullPointerException("viewer is marked non-null but is null");
      }

      this.hideNameTag((TabPlayer)player, (TabPlayer)viewer, NameTagInvisibilityReason.API_HIDE, "Processing API call (hideNameTag)", false);
   }

   @Override
   public void showNameTag(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.showNameTag((TabPlayer)player, NameTagInvisibilityReason.API_HIDE, "Processing API call (showNameTag)", false);
   }

   @Override
   public void showNameTag(@NonNull TabPlayer player, @NonNull TabPlayer viewer) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (viewer == null) {
         throw new NullPointerException("viewer is marked non-null but is null");
      }

      this.showNameTag((TabPlayer)player, (TabPlayer)viewer, NameTagInvisibilityReason.API_HIDE, "Processing API call (showNameTag)", false);
   }

   @Override
   public boolean hasHiddenNameTag(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      return ((TabPlayer)player).teamData.hasHiddenNametag(NameTagInvisibilityReason.API_HIDE);
   }

   @Override
   public boolean hasHiddenNameTag(@NonNull TabPlayer player, @NonNull TabPlayer viewer) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (viewer == null) {
         throw new NullPointerException("viewer is marked non-null but is null");
      }

      this.ensureActive();
      return ((TabPlayer)player).teamData.hasHiddenNametag((TabPlayer)viewer, NameTagInvisibilityReason.API_HIDE);
   }

   @Override
   public void pauseTeamHandling(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      this.customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         TabPlayer p = (TabPlayer)player;
         p.ensureLoaded();
         if (!p.teamData.teamHandlingPaused) {
            if (!p.teamData.isDisabled()) {
               this.unregisterTeam(p.teamData.teamName);
            }

            p.teamData.teamHandlingPaused = true;
         }
      }, this.getFeatureName(), "Processing API call (pauseTeamHandling)"));
   }

   @Override
   public void resumeTeamHandling(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      this.customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         TabPlayer p = (TabPlayer)player;
         p.ensureLoaded();
         if (p.teamData.teamHandlingPaused) {
            p.teamData.teamHandlingPaused = false;
            if (!p.teamData.isDisabled()) {
               this.registerTeam(p);
            }
         }
      }, this.getFeatureName(), "Processing API call (resumeTeamHandling)"));
   }

   @Override
   public boolean hasTeamHandlingPaused(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      } else {
         return ((TabPlayer)player).teamData.teamHandlingPaused;
      }
   }

   @Override
   public void setCollisionRule(@NonNull TabPlayer player, Boolean collision) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      this.customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         TabPlayer p = (TabPlayer)player;
         p.ensureLoaded();
         if (!Objects.equals(p.teamData.forcedCollision, collision)) {
            p.teamData.forcedCollision = collision;
            this.updateCollision(p, true);
         }
      }, this.getFeatureName(), "Processing API call (setCollisionRule)"));
   }

   @Override
   public Boolean getCollisionRule(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      TabPlayer p = (TabPlayer)player;
      p.ensureLoaded();
      return p.teamData.forcedCollision;
   }

   @Override
   public void setPrefix(@NonNull TabPlayer player, @Nullable String prefix) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      this.customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         TabPlayer p = (TabPlayer)player;
         p.ensureLoaded();
         p.teamData.prefix.setTemporaryValue(prefix);
         this.updatePrefixSuffix(p);
      }, this.getFeatureName(), "Processing API call (setPrefix)"));
   }

   @Override
   public void setSuffix(@NonNull TabPlayer player, @Nullable String suffix) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      this.customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         TabPlayer p = (TabPlayer)player;
         p.ensureLoaded();
         p.teamData.suffix.setTemporaryValue(suffix);
         this.updatePrefixSuffix(p);
      }, this.getFeatureName(), "Processing API call (setSuffix)"));
   }

   @Override
   public String getCustomPrefix(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      TabPlayer p = (TabPlayer)player;
      p.ensureLoaded();
      return p.teamData.prefix.getTemporaryValue();
   }

   @Override
   public String getCustomSuffix(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      TabPlayer p = (TabPlayer)player;
      p.ensureLoaded();
      return p.teamData.suffix.getTemporaryValue();
   }

   @NotNull
   @Override
   public String getOriginalRawPrefix(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      TabPlayer p = (TabPlayer)player;
      p.ensureLoaded();
      return p.teamData.prefix.getOriginalRawValue();
   }

   @NotNull
   @Override
   public String getOriginalRawSuffix(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      TabPlayer p = (TabPlayer)player;
      p.ensureLoaded();
      return p.teamData.suffix.getOriginalRawValue();
   }

   @NotNull
   @Override
   public String getOriginalReplacedPrefix(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      TabPlayer p = (TabPlayer)player;
      p.ensureLoaded();
      return p.teamData.prefix.getOriginalReplacedValue();
   }

   @NotNull
   @Override
   public String getOriginalReplacedSuffix(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      TabPlayer p = (TabPlayer)player;
      p.ensureLoaded();
      return p.teamData.suffix.getOriginalReplacedValue();
   }

   @NotNull
   @Override
   public String getOriginalPrefix(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      } else {
         return this.getOriginalRawPrefix(player);
      }
   }

   @NotNull
   @Override
   public String getOriginalSuffix(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      } else {
         return this.getOriginalRawSuffix(player);
      }
   }

   @Override
   public void toggleNameTagVisibilityView(@NonNull TabPlayer p, boolean sendToggleMessage) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      this.setNameTagVisibilityView((TabPlayer)p, ((TabPlayer)p).teamData.invisibleNameTagView, sendToggleMessage);
   }

   @Override
   public void showNameTagVisibilityView(@NonNull TabPlayer p, boolean sendToggleMessage) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      this.setNameTagVisibilityView((TabPlayer)p, true, sendToggleMessage);
   }

   @Override
   public void hideNameTagVisibilityView(@NonNull TabPlayer p, boolean sendToggleMessage) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      this.setNameTagVisibilityView((TabPlayer)p, false, sendToggleMessage);
   }

   private void setNameTagVisibilityView(@NonNull TabPlayer player, boolean visible, boolean sendToggleMessage) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      if (player.teamData.invisibleNameTagView == visible) {
         player.teamData.invisibleNameTagView = !visible;
         if (sendToggleMessage) {
            MessageFile messageFile = TAB.getInstance().getConfiguration().getMessages();
            player.sendMessage(visible ? messageFile.getNameTagViewShown() : messageFile.getNameTagViewHidden());
         }

         TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagVisibility(player, visible);

         for (TabPlayer all : this.onlinePlayers.getPlayers()) {
            this.updateVisibility(all, player);
         }
      }
   }

   @Override
   public boolean hasHiddenNameTagVisibilityView(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      return ((TabPlayer)player).teamData.invisibleNameTagView;
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return "NameTags";
   }

   @Generated
   @Override
   public ThreadExecutor getCustomThread() {
      return this.customThread;
   }

   @Generated
   public OnlinePlayers getOnlinePlayers() {
      return this.onlinePlayers;
   }

   @Generated
   public TeamConfiguration getConfiguration() {
      return this.configuration;
   }

   @Generated
   public StringToComponentCache getPrefixCache() {
      return this.prefixCache;
   }

   @Generated
   public StringToComponentCache getLastColorCache() {
      return this.lastColorCache;
   }

   @Generated
   public StringToComponentCache getSuffixCache() {
      return this.suffixCache;
   }

   @Generated
   public VisibilityRefresher getVisibilityRefresher() {
      return this.visibilityRefresher;
   }

   @Generated
   public CollisionManager getCollisionManager() {
      return this.collisionManager;
   }

   @Generated
   public int getTeamOptions() {
      return this.teamOptions;
   }

   @Generated
   public DisableChecker getDisableChecker() {
      return this.disableChecker;
   }

   @Nullable
   @Generated
   public ProxySupport getProxy() {
      return this.proxy;
   }
}
