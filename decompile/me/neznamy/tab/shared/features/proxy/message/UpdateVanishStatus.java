package me.neznamy.tab.shared.features.proxy.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import java.util.UUID;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.QueuedData;
import org.jetbrains.annotations.NotNull;

public class UpdateVanishStatus extends ProxyMessage {
   @NotNull
   private final UUID playerId;
   private final boolean vanished;

   public UpdateVanishStatus(@NotNull ByteArrayDataInput in) {
      this.playerId = this.readUUID(in);
      this.vanished = in.readBoolean();
   }

   @Override
   public void write(@NotNull ByteArrayDataOutput out) {
      this.writeUUID(out, this.playerId);
      out.writeBoolean(this.vanished);
   }

   @Override
   public void process(@NotNull ProxySupport proxySupport) {
      ProxyPlayer target = proxySupport.getProxyPlayers().get(this.playerId);
      if (target == null) {
         this.unknownPlayer(this.playerId.toString(), "vanish status update");
         QueuedData data = proxySupport.getQueuedData().computeIfAbsent(this.playerId, k -> new QueuedData());
         data.setVanished(this.vanished);
      } else {
         target.setVanished(this.vanished);
         TAB.getInstance().getFeatureManager().onVanishStatusChange(target);
      }
   }

   @Generated
   public UpdateVanishStatus(@NotNull UUID playerId, boolean vanished) {
      if (playerId == null) {
         throw new NullPointerException("playerId is marked non-null but is null");
      }

      this.playerId = playerId;
      this.vanished = vanished;
   }

   @Generated
   @Override
   public String toString() {
      return "UpdateVanishStatus(playerId=" + this.playerId + ", vanished=" + this.vanished + ")";
   }
}
