package me.neznamy.tab.shared.hook;

import java.util.UUID;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

public class FloodgateHook {
   private static final FloodgateHook instance = new FloodgateHook();
   private final boolean installed = ReflectionUtils.classExists("org.geysermc.floodgate.api.FloodgateApi");

   public boolean isFloodgatePlayer(@NotNull UUID uniqueId, @NotNull String name) {
      if (!this.installed) {
         return false;
      } else if (FloodgateApi.getInstance() == null) {
         TAB.getInstance().debug("Floodgate is installed, but API returned null. Could not check player " + name);
         return false;
      } else {
         boolean bedrock = FloodgateApi.getInstance().isFloodgatePlayer(uniqueId);
         TAB.getInstance().debug("Floodgate returned bedrock status " + String.valueOf(bedrock).toUpperCase() + " for player " + name);
         return bedrock;
      }
   }

   @Generated
   public static FloodgateHook getInstance() {
      return instance;
   }
}
