package me.neznamy.tab.platforms.bungeecord.features;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import lombok.Generated;
import me.neznamy.tab.platforms.bungeecord.BungeeTAB;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class BungeeRedisSupport extends ProxySupport implements Listener {
   @NotNull
   private final BungeeTAB plugin;

   @EventHandler
   public void onMessage(@NotNull PubSubMessageEvent e) {
      if (e.getChannel().equals("TAB-2")) {
         this.processMessage(e.getMessage());
      }
   }

   @Override
   public void register() {
      ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, this);

      try {
         RedisBungeeAPI.getRedisBungeeApi().registerPubSubChannels(new String[]{"TAB-2"});
      } catch (NullPointerException e) {
         TAB.getInstance().getErrorManager().redisBungeeRegisterFail(e);
      }
   }

   @Override
   public void unregister() {
      ProxyServer.getInstance().getPluginManager().unregisterListener(this);
      RedisBungeeAPI.getRedisBungeeApi().unregisterPubSubChannels(new String[]{"TAB-2"});
   }

   @Override
   public void sendMessage(@NotNull String message) {
      try {
         RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage("TAB-2", message);
      } catch (Exception e) {
         TAB.getInstance().getErrorManager().redisBungeeMessageSendFail(e);
      }
   }

   @Generated
   public BungeeRedisSupport(@NotNull BungeeTAB plugin) {
      if (plugin == null) {
         throw new NullPointerException("plugin is marked non-null but is null");
      }

      this.plugin = plugin;
   }
}
