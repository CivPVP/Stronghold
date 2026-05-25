package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.protocol.DefinedPacket;
import org.jetbrains.annotations.NotNull;

public class BungeeTabPlayer extends ProxyTabPlayer {
   public BungeeTabPlayer(@NotNull BungeePlatform platform, @NotNull ProxiedPlayer p) {
      super(platform, p, p.getUniqueId(), p.getName(), p.getServer() != null ? p.getServer().getInfo().getName() : "-", p.getPendingConnection().getVersion());
   }

   @Override
   public boolean hasPermission0(@NotNull String permission) {
      return this.getPlayer().hasPermission(permission);
   }

   @Override
   public int getPing() {
      return this.getPlayer().getPing();
   }

   @Override
   public void sendMessage(@NotNull TabComponent message) {
      this.getPlayer().sendMessage(this.getPlatform().transformComponent(message, this.getVersion()));
   }

   @NotNull
   public ProxiedPlayer getPlayer() {
      return (ProxiedPlayer)this.player;
   }

   public BungeePlatform getPlatform() {
      return (BungeePlatform)this.platform;
   }

   @Override
   public void sendPluginMessage(byte[] message) {
      Server server = this.getPlayer().getServer();
      if (server != null) {
         server.sendData("tab:bridge-6", message);
      }
   }

   public void sendPacket(@NotNull DefinedPacket packet) {
      ((UserConnection)this.getPlayer()).sendPacketQueued(packet);
   }
}
