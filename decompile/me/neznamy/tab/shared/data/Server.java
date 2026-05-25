package me.neznamy.tab.shared.data;

import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Server {
   @NonNull
   private final String name;
   private boolean isSpyServer;
   @Nullable
   private ServerGroup serverGroup;

   Server(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      this.name = name;
      this.serverGroup = TAB.getInstance().getDataManager().computeServerGroup(this);
   }

   @Contract("!null -> !null")
   public static Server byName(@Nullable String name) {
      return name == null ? null : TAB.getInstance().getDataManager().getServers().computeIfAbsent(name, Server::new);
   }

   public void markSpyServer() {
      this.isSpyServer = true;
   }

   public boolean canSee(@NotNull Server other) {
      return this.isSpyServer || this.serverGroup == other.serverGroup;
   }

   @NonNull
   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public boolean isSpyServer() {
      return this.isSpyServer;
   }

   @Nullable
   @Generated
   public ServerGroup getServerGroup() {
      return this.serverGroup;
   }

   @Generated
   public void setServerGroup(@Nullable ServerGroup serverGroup) {
      this.serverGroup = serverGroup;
   }
}
