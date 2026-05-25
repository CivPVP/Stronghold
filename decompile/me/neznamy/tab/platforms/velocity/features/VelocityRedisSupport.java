package me.neznamy.tab.platforms.velocity.features;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import com.velocitypowered.api.event.Subscribe;
import lombok.Generated;
import me.neznamy.tab.platforms.velocity.VelocityTAB;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import org.jetbrains.annotations.NotNull;

public class VelocityRedisSupport extends ProxySupport {
   @NotNull
   private final VelocityTAB plugin;

   @Subscribe
   public void onMessage(PubSubMessageEvent e) {
      if (e.getChannel().equals("TAB-2")) {
         this.processMessage(e.getMessage());
      }
   }

   @Override
   public void register() {
      this.plugin.getServer().getEventManager().register(this.plugin, this);

      try {
         RedisBungeeAPI.getRedisBungeeApi().registerPubSubChannels(new String[]{"TAB-2"});
      } catch (NullPointerException e) {
         TAB.getInstance().getErrorManager().redisBungeeRegisterFail(e);
      }
   }

   @Override
   public void unregister() {
      this.plugin.getServer().getEventManager().unregisterListener(this.plugin, this);
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
   public VelocityRedisSupport(@NotNull VelocityTAB plugin) {
      if (plugin == null) {
         throw new NullPointerException("plugin is marked non-null but is null");
      }

      this.plugin = plugin;
   }
}
