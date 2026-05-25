package me.neznamy.tab.shared.features.proxy;

import java.util.UUID;
import lombok.Generated;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.features.belowname.BelowNameProxyPlayerData;
import me.neznamy.tab.shared.features.nametags.NameTagProxyPlayerData;
import me.neznamy.tab.shared.features.playerlist.PlayerListProxyPlayerData;
import me.neznamy.tab.shared.features.playerlistobjective.PlayerListObjectiveProxyPlayerData;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProxyPlayer {
   @NotNull
   private final UUID uniqueId;
   @NotNull
   private final UUID tablistId;
   @NotNull
   private final String name;
   @NotNull
   private String nickname;
   @NotNull
   public Server server;
   private boolean vanished;
   private final boolean staff;
   @Nullable
   private BelowNameProxyPlayerData belowname;
   @Nullable
   private PlayerListObjectiveProxyPlayerData playerlist;
   @Nullable
   private final TabList.Skin skin;
   @Nullable
   private PlayerListProxyPlayerData tabFormat;
   @Nullable
   private NameTagProxyPlayerData nametag;
   @NotNull
   private ProxyPlayer.ConnectionState connectionState = ProxyPlayer.ConnectionState.QUEUED;

   public ProxyPlayer(
      @NotNull UUID uniqueId,
      @NotNull UUID tablistId,
      @NotNull String name,
      @NotNull Server server,
      boolean vanished,
      boolean staff,
      @Nullable TabList.Skin skin
   ) {
      this.uniqueId = uniqueId;
      this.tablistId = tablistId;
      this.name = name;
      this.nickname = name;
      this.server = server;
      this.vanished = vanished;
      this.staff = staff;
      this.skin = skin;
   }

   @NotNull
   public TabList.Entry asEntry() {
      return new TabList.Entry(
         this.uniqueId, this.nickname, this.skin, true, 0, 0, this.tabFormat == null ? null : this.tabFormat.getFormatComponent(), 0, true
      );
   }

   @NotNull
   @Generated
   public UUID getUniqueId() {
      return this.uniqueId;
   }

   @NotNull
   @Generated
   public UUID getTablistId() {
      return this.tablistId;
   }

   @NotNull
   @Generated
   public String getName() {
      return this.name;
   }

   @NotNull
   @Generated
   public String getNickname() {
      return this.nickname;
   }

   @NotNull
   @Generated
   public Server getServer() {
      return this.server;
   }

   @Generated
   public boolean isVanished() {
      return this.vanished;
   }

   @Generated
   public boolean isStaff() {
      return this.staff;
   }

   @Nullable
   @Generated
   public BelowNameProxyPlayerData getBelowname() {
      return this.belowname;
   }

   @Nullable
   @Generated
   public PlayerListObjectiveProxyPlayerData getPlayerlist() {
      return this.playerlist;
   }

   @Nullable
   @Generated
   public TabList.Skin getSkin() {
      return this.skin;
   }

   @Nullable
   @Generated
   public PlayerListProxyPlayerData getTabFormat() {
      return this.tabFormat;
   }

   @Nullable
   @Generated
   public NameTagProxyPlayerData getNametag() {
      return this.nametag;
   }

   @NotNull
   @Generated
   public ProxyPlayer.ConnectionState getConnectionState() {
      return this.connectionState;
   }

   @Generated
   public void setNickname(@NotNull String nickname) {
      if (nickname == null) {
         throw new NullPointerException("nickname is marked non-null but is null");
      }

      this.nickname = nickname;
   }

   @Generated
   public void setServer(@NotNull Server server) {
      if (server == null) {
         throw new NullPointerException("server is marked non-null but is null");
      }

      this.server = server;
   }

   @Generated
   public void setVanished(boolean vanished) {
      this.vanished = vanished;
   }

   @Generated
   public void setBelowname(@Nullable BelowNameProxyPlayerData belowname) {
      this.belowname = belowname;
   }

   @Generated
   public void setPlayerlist(@Nullable PlayerListObjectiveProxyPlayerData playerlist) {
      this.playerlist = playerlist;
   }

   @Generated
   public void setTabFormat(@Nullable PlayerListProxyPlayerData tabFormat) {
      this.tabFormat = tabFormat;
   }

   @Generated
   public void setNametag(@Nullable NameTagProxyPlayerData nametag) {
      this.nametag = nametag;
   }

   @Generated
   public void setConnectionState(@NotNull ProxyPlayer.ConnectionState connectionState) {
      if (connectionState == null) {
         throw new NullPointerException("connectionState is marked non-null but is null");
      }

      this.connectionState = connectionState;
   }

   public enum ConnectionState {
      CONNECTED,
      QUEUED,
      DISCONNECTED;
   }
}
