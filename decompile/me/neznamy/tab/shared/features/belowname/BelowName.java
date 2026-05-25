package me.neznamy.tab.shared.features.belowname;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import lombok.Generated;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.DisableChecker;
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
import me.neznamy.tab.shared.util.OnlinePlayers;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BelowName
   extends RefreshableFeature
   implements JoinListener,
   QuitListener,
   Loadable,
   WorldSwitchListener,
   ServerSwitchListener,
   CustomThreaded,
   ProxyFeature,
   VanishListener {
   public static final String OBJECTIVE_NAME = "TAB-BelowName";
   private final StringToComponentCache cache = new StringToComponentCache("BelowName", 1000);
   private final ThreadExecutor customThread = new ThreadExecutor("TAB Belowname Objective Thread");
   private OnlinePlayers onlinePlayers;
   private final BelowNameConfiguration configuration;
   private final BelowNameTitleRefresher textRefresher = new BelowNameTitleRefresher(this, this.customThread);
   private final DisableChecker disableChecker;
   @Nullable
   private final ProxySupport proxy = TAB.getInstance().getFeatureManager().getFeature("ProxySupport");

   public BelowName(@NotNull BelowNameConfiguration configuration) {
      this.configuration = configuration;
      this.disableChecker = new DisableChecker(
         this,
         TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(configuration.getDisableCondition()),
         this::onDisableConditionChange,
         p -> p.belowNameData.disabled
      );
      TAB.getInstance().getFeatureManager().registerFeature("BelowName-Condition", this.disableChecker);
      TAB.getInstance().getFeatureManager().registerFeature("BelowNameText", this.textRefresher);
      if (this.proxy != null) {
         this.proxy.registerMessage(BelowNameProxyPlayerData.class, in -> new BelowNameProxyPlayerData(in, this));
      }
   }

   @Override
   public void load() {
      this.onlinePlayers = new OnlinePlayers(TAB.getInstance().getOnlinePlayers());
      Map<TabPlayer, Integer> values = new HashMap<>();

      for (TabPlayer loaded : this.onlinePlayers.getPlayers()) {
         this.loadProperties(loaded);
         if (this.disableChecker.isDisableConditionMet(loaded)) {
            loaded.belowNameData.disabled.set(true);
         } else {
            this.register(loaded);
         }

         values.put(loaded, this.getValue(loaded));
         this.sendProxyMessage(loaded.getUniqueId(), values.get(loaded), loaded.belowNameData.numberFormat.get());
      }

      for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
         for (Entry<TabPlayer, Integer> entry : values.entrySet()) {
            TabPlayer target = entry.getKey();
            if (this.sameServerAndWorld(viewer, target)) {
               this.setScore(viewer, target, entry.getValue(), target.belowNameData.numberFormat.getFormat(viewer));
            }
         }
      }
   }

   private void loadProperties(@NotNull TabPlayer player) {
      player.belowNameData.score = new Property(this, player, this.configuration.getValue());
      player.belowNameData.numberFormat = new Property(this, player, this.configuration.getFancyValue());
      player.belowNameData.text = new Property(this.textRefresher, player, this.configuration.getTitle());
      player.belowNameData.defaultNumberFormat = new Property(this.textRefresher, player, this.configuration.getFancyValueDefault());
   }

   @Override
   public void onJoin(@NotNull TabPlayer connectedPlayer) {
      this.onlinePlayers.addPlayer(connectedPlayer);
      this.loadProperties(connectedPlayer);
      if (this.disableChecker.isDisableConditionMet(connectedPlayer)) {
         connectedPlayer.belowNameData.disabled.set(true);
      } else {
         this.register(connectedPlayer);
      }

      int number = this.getValue(connectedPlayer);
      Property fancy = connectedPlayer.belowNameData.numberFormat;

      for (TabPlayer all : this.onlinePlayers.getPlayers()) {
         if (this.sameServerAndWorld(connectedPlayer, all)) {
            this.setScore(all, connectedPlayer, number, fancy.getFormat(all));
            if (all != connectedPlayer) {
               this.setScore(connectedPlayer, all, this.getValue(all), all.belowNameData.numberFormat.getFormat(connectedPlayer));
            }
         }
      }

      if (this.proxy != null) {
         this.sendProxyMessage(connectedPlayer);
         if (connectedPlayer.belowNameData.disabled.get()) {
            return;
         }

         for (ProxyPlayer proxyPlayer : this.proxy.getProxyPlayers().values()) {
            if (proxyPlayer.getBelowname() != null) {
               connectedPlayer.getScoreboard()
                  .setScore(
                     "TAB-BelowName",
                     proxyPlayer.getNickname(),
                     proxyPlayer.getBelowname().getValue(),
                     null,
                     this.cache.get(proxyPlayer.getBelowname().getFancyValue())
                  );
            }
         }
      }
   }

   public void onDisableConditionChange(TabPlayer p, boolean disabledNow) {
      if (disabledNow) {
         p.getScoreboard().unregisterObjective("TAB-BelowName");
      } else {
         this.register(p);

         for (TabPlayer all : this.onlinePlayers.getPlayers()) {
            if (this.sameServerAndWorld(p, all)) {
               this.setScore(p, all, this.getValue(all), all.belowNameData.numberFormat.getFormat(p));
            }
         }

         if (this.proxy != null) {
            this.sendProxyMessage(p);

            for (ProxyPlayer proxyPlayer : this.proxy.getProxyPlayers().values()) {
               if (proxyPlayer.getBelowname() != null) {
                  p.getScoreboard()
                     .setScore(
                        "TAB-BelowName",
                        proxyPlayer.getNickname(),
                        proxyPlayer.getBelowname().getValue(),
                        null,
                        this.cache.get(proxyPlayer.getBelowname().getFancyValue())
                     );
               }
            }
         }
      }
   }

   public int getValue(@NotNull TabPlayer p) {
      String string = p.belowNameData.score.updateAndGet();

      try {
         return Integer.parseInt(string);
      } catch (NumberFormatException e) {
         try {
            int value = (int)Math.round(Double.parseDouble(string));
            TAB.getInstance().getConfigHelper().runtime().floatInBelowName(p, this.configuration.getValue(), string);
            return value;
         } catch (NumberFormatException e2) {
            TAB.getInstance().getConfigHelper().runtime().invalidNumberForBelowName(p, this.configuration.getValue(), string);
            return 0;
         }
      }
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Updating BelowName value";
   }

   @Override
   public void refresh(@NotNull TabPlayer refreshed, boolean force) {
      if (refreshed.belowNameData.score != null) {
         int number = this.getValue(refreshed);
         Property fancy = refreshed.belowNameData.numberFormat;
         fancy.update();

         for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
            if (this.sameServerAndWorld(viewer, refreshed)) {
               this.setScore(viewer, refreshed, number, fancy.getFormat(viewer));
            }
         }

         this.sendProxyMessage(refreshed.getUniqueId(), number, fancy.get());
      }
   }

   private void register(@NotNull TabPlayer player) {
      player.getScoreboard()
         .registerObjective(
            "TAB-BelowName",
            this.cache.get(player.belowNameData.text.updateAndGet()),
            Scoreboard.HealthDisplay.INTEGER,
            this.cache.get(player.belowNameData.defaultNumberFormat.updateAndGet())
         );
      player.getScoreboard().setDisplaySlot("TAB-BelowName", Scoreboard.DisplaySlot.BELOW_NAME);
   }

   public void setScore(@NotNull TabPlayer viewer, @NotNull TabPlayer scoreHolder, int value, @NotNull String fancyDisplay) {
      if (!viewer.belowNameData.disabled.get()) {
         if (viewer.canSee(scoreHolder)) {
            viewer.getScoreboard().setScore("TAB-BelowName", scoreHolder.getNickname(), value, null, this.cache.get(fancyDisplay));
         }
      }
   }

   private boolean sameServerAndWorld(@NotNull TabPlayer player1, @NotNull TabPlayer player2) {
      return player1.server == player2.server && player1.world == player2.world;
   }

   @Override
   public void onServerChange(@NotNull TabPlayer changed, @NotNull Server from, @NotNull Server to) {
      this.updatePlayer(changed);
   }

   @Override
   public void onWorldChange(@NotNull TabPlayer changed, @NotNull World from, @NotNull World to) {
      this.updatePlayer(changed);
   }

   private void updatePlayer(@NotNull TabPlayer player) {
      for (TabPlayer all : this.onlinePlayers.getPlayers()) {
         if (this.sameServerAndWorld(all, player)) {
            this.setScore(player, all, this.getValue(all), all.belowNameData.numberFormat.getFormat(player));
            if (all != player) {
               this.setScore(all, player, this.getValue(player), player.belowNameData.numberFormat.getFormat(all));
            }
         }
      }
   }

   public void processNicknameChange(@NotNull TabPlayer player) {
      this.customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         int value = this.getValue(player);

         for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
            this.setScore(viewer, player, value, player.belowNameData.numberFormat.get());
         }
      }, this.getFeatureName(), "Processing nickname change"));
   }

   @Override
   public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
      this.onlinePlayers.removePlayer(disconnectedPlayer);
   }

   @Override
   public void onJoin(@NotNull ProxyPlayer player) {
      this.updatePlayer(player);
   }

   public void updatePlayer(@NotNull ProxyPlayer player) {
      if (player.getBelowname() != null) {
         for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
            if (!viewer.belowNameData.disabled.get()) {
               viewer.getScoreboard()
                  .setScore(
                     "TAB-BelowName", player.getNickname(), player.getBelowname().getValue(), null, this.cache.get(player.getBelowname().getFancyValue())
                  );
            }
         }
      }
   }

   private void sendProxyMessage(@NotNull TabPlayer player) {
      if (this.proxy != null) {
         this.sendProxyMessage(player.getUniqueId(), this.getValue(player), player.belowNameData.numberFormat.get());
      }
   }

   private void sendProxyMessage(@NotNull UUID uniqueId, int value, @NotNull String fancyValue) {
      if (this.proxy != null) {
         this.proxy.sendMessage(new BelowNameProxyPlayerData(this, this.proxy.getIdCounter().incrementAndGet(), uniqueId, value, fancyValue));
      }
   }

   @Override
   public void onProxyLoadRequest() {
      for (TabPlayer all : this.onlinePlayers.getPlayers()) {
         this.sendProxyMessage(all);
      }
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return "BelowName";
   }

   @Override
   public void onVanishStatusChange(@NotNull TabPlayer player) {
      if (!player.isVanished()) {
         for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
            this.setScore(viewer, player, this.getValue(player), player.belowNameData.numberFormat.getFormat(viewer));
         }
      }
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
   public OnlinePlayers getOnlinePlayers() {
      return this.onlinePlayers;
   }
}
