package me.neznamy.tab.shared.features.playerlistobjective;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import java.util.UUID;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.QueuedData;
import me.neznamy.tab.shared.features.proxy.message.ProxyMessage;
import org.jetbrains.annotations.NotNull;

public class PlayerListObjectiveProxyPlayerData extends ProxyMessage {
   @NotNull
   private final YellowNumber feature;
   private final long id;
   @NotNull
   private final UUID playerId;
   private final int value;
   @NotNull
   private final String fancyValue;

   public PlayerListObjectiveProxyPlayerData(@NotNull YellowNumber feature, @NotNull ByteArrayDataInput in) {
      this.feature = feature;
      this.id = in.readLong();
      this.playerId = this.readUUID(in);
      this.value = in.readInt();
      this.fancyValue = in.readUTF();
   }

   @NotNull
   @Override
   public ThreadExecutor getCustomThread() {
      return this.feature.getCustomThread();
   }

   @Override
   public void write(@NotNull ByteArrayDataOutput out) {
      out.writeLong(this.id);
      this.writeUUID(out, this.playerId);
      out.writeInt(this.value);
      out.writeUTF(this.fancyValue);
   }

   @Override
   public void process(@NotNull ProxySupport proxySupport) {
      ProxyPlayer target = proxySupport.getProxyPlayers().get(this.playerId);
      if (target != null) {
         if (target.getPlayerlist() != null && target.getPlayerlist().id > this.id) {
            TAB.getInstance()
               .debug("Dropping playerlist objective update action for player " + target.getName() + " due to newer action already being present");
         } else {
            target.setPlayerlist(this);
            if (target.getConnectionState() == ProxyPlayer.ConnectionState.CONNECTED) {
               this.feature.updatePlayer(target);
            }
         }
      } else {
         this.unknownPlayer(this.playerId.toString(), "playerlist objective update");
         QueuedData data = proxySupport.getQueuedData().computeIfAbsent(this.playerId, k -> new QueuedData());
         if (data.getPlayerlist() == null || data.getPlayerlist().id < this.id) {
            data.setPlayerlist(this);
         }
      }
   }

   @Generated
   public PlayerListObjectiveProxyPlayerData(@NotNull YellowNumber feature, long id, @NotNull UUID playerId, int value, @NotNull String fancyValue) {
      if (feature == null) {
         throw new NullPointerException("feature is marked non-null but is null");
      }

      if (playerId == null) {
         throw new NullPointerException("playerId is marked non-null but is null");
      }

      if (fancyValue == null) {
         throw new NullPointerException("fancyValue is marked non-null but is null");
      }

      this.feature = feature;
      this.id = id;
      this.playerId = playerId;
      this.value = value;
      this.fancyValue = fancyValue;
   }

   @Generated
   @Override
   public String toString() {
      return "PlayerListObjectiveProxyPlayerData(id="
         + this.getId()
         + ", playerId="
         + this.getPlayerId()
         + ", value="
         + this.getValue()
         + ", fancyValue="
         + this.getFancyValue()
         + ")";
   }

   @NotNull
   @Generated
   public YellowNumber getFeature() {
      return this.feature;
   }

   @Generated
   public long getId() {
      return this.id;
   }

   @NotNull
   @Generated
   public UUID getPlayerId() {
      return this.playerId;
   }

   @Generated
   public int getValue() {
      return this.value;
   }

   @NotNull
   @Generated
   public String getFancyValue() {
      return this.fancyValue;
   }
}
