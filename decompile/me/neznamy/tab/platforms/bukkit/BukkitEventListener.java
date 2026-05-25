package me.neznamy.tab.platforms.bukkit;

import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class BukkitEventListener implements EventListener<Player>, Listener {
   @EventHandler(priority = EventPriority.HIGHEST)
   public void onQuit(PlayerQuitEvent e) {
      this.quit(e.getPlayer().getUniqueId());
   }

   @EventHandler(priority = EventPriority.LOW)
   public void onJoin(PlayerJoinEvent e) {
      this.join(e.getPlayer());
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onWorldChange(PlayerChangedWorldEvent e) {
      this.worldChange(e.getPlayer().getUniqueId(), e.getPlayer().getWorld().getName());
   }

   @NotNull
   public TabPlayer createPlayer(@NotNull Player player) {
      return new BukkitTabPlayer((BukkitPlatform)TAB.getInstance().getPlatform(), player);
   }
}
