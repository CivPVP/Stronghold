package me.neznamy.tab.shared.features.proxy.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import java.util.UUID;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import org.jetbrains.annotations.NotNull;

public class PlayerQuit extends ProxyMessage {
   @NotNull
   private final UUID playerId;

   public PlayerQuit(@NotNull ByteArrayDataInput in) {
      this.playerId = this.readUUID(in);
   }

   @Override
   public void write(@NotNull ByteArrayDataOutput out) {
      this.writeUUID(out, this.playerId);
   }

   @Override
   public void process(@NotNull ProxySupport proxySupport) {
      ProxyPlayer target = proxySupport.getProxyPlayers().get(this.playerId);
      if (target == null) {
         this.unknownPlayer(this.playerId.toString(), "disconnect");
         proxySupport.getQueuedData().remove(this.playerId);
      } else {
         TAB.getInstance().getFeatureManager().onQuit(target);
         proxySupport.getProxyPlayers().remove(target.getUniqueId());
      }
   }

   @Generated
   public PlayerQuit(@NotNull UUID playerId) {
      if (playerId == null) {
         throw new NullPointerException("playerId is marked non-null but is null");
      }

      this.playerId = playerId;
   }

   @Generated
   @Override
   public String toString() {
      return "PlayerQuit(playerId=" + this.playerId + ")";
   }
}
