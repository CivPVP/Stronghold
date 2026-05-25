package me.neznamy.tab.shared.backend;

import java.util.UUID;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.hook.ViaVersionHook;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class BackendTabPlayer extends TabPlayer {
   private boolean vanished;
   private long lastVanishCheck;

   protected BackendTabPlayer(
      @NotNull BackendPlatform platform, @NotNull Object player, @NotNull UUID uniqueId, @NotNull String name, @NotNull String world, int serverVersion
   ) {
      super(
         platform,
         player,
         uniqueId,
         name,
         TAB.getInstance().getConfiguration().getConfig().getServerName(),
         world,
         ViaVersionHook.getInstance().getPlayerVersion(uniqueId, name, serverVersion),
         true
      );
   }

   @Override
   public boolean isVanished() {
      long currentTime = System.currentTimeMillis();
      if (currentTime - this.lastVanishCheck >= 900L) {
         this.lastVanishCheck = currentTime;
         this.vanished = this.isVanished0();
      }

      return this.vanished;
   }

   public abstract double getHealth();

   public abstract String getDisplayName();

   public abstract boolean isVanished0();

   public abstract int getDeaths();
}
