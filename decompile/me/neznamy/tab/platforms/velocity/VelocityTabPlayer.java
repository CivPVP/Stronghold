package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.Player;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

public class VelocityTabPlayer extends ProxyTabPlayer {
   public VelocityTabPlayer(@NotNull VelocityPlatform platform, @NotNull Player p) {
      super(
         platform,
         p,
         p.getUniqueId(),
         p.getUsername(),
         p.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("null"),
         p.getProtocolVersion().getProtocol()
      );
   }

   @Override
   public boolean hasPermission0(@NotNull String permission) {
      return this.getPlayer().hasPermission(permission);
   }

   @Override
   public int getPing() {
      return (int)this.getPlayer().getPing();
   }

   @Override
   public void sendMessage(@NotNull TabComponent message) {
      this.getPlayer().sendMessage(message.toAdventure());
   }

   @NotNull
   public Player getPlayer() {
      return (Player)this.player;
   }

   public VelocityPlatform getPlatform() {
      return (VelocityPlatform)this.platform;
   }

   @Override
   public void sendPluginMessage(byte[] message) {
      try {
         this.getPlayer().getCurrentServer().ifPresent(currentServer -> currentServer.sendPluginMessage(this.getPlatform().getMCI(), message));
      } catch (IllegalStateException var3) {
      }
   }
}
