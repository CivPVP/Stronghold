package me.neznamy.tab.shared.features.proxy.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import java.util.UUID;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.QueuedData;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerJoin extends ProxyMessage {
   @NotNull
   private final UUID uniqueId;
   @NotNull
   private final UUID tablistId;
   @NotNull
   private final String name;
   @NotNull
   private final Server server;
   private final boolean vanished;
   private final boolean staff;
   @Nullable
   private final TabList.Skin skin;

   public PlayerJoin(@NotNull TabPlayer encodedPlayer) {
      this.uniqueId = encodedPlayer.getUniqueId();
      this.tablistId = encodedPlayer.getTablistId();
      this.name = encodedPlayer.getName();
      this.server = encodedPlayer.server;
      this.vanished = encodedPlayer.isVanished();
      this.staff = encodedPlayer.hasPermission("tab.staff");
      this.skin = encodedPlayer.getTabList().getSkin();
   }

   public PlayerJoin(@NotNull ByteArrayDataInput in) {
      this.uniqueId = this.readUUID(in);
      this.tablistId = this.readUUID(in);
      this.name = in.readUTF();
      this.server = Server.byName(in.readUTF());
      this.vanished = in.readBoolean();
      this.staff = in.readBoolean();
      this.skin = this.readSkin(in);
   }

   @Override
   public void write(@NotNull ByteArrayDataOutput out) {
      this.writeUUID(out, this.uniqueId);
      this.writeUUID(out, this.tablistId);
      out.writeUTF(this.name);
      out.writeUTF(this.server.getName());
      out.writeBoolean(this.vanished);
      out.writeBoolean(this.staff);
      this.writeSkin(out, this.skin);
   }

   @Override
   public void process(@NotNull ProxySupport proxySupport) {
      ProxyPlayer decodedPlayer = new ProxyPlayer(this.uniqueId, this.tablistId, this.name, this.server, this.vanished, this.staff, this.skin);
      if (proxySupport.getProxyPlayers().containsKey(decodedPlayer.getUniqueId())) {
         TAB.getInstance().debug("[Proxy Support] The proxy player " + decodedPlayer.getName() + " is already connected, cannot process join.");
      } else {
         proxySupport.getProxyPlayers().put(decodedPlayer.getUniqueId(), decodedPlayer);
         QueuedData data = proxySupport.getQueuedData().remove(decodedPlayer.getUniqueId());
         if (data != null) {
            decodedPlayer.setBelowname(data.getBelowname());
            decodedPlayer.setTabFormat(data.getTabFormat());
            decodedPlayer.setNametag(data.getNametag());
            decodedPlayer.setPlayerlist(data.getPlayerlist());
            decodedPlayer.setVanished(data.isVanished());
         }

         if (TAB.getInstance().getPlayer(decodedPlayer.getUniqueId()) == null) {
            TAB.getInstance().getFeatureManager().onJoin(decodedPlayer);
         }
      }
   }

   @Generated
   @Override
   public String toString() {
      return "PlayerJoin(uniqueId="
         + this.uniqueId
         + ", tablistId="
         + this.tablistId
         + ", name="
         + this.name
         + ", server="
         + this.server
         + ", vanished="
         + this.vanished
         + ", staff="
         + this.staff
         + ", skin="
         + this.skin
         + ")";
   }
}
