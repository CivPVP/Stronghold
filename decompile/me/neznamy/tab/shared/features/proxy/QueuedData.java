package me.neznamy.tab.shared.features.proxy;

import lombok.Generated;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.features.belowname.BelowNameProxyPlayerData;
import me.neznamy.tab.shared.features.nametags.NameTagProxyPlayerData;
import me.neznamy.tab.shared.features.playerlist.PlayerListProxyPlayerData;
import me.neznamy.tab.shared.features.playerlistobjective.PlayerListObjectiveProxyPlayerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QueuedData {
   @Nullable
   private BelowNameProxyPlayerData belowname;
   @Nullable
   private PlayerListObjectiveProxyPlayerData playerlist;
   @Nullable
   private PlayerListProxyPlayerData tabFormat;
   @Nullable
   private NameTagProxyPlayerData nametag;
   private boolean vanished;
   @NotNull
   public Server server;

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
   public PlayerListProxyPlayerData getTabFormat() {
      return this.tabFormat;
   }

   @Nullable
   @Generated
   public NameTagProxyPlayerData getNametag() {
      return this.nametag;
   }

   @Generated
   public boolean isVanished() {
      return this.vanished;
   }

   @NotNull
   @Generated
   public Server getServer() {
      return this.server;
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
   public void setVanished(boolean vanished) {
      this.vanished = vanished;
   }

   @Generated
   public void setServer(@NotNull Server server) {
      if (server == null) {
         throw new NullPointerException("server is marked non-null but is null");
      }

      this.server = server;
   }
}
