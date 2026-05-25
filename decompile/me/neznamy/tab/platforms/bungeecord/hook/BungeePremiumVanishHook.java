package me.neznamy.tab.platforms.bungeecord.hook;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.hook.PremiumVanishHook;
import me.neznamy.tab.shared.platform.Platform;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

public class BungeePremiumVanishHook extends PremiumVanishHook {
   private final boolean canSeeEnabled;

   public BungeePremiumVanishHook(@NotNull Platform platform) {
      if (ReflectionUtils.methodExists(BungeeVanishAPI.class, "canSee", ProxiedPlayer.class, ProxiedPlayer.class)) {
         this.canSeeEnabled = true;
      } else {
         this.canSeeEnabled = false;
         platform.logWarn(
            new TabTextComponent(
               "Detected an outdated version of PremiumVanish with limited API. Vanish compatibility may not work as expected. Update PremiumVanish to version 2.7.11+ for optimal experience.",
               TabTextColor.RED
            )
         );
      }
   }

   @Override
   public boolean canSee(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
      return this.canSeeEnabled && BungeeVanishAPI.canSee(((BungeeTabPlayer)viewer).getPlayer(), ((BungeeTabPlayer)target).getPlayer());
   }

   @Override
   public boolean isVanished(@NotNull TabPlayer player) {
      try {
         return BungeeVanishAPI.isInvisible(((BungeeTabPlayer)player).getPlayer());
      } catch (IllegalStateException var3) {
         return false;
      }
   }
}
