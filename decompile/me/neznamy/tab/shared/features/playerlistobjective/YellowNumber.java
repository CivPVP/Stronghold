package me.neznamy.tab.shared.features.playerlistobjective;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import lombok.Generated;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.DisableChecker;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.ProxyFeature;
import me.neznamy.tab.shared.features.types.QuitListener;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.OnlinePlayers;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class YellowNumber extends RefreshableFeature implements JoinListener, QuitListener, Loadable, CustomThreaded, ProxyFeature {
   public static final String OBJECTIVE_NAME = "TAB-PlayerList";
   private final StringToComponentCache cache = new StringToComponentCache("Playerlist Objective", 1000);
   private final ThreadExecutor customThread = new ThreadExecutor("TAB Playerlist Objective Thread");
   private OnlinePlayers onlinePlayers;
   private final PlayerListObjectiveConfiguration configuration;
   private final PlayerListObjectiveTitleRefresher titleRefresher = new PlayerListObjectiveTitleRefresher(this);
   private final DisableChecker disableChecker;
   @Nullable
   private final ProxySupport proxy = TAB.getInstance().getFeatureManager().getFeature("ProxySupport");

   public YellowNumber(@NotNull PlayerListObjectiveConfiguration configuration) {
      this.configuration = configuration;
      this.disableChecker = new DisableChecker(
         this,
         TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(configuration.getDisableCondition()),
         this::onDisableConditionChange,
         p -> p.playerlistObjectiveData.disabled
      );
      TAB.getInstance().getFeatureManager().registerFeature("YellowNumber-Condition", this.disableChecker);
      TAB.getInstance().getFeatureManager().registerFeature("YellowNumberText", this.titleRefresher);
      if (this.proxy != null) {
         this.proxy.registerMessage(PlayerListObjectiveProxyPlayerData.class, in -> new PlayerListObjectiveProxyPlayerData(this, in));
      }
   }

   @Override
   public void load() {
      this.onlinePlayers = new OnlinePlayers(TAB.getInstance().getOnlinePlayers());
      Map<TabPlayer, Integer> values = new HashMap<>();

      for (TabPlayer loaded : this.onlinePlayers.getPlayers()) {
         this.loadProperties(loaded);
         if (this.disableChecker.isDisableConditionMet(loaded)) {
            loaded.playerlistObjectiveData.disabled.set(true);
         } else {
            this.register(loaded);
         }

         values.put(loaded, this.getValueNumber(loaded));
         this.sendProxyMessage(loaded.getUniqueId(), values.get(loaded), loaded.playerlistObjectiveData.valueModern.get());
      }

      for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
         for (Entry<TabPlayer, Integer> entry : values.entrySet()) {
            this.setScore(viewer, entry.getKey(), entry.getValue(), entry.getKey().playerlistObjectiveData.valueModern.getFormat(viewer));
         }
      }
   }

   private void loadProperties(@NotNull TabPlayer player) {
      player.playerlistObjectiveData.valueLegacy = new Property(this, player, this.configuration.getValue());
      player.playerlistObjectiveData.valueModern = new Property(this, player, this.configuration.getFancyValue());
      player.playerlistObjectiveData.title = new Property(this.titleRefresher, player, this.configuration.getTitle());
   }

   @Override
   public void onJoin(@NotNull TabPlayer connectedPlayer) {
      this.onlinePlayers.addPlayer(connectedPlayer);
      this.loadProperties(connectedPlayer);
      if (this.disableChecker.isDisableConditionMet(connectedPlayer)) {
         connectedPlayer.playerlistObjectiveData.disabled.set(true);
      } else {
         this.register(connectedPlayer);
      }

      int value = this.getValueNumber(connectedPlayer);
      Property valueFancy = connectedPlayer.playerlistObjectiveData.valueModern;
      valueFancy.update();

      for (TabPlayer all : this.onlinePlayers.getPlayers()) {
         this.setScore(all, connectedPlayer, value, valueFancy.getFormat(connectedPlayer));
         if (all != connectedPlayer) {
            this.setScore(connectedPlayer, all, this.getValueNumber(all), all.playerlistObjectiveData.valueModern.getFormat(connectedPlayer));
         }
      }

      if (this.proxy != null) {
         this.sendProxyMessage(connectedPlayer);
         if (connectedPlayer.playerlistObjectiveData.disabled.get()) {
            return;
         }

         for (ProxyPlayer proxied : this.proxy.getProxyPlayers().values()) {
            if (proxied.getPlayerlist() != null) {
               connectedPlayer.getScoreboard()
                  .setScore(
                     "TAB-PlayerList", proxied.getNickname(), proxied.getPlayerlist().getValue(), null, this.cache.get(proxied.getPlayerlist().getFancyValue())
                  );
            }
         }
      }
   }

   public void onDisableConditionChange(TabPlayer p, boolean disabledNow) {
      if (disabledNow) {
         p.getScoreboard().unregisterObjective("TAB-PlayerList");
      } else {
         this.register(p);

         for (TabPlayer all : this.onlinePlayers.getPlayers()) {
            this.setScore(p, all, this.getValueNumber(all), all.playerlistObjectiveData.valueModern.getFormat(p));
         }

         if (this.proxy != null) {
            this.sendProxyMessage(p);

            for (ProxyPlayer proxied : this.proxy.getProxyPlayers().values()) {
               if (proxied.getPlayerlist() != null) {
                  p.getScoreboard()
                     .setScore(
                        "TAB-PlayerList",
                        proxied.getNickname(),
                        proxied.getPlayerlist().getValue(),
                        null,
                        this.cache.get(proxied.getPlayerlist().getFancyValue())
                     );
               }
            }
         }
      }
   }

   public int getValueNumber(@NotNull TabPlayer p) {
      String string = p.playerlistObjectiveData.valueLegacy.updateAndGet();

      try {
         return Integer.parseInt(string);
      } catch (NumberFormatException e) {
         try {
            int value = (int)Math.round(Double.parseDouble(string));
            TAB.getInstance().getConfigHelper().runtime().floatInPlayerlistObjective(p, this.configuration.getValue(), string);
            return value;
         } catch (NumberFormatException e2) {
            TAB.getInstance().getConfigHelper().runtime().invalidNumberForPlayerlistObjective(p, this.configuration.getValue(), string);
            return 0;
         }
      }
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Updating value";
   }

   @Override
   public void refresh(@NotNull TabPlayer refreshed, boolean force) {
      if (refreshed.playerlistObjectiveData.valueLegacy != null) {
         int value = this.getValueNumber(refreshed);
         refreshed.playerlistObjectiveData.valueModern.update();

         for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
            this.setScore(viewer, refreshed, value, refreshed.playerlistObjectiveData.valueModern.getFormat(viewer));
         }

         this.sendProxyMessage(refreshed.getUniqueId(), value, refreshed.playerlistObjectiveData.valueModern.get());
      }
   }

   private void register(@NotNull TabPlayer player) {
      player.getScoreboard()
         .registerObjective(
            "TAB-PlayerList", this.cache.get(player.playerlistObjectiveData.title.updateAndGet()), this.configuration.getHealthDisplay(), TabComponent.empty()
         );
      player.getScoreboard().setDisplaySlot("TAB-PlayerList", Scoreboard.DisplaySlot.PLAYER_LIST);
   }

   public void setScore(@NotNull TabPlayer viewer, @NotNull TabPlayer scoreHolder, int value, @NotNull String fancyValue) {
      if (!viewer.playerlistObjectiveData.disabled.get()) {
         viewer.getScoreboard().setScore("TAB-PlayerList", scoreHolder.getNickname(), value, null, this.cache.get(fancyValue));
      }
   }

   public void processNicknameChange(@NotNull TabPlayer player) {
      this.customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         int value = this.getValueNumber(player);

         for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
            this.setScore(viewer, player, value, player.playerlistObjectiveData.valueModern.get());
         }
      }, this.getFeatureName(), "Processing nickname change"));
   }

   @Override
   public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
      this.onlinePlayers.removePlayer(disconnectedPlayer);
   }

   private void sendProxyMessage(@NotNull TabPlayer player) {
      if (this.proxy != null) {
         this.sendProxyMessage(player.getUniqueId(), this.getValueNumber(player), player.playerlistObjectiveData.valueModern.get());
      }
   }

   private void sendProxyMessage(@NotNull UUID uniqueId, int value, @NotNull String fancyValue) {
      if (this.proxy != null) {
         this.proxy.sendMessage(new PlayerListObjectiveProxyPlayerData(this, this.proxy.getIdCounter().incrementAndGet(), uniqueId, value, fancyValue));
      }
   }

   @Override
   public void onProxyLoadRequest() {
      for (TabPlayer all : this.onlinePlayers.getPlayers()) {
         this.sendProxyMessage(all);
      }
   }

   @Override
   public void onJoin(@NotNull ProxyPlayer player) {
      this.updatePlayer(player);
   }

   public void updatePlayer(@NotNull ProxyPlayer player) {
      if (player.getPlayerlist() != null) {
         for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
            if (!viewer.playerlistObjectiveData.disabled.get()) {
               viewer.getScoreboard()
                  .setScore(
                     "TAB-PlayerList", player.getNickname(), player.getPlayerlist().getValue(), null, this.cache.get(player.getPlayerlist().getFancyValue())
                  );
            }
         }
      }
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return "Playerlist Objective";
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

   @Generated
   public PlayerListObjectiveConfiguration getConfiguration() {
      return this.configuration;
   }

   @Generated
   public PlayerListObjectiveTitleRefresher getTitleRefresher() {
      return this.titleRefresher;
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
