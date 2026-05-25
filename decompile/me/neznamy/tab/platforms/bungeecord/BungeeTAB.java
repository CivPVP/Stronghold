package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeTAB extends Plugin {
   public void onEnable() {
      if (this.isCompatible()) {
         TAB.create(new BungeePlatform(this));
      } else {
         this.logIncompatibleVersionWarning();
      }
   }

   private boolean isCompatible() {
      return ReflectionUtils.classExists("net.md_5.bungee.api.chat.ObjectComponent");
   }

   private void logIncompatibleVersionWarning() {
      int buildNumber = 2000;
      String releaseDate = "September 30th, 2025";
      String oldTabVersion = "5.2.5";
      this.getLogger().warning("§c====================================================================================================");
      this.getLogger()
         .warning(
            String.format("§cThe plugin requires BungeeCord build #%d (released on %s) and up (or an equivalent fork) to work.", buildNumber, releaseDate)
         );
      this.getLogger()
         .warning(
            String.format(
               "§cIf you are using a fork that did not update to the new BungeeCord version yet, stay on TAB v%s, which supports older builds.", oldTabVersion
            )
         );
      this.getLogger().warning("§c====================================================================================================");
   }

   public void onDisable() {
      if (TAB.getInstance() != null) {
         TAB.getInstance().unload();
      }
   }
}
