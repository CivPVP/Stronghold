package me.neznamy.tab.shared.features.nametags;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import java.util.Collections;
import java.util.UUID;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.QueuedData;
import me.neznamy.tab.shared.features.proxy.message.ProxyMessage;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NameTagProxyPlayerData extends ProxyMessage {
   @NotNull
   private final NameTag feature;
   private final long id;
   @NotNull
   private final UUID playerId;
   @NotNull
   private final String teamName;
   @NotNull
   private final String prefix;
   @NotNull
   private final String suffix;
   @NotNull
   private final Scoreboard.NameVisibility nameVisibility;
   @Nullable
   private String resolvedTeamName;

   public NameTagProxyPlayerData(@NotNull ByteArrayDataInput in, @NotNull NameTag feature) {
      this.feature = feature;
      this.id = in.readLong();
      this.playerId = this.readUUID(in);
      this.teamName = in.readUTF();
      this.prefix = in.readUTF();
      this.suffix = in.readUTF();
      this.nameVisibility = Scoreboard.NameVisibility.getByName(in.readUTF());
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
      out.writeUTF(this.teamName);
      out.writeUTF(this.prefix);
      out.writeUTF(this.suffix);
      out.writeUTF(this.nameVisibility.toString());
   }

   @Override
   public void process(@NotNull ProxySupport proxySupport) {
      ProxyPlayer target = proxySupport.getProxyPlayers().get(this.playerId);
      if (target == null) {
         this.unknownPlayer(this.playerId.toString(), "nametag update update");
         QueuedData data = proxySupport.getQueuedData().computeIfAbsent(this.playerId, k -> new QueuedData());
         if (data.getNametag() == null || data.getNametag().id < this.id) {
            this.resolvedTeamName = this.checkTeamName(null, this.teamName.substring(0, this.teamName.length() - 1));
            data.setNametag(this);
         }
      } else if (target.getNametag() != null && target.getNametag().id > this.id) {
         TAB.getInstance().debug("Dropping nametag update action for player " + target.getName() + " due to newer action already being present");
      } else {
         NameTagProxyPlayerData oldData = target.getNametag();
         this.resolvedTeamName = this.checkTeamName(target, this.teamName.substring(0, this.teamName.length() - 1));
         target.setNametag(this);
         if (target.getConnectionState() == ProxyPlayer.ConnectionState.CONNECTED) {
            TabComponent prefix = this.feature.getPrefixCache().get(this.prefix);
            TabComponent lastColor = this.feature.getLastColorCache().get(this.prefix);
            TabComponent suffix = this.feature.getSuffixCache().get(this.suffix);

            for (TabPlayer viewer : this.feature.getOnlinePlayers().getPlayers()) {
               if (oldData != null && this.resolvedTeamName.equals(oldData.resolvedTeamName)) {
                  viewer.getScoreboard()
                     .updateTeam(
                        oldData.teamName,
                        prefix,
                        suffix,
                        this.nameVisibility,
                        Scoreboard.CollisionRule.ALWAYS,
                        this.feature.getTeamOptions(),
                        lastColor.getLastStyle().toEnumChatFormat()
                     );
               } else {
                  if (oldData != null) {
                     viewer.getScoreboard().unregisterTeam(oldData.resolvedTeamName);
                  }

                  viewer.getScoreboard()
                     .registerTeam(
                        this.resolvedTeamName,
                        prefix,
                        suffix,
                        this.nameVisibility,
                        Scoreboard.CollisionRule.ALWAYS,
                        Collections.singletonList(target.getNickname()),
                        this.feature.getTeamOptions(),
                        lastColor.getLastStyle().toEnumChatFormat()
                     );
               }
            }
         }
      }
   }

   @NotNull
   private String checkTeamName(@Nullable ProxyPlayer player, @NotNull String currentName15) {
      char id = 'A';

      while (true) {
         String potentialTeamName = currentName15 + id;
         boolean nameTaken = false;

         for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (potentialTeamName.equals(all.sortingData.shortTeamName)) {
               nameTaken = true;
               break;
            }
         }

         if (!nameTaken && this.feature.getProxy() != null) {
            for (ProxyPlayer all : this.feature.getProxy().getProxyPlayers().values()) {
               if (all != player && all.getNametag() != null && potentialTeamName.equals(all.getNametag().teamName)) {
                  nameTaken = true;
                  break;
               }
            }
         }

         if (!nameTaken) {
            return potentialTeamName;
         }

         id++;
      }
   }

   @Generated
   public NameTagProxyPlayerData(
      @NotNull NameTag feature,
      long id,
      @NotNull UUID playerId,
      @NotNull String teamName,
      @NotNull String prefix,
      @NotNull String suffix,
      @NotNull Scoreboard.NameVisibility nameVisibility
   ) {
      if (feature == null) {
         throw new NullPointerException("feature is marked non-null but is null");
      }

      if (playerId == null) {
         throw new NullPointerException("playerId is marked non-null but is null");
      }

      if (teamName == null) {
         throw new NullPointerException("teamName is marked non-null but is null");
      }

      if (prefix == null) {
         throw new NullPointerException("prefix is marked non-null but is null");
      }

      if (suffix == null) {
         throw new NullPointerException("suffix is marked non-null but is null");
      }

      if (nameVisibility == null) {
         throw new NullPointerException("nameVisibility is marked non-null but is null");
      }

      this.feature = feature;
      this.id = id;
      this.playerId = playerId;
      this.teamName = teamName;
      this.prefix = prefix;
      this.suffix = suffix;
      this.nameVisibility = nameVisibility;
   }

   @Generated
   @Override
   public String toString() {
      return "NameTagProxyPlayerData(id="
         + this.getId()
         + ", playerId="
         + this.getPlayerId()
         + ", teamName="
         + this.getTeamName()
         + ", prefix="
         + this.getPrefix()
         + ", suffix="
         + this.getSuffix()
         + ", nameVisibility="
         + this.getNameVisibility()
         + ", resolvedTeamName="
         + this.getResolvedTeamName()
         + ")";
   }

   @NotNull
   @Generated
   public NameTag getFeature() {
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
   public String getTeamName() {
      return this.teamName;
   }

   @NotNull
   @Generated
   public String getPrefix() {
      return this.prefix;
   }

   @NotNull
   @Generated
   public String getSuffix() {
      return this.suffix;
   }

   @NotNull
   @Generated
   public Scoreboard.NameVisibility getNameVisibility() {
      return this.nameVisibility;
   }

   @Nullable
   @Generated
   public String getResolvedTeamName() {
      return this.resolvedTeamName;
   }
}
