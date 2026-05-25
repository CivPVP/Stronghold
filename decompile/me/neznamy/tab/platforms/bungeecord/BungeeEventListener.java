package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.SafeBossBar;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class BungeeEventListener implements EventListener<ProxiedPlayer>, Listener {
   @EventHandler
   public void onQuit(PlayerDisconnectEvent e) {
      this.quit(e.getPlayer().getUniqueId());
   }

   @EventHandler
   public void onSwitch(ServerSwitchEvent e) {
      TAB tab = TAB.getInstance();
      if (!tab.isPluginDisabled()) {
         TabPlayer p = tab.getPlayer(e.getPlayer().getUniqueId());
         if (p != null && p.getVersionId() >= ProtocolVersion.V1_20_2.getNetworkId()) {
            ((SafeScoreboard)p.getScoreboard()).setFrozen(true);
            ((SafeBossBar)p.getBossBar()).freeze();
         }

         tab.getCPUManager().runTask(() -> {
            TabPlayer player = tab.getPlayer(e.getPlayer().getUniqueId());
            if (player == null) {
               player = this.createPlayer(e.getPlayer());
               if (player.getVersionId() >= ProtocolVersion.V1_20_2.getNetworkId()) {
                  ((SafeScoreboard)player.getScoreboard()).setFrozen(true);
                  ((SafeBossBar)player.getBossBar()).freeze();
               }

               tab.getFeatureManager().onJoin(player);
            } else {
               tab.getFeatureManager().onServerChange(player.getUniqueId(), Server.byName(e.getPlayer().getServer().getInfo().getName()));
               if (player.getVersionId() < ProtocolVersion.V1_20_2.getNetworkId()) {
                  tab.getFeatureManager().onTabListClear(player);
               }
            }
         });
      }
   }

   @EventHandler
   public void onPluginMessage(PluginMessageEvent e) {
      if (e.getTag().equals("tab:bridge-6")) {
         e.setCancelled(true);
         if (!TAB.getInstance().isPluginDisabled()) {
            if (e.getReceiver() instanceof ProxiedPlayer) {
               this.pluginMessage(((ProxiedPlayer)e.getReceiver()).getUniqueId(), e.getData());
            }
         }
      }
   }

   @NotNull
   public TabPlayer createPlayer(@NotNull ProxiedPlayer player) {
      return new BungeeTabPlayer((BungeePlatform)TAB.getInstance().getPlatform(), player);
   }
}
