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
import org.jetbrains.annotations.NotNull;

public class ServerSwitch extends ProxyMessage {
   @NotNull
   private final UUID playerId;
   @NotNull
   private final Server newServer;

   public ServerSwitch(@NotNull ByteArrayDataInput in) {
      this.playerId = this.readUUID(in);
      this.newServer = Server.byName(in.readUTF());
   }

   @Override
   public void write(@NotNull ByteArrayDataOutput out) {
      this.writeUUID(out, this.playerId);
      out.writeUTF(this.newServer.getName());
   }

   @Override
   public void process(@NotNull ProxySupport proxySupport) {
      ProxyPlayer target = proxySupport.getProxyPlayers().get(this.playerId);
      if (target == null) {
         this.unknownPlayer(this.playerId.toString(), "server switch");
         QueuedData data = proxySupport.getQueuedData().computeIfAbsent(this.playerId, k -> new QueuedData());
         data.setServer(this.newServer);
      } else {
         target.setServer(this.newServer);
         TAB.getInstance().getFeatureManager().onServerSwitch(target);
      }
   }

   @Generated
   public ServerSwitch(@NotNull UUID playerId, @NotNull Server newServer) {
      if (playerId == null) {
         throw new NullPointerException("playerId is marked non-null but is null");
      }

      if (newServer == null) {
         throw new NullPointerException("newServer is marked non-null but is null");
      }

      this.playerId = playerId;
      this.newServer = newServer;
   }

   @Generated
   @Override
   public String toString() {
      return "ServerSwitch(playerId=" + this.playerId + ", newServer=" + this.newServer + ")";
   }
}
