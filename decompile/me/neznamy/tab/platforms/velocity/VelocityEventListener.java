package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent.ForwardResult;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.SafeBossBar;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

public class VelocityEventListener implements EventListener<Player> {
   private static final boolean BOSSBAR_BUG_COMPENSATION = !ReflectionUtils.classExists("com.velocitypowered.proxy.connection.player.bossbar.BossBarManager");
   private final Map<Player, UUID> players = new ConcurrentHashMap<>();

   @Subscribe
   public void onQuit(@NotNull DisconnectEvent e) {
      if (!TAB.getInstance().isPluginDisabled()) {
         UUID id = this.players.remove(e.getPlayer());
         if (id != null) {
            this.quit(id);
         }
      }
   }

   @Subscribe
   public void preConnect(@NotNull ServerPreConnectEvent e) {
      if (BOSSBAR_BUG_COMPENSATION) {
         if (!TAB.getInstance().isPluginDisabled()) {
            if (e.getResult().isAllowed()) {
               TabPlayer p = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
               if (p != null && p.getVersionId() >= ProtocolVersion.V1_20_2.getNetworkId()) {
                  ((SafeBossBar)p.getBossBar()).freeze();
               }
            }
         }
      }
   }

   @Subscribe
   public void onConnect(@NotNull ServerPostConnectEvent e) {
      TAB tab = TAB.getInstance();
      if (!tab.isPluginDisabled()) {
         tab.getCPUManager()
            .runTask(
               () -> {
                  TabPlayer player = tab.getPlayer(e.getPlayer().getUniqueId());
                  if (player == null) {
                     this.players.put(e.getPlayer(), e.getPlayer().getUniqueId());
                     tab.getFeatureManager().onJoin(this.createPlayer(e.getPlayer()));
                  } else {
                     if (!(player.getScoreboard() instanceof VelocityScoreboard)) {
                        player.getScoreboard().resend();
                     }

                     tab.getFeatureManager()
                        .onServerChange(
                           player.getUniqueId(), Server.byName(e.getPlayer().getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("null"))
                        );
                     tab.getFeatureManager().onTabListClear(player);
                     if (BOSSBAR_BUG_COMPENSATION && player.getVersionId() >= ProtocolVersion.V1_20_2.getNetworkId()) {
                        ((SafeBossBar)player.getBossBar()).unfreezeAndResend();
                     }
                  }
               }
            );
      }
   }

   @Subscribe
   public void onPluginMessageEvent(@NotNull PluginMessageEvent e) {
      if (e.getIdentifier().getId().equals("tab:bridge-6")) {
         e.setResult(ForwardResult.handled());
         if (!TAB.getInstance().isPluginDisabled()) {
            if (e.getTarget() instanceof Player) {
               this.pluginMessage(((Player)e.getTarget()).getUniqueId(), e.getData());
            }
         }
      }
   }

   @NotNull
   public TabPlayer createPlayer(@NotNull Player player) {
      return new VelocityTabPlayer((VelocityPlatform)TAB.getInstance().getPlatform(), player);
   }
}
