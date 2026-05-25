package me.neznamy.tab.shared.features.playerlist;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import java.util.UUID;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.QueuedData;
import me.neznamy.tab.shared.features.proxy.message.ProxyMessage;
import org.jetbrains.annotations.NotNull;

public class PlayerListProxyPlayerData extends ProxyMessage {
   @NotNull
   private final PlayerList feature;
   private final long id;
   @NotNull
   private final UUID playerId;
   @NotNull
   private final String player;
   @NotNull
   private final String format;
   @NotNull
   private final TabComponent formatComponent;

   public PlayerListProxyPlayerData(@NotNull ByteArrayDataInput in, @NotNull PlayerList feature) {
      this.feature = feature;
      this.id = in.readLong();
      this.playerId = this.readUUID(in);
      this.player = in.readUTF();
      this.format = in.readUTF();
      this.formatComponent = feature.getCache().get(this.format);
   }

   @Override
   public void write(@NotNull ByteArrayDataOutput out) {
      out.writeLong(this.id);
      this.writeUUID(out, this.playerId);
      out.writeUTF(this.player);
      out.writeUTF(this.format);
   }

   @Override
   public void process(@NotNull ProxySupport proxySupport) {
      ProxyPlayer target = proxySupport.getProxyPlayers().get(this.playerId);
      if (target != null) {
         if (target.getTabFormat() != null && target.getTabFormat().id > this.id) {
            TAB.getInstance().debug("Dropping tabformat update action for player " + target.getName() + " due to newer action already being present");
         } else {
            target.setTabFormat(this);
            if (target.getConnectionState() == ProxyPlayer.ConnectionState.CONNECTED) {
               this.feature.formatPlayerForEveryone(target);
            }
         }
      } else {
         this.unknownPlayer(this.playerId.toString(), "tablist format update");
         QueuedData data = proxySupport.getQueuedData().computeIfAbsent(this.playerId, k -> new QueuedData());
         if (data.getTabFormat() == null || data.getTabFormat().id < this.id) {
            data.setTabFormat(this);
         }
      }
   }

   @Generated
   public PlayerListProxyPlayerData(
      @NotNull PlayerList feature, long id, @NotNull UUID playerId, @NotNull String player, @NotNull String format, @NotNull TabComponent formatComponent
   ) {
      if (feature == null) {
         throw new NullPointerException("feature is marked non-null but is null");
      }

      if (playerId == null) {
         throw new NullPointerException("playerId is marked non-null but is null");
      }

      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (format == null) {
         throw new NullPointerException("format is marked non-null but is null");
      }

      if (formatComponent == null) {
         throw new NullPointerException("formatComponent is marked non-null but is null");
      }

      this.feature = feature;
      this.id = id;
      this.playerId = playerId;
      this.player = player;
      this.format = format;
      this.formatComponent = formatComponent;
   }

   @Generated
   @Override
   public String toString() {
      return "PlayerListProxyPlayerData(id="
         + this.getId()
         + ", playerId="
         + this.getPlayerId()
         + ", player="
         + this.getPlayer()
         + ", format="
         + this.getFormat()
         + ")";
   }

   @NotNull
   @Generated
   public PlayerList getFeature() {
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

   @NotNull
   @Generated
   public String getPlayer() {
      return this.player;
   }

   @NotNull
   @Generated
   public String getFormat() {
      return this.format;
   }

   @NotNull
   @Generated
   public TabComponent getFormatComponent() {
      return this.formatComponent;
   }
}
