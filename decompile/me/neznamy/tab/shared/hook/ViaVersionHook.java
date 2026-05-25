package me.neznamy.tab.shared.hook;

import com.viaversion.viaversion.api.Via;
import java.util.UUID;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

public class ViaVersionHook {
   private static final ViaVersionHook instance = new ViaVersionHook();
   private final boolean installed = ReflectionUtils.classExists("com.viaversion.viaversion.api.Via");

   public int getPlayerVersion(@NotNull UUID player, @NotNull String playerName, int serverVersion) {
      if (!this.installed) {
         return serverVersion;
      }

      int version;
      try {
         version = Via.getAPI().getPlayerVersion(player);
      } catch (IllegalArgumentException e) {
         return serverVersion;
      }

      if (version == -1) {
         return serverVersion;
      }

      TAB.getInstance().debug("ViaVersion returned protocol version " + version + " for " + playerName);
      return version;
   }

   @Generated
   public static ViaVersionHook getInstance() {
      return instance;
   }

   @Generated
   public boolean isInstalled() {
      return this.installed;
   }
}
