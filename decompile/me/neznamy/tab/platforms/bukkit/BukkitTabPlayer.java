package me.neznamy.tab.platforms.bukkit;

import me.neznamy.tab.platforms.bukkit.hook.LibsDisguisesHook;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.chat.component.TabComponent;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class BukkitTabPlayer extends BackendTabPlayer {
   public BukkitTabPlayer(@NotNull BukkitPlatform platform, @NotNull Player p) {
      super(platform, p, p.getUniqueId(), p.getName(), p.getWorld().getName(), platform.getServerVersion().getNetworkId());
   }

   @Override
   public boolean hasPermission(@NotNull String permission) {
      return this.getPlayer().hasPermission(permission);
   }

   @Override
   public int getPing() {
      return this.getPlatform().getImplementationProvider().getPing(this);
   }

   @Override
   public void sendMessage(@NotNull TabComponent message) {
      this.getPlayer().sendMessage(this.getPlatform().toBukkitFormat(message));
   }

   @Override
   public boolean hasInvisibilityPotion() {
      return this.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY);
   }

   @Override
   public boolean isDisguised() {
      return LibsDisguisesHook.isDisguised(this);
   }

   @NotNull
   public Player getPlayer() {
      return (Player)this.player;
   }

   public BukkitPlatform getPlatform() {
      return (BukkitPlatform)this.platform;
   }

   @Override
   public boolean isVanished0() {
      for (MetadataValue v : this.getPlayer().getMetadata("vanished")) {
         if (v.asBoolean()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public int getDeaths() {
      return this.getPlayer().getStatistic(Statistic.DEATHS);
   }

   @Override
   public int getGamemode() {
      return this.getPlayer().getGameMode().getValue();
   }

   @Override
   public double getHealth() {
      return this.getPlayer().getHealth();
   }

   @NotNull
   @Override
   public String getDisplayName() {
      return this.getPlayer().getDisplayName();
   }
}
