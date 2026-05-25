package me.neznamy.tab.platforms.bukkit.features;

import java.util.List;
import java.util.Map.Entry;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PerWorldPlayerListConfiguration;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.features.types.UnLoadable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PerWorldPlayerList extends TabFeature implements Listener, Loadable, UnLoadable {
   @NotNull
   private final BukkitPlatform platform;
   @NotNull
   private final PerWorldPlayerListConfiguration configuration;

   public PerWorldPlayerList(@NotNull JavaPlugin plugin, @NotNull BukkitPlatform platform, @NotNull PerWorldPlayerListConfiguration configuration) {
      this.configuration = configuration;
      this.platform = platform;
      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   @Override
   public void load() {
      for (Player p : this.platform.getOnlinePlayers()) {
         this.checkPlayer(p);
      }
   }

   @Override
   public void unload() {
      for (Player p : this.platform.getOnlinePlayers()) {
         for (Player pl : this.platform.getOnlinePlayers()) {
            p.showPlayer(pl);
         }
      }

      HandlerList.unregisterAll(this);
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent e) {
      long time = System.nanoTime();
      this.checkPlayer(e.getPlayer());
      TAB.getInstance().getCPUManager().addTime(this.getFeatureName(), "Player Join", System.nanoTime() - time);
   }

   @EventHandler
   public void onWorldChange(PlayerChangedWorldEvent e) {
      long time = System.nanoTime();
      this.checkPlayer(e.getPlayer());
      TAB.getInstance().getCPUManager().addTime(this.getFeatureName(), "World Switch", System.nanoTime() - time);
   }

   private void checkPlayer(@NotNull Player p) {
      for (Player all : this.platform.getOnlinePlayers()) {
         if (all != p) {
            if (!this.shouldSee(p, all) && p.canSee(all)) {
               p.hidePlayer(all);
            }

            if (this.shouldSee(p, all) && !p.canSee(all)) {
               p.showPlayer(all);
            }

            if (!this.shouldSee(all, p) && all.canSee(p)) {
               all.hidePlayer(p);
            }

            if (this.shouldSee(all, p) && !all.canSee(p)) {
               all.showPlayer(p);
            }
         }
      }
   }

   private boolean shouldSee(@NotNull Player viewer, @NotNull Player target) {
      if (target == viewer) {
         return true;
      }

      if ((!this.configuration.isAllowBypassPermission() || !viewer.hasPermission("tab.bypass"))
         && !this.configuration.getIgnoredWorlds().contains(viewer.getWorld().getName())) {
         String viewerWorldGroup = viewer.getWorld().getName() + "-default";
         String targetWorldGroup = target.getWorld().getName() + "-default";

         for (Entry<String, List<String>> group : this.configuration.getSharedWorlds().entrySet()) {
            if (group.getValue() != null) {
               if (group.getValue().contains(viewer.getWorld().getName())) {
                  viewerWorldGroup = group.getKey();
               }

               if (group.getValue().contains(target.getWorld().getName())) {
                  targetWorldGroup = group.getKey();
               }
            }
         }

         return viewerWorldGroup.equals(targetWorldGroup);
      } else {
         return true;
      }
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return "Per world PlayerList";
   }
}
