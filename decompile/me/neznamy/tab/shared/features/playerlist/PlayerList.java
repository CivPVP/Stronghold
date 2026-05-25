package me.neznamy.tab.shared.features.playerlist;

import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.api.tablist.TabListFormatManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.features.layout.PlayerSlot;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.types.DisableChecker;
import me.neznamy.tab.shared.features.types.GroupListener;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.ProxyFeature;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.features.types.ServerSwitchListener;
import me.neznamy.tab.shared.features.types.UnLoadable;
import me.neznamy.tab.shared.features.types.VanishListener;
import me.neznamy.tab.shared.features.types.WorldSwitchListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerList
   extends RefreshableFeature
   implements TabListFormatManager,
   JoinListener,
   Loadable,
   UnLoadable,
   WorldSwitchListener,
   ServerSwitchListener,
   VanishListener,
   ProxyFeature,
   GroupListener {
   @NotNull
   private final StringToComponentCache cache = new StringToComponentCache("Tablist name formatting", 1000);
   @NotNull
   private final TablistFormattingConfiguration configuration;
   @Nullable
   private final ProxySupport proxy = TAB.getInstance().getFeatureManager().getFeature("ProxySupport");
   @NotNull
   private final DisableChecker disableChecker;

   public PlayerList(@NotNull TablistFormattingConfiguration configuration) {
      this.configuration = configuration;
      this.disableChecker = new DisableChecker(
         this,
         TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(configuration.getDisableCondition()),
         this::onDisableConditionChange,
         p -> p.tablistData.disabled
      );
      TAB.getInstance().getFeatureManager().registerFeature("PlayerList-Condition", this.disableChecker);
      TAB.getInstance().getCpu().getTablistEntryCheckThread().repeatTask(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            ((TrackedTabList)p.getTabList()).checkDisplayNames();
         }
      }, this.getFeatureName(), "Tablist anti override (periodic task)"), 500);
      if (this.proxy != null) {
         this.proxy.registerMessage(PlayerListProxyPlayerData.class, in -> new PlayerListProxyPlayerData(in, this));
      }
   }

   private void updateDisplayName(@NotNull TabPlayer viewer, @NotNull TabPlayer target, @Nullable TabComponent displayName) {
      if (viewer.layoutData.currentLayout != null) {
         PlayerSlot slot = viewer.layoutData.currentLayout.view.getSlot(target);
         if (slot != null) {
            viewer.getTabList().updateDisplayName(slot.getUniqueId(), displayName);
            return;
         }
      }

      viewer.getTabList().updateDisplayName(target, displayName);
   }

   public void loadProperties(@NotNull TabPlayer player) {
      player.tablistData.prefix = player.loadPropertyFromConfig(this, "tabprefix", "");
      player.tablistData.name = player.loadPropertyFromConfig(this, "customtabname", player.getName());
      player.tablistData.suffix = player.loadPropertyFromConfig(this, "tabsuffix", "");
   }

   public boolean updateProperties(@NotNull TabPlayer p) {
      boolean changed = p.updatePropertyFromConfig(p.tablistData.prefix, "");
      if (p.updatePropertyFromConfig(p.tablistData.name, p.getName())) {
         changed = true;
      }

      if (p.updatePropertyFromConfig(p.tablistData.suffix, "")) {
         changed = true;
      }

      return changed;
   }

   public void formatPlayerForEveryone(@NotNull TabPlayer player, boolean format) {
      if (!player.tablistData.disabled.get()) {
         for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.server.canSee(player.server) && viewer.canSee(player)) {
               this.updateDisplayName(viewer, player, format ? this.getTabFormat(player, viewer) : null);
            }
         }

         this.sendProxyMessage(player);
      }
   }

   @Nullable
   public TabComponent getTabFormat(@NotNull TabPlayer p, @NotNull TabPlayer viewer) {
      Property prefix = p.tablistData.prefix;
      Property name = p.tablistData.name;
      Property suffix = p.tablistData.suffix;
      return prefix != null && name != null && suffix != null
         ? this.cache.get(prefix.getFormat(viewer) + name.getFormat(viewer) + suffix.getFormat(viewer))
         : null;
   }

   @Override
   public void load() {
      for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
         this.loadProperties(all);
         if (this.disableChecker.isDisableConditionMet(all)) {
            all.tablistData.disabled.set(true);
         }
      }

      for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
         this.formatPlayerForEveryone(target, true);
      }
   }

   @Override
   public void unload() {
      for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
         this.formatPlayerForEveryone(target, false);
      }
   }

   @Override
   public void onServerChange(@NotNull TabPlayer p, @NotNull Server from, @NotNull Server to) {
      this.updateProperties(p);
      this.formatPlayerForEveryone(p, true);
      if (!TAB.getInstance().getFeatureManager().isFeatureEnabled("injection")) {
         TAB.getInstance().getCpu().getProcessingThread().executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
               if (!all.tablistData.disabled.get() && p.server.canSee(all.server) && p.canSee(all)) {
                  this.updateDisplayName(p, all, this.getTabFormat(all, p));
               }

               if (all != p && !p.tablistData.disabled.get() && all.server.canSee(p.server) && all.canSee(p)) {
                  this.updateDisplayName(all, p, this.getTabFormat(p, all));
               }
            }

            if (this.proxy != null) {
               for (ProxyPlayer proxied : this.proxy.getProxyPlayers().values()) {
                  if (proxied.getTabFormat() != null) {
                     p.getTabList().updateDisplayName(proxied.getTablistId(), proxied.getTabFormat().getFormatComponent());
                  }
               }
            }
         }, this.getFeatureName(), "Player Join"), 300);
      }
   }

   @Override
   public void onWorldChange(@NotNull TabPlayer changed, @NotNull World from, @NotNull World to) {
      if (this.updateProperties(changed)) {
         this.formatPlayerForEveryone(changed, true);
      }
   }

   public void onDisableConditionChange(TabPlayer p, boolean disabledNow) {
      if (disabledNow) {
         for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.server.canSee(p.server) && viewer.canSee(p)) {
               this.updateDisplayName(viewer, p, null);
            }
         }

         this.sendProxyMessage(p);
      } else {
         this.formatPlayerForEveryone(p, true);
      }
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Updating TabList format";
   }

   @Override
   public void refresh(@NotNull TabPlayer refreshed, boolean force) {
      if (refreshed.tablistData.prefix != null) {
         boolean refresh;
         if (force) {
            this.updateProperties(refreshed);
            refresh = true;
         } else {
            boolean prefix = refreshed.tablistData.prefix.update();
            boolean name = refreshed.tablistData.name.update();
            boolean suffix = refreshed.tablistData.suffix.update();
            refresh = prefix || name || suffix;
         }

         if (refresh) {
            this.formatPlayerForEveryone(refreshed, true);
         }
      }
   }

   @Override
   public void onGroupChange(@NotNull TabPlayer player) {
      if (this.updateProperties(player)) {
         this.formatPlayerForEveryone(player, true);
      }
   }

   @Override
   public void onJoin(@NotNull TabPlayer connectedPlayer) {
      this.loadProperties(connectedPlayer);
      if (this.disableChecker.isDisableConditionMet(connectedPlayer)) {
         connectedPlayer.tablistData.disabled.set(true);
      } else {
         this.formatPlayerForEveryone(connectedPlayer, true);
      }

      Runnable r = () -> {
         for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all != connectedPlayer && !all.tablistData.disabled.get() && connectedPlayer.server.canSee(all.server) && connectedPlayer.canSee(all)) {
               this.updateDisplayName(connectedPlayer, all, this.getTabFormat(all, connectedPlayer));
            }
         }

         if (this.proxy != null) {
            for (ProxyPlayer proxied : this.proxy.getProxyPlayers().values()) {
               if (proxied.getTabFormat() != null) {
                  connectedPlayer.getTabList().updateDisplayName(proxied.getTablistId(), proxied.getTabFormat().getFormatComponent());
               }
            }
         }
      };
      if (!TAB.getInstance().getFeatureManager().isFeatureEnabled("injection")) {
         TAB.getInstance()
            .getCpu()
            .getProcessingThread()
            .executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(), r, this.getFeatureName(), "Player Join"), 300);
      } else {
         r.run();
      }
   }

   @Override
   public void onVanishStatusChange(@NotNull TabPlayer player) {
      if (!player.isVanished()) {
         this.formatPlayerForEveryone(player, true);
      }
   }

   @Override
   public void setPrefix(@NonNull TabPlayer player, @Nullable String prefix) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      ((TabPlayer)player).ensureLoaded();
      ((TabPlayer)player).tablistData.prefix.setTemporaryValue(prefix);
      this.formatPlayerForEveryone((TabPlayer)player, true);
   }

   @Override
   public void setName(@NonNull TabPlayer player, @Nullable String customName) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      ((TabPlayer)player).ensureLoaded();
      ((TabPlayer)player).tablistData.name.setTemporaryValue(customName);
      this.formatPlayerForEveryone((TabPlayer)player, true);
   }

   @Override
   public void setSuffix(@NonNull TabPlayer player, @Nullable String suffix) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      ((TabPlayer)player).ensureLoaded();
      ((TabPlayer)player).tablistData.suffix.setTemporaryValue(suffix);
      this.formatPlayerForEveryone((TabPlayer)player, true);
   }

   @Override
   public String getCustomPrefix(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      ((TabPlayer)player).ensureLoaded();
      return ((TabPlayer)player).tablistData.prefix.getTemporaryValue();
   }

   @Override
   public String getCustomName(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      ((TabPlayer)player).ensureLoaded();
      return ((TabPlayer)player).tablistData.name.getTemporaryValue();
   }

   @Override
   public String getCustomSuffix(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      ((TabPlayer)player).ensureLoaded();
      return ((TabPlayer)player).tablistData.suffix.getTemporaryValue();
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
   public String getOriginalName(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      } else {
         return this.getOriginalRawName(player);
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

   @NotNull
   @Override
   public String getOriginalRawPrefix(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      ((TabPlayer)player).ensureLoaded();
      return ((TabPlayer)player).tablistData.prefix.getOriginalRawValue();
   }

   @NotNull
   @Override
   public String getOriginalRawName(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      ((TabPlayer)player).ensureLoaded();
      return ((TabPlayer)player).tablistData.name.getOriginalRawValue();
   }

   @NotNull
   @Override
   public String getOriginalRawSuffix(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      ((TabPlayer)player).ensureLoaded();
      return ((TabPlayer)player).tablistData.suffix.getOriginalRawValue();
   }

   @NotNull
   @Override
   public String getOriginalReplacedPrefix(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      ((TabPlayer)player).ensureLoaded();
      return ((TabPlayer)player).tablistData.prefix.getOriginalReplacedValue();
   }

   @NotNull
   @Override
   public String getOriginalReplacedName(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      ((TabPlayer)player).ensureLoaded();
      return ((TabPlayer)player).tablistData.name.getOriginalReplacedValue();
   }

   @NotNull
   @Override
   public String getOriginalReplacedSuffix(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      ((TabPlayer)player).ensureLoaded();
      return ((TabPlayer)player).tablistData.suffix.getOriginalReplacedValue();
   }

   private void sendProxyMessage(@NotNull TabPlayer player) {
      if (this.proxy != null) {
         this.proxy
            .sendMessage(
               new PlayerListProxyPlayerData(
                  this,
                  this.proxy.getIdCounter().incrementAndGet(),
                  player.getUniqueId(),
                  player.getName(),
                  player.tablistData.prefix.get() + player.tablistData.name.get() + player.tablistData.suffix.get(),
                  TabComponent.empty()
               )
            );
      }
   }

   @Override
   public void onProxyLoadRequest() {
      for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
         this.sendProxyMessage(all);
      }
   }

   @Override
   public void onVanishStatusChange(@NotNull ProxyPlayer player) {
      this.formatPlayerForEveryone(player);
   }

   @Override
   public void onJoin(@NotNull ProxyPlayer player) {
      this.formatPlayerForEveryone(player);
   }

   public void formatPlayerForEveryone(@NotNull ProxyPlayer player) {
      if (!player.isVanished()) {
         if (player.getTabFormat() != null) {
            for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
               viewer.getTabList().updateDisplayName(player.getTablistId(), player.getTabFormat().getFormatComponent());
            }
         }
      }
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return "Tablist name formatting";
   }

   @NotNull
   @Generated
   public StringToComponentCache getCache() {
      return this.cache;
   }

   @NotNull
   @Generated
   public TablistFormattingConfiguration getConfiguration() {
      return this.configuration;
   }

   @Nullable
   @Generated
   public ProxySupport getProxy() {
      return this.proxy;
   }

   @NotNull
   @Generated
   public DisableChecker getDisableChecker() {
      return this.disableChecker;
   }
}
