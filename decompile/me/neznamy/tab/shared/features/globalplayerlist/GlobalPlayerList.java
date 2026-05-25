package me.neznamy.tab.shared.features.globalplayerlist;

import java.util.List;
import java.util.Map.Entry;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.features.playerlist.PlayerList;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.GameModeListener;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.ProxyFeature;
import me.neznamy.tab.shared.features.types.QuitListener;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.features.types.ServerSwitchListener;
import me.neznamy.tab.shared.features.types.TabListClearListener;
import me.neznamy.tab.shared.features.types.UnLoadable;
import me.neznamy.tab.shared.features.types.VanishListener;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.OnlinePlayers;
import me.neznamy.tab.shared.util.PerformanceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GlobalPlayerList
   extends RefreshableFeature
   implements JoinListener,
   QuitListener,
   VanishListener,
   GameModeListener,
   Loadable,
   UnLoadable,
   ServerSwitchListener,
   TabListClearListener,
   CustomThreaded,
   ProxyFeature {
   private final ThreadExecutor customThread = new ThreadExecutor("TAB Global PlayerList Thread");
   private OnlinePlayers onlinePlayers;
   @Nullable
   private final ProxySupport proxy = TAB.getInstance().getFeatureManager().getFeature("ProxySupport");
   @NotNull
   private final GlobalPlayerListConfiguration configuration;
   @Nullable
   private final PlayerList playerlist = TAB.getInstance().getFeatureManager().getFeature("PlayerList");

   public GlobalPlayerList(@NotNull GlobalPlayerListConfiguration configuration) {
      this.configuration = configuration;
      TAB.getInstance().getDataManager().applyConfiguration(configuration);

      for (Entry<String, List<String>> entry : configuration.getSharedServers().entrySet()) {
         TAB.getInstance()
            .getPlaceholderManager()
            .registerInternalServerPlaceholder(TabConstants.Placeholder.globalPlayerListGroup(entry.getKey()), 1000, () -> {
               if (this.onlinePlayers == null) {
                  return "0";
               }

               int count = 0;

               for (TabPlayer player : this.onlinePlayers.getPlayers()) {
                  if (entry.getValue().contains(player.server.getName()) && !player.isVanished()) {
                     count++;
                  }
               }

               if (this.proxy != null) {
                  for (ProxyPlayer player : this.proxy.getProxyPlayers().values()) {
                     if (entry.getValue().contains(player.server.getName()) && !player.isVanished()) {
                        count++;
                     }
                  }
               }

               return PerformanceUtil.toString(count);
            });
      }
   }

   @Override
   public void load() {
      this.onlinePlayers = new OnlinePlayers(TAB.getInstance().getOnlinePlayers());
      if (this.configuration.isUpdateLatency()) {
         this.addUsedPlaceholder("%ping%");
      }

      for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
         for (TabPlayer displayed : this.onlinePlayers.getPlayers()) {
            if (viewer.server != displayed.server && this.shouldSee(viewer, displayed)) {
               viewer.getTabList().addEntry(this.getAddInfoData(displayed, viewer));
            }
         }
      }
   }

   public boolean shouldSee(@NotNull TabPlayer viewer, @NotNull TabPlayer displayed) {
      return viewer.server.canSee(displayed.server) && viewer.canSee(displayed);
   }

   @Override
   public void unload() {
      for (TabPlayer displayed : this.onlinePlayers.getPlayers()) {
         for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
            if (displayed.server != viewer.server) {
               viewer.getTabList().removeEntry(displayed.getTablistId());
            }
         }
      }
   }

   @Override
   public void onJoin(@NotNull TabPlayer connectedPlayer) {
      this.onlinePlayers.addPlayer(connectedPlayer);

      for (TabPlayer all : this.onlinePlayers.getPlayers()) {
         if (connectedPlayer.server != all.server) {
            if (this.shouldSee(all, connectedPlayer)) {
               all.getTabList().addEntry(this.getAddInfoData(connectedPlayer, all));
            }

            if (this.shouldSee(connectedPlayer, all)) {
               connectedPlayer.getTabList().addEntry(this.getAddInfoData(all, connectedPlayer));
            }
         }
      }

      if (this.proxy != null) {
         for (ProxyPlayer proxied : this.proxy.getProxyPlayers().values()) {
            if (proxied.server != connectedPlayer.server && this.shouldSee(connectedPlayer, proxied)) {
               connectedPlayer.getTabList().addEntry(proxied.asEntry());
            }
         }
      }
   }

   @Override
   public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
      this.onlinePlayers.removePlayer(disconnectedPlayer);

      for (TabPlayer all : this.onlinePlayers.getPlayers()) {
         all.getTabList().removeEntry(disconnectedPlayer.getTablistId());
      }
   }

   @Override
   public void onServerChange(@NotNull TabPlayer changed, @NotNull Server from, @NotNull Server to) {
      this.customThread.executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         if (changed.isOnline()) {
            for (TabPlayer all : this.onlinePlayers.getPlayers()) {
               if (all.server != changed.server) {
                  all.getTabList().removeEntry(changed.getTablistId());
                  if (this.shouldSee(all, changed)) {
                     all.getTabList().addEntry(this.getAddInfoData(changed, all));
                  }
               }
            }
         }
      }, this.getFeatureName(), "Server Switch"), 200);
   }

   @Override
   public void onTabListClear(@NotNull TabPlayer player) {
      for (TabPlayer all : this.onlinePlayers.getPlayers()) {
         if (all.server != player.server && this.shouldSee(player, all)) {
            player.getTabList().addEntry(this.getAddInfoData(all, player));
         }
      }

      if (this.proxy != null) {
         for (ProxyPlayer proxied : this.proxy.getProxyPlayers().values()) {
            if (proxied.server != player.server && this.shouldSee(player, proxied)) {
               player.getTabList().addEntry(proxied.asEntry());
            }
         }
      }
   }

   @NotNull
   public TabList.Entry getAddInfoData(@NotNull TabPlayer p, @NotNull TabPlayer viewer) {
      TabComponent format = null;
      if (this.playerlist != null && !p.tablistData.disabled.get()) {
         format = this.playerlist.getTabFormat(p, viewer);
      }

      return new TabList.Entry(
         p.getTablistId(),
         p.getNickname(),
         p.getTabList().getSkin(),
         true,
         this.configuration.isUpdateLatency() ? p.getPing() : 0,
         !this.configuration.isOthersAsSpectators() && (!this.configuration.isVanishedAsSpectators() || !p.isVanished()) ? p.getGamemode() : 3,
         viewer.getVersion().getMinorVersion() >= 8 ? format : null,
         0,
         true
      );
   }

   @Override
   public void onGameModeChange(@NotNull TabPlayer player) {
      for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
         if (player.server != viewer.server) {
            viewer.getTabList().updateGameMode(player, this.configuration.isOthersAsSpectators() ? 3 : player.getGamemode());
         }
      }
   }

   @Override
   public void onVanishStatusChange(@NotNull TabPlayer p) {
      if (p.isVanished()) {
         for (TabPlayer all : this.onlinePlayers.getPlayers()) {
            if (all != p && !this.shouldSee(all, p)) {
               all.getTabList().removeEntry(p.getTablistId());
            }
         }
      } else {
         for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
            if (viewer != p && this.shouldSee(viewer, p)) {
               viewer.getTabList().addEntry(this.getAddInfoData(p, viewer));
            }
         }
      }
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Updating latency";
   }

   @Override
   public void refresh(@NotNull TabPlayer refreshed, boolean force) {
      for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
         if (refreshed.server != viewer.server && viewer.server.canSee(refreshed.server)) {
            viewer.getTabList().updateLatency(refreshed, refreshed.getPing());
         }
      }
   }

   private boolean shouldSee(@NotNull TabPlayer viewer, @NotNull ProxyPlayer target) {
      return TAB.getInstance().isPlayerConnected(target.getTablistId())
         ? false
         : viewer.server.canSee(target.server) && (!target.isVanished() || viewer.hasPermission("tab.seevanished"));
   }

   @Override
   public void onJoin(@NotNull ProxyPlayer player) {
      for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
         if (this.shouldSee(viewer, player) && viewer.server != player.server) {
            viewer.getTabList().addEntry(player.asEntry());
         }
      }
   }

   @Override
   public void onServerSwitch(@NotNull ProxyPlayer player) {
      for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
         if (viewer.server != player.server) {
            if (this.shouldSee(viewer, player)) {
               viewer.getTabList().addEntry(player.asEntry());
            } else {
               viewer.getTabList().removeEntry(player.getTablistId());
            }
         }
      }
   }

   @Override
   public void onQuit(@NotNull ProxyPlayer player) {
      TabPlayer connected = TAB.getInstance().getPlayer(player.getUniqueId());

      for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
         if (player.server != viewer.server && (connected == null || !this.shouldSee(viewer, connected))) {
            viewer.getTabList().removeEntry(player.getTablistId());
         }
      }
   }

   @Override
   public void onVanishStatusChange(@NotNull ProxyPlayer player) {
      if (player.isVanished()) {
         for (TabPlayer all : this.onlinePlayers.getPlayers()) {
            if (!this.shouldSee(all, player)) {
               all.getTabList().removeEntry(player.getTablistId());
            }
         }
      } else {
         for (TabPlayer viewer : this.onlinePlayers.getPlayers()) {
            if (this.shouldSee(viewer, player)) {
               viewer.getTabList().addEntry(player.asEntry());
            }
         }
      }
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return "Global PlayerList";
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
